package com.tool.send_email.springmvc.controller;

import com.tool.send_email.dto.ApiResponse;
import com.tool.send_email.model.SendActivityEntry;
import com.tool.send_email.service.SendActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 发送活动统计与记录查询。
 */
@RestController
@RequestMapping("/api/activity")
@Tag(name = "7. 发送统计与记录", description = "基于本地日志的统计与最近记录")
@SecurityRequirement(name = "basicAuth")
public class WebActivityController {

    @Autowired
    private SendActivityLogService sendActivityLogService;

    @GetMapping("/stats")
    @Operation(summary = "汇总统计", description = "总次数、成功/失败、按来源、近 7 日条数")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(sendActivityLogService.stats());
    }

    @GetMapping("/records")
    @Operation(summary = "最近记录", description = "从新到旧，limit 最大 500")
    public ApiResponse<List<SendActivityEntry>> records(@RequestParam(defaultValue = "50") int limit) {
        int cap = Math.min(Math.max(limit, 1), 500);
        return ApiResponse.ok(sendActivityLogService.readRecent(cap));
    }
}
