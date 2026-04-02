package com.tool.send_email.dto;

import java.util.List;

/**
 * 内置演练模板清单（manifest.json）。
 */
public class DrillTemplateDefinition {

    private String id;
    private String title;
    private String file;
    private String description;
    private List<String> variables;
    private String sampleCsv;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public String getSampleCsv() {
        return sampleCsv;
    }

    public void setSampleCsv(String sampleCsv) {
        this.sampleCsv = sampleCsv;
    }
}
