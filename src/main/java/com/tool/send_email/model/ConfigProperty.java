package com.tool.send_email.model;

import com.tool.send_email.config.MailConfig;
import com.tool.send_email.config.ProxyConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 邮件配置实体类
 */

@Component
@Data
public class ConfigProperty {

    private final MailConfig mailConfig;
    private final ProxyConfig proxyConfig;

    @Autowired
    public ConfigProperty(MailConfig mailConfig, ProxyConfig proxyConfig) {
        this.mailConfig = mailConfig;
        this.proxyConfig = proxyConfig;
    }

    public void addAccount(MailConfig.MailAccount account) {
        this.mailConfig.getAccounts().add(account);
    }

    public void removeAccount(int index) {
        this.mailConfig.getAccounts().remove(index);
    }

    public void updateAccount(MailConfig.MailAccount account, int index) {
        this.mailConfig.getAccounts().set(index, account);
    }

    public MailConfig.MailAccount getAccount(int index) {
        return this.mailConfig.getAccounts().get(index);
    }

    public List<MailConfig.MailAccount> getAllAccounts() {
        return this.mailConfig.getAccounts();
    }

    public void setProxy(boolean enable, String type, String host, int port, String username, String password) {
        this.proxyConfig.setEnable(enable);
        this.proxyConfig.setType(type);
        this.proxyConfig.setHost(host);
        this.proxyConfig.setPort(port);
        this.proxyConfig.setUsername(username);
        this.proxyConfig.setPassword(password);
    }

    public boolean checkAccountId(int accountId) {
        for (MailConfig.MailAccount account : this.mailConfig.getAccounts()) {
            if (account.getId() == accountId && account != null) {
                return true;
            }
        }
        return false;
    }
}