package com.tool.send_email.javafx.controller;

import com.tool.send_email.config.MailConfig;
import com.tool.send_email.service.ConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

import static com.tool.send_email.utils.FileParserUtils.checkFileExist;

/**
 * 配置页面
 */

@Controller
public class GuiConfigController {

    @FXML
    private TextField smtpHostTextField;
    @FXML
    private TextField smtpPortTextField;
    @FXML
    private TextField smtpSslTextField;
    @FXML
    private TextField smtpUserNameTextField;
    @FXML
    private TextField smtpPassWordTextField;
    @FXML
    private TextField smtpFromTextField;
    @FXML
    private TextField smtpNickNameTextField;
    @FXML
    private TextField smtpConfigIdTextField;
    @FXML
    private TextField smtpAuthRequiredTextField;

    @FXML
    private TextArea resultArea;
    private ConfigService configService;

    public GuiConfigController() {
    }

    @Autowired
    public GuiConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @FXML
    public void addConfigurationOptionsMenuButton() {
        // 获取最后一个配置的id
        int lastIndex = configService.getAllAccounts().size();
        resultArea.appendText(String.format("请输入序号ID: %d, 并开始\n", lastIndex));
    }

    @FXML
    public void addButton() {
        MailConfig.MailAccount account = new MailConfig.MailAccount();
        if (isFormValid()) {
            account.setId(Integer.parseInt(smtpConfigIdTextField.getText()));
            account.setHost(smtpHostTextField.getText());
            account.setPort(Integer.parseInt(smtpPortTextField.getText()));
            account.setSsl(Boolean.parseBoolean(smtpSslTextField.getText()));
            account.setUsername(smtpUserNameTextField.getText());
            account.setPassword(smtpPassWordTextField.getText());
            account.setFrom(smtpFromTextField.getText());
            account.setNickname(smtpNickNameTextField.getText());
            account.setAuthrequired(Boolean.parseBoolean(smtpAuthRequiredTextField.getText()));

            int index = Integer.parseInt(smtpConfigIdTextField.getText());

            if (configService.checkAccountId(index)) {
                showConfirmationDialog(index, account);
            } else {
                configService.addAccount(account, index);
                resultArea.appendText("配置已添加, 请到配置文件确认\n");
            }
        }
    }

    @FXML
    public void getConfigurationOptionsMenuButton() {
        if (!smtpConfigIdTextField.getText().isEmpty()) {
            int index = Integer.parseInt(smtpConfigIdTextField.getText());
            MailConfig.MailAccount account = configService.getAccount(index);
            resultArea.appendText(account.toString().replace(",", "\n"));
        } else {
            resultArea.appendText("请输入序号ID, 从0开始\n并再次点击“查询配置”按钮\n");
        }

    }

    @FXML
    public void getAllConfigurationOptionsMenuButton() {
        List<MailConfig.MailAccount> accounts = configService.getAllAccounts();
        int size = accounts.size();

        resultArea.appendText(String.format("----------共查询到 %d 个配置----------\n", size));

        int index = 0;
        for (MailConfig.MailAccount account : accounts) {
            resultArea.appendText(String.format("第 %d 个配置:\n", index++));
            String formattedAccount = account.toString().replace(",", "\n");
            resultArea.appendText(formattedAccount + "\n\n");
        }
    }

    @FXML
    public void otherOperationsButton() {
        String path = checkFileExist();
        resultArea.appendText(String.format("\n请手动修改config.properties文件: %s\n", path));
    }

    private boolean isFormValid() {
        if (!smtpConfigIdTextField.getText().isEmpty()
                && !smtpHostTextField.getText().isEmpty()
                && !smtpPortTextField.getText().isEmpty()
                && !smtpSslTextField.getText().isEmpty()
                && !smtpUserNameTextField.getText().isEmpty()
                && !smtpPassWordTextField.getText().isEmpty()
                && !smtpFromTextField.getText().isEmpty()
                && !smtpNickNameTextField.getText().isEmpty()
                && !smtpAuthRequiredTextField.getText().isEmpty()
        ) {
            return true;

        } else {
            resultArea.appendText("请输入完整配置信息：\n");
            resultArea.appendText("ID, Host, Port, SSL, UserName, Password, From, NickName, AuthRequired\n");
            return false;
        }
    }

    private void showConfirmationDialog(int configId, MailConfig.MailAccount account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认覆盖配置");
        alert.setHeaderText("配置 ID " + configId + " 已存在！");
        alert.setContentText("是否覆盖现有配置？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            configService.addAccount(account, configId); // 覆盖现有配置
            resultArea.appendText("配置已被覆盖。\n");
        }
    }
}
