# 10 Docker 部署与 DevOps 设计

> **定位**：定义 Docker Compose 单机编排方案、多阶段镜像构建、CI/CD 流程、监控与告警体系。
> **更新时间**：2026-10-30

---

## 一、整体部署架构

### 1.1 单机部署 (10-500 人团队)

```text
┌─────────────────────────────────────────────────────┐
│              单台物理机 / 云服务器 (16C32G)           │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │                Docker Compose                  │  │
│  │                                               │  │
│  │  ┌─────────────┐  ┌──────────┐  ┌─────────┐  │  │
│  │  │ Nginx (80)  │  │ Nacos    │  │ MySQL   │  │  │
│  │  │ Gateway +   │  │ (8848)   │  │ M1-M9   │  │  │
│  │  │ Static Web  │  │          │  │ (3306)  │  │  │
│  │  └─────────────┘  └──────────┘  └─────────┘  │  │
│  │                                               │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐    │  │
│  │  │ Redis    │  │ RabbitMQ │  │  MinIO   │    │  │
│  │  │ (6379)   │  │ (5672)   │  │ (9000)   │    │  │
│  │  └──────────┘  └──────────┘  └──────────┘    │  │
│  │                                               │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐    │  │
│  │  │ LiveKit  │  │ONLYOFFICE│  │ Prometheus│   │  │
│  │  │ (7880)   │  │ (80->90) │  │ +Grafana │    │  │
│  │  └──────────┘  └──────────┘  └──────────┘    │  │
│  │                                               │  │
│  │  ┌──────────────────────────────────────┐    │  │
│  │  │         12 个 Spring Boot 微服务        │    │  │
│  │  │  im-gateway (8080)                    │    │  │
│  │  │  im-auth (8081)                       │    │  │
│  │  │  im-user (8082)                       │    │  │
│  │  │  im-message (8083)                    │    │  │
│  │  │  im-session (8084)                    │    │  │
│  │  │  im-real-time (8085, x3)              │    │  │
│  │  │  im-group (8086)                      │    │  │
│  │  │  im-call (8087)                       │    │  │
│  │  │  im-collab (8088)                     │    │  │
│  │  │  im-calendar (8090)                   │    │  │
│  │  │  im-doc (8091)                        │    │  │
│  │  │  im-admin (8092)                      │    │  │
│  │  └──────────────────────────────────────┘    │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

---

## 二、Docker Compose 配置

### 2.1 docker-compose.yml (核心服务)

```yaml
version: '3.9'

networks:
  im-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.28.0.0/16

volumes:
  mysql-m1-data:
  mysql-m2-data:
  mysql-m3-data:
  redis-data:
  rabbitmq-data:
  minio-data:
  nacos-logs:
  prometheus-data:
  grafana-data:

services:
  # ========== 基础设施 ==========
  nginx:
    image: nginx:1.27-alpine
    container_name: im-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/certs:/etc/nginx/certs:ro
      - ./nginx/html:/usr/share/nginx/html:ro
    networks:
      - im-network
    depends_on:
      - im-gateway
    restart: unless-stopped

  nacos:
    image: nacos/nacos-server:v2.4.3
    container_name: im-nacos
    environment:
      - MODE=standalone
      - JVM_XMS=512m
      - JVM_XMX=512m
      - NACOS_AUTH_ENABLE=true
      - NACOS_AUTH_TOKEN=SecretKey01234567890123456789012345678901
    ports:
      - "8848:8848"
      - "9848:9848"
    volumes:
      - nacos-logs:/home/nacos/logs
    networks:
      - im-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/v1/console/health/readiness"]
      interval: 15s
      timeout: 5s
      retries: 3
    restart: unless-stopped

  mysql-m1:  # 主库: user + admin + file
    image: mysql:8.4
    container_name: im-mysql-m1
    environment:
      MYSQL_ROOT_PASSWORD: root_pwd_123
      MYSQL_DATABASE: im_user
    ports:
      - "3306:3306"
    volumes:
      - mysql-m1-data:/var/lib/mysql
      - ./mysql/m1-init:/docker-entrypoint-initdb.d:ro
    command: >
      --server-id=1
      --log-bin=mysql-bin
      --binlog-format=ROW
      --gtid-mode=ON
      --enforce-gtid-consistency=ON
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --max-connections=200
    networks:
      - im-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-uroot", "-proot_pwd_123"]
      interval: 10s
      timeout: 5s
      retries: 5

  mysql-m2:  # 主库: im_msg + im_sess + im_grp
    image: mysql:8.4
    container_name: im-mysql-m2
    environment:
      MYSQL_ROOT_PASSWORD: root_pwd_123
      MYSQL_DATABASE: im_msg
    ports:
      - "3307:3306"
    volumes:
      - mysql-m2-data:/var/lib/mysql
      - ./mysql/m2-init:/docker-entrypoint-initdb.d:ro
    command: >
      --server-id=2
      --log-bin=mysql-bin
      --binlog-format=ROW
      --character-set-server=utf8mb4
    networks:
      - im-network

  mysql-m3:  # 主库: im_calendar + im_collab + im_doc
    image: mysql:8.4
    container_name: im-mysql-m3
    environment:
      MYSQL_ROOT_PASSWORD: root_pwd_123
      MYSQL_DATABASE: im_calendar
    ports:
      - "3308:3306"
    volumes:
      - mysql-m3-data:/var/lib/mysql
      - ./mysql/m3-init:/docker-entrypoint-initdb.d:ro
    command: >
      --server-id=3
      --log-bin=mysql-bin
      --character-set-server=utf8mb4
    networks:
      - im-network

  redis:
    image: redis:7.4-alpine
    container_name: im-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
      - ./redis/redis.conf:/usr/local/etc/redis/redis.conf:ro
    command: redis-server /usr/local/etc/redis/redis.conf
      --requirepass redis_pwd_123
      --appendonly yes
      --maxmemory 2gb
      --maxmemory-policy allkeys-lru
    networks:
      - im-network
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "redis_pwd_123", "ping"]
      interval: 10s

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: im-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: rmq_pwd_123
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
      - ./rabbitmq/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro
      - ./rabbitmq/definitions.json:/etc/rabbitmq/definitions.json:ro
    networks:
      - im-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "check_running"]
      interval: 15s

  minio:
    image: minio/minio:RELEASE.2024-10-02T00-00-00Z
    container_name: im-minio
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minio_pwd_123
      MINIO_BROWSER: "on"
    volumes:
      - minio-data:/data
    command: server /data --console-address ":9001"
    networks:
      - im-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 15s

  onlyoffice:
    editor: onlyoffice/documentserver:9.4
    container_name: im-onlyoffice
    ports:
      - "9090:80"
    environment:
      JWT_ENABLED: "true"
      JWT_SECRET: oo_jwt_secret_123
      AMQP_URI: amqp://admin:rmq_pwd_123@rabbitmq:5672/
    volumes:
      - onlyoffice-data:/var/lib/onlyoffice
      - onlyoffice-logs:/var/log/onlyoffice
    networks:
      - im-network

  livekit:
    image: livekit/livekit-server:v1.7
    container_name: im-livekit
    ports:
      - "7880:7880"
      - "7881:7881/udp"
      - "7882:7882/udp"
    volumes:
      - ./livekit/livekit.yaml:/livekit.yaml:ro
    command: --config /livekit.yaml
    networks:
      - im-network

  # ========== 业务服务 ==========
  im-gateway:
    image: ruoyi-im/gateway:1.0.0
    container_name: im-gateway
    expose:
      - "8080"
    environment:
      NAMESPACE: prod
      JVM_OPTS: >
        -Xms512m -Xmx512m
        -XX:+UseG1GC
        -Dcom.sun.management.jmxremote
        -Dcom.sun.management.jmxremote.port=9010
        -Dcom.sun.management.jmxremote.authenticate=false
        -Dcom.sun.management.jmxremote.ssl=false
    depends_on:
      nacos: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  im-message:
    image: ruoyi-im/message-service:1.0.0
    container_name: im-message
    expose:
      - "8081"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms512m -Xmx512m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m2: { condition: service_healthy }
      redis: { condition: service_healthy }
      rabbitmq: { condition: service_healthy }
    networks:
      - im-network

  im-session:
    image: ruoyi-im/session-service:1.0.0
    container_name: im-session
    expose:
      - "8082"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx256m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m2: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  im-group:
    image: ruoyi-im/group-service:1.0.0
    container_name: im-group
    expose:
      - "8083"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx256m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m2: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  im-real-time-1:  # WS 实例 1 (port 8084, 对齐 doc 01/03)
    image: ruoyi-im/real-time-service:1.0.0
    container_name: im-real-time-1
    expose:
      - "8084"
      - "9095"  # JMX
    environment:
      NAMESPACE: prod
      SERVER_NODE_ID: node-1
      JVM_OPTS: "-Xms512m -Xmx512m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  im-real-time-2:  # WS 实例 2
    image: ruoyi-im/real-time-service:1.0.0
    container_name: im-real-time-2
    expose:
      - "8084"
    environment:
      NAMESPACE: prod
      SERVER_NODE_ID: node-2
      JVM_OPTS: "-Xms512m -Xmx512m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  im-real-time-3:  # WS 实例 3
    image: ruoyi-im/real-time-service:1.0.0
    container_name: im-real-time-3
    expose:
      - "8084"
    environment:
      NAMESPACE: prod
      SERVER_NODE_ID: node-3
      JVM_OPTS: "-Xms512m -Xmx512m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  collab-task:
    image: ruoyi-im/collab-task-service:1.0.0
    container_name: collab-task
    expose:
      - "8085"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx256m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m2: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  collab-calendar:
    image: ruoyi-im/collab-calendar-service:1.0.0
    container_name: collab-calendar
    expose:
      - "8086"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx256m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m3: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  collab-doc:
    image: ruoyi-im/collab-doc-service:1.0.0
    container_name: collab-doc
    expose:
      - "8087"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m3: { condition: service_healthy }
      redis: { condition: service_healthy }
      minio: { condition: service_healthy }
      onlyoffice: { condition: service_started }
    networks:
      - im-network

  file-storage:
    image: ruoyi-im/file-storage-service:1.0.0
    container_name: file-storage
    expose:
      - "8088"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m3: { condition: service_healthy }
      redis: { condition: service_healthy }
      minio: { condition: service_healthy }
    networks:
      - im-network

  platform-user:
    image: ruoyi-im/platform-user-service:1.0.0
    container_name: platform-user
    expose:
      - "8089"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx256m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m1: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  admin-system:
    image: ruoyi-im/admin-system-service:1.0.0
    container_name: admin-system
    expose:
      - "8090"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx256m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      mysql-m1: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  platform-ai:
    image: ruoyi-im/platform-ai-service:1.0.0
    container_name: platform-ai
    expose:
      - "8091"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx256m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  platform-search:
    image: ruoyi-im/platform-search-service:1.0.0
    container_name: platform-search
    expose:
      - "8092"
    environment:
      NAMESPACE: prod
      JVM_OPTS: "-Xms256m -Xmx256m -XX:+UseG1GC"
    depends_on:
      nacos: { condition: service_healthy }
      redis: { condition: service_healthy }
    networks:
      - im-network

  # ========== 监控 ==========
  prometheus:
    image: prom/prometheus:v2.55
    container_name: im-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./prometheus/alerts.yml:/etc/prometheus/alerts.yml:ro
      - prometheus-data:/prometheus
    command:
      - --config.file=/etc/prometheus/prometheus.yml
      - --storage.tsdb.retention.time=15d
      - --web.enable-lifecycle
    networks:
      - im-network

  grafana:
    image: grafana/grafana:11.3
    container_name: im-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: grafana_admin_123
      GF_USERS_ALLOW_SIGN_UP: "false"
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/datasources:/etc/grafana/provisioning/datasources:ro
      - ./grafana/dashboards:/etc/grafana/provisioning/dashboards:ro
    depends_on:
      - prometheus
    networks:
      - im-network
```

---

## 三、Nginx 配置

```nginx
# nginx/nginx.conf
upstream im-gateway {
    server im-gateway:8080;
}

upstream im-ws {  # WS 负载均衡 (ip_hash 保证同节点粘性)
    ip_hash;
    server im-real-time-1:8084;
    server im-real-time-2:8084;
    server im-real-time-3:8084;
}

map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
}

server {
    listen 80;
    server_name im.ruoyi.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name im.ruoyi.com;

    ssl_certificate     /etc/nginx/certs/fullchain.pem;
    ssl_certificate_key /etc/nginx/certs/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    # 安全 Header
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
    add_header X-XSS-Protection "1; mode=block";

    # Web 前端 (Vite 构建产物)
    root /usr/share/nginx/html;
    location / {
        try_files $uri $uri/ /index.html;
        expires 7d;
    }

    # API 代理到 Gateway
    location /im/ {
        proxy_pass http://im-gateway;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 5s;
        proxy_read_timeout 30s;
    }

    # WebSocket 代理
    location /im/ws/ {
        rewrite ^/im/ws/?(.*)$ /$1 break;
        proxy_pass http://im-ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 文件下载 (MinIO 代理)
    location /im/file/download/ {
        proxy_pass http://im-minio:9000/;
        proxy_set_header Host $host;
        proxy_buffering off;
    }
}
```

---

## 四、多阶段 Dockerfile

```dockerfile
# 每个 Spring Boot 服务通用 Dockerfile
# ===== Stage 1: Build =====
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /build

# 先复制父 POM 和公共模块 (利用 Docker 缓存层)
COPY pom.xml .
COPY ruoyi-im-common/pom.xml ruoyi-im-common/
COPY ruoyi-im-api/pom.xml ruoyi-im-api/
COPY modules/ modules/
COPY ruoyi-im-common/src ./ruoyi-im-common/src
COPY ruoyi-im-api/src ./ruoyi-im-api/src

# 编译整个多模块项目 (跳过测试加速，测试在 CI 跑)
RUN mvn clean package -DskipTests -pl ruoyi-im-gateway -am \
    -Dspring-boot.repackage.skip=false

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:25-jre-alpine AS runtime

# 安全加固: 非 root 用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 安装必要工具 (curl 用于 healthcheck)
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app

# 从 builder 阶段拷贝 JAR
COPY --from=builder /build/ruoyi-im-gateway/target/*.jar app.jar

# JVM 安全属性
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8"

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## 五、CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_PREFIX: ruoyi-im

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'
          cache: 'maven'

      - name: Run Tests
        run: mvn test -B -Ptest

      - name: Mutation Testing (PIT)
        run: mvn org.pitest:pitest-maven:mutationCoverage -B

      - name: Upload Coverage
        if: github.ref == 'refs/heads/main'
        uses: codecov/codecov-action@v4

  sonar:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4
      - uses: SonarSource/sonarcloud-github-action@v3
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

  build:
    runs-on: ubuntu-latest
    needs: test
    strategy:
      matrix:
        service: [gateway,auth,user,message,session,real-time,group,call,collab,calendar,doc,admin]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '25', distribution: 'temurin', cache: maven }

      - name: Build & Push Image
        run: |
          mvn package -pl ruoyi-im-${{ matrix.service }} -am -DskipTests
          docker build \
            -t $REGISTRY/$IMAGE_PREFIX/${{ matrix.service }}:${{ github.sha }} \
            -t $REGISTRY/$IMAGE_PREFIX/${{ matrix.service }}:latest \
            -f Dockerfile .
          docker push $REGISTRY/$IMAGE_PREFIX/${{ matrix.service }}:${{ github.sha }}

  deploy-staging:
    runs-on: ubuntu-latest
    needs: build
    if: github.ref == 'refs/heads/main'
    environment: staging
    steps:
      - name: Deploy to Staging
        run: |
          ssh staging "cd /opt/ruoyi-im && docker compose pull && docker compose up -d"

  e2e:
    runs-on: ubuntu-latest
    needs: deploy-staging
    steps:
      - name: E2E Test
        run: |
          npx playwright test --config=e2e/playwright.config.ts

  deploy-prod:
    runs-on: ubuntu-latest
    needs: e2e
    if: github.ref == 'refs/heads/main'
    environment: production
    steps:
      - name: Deploy to Prod (滚动更新)
        run: |
          ssh prod "cd /opt/ruoyi-im && docker compose pull && \
            docker compose up -d --no-deps --build im-gateway && \
            docker compose up -d --no-deps im-auth && \
            docker compose up -d --no-deps im-user && \
            docker compose up -d --no-deps im-message && \
            docker compose up -d --no-deps im-real-time-1 && \
            docker compose up -d --no-deps im-real-time-2 && \
            docker compose up -d --no-deps im-real-time-3"
```

---

## 六、监控与告警

### 6.1 Prometheus 指标采集

```yaml
# prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alerts.yml"

scrape_configs:
  - job_name: 'im-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'im-gateway:8080'
        - 'im-message:8081'
        - 'im-session:8082'
        - 'im-group:8083'
        - 'im-real-time-1:8084'
        - 'collab-task:8085'
        - 'collab-calendar:8086'
        - 'collab-doc:8087'
        - 'file-storage:8088'
        - 'platform-user:8089'
        - 'admin-system:8090'
        - 'platform-ai:8091'
        - 'platform-search:8092'
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']

  - job_name: 'mysql'
    static_configs:
      - targets: ['mysqld-exporter:9104']

  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['rabbitmq:15692']
```

### 6.2 告警规则

```yaml
# prometheus/alerts.yml
groups:
  - name: im-critical
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "5xx 错误率 > 5% ({{ $labels.instance }})"

      - alert: HighLatency
        expr: histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "P99 延迟 > 2s ({{ $labels.instance }})"

      - alert: ServiceDown
        expr: up{job="im-services"} == 0
        for: 30s
        labels:
          severity: critical
        annotations:
          summary: "{{ $labels.instance }} 已下线"

      - alert: HighWSConnections
        expr: im_ws_active_connections > 8000
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "WS 连接数接近上限: {{ $value }}"

      - alert: MQConsumerLag
        expr: rabbitmq_queue_messages_ready{queue="im.msg.store"} > 10000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "消息积压: {{ $value }} 条"

      - alert: JVMMemoryPressure
        expr: process_resident_memory_bytes{job="im-services"} / 1024 / 1024 / 1024 > 2.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "{{ $labels.instance }} 内存使用过高: {{ $value }}GB"
```

---

## 七、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-30 | 完成 Docker Compose 编排定义、Nginx 配置、多阶段 Dockerfile、CI/CD Pipeline、Prometheus + Grafra 监控告警 |
