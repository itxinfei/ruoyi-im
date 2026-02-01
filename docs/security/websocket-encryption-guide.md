# WebSocket 加密传输配置指南

## 当前安全状态

### 已实现的加密

| 层级 | 加密方式 | 状态 |
|------|----------|------|
| **存储层** | AES-256-GCM | ✅ 已实现 |
| **传输层** | TLS/SSL (WSS) | ⚠️ 需配置 |

### 存储加密详情

消息在存储到数据库前已经使用 AES-256-GCM 加密：

```java
// ImMessageServiceImpl.java:212
message.setContent(encryptionUtil.encryptMessage(plainContent));
```

- 加密算法：AES-256-GCM
- 密钥配置：`application.yml` 中的 `im.message.encryption.key`
- IV 长度：12 字节
- Tag 长度：128 位

## 传输加密方案

### 方案 A：WSS（推荐）

通过配置 HTTPS/WSS 实现 TLS 加密传输。

#### 1. 生成自签名证书（内网环境）

```bash
# 生成私钥和证书
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# 或生成 PKCS12 格式
openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12 -name imserver
```

#### 2. Spring Boot 配置 HTTPS

修改 `application.yml`：

```yaml
server:
  port: 8443  # HTTPS 端口
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: yourpassword
    key-store-type: PKCS12
    key-alias: imserver
    trust-store: classpath:keystore.p12
    trust-store-password: yourpassword

  # 同时保留 HTTP 端口用于重定向
  port: 8080
```

#### 3. Nginx 反向代理配置（推荐用于生产）

```nginx
# /etc/nginx/conf.d/im.conf
upstream im_backend {
    server 127.0.0.1:8080;
}

# HTTP 重定向到 HTTPS
server {
    listen 80;
    server_name im.internal.company.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS/WSS
server {
    listen 443 ssl http2;
    server_name im.internal.company.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # WebSocket 代理
    location /ws/im {
        proxy_pass http://im_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 超时配置
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }

    # API 代理
    location /api/ {
        proxy_pass http://im_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 4. 前端连接地址修改

```javascript
// 开发环境
const wsUrl = 'ws://localhost:8080/ws/im'

// 生产环境（WSS）
const wsUrl = 'wss://im.internal.company.com/ws/im'
```

### 方案 B：消息负载加密

如果不想配置 HTTPS，可以在应用层加密 WebSocket 消息负载。

**注意**：此方案需要修改前端代码以支持解密。

#### 后端实现

保持 WebSocket 消息中的 content 为加密状态：

```java
// ImWebSocketBroadcastServiceImpl.java
// 修改 createMessageData 方法，不解密内容
data.put("content", message.getContent());  // 保持加密状态
data.put("encrypted", true);  // 标记为加密
```

#### 前端实现

需要在前端集成 AES-GCM 解密（使用 crypto-js 或 Web Crypto API）。

## 验证加密效果

### 1. 检查存储加密

```sql
-- 查看数据库中的消息内容（应该是加密的）
SELECT id, content FROM im_message LIMIT 5;
```

### 2. 检查传输加密

使用浏览器开发者工具：
- **WS (未加密)**：可以看到明文消息内容
- **WSS (已加密)**：消息内容被 TLS 加密，无法直接查看

### 3. Wireshark 抓包验证

```
# 过滤 WebSocket
# ws 或 tcp.port == 8080

# WSS 加密后只能看到 TLS 数据包
# tcp.port == 8443
```

## 安全建议

1. **生产环境必须使用 WSS**
   - TLS 1.2 或更高版本
   - 强密码套件
   - 证书有效期管理

2. **内网环境**
   - 可使用自签名证书
   - 客户端需要信任该证书

3. **定期更新**
   - 证书过期前更新
   - 密钥轮换策略

4. **监控日志**
   - 记录加密/解密失败
   - 异常连接尝试
