package com.tool.send_email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证模板文件，用于Swagger获取参数
 */

@Data
public class ValidationTemplateDTO {
    @Schema(description = "模板文件的路径", example = "/path/to/template.html", required = true)
    private String templatePath;

    @Schema(description = "CSV文件的路径", example = "/path/to/csv.csv", required = true)
    private String csvPath;
}
