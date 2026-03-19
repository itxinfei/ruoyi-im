package com.ruoyi.im.filter;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.*;

/**
 * XSS防护请求包装器
 * 用于过滤HTTP请求中的XSS内容
 *
 * @author ruoyi
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 定义安全的HTML标签白名单
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

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }

        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = cleanXss(values[i]);
        }

        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return cleanXss(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return cleanXss(value);
    }

    @Override
    public String getQueryString() {
        String value = super.getQueryString();
        return cleanXss(value);
    }

    @Override
    public String getRequestURI() {
        String value = super.getRequestURI();
        return cleanXss(value);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 获取原始请求体
        String originBody = getBodyString(super.getInputStream());
        // 清理XSS内容
        String cleanBody = cleanXss(originBody);

        // 创建新的输入流
        ByteArrayInputStream bais = new ByteArrayInputStream(cleanBody.getBytes("utf-8"));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 不需要实现
            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

    /**
     * 从输入流中读取请求体内容
     *
     * @param inputStream 输入流
     * @return 请求体内容
     * @throws IOException
     */
    private String getBodyString(ServletInputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
        return sb.toString();
    }

    /**
     * 清理XSS内容
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    private String cleanXss(String input) {
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