-- 添加会话成员表的归档、颜色和标签字段
-- 执行时间: 2024-01-01
-- 说明: 支持会话归档、自定义颜色和标签管理功能

USE `im`;

-- 添加归档字段
ALTER TABLE `im_conversation_member` ADD COLUMN `archived` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否归档（0否 1是）' AFTER `muted`;

-- 添加颜色字段
ALTER TABLE `im_conversation_member` ADD COLUMN `color` varchar(20) DEFAULT NULL COMMENT '会话颜色' AFTER `archived`;

-- 添加标签字段
ALTER TABLE `im_conversation_member` ADD COLUMN `tags` varchar(500) DEFAULT NULL COMMENT '会话标签（多个标签用逗号分隔）' AFTER `color`;

-- 为归档字段添加索引，提升查询性能
CREATE INDEX `idx_archived` ON `im_conversation_member` (`archived`);
