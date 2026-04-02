package com.tool.send_email.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tool.send_email.dto.DrillTemplateDefinition;
import com.tool.send_email.utils.UploadPathUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
/**
 * 从 classpath 加载内置演练模板，并可部署到本地上传目录供「模板邮件」使用。
 */
@Service
public class DrillTemplateService {

    private final ObjectMapper objectMapper;
    private List<DrillTemplateDefinition> templates = Collections.emptyList();

    public DrillTemplateService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadManifest() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("templates/drill/manifest.json")) {
            if (in == null) {
                templates = Collections.emptyList();
                return;
            }
            templates = objectMapper.readValue(in, new TypeReference<List<DrillTemplateDefinition>>() {
            });
            if (templates == null) {
                templates = Collections.emptyList();
            }
        } catch (IOException e) {
            templates = Collections.emptyList();
        }
    }

    public List<DrillTemplateDefinition> listTemplates() {
        return templates;
    }

    public DrillTemplateDefinition findById(String id) {
        if (id == null) {
            return null;
        }
        for (DrillTemplateDefinition d : templates) {
            if (id.equals(d.getId())) {
                return d;
            }
        }
        return null;
    }

    /**
     * 将模板复制到临时目录下的 builtin-templates，返回绝对路径（供 Thymeleaf 文件模板解析）。
     */
    public String deployToDisk(String id) throws IOException {
        DrillTemplateDefinition def = findById(id);
        if (def == null) {
            throw new IllegalArgumentException("未知模板: " + id);
        }
        String resourcePath = "templates/drill/" + def.getFile();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("找不到资源: " + resourcePath);
            }
            File dir = UploadPathUtils.resolveUploadSubdir("builtin-templates");
            File out = new File(dir, id + ".html");
            Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return out.getAbsolutePath();
        }
    }

    public byte[] readSampleCsv(String id) throws IOException {
        DrillTemplateDefinition def = findById(id);
        if (def == null || def.getSampleCsv() == null || def.getSampleCsv().isEmpty()) {
            return null;
        }
        String path = "templates/drill/" + def.getSampleCsv();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("找不到示例 CSV: " + path);
            }
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) != -1) {
                buf.write(b, 0, n);
            }
            return buf.toByteArray();
        }
    }

}
