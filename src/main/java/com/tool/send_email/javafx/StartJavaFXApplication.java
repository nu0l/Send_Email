package com.tool.send_email.javafx;

import com.tool.send_email.StartApplicationMain;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * JavaFX 启动类
 */

public class StartJavaFXApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        loadView("/static/views/fxml/MainControllerView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            // 设置控制器工厂，以便 Spring 管理控制器
            fxmlLoader.setControllerFactory(StartApplicationMain.getContext()::getBean);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 800, 740);
            String css = Objects.requireNonNull(
                    getClass().getResource("/static/styles/javafx-app.css"),
                    "javafx-app.css").toExternalForm();
            scene.getStylesheets().add(css);
            Stage stage = new Stage();
            stage.setTitle("Send Email | by: iak3ec");
            URL iconUrl = getClass().getResource("/static/images/favicon.png");
            if (iconUrl != null) {
                stage.getIcons().add(new Image(iconUrl.toString()));
            }
            stage.setScene(scene);
            stage.setMinWidth(680);
            stage.setMinHeight(560);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
