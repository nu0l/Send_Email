package com.tool.send_email.service;

import com.tool.send_email.StartApplicationMain;
import com.tool.send_email.model.Proxy;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.tool.send_email.utils.FileParserUtils.checkFileExist;

/**
 * 代理服务
 */

@Service
public class ProxyService {
    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

    private Proxy proxy;

    /**
     * 清除代理设置
     */
    public void unsetProxy(Proxy proxy) {
        if (!proxy.isEnable()) {
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
            updateProxyConfig(proxy);
            logger.info("清空代理");
        }
    }

    /**
     * 应用代理设置
     */
    private void applyProxy(String type, String host, int port, String username, String password) {
        try {
            // 设置代理属性
            switch (type.toLowerCase()) {
                case "http":
                    System.setProperty("http.proxyHost", host);
                    System.setProperty("http.proxyPort", String.valueOf(port));
                    break;
                case "https":
                    System.setProperty("https.proxyHost", host);
                    System.setProperty("https.proxyPort", String.valueOf(port));
                    break;
                case "socks5":
                    System.setProperty("socksProxyHost", host);
                    System.setProperty("socksProxyPort", String.valueOf(port));
                    break;
                default:
                    logger.error("Invalid proxy type: {}", type);
                    return;
            }

            // 设置代理认证信息（如果有）
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                System.setProperty(type + ".proxyUser", username);
                System.setProperty(type + ".proxyPassword", password);
            }

            logger.info("已应用代理设置: {}://{}:{}", type, host, port);
        } catch (Exception e) {
            logger.error("应用代理设置失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新代理配置到文件
     */
    public void updateProxyConfig(Proxy proxy) {
        try {
            this.proxy = proxy;
            String filePath = checkFileExist();

            if (filePath == null) {
                logger.error("配置文件不存在！");
                return;
            }

            // 创建配置对象
            PropertiesConfiguration config = new PropertiesConfiguration();
            File configFile = new File(filePath);

            // 使用 FileReader 来读取配置文件
            try (FileReader reader = new FileReader(configFile)) {
                config.read(reader);  // 读取现有配置
            } catch (IOException | ConfigurationException e) {
                logger.error("无法读取配置文件: {}", e.getMessage());
                return;
            }

            config.setIOFactory(new PropertiesConfiguration.JupIOFactory(false)); // 禁用Unicode自动转换
            // 更新代理配置
            config.setProperty("proxy.enable", String.valueOf(proxy.isEnable()));
            config.setProperty("proxy.type", proxy.getType() == null ? "" : proxy.getType());
            config.setProperty("proxy.host", proxy.getHost() == null ? "" : proxy.getHost());
            config.setProperty("proxy.port", String.valueOf(proxy.getPort()));
            config.setProperty("proxy.username", proxy.getUsername() == null ? "" : proxy.getUsername());
            config.setProperty("proxy.password", proxy.getPassword() == null ? "" : proxy.getPassword());

            // 使用 FileWriter 来保存配置
            try (FileWriter writer = new FileWriter(configFile)) {
                config.write(writer);  // 写入配置文件
                logger.info("代理配置已成功更新, 检查配置文件: {}", filePath);
            } catch (IOException e) {
                logger.error("保存配置文件失败: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("更新代理配置失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取当前代理配置
     */
    public Proxy getProxy() {
        return this.proxy;
    }

    /**
     * 设置代理并更新配置
     */
    public void setProxy(Proxy proxy) {
        if (proxy.isEnable()) {
            applyProxy(proxy.getType(), proxy.getHost(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        }
        updateProxyConfig(proxy);
    }

    /**
     * 打开代理设置窗口
     */
    public void openProxySettings(Stage ownerStage) {
        try {
            Stage newStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/static/views/fxml/ProxyControllerView.fxml"));
            fxmlLoader.setControllerFactory(StartApplicationMain.getContext()::getBean);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            newStage.setTitle("代理设置");
            newStage.setScene(scene);
            if (ownerStage != null) {
                newStage.initOwner(ownerStage);
            }
            newStage.show();
        } catch (IOException e) {
            logger.error("打开代理设置窗口失败", e);
        }
    }
}