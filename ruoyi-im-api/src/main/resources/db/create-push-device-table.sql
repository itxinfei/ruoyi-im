-- 推送设备表
CREATE TABLE IF NOT EXISTS `im_push_device` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型：ANDROID/iOS/WEB',
    `device_token` VARCHAR(500) NOT NULL COMMENT '设备Token',
    `gotify_client_id` VARCHAR(64) COMMENT 'Gotify客户端ID',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否激活',
    `last_active_time` DATETIME COMMENT '最后活跃时间',
    `device_name` VARCHAR(100) COMMENT '设备名称',
    `app_version` VARCHAR(50) COMMENT '应用版本',
    `os_version` VARCHAR(50) COMMENT '操作系统版本',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_user_active` (`user_id`, `is_active`),
    UNIQUE KEY `uk_user_token` (`user_id`, `device_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送设备表';
