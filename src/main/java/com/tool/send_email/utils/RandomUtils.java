package com.tool.send_email.utils;

import java.util.Random;

/**
 * 干扰字符工具类
 */

public class RandomUtils {
    private static final Random random = new Random();

    // 定义不可见字符
    private static final char[] INVISIBLE_CHARS = {
            '\u200B', '\uFEFF', '\u200D', '\u200C', '\u200E', '\u200F', '\u2060', '\u2061', '\u2062', '\u2063'
    };

    // 随机插入的隐藏文本内容
    private static final String[] INVISIBLE_STATEMENT = {
            "您的订单已成功付款，感谢您的支持",
            "这封电子邮件由系统自动生成，请勿回复",
            "我们发现您的账号在一部设备上有新的登录活动",
            "您购买的商品，电子发票已经开具成功，如有开票问题，可直接联系商家",
            "请参阅随附的电子发票并保存以备将来参考",
            "恭喜你通过校园招聘面试考核",
            "您在中国铁路客户服务中心网站(12306)成功办理了候补购票业务， 成功兑现了2张车票",
            "您好，感谢使用，您的账号正在使用邮箱验证，本次请求的验证码为363697，5分钟内有效",
            "开发者您好，准备迎接新一年的全球开发者活动",
            "您的预约即将开始，请查看以下步骤", // added
    };

    // 随机替换不可见字符串
    public static String insertInvisibleChars(String text) {
        StringBuilder sb = new StringBuilder();
        boolean insideTag = false; // 标记是否在 HTML 标签内

        for (char c : text.toCharArray()) {
            if (c == '<') {
                insideTag = true; // 进入 HTML 标签
            } else if (c == '>') {
                insideTag = false; // 退出 HTML 标签
            }

            sb.append(c);

            // 随机决定是否插入不可见字符，但避免在 HTML 标签内部插入
            if (!insideTag && random.nextBoolean()) {
                char invisibleChar = INVISIBLE_CHARS[random.nextInt(INVISIBLE_CHARS.length)];
                sb.append(invisibleChar);
            }
        }
        return sb.toString();
    }

    // 随机生成隐藏的干扰字符段
    private static String generateHiddenSpan() {
        String interferenceChar = INVISIBLE_STATEMENT[random.nextInt(INVISIBLE_STATEMENT.length)];
        int randomStyle = random.nextInt(4);

        switch (randomStyle) {
            case 0:
                return "<span style=\"font-size:0px;opacity:0;\">" + interferenceChar + "</span>";
            case 1:
                return "<span style=\"position:absolute;left:-9999px;\">" + interferenceChar + "</span>";
            case 2:
                return "<p><span style=\"display:none;\">" + interferenceChar + "</span></p>";
            case 3:
                return "<img src=\"data:image/gif;base64,R0lGODlhAQABAAAAACw=\" alt=\"" + interferenceChar + "\" style=\"display:none;\" />";
            default:
                return "<span style=\"font-size:0px;opacity:0;\">" + interferenceChar + "</span>";
        }
    }

    // 在HTML中插入干扰字符
    // 思路来源： https://mp.weixin.qq.com/s/twWHxHKVEXsje7dnNlGYYw
    public static String insertHiddenCharacters(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return htmlContent; // 原始内容为空直接返回
        }

        // 在内容末尾插入隐藏字符
        return htmlContent + generateHiddenSpan();
    }

}
