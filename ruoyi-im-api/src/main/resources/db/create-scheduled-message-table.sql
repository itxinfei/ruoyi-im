-- 定时消息表
CREATE TABLE IF NOT EXISTS `im_scheduled_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型：TEXT/FILE/IMAGE/VOICE/VIDEO',
    `content` TEXT COMMENT '消息内容',
    `file_url` VARCHAR(500) COMMENT '文件URL',
    `file_name` VARCHAR(255) COMMENT '文件名',
    `reply_to_message_id` BIGINT COMMENT '回复的消息ID',
    `scheduled_time` DATETIME NOT NULL COMMENT '定时发送时间',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/SENT/FAILED/CANCELLED',
    `sent_message_id` BIGINT COMMENT '发送后的消息ID',
    `error_msg` VARCHAR(500) COMMENT '失败原因',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `sent_time` DATETIME COMMENT '实际发送时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_status` (`user_id`, `status`),
    INDEX `idx_scheduled_time` (`scheduled_time`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时消息表';
