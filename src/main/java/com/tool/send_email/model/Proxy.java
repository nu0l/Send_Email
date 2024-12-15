package com.tool.send_email.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代理配置实体类
 */

@Data
@NoArgsConstructor
public class Proxy {
    private boolean enable = false; // 默认禁用代理
    private String type;
    private String host;
    private int port;
    private String username;
    private String password;

    public Proxy(boolean enable, String type, String host, int port, String username, String password) {
        this.enable = enable;
        this.type = type;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }
}
