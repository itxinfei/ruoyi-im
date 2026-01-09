-- ----------------------------
-- 文档模块相关表
-- ----------------------------

-- 在线文档表
CREATE TABLE IF NOT EXISTS `im_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
    `doc_type` VARCHAR(20) NOT NULL DEFAULT 'doc' COMMENT '文档类型（doc=文档, sheet=表格, mind=思维导图）',
    `content` LONGTEXT COMMENT '文档内容（HTML/JSON格式）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '文档状态（draft=草稿, published=已发布, archived=已归档）',
    `owner_id` BIGINT NOT NULL COMMENT '所有者ID',
    `folder_id` BIGINT COMMENT '所属文件夹ID',
    `is_starred` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否收藏',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（回收站）',
    `deleted_time` DATETIME COMMENT '删除时间',
    `auto_delete_days` INT COMMENT '自动删除天数（回收站）',
    `last_modified_by` BIGINT COMMENT '最后修改者ID',
    `last_modified_time` DATETIME COMMENT '最后修改时间',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `parent_id` BIGINT COMMENT '父文档ID',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_is_deleted` (`is_deleted`),
    KEY `idx_is_starred` (`is_starred`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线文档表';

-- 文档分享表
CREATE TABLE IF NOT EXISTS `im_document_share` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分享ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `shared_to_user_id` BIGINT NOT NULL COMMENT '分享给的用户ID',
    `shared_by_user_id` BIGINT NOT NULL COMMENT '分享者ID',
    `permission` VARCHAR(20) NOT NULL DEFAULT 'view' COMMENT '权限（view=查看, edit=编辑, admin=管理）',
    `is_expired` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否过期',
    `expire_time` DATETIME COMMENT '过期时间',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_document_user` (`document_id`, `shared_to_user_id`),
    KEY `idx_shared_to_user_id` (`shared_to_user_id`),
    KEY `idx_is_expired` (`is_expired`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档分享表';

-- 文档评论表
CREATE TABLE IF NOT EXISTS `im_document_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT COMMENT '父评论ID（用于回复）',
    `selected_text` TEXT COMMENT '选中的文本（用于高亮评论）',
    `selection_offset` INT COMMENT '选中文本的偏移量',
    `is_resolved` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已解决',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档评论表';

-- 文档版本表
CREATE TABLE IF NOT EXISTS `im_document_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '版本ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `version` INT NOT NULL COMMENT '版本号',
    `content` LONGTEXT COMMENT '文档内容',
    `title_snapshot` VARCHAR(200) COMMENT '文档标题快照',
    `modified_by` BIGINT COMMENT '修改者ID',
    `modified_by_name` VARCHAR(100) COMMENT '修改者名称',
    `change_description` VARCHAR(500) COMMENT '修改说明',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_version` (`version`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档版本表';
