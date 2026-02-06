-- =====================================================
-- RuoYi-IM 消息同步点表
-- 用于记录用户各设备的最后同步时间戳，实现断线重连后的消息补发
-- 参考野火IM的同步机制设计
-- =====================================================

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

-- =====================================================
-- 索引说明
-- =====================================================
-- uk_user_device: 确保同一用户同一设备只有一个同步点记录
-- idx_user_id: 加速查询用户的所有设备同步点
-- idx_update_time: 用于清理长期未更新的同步点
-- =====================================================
