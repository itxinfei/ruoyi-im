package com.ruoyi.im.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * IM系统配置
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Configuration
@ConfigurationProperties(prefix = "im")
@EnableScheduling
public class ImConfig {

    /** JWT配置 */
    private Jwt jwt = new Jwt();

    /** 消息配置 */
    private Message message = new Message();
    
    /** 文件配置 */
    private File file = new File();
    
    /** 敏感词配置 */
    private Sensitive sensitive = new Sensitive();
    
    /** 导出配置 */
    private Export export = new Export();

    /** 安全配置 */
    private Security security = new Security();

    public static class Jwt {
        /** JWT密钥 */
        private String secret;

        /** JWT过期时间(毫秒) */
        private Long expiration;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Long getExpiration() {
            return expiration;
        }

        public void setExpiration(Long expiration) {
            this.expiration = expiration;
        }
    }

    public static class Message {
        /** 撤回时间窗口（秒） */
        private Integer recallWindowSeconds = 120;
        
        /** 消息最大长度 */
        private Integer maxLength = 2000;
        
        /** 消息过期天数 */
        private Integer expireDays = 30;

        /** 加密配置 */
        private Encryption encryption = new Encryption();

        public Integer getRecallWindowSeconds() {
            return recallWindowSeconds;
        }

        public void setRecallWindowSeconds(Integer recallWindowSeconds) {
            this.recallWindowSeconds = recallWindowSeconds;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }

        public Integer getExpireDays() {
            return expireDays;
        }

        public void setExpireDays(Integer expireDays) {
            this.expireDays = expireDays;
        }

        public Encryption getEncryption() {
            return encryption;
        }

        public void setEncryption(Encryption encryption) {
            this.encryption = encryption;
        }
    }

    public static class Encryption {
        /** 是否启用消息加密 */
        private Boolean enabled = true;

        /** 加密密钥 */
        private String key;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public static class File {
        /** 最大文件大小（MB） */
        private Integer maxSizeMb = 20;
        
        /** 允许的文件扩展名 */
        private List<String> allowExt = Arrays.asList("pdf", "docx", "xlsx", "png", "jpg", "txt");
        
        /** 是否启用预览水印 */
        private Boolean enablePreviewWatermark = true;
        
        /** 下载链接过期时间（分钟） */
        private Integer downloadExpireMinutes = 60;
        
        /** 最大下载次数 */
        private Integer maxDownloadCount = 10;

        public Integer getMaxSizeMb() {
            return maxSizeMb;
        }

        public void setMaxSizeMb(Integer maxSizeMb) {
            this.maxSizeMb = maxSizeMb;
        }

        public List<String> getAllowExt() {
            return allowExt;
        }

        public void setAllowExt(List<String> allowExt) {
            this.allowExt = allowExt;
        }

        public Boolean getEnablePreviewWatermark() {
            return enablePreviewWatermark;
        }

        public void setEnablePreviewWatermark(Boolean enablePreviewWatermark) {
            this.enablePreviewWatermark = enablePreviewWatermark;
        }

        public Integer getDownloadExpireMinutes() {
            return downloadExpireMinutes;
        }

        public void setDownloadExpireMinutes(Integer downloadExpireMinutes) {
            this.downloadExpireMinutes = downloadExpireMinutes;
        }

        public Integer getMaxDownloadCount() {
            return maxDownloadCount;
        }

        public void setMaxDownloadCount(Integer maxDownloadCount) {
            this.maxDownloadCount = maxDownloadCount;
        }
    }

    public static class Sensitive {
        /** 是否启用敏感词检测 */
        private Boolean enabled = true;
        
        /** 敏感词拦截策略 */
        private String onBlock = "REJECT"; // REJECT | MASK | WARN
        
        /** 敏感词检测缓存时间（分钟） */
        private Integer cacheMinutes = 30;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getOnBlock() {
            return onBlock;
        }

        public void setOnBlock(String onBlock) {
            this.onBlock = onBlock;
        }

        public Integer getCacheMinutes() {
            return cacheMinutes;
        }

        public void setCacheMinutes(Integer cacheMinutes) {
            this.cacheMinutes = cacheMinutes;
        }
    }

    public static class Export {
        /** 是否需要审批 */
        private Boolean requireApproval = true;
        
        /** 导出链接过期时间（分钟） */
        private Integer linkExpireMinutes = 60;
        
        /** 最大导出记录数 */
        private Integer maxExportRecords = 10000;
        
        /** 是否启用脱敏 */
        private Boolean enableMasking = true;

        public Boolean getRequireApproval() {
            return requireApproval;
        }

        public void setRequireApproval(Boolean requireApproval) {
            this.requireApproval = requireApproval;
        }

        public Integer getLinkExpireMinutes() {
            return linkExpireMinutes;
        }

        public void setLinkExpireMinutes(Integer linkExpireMinutes) {
            this.linkExpireMinutes = linkExpireMinutes;
        }

        public Integer getMaxExportRecords() {
            return maxExportRecords;
        }

        public void setMaxExportRecords(Integer maxExportRecords) {
            this.maxExportRecords = maxExportRecords;
        }

        public Boolean getEnableMasking() {
            return enableMasking;
        }

        public void setEnableMasking(Boolean enableMasking) {
            this.enableMasking = enableMasking;
        }
    }

    public static class Security {
        /** 是否启用认证（测试时可设置为false） */
        private Boolean enableAuth = true;

        /** 允许的跨域源 */
        private List<String> corsAllowOrigins = Arrays.asList();

        /** IP白名单 */
        private List<String> ipWhitelist = Arrays.asList();

        /** 最大并发连接数 */
        private Integer maxConnections = 10000;

        /** WebSocket心跳间隔（秒） */
        private Integer heartbeatInterval = 30;

        public Boolean getEnableAuth() {
            return enableAuth;
        }

        public void setEnableAuth(Boolean enableAuth) {
            this.enableAuth = enableAuth;
        }

        public List<String> getCorsAllowOrigins() {
            return corsAllowOrigins;
        }

        public void setCorsAllowOrigins(List<String> corsAllowOrigins) {
            this.corsAllowOrigins = corsAllowOrigins;
        }

        public List<String> getIpWhitelist() {
            return ipWhitelist;
        }

        public void setIpWhitelist(List<String> ipWhitelist) {
            this.ipWhitelist = ipWhitelist;
        }

        public Integer getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
        }

        public Integer getHeartbeatInterval() {
            return heartbeatInterval;
        }

        public void setHeartbeatInterval(Integer heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
        }
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Sensitive getSensitive() {
        return sensitive;
    }

    public void setSensitive(Sensitive sensitive) {
        this.sensitive = sensitive;
    }

    public Export getExport() {
        return export;
    }

    public void setExport(Export export) {
        this.export = export;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }
}