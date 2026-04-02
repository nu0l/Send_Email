package com.tool.send_email.model;

import com.tool.send_email.utils.RandomUtils;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 邮件实体类
 */

@Data
public class Email {
    private List<String> to; // 收件人
    private String subject; // 邮件主题
    private String content; // 邮件内容（支持 HTML 格式）
    private Map<String, String> attachments; // 附件路径列表（可选）
    /** 发送来源，用于统计：SEND / WEB / GUI / CUSTOM 等 */
    private String activitySource = "SEND";

    // Forgery 发件人（可选，用于本地研究测试）
    private String forgeFromEmail;
    private String forgeFromNickname;

    // Reply-To（可选，用于本地研究测试）
    private String replyToEmail;
    private String replyToNickname;

    public Email() {
    }

    public Email(List<String> to, String subject, String content, Map<String, String> attachments) {
        this(to, subject, content, attachments, "SEND");
    }

    public Email(List<String> to, String subject, String content, Map<String, String> attachments, String activitySource) {
        this.to = to;
        this.subject = subject;
        this.attachments = attachments;
        this.activitySource = activitySource != null ? activitySource : "SEND";
        // 对 HTML 正文进行更强随机“混淆/反检测”处理（防止每次正文完全一致）
        this.content = RandomUtils.obfuscateHtml(content, this.activitySource);
    }
}
