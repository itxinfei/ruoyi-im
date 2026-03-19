package com.ruoyi.im.filter;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * XSS防护过滤器
 * 用于过滤请求参数中的恶意脚本，防止XSS攻击
 *
 * @author ruoyi
 */
@Component
public class XssFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(XssFilter.class);

    // 定义安全的HTML标签白名单
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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("XSS过滤器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 设置CSP头部，防止XSS攻击
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none';");
        
        // 设置X-XSS-Protection头部
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        // 设置X-Content-Type-Options头部
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 包装请求以过滤XSS
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(httpRequest);

        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {
        logger.info("XSS过滤器销毁");
    }

    /**
     * 清理字符串中的XSS内容
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String cleanXss(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 使用Jsoup清理HTML内容
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