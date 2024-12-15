package com.tool.send_email.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 程序说明界面
 */

@Controller
public class GuiReadMeController {

    @FXML
    private WebView webView;

    public GuiReadMeController() {
    }

    public void initialize() throws Exception {
        String readmePath = "static/GUI_README.html";
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(readmePath);

        if (resourceStream == null) {
            throw new IOException("README File not found: " + readmePath);
        }

        // 替代 readAllBytes 的实现
        String htmlContent = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        webView.getEngine().loadContent(htmlContent);
    }

}
