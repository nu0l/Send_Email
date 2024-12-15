package com.tool.send_email;

import com.tool.send_email.javafx.StartJavaFXApplication;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import static com.tool.send_email.utils.FileParserUtils.createFile;

/**
 * 启动入口
 */

@SpringBootApplication
@EnableAsync
public class StartApplicationMain {
    private static final Logger logger = LoggerFactory.getLogger(StartApplicationMain.class);
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) throws Exception {
        createFile();
        if (args.length > 0 && args[0].equals("gui")) {
            logger.info("gui mode");
            // 禁用 web
            System.setProperty("spring.main.web-application-type", "none");
            // 禁用 Spring Security
            System.setProperty("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration");
            context = SpringApplication.run(StartApplicationMain.class, args);
            Application.launch(StartJavaFXApplication.class, args);
        } else if (args.length > 0 && args[0].equals("web")) {
            logger.info("web mode");
            context = SpringApplication.run(StartApplicationMain.class, args);
        } else {
            System.out.println("use: java -jar send_email.jar [gui|web]");
        }
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }
}