-- ========================================
-- 链接卡片解析功能 - 消息数据结构扩展
-- Date: 2026-02-05
-- Description: 为im_message表添加link_preview_data字段用于存储链接预览信息
-- ========================================

-- 添加链接预览数据列
ALTER TABLE im_message ADD COLUMN IF NOT EXISTS link_preview_data TEXT COMMENT '链接预览JSON数据，包含title、description、image、favicon等信息';

-- 为已有数据添加注释说明（可选）
ALTER TABLE im_message MODIFY COLUMN link_preview_data TEXT COMMENT '链接预览JSON数据，包含title、description、image、favicon等信息';

-- 创建索引以提高查询性能（如果经常需要查询包含链接的消息）
CREATE INDEX IF NOT EXISTS idx_im_message_has_link ON im_message((link_preview_data IS NOT NULL AND link_preview_data != ''));

-- ========================================
-- 回滚脚本（如需回滚请执行以下语句）
-- ========================================
-- DROP INDEX IF EXISTS idx_im_message_has_link ON im_message;
-- ALTER TABLE im_message DROP COLUMN IF EXISTS link_preview_data;
