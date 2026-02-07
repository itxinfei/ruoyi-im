package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 端到端加密工具类
 *
 * 提供AES-256-GCM加密和RSA/ECDH密钥交换功能
 *
 * @author ruoyi
 */
public class E2EEncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(E2EEncryptionUtil.class);

    /** AES-GCM算法 */
    private static final String AES_GCM = "AES/GCM/NoPadding";

    /** GCM认证标签长度 */
    private static final int GCM_TAG_LENGTH = 128;

    /** GCM IV长度 */
    private static final int GCM_IV_LENGTH = 12;

    /** AES密钥长度 */
    private static final int AES_KEY_LENGTH = 256;

    /** RSA算法 */
    private static final String RSA = "RSA";

    /** ECDH算法 */
    private static final String ECDH = "EC";

    /** RSA密钥长度 */
    private static final int RSA_KEY_SIZE = 2048;

    /**
     * 生成RSA密钥对
     *
     * @return 密钥对
     */
    public static KeyPair generateRSAKeyPair() {
        try {
            java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(RSA_KEY_SIZE, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            log.debug("生成RSA密钥对成功");
            return keyPair;
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }

    /**
     * 生成ECDH密钥对
     *
     * @return 密钥对
     */
    public static KeyPair generateECDHKeyPair() {
        try {
            java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance(ECDH);
            keyPairGenerator.initialize(256, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            log.debug("生成ECDH密钥对成功");
            return keyPair;
        } catch (Exception e) {
            log.error("生成ECDH密钥对失败", e);
            throw new RuntimeException("生成ECDH密钥对失败", e);
        }
    }

    /**
     * 从PEM格式解析公钥
     *
     * @param publicKeyPEM PEM格式公钥
     * @return 公钥对象
     */
    public static PublicKey parsePublicKey(String publicKeyPEM) {
        try {
            // 移除PEM头尾
            String publicKeyContent = publicKeyPEM
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            // Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);

            // 创建X509编码密钥规范
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

            // 根据算法创建密钥工厂
            String algorithm = publicKeyPEM.contains("EC") ? ECDH : RSA;
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            log.error("解析公钥失败", e);
            throw new RuntimeException("解析公钥失败", e);
        }
    }

    /**
     * 从PEM格式解析私钥
     *
     * @param privateKeyPEM PEM格式私钥
     * @return 私钥对象
     */
    public static PrivateKey parsePrivateKey(String privateKeyPEM) {
        try {
            // 移除PEM头尾
            String privateKeyContent = privateKeyPEM
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

            // 创建PKCS8编码密钥规范
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

            // 根据算法创建密钥工厂
            String algorithm = privateKeyPEM.contains("EC") ? ECDH : RSA;
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            log.error("解析私钥失败", e);
            throw new RuntimeException("解析私钥失败", e);
        }
    }

    /**
     * 将公钥转换为PEM格式
     *
     * @param publicKey 公钥对象
     * @return PEM格式字符串
     */
    public static String publicKeyToPEM(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);

        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(i + 64, base64.length())).append("\n");
        }
        pem.append("-----END PUBLIC KEY-----");
        return pem.toString();
    }

    /**
     * 将私钥转换为PEM格式
     *
     * @param privateKey 私钥对象
     * @return PEM格式字符串
     */
    public static String privateKeyToPEM(PrivateKey privateKey) {
        byte[] encoded = privateKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);

        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PRIVATE KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(i + 64, base64.length())).append("\n");
        }
        pem.append("-----END PRIVATE KEY-----");
        return pem.toString();
    }

    /**
     * 使用AES-GCM加密消息
     *
     * @param content    明文内容
     * @param sessionKey 会话密钥
     * @return Base64编码的密文（包含IV）
     */
    public static String encryptMessage(String content, SecretKey sessionKey) {
        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // 创建密码器
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey, spec);

            // 加密
            byte[] ciphertext = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

            // 将IV和密文组合
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // 返回Base64编码
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * 使用AES-GCM解密消息
     *
     * @param encryptedData Base64编码的密文
     * @param sessionKey    会话密钥
     * @return 明文内容
     */
    public static String decryptMessage(String encryptedData, SecretKey sessionKey) {
        try {
            // Base64解码
            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            // 分离IV和密文
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            // 创建密码器
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, spec);

            // 解密
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("AES解密失败", e);
        }
    }

    /**
     * 使用RSA加密会话密钥
     *
     * @param sessionKey 会话密钥
     * @param publicKey  接收方公钥
     * @return Base64编码的加密密钥
     */
    public static String encryptSessionKey(SecretKey sessionKey, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedKey = cipher.doFinal(sessionKey.getEncoded());
            return Base64.getEncoder().encodeToString(encryptedKey);
        } catch (Exception e) {
            log.error("RSA加密会话密钥失败", e);
            throw new RuntimeException("RSA加密会话密钥失败", e);
        }
    }

    /**
     * 使用RSA解密会话密钥
     *
     * @param encryptedKey Base64编码的加密密钥
     * @param privateKey   接收方私钥
     * @return 会话密钥
     */
    public static SecretKey decryptSessionKey(String encryptedKey, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedKey = cipher.doFinal(Base64.getDecoder().decode(encryptedKey));
            return new SecretKeySpec(decryptedKey, "AES");
        } catch (Exception e) {
            log.error("RSA解密会话密钥失败", e);
            throw new RuntimeException("RSA解密会话密钥失败", e);
        }
    }

    /**
     * 使用ECDH生成共享密钥
     *
     * @param privateKey    本地私钥
     * @param peerPublicKey 对方公钥
     * @return 共享密钥
     */
    public static SecretKey deriveSharedKey(PrivateKey privateKey, PublicKey peerPublicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(peerPublicKey, true);

            // 生成256位AES密钥
            byte[] sharedSecret = keyAgreement.generateSecret();
            if (sharedSecret == null || sharedSecret.length == 0) {
                throw new RuntimeException("生成共享密钥失败");
            }
            byte[] aesKey = new byte[32]; // 256 bits
            System.arraycopy(sharedSecret, 0, aesKey, 0, Math.min(sharedSecret.length, 32));

            return new SecretKeySpec(aesKey, "AES");
        } catch (Exception e) {
            log.error("ECDH生成共享密钥失败", e);
            throw new RuntimeException("ECDH生成共享密钥失败", e);
        }
    }

    /**
     * 生成随机会话密钥
     *
     * @return AES会话密钥
     */
    public static SecretKey generateSessionKey() {
        try {
            javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_LENGTH, new SecureRandom());
            return keyGenerator.generateKey();
        } catch (Exception e) {
            log.error("生成会话密钥失败", e);
            throw new RuntimeException("生成会话密钥失败", e);
        }
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试RSA加密解密
        KeyPair keyPair = generateRSAKeyPair();
        SecretKey sessionKey = generateSessionKey();

        String encryptedKey = encryptSessionKey(sessionKey, keyPair.getPublic());
        SecretKey decryptedKey = decryptSessionKey(encryptedKey, keyPair.getPrivate());

        // 测试AES加密解密
        String plaintext = "Hello, 这是一个测试消息！";
        String encrypted = encryptMessage(plaintext, decryptedKey);
        String decrypted = decryptMessage(encrypted, decryptedKey);

        System.out.println("原文: " + plaintext);
        System.out.println("解密: " + decrypted);
        System.out.println("测试结果: " + plaintext.equals(decrypted));
    }
}
