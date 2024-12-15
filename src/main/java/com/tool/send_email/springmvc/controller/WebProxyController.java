package com.tool.send_email.springmvc.controller;

import com.tool.send_email.model.Proxy;
import com.tool.send_email.service.ProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 代理接口
 */

@RestController
@RequestMapping("/api/proxy")
@Tag(name = "2. 代理接口", description = "用于代理设置")
@SecurityRequirement(name = "basicAuth")
public class WebProxyController {
    @Autowired
    private ProxyService proxyService;

    /**
     * 设置代理
     *
     * @return
     */
    @PostMapping("/setProxy")
    @Operation(summary = "1. 设置代理", description = "设置代理")
    public ResponseEntity<String> setProxy(@Parameter(description = "代理配置", required = true) @RequestBody Proxy proxy) {
        try {
            proxyService.setProxy(proxy);
            return ResponseEntity.ok("代理设置已保存");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("代理设置保存失败");
        }
    }

    /**
     * 清空代理
     *
     * @return
     */
    @PostMapping("/unSetProxy")
    @Operation(summary = "2. 清空代理", description = "清空代理")
    public ResponseEntity<String> unsetProxy(@Parameter(description = "代理配置", required = true) @RequestBody Proxy proxy) {
        try {
            proxyService.unsetProxy(proxy);
            return ResponseEntity.ok("代理设置已取消");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("代理设置取消失败");
        }
    }

}
