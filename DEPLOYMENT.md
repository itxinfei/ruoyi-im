# RuoYi-IM 部署文档

## 目录
1. [环境要求](#环境要求)
2. [开发环境配置](#开发环境配置)
3. [生产环境部署](#生产环境部署)
4. [常见问题](#常见问题)
5. [维护指南](#维护指南)

---

## 环境要求

### 基础环境
| 组件 | 版本要求 | 说明 |
|-----|---------|------|
| JDK | 1.8+ | 后端运行环境 |
| Maven | 3.6+ | 项目构建工具 |
| Node.js | 16+ | 前端构建工具 |
| MySQL | 5.7+ | 数据库 |
| Redis | 5.0+ | 缓存服务 |
| Nginx | 1.18+ | 反向代理（可选） |

### 服务器配置建议
| 场景 | CPU | 内存 | 硬盘 |
|-----|-----|------|------|
| 开发测试 | 2核 | 4GB | 50GB |
| 小型部署（<100人） | 4核 | 8GB | 100GB |
| 中型部署（100-500人） | 8核 | 16GB | 200GB |
| 大型部署（>500人） | 16核+ | 32GB+ | 500GB+ |

---

## 开发环境配置

### 1. 克隆项目
```bash
git clone <repository-url>
cd im
```

### 2. 数据库配置

#### 2.1 创建数据库
```sql
CREATE DATABASE ry_im CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2.2 导入SQL脚本
按以下顺序执行SQL脚本：
```bash
# 主数据库脚本
mysql -u root -p ry_im < sql/im.sql

# 考勤打卡表
mysql -u root -p ry_im < sql/attendance.sql

# 审批模板数据
mysql -u root -p ry_im < sql/approval_templates.sql

# 敏感词数据
mysql -u root -p ry_im < sql/sensitive_word.sql

# 审计日志表
mysql -u root -p ry_im < sql/audit_log.sql

# 性能优化索引
mysql -u root -p ry_im < sql/performance_optimization.sql
```

### 3. 后端配置

#### 3.1 修改配置文件
编辑 `ruoyi-im-api/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ry_im?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
```

#### 3.2 编译打包
```bash
cd ruoyi-im-api
mvn clean package -DskipTests
```

#### 3.3 启动后端服务
```bash
cd ruoyi-im-api/target
java -jar ruoyi-im-api.jar
```

后端服务默认运行在 `http://localhost:8080`

### 4. 前端配置

#### 4.1 安装依赖
```bash
cd ruoyi-im-web
npm install
```

#### 4.2 开发模式启动
```bash
npm run dev
```

前端服务默认运行在 `http://localhost:8081`

#### 4.3 生产构建
```bash
npm run build
```

构建产物在 `ruoyi-im-web/dist` 目录

---

## 生产环境部署

### 1. 服务器环境准备

#### 1.1 安装JDK 1.8
```bash
# CentOS/RHEL
yum install java-1.8.0-openjdk-devel

# Ubuntu/Debian
apt install openjdk-8-jdk

# 验证安装
java -version
```

#### 1.2 安装MySQL 5.7+
```bash
# CentOS/RHEL
yum install mysql-server
systemctl start mysqld
systemctl enable mysqld

# Ubuntu/Debian
apt install mysql-server
```

#### 1.3 安装Redis
```bash
# CentOS/RHEL
yum install redis
systemctl start redis
systemctl enable redis

# Ubuntu/Debian
apt install redis-server
```

#### 1.4 安装Nginx
```bash
# CentOS/RHEL
yum install nginx
systemctl start nginx
systemctl enable nginx

# Ubuntu/Debian
apt install nginx
```

### 2. 后端部署

#### 2.1 上传部署包
```bash
# 创建部署目录
mkdir -p /opt/ruoyi-im
cd /opt/ruoyi-im

# 上传编译好的jar包
scp ruoyi-im-api/target/ruoyi-im-api.jar user@server:/opt/ruoyi-im/
```

#### 2.2 创建启动脚本
创建 `/opt/ruoyi-im/start.sh`：
```bash
#!/bin/bash
APP_NAME="ruoyi-im-api.jar"
LOG_PATH="/opt/ruoyi-im/logs"

# 创建日志目录
mkdir -p $LOG_PATH

# 停止旧进程
PID=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    kill -9 $PID
fi

# 启动新进程
nohup java -jar \
    -Xms512m -Xmx1024m \
    -Dspring.profiles.active=prod \
    $APP_NAME > $LOG_PATH/application.log 2>&1 &

echo "Application started, logs: $LOG_PATH/application.log"
```

#### 2.3 设置开机自启
创建 `/etc/systemd/system/ruoyi-im.service`：
```ini
[Unit]
Description=RuoYi-IM Application
After=syslog.target network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/ruoyi-im
ExecStart=/usr/bin/java -jar /opt/ruoyi-im/ruoyi-im-api.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

启用服务：
```bash
systemctl daemon-reload
systemctl enable ruoyi-im
systemctl start ruoyi-im
systemctl status ruoyi-im
```

### 3. 前端部署

#### 3.1 配置Nginx
创建 `/etc/nginx/conf.d/ruoyi-im.conf`：
```nginx
server {
    listen 80;
    server_name im.example.com;

    # 前端静态文件
    location / {
        root /opt/ruoyi-im/web/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket代理
    location /ws/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 3600s;
    }

    # 文件上传大小限制
    client_max_body_size 100m;
}
```

重启Nginx：
```bash
nginx -t
nginx -s reload
```

#### 3.2 部署前端文件
```bash
# 创建前端目录
mkdir -p /opt/ruoyi-im/web

# 上传构建产物
scp -r ruoyi-im-web/dist/* user@server:/opt/ruoyi-im/web/dist/
```

### 4. HTTPS配置

#### 4.1 申请SSL证书
使用Let's Encrypt免费证书：
```bash
# 安装certbot
yum install certbot python2-certbot-nginx

# 申请证书
certbot --nginx -d im.example.com
```

#### 4.2 强制HTTPS
修改Nginx配置：
```nginx
server {
    listen 80;
    server_name im.example.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name im.example.com;

    ssl_certificate /etc/letsencrypt/live/im.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/im.example.com/privkey.pem;

    # ... 其他配置
}
```

---

## 常见问题

### 1. 后端启动失败

**问题：** `java.sql.SQLException: Access denied for user`
**解决：** 检查数据库用户名密码配置

**问题：** `java.net.ConnectException: Connection refused`
**解决：** 检查MySQL/Redis服务是否启动，端口是否正确

### 2. WebSocket连接失败

**问题：** WebSocket连接断开
**解决：** 检查Nginx配置中的WebSocket代理设置，确保超时时间足够长

### 3. 文件上传失败

**问题：** 413 Request Entity Too Large
**解决：** 调整Nginx的`client_max_body_size`配置

### 4. 内存溢出

**问题：** java.lang.OutOfMemoryError
**解决：** 调整JVM参数：`-Xms1g -Xmx2g`

### 5. 端口冲突

**问题：** Address already in use
**解决：**
```bash
# 查找占用端口的进程
lsof -i :8080
# 或
netstat -tunlp | grep 8080

# 杀死进程
kill -9 <PID>
```

---

## 维护指南

### 1. 日志管理

#### 日志位置
- 应用日志：`/opt/ruoyi-im/logs/application.log`
- Nginx日志：`/var/log/nginx/`

#### 日志切割
```bash
# 创建logrotate配置
cat > /etc/logrotate.d/ruoyi-im << EOF
/opt/ruoyi-im/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 root root
}
EOF
```

### 2. 数据库备份

#### 定期备份脚本
创建 `/opt/scripts/backup.sh`：
```bash
#!/bin/bash
BACKUP_DIR="/opt/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u root -p${MYSQL_PASSWORD} ry_im | gzip > $BACKUP_DIR/ry_im_$DATE.sql.gz

# 删除30天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

设置定时任务：
```bash
crontab -e
# 每天凌晨2点备份
0 2 * * * /opt/scripts/backup.sh
```

### 3. 监控告警

#### 系统资源监控
```bash
# CPU使用率
top -bn1 | grep "Cpu(s)"

# 内存使用
free -m

# 磁盘使用
df -h

# 端口监听
netstat -tunlp
```

#### 应用健康检查
```bash
# 检查应用是否运行
curl http://localhost:8080/api/im/health

# 检查数据库连接
mysql -u root -p -e "SELECT 1"
```

### 4. 性能优化建议

#### 数据库优化
```sql
-- 定期分析表
ANALYZE TABLE im_message;

-- 优化表
OPTIMIZE TABLE im_message;

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';
```

#### Redis优化
```bash
# 设置最大内存
vim /etc/redis/redis.conf
maxmemory 1gb
maxmemory-policy allkeys-lru
```

---

## 部署检查清单

- [ ] JDK 1.8+ 已安装
- [ ] MySQL 5.7+ 已安装并导入SQL脚本
- [ ] Redis 已安装并启动
- [ ] 后端配置文件已修改（数据库连接、Redis连接）
- [ ] 后端服务已编译并启动
- [ ] 前端已构建并部署
- [ ] Nginx已配置并启动
- [ ] WebSocket连接正常
- [ ] 文件上传功能正常
- [ ] HTTPS证书已配置（生产环境）
- [ ] 数据库备份任务已设置
- [ ] 日志切割已配置
- [ ] 监控告警已配置

---

## 联系方式

- 技术支持：support@example.com
- 项目地址：https://github.com/example/ruoyi-im
