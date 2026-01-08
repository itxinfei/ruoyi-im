-- =====================================================
-- 添加 im_message 表缺失的字段
-- 功能：支持钉钉的回复、转发、删除消息功能
-- 日期: 2026-01-08
-- =====================================================

USE `im`;

-- 1. 添加回复消息ID字段（支持回复/引用功能）
ALTER TABLE `im_message`
ADD COLUMN `reply_to_message_id` bigint(20) NULL DEFAULT NULL COMMENT '回复的消息ID' AFTER `content`,
ADD INDEX `idx_reply_to_message_id` (`reply_to_message_id`);

-- 2. 添加转发来源消息ID字段（支持转发功能）
ALTER TABLE `im_message`
ADD COLUMN `forward_from_message_id` bigint(20) NULL DEFAULT NULL COMMENT '转发来源消息ID' AFTER `reply_to_message_id`,
ADD INDEX `idx_forward_from_message_id` (`forward_from_message_id`);

-- 3. 添加逻辑删除字段（支持单条消息删除功能）
ALTER TABLE `im_message`
ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）' AFTER `edit_time`,
ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 4. 添加删除时间字段
ALTER TABLE `im_message`
ADD COLUMN `deleted_time` datetime NULL DEFAULT NULL COMMENT '删除时间' AFTER `is_deleted`;

-- 5. 添加外键约束（可选，根据实际需求决定是否启用）
-- ALTER TABLE `im_message`
-- ADD CONSTRAINT `fk_message_reply` FOREIGN KEY (`reply_to_message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT;

-- ALTER TABLE `im_message`
-- ADD CONSTRAINT `fk_message_forward` FOREIGN KEY (`forward_from_message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT;

-- =====================================================
-- 验证字段是否添加成功
-- =====================================================
-- DESC `im_message`;
-- SHOW INDEX FROM `im_message`;
