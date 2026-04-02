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
        http
                .csrf()
                .ignoringAntMatchers("/api/**", "/login", "/logout")
                .and()
                .authorizeRequests()
                .antMatchers("/", "/login", "/login.html").permitAll()
                .antMatchers("/js/**", "/css/**", "/favicon.ico").permitAll()
                .antMatchers(
                        "/GUI_README.html",
                        "/error",
                        "/1213300000/swagger-ui.html",
                        "/1213300000/swagger-ui/**",
                        "/1213300000/api-docs/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                ).permitAll()
                .antMatchers("/api/**", "/index", "/index.html").authenticated()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index", true)
                .failureUrl("/login?error")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=1")
                .permitAll();
    }
}
