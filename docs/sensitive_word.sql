-- =============================================
-- 敏感词相关表
-- =============================================

-- 敏感词表
DROP TABLE IF EXISTS `im_sensitive_word`;
CREATE TABLE `im_sensitive_word` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
    `word` varchar(100) NOT NULL COMMENT '敏感词内容',
    `word_type` varchar(20) DEFAULT 'OTHER' COMMENT '敏感词类型（POLITICAL政治、VULGAR低俗、VIOLENCE暴力、AD广告、OTHER其他）',
    `level` int DEFAULT 1 COMMENT '敏感级别（1低 2中 3高）',
    `action` varchar(20) DEFAULT 'REPLACE' COMMENT '处理方式（REPLACE替换、REJECT拒绝、WARN警告）',
    `replacement` varchar(50) DEFAULT '***' COMMENT '替换内容',
    `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`),
    KEY `idx_word_type` (`word_type`),
    KEY `idx_level` (`level`),
    KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 插入示例敏感词数据
INSERT INTO `im_sensitive_word` (`word`, `word_type`, `level`, `action`, `is_enabled`) VALUES
('笨蛋', 'VULGAR', 1, 'REPLACE', 1),
('白痴', 'VULGAR', 1, 'REPLACE', 1),
('傻瓜', 'VULGAR', 1, 'REPLACE', 1),
('暴力', 'VIOLENCE', 2, 'REPLACE', 1),
('赌博', 'VIOLENCE', 3, 'REJECT', 1),
('加微信', 'AD', 1, 'REPLACE', 1),
('代练', 'AD', 1, 'REPLACE', 1);
