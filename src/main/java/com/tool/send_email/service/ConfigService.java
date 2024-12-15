package com.tool.send_email.service;

import com.tool.send_email.config.MailConfig;
import com.tool.send_email.model.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static com.tool.send_email.utils.FileParserUtils.checkFileExist;

/**
 * 邮箱配置服务类
 */

@Service
public class ConfigService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private final ConfigProperty configProperty;

    @Autowired
    public ConfigService(ConfigProperty configProperty) {
        this.configProperty = configProperty;
    }

    // 添加邮箱配置
    public void addAccount(MailConfig.MailAccount account, int index) {
        // 添加到内存中的配置
        configProperty.addAccount(account);
        Properties properties = new Properties();

        // 设置邮箱账户的属性
        properties.setProperty(String.format("mail.accounts[%d].id", index), String.valueOf(index));
        properties.setProperty(String.format("mail.accounts[%d].host", index), account.getHost());
        properties.setProperty(String.format("mail.accounts[%d].port", index), String.valueOf(account.getPort()));
        properties.setProperty(String.format("mail.accounts[%d].ssl", index), String.valueOf(account.isSsl()));
        properties.setProperty(String.format("mail.accounts[%d].username", index), account.getUsername());
        properties.setProperty(String.format("mail.accounts[%d].password", index), account.getPassword());
        properties.setProperty(String.format("mail.accounts[%d].from", index), account.getFrom());
        properties.setProperty(String.format("mail.accounts[%d].nickname", index), account.getNickname());
        properties.setProperty(String.format("mail.accounts[%d].authrequired", index), String.valueOf(account.isAuthrequired()));

        // 保存配置到 config.properties 文件, 追加
        try (FileOutputStream fileOut = new FileOutputStream(checkFileExist(), true)) {
            //写入文件
            properties.store(fileOut, "\n Add mail configuration through the program");
            logger.info("添加邮件配置到文件: " + properties);
        } catch (IOException e) {
            logger.error("写入配置文件失败: " + e.getMessage());
        }
    }

    // 删除邮箱配置
    public void removeAccount(int index) {
        configProperty.removeAccount(index);
    }

    // 更新邮箱配置
    public void updateAccount(MailConfig.MailAccount account, int index) {
        configProperty.updateAccount(account, index);
    }

    // 获取某个邮箱配置
    public MailConfig.MailAccount getAccount(int index) {
        return configProperty.getAccount(index);
    }

    // 获取所有邮箱配置
    public List<MailConfig.MailAccount> getAllAccounts() {
        return configProperty.getAllAccounts();
    }

    public void setProxy(boolean enable, String type, String host, int port, String username, String password) {
        configProperty.setProxy(enable, type, host, port, username, password);
    }

    public boolean checkAccountId(int accountId) {
        return configProperty.checkAccountId(accountId);
    }

}
