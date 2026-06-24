package com.ruoyi.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI大模型配置
 * 支持多种大语言模型：OpenAI、通义千问、文心一言、腾讯混元、讯飞星火
 *
 * @author ruoyi
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /** 默认模型 */
    private String defaultModel = "qwen";

    /** 默认调用超时（秒） */
    private int defaultTimeout = 30;

    /** OpenAI配置 */
    private OpenAiConfig openai = new OpenAiConfig();

    /** 通义千问配置 */
    private QwenConfig qwen = new QwenConfig();

    /** 文心一言配置 */
    private WenxinConfig wenxin = new WenxinConfig();

    /** 腾讯混元配置 */
    private HunyuanConfig hunyuan = new HunyuanConfig();

    /** 讯飞星火配置 */
    private SparkConfig spark = new SparkConfig();

    @Data
    public static class OpenAiConfig {
        /** API密钥 */
        private String apiKey;
        /** API地址 */
        private String apiUrl = "https://api.openai.com/v1";
        /** 模型名称 */
        private String model = "gpt-3.5-turbo";
        /** 请求超时（秒） */
        private int timeout = 30;
    }

    @Data
    public static class QwenConfig {
        /** API密钥（DashScope） */
        private String apiKey;
        /** API地址 */
        private String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        /** 模型名称 */
        private String model = "qwen-turbo";
        /** 请求超时（秒） */
        private int timeout = 30;
    }

    @Data
    public static class WenxinConfig {
        /** API Key */
        private String apiKey;
        /** Secret Key */
        private String secretKey;
        /** 模型名称 */
        private String model = "ERNIE-Bot-turbo";
        /** 请求超时（秒） */
        private int timeout = 30;
        /** Access Token（内部缓存） */
        private transient String accessToken;
        /** Token过期时间 */
        private transient Long tokenExpireTime;
    }

    @Data
    public static class HunyuanConfig {
        /** Secret Id */
        private String secretId;
        /** Secret Key */
        private String secretKey;
        /** 地域 */
        private String region = "ap-guangzhou";
        /** 请求超时（秒） */
        private int timeout = 30;
    }

    @Data
    public static class SparkConfig {
        /** 应用ID */
        private String appId;
        /** API Secret */
        private String apiSecret;
        /** API Key */
        private String apiKey;
        /** 版本 */
        private String version = "v3.5";
        /** 请求超时（秒） */
        private int timeout = 30;
    }

    /**
     * 检查指定模型是否已配置
     */
    public boolean isModelConfigured(String model) {
        if (model == null) {
            return false;
        }
        switch (model.toLowerCase()) {
            case "gpt-3.5-turbo":
            case "gpt-4":
                return openai.getApiKey() != null && !openai.getApiKey().isEmpty();
            case "qwen":
                return qwen.getApiKey() != null && !qwen.getApiKey().isEmpty();
            case "wenxin":
                return wenxin.getApiKey() != null && !wenxin.getApiKey().isEmpty();
            case "hunyuan":
                return hunyuan.getSecretId() != null && !hunyuan.getSecretId().isEmpty();
            case "spark":
                return spark.getAppId() != null && !spark.getAppId().isEmpty();
            default:
                return false;
        }
    }
}
