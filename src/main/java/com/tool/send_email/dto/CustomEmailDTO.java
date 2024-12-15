package com.tool.send_email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 自定义邮件发送参数，用于Swagger获取参数
 */

@Data
public class CustomEmailDTO {
    @Schema(description = "模板文件的路径", example = "/path/to/template.html", required = true)
    private String templatePath;

    @Schema(description = "CSV文件的路径", example = "/path/to/csv.csv", required = true)
    private String csvPath;

    @Schema(description = "附件列表", example = "[{\"fileName\": \"attachment1.jpg\", \"filePath\": \"/path/to/attachment1.jpg\"}]", required = false)
    private List<Map<String, String>> attachments;

}
