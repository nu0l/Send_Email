package com.tool.send_email.springmvc.controller;

import com.tool.send_email.dto.ApiResponse;
import com.tool.send_email.dto.SendEmailDTO;
import com.tool.send_email.model.Email;
import com.tool.send_email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import com.tool.send_email.utils.UploadPathUtils;

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
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile[] files) {
        File uploadDirFile = UploadPathUtils.resolveUploadSubdir("attachments");

        Map<String, String> uploadResultsMap = new HashMap<>();
        try {
            for (MultipartFile file : files) {
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || originalFilename.isEmpty()) {
                    return ApiResponse.fail(400, "文件名无效");
                }

                // 保存文件
                File tempFile = new File(uploadDirFile, originalFilename);
                file.transferTo(tempFile);
                uploadResultsMap.put(originalFilename, tempFile.getAbsolutePath());
            }
            return ApiResponse.ok("文件上传成功:\n" + uploadResultsMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.fail(400, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传文件列表
     *
     * @return
     */
    @GetMapping("/getUploadAttachments")
    @Operation(summary = "2. 获取上传文件列表", description = "获取上传文件列表")
    public ApiResponse<Map<String, String>> getUploads() {
        Map<String, String> uploads = new HashMap<>();
        File uploadDirFile = UploadPathUtils.resolveUploadSubdir("attachments");
        if (uploadDirFile.exists()) {

            File[] files = uploadDirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    uploads.put(file.getName(), file.getAbsolutePath());
                }
            }
        }
        return ApiResponse.ok(uploads);
    }

    /**
     * 清空上传文件
     *
     * @return
     */
    @GetMapping("/delUploadAttachments")
    @Operation(summary = "3. 清空上传文件", description = "清空上传文件")
    public ApiResponse<String> delUploads() {
        File uploadDirFile = UploadPathUtils.resolveUploadSubdir("attachments");
        if (uploadDirFile.exists()) {
            File[] files = uploadDirFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
        return ApiResponse.okMessage("文件删除成功");
    }

    /**
     * 发送邮件
     *
     * @param requestBody
     * @return
     */
    @PostMapping("/send")
    @Operation(summary = "4. 发送邮件", description = "发送邮件")
    public ApiResponse<String> sendEmail(@Parameter(description = "邮件参数", required = true) @RequestBody SendEmailDTO requestBody) {
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
                    if (attachment != null) {
                        String fileName = attachment.get("fileName");
                        String filePath = attachment.get("filePath");
                        if (fileName != null && filePath != null) {
                            attachments.put(fileName, filePath);
                        }
                    }
                }
            }

            if (to == null || to.isEmpty() || subject == null || subject.isEmpty() || content == null || content.isEmpty()) {
                return ApiResponse.fail(400, "请输入收件人、主题和内容");
            }

            // 构建 Email 对象并发送邮件
            Email email = new Email(to, subject, content, attachments == null || attachments.isEmpty() ? null : attachments, "WEB");
            // 本地研究测试：
            // - 不改动 From 邮箱（避免 553/535），只改显示昵称（display name）
            // - 支持 Reply-To 行为测试
            email.setForgeFromNickname(requestBody.getForgeFromNickname());
            email.setReplyToEmail(requestBody.getReplyToEmail());
            email.setReplyToNickname(requestBody.getReplyToNickname());
            emailService.sendEmail(email);

            return ApiResponse.okMessage("邮件正在发送中，请到控制台查看发送日志");
        } catch (MessagingException e) {
            return ApiResponse.fail(400, "邮件发送失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, "邮件发送失败，系统发生错误: " + e.getMessage());
        }
    }

}
