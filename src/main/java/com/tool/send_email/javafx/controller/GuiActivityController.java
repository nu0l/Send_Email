package com.tool.send_email.javafx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tool.send_email.model.SendActivityEntry;
import com.tool.send_email.service.SendActivityLogService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

/**
 * 发送统计与最近记录（JavaFX）。
 */
@Controller
public class GuiActivityController {

    private final SendActivityLogService sendActivityLogService;
    private final ObjectMapper objectMapper;

    @FXML
    private TextArea statsArea;
    @FXML
    private TextArea recordsArea;

    public GuiActivityController(SendActivityLogService sendActivityLogService, ObjectMapper objectMapper) {
        this.sendActivityLogService = sendActivityLogService;
        this.objectMapper = objectMapper;
    }

    @FXML
    public void initialize() {
        refresh();
    }

    @FXML
    public void refresh() {
        try {
            Map<String, Object> stats = sendActivityLogService.stats();
            statsArea.setText(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stats));
        } catch (Exception e) {
            statsArea.setText("读取统计失败: " + e.getMessage());
        }
        try {
            List<SendActivityEntry> recent = sendActivityLogService.readRecent(80);
            StringBuilder sb = new StringBuilder();
            for (SendActivityEntry e : recent) {
                sb.append(String.format("[%tF %<tT] %s | %s | 收件人×%d | %s%n",
                        e.getTs(),
                        e.getSource(),
                        truncate(e.getSubject(), 48),
                        e.getRecipientCount(),
                        e.isSuccess() ? "成功" : "失败"));
                if (e.getDetail() != null && !e.getDetail().isEmpty()) {
                    sb.append("  ").append(e.getDetail()).append("\n");
                }
            }
            if (sb.length() == 0) {
                sb.append("暂无记录。发送邮件成功或失败后会在此追加条目。");
            }
            recordsArea.setText(sb.toString());
        } catch (Exception e) {
            recordsArea.setText("读取记录失败: " + e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
