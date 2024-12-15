package com.tool.send_email.javafx.controller;

import com.tool.send_email.model.Email;
import com.tool.send_email.model.EmailStatusCallback;
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
import org.springframework.stereotype.Controller;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.tool.send_email.utils.FileParserUtils.extractValidEmailsFromFile;
import static com.tool.send_email.utils.FileParserUtils.getFileContentString;

/**
 * 发送邮件界面
 */

@Controller
public class GuiSendEMailController implements EmailStatusCallback {
    private static final Logger logger = LoggerFactory.getLogger(GuiSendEMailController.class);

    private final EmailService emailService;
    private final Map<String, String> attachmentFiles = Collections.synchronizedMap(new HashMap<>());
    @Autowired
    private ProxyService proxyService;
    @FXML
    private TextField recipientEmailTextField;
    @FXML
    private Button loadRecipientsButton;
    @FXML
    private TextField subjectTextField;
    @FXML
    private TextField contentTextField;
    @FXML
    private Button loadHtmlContentButton;
    @FXML
    private Button addAttachmentsButton;
    @FXML
    private TextArea statusDisplayArea;
    public GuiSendEMailController() {
        this.emailService = null;
    }
    @Autowired
    public GuiSendEMailController(EmailService emailService, GuiProxyController proxyController) {
        this.emailService = emailService;
        emailService.setEmailStatusCallback(this);
        proxyController.setEmailStatusCallback(this);
    }

    @FXML
    public void sendButton() throws MessagingException {
        String recipientEmails = recipientEmailTextField.getText();
        String subject = subjectTextField.getText();
        String content = contentTextField.getText();

        if (recipientEmails.isEmpty() || subject.isEmpty() || content.isEmpty()) {
            statusDisplayArea.setText("请输入必填项: 收件人、主题、内容\n");
            return;
        }
        List<String> recipientList = Arrays.asList(recipientEmails.split(",\\s*"));

        Email email = new Email(recipientList, subject, content, attachmentFiles.isEmpty() ? null : attachmentFiles);

        statusDisplayArea.appendText("邮件发送中，请稍候...\n");

        CompletableFuture<Boolean> sendResult = emailService.sendEmail(email);
        sendResult.thenAccept(success -> {
            if (success) {
                statusDisplayArea.appendText("所有邮件发送成功！\n");
            } else {
                statusDisplayArea.appendText("邮件发送失败，请检查日志或重试。\n");
            }
        }).exceptionally(ex -> {
            statusDisplayArea.appendText("邮件发送失败: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
    }

    @FXML
    public void loadRecipientsFromTxt() {
        File selectedFile = chooseFile("选择 TXT 文件", "Text Files", "*.txt");
        if (selectedFile != null) {
            try {
                List<String> recipientEmails = extractValidEmailsFromFile(selectedFile.toPath().toString());
                if (!recipientEmails.isEmpty()) {
                    // 设置为List
                    recipientEmailTextField.setText(String.join(", ", recipientEmails));
                    statusDisplayArea.appendText(String.format("导入成功，共 %d 个有效收件人。\n", recipientEmails.size()));
                } else {
                    statusDisplayArea.appendText("未找到有效收件人，请检查文件内容。\n");
                }
            } catch (Exception e) {
                handleFileReadError((IOException) e);
            }
        }
    }


    @FXML
    public void loadHtmlContent() {
        File selectedFile = chooseFile("选择 HTML 文件", "HTML Files", "*.html");
        if (selectedFile != null) {
            try {
                String htmlContent = getFileContentString(selectedFile.toPath().toString());
                contentTextField.setText(htmlContent);
                statusDisplayArea.appendText("HTML 内容导入成功。\n");
            } catch (Exception e) {
                handleFileReadError((IOException) e);
            }
        }
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

    @FXML
    public void proxySettingsMenuButton() {
        Stage currentStage = (Stage) statusDisplayArea.getScene().getWindow();
        proxyService.openProxySettings(currentStage);
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

    private List<File> chooseMultipleFiles(String title, String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (description != null && extension != null) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        }
        Stage stage = (Stage) statusDisplayArea.getScene().getWindow();
        return fileChooser.showOpenMultipleDialog(stage);
    }

    private void handleFileReadError(IOException e) {
        statusDisplayArea.appendText("文件读取失败: " + e.getMessage() + "\n");
        e.printStackTrace();
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
            logger.warn("SendEMailController的statusDisplayArea 为 null，无法更新状态消息, 如未使用请忽略");
        }
    }

    @Override
    public void initialize() {
        emailService.setEmailStatusCallback(this);
    }

}
