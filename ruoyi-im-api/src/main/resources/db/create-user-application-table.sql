-- 创建用户应用安装表
-- 用于记录用户安装的应用列表和配置信息

CREATE TABLE IF NOT EXISTS `im_user_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `app_id` BIGINT NOT NULL COMMENT '应用ID',
    `is_pinned` TINYINT DEFAULT 0 COMMENT '是否固定到工作台：0=否, 1=是',
    `sort_order` INT DEFAULT 0 COMMENT '排序位置',
    `app_config` TEXT COMMENT '应用配置（JSON格式）',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0=禁用, 1=启用',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_app` (`user_id`, `app_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_app_id` (`app_id`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户应用安装表';
