-- 添加消息表的转发消息ID字段
-- 执行时间: 2024-01-01
-- 说明: 支持消息转发功能，记录转发来源消息ID

USE `im`;

-- 添加转发消息ID字段
ALTER TABLE `im_message` ADD COLUMN `forward_from_message_id` bigint(20) DEFAULT NULL COMMENT '转发的消息ID' AFTER `reply_to_message_id`;

-- 为转发消息ID字段添加索引，提升查询性能
CREATE INDEX `idx_forward_from` ON `im_message` (`forward_from_message_id`);
