package com.tool.send_email.javafx.controller;

import com.tool.send_email.StartApplicationMain;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.springframework.stereotype.Controller;

import java.io.IOException;

/**
 * 主界面
 */

@Controller
public class GuiMainController {

    @FXML
    private Button readMeButton;
    @FXML
    private Button configButton;
    @FXML
    private Button sendEmailButton;
    @FXML
    private Button emlToHtmlButton;
    @FXML
    private Button customEmailButton;
    @FXML
    private Button proxyButton;
    @FXML
    private Button activityButton;
    @FXML
    private Button drillButton;
    @FXML
    private AnchorPane contentPane;

    public GuiMainController() {
    }

    @FXML
    public void initialize() {
        loadView("/static/views/fxml/ReadMeControllerView.fxml");

        // 为每个按钮绑定事件
        readMeButton.setOnAction(event -> loadView("/static/views/fxml/ReadMeControllerView.fxml"));
        configButton.setOnAction(event -> loadView("/static/views/fxml/ConfigControllerView.fxml"));
        emlToHtmlButton.setOnAction(event -> loadView("/static/views/fxml/emlToHtmlControllerView.fxml"));
        sendEmailButton.setOnAction(event -> loadView("/static/views/fxml/SendEMailControllerView.fxml"));
        customEmailButton.setOnAction(event -> loadView("/static/views/fxml/CustomEmailControllerView.fxml"));
        if (proxyButton != null) {
            proxyButton.setOnAction(event -> loadView("/static/views/fxml/ProxyControllerView.fxml"));
        }
        if (activityButton != null) {
            activityButton.setOnAction(event -> loadView("/static/views/fxml/ActivityControllerView.fxml"));
        }
        if (drillButton != null) {
            drillButton.setOnAction(event -> loadView("/static/views/fxml/DrillTemplatesControllerView.fxml"));
        }
    }

    /**
     * 动态加载指定的 FXML 视图
     *
     * @param fxmlPath FXML 文件的路径
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(StartApplicationMain.getContext()::getBean);
            Parent newView = loader.load();
            contentPane.getChildren().setAll(newView);
            AnchorPane.setTopAnchor(newView, 0.0);
            AnchorPane.setRightAnchor(newView, 0.0);
            AnchorPane.setBottomAnchor(newView, 0.0);
            AnchorPane.setLeftAnchor(newView, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
