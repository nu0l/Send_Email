package com.tool.send_email.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 单次发送活动记录（持久化为 JSON 行）。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendActivityEntry {

    private long ts;
    private String source;
    private String subject;
    private int recipientCount;
    private boolean success;
    private String detail;

    public SendActivityEntry() {
    }

    public SendActivityEntry(long ts, String source, String subject, int recipientCount, boolean success, String detail) {
        this.ts = ts;
        this.source = source;
        this.subject = subject;
        this.recipientCount = recipientCount;
        this.success = success;
        this.detail = detail;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
