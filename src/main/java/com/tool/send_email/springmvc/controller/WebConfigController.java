package com.tool.send_email.springmvc.controller;

import com.tool.send_email.dto.ApiResponse;
import com.tool.send_email.config.MailConfig;
import com.tool.send_email.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static com.tool.send_email.utils.FileParserUtils.checkFileExist;


/**
 * config接口
 */

@RestController
@RequestMapping("/api/config")
@Tag(name = "1. 配置管理接口", description = "用于管理邮件配置")
@SecurityRequirement(name = "basicAuth")
public class WebConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * 查
     * 指定ID, 获取单个配置信息
     *
     * @param index
     * @return
     */
    @GetMapping("/getConfig")
    @Operation(summary = "2. 根据ID获取配置信息", description = "根据ID获取配置信息")
    public ApiResponse<MailConfig.MailAccount> getConfig(@Parameter(description = "从0开始递增", required = true) @RequestParam int index) {
        try {
            MailConfig.MailAccount account = configService.getAccount(index);
            return ApiResponse.ok(account);
        } catch (Exception e) {
            return ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "配置不存在");
        }
    }

    /**
     * 查
     * 获取所有配置信息
     *
     * @return
     */
    @GetMapping("/getAllConfig")
    @Operation(summary = "1. 获取全部的配置信息", description = "获取全部的配置信息")
    public ApiResponse<List<MailConfig.MailAccount>> getAllConfig() {
        try {
            List<MailConfig.MailAccount> accounts = configService.getAllAccounts();
            return ApiResponse.ok(accounts != null ? accounts : Collections.emptyList());
        } catch (Exception e) {
            return ApiResponse.ok(Collections.emptyList());
        }
    }

    /**
     * 增
     * 指定配置ID, 新增一个配置
     *
     * @param account
     * @param index
     * @return
     */
    @PostMapping("/addConfig")
    @Operation(summary = "3. 根据ID新增配置", description = "根据ID新增配置")
    public ApiResponse<String> addConfig(@Parameter(description = "配置信息", required = true) @RequestBody MailConfig.MailAccount account, @Parameter(description = "配置ID", required = true) @RequestParam int index) {
        try {
            if (!configService.checkAccountId(index)) {
                configService.addAccount(account, index);
                return ApiResponse.okMessage("success");
            } else {
                return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "fail, 该配置序号已存在");
            }
        } catch (Exception e) {
            return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "error");
        }
    }

    /**
     * 获取配置路径
     *
     * @return
     */
    @GetMapping("/getConfigPath")
    @Operation(summary = "6. 获取配置文件路径", description = "获取配置文件路径")
    public String getConfigPath() {
        String path = checkFileExist();
        return path;
    }

    /**
     * 改
     * 指定配置ID, 更新配置
     *
     * @param account
     * @param index
     * @return
     */
    @PostMapping("/updateConfig")
    @Operation(summary = "4. 根据ID更新配置", description = "根据ID更新配置")
    public ApiResponse<String> updateConfig(@Parameter(description = "配置信息", required = true) @RequestBody MailConfig.MailAccount account, @Parameter(description = "配置ID", required = true) @RequestParam int index) {
        try {
            configService.updateAccount(account, index);
            return ApiResponse.okMessage("success");
        } catch (Exception e) {
            return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "error");
        }
    }

    /**
     * 删
     * 指定配置ID, 删除配置
     *
     * @param index
     * @return
     */
    @GetMapping("/delConfig")
    @Operation(summary = "5. 根据ID删除配置", description = "根据ID删除配置")
    public ApiResponse<String> delConfig(@Parameter(description = "配置ID", required = true) @RequestParam int index) {
        try {
            configService.removeAccount(index);
            return ApiResponse.okMessage("success");
        } catch (Exception e) {
            return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "error");
        }
    }

}
