-- =====================================================
-- RuoYi-IM 完整数据库表创建脚本
-- 包含 Phase 1-6 所有新增表
-- 执行方式: mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-complete.sql
-- =====================================================

-- -----------------------------------------------------
-- Phase 1: 消息同步机制
-- -----------------------------------------------------

-- 1. 消息同步点表
CREATE TABLE IF NOT EXISTS `im_user_sync_point` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID（客户端生成，如：web_abc123）',
    `last_sync_time` BIGINT NOT NULL DEFAULT 0 COMMENT '最后同步时间戳(毫秒)',
    `last_message_id` BIGINT DEFAULT 0 COMMENT '最后同步消息ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_device` (`user_id`, `device_id`) COMMENT '用户设备唯一索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_update_time` (`update_time`) COMMENT '更新时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息同步点表';

-- -----------------------------------------------------
-- Phase 2: 离线消息队列
-- -----------------------------------------------------

-- 2. 离线消息表
CREATE TABLE IF NOT EXISTS `im_offline_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `message_id` BIGINT NOT NULL COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型（TEXT/FILE/IMAGE/VOICE/VIDEO等）',
    `message_content` TEXT COMMENT '消息内容（文本或JSON格式）',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `sender_name` VARCHAR(100) COMMENT '发送者名称（快照，避免查询用户表）',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=未拉取 1=已拉取 2=已删除',
    `pull_time` DATETIME COMMENT '拉取时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_status` (`user_id`, `status`) COMMENT '用户状态联合索引',
    KEY `idx_message_id` (`message_id`) COMMENT '消息ID索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引（用于清理过期数据）',
    KEY `idx_status_pull` (`status`, `pull_time`) COMMENT '状态拉取时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='离线消息表';

-- -----------------------------------------------------
-- Phase 3: 多端设备管理
-- -----------------------------------------------------

-- 3. 用户设备表
CREATE TABLE IF NOT EXISTS `im_user_device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID（客户端生成，UUID格式）',
    `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型：web/ios/android/pc/mac/mini',
    `device_name` VARCHAR(100) COMMENT '设备名称（如：iPhone 14 Pro、Chrome浏览器）',
    `online_status` TINYINT DEFAULT 0 COMMENT '在线状态：0=离线 1=在线',
    `last_online_time` DATETIME COMMENT '最后上线时间',
    `last_heartbeat_time` DATETIME COMMENT '最后心跳时间',
    `client_version` VARCHAR(50) COMMENT '客户端版本号',
    `os_version` VARCHAR(50) COMMENT '操作系统版本',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否当前活跃设备',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_device` (`user_id`, `device_id`) COMMENT '用户设备唯一索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_device_type` (`device_type`) COMMENT '设备类型索引',
    KEY `idx_online_status` (`online_status`) COMMENT '在线状态索引',
    KEY `idx_heartbeat_time` (`last_heartbeat_time`) COMMENT '心跳时间索引（用于查询超时设备）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设备表';

-- -----------------------------------------------------
-- Phase 4: WebSocket ACK机制
-- -----------------------------------------------------

-- 4. 消息确认(ACK)表
CREATE TABLE IF NOT EXISTS `im_message_ack` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `message_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '消息ID',
    `conversation_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '会话ID',
    `client_msg_id` VARCHAR(64) DEFAULT NULL COMMENT '客户端消息ID',
    `ack_type` VARCHAR(20) NOT NULL DEFAULT 'deliver' COMMENT 'ACK类型: deliver-送达, receive-接收, read-已读',
    `device_id` VARCHAR(64) DEFAULT NULL COMMENT '设备ID',
    `ack_timestamp` BIGINT(20) UNSIGNED NOT NULL COMMENT 'ACK时间戳(毫秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_message` (`user_id`, `message_id`),
    KEY `idx_conversation` (`conversation_id`),
    KEY `idx_client_msg` (`client_msg_id`),
    KEY `idx_ack_timestamp` (`ack_timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM消息确认表';

-- -----------------------------------------------------
-- Phase 5: 会话同步
-- -----------------------------------------------------

-- 5. 会话同步点表
CREATE TABLE IF NOT EXISTS `im_conversation_sync_point` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID',
    `last_sync_time` BIGINT(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '最后同步时间戳(毫秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_device` (`user_id`, `device_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM会话同步点表';

-- -----------------------------------------------------
-- Phase 6: 群组功能完善
-- -----------------------------------------------------

-- 6. 群组权限配置表
CREATE TABLE IF NOT EXISTS `im_group_permission` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '群组ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: OWNER-群主, ADMIN-管理员, MEMBER-普通成员',
    `can_invite` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邀请成员权限: 0=禁止, 1=允许',
    `can_remove` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '移除成员权限: 0=禁止, 1=允许',
    `can_mute` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '禁言成员权限: 0=禁止, 1=允许',
    `can_announce` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发布公告权限: 0=禁止, 1=允许',
    `can_upload` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '上传文件权限: 0=禁止, 1=允许',
    `can_edit_group` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '修改群信息权限: 0=禁止, 1=允许',
    `can_kick` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '踢人权限: 0=禁止, 1=允许',
    `can_set_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '设置管理员权限: 0=禁止, 1=允许',
    `can_disband` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '解散群组权限: 0=禁止, 1=允许',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_role` (`group_id`, `role`),
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组权限配置表';

-- 7. 群公告已读状态表
CREATE TABLE IF NOT EXISTS `im_group_announcement_read` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '群组ID',
    `announcement_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '公告ID',
    `user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `read_time` DATETIME NOT NULL COMMENT '已读时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群公告已读状态表';

-- -----------------------------------------------------
-- 其他功能表
-- -----------------------------------------------------

-- 8. 聊天背景表
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

-- 9. 用户应用安装表
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

-- -----------------------------------------------------
-- 验证表是否创建成功
-- -----------------------------------------------------
-- SHOW CREATE TABLE im_user_sync_point;
-- SHOW CREATE TABLE im_offline_message;
-- SHOW CREATE TABLE im_user_device;
-- SHOW CREATE TABLE im_message_ack;
-- SHOW CREATE TABLE im_conversation_sync_point;
-- SHOW CREATE TABLE im_group_permission;
-- SHOW CREATE TABLE im_group_announcement_read;
-- SHOW CREATE TABLE im_chat_background;
-- SHOW CREATE TABLE im_user_application;
