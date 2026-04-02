package com.tool.send_email.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * 邮件配置类，用于配置多个邮件账户和生成对应的 JavaMailSender 实例。
 */

@Configuration
@PropertySource(value = "file:${user.home}/.config/sendEmail/config.properties", encoding = "UTF-8")
@ConfigurationProperties(prefix = "mail") // 指定配置前缀
@Data
@Schema(description = "邮件配置")
public class MailConfig {

    /**
     * 邮件账户配置列表，包含多个邮件账户的配置信息。
     */
    private List<MailAccount> accounts = new ArrayList<>();

    @Autowired
    private ProxyConfig proxyConfig;


    /**
     * 配置并返回多个 JavaMailSender 实例，每个实例对应一个邮件账户。
     *
     * @return 包含多个 JavaMailSender 实例的列表
     */
    @Bean
    public List<JavaMailSender> mailSenders() {
        List<JavaMailSender> mailSenders = new ArrayList<>();
        for (MailAccount account : accounts) {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(account.getHost());
            mailSender.setPort(account.getPort());
            mailSender.setUsername(account.getUsername());
            mailSender.setPassword(account.getPassword());
            mailSender.setJavaMailProperties(getMailPropertiesForAccount(account));
            mailSenders.add(mailSender);
        }
        return mailSenders;
    }

    /**
     * 每个邮箱账户使用各自的 SMTP 属性；多账户时不再被循环覆盖为最后一项的配置。
     */
    private Properties getMailPropertiesForAccount(MailAccount account) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", String.valueOf(account.isAuthrequired()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(account.isSsl()));
        properties.put("mail.smtp.ssl.enable", String.valueOf(account.isSsl()));
        properties.put("mail.smtp.localhost", "localhost");
        // 关键：当我们允许“Forgery 发件人邮箱/昵称”时，不同邮件服务端会校验 SMTP 信封 MAIL FROM
        // 是否等于已认证用户。显式设置为每个账号的授权 from，可避免 553 拒收。
        properties.put("mail.smtp.from", account.getFrom());

        String proxyType = proxyConfig.getType();
        if (proxyType != null && "socks5".equalsIgnoreCase(proxyType.trim())) {
            properties.put("mail.smtp.socks.host", proxyConfig.getHost());
            properties.put("mail.smtp.socks.port", String.valueOf(proxyConfig.getPort()));
        }

        return properties;
    }

    /**
     * 邮件账户配置项，包含单个邮件账户的基本信息。
     */
    @Data
    public static class MailAccount {
        @Schema(description = "配置ID")
        private int id;
        @Schema(description = "邮件服务器的主机地址", example = "smtp.163.com")
        private String host; // 邮件服务器的主机地址
        @Schema(description = "邮件服务器的端口号", example = "465")
        private int port; //邮件服务器的端口号
        @Schema(description = "是否启用SSL加密", example = "true")
        private boolean ssl; //是否启用SSL加密
        @Schema(description = "邮件账户的用户名", example = "xxx@163.com")
        private String username; //邮件账户的用户名
        @Schema(description = "邮件账户的密码", example = "xxxxxx")
        private String password; //邮件账户的密码
        @Schema(description = "发件人地址", example = "xxx@163.com")
        private String from; //邮件的发件人
        @Schema(description = "发件人昵称", example = "管理员")
        private String nickname; //邮件的发件人昵称
        @Schema(description = "是否需要身份验证", example = "true")
        @JsonAlias({"authRequired"})
        private boolean authrequired;
    }
}