package com.ruoyi.im.util;

import com.ruoyi.im.config.ImConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Message Encryption Utility
 * Provides AES-256-GCM encryption for sensitive message content
 */
@Component
public class MessageEncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(MessageEncryptionUtil.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    @Autowired
    private ImConfig imConfig;

    private boolean encryptionEnabled;
    private String encryptionKey;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (imConfig != null && imConfig.getMessage() != null && imConfig.getMessage().getEncryption() != null) {
            this.encryptionEnabled = imConfig.getMessage().getEncryption().getEnabled();
            this.encryptionKey = imConfig.getMessage().getEncryption().getKey();
            log.info("消息加密配置已加载 - 启用: {}, 密钥: {}", encryptionEnabled, encryptionKey != null ? "已配置" : "未配置");
        } else {
            log.warn("消息加密配置未正确加载，使用默认值");
            this.encryptionEnabled = false;
            this.encryptionKey = null;
        }
    }

    public String encryptMessage(String plaintext) {
        if (!encryptionEnabled || plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            SecretKey key = getSecretKey();
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Message encryption failed", e);
            return plaintext;
        }
    }

    public String decryptMessage(String encrypted) {
        if (!encryptionEnabled || encrypted == null || encrypted.isEmpty()) {
            return encrypted;
        }

        // 如果不是有效的Base64加密格式（简短明文），直接返回
        if (!isEncryptedFormat(encrypted)) {
            return encrypted;
        }

        try {
            SecretKey key = getSecretKey();
            if (key == null) {
                log.warn("Encryption key not available, treating content as plaintext");
                return encrypted;
            }

            byte[] decoded = Base64.getDecoder().decode(encrypted);

            // 检查解码后的长度是否有效（至少包含IV + 最小密文长度）
            if (decoded.length < GCM_IV_LENGTH + 16) {
                log.warn("Invalid encrypted message length: {}, treating as plaintext", decoded.length);
                return encrypted;
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            String result = new String(plaintext, StandardCharsets.UTF_8);
            log.debug("Successfully decrypted message, length: {}", result.length());
            return result;
        } catch (IllegalArgumentException e) {
            // Base64解码失败，说明是明文内容
            log.debug("Content is not valid Base64, treating as plaintext: {}", encrypted.substring(0, Math.min(20, encrypted.length())));
            return encrypted;
        } catch (javax.crypto.AEADBadTagException e) {
            // 密钥不匹配或数据被篡改
            log.warn("Message decryption failed: incorrect key or corrupted data, returning placeholder");
            return "[无法解析的消息]";
        } catch (Exception e) {
            log.error("Message decryption failed, returning placeholder", e);
            return "[消息解析失败]";
        }
    }

    /**
     * 检查字符串是否为加密格式
     * Base64编码的加密内容通常较长且只包含Base64字符
     */
    private boolean isEncryptedFormat(String content) {
        if (content == null || content.length() < 20) {
            // 加密后的内容至少20字符（IV 12字节 + 最小密文）
            return false;
        }
        // 简单检查是否为有效的Base64格式
        try {
            // 尝试Base64解码，如果失败则不是加密格式
            java.util.Base64.getDecoder().decode(content);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private SecretKey getSecretKey() {
        if (secretKey == null && encryptionKey != null && !encryptionKey.isEmpty()) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                byte[] keyBytes = sha.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
                secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            } catch (Exception e) {
                log.error("Failed to generate secret key", e);
            }
        }
        return secretKey;
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void setEncryptionEnabled(boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
        this.secretKey = null;
    }
}
