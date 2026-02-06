-- =============================================
-- RuoYi-IM 群公告已读状态表
-- 用于记录群组成员对公告的已读状态
-- =============================================

CREATE TABLE IF NOT EXISTS `im_group_announcement_read` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '群组ID',
    `announcement_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '公告ID',
    `user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `read_time` DATETIME NOT NULL COMMENT '已读时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群公告已读状态表';
