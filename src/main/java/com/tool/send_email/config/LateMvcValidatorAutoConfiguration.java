package com.tool.send_email.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 在 {@link WebMvcAutoConfiguration} 之后注册同名 {@code mvcValidator}，配合
 * {@code spring.main.allow-bean-definition-overriding=true} 覆盖默认工厂（避免走 ValidatorAdapter → javax.validation）。
 * 仅通过 META-INF/spring.factories 启用，并由 {@link com.tool.send_email.StartApplicationMain} 的 excludeFilters 排除组件扫描，防止重复注册。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class LateMvcValidatorAutoConfiguration {

    @Bean(name = "mvcValidator")
    @Primary
    public Validator mvcValidator() {
        return new Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return false;
            }

            @Override
            public void validate(Object target, Errors errors) {
            }
        };
    }
}
