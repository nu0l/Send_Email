package com.tool.send_email.javafx.controller;

import com.tool.send_email.model.EmailStatusCallback;
import com.tool.send_email.model.Proxy;
import com.tool.send_email.service.ConfigService;
import com.tool.send_email.service.ProxyService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * 代理设置页面
 */

@Controller
public class GuiProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private ProxyService proxyService;
    private Proxy proxy;
    private EmailStatusCallback callback;

    public GuiProxyController(){}

    @Autowired
    public GuiProxyController(ProxyService proxyService){
        this.proxyService = proxyService;
    }

    @FXML
    private ComboBox proxyTypeComboBox;
    @FXML
    private TextField proxyHostTextField;
    @FXML
    private TextField proxyPortTextField;
    @FXML
    private TextField proxyUsernameTextField;
    @FXML
    private TextField proxyPasswordTextField;
    @FXML
    private Button saveProxySettingButton;
    @FXML
    private ToggleGroup proxyToggleGroup;
    @FXML
    private RadioButton enableRadioButton;
    @FXML
    private RadioButton disableRadioButton;
    private boolean enable;

    @FXML
    public void initialize() {
        // 创建并绑定 ToggleGroup
        proxyToggleGroup = new ToggleGroup();
        enableRadioButton.setToggleGroup(proxyToggleGroup);
        disableRadioButton.setToggleGroup(proxyToggleGroup);

        // 添加监听器以处理选中状态的切换
        proxyToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == enableRadioButton) {
                enable = true; // 更新 enable 为 true
                logger.info("选择了开启代理");
            } else if (newValue == disableRadioButton) {
                enable = false; // 更新 enable 为 false
                logger.info("选择了关闭代理");
            }
        });

        // 初始化默认状态
        enable = enableRadioButton.isSelected(); // 根据默认选择更新 enable 的值
        logger.info(enable ? "代理默认开启" : "代理默认关闭");
    }

    // 获取代理启用状态的方法
    public boolean isEnable() {
        return enable;
    }


    @FXML
    public void saveProxySettingButton() {
        // 使用信息类型的弹窗
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("代理状态");
        alert.setHeaderText("Jvav");

        if (enable) {
            String type = proxyTypeComboBox.getValue().toString();
            String host = proxyHostTextField.getText();
            String port = proxyPortTextField.getText();
            String username = proxyUsernameTextField.getText();
            String password = proxyPasswordTextField.getText();

            if (!type.isEmpty() || !host.isEmpty() || !port.isEmpty()) {
                username = username.isEmpty() ? null : username;
                password = password.isEmpty() ? null : password;
                proxyService.setProxy(new Proxy(enable, type, host, Integer.parseInt(port), username, password));

                // 设置弹窗内容并显示
                alert.setContentText("代理设置已保存\n");
                alert.showAndWait();
                notifyStatus(String.format("代理设置已应用: %s://%s:%s", type, host, port));
            }
        } else {
            proxyService.unsetProxy(new Proxy(enable, null, null, 0, null, null));

            // 设置弹窗内容并显示
            alert.setContentText("代理设置已关闭");
            alert.showAndWait();
            notifyStatus("代理设置已清空");
        }
    }

    /**
     * 设置邮件发送状态回调接口
     * @param callback
     */
    public void setEmailStatusCallback(EmailStatusCallback callback) {
        this.callback = callback; // 设置回调接口
    }

    /**
     * 通知界面更新邮件发送状态
     * @param message
     */
    private void notifyStatus(String message) {
        if (callback != null) {
            callback.onStatusUpdate(message); // 调用回调接口的方法来通知界面
        }
    }
}
