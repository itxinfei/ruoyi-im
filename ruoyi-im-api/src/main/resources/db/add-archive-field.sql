-- 为会话成员表添加归档字段
-- 用于支持会话归档功能

-- 添加 is_archived 字段
ALTER TABLE `im_conversation_member`
ADD COLUMN `is_archived` TINYINT DEFAULT 0 COMMENT '是否归档：0=否, 1=是';

-- 为查询归档会话添加索引
ALTER TABLE `im_conversation_member`
ADD INDEX `idx_user_archived` (`user_id`, `is_archived`, `is_deleted`);
