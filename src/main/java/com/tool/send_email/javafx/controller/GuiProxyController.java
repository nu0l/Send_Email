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
    private ComboBox<String> proxyTypeComboBox;
    @FXML
    private TextField proxyHostTextField;
    @FXML
    private TextField proxyPortTextField;
    @FXML
    private TextField proxyUsernameTextField;
    @FXML
    private TextField proxyPasswordTextField;
    @FXML
    private Button btnSaveProxy;
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

        if (proxyTypeComboBox != null
                && !proxyTypeComboBox.getItems().isEmpty()
                && proxyTypeComboBox.getValue() == null) {
            proxyTypeComboBox.getSelectionModel().selectFirst();
        }
    }

    // 获取代理启用状态的方法
    public boolean isEnable() {
        return enable;
    }


    @FXML
    public void handleSaveProxySettings() {
        boolean proxyOn = enableRadioButton != null && enableRadioButton.isSelected();
        enable = proxyOn;

        if (!proxyOn) {
            proxyService.unsetProxy(new Proxy(false, null, null, 0, null, null));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("代理");
            alert.setHeaderText(null);
            alert.setContentText("代理已关闭。");
            alert.showAndWait();
            notifyStatus("代理设置已清空");
            return;
        }

        String type = proxyTypeComboBox != null && proxyTypeComboBox.getValue() != null
                ? proxyTypeComboBox.getValue().trim()
                : "";
        String host = safeTrim(proxyHostTextField != null ? proxyHostTextField.getText() : null);
        String portStr = safeTrim(proxyPortTextField != null ? proxyPortTextField.getText() : null);

        if (type.isEmpty() || host.isEmpty() || portStr.isEmpty()) {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setTitle("代理配置不完整");
            warn.setHeaderText(null);
            warn.setContentText("启用代理时，请选择协议并填写主机与端口。");
            warn.showAndWait();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("端口无效");
            err.setHeaderText(null);
            err.setContentText("端口必须是有效数字。");
            err.showAndWait();
            return;
        }

        String username = safeTrimOrNull(proxyUsernameTextField != null ? proxyUsernameTextField.getText() : null);
        String password = safeTrimOrNull(proxyPasswordTextField != null ? proxyPasswordTextField.getText() : null);

        proxyService.setProxy(new Proxy(true, type, host, port, username, password));

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("代理");
        ok.setHeaderText(null);
        ok.setContentText("代理设置已保存。");
        ok.showAndWait();
        notifyStatus(String.format("代理设置已应用: %s://%s:%d", type, host, port));
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String safeTrimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
