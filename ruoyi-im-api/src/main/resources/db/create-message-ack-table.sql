-- =============================================
-- RuoYi-IM 消息确认(ACK)表
-- 用于记录消息的送达、接收、已读确认
-- =============================================

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

-- =============================================
-- 消息ACK统计视图（可选）
-- =============================================
