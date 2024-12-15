package com.tool.send_email.service;

import com.tool.send_email.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tool.send_email.utils.CsvParserUtils.extractValidEmailsFromCsv;

/**
 * 自定义邮件服务类
 */

@Service
public class CustomEmailService {
    private static final Logger logger = LoggerFactory.getLogger(CustomEmailService.class);

    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @Autowired
    public CustomEmailService(EmailService emailService) {
        this.emailService = emailService;

        // 使用 FileTemplateResolver 来加载文件路径
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix("");  // 设置文件路径的前缀
        templateResolver.setSuffix(".html");  // 设置文件后缀
        templateResolver.setCharacterEncoding("UTF-8");

        // 设置 TemplateEngine 使用 FileTemplateResolver
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    /**
     * 渲染邮件模板并通过 EmailService 发送邮件
     *
     * @param recipients   收件人信息列表
     * @param templatePath 模板路径
     * @throws MessagingException
     */
    public void parseTemplateAndSendEmail(List<Map<String, String>> recipients, String templatePath, Map<String, String> attachmentFiles) throws MessagingException {
        if (recipients == null || recipients.isEmpty()) {
            logger.error("解析csv文件时出错");
            return;
        }

        // 过滤有效的收件人列表，只保留包含 "toEmail" 字段的项
        List<Map<String, String>> validRecipients = recipients.stream()
                .filter(recipient -> recipient.containsKey("toEmail") && recipient.get("toEmail") != null && !recipient.get("toEmail").isEmpty())
                .collect(Collectors.toList());

        List<Map<String, String>> maps = extractValidEmailsFromCsv(validRecipients);
        logger.info("有效收件人数量: {}", maps.size());
        // 依次处理每个有效的收件人
        for (Map<String, String> recipient : maps) {
            String renderedContent = renderTemplate(recipient, templatePath);
            List<String> to = Collections.singletonList(recipient.get("toEmail"));
            String subject = recipient.get("emailSubject");
            Email email = new Email(to, subject, renderedContent, attachmentFiles.isEmpty() ? null : attachmentFiles);

            // 调用邮件发送服务
            emailService.sendEmail(email);

        }
    }

    public String validationTemplate(List<Map<String, String>> recipients, String templatePath) {
        if (recipients == null || recipients.isEmpty()) {
            logger.error("解析csv文件时出错");
            return "解析csv文件时出错";
        }

        // 过滤有效的收件人列表
        List<Map<String, String>> validRecipients = recipients.stream()
                .filter(recipient -> recipient.containsKey("toEmail") && recipient.get("toEmail") != null && !recipient.get("toEmail").isEmpty())
                .collect(Collectors.toList());

        // 输出有效收件人的数量
        logger.info("有效收件人数量: {}", validRecipients.size());

        if (validRecipients.isEmpty()) {
            return "没有有效的收件人";
        }

        // 遍历所有有效收件人，并返回第一个有效的渲染内容
        for (Map<String, String> recipient : validRecipients) {
            String renderedContent = renderTemplate(recipient, templatePath);
            if (renderedContent != null && !renderedContent.isEmpty()) {
                return renderedContent; // 返回第一个有效的渲染内容
            }
        }

        return "模板渲染失败";
    }


    /**
     * 渲染 Thymeleaf 模板
     *
     * @param recipient    收件人信息
     * @param templatePath 模板路径
     * @return 渲染后的邮件内容
     */
    private String renderTemplate(Map<String, String> recipient, String templatePath) {
        // 使用收件人信息设置模板变量
        Map<String, Object> contextVariables = new HashMap<>(recipient);
        Context context = new Context();
        context.setVariables(contextVariables);

        // 动态加载模板文件
        return templateEngine.process(templatePath, context);
    }
}
