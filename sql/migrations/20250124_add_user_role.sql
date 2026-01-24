-- 添加用户角色字段
-- 执行日期: 2025-01-24
-- 说明: 为 im_user 表添加 role 字段，用于区分普通用户和管理员

-- 添加 role 字段
ALTER TABLE im_user ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '用户角色: USER=普通用户, ADMIN=管理员, SUPER_ADMIN=超级管理员' AFTER status;

-- 为现有用户设置默认角色（id=1 的设为 SUPER_ADMIN，其他设为 USER）
UPDATE im_user SET role = 'SUPER_ADMIN' WHERE id = 1;
UPDATE im_user SET role = 'USER' WHERE id != 1 OR id IS NULL;

-- 添加索引以提升查询性能
CREATE INDEX idx_user_role ON im_user(role);

-- 添加角色字段注释
ALTER TABLE im_user MODIFY COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '用户角色: USER=普通用户, ADMIN=管理员, SUPER_ADMIN=超级管理员';
