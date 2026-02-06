-- 创建聊天背景表
-- 用于存储用户的聊天背景设置（全局或特定会话）

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
