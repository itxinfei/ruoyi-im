-- ============================================================
-- Migration: 添加 DING 消息优先级字段
-- Date: 2026-03-11
-- Description: im_ding_message 表添加 priority 字段用于区分紧急/普通 DING
-- ============================================================

-- 添加 priority 字段
ALTER TABLE `im_ding_message`
ADD COLUMN `priority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'NORMAL' COMMENT '优先级：URGENT紧急/NORMAL普通' AFTER `ding_type`;

-- 添加过期时间字段（如果不存在）
ALTER TABLE `im_ding_message`
ADD COLUMN `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间' AFTER `status`;

-- 添加索引
ALTER TABLE `im_ding_message`
ADD INDEX `idx_priority`(`priority`) USING BTREE,
ADD INDEX `idx_expire_time`(`expire_time`) USING BTREE;

-- 更新现有数据：is_urgent=1 的记录设置为 URGENT
UPDATE `im_ding_message` SET `priority` = 'URGENT' WHERE `is_urgent` = 1;
