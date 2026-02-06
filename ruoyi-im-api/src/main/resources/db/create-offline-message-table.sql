-- =====================================================
-- RuoYi-IM 离线消息表
-- 用于存储用户离线期间的消息，用户上线后推送给客户端
-- 参考野火IM的离线消息队列设计
-- =====================================================
-- 注意：本表作为持久化备份，主要使用Redis存储离线消息
-- 当Redis容量不足或重启时，可从此表恢复
-- =====================================================

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

-- =====================================================
-- 索引说明
-- =====================================================
-- idx_user_status: 加速查询用户的未拉取离线消息
-- idx_message_id: 关联原消息表
-- idx_create_time: 用于定时清理过期离线消息（如7天前）
-- idx_status_pull: 查询已拉取但未删除的消息
-- =====================================================
-- =====================================================
-- 定期清理过期离线消息（可选，通过定时任务执行）
-- DELETE FROM im_offline_message WHERE create_time < DATE_SUB(NOW(), INTERVAL 7 DAY);
-- =====================================================
