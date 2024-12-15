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

}
