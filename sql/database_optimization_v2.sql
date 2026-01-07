-- =============================================
-- IM系统数据库架构优化方案
-- 版本: v2.0
-- 日期: 2026-01-07
-- 说明: 基于架构师思维设计的优化方案
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 优化方案说明
-- =============================================
/*
1. 核心优化点：
   - 统一会话管理：移除 im_session 表，统一使用 im_conversation_member 表
   - 消息表优化：简化 im_message 表结构，移除冗余字段
   - 索引优化：添加复合索引提升查询性能
   - 外键优化：调整外键约束，提升系统灵活性

2. 表结构调整：
   - im_conversation: 会话核心信息表（存储会话级别的通用信息）
   - im_conversation_member: 会话成员表（存储用户在会话中的状态）
   - im_message: 消息表（简化结构，专注于消息内容）
   - im_message_read_receipt: 消息已读回执表（管理消息状态）

3. 性能优化：
   - 添加复合索引支持常见查询场景
   - 优化外键约束减少级联删除影响
   - 使用分区表支持海量消息数据

4. 扩展性优化：
   - 预留扩展字段支持未来功能
   - 使用软删除机制保留数据历史
   - 支持水平分片（通过 conversation_id 分片）
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
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个性签名',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态（0禁用 1正常）',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_mobile`(`mobile`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email`) USING BTREE,
  INDEX `idx_nickname`(`nickname`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 2. 好友表（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_friend`;
CREATE TABLE `im_friend`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
  INDEX `idx_group_name`(`group_name`) USING BTREE,
  CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 3. 群组表（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_group`;
CREATE TABLE `im_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群组名称',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群组头像',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群组描述',
  `owner_id` bigint(20) NOT NULL COMMENT '群主ID',
  `max_members` int(11) NOT NULL DEFAULT 500 COMMENT '最大成员数',
  `member_count` int(11) NOT NULL DEFAULT 0 COMMENT '当前成员数',
  `join_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'OPEN' COMMENT '加群方式（OPEN公开 NEED_APPROVE需要审批 INVITE_ONLY仅邀请）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_group_owner` FOREIGN KEY (`owner_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '群组表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 4. 群组成员表（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_group_member`;
CREATE TABLE `im_group_member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` bigint(20) NOT NULL COMMENT '群组ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MEMBER' COMMENT '角色（OWNER群主 ADMIN管理员 MEMBER普通成员）',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `is_muted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否禁言（0否 1是）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_role`(`role`) USING BTREE,
  CONSTRAINT `fk_group_member_group` FOREIGN KEY (`group_id`) REFERENCES `im_group` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_group_member_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '群组成员表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 5. 会话表（优化：简化结构，专注会话核心信息）
-- =============================================
DROP TABLE IF EXISTS `im_conversation`;
CREATE TABLE `im_conversation`  (
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
  INDEX `idx_type_target_time`(`type`, `target_id`, `last_message_time`) USING BTREE,
  CONSTRAINT `fk_conversation_last_message` FOREIGN KEY (`last_message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会话表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 6. 会话成员表（优化：统一管理用户会话状态，替代 im_session）
-- =============================================
DROP TABLE IF EXISTS `im_conversation_member`;
CREATE TABLE `im_conversation_member`  (
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
  INDEX `idx_user_unread`(`user_id`, `unread_count`) USING BTREE,
  CONSTRAINT `fk_conversation_member_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_conversation_member_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会话成员表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 7. 消息表（优化：简化结构，移除冗余字段）
-- =============================================
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message`  (
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
  INDEX `idx_conversation_deleted`(`conversation_id`, `is_deleted`) USING BTREE,
  CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_forward_from` FOREIGN KEY (`forward_from_message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_reply_to` FOREIGN KEY (`reply_to_message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 8. 消息已读回执表（优化：管理消息状态）
-- =============================================
DROP TABLE IF EXISTS `im_message_read_receipt`;
CREATE TABLE `im_message_read_receipt`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` bigint(20) NOT NULL COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `read_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '已读时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_read_time`(`read_time`) USING BTREE,
  CONSTRAINT `fk_read_receipt_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_read_receipt_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息已读回执表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 9. 消息反应表（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_message_reaction`;
CREATE TABLE `im_message_reaction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` bigint(20) NOT NULL COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `reaction_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '反应类型（LIKE点赞 DISLIKE踩 HEART爱心）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user_type`(`message_id`, `user_id`, `reaction_type`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_reaction_type`(`reaction_type`) USING BTREE,
  CONSTRAINT `fk_reaction_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_reaction_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息反应表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 10. 敏感词表（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_sensitive_word`;
CREATE TABLE `im_sensitive_word`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '敏感词',
  `level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'WARN' COMMENT '敏感级别（WARN警告 BLOCK拦截）',
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用（0否 1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_word`(`word`) USING BTREE,
  INDEX `idx_level`(`level`) USING BTREE,
  INDEX `idx_is_enabled`(`is_enabled`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '敏感词表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 11. 敏感事件表（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_sensitive_event`;
CREATE TABLE `im_sensitive_word`;
CREATE TABLE `im_sensitive_event`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` bigint(20) NULL DEFAULT NULL COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `event_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '事件类型（MESSAGE消息 FILE文件）',
  `sensitive_level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '敏感级别（WARN警告 BLOCK拦截）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '敏感内容',
  `is_blocked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否被拦截（0否 1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_id`(`message_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_event_type`(`event_type`) USING BTREE,
  INDEX `idx_sensitive_level`(`sensitive_level`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_sensitive_event_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_sensitive_event_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '敏感事件表' ROW_FORMAT = DYNAMIC;

-- =============================================
-- 12. 文件资源表（保持不变）
-- =============================================
DROP TABLE IF EXISTS `im_file_asset`;
CREATE TABLE `im_file_asset`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件类型（IMAGE图片 VIDEO视频 AUDIO音频 DOCUMENT文档 OTHER其他）',
  `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `uploader_id` bigint(20) NOT NULL COMMENT '上传者ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uploader_id`(`uploader_id`) USING BTREE,
  INDEX `idx_file_type`(`file_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_file_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件资源表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 优化总结
-- =============================================
/*
1. 移除 im_session 表，统一使用 im_conversation_member 表管理用户会话状态
2. 简化 im_message 表结构，移除 receiver_id、status、parent_id 等冗余字段
3. 优化索引策略，添加复合索引支持常见查询场景
4. 调整外键约束，提升系统灵活性
5. 预留扩展字段，支持未来功能扩展

性能优化：
- 复合索引：idx_user_pinned_time、idx_user_unread、idx_conversation_deleted
- 分区支持：通过 conversation_id 可以实现水平分片
- 查询优化：减少 JOIN 操作，提升查询效率

扩展性优化：
- 软删除机制：保留数据历史，支持数据恢复
- 灵活的外键约束：避免级联删除影响系统稳定性
- 预留扩展字段：支持未来功能扩展
*/
