package com.ruoyi.im.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全配置属性
 * 用于管理安全相关的配置项，包括JWT配置等
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "im.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public static class Jwt {
        /**
         * JWT密钥 - 从环境变量或配置中心获取
         */
        private String secret;

        /**
         * JWT过期时间（毫秒）- 默认15分钟
         */
        private Long expiration = 15 * 60 * 1000L;

        /**
         * 刷新Token过期时间（毫秒）- 默认7天
         */
        private Long refreshExpiration = 7 * 24 * 60 * 60 * 1000L;

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

        public Long getRefreshExpiration() {
            return refreshExpiration;
        }

        public void setRefreshExpiration(Long refreshExpiration) {
            this.refreshExpiration = refreshExpiration;
        }
    }
}