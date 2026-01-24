# IM 即时通讯系统需求文档

# 一、项目概述

## 1.1 项目目标

基于 Spring Boot + Vue 3 框架开发即时通讯系统，核心定位为企业级内部即时沟通平台，支持私聊、群聊、文件传输等核心功能，满足企业内部高效协同沟通需求。采用前后端分离架构，保障系统可扩展性、可维护性，适配企业级权限管控与业务集成需求。

## 1.2 统一架构升级

2025-01-24 完成架构升级，移除独立的管理系统，实现前后端统一架构设计：
- **后端统一**：管理功能集成到 im-api，通过路径隔离和角色权限控制
- **前端统一**：根据用户权限动态渲染用户端或管理端界面
- **权限统一**：使用 Spring Security 角色权限体系，简化认证流程

## 1.3 核心约束

- 技术栈约束：前端固定为 Vue 3 + Vite + Element Plus，后端基于 Spring Boot 2.7，通信核心采用 WebSocket/Netty，确保实时性；数据持久化使用 MySQL，缓存与消息队列采用 Redis

- 权限约束：使用 Spring Security 实现用户/角色/菜单权限体系，实现管理员与普通用户的差异化功能权限（如管理员拥有消息审计、用户封禁权限）

- 规范约束：全栈开发严格遵守《阿里巴巴Java开发手册》（最新版）、《阿里巴巴前端开发手册》（Vue专项）、《阿里巴巴数据库设计规范》，确保代码可读性、可维护性、安全性

- 性能约束：支持单频道并发在线用户≥500人，消息推送延迟≤300ms，文件传输支持最大100MB单文件，消息存储保留90天

# 二、统一架构设计

## 2.1 架构概述

移除独立的管理系统，将管理功能集成到 im-api 中，前端复用 im-web，根据用户权限动态展示用户端或管理端界面。

**核心设计原则**：
- **单一后端**：仅需部署 im-api 服务，简化运维
- **统一前端**：根据角色权限动态渲染不同界面
- **路径隔离**：`/api/im/*` 面向普通用户，`/api/admin/*` 面向管理员
- **注解权限**：使用 Spring Security `@PreAuthorize` 细粒度控制访问

## 2.2 项目结构

|模块|说明|技术栈|默认端口|核心职责|
|---|---|---|---|---|
|im-api|核心后端 API 服务|Spring Boot 2.7, WebSocket, Netty, MyBatis Plus|8080|用户认证、消息收发、好友/群组管理、文件传输核心逻辑、管理后台接口|
|im-web|前端 Web 聊天界面|Vue 3, Vite, Element Plus, Vuex, Axios|5173 (Dev)|用户登录、聊天界面展示、消息收发交互、文件/图片上传、个人信息管理、管理后台界面|
|sql/im.sql|数据库初始化脚本|MySQL|-|创建用户、消息、好友、群组等核心表结构，初始化基础数据|

# 三、技术栈详情

## 3.1 后端技术栈

- 核心框架：Spring Boot 2.7

- ORM 框架：MyBatis Plus（简化数据库操作）

- 通信技术：WebSocket（实时消息推送）、Netty（高性能网络通信）

- 缓存中间件：Redis 3.0+（用户状态缓存、消息队列、在线状态存储）

- 数据库：MySQL 5.7/8.0（数据持久化）

- 安全框架：Spring Security（实现权限认证）

- 文件存储：本地文件系统（基础版）

- 日志框架：SLF4J + Logback（日志记录与审计）

- 工具：hutool（日期时间处理、加密解密、文件操作等）、Lombok

## 3.2 前端技术栈

- 核心框架：Vue 3

- 构建工具：Vite（快速构建与热更新）

- UI 组件库：Element Plus

- 状态管理：Vuex（全局状态管理）

- 网络请求：Axios（HTTP 请求）、WebSocket API（实时消息）

- 文件上传：vue-upload-component（大文件分片上传）

- 富文本编辑：wangeditor（支持消息格式化）

- 图标系统：Material Icons Outlined（Google Material Design 风格图标）

## 3.3 中间件与依赖

- Redis：用于缓存用户 Token、在线状态、未读消息计数、消息队列（异步消息推送）

- Nginx：前端静态资源部署、反向代理后端接口、WebSocket 连接转发

# 四、快速开始（可直接执行）

## 4.1 环境准备

### 4.1.1 基础环境要求

- JDK：1.8+（推荐 1.8.0_381），配置环境变量 JAVA_HOME，PATH 添加 %JAVA_HOME%/bin

- MySQL：5.7.44，配置字符集为 utf8mb4（支持表情符号），排序规则 utf8mb4_general_ci，max_connections ≥ 1000

- Redis：3.0+，配置 requirepass 密码，bind 限制访问 IP（生产环境），默认端口 6379

- Node.js：16+（建议 18+），npm 8+ 或 yarn 1.22+

- 操作系统：开发/测试环境支持 Windows 10/11、CentOS 7.9；生产环境推荐 CentOS 7.9（64位），关闭 SELinux，开放 8080、8081、5173、6379 端口

### 4.1.2 环境验证命令

```bash

# 验证 JDK
java -version
# 验证 MySQL
mysql -V
# 验证 Redis
redis-cli -v
# 验证 Node.js
node -v
npm -v
```

## 4.2 数据库初始化

1. 登录 MySQL 客户端（命令行/Navicat 等），执行以下命令创建数据库：
        `CREATE DATABASE IF NOT EXISTS im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;`

2. 导入项目根目录下的 `im.sql` 脚本（两种方式）：
        `# 命令行导入
mysql -u root -p im < im.sql`或通过 Navicat 等工具，右键数据库选择"运行 SQL 文件"，选择 im.sql 执行

3. 验证：导入完成后，检查是否生成 user（用户）、friend（好友）、group（群组）、message（消息）等核心表，确保无报错

## 4.3 后端启动（im-api）

1. 打开 IDE（IDEA/Eclipse），导入 im-api 模块

2. 配置数据源：修改 `im-api/src/main/resources/application.yml` 中以下配置：
        `spring:
    datasource:
    url: jdbc:mysql://localhost:3306/im?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: root  # 你的 MySQL 用户名
    password: 123456  # 你的 MySQL 密码
    redis:
    host: localhost  # 你的 Redis 地址
    port: 6379
    password: 123456  # 你的 Redis 密码
    database: 0`

3. 配置 WebSocket/Netty：检查 application.yml 中通信相关配置（默认无需修改，按需调整端口）：
        `im:
    websocket:
    port: 8080  # WebSocket 绑定端口（与 API 服务端口一致）
    netty:
    port: 8888  # Netty 服务端口（高性能通信备用）`

4. 启动服务：运行 `com.im.ImApplication` 类的 main 方法，控制台输出"Started ImApplication in XXX seconds"即为启动成功

### 4.3.1 管理后台访问

登录成功后，系统会根据用户角色自动跳转：
- **普通用户**：进入聊天界面
- **管理员**：显示侧边栏管理菜单，可访问用户管理、群组管理、数据统计等功能

**默认管理员账号**：admin/123456（需在数据库中设置 role 字段为 ADMIN）

## 4.4 前端启动（im-web）

```bash

# 进入前端项目目录
cd im-web

# 安装依赖（推荐使用 npm，避免依赖冲突）
npm install
# 若安装失败，可尝试清除缓存后重新安装
npm cache clean --force
npm install

# 启动开发服务器（默认端口 5173）
npm run dev
```

启动成功后，控制台会输出本地访问地址（通常为 http://localhost:5173），打开浏览器访问即可进入登录界面

### 4.4.1 前端配置修改（对接后端）

若后端服务地址/端口非默认，修改以下配置：

1. **API 地址**：修改 `im-web/src/utils/request.js` 中的 baseURL
```javascript
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080', // 后端 API 地址
  timeout: 5000
})
```

2. **WebSocket 地址**：修改 `im-web/src/views/chat/index.vue` 中的连接地址
```javascript
const ws = new WebSocket(`ws://localhost:8080/im/websocket/${userId}`)
```

3. **管理界面路由**：`im-web/src/router/index.js` 中已配置权限守卫，无需额外修改

# 五、核心功能特性

## 5.1 用户体系

- 登录认证：使用 JWT Token 认证，支持账号密码登录、验证码登录，登录成功后生成 Token 并缓存至 Redis

- 注册功能：支持手机号注册（需短信验证码，可集成阿里云短信服务），注册后默认分配普通用户角色

- 个人信息管理：支持修改昵称、头像、密码、手机号，查看登录日志，配置消息提醒方式（弹窗/声音）

## 5.2 好友关系

- 用户搜索：支持按用户名、手机号模糊搜索，显示用户在线/离线状态

- 好友申请：发送申请时需填写验证消息，对方可通过/拒绝申请，申请记录保留 7 天

- 好友列表：按字母排序，显示好友在线状态（绿色在线/灰色离线）、最新消息预览、未读消息计数

- 好友管理：支持删除好友、设置好友备注、将好友加入黑名单（屏蔽消息）

## 5.3 即时通讯核心功能

### 5.3.1 聊天模式

- 单聊：一对一实时消息收发，支持消息已读/未读状态显示（对方查看后标记为已读）

- 群聊：支持多人群聊（最大群成员 500 人），显示群成员在线状态，支持@群成员、群公告发布

### 5.3.2 消息类型

- 文本消息：支持表情、特殊符号，支持消息格式化（加粗、斜体等简单样式）

- 多媒体消息：支持图片（JPG/PNG/GIF）、文件（文档、压缩包等）发送，图片可预览，文件支持下载

- 系统消息：好友申请通知、群成员加入/退出通知、系统公告等，统一标记为系统消息

### 5.3.3 消息操作

- 消息撤回：支持 2 分钟内撤回已发送消息，撤回后对方显示"对方撤回一条消息"

- 消息漫游：历史消息存储至数据库，支持分页加载（默认每页 20 条），可查看 90 天内历史消息

- 敏感词过滤：后台可配置敏感词库，发送消息时自动过滤（替换为*），敏感词匹配规则为全匹配/半匹配可配置

## 5.4 群组管理

- 群组创建：普通用户可创建群组（默认最大创建 10 个），需填写群名称、群公告，选择群成员

- 成员管理：群主可邀请/移除成员，转让群主权限，设置群成员禁言/解除禁言

- 群组设置：支持修改群名称、群公告、群头像，解散群组（仅群主可操作）

## 5.5 后台管理功能

- 用户管理：查看所有用户列表，编辑用户信息，封禁/解封用户，重置用户密码

- 群组管理：查看所有群组列表，审核群组创建申请（可选），解散违规群组

- 消息审计：查看用户聊天记录（需开启审计功能），按用户/群组/时间范围筛选，导出审计报告

- 系统配置：配置敏感词库、消息保留时长、文件上传大小限制、WebSocket 连接超时时间等

# 六、落地保障规范

## 6.1 部署流程规范

### 6.1.1 开发环境部署（本地）

参考"快速开始"章节，完成环境准备、数据库初始化、前后端启动即可，核心用于开发调试

### 6.1.2 测试环境部署（服务器）

1. 后端部署：
        `# 打包 im-api 模块（IDEA 中执行 mvn clean package 或命令行）
cd im-api
mvn clean package -Dmaven.test.skip=true

# 上传 jar 包至服务器 /usr/local/im/api 目录
scp target/im-api.jar root@服务器IP:/usr/local/im/api

# 启动服务（后台运行）
cd /usr/local/im/api
nohup java -jar im-api.jar --spring.profiles.active=test > im-api.log 2>&1 `&      

2. 前端部署：
`# 构建生产环境包
cd im-web
npm run build

# 上传 dist 目录至服务器 Nginx 静态资源目录
scp -r dist root@服务器IP:/usr/local/nginx/html/im

# 配置 Nginx（/usr/local/nginx/conf/nginx.conf）
server {
  listen 80;
  server_name 测试服务器IP;

  location / {
    root /usr/local/nginx/html/im;
    index index.html;
  }

  # 反向代理后端 API
  location /api {
    proxy_pass http://localhost:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }

  # WebSocket 代理
  location /im/websocket {
    proxy_pass http://localhost:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
  }
}

# 重启 Nginx
nginx -s reload`

### 6.1.3 生产环境部署（集群）

- 后端部署：采用多实例部署（2 台及以上应用服务器），通过 Nginx 负载均衡分发请求，配置 Redis 实现会话共享（Token 缓存）

- 前端部署：构建 dist 后上传至 OSS，配置 CDN 加速，Nginx 配置 HTTPS（安装 SSL 证书）

- 数据库部署：主从复制架构，主库负责写入，从库负责读取，配置读写分离

- 监控配置：部署 Prometheus + Grafana，监控应用服务器 CPU、内存、接口响应时间、WebSocket 连接数，配置告警规则（如连接数＞1000 告警）

## 6.2 数据迁移与备份规范

### 6.2.1 数据迁移（旧系统迁移场景）

- 用户数据迁移：从旧系统导出用户信息，映射至 im.user 表，设置迁移标识字段 migrate_flag=1，验证用户名/手机号唯一性

- 消息数据迁移：导出旧系统历史消息，按 im.message 表结构整理，确保消息发送时间、发送人、接收人等字段匹配，迁移后校验消息完整性

- 回滚机制：迁移前备份 im 数据库，迁移失败执行回滚脚本（drop database im; 重新导入备份）

### 6.2.2 数据备份

- MySQL 备份：每天凌晨 2 点执行全量备份，使用 crontab 定时任务，备份文件保留 30 天：
        `0 2 * * * mysqldump -u root -p123456 im > /usr/local/backup/im_$(date +%Y%m%d).sql`

- 文件备份：用户上传的文件（图片/文档）每天同步至备份服务器，采用 rsync 工具实现增量同步

## 6.3 运维监控规范

- 日志管理：后端日志存储在 /usr/local/im/logs 目录，按天分割（如 im-api-20240520.log），保留 30 天；前端日志通过 Sentry 收集，监控前端报错

- 日志查看命令：
        `# 实时查看后端日志
tail -f /usr/local/im/logs/im-api.log

# 查看错误日志
grep "ERROR" /usr/local/im/logs/im-api.log`

- 核心指标监控：
        

    - 后端：接口响应时间（≤300ms）、WebSocket 连接数、Redis 缓存命中率（≥90%）

    - 前端：页面加载时间（≤2s）、资源加载成功率（≥99%）、消息发送成功率（≥99.9%）

# 七、设计系统规范

## 7.1 颜色系统

```scss
// 主色调
--dt-brand-color: #1677ff
--dt-brand-hover: #4096ff
--dt-brand-active: #0958d9
--dt-brand-bg: #e6f4ff

// 背景色
--dt-bg-body: #f4f7f9
--dt-bg-sidebar: #1677ff
--dt-bg-card: #ffffff

// 暗色模式
--dt-bg-body-dark: #0f172a
--dt-bg-sidebar-dark: #1e293b
```

## 7.2 尺寸规范

- 侧边栏宽度：80px（w-20）
- 会话面板宽度：280px（w-72）
- 圆角：8px（默认）、12px（xl）、16px（2xl）
- 滚动条宽度：6px

## 7.3 图标使用

项目使用 **Material Icons Outlined** 图标系统：

```html
<!-- 使用方式 -->
<span class="material-icons-outlined">icon_name</span>
```

**常用图标映射**：
- 消息：`chat_bubble`
- 通讯录：`group`
- 工作台：`grid_view`
- 电话：`phone_in_talk`
- 文档：`description`
- 日历：`calendar_today`
- 搜索：`search`
- 添加：`add`
- 设置：`settings`
- 更多：`more_vert`
- 发送：`send`

## 7.4 字体引用

在 `index.html` 中已自动引入：
```html
<link href="https://fonts.googleapis.com/icon?family=Material+Icons+Outlined" rel="stylesheet">
```

# 八、许可证

MIT License
