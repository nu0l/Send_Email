package com.tool.send_email.utils;

import com.tool.send_email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 文件解析工具类
 */

public class FileParserUtils {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * 读取文件内容返回List
     * 逐个检查文件内容是否是邮箱格式
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static List<String> extractValidEmailsFromFile(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);
        Set<String> uniqueEmails = new LinkedHashSet<>();

        for (String line : lines) {
            if (line.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                uniqueEmails.add(line);
            } else {
                logger.warn("第{}行 {}, 非邮箱格式, 已删除", lines.indexOf(line) + 1, line);
            }
        }

        return new ArrayList<>(uniqueEmails);
    }

    /**
     * 读取文件内容返回String
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String getFileContentString(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }

    // 创建配置文件
    public static void createFile() {
        String userHome = System.getProperty("user.home");
        String folderPath = userHome + "/.config/sendEmail/";
        String fileName = "config.properties";
        if (!Files.exists(Paths.get(folderPath))) {
            try {
                Files.createDirectories(Paths.get(folderPath));
            } catch (Exception e) {
                logger.error("创建文件夹失败: {}", e.getMessage());
            }
        }
        if (!Files.exists(Paths.get(folderPath + fileName))) {
            try {
                Files.createFile(Paths.get(folderPath + fileName));
            } catch (Exception e) {
                logger.error("创建文件失败: {}", e.getMessage());
            }
        }
    }

    // 验证配置文件是否存在
    public static String checkFileExist() {
        String userHome = System.getProperty("user.home");
        String folderPath = userHome + "/.config/sendEmail/";
        String fileName = "config.properties";
        if (Files.exists(Paths.get(folderPath + fileName))) {
            return folderPath + fileName;
        } else {
            return null;
        }
    }
}