# 链接卡片预览功能文档

## 功能概述

当用户在消息中输入 URL 链接时，系统自动抓取网页元数据（标题、描述、缩略图），并以卡片形式展示。

---

## 技术实现

### 后端实现

#### 1. 数据库表：`im_url_metadata`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint(20) | 主键 |
| url | varchar(1024) | URL 地址（唯一） |
| title | varchar(500) | 页面标题 |
| description | varchar(1000) | 页面描述 |
| image_url | varchar(500) | 缩略图 URL |
| site_name | varchar(200) | 网站名称 |
| favicon_url | varchar(500) | 网站图标 |
| content_type | varchar(50) | 内容类型 |
| fetch_status | varchar(20) | 抓取状态：PENDING/SUCCESS/FAILED |
| error_message | varchar(500) | 错误信息 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| expire_time | datetime | 过期时间（7 天） |

#### 2. API 接口

**解析 URL 元数据**
```
GET /api/im/url/parse?url=https://example.com
```

**刷新 URL 元数据**
```
POST /api/im/url/refresh?url=https://example.com
```

#### 3. 核心流程

```
1. 接收请求 → 验证 URL 格式
2. 查询缓存 → 检查是否过期（7 天内有效）
3. 异步抓取 → Jsoup 解析网页（15 秒超时）
4. 解析元数据 → Open Graph 协议优先
5. 保存缓存 → 写入数据库
6. 返回结果 → 前端展示卡片
```

### 前端实现

#### 1. 输入检测（MessageInput.vue）

- 正则检测：`/(https?:\/\/[^\s]+)/g`
- 检测到链接时显示预览提示
- 发送时自动调用解析 API

#### 2. 消息渲染（MessageBubble.vue）

- 支持 `LINK` 类型消息
- 使用 `LinkCardMessage` 组件渲染

#### 3. 卡片组件（LinkCardMessage.vue）

- 缩略图展示（120px 高度）
- 标题（最多 200 字符）
- 描述（最多 2 行，省略号）
- URL 缩短显示

---

## 使用场景

### 场景 1：发送单个链接

用户输入：
```
https://github.com
```

发送后展示：
```
┌─────────────────────────┐
│  [缩略图]                │
│  GitHub                  │
│  Build software better   │
│  🔗 github.com           │
└─────────────────────────┘
```

### 场景 2：发送带文字的链接

用户输入：
```
查看这个链接 https://example.com
```

发送后展示：同上（自动检测第一个链接）

---

## 异常处理

| 异常 | 处理方式 |
|------|----------|
| URL 格式无效 | 降级为普通文本消息 |
| 抓取超时（15 秒） | 返回失败状态，显示原文本 |
| 网页无法访问 | 记录错误信息，降级为文本 |
| 无元数据 | 显示默认标题"链接预览" |
| 图片加载失败 | 隐藏缩略图区域 |

---

## 数据库迁移

执行以下 SQL 创建表：

```bash
mysql -h 172.168.20.222 -P 3306 -u im -p123456 im < sql/migrations/20260304_url_metadata.sql
```

---

## 测试用例

### 测试 1：解析有效 URL
```
输入：https://www.baidu.com
期望：返回标题"百度一下，你就知道"
```

### 测试 2：解析带 Open Graph 的 URL
```
输入：https://github.com
期望：返回 title、description、image
```

### 测试 3：无效 URL
```
输入：not-a-url
期望：返回错误"无效的 URL 格式"
```

### 测试 4：缓存测试
```
1. 第一次解析 https://example.com
2. 立即再次解析同一 URL
期望：第二次直接返回缓存结果
```

---

## 后续优化

1. **批量解析**：支持一条消息多个链接的解析
2. **富媒体预览**：支持视频、音频等特殊类型
3. **自定义 favicon**：使用 Google favicon 服务
4. **定期清理**：定时任务清理过期缓存
