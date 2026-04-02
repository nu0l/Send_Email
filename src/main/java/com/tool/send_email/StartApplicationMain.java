package com.tool.send_email;

import com.tool.send_email.config.LateMvcValidatorAutoConfiguration;
import com.tool.send_email.javafx.StartJavaFXApplication;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tool.send_email.utils.FileParserUtils.createFile;

/**
 * 启动入口（使用 {@link Configuration} + {@link EnableAutoConfiguration} + {@link ComponentScan}，
 * 以便排除 {@link LateMvcValidatorAutoConfiguration} 的组件扫描，仅由 spring.factories 注册）。
 */

@Configuration
@EnableAutoConfiguration
@ComponentScan(
        basePackages = "com.tool.send_email",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = LateMvcValidatorAutoConfiguration.class))
@EnableAsync
public class StartApplicationMain {

    /**
     * 不在此使用 {@code @SpringBootApplication(exclude=...)}，否则会提前加载含 javax.validation 字节码引用的自动配置类。
     * 通过命令行参数排除，优先级高于 ~/.config/sendEmail/config.properties 等外部 PropertySource，避免被覆盖。
     */
    private static final String EXCLUDE_VALIDATION =
            "org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration";

    private static final Logger logger = LoggerFactory.getLogger(StartApplicationMain.class);
    private static ConfigurableApplicationContext context;

    public static void main(String[] args) throws Exception {
        createFile();
        if (args.length > 0 && args[0].equals("gui")) {
            logger.info("gui mode");
            System.setProperty("spring.main.web-application-type", "none");
            context = runSpring(springBootArgs(args, true));
            Application.launch(StartJavaFXApplication.class, args);
        } else if (args.length > 0 && args[0].equals("web")) {
            logger.info("web mode");
            context = runSpring(springBootArgs(args, false));
        } else {
            System.out.println("use: java -jar send_email.jar [gui|web]");
        }
    }

    private static String[] springBootArgs(String[] userArgs, boolean guiMode) {
        List<String> list = new ArrayList<>(Arrays.asList(userArgs));
        if (guiMode) {
            list.add("--spring.autoconfigure.exclude="
                    + "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,"
                    + EXCLUDE_VALIDATION);
        } else {
            list.add("--spring.autoconfigure.exclude=" + EXCLUDE_VALIDATION);
        }
        return list.toArray(new String[0]);
    }

    private static ConfigurableApplicationContext runSpring(String[] args) {
        SpringApplication app = new SpringApplication(StartApplicationMain.class);
        app.setAllowBeanDefinitionOverriding(true);
        return app.run(args);
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }
}