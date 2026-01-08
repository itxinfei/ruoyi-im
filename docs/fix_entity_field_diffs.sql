-- ============================================
-- 实体类与数据库表字段差异修复脚本
-- 自动生成日期: 2026-01-08
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;

-- ============================================
-- 1. im_department 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_department` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
  `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父部门ID，根部门为0',
  `ancestors` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '祖级列表，逗号分隔，如：0,1,2',
  `order_num` INT(11) NOT NULL DEFAULT 0 COMMENT '显示顺序，数字越小越靠前',
  `leader_id` BIGINT(20) NULL DEFAULT NULL COMMENT '负责人用户ID',
  `phone` VARCHAR(20) NULL DEFAULT NULL COMMENT '联系电话',
  `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '部门状态: 0=正常, 1=停用',
  `del_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志: 0=存在, 1=删除',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '部门描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id` (`parent_id`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IM部门表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 2. im_department_member 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_department_member` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `department_id` BIGINT(20) NOT NULL COMMENT '部门ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为主部门: 0=否, 1=是',
  `position` VARCHAR(50) NULL DEFAULT NULL COMMENT '职位名称',
  `join_time` DATETIME NULL DEFAULT NULL COMMENT '入职时间',
  `leave_time` DATETIME NULL DEFAULT NULL COMMENT '离职时间',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态: 0=在职, 1=离职',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_department_id` (`department_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_is_primary` (`is_primary`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IM部门成员关系表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 3. im_friend 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_friend` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '好友关系ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `friend_id` BIGINT(20) NOT NULL COMMENT '好友ID',
  `remark` VARCHAR(100) NULL DEFAULT NULL COMMENT '备注',
  `group_name` VARCHAR(50) NULL DEFAULT NULL COMMENT '好友分组',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否, 1=是',
  `deleted_time` DATETIME NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend` (`user_id`, `friend_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_friend_id` (`friend_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友关系表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 4. im_friend_request 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_friend_request` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_user_id` BIGINT(20) NOT NULL COMMENT '申请人用户ID',
  `to_user_id` BIGINT(20) NOT NULL COMMENT '被申请人用户ID',
  `message` VARCHAR(500) NULL DEFAULT NULL COMMENT '申请消息',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING待处理 APPROVED已同意 REJECTED已拒绝）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handled_time` DATETIME NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_user` (`from_user_id`) USING BTREE,
  INDEX `idx_to_user` (`to_user_id`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友申请表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 5. im_group_announcement 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_group_announcement` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `group_id` BIGINT(20) NOT NULL COMMENT '群组ID',
  `sender_id` BIGINT(20) NOT NULL COMMENT '发送者ID',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `type` INT(11) NOT NULL DEFAULT 1 COMMENT '公告类型: 1=普通公告, 2=系统公告, 3=活动通知',
  `attachment_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '附件URL（图片、文件等）',
  `is_pinned` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶: 0=否, 1=是',
  `status` INT(11) NOT NULL DEFAULT 1 COMMENT '状态: 1=正常, 0=已撤回',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `expire_time` DATETIME NULL DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_id` (`group_id`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '群组公告表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 6. im_group_blacklist 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_group_blacklist` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` BIGINT(20) NOT NULL COMMENT '群组ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '被禁言/拉黑用户ID',
  `operator_id` BIGINT(20) NOT NULL COMMENT '操作人ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型: MUTE=禁言, BLACKLIST=黑名单',
  `reason` VARCHAR(500) NULL DEFAULT NULL COMMENT '原因',
  `expire_time` DATETIME NULL DEFAULT NULL COMMENT '过期时间',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否生效：0=否, 1=是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_id` (`group_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_type` (`type`) USING BTREE,
  INDEX `idx_is_active` (`is_active`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IM群组黑名单/禁言表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 7. im_message_mention 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_message_mention` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` BIGINT(20) NOT NULL COMMENT '消息ID',
  `mentioned_user_id` BIGINT(20) NOT NULL COMMENT '被@的用户ID',
  `mentioned_by` BIGINT(20) NOT NULL COMMENT '@操作者ID',
  `mention_type` VARCHAR(20) NOT NULL COMMENT '提及类型：USER用户、ALL所有人',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读、1已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_id` (`message_id`) USING BTREE,
  INDEX `idx_mentioned_user` (`mentioned_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息@提及表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 8. im_message_read 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_message_read` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` BIGINT(20) NOT NULL COMMENT '消息ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '已读用户ID',
  `conversation_id` BIGINT(20) NOT NULL COMMENT '会话ID',
  `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '已读时间',
  `read_type` VARCHAR(20) NULL DEFAULT NULL COMMENT '已读类型',
  `device_type` VARCHAR(20) NULL DEFAULT NULL COMMENT '设备类型',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_id` (`message_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IM消息已读回执表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 9. im_message_reaction 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_message_reaction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '反应ID',
  `message_id` BIGINT(20) NOT NULL COMMENT '消息ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `reaction_type` VARCHAR(20) NULL DEFAULT NULL COMMENT '反应类型',
  `emoji` VARCHAR(50) NULL DEFAULT NULL COMMENT 'emoji表情字符',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user_emoji` (`message_id`, `user_id`, `emoji`) USING BTREE,
  INDEX `idx_message_id` (`message_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IM消息表情反应表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 10. im_file_share 表创建（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `im_file_share` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分享ID',
  `file_id` BIGINT(20) NOT NULL COMMENT '文件ID',
  `sharer_id` BIGINT(20) NOT NULL COMMENT '分享者ID',
  `receiver_ids` VARCHAR(500) NULL DEFAULT NULL COMMENT '接收者ID，多个接收者用逗号分隔',
  `permission` INT(11) NOT NULL DEFAULT 1 COMMENT '分享权限: 1=公开, 2=指定人可见, 3=密码保护',
  `access_password` VARCHAR(50) NULL DEFAULT NULL COMMENT '访问密码',
  `allow_download` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '允许下载: 0=否, 1=是',
  `allow_preview` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '允许预览: 0=否, 1=是',
  `expire_time` DATETIME NULL DEFAULT NULL COMMENT '过期时间',
  `access_count` INT(11) NOT NULL DEFAULT 0 COMMENT '访问次数',
  `download_count` INT(11) NOT NULL DEFAULT 0 COMMENT '下载次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_file_id` (`file_id`) USING BTREE,
  INDEX `idx_sharer_id` (`sharer_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件分享记录表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 修改现有表结构（添加可能缺失的字段）
-- ============================================

-- im_group 表添加 all_muted 字段
ALTER TABLE `im_group`
ADD COLUMN `all_muted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '全员禁言：0=否，1=是' AFTER `max_members`;

-- im_message 表添加编辑相关字段
ALTER TABLE `im_message`
ADD COLUMN `is_edited` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已编辑（0否 1是）' AFTER `is_revoked`,
ADD COLUMN `edited_content` TEXT NULL DEFAULT NULL COMMENT '编辑后的内容' AFTER `is_edited`,
ADD COLUMN `edit_count` INT(11) NOT NULL DEFAULT 0 COMMENT '编辑次数' AFTER `edited_content`,
ADD COLUMN `edit_time` DATETIME NULL DEFAULT NULL COMMENT '最后编辑时间' AFTER `edit_count`;

-- ============================================
-- 修复 im_sensitive_word 表（使其与实体类匹配）
-- ============================================
-- 注意：实体类 ImSensitiveWord 只有 word, level, status, createTime, updateTime
-- 需要决定是修改实体类还是数据库表
-- 这里提供两种方案：

-- 方案1：修改数据库表以匹配实体类（简化版）
-- ALTER TABLE `im_sensitive_word`
-- DROP COLUMN `word_type`,
-- DROP COLUMN `action`,
-- DROP COLUMN `replacement`,
-- MODIFY COLUMN `level` VARCHAR(20) NULL DEFAULT NULL COMMENT '敏感级别：WARN警告, BLOCK拦截',
-- ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE启用, DISABLED停用' AFTER `level`;

-- 方案2：保持数据库表结构，更新实体类（推荐）
-- 实体类需要添加的字段已在数据库中存在

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '============================================' AS '';
SELECT '实体类与数据库表字段差异修复完成！' AS message;
SELECT '============================================' AS '';
