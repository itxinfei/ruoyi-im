-- 端到端加密密钥表
CREATE TABLE IF NOT EXISTS `im_e2e_key` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `public_key` TEXT NOT NULL COMMENT '公钥(PEM格式)',
    `key_version` INT DEFAULT 1 COMMENT '密钥版本',
    `key_algorithm` VARCHAR(20) DEFAULT 'RSA' COMMENT '密钥算法：RSA/ECC',
    `key_size` INT DEFAULT 2048 COMMENT '密钥长度',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否激活',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `expire_time` DATETIME COMMENT '过期时间',
    UNIQUE KEY `uk_user_version` (`user_id`, `key_version`),
    INDEX `idx_user_active` (`user_id`, `is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='端到端加密密钥表';

-- 为im_message表添加E2E加密标识字段
ALTER TABLE `im_message` ADD COLUMN IF NOT EXISTS `is_encrypted` TINYINT DEFAULT 0 COMMENT '是否E2E加密';
ALTER TABLE `im_message` ADD COLUMN IF NOT EXISTS `encryption_key_version` INT COMMENT '加密密钥版本';
