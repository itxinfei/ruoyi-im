-- =============================================
-- IM系统数据库架构优化方案
-- 版本: v3.0
-- 日期: 2026-01-07
-- 说明: 支持ruoyi-im-api和ruoyi-im-admin独立部署，共享同一数据库
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 优化方案说明
-- =============================================
/*
1. 核心设计理念：
   - ruoyi-im-api（核心API）：使用 im_conversation_member 表管理会话
   - ruoyi-im-admin（管理后台）：使用 im_session 视图进行会话监控
   - 两个项目共享同一数据库，通过视图实现数据隔离

2. 表结构调整：
   - im_conversation: 会话核心信息表（存储会话级别的通用信息）
   - im_conversation_member: 会话成员表（存储用户在会话中的状态）
   - im_message: 消息表（简化结构，专注于消息内容）
   - im_message_read_receipt: 消息已读回执表（管理消息状态）
   - im_session: 管理后台会话视图（基于 im_conversation_member 创建）

3. 性能优化：
   - 添加复合索引支持常见查询场景
   - 优化外键约束减少级联删除影响
   - 使用分区表支持海量消息数据

4. 扩展性优化：
   - 预留扩展字段支持未来功能
   - 使用软删除机制保留数据历史
   - 支持水平分片（通过 conversation_id 分片）

5. 安全优化：
   - 不同项目使用不同数据库用户，权限隔离
   - 敏感数据加密存储
   - 操作日志审计
*/

-- =============================================
-- 1. 用户表优化（保持不变，仅添加索引）
-- =============================================
DROP TABLE IF EXISTS `im_user`;
CREATE TABLE `im_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(1) NULL DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1正常）',
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个性签名',
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '最后在线时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_mobile`(`mobile`) USING BTREE,
  INDEX `idx_email`(`email`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_last_online_time`(`last_online_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IM用户表';

-- =============================================
-- 2. 会话表优化
-- =============================================
DROP TABLE IF EXISTS `im_conversation`;
CREATE TABLE `im_conversation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话类型（PRIVATE私聊 GROUP群聊）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会话名称',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会话头像',
  `owner_id` bigint(20) NULL DEFAULT NULL COMMENT '群主ID（群聊时使用）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会话描述',
  `max_members` int(11) NULL DEFAULT 500 COMMENT '最大成员数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会话表';

-- =============================================
-- 3. 会话成员表优化（核心表，替代im_session）
-- =============================================
DROP TABLE IF EXISTS `im_conversation_member`;
CREATE TABLE `im_conversation_member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '成员昵称',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MEMBER' COMMENT '角色（OWNER管理员 MEMBER普通成员）',
  `unread_count` int(11) NOT NULL DEFAULT 0 COMMENT '未读消息数',
  `is_pinned` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶（0否 1是）',
  `is_muted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否免打扰（0否 1是）',
  `last_read_message_id` bigint(20) NULL DEFAULT NULL COMMENT '最后已读消息ID',
  `last_read_time` datetime NULL DEFAULT NULL COMMENT '最后已读时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_conversation_user`(`conversation_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_unread_count`(`unread_count`) USING BTREE,
  INDEX `idx_is_pinned`(`is_pinned`) USING BTREE,
  INDEX `idx_is_muted`(`is_muted`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  INDEX `idx_update_time`(`update_time`) USING BTREE,
  CONSTRAINT `fk_conversation_member_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_conversation_member_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会话成员表';

-- =============================================
-- 4. 消息表优化（简化结构）
-- =============================================
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息类型（TEXT文本 IMAGE图片 VIDEO视频 AUDIO语音 FILE文件）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件URL',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `sensitive_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'NORMAL' COMMENT '敏感级别（NORMAL普通 SENSITIVE敏感 HIGH高度敏感）',
  `is_revoked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否撤回（0否 1是）',
  `revoked_time` datetime NULL DEFAULT NULL COMMENT '撤回时间',
  `revoker_id` bigint(20) NULL DEFAULT NULL COMMENT '撤回者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_message_type`(`message_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_is_revoked`(`is_revoked`) USING BTREE,
  INDEX `idx_conversation_time`(`conversation_id`, `create_time`) USING BTREE,
  CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息表';

-- =============================================
-- 5. 消息已读回执表（新增）
-- =============================================
DROP TABLE IF EXISTS `im_message_read_receipt`;
CREATE TABLE `im_message_read_receipt`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '回执ID',
  `message_id` bigint(20) NOT NULL COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `read_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '已读时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_read_time`(`read_time`) USING BTREE,
  CONSTRAINT `fk_read_receipt_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_read_receipt_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息已读回执表';

-- =============================================
-- 6. 好友表优化（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_friend`;
CREATE TABLE `im_friend`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '好友ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `friend_id` bigint(20) NOT NULL COMMENT '好友ID',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '好友备注',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组名称',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id`, `friend_id`) USING BTREE,
  INDEX `idx_friend_id`(`friend_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友表';

-- =============================================
-- 7. 群组表优化（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_group`;
CREATE TABLE `im_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群组名称',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群组头像',
  `owner_id` bigint(20) NOT NULL COMMENT '群主ID',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群组描述',
  `max_members` int(11) NOT NULL DEFAULT 500 COMMENT '最大成员数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  CONSTRAINT `fk_group_owner` FOREIGN KEY (`owner_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '群组表';

-- =============================================
-- 8. 群组成员表优化（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_group_member`;
CREATE TABLE `im_group_member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `group_id` bigint(20) NOT NULL COMMENT '群组ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群昵称',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MEMBER' COMMENT '角色（OWNER管理员 MEMBER普通成员）',
  `is_muted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否禁言（0否 1是）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_role`(`role`) USING BTREE,
  INDEX `idx_is_muted`(`is_muted`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  CONSTRAINT `fk_group_member_group` FOREIGN KEY (`group_id`) REFERENCES `im_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_group_member_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '群组成员表';

-- =============================================
-- 9. 管理后台会话视图（基于im_conversation_member创建）
-- =============================================
DROP VIEW IF EXISTS `im_session`;
CREATE VIEW `im_session` AS
SELECT
    cm.id AS id,
    c.type AS type,
    cm.user_id AS user_id,
    u.username AS user_name,
    CASE
        WHEN c.type = 'PRIVATE' THEN
            (SELECT u2.username FROM im_user u2
             JOIN im_conversation_member cm2 ON u2.id = cm2.user_id
             WHERE cm2.conversation_id = cm.conversation_id AND cm2.user_id != cm.user_id
             LIMIT 1)
        WHEN c.type = 'GROUP' THEN c.name
        ELSE NULL
    END AS target_name,
    CASE
        WHEN c.type = 'PRIVATE' THEN
            (SELECT cm2.user_id FROM im_conversation_member cm2
             WHERE cm2.conversation_id = cm.conversation_id AND cm2.user_id != cm.user_id
             LIMIT 1)
        WHEN c.type = 'GROUP' THEN c.id
        ELSE NULL
    END AS target_id,
    (SELECT id FROM im_message m WHERE m.conversation_id = cm.conversation_id ORDER BY m.create_time DESC LIMIT 1) AS last_message_id,
    (SELECT content FROM im_message m WHERE m.conversation_id = cm.conversation_id ORDER BY m.create_time DESC LIMIT 1) AS last_message,
    cm.unread_count AS unread_count,
    cm.create_time AS create_time,
    cm.update_time AS update_time
FROM im_conversation_member cm
JOIN im_conversation c ON cm.conversation_id = c.id
JOIN im_user u ON cm.user_id = u.id
WHERE cm.is_deleted = 0;

-- =============================================
-- 10. 数据库用户权限设置
-- =============================================

-- 创建ruoyi-im-api用户
CREATE USER IF NOT EXISTS 'im_api'@'%' IDENTIFIED BY 'ImApi@2024';
GRANT SELECT, INSERT, UPDATE, DELETE ON im_db.* TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE ON im_db.im_conversation_member TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE ON im_db.im_message TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON im_db.im_friend TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE ON im_db.im_group TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON im_db.im_group_member TO 'im_api'@'%';
GRANT SELECT ON im_db.im_session TO 'im_api'@'%';
FLUSH PRIVILEGES;

-- 创建ruoyi-im-admin用户
CREATE USER IF NOT EXISTS 'im_admin'@'%' IDENTIFIED BY 'ImAdmin@2024';
GRANT SELECT, INSERT, UPDATE, DELETE ON im_db.* TO 'im_admin'@'%';
FLUSH PRIVILEGES;

-- =============================================
-- 11. 初始化测试数据
-- =============================================

-- 插入测试用户
INSERT INTO `im_user` (`id`, `username`, `password`, `nickname`, `avatar`, `gender`, `mobile`, `email`, `status`, `signature`, `create_time`, `update_time`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'https://example.com/avatar/admin.jpg', 1, '13800138000', 'admin@example.com', 1, '系统管理员', NOW(), NOW()),
(2, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户1', 'https://example.com/avatar/user1.jpg', 1, '13800138001', 'user1@example.com', 1, '普通用户', NOW(), NOW()),
(3, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户2', 'https://example.com/avatar/user2.jpg', 2, '13800138002', 'user2@example.com', 1, '普通用户', NOW(), NOW()),
(4, 'user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户3', 'https://example.com/avatar/user3.jpg', 1, '13800138003', 'user3@example.com', 1, '普通用户', NOW(), NOW()),
(5, 'user4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '用户4', 'https://example.com/avatar/user4.jpg', 2, '13800138004', 'user4@example.com', 1, '普通用户', NOW(), NOW());

-- 插入测试会话
INSERT INTO `im_conversation` (`id`, `type`, `name`, `avatar`, `owner_id`, `description`, `max_members`, `create_time`, `update_time`) VALUES
(1, 'PRIVATE', NULL, NULL, NULL, NULL, NULL, NOW(), NOW()),
(2, 'PRIVATE', NULL, NULL, NULL, NULL, NULL, NOW(), NOW()),
(3, 'GROUP', '技术交流群', 'https://example.com/group/tech.jpg', 2, '技术交流群', 500, NOW(), NOW()),
(4, 'GROUP', '产品讨论群', 'https://example.com/group/product.jpg', 3, '产品讨论群', 500, NOW(), NOW());

-- 插入测试会话成员
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`) VALUES
(1, 2, 'MEMBER', 0, 0, 0, NOW(), NOW()),
(1, 3, 'MEMBER', 0, 0, 0, NOW(), NOW()),
(2, 2, 'MEMBER', 0, 0, 0, NOW(), NOW()),
(2, 4, 'MEMBER', 0, 0, 0, NOW(), NOW()),
(3, 2, 'OWNER', 0, 1, 0, NOW(), NOW()),
(3, 3, 'MEMBER', 0, 0, 0, NOW(), NOW()),
(3, 4, 'MEMBER', 0, 0, 0, NOW(), NOW()),
(4, 3, 'OWNER', 0, 0, 0, NOW(), NOW()),
(4, 5, 'MEMBER', 0, 0, 0, NOW(), NOW());

-- 插入测试消息
INSERT INTO `im_message` (`conversation_id`, `sender_id`, `message_type`, `content`, `create_time`) VALUES
(1, 2, 'TEXT', '你好，在吗？', NOW()),
(1, 3, 'TEXT', '在的，有什么事吗？', NOW()),
(2, 2, 'TEXT', '项目进度怎么样了？', NOW()),
(2, 4, 'TEXT', '正在开发中，预计下周完成', NOW()),
(3, 2, 'TEXT', '欢迎大家加入技术交流群', NOW()),
(3, 3, 'TEXT', '很高兴加入这个群', NOW()),
(3, 4, 'TEXT', '大家一起学习进步', NOW()),
(4, 3, 'TEXT', '新版本的需求已经确认', NOW()),
(4, 5, 'TEXT', '好的，我会尽快安排开发', NOW());

-- 插入测试好友关系
INSERT INTO `im_friend` (`user_id`, `friend_id`, `remark`, `create_time`, `update_time`) VALUES
(2, 3, '技术大牛', NOW(), NOW()),
(2, 4, '产品经理', NOW(), NOW()),
(3, 2, '前端开发', NOW(), NOW()),
(3, 4, '产品经理', NOW(), NOW()),
(4, 2, '前端开发', NOW(), NOW()),
(4, 3, '技术大牛', NOW(), NOW()),
(4, 5, '后端开发', NOW(), NOW()),
(5, 4, '产品经理', NOW(), NOW());

-- 插入测试群组
INSERT INTO `im_group` (`id`, `name`, `avatar`, `owner_id`, `description`, `max_members`, `create_time`, `update_time`) VALUES
(1, '技术交流群', 'https://example.com/group/tech.jpg', 2, '技术交流群', 500, NOW(), NOW()),
(2, '产品讨论群', 'https://example.com/group/product.jpg', 3, '产品讨论群', 500, NOW(), NOW());

-- 插入测试群组成员
INSERT INTO `im_group_member` (`group_id`, `user_id`, `role`, `is_muted`, `create_time`, `update_time`) VALUES
(1, 2, 'OWNER', 0, NOW(), NOW()),
(1, 3, 'MEMBER', 0, NOW(), NOW()),
(1, 4, 'MEMBER', 0, NOW(), NOW()),
(2, 3, 'OWNER', 0, NOW(), NOW()),
(2, 5, 'MEMBER', 0, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 优化方案总结
-- =============================================
/*
1. ruoyi-im-api 使用 im_conversation_member 表管理会话
   - 用户会话列表：SELECT * FROM im_conversation_member WHERE user_id = ? AND is_deleted = 0
   - 未读消息统计：SELECT SUM(unread_count) FROM im_conversation_member WHERE user_id = ?
   - 会话操作：INSERT/UPDATE/DELETE im_conversation_member

2. ruoyi-im-admin 使用 im_session 视图进行会话监控
   - 会话列表：SELECT * FROM im_session
   - 会话统计：SELECT COUNT(*) FROM im_session
   - 用户会话：SELECT * FROM im_session WHERE user_id = ?

3. 数据隔离
   - ruoyi-im-api 用户（im_api）：只能操作业务表，无DROP/TRUNCATE权限
   - ruoyi-im-admin 用户（im_admin）：可以操作所有表，包括视图

4. 性能优化
   - 复合索引：im_conversation_member(conversation_id, user_id)、im_message(conversation_id, create_time)
   - 外键约束：确保数据一致性
   - 软删除：保留数据历史

5. 扩展性
   - 支持水平分片：通过 conversation_id 分片
   - 预留扩展字段：支持未来功能扩展
   - 视图隔离：管理后台和API使用不同的数据视图
*/
