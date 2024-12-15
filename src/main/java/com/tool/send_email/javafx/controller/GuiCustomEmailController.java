package com.tool.send_email.javafx.controller;

import com.tool.send_email.model.EmailStatusCallback;
import com.tool.send_email.service.CustomEmailService;
import com.tool.send_email.service.EmailService;
import com.tool.send_email.service.ProxyService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tool.send_email.utils.CsvParserUtils.readCsv;

/**
 * 自定义邮件发送界面
 */

@Controller
public class GuiCustomEmailController implements EmailStatusCallback {
    private static final Logger logger = LoggerFactory.getLogger(GuiCustomEmailController.class);
    private final EmailService emailService;
    private final CustomEmailService customEmailService;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private ProxyService proxyService;
    private final Map<String, String> attachmentFiles = Collections.synchronizedMap(new HashMap<>());

    @FXML
    private Button startButton;
    @FXML
    private TextField templatePathTextField;
    @FXML
    private TextField csvPathTextField;
    @FXML
    private TextArea statusDisplayArea;

    @Autowired
    public GuiCustomEmailController(EmailService emailService, CustomEmailService customEmailService) {
        this.emailService = emailService;
        this.customEmailService = customEmailService;
        emailService.setEmailStatusCallback(this);
    }

    @FXML
    public void handleStartButton() throws IOException {
        // 获取用户输入的模板路径
        String templatePath = templatePathTextField.getText();
        String csvPath = csvPathTextField.getText();

        // 检查模板路径是否为空
        if (templatePath.isEmpty() || csvPath.isEmpty()) {
            statusDisplayArea.appendText("请提供模板路径！\n");
            return;
        }

        // 读取CSV文件
        List<Map<String, String>> recipients = readCsv(csvPath);
        if (recipients == null || recipients.isEmpty()) {
            statusDisplayArea.appendText("CSV文件解析失败，请检查文件格式！\n");
            return;
        }

        // 渲染并发送邮件
        try {
            statusDisplayArea.appendText("开始发送邮件...\n");
            customEmailService.parseTemplateAndSendEmail(recipients, templatePath, attachmentFiles);
        } catch (Exception e) {
            statusDisplayArea.appendText("邮件发送失败: " + e.getMessage());
        }
    }

    @FXML
    public void loadHtmlContentButton() {
        loadFileContent("选择 HTML 文件", "HTML 文件", "*.html", templatePathTextField);
    }

    @FXML
    public void loadCsvContentButton() {
        loadFileContent("选择 CSV 文件", "CSV 文件", "*.csv", csvPathTextField);
    }

    @FXML
    public void verifyButton() throws IOException {
        String templatePath = templatePathTextField.getText();
        String csvPath = csvPathTextField.getText();

        // 检查模板路径是否为空
        if (templatePath.isEmpty() || csvPath.isEmpty()) {
            statusDisplayArea.appendText("请提供模板路径！\n");
            return;
        }

        // 读取CSV文件
        List<Map<String, String>> recipients = readCsv(csvPath);
        if (recipients == null || recipients.isEmpty()) {
            statusDisplayArea.appendText("CSV文件解析失败，请检查文件格式！\n");
            return;
        }

        String s = customEmailService.validationTemplate(recipients, templatePath);
        statusDisplayArea.appendText("验证结果(只展示第一个)：\n");
        statusDisplayArea.appendText(s + "\n");
    }

    @FXML
    public void proxySettingsMenuButton() {
        Stage currentStage = (Stage) statusDisplayArea.getScene().getWindow();
        proxyService.openProxySettings(currentStage);
    }

    @FXML
    public void addAttachments() {
        List<File> selectedFiles = chooseMultipleFiles("选择附件", null, null);
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                attachmentFiles.put(file.getName(), file.getAbsolutePath());
                statusDisplayArea.appendText("已添加附件: " + file.getName() + "\n");
            }
        }
    }

    @Override
    public void onStatusUpdate(String message) {
        if (message == null) {
            message = " ";
        }
        // 检查 statusDisplayArea 是否为 null
        if (statusDisplayArea != null) {
            String finalMessage = message;
            Platform.runLater(() -> statusDisplayArea.appendText(finalMessage + "\n"));
        } else {
            logger.warn("CustomEmailController的statusDisplayArea 为 null，无法更新状态消息, 如未使用请忽略");
        }
    }

    @Override
    public void initialize() {
        emailService.setEmailStatusCallback(this);
    }

    private void loadFileContent(String title, String description, String extension, TextField targetTextField) {
        File selectedFile = chooseFile(title, description, extension);
        if (selectedFile != null) {
            try {
                targetTextField.setText(selectedFile.toPath().toString());
                statusDisplayArea.appendText(description + " 导入成功。\n");
            } catch (Exception e) {
                handleFileReadError((IOException) e);
            }
        }
    }

    private File chooseFile(String title, String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (description != null && extension != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        }
        Stage stage = (Stage) statusDisplayArea.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    private void handleFileReadError(IOException e) {
        statusDisplayArea.appendText("文件读取失败: " + e.getMessage() + "\n");
        e.printStackTrace();
    }

    private List<File> chooseMultipleFiles(String title, String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (description != null && extension != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        }
        Stage stage = (Stage) statusDisplayArea.getScene().getWindow();
        return fileChooser.showOpenMultipleDialog(stage);
    }
}
