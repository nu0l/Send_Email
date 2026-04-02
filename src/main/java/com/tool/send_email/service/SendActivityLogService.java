package com.tool.send_email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tool.send_email.model.SendActivityEntry;
import com.tool.send_email.utils.UploadPathUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 发送活动记录与简单统计（本地 JSONL 文件，位于系统临时目录下）。
 */
@Service
public class SendActivityLogService {

    private static final int MAX_LINES = 5000;
    private static final String FILE_NAME = "send-activity.jsonl";

    private final ObjectMapper objectMapper;

    public SendActivityLogService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private File logFile() {
        return new File(UploadPathUtils.resolveUploadSubdir("activity"), FILE_NAME);
    }

    /**
     * 追加一条发送记录（线程安全）。
     */
    public synchronized void record(String source, String subject, int recipientCount, boolean success, String detail) {
        SendActivityEntry e = new SendActivityEntry(
                System.currentTimeMillis(),
                source != null ? source : "UNKNOWN",
                subject != null ? subject : "",
                recipientCount,
                success,
                detail
        );
        try {
            String line = objectMapper.writeValueAsString(e);
            File f = logFile();
            try (BufferedWriter w = Files.newBufferedWriter(f.toPath(), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
                w.write(line);
                w.newLine();
            }
            trimIfNeeded(f);
        } catch (IOException ex) {
            throw new IllegalStateException("写入活动日志失败", ex);
        }
    }

    private void trimIfNeeded(File f) throws IOException {
        List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        if (lines.size() <= MAX_LINES) {
            return;
        }
        List<String> tail = lines.subList(lines.size() - MAX_LINES, lines.size());
        Files.write(f.toPath(), tail, StandardCharsets.UTF_8);
    }

    public synchronized List<SendActivityEntry> readRecent(int limit) {
        File f = logFile();
        if (!f.exists()) {
            return Collections.emptyList();
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Collections.emptyList();
        }
        List<SendActivityEntry> out = new ArrayList<>();
        for (int i = lines.size() - 1; i >= 0 && out.size() < limit; i--) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                out.add(objectMapper.readValue(line, SendActivityEntry.class));
            } catch (Exception ignored) {
                // 跳过损坏行
            }
        }
        return out;
    }

    public synchronized Map<String, Object> stats() {
        File f = logFile();
        Map<String, Object> map = new HashMap<>();
        if (!f.exists()) {
            map.put("total", 0);
            map.put("success", 0);
            map.put("fail", 0);
            map.put("bySource", Collections.emptyMap());
            map.put("last7Days", 0);
            return map;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            map.put("total", 0);
            map.put("success", 0);
            map.put("fail", 0);
            map.put("bySource", Collections.emptyMap());
            map.put("last7Days", 0);
            return map;
        }
        int total = 0;
        int success = 0;
        int fail = 0;
        Map<String, Integer> bySource = new HashMap<>();
        long weekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
        int last7 = 0;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                SendActivityEntry e = objectMapper.readValue(line, SendActivityEntry.class);
                total++;
                if (e.isSuccess()) {
                    success++;
                } else {
                    fail++;
                }
                String src = e.getSource() != null ? e.getSource() : "UNKNOWN";
                bySource.put(src, bySource.getOrDefault(src, 0) + 1);
                if (e.getTs() >= weekAgo) {
                    last7++;
                }
            } catch (Exception ignored) {
                // skip
            }
        }
        map.put("total", total);
        map.put("success", success);
        map.put("fail", fail);
        map.put("bySource", bySource);
        map.put("last7Days", last7);
        return map;
    }
}
