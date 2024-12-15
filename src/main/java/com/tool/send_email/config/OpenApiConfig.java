package com.tool.send_email.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApi配置类，开启认证和设置描述
 */

@Configuration
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic", description = "Basic Authentication")
@OpenAPIDefinition(info = @Info(title = "Send Email API", version = "1.0", description = "API documentation for Send Email Application"))

public class OpenApiConfig {
}
