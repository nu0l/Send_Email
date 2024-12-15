package com.tool.send_email.springmvc.controller;

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
    public MailConfig.MailAccount getConfig(@Parameter(description = "从0开始递增", required = true) @RequestParam int index) {
        try {
            MailConfig.MailAccount account = configService.getAccount(index);
            return account;
        } catch (Exception e) {
            return null;
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
    public List<MailConfig.MailAccount> getAllConfig() {
        try {
            List<MailConfig.MailAccount> accounts = configService.getAllAccounts();
            return accounts;
        } catch (Exception e) {
            return null;
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
    public ResponseEntity<String> addConfig(@Parameter(description = "配置信息", required = true) @RequestBody MailConfig.MailAccount account, @Parameter(description = "配置ID", required = true) @RequestParam int index) {
        try {
            if (!configService.checkAccountId(index)) {
                configService.addAccount(account, index);
                return ResponseEntity.ok("success");
            } else {
                return ResponseEntity.ok("fail, ID为空");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
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
    public ResponseEntity<String> updateConfig(@Parameter(description = "配置信息", required = true) @RequestBody MailConfig.MailAccount account, @Parameter(description = "配置ID", required = true) @RequestParam int index) {
        try {
            configService.updateAccount(account, index);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
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
    public ResponseEntity<String> delConfig(@Parameter(description = "配置ID", required = true) @RequestParam int index) {
        try {
            configService.removeAccount(index);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

}
