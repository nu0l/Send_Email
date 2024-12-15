package com.tool.send_email.javafx.controller;

import com.tool.send_email.StartApplicationMain;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
            AnchorPane newView = loader.load();
            newView.setPrefWidth(346.0);
            newView.setPrefHeight(346.0);
            contentPane.getChildren().setAll(newView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
