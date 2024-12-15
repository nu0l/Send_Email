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

    public Email() {
    }

    public Email(List<String> to, String subject, String content, Map<String, String> attachments) {
        this.to = to;
        this.subject = subject;
        this.content = RandomUtils.insertInvisibleChars(content); // 插入随机不可见字符
        this.content = RandomUtils.insertHiddenCharacters(this.content); // 插入随机隐藏字符
        //this.content = content;
        this.attachments = attachments;
    }
}
