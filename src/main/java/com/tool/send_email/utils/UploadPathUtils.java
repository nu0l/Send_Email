package com.tool.send_email.utils;

import java.io.File;

/**
 * 上传文件目录：使用系统临时目录下的固定子目录，避免依赖 classpath（JAR 内 {@code getResource("/")} 不可用）。
 */
public final class UploadPathUtils {

    private static final String ROOT = "send-email-app";

    private UploadPathUtils() {
    }

    public static File resolveUploadSubdir(String subdir) {
        File base = new File(System.getProperty("java.io.tmpdir"), ROOT);
        File dir = new File(base, subdir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("无法创建上传目录: " + dir.getAbsolutePath());
        }
        return dir;
    }
}
