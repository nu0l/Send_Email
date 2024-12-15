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
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 邮件发送服务
 */

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final List<String> faillist = new ArrayList<>(); // 用于记录发送失败的收件人
    @Autowired
    private List<JavaMailSender> mailSenders;
    @Autowired
    private MailConfig mailConfig;
    private EmailStatusCallback callback;

    /**
     * 异步发送邮件
     *
     * @param email Email 对象，封装了收件人、主题、内容等信息
     * @throws MessagingException
     */
    @Async
    public CompletableFuture<Boolean> sendEmail(Email email) throws MessagingException {
        synchronized (faillist) {
            faillist.clear(); // 清空列表
        }

        logger.info("开始发送邮件");
        for (String to : email.getTo()) {
            boolean emailSent = sendEmailWithRetry(to, email.getSubject(), email.getContent(), email.getAttachments(), 0); // 从第一个邮件配置开始尝试
            if (!emailSent) {
                faillist.add(to); // 如果邮件发送失败，记录失败的收件人
            }
        }

        // 记录所有失败的收件人
        if (!faillist.isEmpty()) {
            String failListStr = "以下收件人邮件发送失败：" + String.join(", ", faillist);
            logger.error(failListStr);
            notifyStatus(failListStr);
            return CompletableFuture.completedFuture(false);
        } else {
            logger.info("所有邮件发送成功");
            return CompletableFuture.completedFuture(true);
        }
    }

    /**
     * 发送邮件并在失败时尝试下一个邮箱配置
     *
     * @param to          收件人
     * @param subject     邮件主题
     * @param text        邮件内容
     * @param attachments 附件，键为文件名，值为文件路径
     * @param index       当前使用的 mailSender 索引
     * @throws MessagingException
     */
    private boolean sendEmailWithRetry(String to, String subject, String text, Map<String, String> attachments, int index) throws MessagingException {
        // 如果已尝试所有邮件配置，返回失败
        if (index >= mailSenders.size()) {
            logger.error("所有邮件账户均无法发送邮件给: {}", to);
            notifyStatus(String.format("所有邮件账户均无法发送邮件给: %s", to));
            return false;
        }

        JavaMailSender mailSender = mailSenders.get(index); // 获取当前邮件配置
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // 获取当前邮件配置的发件人地址和昵称
        String from = mailConfig.getAccounts().get(index).getFrom();
        String nickname = mailConfig.getAccounts().get(index).getNickname();

        try {
            randomDelay(); // 在每封邮件发送前加入随机延迟

            mimeMessage.setFrom(new InternetAddress(from, nickname, "UTF-8"));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            mimeMessage.setSubject(subject, "UTF-8");
            MimeMultipart multipart = new MimeMultipart("mixed");
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(text, "text/html; charset=UTF-8"); // 设置为 HTML 格式
            textPart.setHeader("Content-Transfer-Encoding", "quoted-printable"); // 设置编码为 quoted-printable
            multipart.addBodyPart(textPart); // 添加邮件正文

            // 添加附件
            if (attachments != null && !attachments.isEmpty()) {
                for (Map.Entry<String, String> entry : attachments.entrySet()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(new File(entry.getValue()));
                    attachmentPart.setFileName(MimeUtility.encodeText(entry.getKey(), "UTF-8", null));
                    multipart.addBodyPart(attachmentPart);
                }
            }

            mimeMessage.setContent(multipart);

            // 清理邮件头部
            mimeMessage.removeHeader("Received");
            mimeMessage.removeHeader("X-Originating-IP");

            // 发送邮件
            mailSender.send(mimeMessage);
            logger.info("邮件发送成功: {} -> {}", from, to);
            notifyStatus(String.format("邮件发送成功: %s -> %s", from, to));
            return true;
        } catch (Exception e) {
            // 邮件发送失败，记录日志并尝试使用下一个配置
            logger.error("邮件发送失败: {} -> {}, 尝试使用下一个邮箱配置。错误信息：\n{}", from, to, e.getMessage());
            notifyStatus(String.format("邮件发送失败: %s -> %s, 尝试使用下一个邮箱配置。错误信息：\n%s", from, to, e.getMessage()));
            // 递归尝试下一个配置
            return sendEmailWithRetry(to, subject, text, attachments, index + 1);
        }
    }

    /**
     * 在每封邮件发送前，加入一个随机延迟，延迟时间为 1秒到5秒之间
     *
     * @throws InterruptedException
     */
    private void randomDelay() throws InterruptedException {
        long delay = 1000L + new Random().nextInt(4000); // 随机延迟 1秒到5秒之间
        logger.info("随机延迟: {} 毫秒", delay);
        notifyStatus(String.format("随机延迟: %d 毫秒", delay));
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