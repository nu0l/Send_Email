package com.tool.send_email.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 代理配置类
 */

@Configuration
@PropertySource(value = "file:${user.home}/.config/sendEmail/config.properties", encoding = "UTF-8")
@ConfigurationProperties(prefix = "proxy") // 指定配置前缀
@Data
@Schema(description = "代理配置")
public class ProxyConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProxyConfig.class);

    @Schema(description = "是否启用代理", example = "true")
    private boolean enable;    // 是否启用代理
    @Schema(description = "代理类型", example = "SOCKS5")
    private String type;       // 代理类型：HTTP、HTTPS、SOCKS5
    @Schema(description = "代理服务器地址", example = "127.0.0.1")
    private String host;       // 代理服务器地址
    @Schema(description = "代理服务器端口号", example = "7890")
    private int port;          // 代理服务器端口
    @Schema(description = "代理用户名", example = "username")
    private String username;   // 代理用户名
    @Schema(description = "代理密码", example = "password")
    private String password;   // 代理密码

}