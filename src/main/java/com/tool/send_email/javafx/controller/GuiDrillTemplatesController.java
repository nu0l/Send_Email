package com.tool.send_email.javafx.controller;

import com.tool.send_email.dto.DrillTemplateDefinition;
import com.tool.send_email.service.DrillTemplateService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * 内置演练模板：说明与部署到本机路径。
 */
@Controller
public class GuiDrillTemplatesController {

    private final DrillTemplateService drillTemplateService;

    @FXML
    private ListView<String> templateList;
    @FXML
    private TextArea detailArea;
    @FXML
    private Label pathLabel;

    private final Map<String, DrillTemplateDefinition> idByTitle = new HashMap<>();
    private String lastDeployedPath;

    public GuiDrillTemplatesController(DrillTemplateService drillTemplateService) {
        this.drillTemplateService = drillTemplateService;
    }

    @FXML
    public void initialize() {
        templateList.getItems().clear();
        idByTitle.clear();
        for (DrillTemplateDefinition d : drillTemplateService.listTemplates()) {
            String label = d.getTitle() + "  [" + d.getId() + "]";
            idByTitle.put(label, d);
            templateList.getItems().add(label);
        }
        templateList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> showDetail(n));
        if (!templateList.getItems().isEmpty()) {
            templateList.getSelectionModel().selectFirst();
        }
    }

    private void showDetail(String label) {
        if (label == null) {
            detailArea.setText("");
            return;
        }
        DrillTemplateDefinition d = idByTitle.get(label);
        if (d == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(d.getDescription()).append("\n\n");
        if (d.getVariables() != null && !d.getVariables().isEmpty()) {
            sb.append("CSV 列：").append(String.join(", ", d.getVariables())).append("\n");
        }
        sb.append("\n部署后可将「模板文件绝对路径」填到「模板邮件」页对应输入框。");
        detailArea.setText(sb.toString());
    }

    @FXML
    public void deploySelected() {
        String label = templateList.getSelectionModel().getSelectedItem();
        DrillTemplateDefinition d = label == null ? null : idByTitle.get(label);
        if (d == null) {
            pathLabel.setText("请先选择一个模板。");
            return;
        }
        try {
            lastDeployedPath = drillTemplateService.deployToDisk(d.getId());
            pathLabel.setText(lastDeployedPath);
        } catch (Exception ex) {
            pathLabel.setText("部署失败: " + ex.getMessage());
            lastDeployedPath = null;
        }
    }

    @FXML
    public void copyPath() {
        if (lastDeployedPath == null || lastDeployedPath.isEmpty()) {
            pathLabel.setText("请先点击「部署到本机」。");
            return;
        }
        ClipboardContent cc = new ClipboardContent();
        cc.putString(lastDeployedPath);
        Clipboard.getSystemClipboard().setContent(cc);
        pathLabel.setText("已复制到剪贴板: " + lastDeployedPath);
    }
}
