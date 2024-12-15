package com.tool.send_email.model;

/**
 * 邮件发送状态回调接口
 */

public interface EmailStatusCallback {
    void initialize();

    void onStatusUpdate(String message);
}
