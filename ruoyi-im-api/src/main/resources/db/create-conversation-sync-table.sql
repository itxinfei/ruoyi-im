-- =============================================
-- RuoYi-IM 会话同步点表
-- 用于记录用户各设备的会话设置同步点
-- =============================================

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
