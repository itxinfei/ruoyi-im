-- =====================================================
-- RuoYi-IM 数据库表创建脚本
-- 用于消息同步、离线消息和多端设备管理
-- 执行方式: mysql -h 172.168.20.222 -u im -p123456 im < ruoyi-im-create-tables.sql
-- =====================================================

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

-- =====================================================
-- 验证表是否创建成功
-- =====================================================
-- SHOW CREATE TABLE im_user_sync_point;
-- SHOW CREATE TABLE im_offline_message;
-- SHOW CREATE TABLE im_user_device;
