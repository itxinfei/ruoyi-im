-- 为 im_user 表添加生日字段
ALTER TABLE im_user ADD COLUMN birthday DATE DEFAULT NULL COMMENT '生日' AFTER position;
