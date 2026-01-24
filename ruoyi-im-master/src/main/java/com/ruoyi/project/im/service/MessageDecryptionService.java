package com.ruoyi.web.service;

import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 消息解密服务
 * 提供多种编码格式的消息解密功能
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Service
public class MessageDecryptionService {

    /**
     * 解密消息内容
     * 尝试各种编码格式处理乱码
     */
    public String decryptMessage(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String decrypted = tryBase64Decode(content);
        if (decrypted != null) return decrypted;

        decrypted = tryHexDecode(content);
        if (decrypted != null) return decrypted;

        decrypted = tryCharsetDecode(content, "GBK");
        if (decrypted != null) return decrypted;

        decrypted = tryCharsetDecode(content, "GB2312");
        if (decrypted != null) return decrypted;

        decrypted = tryUrlDecode(content);
        if (decrypted != null) return decrypted;

        return getEncryptedMessageDisplay(content);
    }

    private String tryBase64Decode(String content) {
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(content);
            String decodedStr = new String(decoded, StandardCharsets.UTF_8);
            if (isReadableText(decodedStr)) {
                return decodedStr;
            }
        } catch (Exception e) {
            // Base64解码失败，继续其他尝试
        }
        return null;
    }

    private String tryHexDecode(String content) {
        if (!isHexFormat(content)) {
            return null;
        }
        
        try {
            byte[] hex = hexStringToBytes(content);
            String hexStr = new String(hex, StandardCharsets.UTF_8);
            if (isReadableText(hexStr)) {
                return hexStr;
            }
        } catch (Exception e) {
            // 十六进制解码失败，继续其他尝试
        }
        return null;
    }

    private String tryCharsetDecode(String content, String charsetName) {
        try {
            byte[] bytes = content.getBytes(StandardCharsets.ISO_8859_1);
            String decodedStr = new String(bytes, Charset.forName(charsetName));
            if (isReadableText(decodedStr) && containsChinese(decodedStr)) {
                return decodedStr;
            }
        } catch (Exception e) {
            // 字符集解码失败，继续其他尝试
        }
        return null;
    }

    private String tryUrlDecode(String content) {
        try {
            String urlDecoded = java.net.URLDecoder.decode(content, "UTF-8");
            if (isReadableText(urlDecoded) && !urlDecoded.equals(content)) {
                return urlDecoded;
            }
        } catch (Exception e) {
            // URL解码失败，返回原内容
        }
        return null;
    }

    private String getEncryptedMessageDisplay(String content) {
        if (content.length() > 50 && isBase64Format(content)) {
            return "[加密消息] " + content.substring(0, 20) + "...]";
        }
        return content;
    }

    private boolean isHexFormat(String content) {
        return content.matches("^[0-9a-fA-F]+$") && content.length() > 20;
    }

    private boolean isBase64Format(String content) {
        if (content == null || content.length() < 20) {
            return false;
        }
        return content.matches("^[A-Za-z0-9+/=]+$");
    }

    private boolean isReadableText(String text) {
        if (text == null || text.length() < 5) {
            return false;
        }
        return text.matches(".*[\\p{Alnum}\\p{Punct}\\s\\u4e00-\\u9fff]+.*");
    }

    private boolean containsChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches(".*[\\u4e00-\\u9fff]+.*");
    }

    private byte[] hexStringToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = hexCharToInt(s.charAt(i));
            int low = hexCharToInt(s.charAt(i + 1));
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    private int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return 10 + (c - 'a');
        }
        if (c >= 'A' && c <= 'F') {
            return 10 + (c - 'A');
        }
        return -1;
    }
}