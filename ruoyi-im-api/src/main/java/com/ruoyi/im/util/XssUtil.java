package com.ruoyi.im.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * XSS 清理工具类
 * 提供统一的 XSS 清理方法，避免重复代码
 *
 * @author ruoyi
 */
public final class XssUtil {

    private XssUtil() {
        // 防止实例化
    }

    /**
     * 定义安全的 HTML 标签白名单
     */
    private static final Safelist SAFE_LIST = Safelist.relaxed()
            .addTags("span", "div", "p", "br", "hr", "pre", "blockquote", "ol", "ul", "li", "dl", "dt", "dd")
            .addAttributes("a", "href", "title", "target")
            .addAttributes("img", "src", "alt", "title", "width", "height")
            .addAttributes("table", "border", "cellpadding", "cellspacing")
            .addAttributes("td", "colspan", "rowspan")
            .addAttributes("th", "colspan", "rowspan")
            .addProtocols("a", "href", "http", "https", "#")
            .addProtocols("img", "src", "http", "https")
            .preserveRelativeLinks(true);

    /**
     * 清理字符串中的 XSS 内容
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String cleanXss(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 使用 Jsoup 清理 HTML 内容
        String cleaned = Jsoup.clean(input, SAFE_LIST);

        // 进一步清理可能的危险字符
        cleaned = cleaned.replaceAll("<script>(.*?)</script>", "");
        cleaned = cleaned.replaceAll("javascript:", "");
        cleaned = cleaned.replaceAll("onload=", "&#111;nload=");
        cleaned = cleaned.replaceAll("onerror=", "&#111;nerror=");
        cleaned = cleaned.replaceAll("onmouseover=", "&#111;nmouseover=");
        cleaned = cleaned.replaceAll("onclick=", "&#111;nclick=");
        cleaned = cleaned.replaceAll("onfocus=", "&#111;nfocus=");
        cleaned = cleaned.replaceAll("onblur=", "&#111;nblur=");

        return cleaned;
    }
}