package com.tool.send_email.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileInputStream;

import static com.tool.send_email.utils.EmlParserUtils.parseEmlFile;
import static com.tool.send_email.utils.EmlParserUtils.saveToHtmlFile;
import static com.tool.send_email.utils.FileParserUtils.getFileContentString;

/**
 * EML 转 HTML界面
 */

@Controller
public class GuiEmlToHtmlController {

    private static FileInputStream emlCode;
    private static String htmlCode;
    @FXML
    private TextField fileInputPathTextField;
    @FXML
    private TextField fileOutputPathTextField;
    @FXML
    private WebView webView;

    public GuiEmlToHtmlController() {
    }

    /**
     * 导入 EML 文件
     */
    @FXML
    public void importEmlFileButton() {
        File selectedFile = chooseFile("选择 EML 文件", "Text Files", "*.eml");
        if (selectedFile != null) {
            try {
                String filePath = selectedFile.toPath().toString();
                String path = selectedFile.toPath().getParent().toString();
                String fileName = selectedFile.getName();
                if (!filePath.isEmpty()) {
                    FileInputStream emlFile = new FileInputStream(filePath);
                    emlCode = emlFile;
                    fileInputPathTextField.setText(filePath);
                    fileOutputPathTextField.setText(path + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + ".html");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    public void exportHtmlFileButton() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("转换详情");
        if (!fileInputPathTextField.getText().isEmpty() && !fileOutputPathTextField.getText().isEmpty()) {
            try {
                String emlPath = fileInputPathTextField.getText();
                String htmlContent = parseEmlFile(emlCode);
                htmlCode = htmlContent;

                if (!htmlContent.isEmpty()) {
                    String outputHtmlFilePath = fileOutputPathTextField.getText();
                    saveToHtmlFile(htmlContent, outputHtmlFilePath);
                    // 设置弹窗内容并显示
                    alert.setContentText(String.format("文件已生成, 请预览检查: %s", outputHtmlFilePath));
                    alert.showAndWait();
                } else {
                    alert.setContentText("文件转换失败");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void htmlPreviewButton() {
        if (!fileOutputPathTextField.getText().isEmpty()) {
            try {
                String htmlCode = getFileContentString(fileOutputPathTextField.getText());
                webView.getEngine().loadContent(htmlCode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setContentText("输出路径不能为空");
            alert.showAndWait();
        }

    }

    private File chooseFile(String title, String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (description != null && extension != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        }
        Stage stage = (Stage) fileInputPathTextField.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }
}
