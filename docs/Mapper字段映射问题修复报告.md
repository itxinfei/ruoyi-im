# Mapper字段映射问题修复报告

**项目**: 私有化Web钉钉系统
**日期**: 2026-01-08
**修复原则**: 根据钉钉业务需求决定字段保留或删除

---

## 一、问题概述

后台出现"缺字段"报错。经分析发现`ImMessageMapper.xml`引用了数据库表中不存在的字段。

**根据需求文档分析**（`需求文档.md` 第4.2.1节），钉钉消息功能包括：
- **消息操作**: **撤回（2分钟内）、转发、复制、收藏**

因此这些字段都是**必须保留的**，需要在数据库中添加。

---

## 二、修复方案

| 字段 | 对应钉钉功能 | 决策 |
|------|-------------|------|
| `reply_to_message_id` | 回复/引用消息功能 | **保留** - 在数据库中添加 |
| `forward_from_message_id` | 转发消息功能 | **保留** - 在数据库中添加 |
| `is_deleted` + `deleted_time` | 删除消息功能 | **保留** - 在数据库中添加 |

---

## 三、修复内容

### 3.1 数据库SQL修改

**文件**: `sql/im.sql` (第1408-1445行)

**添加的字段**:
```sql
-- 在 content 字段后添加
`reply_to_message_id` bigint(20) NULL DEFAULT NULL COMMENT '回复的消息ID（支持引用/回复功能）',
`forward_from_message_id` bigint(20) NULL DEFAULT NULL COMMENT '转发来源消息ID（支持转发功能）',

-- 在 edit_time 字段后添加
`is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
`deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',

-- 添加索引
INDEX `idx_reply_to_message_id`(`reply_to_message_id`) USING BTREE,
INDEX `idx_forward_from_message_id`(`forward_from_message_id`) USING BTREE,
INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
```

**数据库迁移脚本**: `sql/alter/im_message_add_missing_fields.sql`

### 3.2 实体类修改

**文件**: `ruoyi-im-api/src/main/java/com/ruoyi/im/domain/ImMessage.java`

将以下字段从`@TableField(exist = false)`（非数据库字段）改为数据库字段：

```java
/** 回复的消息ID（支持回复/引用功能） */
@TableField("reply_to_message_id")
private Long replyToMessageId;

/** 转发来源消息ID（支持转发功能） */
@TableField("forward_from_message_id")
private Long forwardFromMessageId;

/** 是否删除：0否 1是 */
@TableField("is_deleted")
private Integer isDeleted;

/** 删除时间 */
@TableField("deleted_time")
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime deletedTime;

/** 更新时间 */
@TableField("update_time")
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime updateTime;
```

### 3.3 Mapper.xml修改

**文件**: `ruoyi-im-api/src/main/resources/mapper/ImMessageMapper.xml`

1. **更新ResultMap** - 添加字段映射
2. **更新selectImMessageVo** - 添加字段到SELECT语句
3. **更新insertImMessage** - 添加字段到INSERT语句
4. **更新updateImMessage** - 添加字段到UPDATE语句
5. **更新batchInsertImMessage** - 添加字段到批量INSERT
6. **更新searchMessages/countSearchResults** - 默认过滤已删除消息

### 3.4 测试数据修改

**文件**: `sql/im.sql` (第1450-1530行)

更新所有INSERT语句，添加新字段的占位符（NULL值）。

---

## 四、修复后的im_message表结构

```sql
CREATE TABLE `im_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `message_type` varchar(20) NOT NULL COMMENT '消息类型',
  `content` text NULL COMMENT '消息内容',
  `reply_to_message_id` bigint(20) NULL DEFAULT NULL COMMENT '回复的消息ID',
  `forward_from_message_id` bigint(20) NULL DEFAULT NULL COMMENT '转发来源消息ID',
  `file_url` varchar(500) NULL DEFAULT NULL COMMENT '文件URL',
  `file_name` varchar(200) NULL DEFAULT NULL COMMENT '文件名',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小',
  `sensitive_level` varchar(20) DEFAULT 'NORMAL' COMMENT '敏感级别',
  `is_revoked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否撤回',
  `is_edited` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已编辑',
  `edited_content` text NULL COMMENT '编辑后的内容',
  `edit_count` int(11) NOT NULL DEFAULT 0 COMMENT '编辑次数',
  `edit_time` datetime NULL DEFAULT NULL COMMENT '最后编辑时间',
  `revoked_time` datetime NULL DEFAULT NULL COMMENT '撤回时间',
  `revoker_id` bigint(20) NULL DEFAULT NULL COMMENT '撤回者ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_conversation_id`(`conversation_id`),
  INDEX `idx_sender_id`(`sender_id`),
  INDEX `idx_message_type`(`message_type`),
  INDEX `idx_create_time`(`create_time`),
  INDEX `idx_is_revoked`(`is_revoked`),
  INDEX `idx_conversation_time`(`conversation_id`, `create_time`),
  INDEX `idx_is_edited`(`is_edited`),
  INDEX `idx_reply_to_message_id`(`reply_to_message_id`),
  INDEX `idx_forward_from_message_id`(`forward_from_message_id`),
  INDEX `idx_is_deleted`(`is_deleted`)
)
```

---

## 五、部署步骤

### 5.1 执行数据库迁移

```bash
# 在MySQL中执行
mysql -u root -p ry-vue < sql/alter/im_message_add_missing_fields.sql
```

### 5.2 验证字段添加

```sql
DESC im_message;
-- 应该能看到新增的4个字段
```

### 5.3 重启后端服务

```bash
cd ruoyi-im-admin
mvn clean package
java -jar target/ruoyi-im-admin.jar
```

### 5.4 测试验证

1. 发送消息 - 基础功能正常
2. 回复消息 - reply_to_message_id字段正确存储
3. 转发消息 - forward_from_message_id字段正确存储
4. 删除消息 - is_deleted/deleted_time字段正确更新
5. 搜索消息 - 默认过滤已删除消息

---

## 六、修改文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `sql/alter/im_message_add_missing_fields.sql` | 新增 | 数据库迁移脚本 |
| `sql/im.sql` | 修改 | 更新表结构和测试数据 |
| `ruoyi-im-api/.../ImMessage.java` | 修改 | 更新实体类字段注解 |
| `ruoyi-im-api/.../ImMessageMapper.xml` | 修改 | 更新Mapper映射 |

---

## 七、总结

**修复原则**: 字段根据业务需求决定保留或删除
- 钉钉有此功能 → **保留字段** → 添加到数据库、更新代码、更新文档
- 钉钉无此功能 → **删除字段** → 从代码中移除

本次修复确认`reply_to_message_id`、`forward_from_message_id`、`is_deleted`、`deleted_time`这4个字段对应钉钉的核心功能（回复、转发、删除），因此选择在数据库中添加这些字段并更新相关代码。

---

**报告结束**
