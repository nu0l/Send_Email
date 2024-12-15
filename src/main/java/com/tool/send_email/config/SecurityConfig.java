package com.tool.send_email.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Spring Security 配置类，用于保护 /api 路径下的资源，需要身份验证。
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().and()
                // 禁用 CSRF 对 /api 路径的保护
                .csrf().ignoringAntMatchers("/api/**").and()
                // 配置表单登录
                .formLogin().permitAll().and() // 允许所有人访问登录页面
                // 配置授权请求
                .authorizeRequests().antMatchers("/login", "/logout").permitAll() // 允许所有人访问登录和注销页面
                .antMatchers("/api/**").authenticated() // 只有认证用户才能访问 /api 路径下的资源
                .anyRequest().authenticated(); // 其他请求需要认证
    }
}
