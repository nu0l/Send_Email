package com.tool.send_email.springmvc.controller;

import com.tool.send_email.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 当前登录用户（供控制台页眉展示）。
 */
@RestController
@RequestMapping("/api")
@Tag(name = "0. 会话", description = "当前用户")
@SecurityRequirement(name = "basicAuth")
public class WebUserInfoController {

    @GetMapping("/me")
    @Operation(summary = "当前用户")
    public ApiResponse<Map<String, Object>> me(Principal principal) {
        Map<String, Object> m = new HashMap<>();
        m.put("username", principal != null ? principal.getName() : "");
        m.put("authenticated", principal != null);
        return ApiResponse.ok(m);
    }
}
