-- =============================================
-- RuoYi-IM 群组权限配置表
-- 用于配置群组不同角色的细粒度权限
-- =============================================

CREATE TABLE IF NOT EXISTS `im_group_permission` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '群组ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: OWNER-群主, ADMIN-管理员, MEMBER-普通成员',
    `can_invite` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邀请成员权限: 0=禁止, 1=允许',
    `can_remove` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '移除成员权限: 0=禁止, 1=允许',
    `can_mute` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '禁言成员权限: 0=禁止, 1=允许',
    `can_announce` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '发布公告权限: 0=禁止, 1=允许',
    `can_upload` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '上传文件权限: 0=禁止, 1=允许',
    `can_edit_group` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '修改群信息权限: 0=禁止, 1=允许',
    `can_kick` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '踢人权限: 0=禁止, 1=允许',
    `can_set_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '设置管理员权限: 0=禁止, 1=允许',
    `can_disband` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '解散群组权限: 0=禁止, 1=允许',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_role` (`group_id`, `role`),
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组权限配置表';
