package com.tool.send_email.utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CSV 文件解析工具类
 */

@Component
public class CsvParserUtils {
    private static final Logger logger = LoggerFactory.getLogger(CsvParserUtils.class);

    private final TemplateEngine templateEngine;

    @Autowired
    public CsvParserUtils(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    // 读取 CSV 文件
    public static List<Map<String, String>> readCsv(String filePath) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] headerRow = reader.readNext();
            if (headerRow == null) {
                return records;
            }
            List<String> headers = Arrays.asList(headerRow);

            // 去除列头中的空格和 BOM 字符
            headers = cleanHeaders(headers);

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                Map<String, String> record = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String cell = i < nextLine.length && nextLine[i] != null ? nextLine[i].trim() : "";
                    record.put(headers.get(i), cell);
                }
                records.add(record);
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        return records;
    }

    public static List<Map<String, String>> extractValidEmailsFromCsv(List<Map<String, String>> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用正则表达式过滤有效的记录
        List<Map<String, String>> validRecords = records.stream()
                .filter(record -> {
                    String email = record.get("toEmail");
                    if (email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        return true; // 保留有效的记录
                    } else {
                        logger.warn("记录无效：{}", record);
                        return false; // 过滤掉无效的记录
                    }
                })
                .collect(Collectors.toList());

        logger.info("从 CSV 中提取到 {} 条有效记录", validRecords.size());
        return validRecords;
    }


    // 清理列头中的空白字符与 UTF-8 BOM，保留中文等列名
    private static List<String> cleanHeaders(List<String> headers) {
        List<String> cleanedHeaders = new ArrayList<>();
        for (String header : headers) {
            cleanedHeaders.add(stripBomAndTrim(header));
        }
        return cleanedHeaders;
    }

    private static String stripBomAndTrim(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        if (t.startsWith("\uFEFF")) {
            t = t.substring(1).trim();
        }
        return t;
    }

}
