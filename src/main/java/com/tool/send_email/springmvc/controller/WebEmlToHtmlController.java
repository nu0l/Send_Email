package com.tool.send_email.springmvc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import static com.tool.send_email.utils.EmlParserUtils.parseEmlFile;


/**
 * 解析eml文件
 */

@RestController
@RequestMapping("/api/file")
@Tag(name = "3. 文件解析接口", description = "用于解析文件")
@SecurityRequirement(name = "basicAuth")
public class WebEmlToHtmlController {

    /**
     * 将 .eml 文件转换为 .html 文件
     *
     * @param file
     * @return
     */
    @PostMapping("/conversion")
    @Operation(summary = "1. 文件转换", description = "将 .eml 文件转换为 .html 文件")
    public ResponseEntity<Resource> emlToHtml(@RequestParam("file") MultipartFile file) {
        File tempEmlFile = null;
        File tempHtmlFile = null;

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".eml")) {
                throw new RuntimeException("呵呵， 仅支持上传 .eml 文件");
            }

            tempEmlFile = File.createTempFile("upload-", ".eml");
            file.transferTo(tempEmlFile);

            String htmlFilename = originalFilename.substring(0, originalFilename.lastIndexOf(".")) + ".html";
            tempHtmlFile = File.createTempFile("converted-", ".html");

            try (FileInputStream fileInputStream = new FileInputStream(tempEmlFile);
                 FileWriter writer = new FileWriter(tempHtmlFile)) {

                // 调用解析方法
                String htmlContent = parseEmlFile(fileInputStream);

                writer.write(htmlContent);
            }

            // 返回 HTML 文件作为附件下载
            Resource resource = new InputStreamResource(new FileInputStream(tempHtmlFile));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + htmlFilename + "\"")
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("文件读取失败", e);
        } catch (Exception e) {
            throw new RuntimeException("文件解析失败", e);
        } finally {
            // 删除临时文件
            if (tempEmlFile != null && tempEmlFile.exists()) {
                tempEmlFile.delete();
            }
            if (tempHtmlFile != null && tempHtmlFile.exists()) {
                tempHtmlFile.delete();
            }
        }
    }

}
