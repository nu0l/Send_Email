package com.tool.send_email.config;

import com.tool.send_email.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ObfuscationConfig {

    @Value("${sendEmail.obfuscation.enabled:true}")
    private boolean enabled;

    // 强度倍率：1.0 表示不额外放大，默认开启时建议略高于 1
    @Value("${sendEmail.obfuscation.strengthMultiplier:1.2}")
    private double strengthMultiplier;

    @PostConstruct
    public void init() {
        RandomUtils.configure(enabled, strengthMultiplier);
    }
}

