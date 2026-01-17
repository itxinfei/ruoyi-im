# RuoYi-IM Dashboard 数据可视化部署文档

## 📋 目录

1. [系统概述](#系统概述)
2. [环境要求](#环境要求)
3. [安装步骤](#安装步骤)
4. [配置说明](#配置说明)
5. [启动与停止](#启动与停止)
6. [功能说明](#功能说明)
7. [常见问题](#常见问题)
8. [性能优化](#性能优化)

---

## 系统概述

### 功能特性

**RuoYi-IM Dashboard** 是一个企业级即时通讯管理后台的数据可视化系统，提供以下功能：

- ✅ **基础统计**：用户、消息、群组、会话总数统计
- ✅ **趋势分析**：用户注册、消息发送、群组创建趋势（折线图）
- ✅ **占比分析**：消息类型、文件类型、用户活跃度分布（饼图）
- ✅ **对比分析**：本周vs上周、本月vs上月、今天vs昨天（柱状图）
- ✅ **排行榜**：最活跃用户、最活跃群组、消息发送排行
- ✅ **实时监控**：在线用户数、实时消息流量（仪表盘+折线图）

### 技术栈

- **后端**：Spring Boot 2.7 + MyBatis Plus + MySQL 5.7+
- **前端**：Thymeleaf + jQuery 3.6.3 + ECharts 4.2.1 + Bootstrap 3.3.7
- **缓存**：Redis 3.0+（可选，当前版本已禁用）
- **数据库**：MySQL 5.7.44

---

## 环境要求

### 必需环境

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8+ | 必须是JDK 1.8 |
| MySQL | 5.7+ | 推荐5.7.44 |
| Maven | 3.6+ | 用于构建项目 |

### 可选环境

| 软件 | 版本 | 说明 |
|------|------|------|
| Redis | 3.0+ | 用于缓存（当前版本已禁用） |
| Nginx | 1.18+ | 用于反向代理 |

### 硬件要求

| 配置 | 最低要求 | 推荐配置 |
|------|----------|----------|
| CPU | 2核 | 4核+ |
| 内存 | 4GB | 8GB+ |
| 磁盘 | 20GB | 50GB+ |

---

## 安装步骤

### 1. 数据库初始化

#### 1.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS ruoyi_im_admin
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;
```

#### 1.2 执行SQL脚本

```bash
# 导入数据库结构
mysql -u root -p ruoyi_im_admin < D:\MyCode\im\sql\ruoyi_im_admin.sql
```

#### 1.3 验证表结构

检查是否存在以下核心表：

```sql
-- 检查用户表
SHOW TABLES LIKE 'im_user';

-- 检查消息表
SHOW TABLES LIKE 'im_message';

-- 检查群组表
SHOW TABLES LIKE 'im_group';

-- 检查会话表
SHOW TABLES LIKE 'im_conversation';
```

### 2. 项目构建

#### 2.1 克隆项目（如果需要）

```bash
cd D:\MyCode\im
```

#### 2.2 Maven构建

```bash
cd D:\MyCode\im\ruoyi-im-admin
mvn clean package -DskipTests
```

**预期输出**：
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
```

#### 2.3 验证编译产物

检查生成的JAR文件：

```bash
ls -lh ruoyi-admin/target/ruoyi-admin.jar
```

### 3. 配置文件修改

#### 3.1 修改数据库配置

编辑 `ruoyi-admin/src/main/resources/application-druid.yml`：

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ruoyi_im_admin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root  # 修改为你的数据库用户名
    password: your_password  # 修改为你的数据库密码
```

#### 3.2 修改端口配置（可选）

编辑 `ruoyi-admin/src/main/resources/application.yml`：

```yaml
server:
  port: 8081  # 修改为其他端口（可选）
```

---

## 配置说明

### 数据库配置

**文件位置**：`ruoyi-admin/src/main/resources/application-druid.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ruoyi_im_admin?useUnicode=true&characterEncoding=utf8
    username: root
    password: 123456
    # 连接池配置
    initialSize: 5
    minIdle: 5
    maxActive: 20
    maxWait: 60000
```

### 日志配置

**文件位置**：`ruoyi-admin/src/main/resources/application.yml`

```yaml
logging:
  level:
    com.ruoyi: debug  # 开发环境使用debug，生产环境使用info
    org.springframework: warn
```

### MyBatis配置

**文件位置**：`ruoyi-admin/src/main/resources/mybatis/mybatis-config.xml`

```xml
<settings>
  <!-- 开启驼峰命名转换 -->
  <setting name="mapUnderscoreToCamelCase" value="true"/>
  <!-- 开启二级缓存 -->
  <setting name="cacheEnabled" value="true"/>
</settings>
```

---

## 启动与停止

### 方式一：IDEA中启动（开发环境）

#### 1.1 配置启动类

找到主类：
```
ruoyi-admin/src/main/java/com/ruoyi/RuoYiApplication.java
```

#### 1.2 点击运行

右键点击 `RuoYiApplication.java` → Run 'RuoYiApplication'

#### 1.3 验证启动成功

看到以下日志表示启动成功：

```
Started RuoYiApplication in XX seconds
Tomcat started on port(s): 8081 (http)
```

#### 1.4 访问系统

打开浏览器访问：
```
http://localhost:8081
```

默认账号：
- 用户名：`admin`
- 密码：`admin123`

### 方式二：JAR包启动（生产环境）

#### 2.1 打包项目

```bash
cd D:\MyCode\im\ruoyi-im-admin
mvn clean package -DskipTests
```

#### 2.2 启动服务

```bash
cd ruoyi-admin/target
java -jar ruoyi-admin.jar
```

#### 2.3 后台启动（Linux）

```bash
nohup java -jar ruoyi-admin.jar > log.txt 2>&1 &
```

#### 2.4 停止服务

查找进程ID：

```bash
ps -ef | grep ruoyi-admin.jar
```

杀死进程：

```bash
kill -9 <PID>
```

---

## 功能说明

### 1. Dashboard首页

**访问路径**：`http://localhost:8081/main.html`

**页面布局**：

```
┌─────────────────────────────────────────────┐
│              系统标题栏                      │
├─────────────────────────────────────────────┤
│  ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐   │
│  │用户数 │ │在线数 │ │消息数 │ │群组数 │   │  ← 统计卡片
│  └───────┘ └───────┘ └───────┘ └───────┘   │
├─────────────────────────────────────────────┤
│  ┌───────────────┐ ┌───────────────┐       │
│  │  用户趋势图   │ │  消息趋势图   │       │  ← 趋势图表
│  └───────────────┘ └───────────────┘       │
├─────────────────────────────────────────────┤
│  ┌─────┐ ┌─────┐ ┌─────┐                  │
│  │分布图│ │分布图│ │分布图│                  │  ← 饼图
│  └─────┘ └─────┘ └─────┘                  │
├─────────────────────────────────────────────┤
│  ┌───────────────┐ ┌───────────────┐       │
│  │  对比柱状图   │ │  周对比图     │       │  ← 柱状图
│  └───────────────┘ └───────────────┘       │
├─────────────────────────────────────────────┤
│  ┌───────────────┐ ┌───────────────┐       │
│  │ 用户排行榜   │ │ 群组排行榜   │       │  ← 横向柱状图
│  └───────────────┘ └───────────────┘       │
├─────────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐   │
│  │          实时监控面板               │   │  ← 仪表盘+折线图
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

### 2. API接口

**基础路径**：`/im/dashboard`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/basic` | GET | 获取基础统计数据 |
| `/trend` | GET | 获取趋势数据 |
| `/distribution` | GET | 获取分布数据 |
| `/comparison` | GET | 获取对比数据 |
| `/ranking` | GET | 获取排行榜数据 |
| `/realtime` | GET | 获取实时监控数据 |
| `/refresh` | POST | 刷新缓存 |

详见：[API测试文档](./dashboard-api-test.md)

### 3. 图表交互

#### 3.1 时间范围切换

趋势图表支持切换时间范围（7天/30天/90天）：

```
[7天] [30天] [90天]
```

#### 3.2 刷新数据

点击"刷新所有图表"按钮，强制刷新所有图表数据。

#### 3.3 数据导出

图表支持导出为PNG图片或CSV文件：

```javascript
// 导出为PNG
$('#userTrendChart').dashboardChart('exportImage', '用户趋势.png');

// 导出为CSV
$('#userTrendChart').dashboardChart('exportCSV', '用户趋势.csv');
```

---

## 常见问题

### 1. 启动失败

#### 问题：数据库连接失败

**错误信息**：
```
Communications link failure
```

**解决方案**：
1. 检查MySQL是否启动
2. 检查数据库配置是否正确
3. 检查防火墙是否开放3306端口

```bash
# 检查MySQL状态
systemctl status mysql

# 检查MySQL端口
netstat -tlnp | grep 3306
```

#### 问题：端口被占用

**错误信息**：
```
Port 8081 was already in use
```

**解决方案**：
1. 查找占用端口的进程
2. 杀死进程或更换端口

```bash
# Windows
netstat -ano | findstr 8081
taskkill /F /PID <PID>

# Linux
netstat -tlnp | grep 8081
kill -9 <PID>
```

### 2. 页面加载失败

#### 问题：图表不显示

**可能原因**：
1. ECharts库未加载
2. 数据接口返回错误
3. JavaScript代码错误

**解决方案**：
1. 打开浏览器控制台（F12）查看错误信息
2. 检查Network标签页，确认API请求成功
3. 检查Console标签页，确认无JavaScript错误

#### 问题：数据加载缓慢

**可能原因**：
1. 数据库查询慢
2. 数据量过大
3. 缺少索引

**解决方案**：
1. 添加数据库索引
2. 优化SQL查询
3. 启用Redis缓存

### 3. 数据异常

#### 问题：数据显示为0

**可能原因**：
1. 数据库中没有数据
2. SQL查询条件错误

**解决方案**：
1. 生成测试数据
2. 检查SQL语句

---

## 性能优化

### 1. 数据库优化

#### 1.1 添加索引

```sql
-- 用户表索引
CREATE INDEX idx_user_create_time ON im_user(create_time);
CREATE INDEX idx_user_status ON im_user(status);

-- 消息表索引
CREATE INDEX idx_message_create_time ON im_message(create_time);
CREATE INDEX idx_message_sender_id ON im_message(sender_id);
CREATE INDEX idx_message_type ON im_message(message_type);

-- 群组表索引
CREATE INDEX idx_group_create_time ON im_group(create_time);

-- 会话表索引
CREATE INDEX idx_conversation_type ON im_conversation(type);
CREATE INDEX idx_conversation_related_id ON im_conversation(related_id);
```

#### 1.2 优化SQL查询

- 避免 `SELECT *`，只查询需要的字段
- 使用 `LIMIT` 限制返回条数
- 合理使用索引

### 2. 缓存优化

#### 2.1 启用Redis缓存

当前版本已禁用缓存，如需启用：

1. 安装并启动Redis服务
2. 取消注释 `application.yml` 中的Redis配置
3. 取消注释 `ruoyi-framework/pom.xml` 中的Redis依赖
4. 取消注释 `ImDashboardServiceImpl.java` 中的缓存代码
5. 重启项目

#### 2.2 缓存策略

| 数据类型 | TTL | 说明 |
|---------|-----|------|
| 基础统计 | 60秒 | 变化频率高 |
| 趋势数据 | 5分钟 | 历史数据不变 |
| 分布数据 | 10分钟 | 历史数据不变 |
| 排行榜 | 10分钟 | 历史数据不变 |
| 实时数据 | 30秒 | 近实时更新 |

### 3. 前端优化

#### 3.1 启用自动刷新

页面默认30秒自动刷新一次，可根据需要调整：

```javascript
setInterval(loadStatistics, 30000); // 30秒
```

#### 3.2 使用CDN加载ECharts

```html
<!-- 使用CDN -->
<script src="https://cdn.jsdelivr.net/npm/echarts@4.2.1/dist/echarts.min.js"></script>
```

---

## 附录

### A. 目录结构

```
ruoyi-im-admin/
├── ruoyi-admin/              # Web服务入口
│   ├── src/main/
│   │   ├── java/
│   │   │   └── com/ruoyi/web/
│   │   │       ├── controller/im/
│   │   │       │   └── ImDashboardController.java
│   │   │       ├── service/
│   │   │       │   ├── ImDashboardService.java
│   │   │       │   └── impl/
│   │   │       │       ├── ImDashboardServiceImpl.java
│   │   │       │       └── ImDashboardCacheService.java
│   │   │       ├── mapper/
│   │   │       │   └── ImStatisticsMapper.java
│   │   │       └── domain/
│   │   └── resources/
│   │       ├── mapper/web/
│   │       │   └── ImStatisticsMapper.xml
│   │       ├── templates/
│   │       │   └── main-new.html  # 新Dashboard页面
│   │       ├── static/js/
│   │       │   └── dashboard-charts.js  # 图表插件
│   │       └── application.yml
├── ruoyi-framework/          # 框架核心
│   └── src/main/java/com/ruoyi/framework/config/
│       └── RedisConfig.java
└── docs/                    # 文档
    ├── dashboard-api-test.md
    └── dashboard-deployment.md  # 本文档
```

### B. 默认账号

| 用户类型 | 用户名 | 密码 | 权限 |
|---------|--------|------|------|
| 超级管理员 | admin | admin123 | 所有权限 |

### C. 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| HTTP | 8081 | Web服务端口 |

### D. 日志文件

| 日志类型 | 路径 | 说明 |
|---------|------|------|
| 应用日志 | `logs/sys-info.log` | 应用运行日志 |
| 错误日志 | `logs/sys-error.log` | 错误日志 |

---

## 联系方式

如有问题，请联系：

- 项目地址：`D:\MyCode\im`
- 文档地址：`D:\MyCode\im\ruoyi-im-admin\docs`
- API测试：参见 `dashboard-api-test.md`

---

**最后更新时间**：2025-01-17
**文档版本**：v1.0
