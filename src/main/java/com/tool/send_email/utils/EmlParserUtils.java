package com.tool.send_email.utils;

import com.tool.send_email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * EML 文件解析工具类
 */

public class EmlParserUtils {

    /**
     * 解析 EML 文件，提取其中的 HTML 内容，并处理嵌入的图片
     *
     * @param emlPath EML 文件路径
     * @return 提取的 HTML 内容，如果没有找到 HTML 内容，则返回空字符串
     * @throws Exception 异常
     */
    private static final Logger logger = LoggerFactory.getLogger(EmlParserUtils.class);

    public static String parseEmlFile(FileInputStream emlCode) throws Exception {
        // 设置邮件会话的属性
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            MimeMessage message = new MimeMessage(session, emlCode);

            // 解析邮件内容
            Object content = message.getContent();
            if (content instanceof Multipart) {
                return extractHtmlFromMultipart((Multipart) content);
            } else if (content instanceof String) {
                logger.info("邮件内容是纯文本: {}", content);
                return (String) content;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.error("无法解析 EML 文件");
        return "";
    }

    /**
     * 递归提取 multipart 部分中的 HTML 内容，并处理嵌入的图片
     *
     * @param multipart Multipart 邮件部分
     * @return 提取的 HTML 内容
     * @throws Exception 异常
     */
    private static String extractHtmlFromMultipart(Multipart multipart) throws Exception {
        StringBuilder htmlContent = new StringBuilder();
        Map<String, String> embeddedImages = new HashMap<>();

        // 遍历邮件的所有部分
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            String contentType = bodyPart.getContentType();

            if (contentType.contains("text/html")) {
                logger.info("邮件内容是 HTML: {}", contentType);
                htmlContent.append((String) bodyPart.getContent());
            } else if (contentType.contains("image")) {
                logger.info("邮件内容是图片: {}", contentType);
                String contentId = getContentId(bodyPart);
                if (contentId != null) {
                    String base64Image = extractBase64Data(bodyPart);
                    embeddedImages.put(contentId, base64Image);
                }
            }
            // 如果该部分是另一个 multipart，递归处理
            else if (bodyPart.getContent() instanceof Multipart) {
                htmlContent.append(extractHtmlFromMultipart((Multipart) bodyPart.getContent()));
            }
        }

        if (htmlContent.length() > 0) {
            logger.info("处理 HTML 中的嵌入图片...");
            return handleEmbeddedImages(htmlContent.toString(), embeddedImages);
        }
        logger.info("没有找到 HTML 内容");
        return "";
    }

    /**
     * 获取邮件中的 Content-ID
     *
     * @param bodyPart 邮件的一部分
     * @return Content-ID 值
     */
    private static String getContentId(BodyPart bodyPart) throws MessagingException {
        String[] contentIdHeader = bodyPart.getHeader("Content-ID");
        if (contentIdHeader != null && contentIdHeader.length > 0) {
            return contentIdHeader[0].replaceAll("[<>]", ""); // 去除 < 和 >
        }
        return null;
    }

    /**
     * 处理 HTML 中的嵌入式图片，将其转换为 base64 编码格式
     *
     * @param htmlContent    HTML 内容
     * @param embeddedImages 嵌入图片的映射
     * @return 处理后的 HTML 内容
     */
    private static String handleEmbeddedImages(String htmlContent, Map<String, String> embeddedImages) {
        // 使用 StringBuilder 构造替换后的 HTML
        StringBuilder updatedHtmlContent = new StringBuilder(htmlContent);

        // 替换 HTML 中的 cid 引用为 base64 编码
        for (Map.Entry<String, String> entry : embeddedImages.entrySet()) {
            String cid = entry.getKey();
            String base64Image = entry.getValue();

            // 构造 data URI
            String dataUri = "data:image/png;base64," + base64Image;

            // 替换 HTML 中的 src="cid:xxxx" 为 data URI
            int startIndex = 0;
            while ((startIndex = updatedHtmlContent.indexOf("cid:" + cid, startIndex)) != -1) {
                updatedHtmlContent.replace(startIndex, startIndex + ("cid:" + cid).length(), dataUri);
                startIndex += dataUri.length();
            }
        }

        return updatedHtmlContent.toString();
    }

    /**
     * 从邮件部分中提取 base64 编码的图片数据
     *
     * @param bodyPart 邮件的一个 body 部分
     * @return base64 编码的图片数据
     * @throws Exception 异常
     */
    private static String extractBase64Data(BodyPart bodyPart) throws Exception {
        try (InputStream inputStream = bodyPart.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            // 将图片数据转换为 base64 编码
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        }
    }

    /**
     * 保存 HTML 内容为文件
     *
     * @param htmlContent        HTML 内容
     * @param outputHtmlFilePath 输出 HTML 文件路径
     * @throws IOException
     */
    public static void saveToHtmlFile(String htmlContent, String outputHtmlFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputHtmlFilePath))) {
            writer.write(htmlContent); // 将 HTML 内容保存到文件
        }
    }

}