package com.tool.send_email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 发送邮件的参数，用于Swagger获取参数
 */

@Data
public class SendEmailDTO {
    @Schema(description = "收件人列表", example = "[\"example1@example.com\", \"example2@example.com\"]", required = true)
    private List<String> to;

    @Schema(description = "邮件主题", example = "邮件主题内容", required = true)
    private String subject;

    @Schema(description = "邮件正文", example = "邮件的正文内容", required = true)
    private String content;

    @Schema(description = "附件列表", example = "[{\"fileName\": \"attachment1.jpg\", \"filePath\": \"/path/to/attachment1.jpg\"}]", required = false)
    private List<Map<String, String>> attachments;

    @Schema(description = "投递间隔（毫秒），用于连续发送时的等待时长；0 表示不控制", example = "1000", required = false)
    private Long deliveryIntervalMs;

    @Schema(description = "Forgery 发件人邮箱（本地研究测试用，可选）", example = "forged@example.com", required = false)
    private String forgeFromEmail;

    @Schema(description = "Forgery 发件人昵称（可选）", example = "昵称", required = false)
    private String forgeFromNickname;

    @Schema(description = "Reply-To 邮箱（可选，用于本地研究测试）", example = "replyto@example.com", required = false)
    private String replyToEmail;

    @Schema(description = "Reply-To 昵称（可选）", example = "Reply Nick", required = false)
    private String replyToNickname;

}
