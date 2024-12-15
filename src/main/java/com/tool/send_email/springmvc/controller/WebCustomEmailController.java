package com.tool.send_email.springmvc.controller;

import com.tool.send_email.dto.CustomEmailDTO;
import com.tool.send_email.dto.ValidationTemplateDTO;
import com.tool.send_email.service.CustomEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tool.send_email.utils.CsvParserUtils.readCsv;


/**
 * 自定义邮件发送
 */

@RestController
@RequestMapping("/api/email")
@Tag(name = "5. 自定义邮件发送接口", description = "用于发送自定义邮件")
@SecurityRequirement(name = "basicAuth")
public class WebCustomEmailController {

    @Autowired
    private CustomEmailService customEmailService;

    /**
     * 上传HTML模板和CSV文件
     *
     * @param files
     * @return
     */
    @PostMapping("/uploadTemplateAndCsv")
    @Operation(summary = "1. 上传HTML模板和CSV文件", description = "上传HTML模板和CSV文件(支持多文件上传)")
    public ResponseEntity<String> uploadFiles(@Parameter(description = "选择HTML或CSV文件", required = true) @RequestParam("file") MultipartFile[] files) {
        String uploadDir = new File(this.getClass().getResource("/").getPath(), ".tmp_uploads_templateAndCsv").getAbsolutePath();
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }
        Map<String, String> uploadResultsMap = new HashMap<>();

        try {
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();

                if (fileName == null || (!fileName.toLowerCase().endsWith(".html") && !fileName.toLowerCase().endsWith(".csv"))) {
                    return ResponseEntity.badRequest().body("呵呵，无效文件类型，仅支持 .html 和 .csv 文件\n");
                }

                // 保存文件
                File targetFile = new File(uploadDir, fileName);
                file.transferTo(targetFile);
                uploadResultsMap.put(fileName, targetFile.getAbsolutePath());
            }

            return ResponseEntity.ok("文件上传成功:\n" + uploadResultsMap);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传的模板和CSV文件
     *
     * @return
     */
    @GetMapping("/getUploadTemplateAndCsv")
    @Operation(summary = "2. 获取上传的HTML模板和CSV文件列表", description = "获取上传的HTML模板和CSV文件列表")
    public ResponseEntity<Map<String, String>> getUploads() {
        Map<String, String> uploads = new HashMap<>();
        String uploadDir = new File(this.getClass().getResource("/").getPath(), ".tmp_uploads_templateAndCsv").getAbsolutePath();
        File uploadDirFile = new File(uploadDir);
        if (uploadDirFile.exists()) {

            File[] files = uploadDirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    uploads.put(file.getName(), file.getAbsolutePath());
                }
            }
        }
        return ResponseEntity.ok(uploads);
    }

    /**
     * 删除上传的模板和CSV文件
     *
     * @return
     */
    @GetMapping("/delUploadTemplateAndCsv")
    @Operation(summary = "3. 清空上传的HTML模板和CSV文件", description = "清空上传的HTML模板和CSV文件")
    public ResponseEntity<String> delUploads() {
        String uploadDir = new File(this.getClass().getResource("/").getPath(), ".tmp_uploads_templateAndCsv").getAbsolutePath();
        File uploadDirFile = new File(uploadDir);
        if (uploadDirFile.exists()) {
            File[] files = uploadDirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
        return ResponseEntity.ok("文件删除成功");
    }

    /**
     * 发送自定义邮件
     *
     * @param requestBody
     * @return
     * @throws IOException
     */
    @PostMapping("/sendCustomEmail")
    @Operation(summary = "5. 发送自定义邮件", description = "发送自定义邮件")
    public ResponseEntity<String> sendEmail(@Parameter(description = "自定义邮件参数", required = true) @RequestBody CustomEmailDTO requestBody) throws IOException {
        String templatePath = requestBody.getTemplatePath();
        String csvPath = requestBody.getCsvPath();

        if (templatePath == null || templatePath.isEmpty() || csvPath == null || csvPath.isEmpty()) {
            return ResponseEntity.badRequest().body("请输入模板和CSV文件路径");
        }

        List<Map<String, String>> attachmentsList = requestBody.getAttachments();
        Map<String, String> attachments = null;

        if (attachmentsList != null && !attachmentsList.isEmpty()) {
            attachments = new HashMap<>();
            // 合并所有附件到一个 Map 中
            for (Map<String, String> attachment : attachmentsList) {
                attachments.putAll(attachment);
            }
        }
        List<Map<String, String>> recipients = readCsv(csvPath);
        try {
            customEmailService.parseTemplateAndSendEmail(recipients, templatePath, attachments);
            return ResponseEntity.ok("邮件正在发送中，请到控制台查看发送日志");
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("邮件发送失败");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("邮件发送失败，系统发生错误: " + e.getMessage());
        }
    }

    /**
     * 验证自定义邮件
     *
     * @param requestBody
     * @return
     * @throws IOException
     */
    @PostMapping("/validationCustomEmail")
    @Operation(summary = "4. 验证自定义邮件渲染情况", description = "验证自定义邮件渲染情况")
    public ResponseEntity<String> validationTemplate(@Parameter(description = "自定义邮件参数", required = true) @RequestBody ValidationTemplateDTO requestBody) throws IOException {
        String templatePath = requestBody.getTemplatePath();
        String csvPath = requestBody.getCsvPath();

        if (templatePath == null || templatePath.isEmpty() || csvPath == null || csvPath.isEmpty()) {
            return ResponseEntity.badRequest().body("请输入模板和CSV文件路径");
        }
        List<Map<String, String>> recipients = readCsv(csvPath);
        try {
            String s = customEmailService.validationTemplate(recipients, templatePath);
            return ResponseEntity.ok(s);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("解析失败: " + e.getMessage());
        }
    }

}
