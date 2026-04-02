package com.tool.send_email.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 干扰字符工具类
 */

public class RandomUtils {
    // 是否启用“混淆/反检测”注入（默认打开）
    private static volatile boolean obfuscationEnabled = true;
    // 强度倍率：用于在现有规则基础上进一步增强注入量
    private static volatile double strengthMultiplier = 1.0;

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

    /**
     * 注入强度配置：用于“更随机/更强反检测”。
     */
    public static class ObfuscationOptions {
        // 每个文本字符位置注入不可见字符的概率
        public double invisibleCharProbability = 0.48;
        // 对整个内容的最大注入次数，避免爆炸性膨胀
        public int maxInvisibleInserts = 3200;
        // 生成隐藏语句的个数范围（插入在 tag 之间）
        public int minHiddenSpanCount = 2;
        public int maxHiddenSpanCount = 6;
        // 避免注入到 <script> / <style> 内部
        public boolean skipScriptStyle = true;
    }

    private static ObfuscationOptions defaultOptions() {
        return new ObfuscationOptions();
    }

    private static ObfuscationOptions optionsForSource(String activitySource) {
        ObfuscationOptions opt = defaultOptions();
        if (activitySource == null) return opt;
        String s = activitySource.trim().toUpperCase(Locale.ROOT);
        if ("SEND".equals(s)) {
            opt.invisibleCharProbability = 0.40;
            opt.maxInvisibleInserts = 2600;
            opt.minHiddenSpanCount = 1;
            opt.maxHiddenSpanCount = 4;
        } else if ("WEB".equals(s)) {
            opt.invisibleCharProbability = 0.45;
            opt.maxInvisibleInserts = 2900;
            opt.minHiddenSpanCount = 1;
            opt.maxHiddenSpanCount = 5;
        } else if ("GUI".equals(s)) {
            // default：更强随机
            opt.invisibleCharProbability = 0.50;
            opt.maxInvisibleInserts = 3300;
            opt.minHiddenSpanCount = 2;
            opt.maxHiddenSpanCount = 7;
        } else if ("CUSTOM".equals(s)) {
            opt.invisibleCharProbability = 0.52;
            opt.maxInvisibleInserts = 3600;
            opt.minHiddenSpanCount = 2;
            opt.maxHiddenSpanCount = 7;
        }
        return applyStrengthMultiplier(opt);
    }

    private static ObfuscationOptions applyStrengthMultiplier(ObfuscationOptions opt) {
        double m = strengthMultiplier;
        if (m <= 0) m = 1.0;
        // clamp 概率与注入量，避免极端 multiplier 导致内容膨胀到不可控
        opt.invisibleCharProbability = clamp(opt.invisibleCharProbability * m, 0.01d, 0.95d);
        opt.maxInvisibleInserts = (int) Math.max(1L, Math.round(opt.maxInvisibleInserts * m));
        opt.minHiddenSpanCount = (int) Math.max(0L, Math.round(opt.minHiddenSpanCount * m));
        opt.maxHiddenSpanCount = (int) Math.max(opt.minHiddenSpanCount, Math.round(opt.maxHiddenSpanCount * m));
        return opt;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    /**
     * Spring 启动时注入配置。
     * @param enabled 是否启用
     * @param multiplier 强度倍率
     */
    public static void configure(boolean enabled, double multiplier) {
        obfuscationEnabled = enabled;
        if (multiplier > 0) {
            strengthMultiplier = multiplier;
        }
    }

    // 随机替换不可见字符串（默认更强随机强度）
    public static String insertInvisibleChars(String text) {
        return insertInvisibleChars(text, defaultOptions());
    }

    public static String insertInvisibleChars(String text, ObfuscationOptions options) {
        if (text == null || text.isEmpty()) return text;
        if (options == null) options = defaultOptions();

        String lower = options.skipScriptStyle ? text.toLowerCase(Locale.ROOT) : null;
        boolean insideTag = false; // 标记是否在 HTML 标签内（<...>）
        boolean scriptStyleSkip = false; // 是否处于 <script>/<style> 内容区间
        boolean willClearAfterTag = false; // 遇到 </script>/<style> 起标，tag 结束后恢复
        int insertedCount = 0;

        StringBuilder sb = new StringBuilder(text.length() + 64);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (!insideTag && c == '<') {
                insideTag = true;
                if (options.skipScriptStyle && lower != null) {
                    if (lower.startsWith("<script", i) || lower.startsWith("<style", i)) {
                        scriptStyleSkip = true;
                    }
                    if (lower.startsWith("</script", i) || lower.startsWith("</style", i)) {
                        willClearAfterTag = true;
                    }
                }
            } else if (insideTag && c == '>') {
                insideTag = false;
                if (willClearAfterTag) {
                    scriptStyleSkip = false;
                    willClearAfterTag = false;
                }
            }

            sb.append(c);

            // 仅在“文本区域”（不在标签内部，且不在 script/style 内容区间）注入不可见字符
            if (!insideTag && !scriptStyleSkip) {
                if (insertedCount < options.maxInvisibleInserts
                        && ThreadLocalRandom.current().nextDouble() < options.invisibleCharProbability) {
                    char invisibleChar = INVISIBLE_CHARS[ThreadLocalRandom.current().nextInt(INVISIBLE_CHARS.length)];
                    sb.append(invisibleChar);
                    insertedCount++;
                }
            }
        }
        return sb.toString();
    }

    // 随机生成隐藏的干扰字符段
    private static String generateHiddenSpan() {
        String interferenceChar = INVISIBLE_STATEMENT[ThreadLocalRandom.current().nextInt(INVISIBLE_STATEMENT.length)];
        // 绝对避免“邮件客户端清洗 style 后导致干扰文本显示”的问题：
        // 使用 HTML 注释注入（注释在渲染层面不可见）。
        // 如果内容包含 "-->" 会破坏注释结构，这里做简单兜底替换。
        String safe = interferenceChar == null ? "" : interferenceChar.replace("--", "- -");
        return "<!--" + safe + "-->";
    }

    // 在HTML中插入干扰字符
    // 思路来源： https://mp.weixin.qq.com/s/twWHxHKVEXsje7dnNlGYYw
    public static String insertHiddenCharacters(String htmlContent) {
        return insertHiddenCharacters(htmlContent, defaultOptions());
    }

    public static String insertHiddenCharacters(String htmlContent, ObfuscationOptions options) {
        if (htmlContent == null || htmlContent.isEmpty()) return htmlContent;
        if (options == null) options = defaultOptions();

        int spanCount = ThreadLocalRandom.current().nextInt(options.minHiddenSpanCount,
                options.maxHiddenSpanCount + 1);
        if (spanCount <= 0) return htmlContent;

        String lower = options.skipScriptStyle ? htmlContent.toLowerCase(Locale.ROOT) : null;
        boolean insideTag = false;
        boolean scriptStyleSkip = false;
        boolean willClearAfterTag = false;

        // 候选插入点：tag 结束后且下一个字符仍为 tag 开始的位置（> < 之间）
        List<Integer> candidates = new ArrayList<>();

        for (int i = 0; i < htmlContent.length(); i++) {
            char c = htmlContent.charAt(i);

            if (!insideTag && c == '<') {
                insideTag = true;
                if (options.skipScriptStyle && lower != null) {
                    if (lower.startsWith("<script", i) || lower.startsWith("<style", i)) {
                        scriptStyleSkip = true;
                    }
                    if (lower.startsWith("</script", i) || lower.startsWith("</style", i)) {
                        willClearAfterTag = true;
                    }
                }
            } else if (insideTag && c == '>') {
                insideTag = false;
                if (willClearAfterTag) {
                    scriptStyleSkip = false;
                    willClearAfterTag = false;
                }

                if (!scriptStyleSkip && i + 1 < htmlContent.length() && htmlContent.charAt(i + 1) == '<') {
                    candidates.add(i + 1);
                }
            }
        }

        if (candidates.isEmpty()) {
            // fallback：追加到末尾
            String out = htmlContent;
            for (int i = 0; i < spanCount; i++) {
                out += generateHiddenSpan();
            }
            return out;
        }

        // 随机挑选插入点（不放回）
        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < spanCount && !candidates.isEmpty(); i++) {
            int idx = ThreadLocalRandom.current().nextInt(candidates.size());
            chosen.add(candidates.remove(idx));
        }
        chosen.sort((a, b) -> b - a);

        String out = htmlContent;
        for (Integer pos : chosen) {
            out = out.substring(0, pos) + generateHiddenSpan() + out.substring(pos);
        }
        return out;
    }

    /**
     * 一键混淆：不可见字符 + 多处隐藏语句。
     */
    public static String obfuscateHtml(String htmlContent) {
        return obfuscateHtml(htmlContent, null);
    }

    public static String obfuscateHtml(String htmlContent, String activitySource) {
        if (!obfuscationEnabled) {
            return htmlContent;
        }
        ObfuscationOptions options = optionsForSource(activitySource);
        String out = insertInvisibleChars(htmlContent, options);
        out = insertHiddenCharacters(out, options);
        return out;
    }

}
