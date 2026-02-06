-- =====================================================
-- RuoYi-IM 用户设备表
-- 用于管理用户的多设备登录，支持设备类型识别、在线状态查询
-- 参考野火IM的设备管理设计
-- =====================================================

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
-- 索引说明
-- =====================================================
-- uk_user_device: 确保同一用户同一设备只有一个记录
-- idx_user_id: 加速查询用户的所有设备
-- idx_device_type: 按设备类型统计
-- idx_online_status: 快速查询在线设备
-- idx_heartbeat_time: 定时任务查询超时设备
-- =====================================================

-- =====================================================
-- 设备类型枚举说明
-- =====================================================
-- web: Web浏览器
-- ios: iOS移动设备（iPhone/iPad）
-- android: Android移动设备
-- pc: Windows PC
-- mac: Mac电脑
-- mini: 微信小程序等小程序平台
-- =====================================================
