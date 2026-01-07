-- =============================================
-- IM系统数据库迁移脚本
-- 版本: v1.0 -> v2.0
-- 日期: 2026-01-07
-- 说明: 从现有数据库结构迁移到优化后的结构
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 迁移前准备
-- =============================================
/*
执行前请确认：
1. 已备份现有数据库
2. 确认系统处于维护状态
3. 确认有足够的磁盘空间
4. 确认MySQL版本为5.7.44或更高版本
*/

-- =============================================
-- 步骤1: 创建临时备份表
-- =============================================

-- 备份 im_session 表数据
DROP TABLE IF EXISTS `im_session_backup`;
CREATE TABLE `im_session_backup` AS SELECT * FROM `im_session`;

-- 备份 im_conversation 表数据
DROP TABLE IF EXISTS `im_conversation_backup`;
CREATE TABLE `im_conversation_backup` AS SELECT * FROM `im_conversation`;

-- 备份 im_conversation_member 表数据
DROP TABLE IF EXISTS `im_conversation_member_backup`;
CREATE TABLE `im_conversation_member_backup` AS SELECT * FROM `im_conversation_member`;

-- 备份 im_message 表数据
DROP TABLE IF EXISTS `im_message_backup`;
CREATE TABLE `im_message_backup` AS SELECT * FROM `im_message`;

-- =============================================
-- 步骤2: 数据迁移 - im_message 表优化
-- =============================================

-- 创建新的 im_message 表结构
DROP TABLE IF EXISTS `im_message_new`;
CREATE TABLE `im_message_new`  (
  `id` bigint(20) NOT NULL COMMENT '消息ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息类型（TEXT文本 FILE文件 NOTICE通知 RECALL撤回 REPLY回复 FORWARD转发 IMAGE图片 VOICE语音 VIDEO视频）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容（JSON格式）',
  `reply_to_message_id` bigint(20) NULL DEFAULT NULL COMMENT '回复的消息ID',
  `forward_from_message_id` bigint(20) NULL DEFAULT NULL COMMENT '转发的消息ID',
  `is_revoked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否撤回（0否 1是）',
  `revoked_time` datetime NULL DEFAULT NULL COMMENT '撤回时间',
  `sensitive_level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NONE' COMMENT '敏感级别（NONE无 WARN警告 BLOCK拦截）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_conversation_time`(`conversation_id`, `create_time`) USING BTREE,
  INDEX `idx_reply_to_message_id`(`reply_to_message_id`) USING BTREE,
  INDEX `idx_forward_from_message_id`(`forward_from_message_id`) USING BTREE,
  FULLTEXT INDEX `ft_content`(`content`),
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  INDEX `idx_conversation_deleted`(`conversation_id`, `is_deleted`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息表（新结构）' ROW_FORMAT = DYNAMIC;

-- 迁移消息数据（移除 receiver_id、status、parent_id、send_time 字段）
INSERT INTO `im_message_new` (
  `id`, `conversation_id`, `sender_id`, `type`, `content`,
  `reply_to_message_id`, `forward_from_message_id`, `is_revoked`,
  `revoked_time`, `sensitive_level`, `is_deleted`, `deleted_time`,
  `create_time`, `update_time`
)
SELECT
  `id`, `conversation_id`, `sender_id`, `type`, `content`,
  `reply_to_message_id`, `forward_from_message_id`, `is_revoked`,
  `revoked_time`, `sensitive_level`, `is_deleted`, `deleted_time`,
  `create_time`, `update_time`
FROM `im_message`;

-- =============================================
-- 步骤3: 数据迁移 - im_conversation 表优化
-- =============================================

-- 创建新的 im_conversation 表结构
DROP TABLE IF EXISTS `im_conversation_new`;
CREATE TABLE `im_conversation_new`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话类型（PRIVATE私聊 GROUP群聊）',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID（私聊为好友ID，群聊为群组ID）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会话名称（群聊为群组名称，私聊为对方昵称）',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会话图标（群聊为群组图标，私聊为对方头像）',
  `last_message_id` bigint(20) NULL DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_target`(`type`, `target_id`) USING BTREE,
  INDEX `idx_last_message`(`last_message_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  INDEX `idx_type_target_time`(`type`, `target_id`, `last_message_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会话表（新结构）' ROW_FORMAT = DYNAMIC;

-- 迁移会话数据
INSERT INTO `im_conversation_new` (
  `id`, `type`, `target_id`, `name`, `avatar`,
  `last_message_id`, `last_message_time`, `is_deleted`, `deleted_time`,
  `create_time`, `update_time`
)
SELECT
  `id`, `type`, `target_id`, `name`, `avatar`,
  `last_message_id`, `last_message_time`, `is_deleted`, `deleted_time`,
  `create_time`, `update_time`
FROM `im_conversation`;

-- =============================================
-- 步骤4: 数据迁移 - im_conversation_member 表优化
-- =============================================

-- 创建新的 im_conversation_member 表结构
DROP TABLE IF EXISTS `im_conversation_member_new`;
CREATE TABLE `im_conversation_member_new`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `unread_count` int(11) NOT NULL DEFAULT 0 COMMENT '未读消息数',
  `last_read_message_id` bigint(20) NULL DEFAULT NULL COMMENT '最后阅读消息ID',
  `is_pinned` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `is_muted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否免打扰',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_conversation_user`(`conversation_id`, `user_id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  INDEX `idx_last_read_message_id`(`last_read_message_id`) USING BTREE,
  INDEX `idx_user_pinned_time`(`user_id`, `is_pinned`, `create_time`) USING BTREE,
  INDEX `idx_user_unread`(`user_id`, `unread_count`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会话成员表（新结构）' ROW_FORMAT = DYNAMIC;

-- 迁移会话成员数据
INSERT INTO `im_conversation_member_new` (
  `id`, `conversation_id`, `user_id`, `unread_count`, `last_read_message_id`,
  `is_pinned`, `is_muted`, `is_deleted`, `deleted_time`,
  `create_time`, `update_time`
)
SELECT
  `id`, `conversation_id`, `user_id`, `unread_count`, `last_read_message_id`,
  `is_pinned`, `is_muted`, `is_deleted`, `deleted_time`,
  `create_time`, `update_time`
FROM `im_conversation_member`;

-- =============================================
-- 步骤5: 迁移 im_session 数据到 im_conversation_member
-- =============================================

-- 将 im_session 中的数据迁移到 im_conversation_member
-- 注意：这里需要根据业务逻辑进行转换
INSERT INTO `im_conversation_member_new` (
  `conversation_id`, `user_id`, `unread_count`, `is_pinned`, `is_muted`,
  `is_deleted`, `deleted_time`, `create_time`, `update_time`
)
SELECT
  `conversation_id`, `user_id`, `unread_count`, `is_pinned`, `is_muted`,
  0, NULL, `create_time`, `update_time`
FROM `im_session`
WHERE `conversation_id` IS NOT NULL
ON DUPLICATE KEY UPDATE
  `unread_count` = VALUES(`unread_count`),
  `is_pinned` = VALUES(`is_pinned`),
  `is_muted` = VALUES(`is_muted`),
  `update_time` = NOW();

-- =============================================
-- 步骤6: 删除旧表并重命名新表
-- =============================================

-- 删除旧的 im_message 表
DROP TABLE IF EXISTS `im_message`;

-- 重命名新的 im_message 表
RENAME TABLE `im_message_new` TO `im_message`;

-- 添加外键约束
ALTER TABLE `im_message`
ADD CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
ADD CONSTRAINT `fk_message_forward_from` FOREIGN KEY (`forward_from_message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
ADD CONSTRAINT `fk_message_reply_to` FOREIGN KEY (`reply_to_message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
ADD CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT;

-- 删除旧的 im_conversation 表
DROP TABLE IF EXISTS `im_conversation`;

-- 重命名新的 im_conversation 表
RENAME TABLE `im_conversation_new` TO `im_conversation`;

-- 添加外键约束
ALTER TABLE `im_conversation`
ADD CONSTRAINT `fk_conversation_last_message` FOREIGN KEY (`last_message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT;

-- 删除旧的 im_conversation_member 表
DROP TABLE IF EXISTS `im_conversation_member`;

-- 重命名新的 im_conversation_member 表
RENAME TABLE `im_conversation_member_new` TO `im_conversation_member`;

-- 添加外键约束
ALTER TABLE `im_conversation_member`
ADD CONSTRAINT `fk_conversation_member_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
ADD CONSTRAINT `fk_conversation_member_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT;

-- =============================================
-- 步骤7: 删除 im_session 表
-- =============================================

-- 删除 im_session 表（已迁移到 im_conversation_member）
DROP TABLE IF EXISTS `im_session`;

-- =============================================
-- 步骤8: 验证数据迁移
-- =============================================

-- 验证消息数据
SELECT '消息数据迁移验证' AS 验证项,
       COUNT(*) AS 记录数
FROM `im_message`;

-- 验证会话数据
SELECT '会话数据迁移验证' AS 验证项,
       COUNT(*) AS 记录数
FROM `im_conversation`;

-- 验证会话成员数据
SELECT '会话成员数据迁移验证' AS 验证项,
       COUNT(*) AS 记录数
FROM `im_conversation_member`;

-- 验证外键约束
SELECT '外键约束验证' AS 验证项,
       COUNT(*) AS 约束数
FROM `information_schema.KEY_COLUMN_USAGE`
WHERE `TABLE_SCHEMA` = 'im'
  AND `CONSTRAINT_NAME` LIKE 'fk_%';

-- =============================================
-- 步骤9: 清理备份表（可选）
-- =============================================

-- 取消下面的注释以删除备份表
-- DROP TABLE IF EXISTS `im_session_backup`;
-- DROP TABLE IF EXISTS `im_conversation_backup`;
-- DROP TABLE IF EXISTS `im_conversation_member_backup`;
-- DROP TABLE IF EXISTS `im_message_backup`;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 迁移完成
-- =============================================

/*
迁移完成后的操作：
1. 验证数据完整性
2. 测试系统功能
3. 更新应用程序代码
4. 更新 Domain 类和 Mapper XML
5. 性能测试
6. 监控系统运行状态

回滚方案：
如果迁移失败，可以使用备份表恢复数据：
1. DROP TABLE `im_message`;
2. RENAME TABLE `im_message_backup` TO `im_message`;
3. 对其他表执行相同操作
*/
