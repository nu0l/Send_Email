package com.tool.send_email.springmvc.controller;

import com.tool.send_email.dto.SendEmailDTO;
import com.tool.send_email.model.Email;
import com.tool.send_email.service.EmailService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送邮件
 */

@RestController
@RequestMapping("/api/email")
@Tag(name = "4. 发送邮件接口", description = "用于发送邮件")
@SecurityRequirement(name = "basicAuth")
public class WebSendEMailController {
    private final Map<String, String> filePaths = new HashMap<>();
    @Autowired
    private EmailService emailService;

    /**
     * 上传附件
     *
     * @param files
     * @return
     */
    @PostMapping("/uploadAttachments")
    @Operation(summary = "1. 上传附件", description = "上传附件(支持多文件上传)")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile[] files) {
        String uploadDir = new File(this.getClass().getResource("/").getPath(), ".tmp_uploads_attachments").getAbsolutePath();
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        Map<String, String> uploadResultsMap = new HashMap<>();
        Map<String, String> filePaths = new HashMap<>();
        try {
            for (MultipartFile file : files) {
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.isEmpty()) {
                    return ResponseEntity.badRequest().body("文件名无效");
                }

                // 保存文件
                File tempFile = new File(uploadDir, originalFilename);
                file.transferTo(tempFile);
                filePaths.put(originalFilename, tempFile.getAbsolutePath());
                uploadResultsMap.put(originalFilename, tempFile.getAbsolutePath());
            }
            return ResponseEntity.ok("文件上传成功:\n" + uploadResultsMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传文件列表
     *
     * @return
     */
    @GetMapping("/getUploadAttachments")
    @Operation(summary = "2. 获取上传文件列表", description = "获取上传文件列表")
    public ResponseEntity<Map<String, String>> getUploads() {
        Map<String, String> uploads = new HashMap<>();
        String uploadDir = new File(this.getClass().getResource("/").getPath(), ".tmp_uploads_attachments").getAbsolutePath();
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
     * 清空上传文件
     *
     * @return
     */
    @GetMapping("/delUploadAttachments")
    @Operation(summary = "3. 清空上传文件", description = "清空上传文件")
    public ResponseEntity<String> delUploads() {
        String uploadDir = new File(this.getClass().getResource("/").getPath(), ".tmp_uploads_attachments").getAbsolutePath();
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
     * 发送邮件
     *
     * @param requestBody
     * @return
     */
    @PostMapping("/send")
    @Operation(summary = "4. 发送邮件", description = "发送邮件")
    public ResponseEntity<String> sendEmail(@Parameter(description = "邮件参数", required = true) @RequestBody SendEmailDTO requestBody) {
        try {
            List<String> to = requestBody.getTo();
            String subject = requestBody.getSubject();
            String content = requestBody.getContent();

            // 获取附件列表并合并为一个 Map
            List<Map<String, String>> attachmentsList = requestBody.getAttachments();
            Map<String, String> attachments = null;

            if (attachmentsList != null && !attachmentsList.isEmpty()) {
                attachments = new HashMap<>();
                // 合并所有附件到一个 Map 中
                for (Map<String, String> attachment : attachmentsList) {
                    attachments.putAll(attachment);
                }
            }

            if (to == null || to.isEmpty() || subject == null || subject.isEmpty() || content == null || content.isEmpty()) {
                return ResponseEntity.badRequest().body("请输入收件人、主题和内容");
            }

            // 构建 Email 对象并发送邮件
            Email email = new Email(to, subject, content, attachments);
            emailService.sendEmail(email);

            return ResponseEntity.ok("邮件正在发送中，请到控制台查看发送日志");
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("邮件发送失败");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("邮件发送失败，系统发生错误: " + e.getMessage());
        }
    }

}
