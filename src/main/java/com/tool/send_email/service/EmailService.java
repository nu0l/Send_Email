package com.tool.send_email.service;

import com.tool.send_email.config.MailConfig;
import com.tool.send_email.model.Email;
import com.tool.send_email.model.EmailStatusCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 邮件发送服务
 */

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    private List<JavaMailSender> mailSenders;
    @Autowired
    private MailConfig mailConfig;
    @Autowired
    private SendActivityLogService sendActivityLogService;
    private EmailStatusCallback callback;
    // 用于轮询分配发送账户，减少每封邮件都从 index=0 开始导致的集中失败/集中延迟
    private final AtomicInteger senderCursor = new AtomicInteger(0);

    /**
     * 异步发送邮件
     *
     * @param email Email 对象，封装了收件人、主题、内容等信息
     * @throws MessagingException
     */
    @Async
    public CompletableFuture<Boolean> sendEmail(Email email) throws MessagingException {
        List<String> failList = new ArrayList<>();

        if (email == null) {
            notifyStatus("邮件发送失败: Email 参数为空");
            return CompletableFuture.completedFuture(false);
        }

        List<String> recipients = email.getTo();
        if (recipients == null || recipients.isEmpty()) {
            notifyStatus("邮件发送失败: 收件人为空");
            return CompletableFuture.completedFuture(false);
        }

        // 发送前校验附件路径是否存在，避免无意义的多账户重试
        if (!validateAttachments(email.getAttachments())) {
            return CompletableFuture.completedFuture(false);
        }

        logger.info("开始发送邮件，收件人数: {}", recipients.size());
        for (String to : recipients) {
            boolean emailSent = sendEmailWithRetry(email, to, email.getSubject(), email.getContent(), email.getAttachments());
            if (!emailSent) failList.add(to);
        }

        // 记录所有失败的收件人
        boolean success = failList.isEmpty();
        String src = email.getActivitySource() != null ? email.getActivitySource() : "SEND";
        String detail = success ? null : "以下收件人邮件发送失败：" + String.join(", ", failList);
        try {
            sendActivityLogService.record(
                    src,
                    email.getSubject(),
                    email.getTo() != null ? email.getTo().size() : 0,
                    success,
                    detail
            );
        } catch (Exception ex) {
            logger.warn("写入活动记录失败: {}", ex.getMessage());
        }

        if (!success) {
            String failListStr = "以下收件人邮件发送失败：" + String.join(", ", failList);
            logger.error(failListStr);
            notifyStatus(failListStr);
            return CompletableFuture.completedFuture(false);
        }
        logger.info("所有邮件发送成功");
        return CompletableFuture.completedFuture(true);
    }

    /**
     * 发送邮件并在失败时尝试下一个邮箱配置
     *
     * @param to          收件人
     * @param subject     邮件主题
     * @param text        邮件内容
     * @param attachments 附件，键为文件名，值为文件路径
     * @throws MessagingException
     */
    private boolean sendEmailWithRetry(Email email,
                                        String to,
                                        String subject,
                                        String text,
                                        Map<String, String> attachments) {
        int usable = resolveUsableSenderCount();
        if (usable <= 0) {
            logger.error("未配置可用的邮件发送账户（mailSenders/accounts 不匹配）。");
            notifyStatus("未配置可用的邮件发送账户");
            return false;
        }

        // 本地研究测试：
        // - 不改动 From 邮箱（避免 553/535），只允许改显示昵称（display name）
        // - 支持设置 Reply-To
        String forgeFromNickname = (email.getForgeFromNickname() != null && !email.getForgeFromNickname().trim().isEmpty())
                ? email.getForgeFromNickname().trim() : null;
        String replyToEmail = (email.getReplyToEmail() != null && !email.getReplyToEmail().trim().isEmpty())
                ? email.getReplyToEmail().trim() : null;
        String replyToNickname = (email.getReplyToNickname() != null && !email.getReplyToNickname().trim().isEmpty())
                ? email.getReplyToNickname().trim() : null;

        int startIndex = Math.floorMod(senderCursor.getAndIncrement(), usable);
        for (int attempt = 0; attempt < usable; attempt++) {
            int index = (startIndex + attempt) % usable;
            String from = null;
            try {
                // 失败后才延迟，减少成功时的额外等待
                if (attempt > 0) {
                    jitterDelay(attempt);
                }

                from = mailConfig.getAccounts().get(index).getFrom();

                String nickname = (forgeFromNickname != null)
                        ? forgeFromNickname
                        : mailConfig.getAccounts().get(index).getNickname();

                if (!sendOnce(to, subject, text, attachments, index, from, nickname, replyToEmail, replyToNickname)) {
                    // sendOnce 失败但不抛异常时，视为失败继续下一账户
                    logger.warn("邮件发送未成功（非异常）: {} -> {}, senderIndex={}", from, to, index);
                    continue;
                }

                logger.info("邮件发送成功: {} -> {}", from, to);
                notifyStatus(String.format("邮件发送成功: %s -> %s", from, to));
                return true;
            } catch (Exception e) {
                logger.error("邮件发送失败: {} -> {}, senderIndex={}, 尝试下一个邮箱配置。错误信息: {}",
                        from, to, index, e.getMessage());

                // Forgery 情况下，很多邮件服务端会对 MAIL FROM 做硬校验。
                // 如果明确是 “Mail from must equal authorized user” 之类拒收，则切换其它账号也仍会失败，
                // 并且会触发更多 SMTP 登录/风控，故直接停止重试。
                if (shouldStopRetryForSendException(e)) {
                    notifyStatus("邮件发送失败（无需重试）：" + (e.getMessage() != null ? e.getMessage() : e.toString()));
                    return false;
                }
                if (attempt == usable - 1) {
                    notifyStatus(String.format("邮件发送失败（已全部尝试）：%s -> %s", from, to));
                }
                // 继续下一账户
            }
        }

        notifyStatus(String.format("所有邮件账户均无法发送邮件给: %s", to));
        return false;
    }

    private boolean shouldStopRetryForSendException(Exception e) {
        String msg = (e == null) ? "" : String.valueOf(e.getMessage());
        if (msg == null || msg.trim().isEmpty()) {
            msg = String.valueOf(e);
        }
        String s = msg.toLowerCase();
        // 典型：553 Mail from must equal authorized user
        if (s.contains("mail from must equal authorized user")) return true;
        if (s.contains("553") && s.contains("mail from")) return true;

        // 典型：QQ SMTP 535 登录失败且包含频率限制/账号异常时继续重试会更容易触发风控
        if (s.contains("535") && (s.contains("login frequency limited") || s.contains("frequency limited") || s.contains("account is abnormal"))) {
            return true;
        }
        if (s.contains("authenticationfailedexception") && (s.contains("login frequency") || s.contains("frequency limited"))) {
            return true;
        }

        return false;
    }

    private int resolveUsableSenderCount() {
        int senderCount = (mailSenders == null) ? 0 : mailSenders.size();
        int accountCount = (mailConfig == null || mailConfig.getAccounts() == null) ? 0 : mailConfig.getAccounts().size();
        return Math.min(senderCount, accountCount);
    }

    private boolean validateAttachments(Map<String, String> attachments) {
        if (attachments == null || attachments.isEmpty()) return true;
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            String filePath = entry.getValue();
            if (filePath == null || filePath.trim().isEmpty()) {
                logger.error("附件路径为空: {}", entry.getKey());
                notifyStatus("附件路径为空，无法发送");
                return false;
            }
            File f = new File(filePath);
            if (!f.exists() || !f.isFile()) {
                logger.error("附件不存在: {} -> {}", entry.getKey(), filePath);
                notifyStatus("附件不存在: " + entry.getKey());
                return false;
            }
        }
        return true;
    }

    private boolean sendOnce(String to,
                              String subject,
                              String text,
                              Map<String, String> attachments,
                              int index,
                              String from,
                              String nickname,
                              String replyToEmail,
                              String replyToNickname) throws MessagingException, java.io.UnsupportedEncodingException, java.io.IOException {
        JavaMailSender mailSender = mailSenders.get(index);
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        mimeMessage.setFrom(new InternetAddress(from, nickname, "UTF-8"));
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        mimeMessage.setSubject(subject, "UTF-8");
        if (replyToEmail != null) {
            // 测试 Reply-To 行为：不影响 SMTP 信封校验
            if (replyToNickname != null && !replyToNickname.isEmpty()) {
                mimeMessage.setReplyTo(new InternetAddress[]{new InternetAddress(replyToEmail, replyToNickname, "UTF-8")});
            } else {
                mimeMessage.setReplyTo(new InternetAddress[]{new InternetAddress(replyToEmail)});
            }
        }

        MimeMultipart multipart = new MimeMultipart("mixed");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(text, "text/html; charset=UTF-8");
        textPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
        multipart.addBodyPart(textPart);

        if (attachments != null && !attachments.isEmpty()) {
            for (Map.Entry<String, String> entry : attachments.entrySet()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(new File(entry.getValue()));
                attachmentPart.setFileName(MimeUtility.encodeText(entry.getKey(), "UTF-8", null));
                multipart.addBodyPart(attachmentPart);
            }
        }

        mimeMessage.setContent(multipart);

        // 清理邮件头部（减少一些指纹）
        mimeMessage.removeHeader("Received");
        mimeMessage.removeHeader("X-Originating-IP");

        // 发送邮件
        mailSender.send(mimeMessage);
        return true;
    }

    /**
     * 在失败重试前，加入一个随机延迟，延迟随 attempt 增长做轻微放大
     */
    private void jitterDelay(int attempt) throws InterruptedException {
        // 基础 800-2000ms，失败越多延迟越大但封顶
        long base = 800L + ThreadLocalRandom.current().nextLong(1200L);
        long extra = (attempt > 0) ? Math.min(2000L, attempt * 250L) : 0L;
        long delay = base + extra;
        logger.info("重试前随机延迟: {} 毫秒（attempt={})", delay, attempt);
        // 避免刷屏：仅在回调存在时通知少量信息
        if (callback != null) {
            notifyStatus(String.format("重试前延迟: %dms", delay));
        }
        TimeUnit.MILLISECONDS.sleep(delay);
    }

    /**
     * 设置邮件发送状态回调接口
     *
     * @param callback
     */
    public void setEmailStatusCallback(EmailStatusCallback callback) {
        this.callback = callback; // 设置回调接口
    }

    /**
     * 通知界面更新邮件发送状态
     *
     * @param message
     */
    private void notifyStatus(String message) {
        if (callback != null) {
            logger.info("通知回调: " + message);
            callback.onStatusUpdate(message); // 调用回调接口的方法来通知界面
        }
    }
}