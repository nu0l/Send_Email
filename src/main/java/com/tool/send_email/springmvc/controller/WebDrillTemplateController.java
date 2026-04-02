package com.tool.send_email.springmvc.controller;

import com.tool.send_email.dto.ApiResponse;
import com.tool.send_email.dto.DrillTemplateDefinition;
import com.tool.send_email.service.DrillTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内置安全意识演练模板：列表、部署到本机路径、下载示例 CSV。
 */
@RestController
@RequestMapping("/api/drill")
@Tag(name = "8. 演练模板", description = "内置模板与示例数据（仅用于经授权的演练）")
@SecurityRequirement(name = "basicAuth")
public class WebDrillTemplateController {

    @Autowired
    private DrillTemplateService drillTemplateService;

    @GetMapping("/templates")
    @Operation(summary = "模板列表", description = "返回 id、标题、说明、变量列名等")
    public ApiResponse<List<DrillTemplateDefinition>> listTemplates() {
        return ApiResponse.ok(drillTemplateService.listTemplates());
    }

    @PostMapping("/deploy")
    @Operation(summary = "部署到本机", description = "复制到临时目录 builtin-templates，返回绝对路径供模板邮件使用")
    public ApiResponse<Map<String, String>> deploy(@RequestParam String id) throws IOException {
        String path = drillTemplateService.deployToDisk(id);
        Map<String, String> m = new HashMap<>();
        m.put("path", path);
        m.put("id", id);
        return ApiResponse.ok(m);
    }

    @GetMapping("/sample-csv")
    @Operation(summary = "下载示例 CSV", description = "与 manifest 中 sampleCsv 对应")
    public ResponseEntity<byte[]> sampleCsv(@RequestParam String id) throws IOException {
        byte[] bytes = drillTemplateService.readSampleCsv(id);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drill-sample-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
