package com.ruoyi.im.filter;

import com.ruoyi.im.util.XssUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.*;

/**
 * XSS防护请求包装器
 * 用于过滤HTTP请求中的XSS内容
 * 注意：对 application/json 请求跳过 body 清洗，Jackson 自身处理转义
 *
 * @author ruoyi
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

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
            encodedValues[i] = XssUtil.cleanXss(values[i]);
        }

        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return XssUtil.cleanXss(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        // 跳过CORS相关请求头的XSS过滤，避免破坏Origin等header
        if (isCorsHeader(name)) {
            return value;
        }
        return XssUtil.cleanXss(value);
    }

    /**
     * 判断是否为CORS相关header
     */
    private boolean isCorsHeader(String name) {
        if (name == null) { return false; }
        String lowerName = name.toLowerCase();
        return lowerName.equals("origin")
                || lowerName.equals("access-control-request-method")
                || lowerName.equals("access-control-request-headers")
                || lowerName.equals("sec-fetch-mode")
                || lowerName.equals("sec-fetch-site")
                || lowerName.equals("sec-fetch-page")
                || lowerName.equals("sec-ch-ua")
                || lowerName.equals("sec-ch-ua-mobile")
                || lowerName.equals("sec-ch-ua-platform");
    }

    @Override
    public String getQueryString() {
        String value = super.getQueryString();
        return XssUtil.cleanXss(value);
    }

    @Override
    public String getRequestURI() {
        String value = super.getRequestURI();
        return XssUtil.cleanXss(value);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 获取 Content-Type
        String contentType = getContentType();
        
        // 对 application/json 请求跳过 body 清洗，Jackson 自身处理转义
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            return super.getInputStream();
        }
        
        // 仅对 application/x-www-form-urlencoded 和 multipart/form-data 请求做 XSS 清洗
        if (contentType != null && 
            (contentType.toLowerCase().contains("application/x-www-form-urlencoded") ||
             contentType.toLowerCase().contains("multipart/form-data"))) {
            
            // 获取原始请求体
            String originBody = getBodyString(super.getInputStream());
            // 清理XSS内容
            String cleanBody = XssUtil.cleanXss(originBody);

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
        
        // 其他 Content-Type 默认不清洗 body
        return super.getInputStream();
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
}