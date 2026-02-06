# 待执行的SQL语句

## 概述
本文档列出了需要手动执行以修复数据库缺失问题的SQL语句。

---

## 1. 聊天背景表 (im_chat_background)

**文件**: `ruoyi-im-api/src/main/resources/db/create-chat-background-table.sql`

```sql
-- 创建聊天背景表
-- 用于存储用户的聊天背景设置（全局或特定会话）

CREATE TABLE IF NOT EXISTS `im_chat_background` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `conversation_id` BIGINT DEFAULT NULL COMMENT '会话ID（NULL表示全局背景）',
    `background_type` VARCHAR(20) DEFAULT 'default' COMMENT '背景类型：default=默认, color=纯色, image=图片',
    `background_value` VARCHAR(500) DEFAULT NULL COMMENT '背景值（颜色值或图片URL）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_conversation` (`user_id`, `conversation_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天背景设置表';
```

**用途**: 支持用户设置聊天背景功能
**相关API**:
- GET `/api/im/user/background`
- PUT `/api/im/user/background`
- DELETE `/api/im/user/background`

---

## 2. 用户应用安装表 (im_user_application)

**文件**: `ruoyi-im-api/src/main/resources/db/create-user-application-table.sql`

```sql
-- 创建用户应用安装表
-- 用于记录用户安装的应用列表和配置信息

CREATE TABLE IF NOT EXISTS `im_user_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `app_id` BIGINT NOT NULL COMMENT '应用ID',
    `is_pinned` TINYINT DEFAULT 0 COMMENT '是否固定到工作台：0=否, 1=是',
    `sort_order` INT DEFAULT 0 COMMENT '排序位置',
    `app_config` TEXT COMMENT '应用配置（JSON格式）',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0=禁用, 1=启用',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_app` (`user_id`, `app_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_app_id` (`app_id`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户应用安装表';
```

**用途**: 记录用户安装的应用列表和配置
**相关API**: `/api/im/app/*`

---

## 执行方法

### 方法一：使用MySQL客户端
```bash
mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-api/src/main/resources/db/create-chat-background-table.sql
mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-api/src/main/resources/db/create-user-application-table.sql
```

### 方法二：手动在数据库客户端执行
1. 打开数据库客户端（如Navicat、DBeaver等）
2. 连接到数据库 `172.168.20.222` 数据库 `im`
3. 分别执行上述两个SQL脚本

---

## 验证

执行完成后，可以使用以下语句验证表是否创建成功：

```sql
-- 检查聊天背景表
SHOW CREATE TABLE im_chat_background;

-- 检查用户应用安装表
SHOW CREATE TABLE im_user_application;

-- 或者使用DESCRIBE
DESCRIBE im_chat_background;
DESCRIBE im_user_application;
```

---

---

## 3. 消息同步点表 (im_user_sync_point)

**文件**: `ruoyi-im-api/src/main/resources/db/create-sync-point-table.sql`

**用途**: 记录用户各设备的最后同步时间戳，实现断线重连后的消息补发
**相关API**:
- GET `/api/im/message/sync` - 同步消息
- GET `/api/im/message/sync/points` - 获取同步点列表
- DELETE `/api/im/message/sync/point/{deviceId}` - 重置同步点

**已完成的代码**:
- 实体类: `ImUserSyncPoint.java`
- Mapper: `ImUserSyncPointMapper.java`
- Service: `ImMessageSyncService.java` / `ImMessageSyncServiceImpl.java`

---

## 4. 离线消息表 (im_offline_message)

**文件**: `ruoyi-im-api/src/main/resources/db/create-offline-message-table.sql`

**用途**: 存储用户离线期间的消息（持久化备份，主要使用Redis）
**相关API**:
- 用户上线时自动通过 WebSocket 推送离线消息

**已完成的代码**:
- 实体类: `ImOfflineMessage.java`
- Mapper: `ImOfflineMessageMapper.java`
- Service: `IOfflineMessageService.java` / `OfflineMessageServiceImpl.java`

---

## 执行方法

### 一键执行所有脚本
```bash
# 按顺序执行所有表创建脚本
mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-api/src/main/resources/db/create-chat-background-table.sql
mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-api/src/main/resources/db/create-user-application-table.sql
mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-api/src/main/resources/db/create-sync-point-table.sql
mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-api/src/main/resources/db/create-offline-message-table.sql
mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-api/src/main/resources/db/create-user-device-table.sql
```

---

## 验证

执行完成后，可以使用以下语句验证所有表是否创建成功：

```sql
-- 检查所有新创建的表
SHOW CREATE TABLE im_chat_background;
SHOW CREATE TABLE im_user_application;
SHOW CREATE TABLE im_user_sync_point;
SHOW CREATE TABLE im_offline_message;
SHOW CREATE TABLE im_user_device;

-- 或者使用DESCRIBE
DESCRIBE im_chat_background;
DESCRIBE im_user_application;
DESCRIBE im_user_sync_point;
DESCRIBE im_offline_message;
```

---

## 后续代码已完成

### 聊天背景功能 (im_chat_background)
1. **实体类**: `ImChatBackground.java`
2. **Mapper接口**: `ImChatBackgroundMapper.java`
3. **Mapper XML**: `ImChatBackgroundMapper.xml`
4. **Service方法**: `ImUserServiceImpl.java` 中的以下方法：
   - `getChatBackground()`
   - `setChatBackground()`
   - `clearChatBackground()`
   - `scanQRCode()`
5. **Controller端点**: `ImUserController.java` 中的以下端点：
   - GET `/api/im/user/background`
   - PUT `/api/im/user/background`
   - DELETE `/api/im/user/background`
   - POST `/api/im/user/scan-qrcode`

### 消息同步功能 (im_user_sync_point)
1. **实体类**: `ImUserSyncPoint.java`
2. **Mapper接口**: `ImUserSyncPointMapper.java`
3. **Service接口**: `ImMessageSyncService.java`
4. **Service实现**: `ImMessageSyncServiceImpl.java`
5. **Controller端点**: `ImMessageController.java` 中的以下端点：
   - GET `/api/im/message/sync`
   - GET `/api/im/message/sync/points`
   - DELETE `/api/im/message/sync/point/{deviceId}`

### 离线消息功能 (im_offline_message)
1. **实体类**: `ImOfflineMessage.java`
2. **Mapper接口**: `ImOfflineMessageMapper.java`
3. **Service接口**: `IOfflineMessageService.java`
4. **Service实现**: `OfflineMessageServiceImpl.java`
5. **WebSocket集成**: `ImWebSocketEndpoint.java` 自动推送离线消息

---

## 5. 用户设备表 (im_user_device)

**文件**: `ruoyi-im-api/src/main/resources/db/create-user-device-table.sql`

**用途**: 管理用户的多设备登录，支持设备类型识别、在线状态查询
**相关API**:
- POST `/api/im/device/register` - 注册设备
- POST `/api/im/device/heartbeat` - 设备心跳
- GET `/api/im/device/list` - 获取设备列表
- GET `/api/im/device/online` - 获取在线设备
- DELETE `/api/im/device/{deviceId}` - 移除设备
- GET `/api/im/device/stats` - 设备统计

**已完成的代码**:
- 实体类: `ImUserDevice.java`
- Mapper: `ImUserDeviceMapper.java`
- Service接口: `ImDeviceService.java`
- Service实现: `ImDeviceServiceImpl.java`
- Controller: `ImDeviceController.java`
- 前端API: `device.js` (含心跳管理器)
- WebSocket集成: `ImWebSocketEndpoint.java` 自动注册设备
