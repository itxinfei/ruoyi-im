-- ===========================
-- IM系统完整数据库表结构
-- ===========================

-- ----------------------------
-- 1. 用户表（复用RuoYi系统用户表sys_user）
-- ----------------------------

-- ----------------------------
-- 2. 好友关系表
-- ----------------------------
DROP TABLE IF EXISTS `im_friend`;
CREATE TABLE `im_friend` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `friend_user_id` bigint(20) NOT NULL COMMENT '好友用户ID',
  `alias` varchar(50) DEFAULT NULL COMMENT '好友别名',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注信息',
  `status` varchar(10) NOT NULL DEFAULT 'NORMAL' COMMENT '状态（NORMAL正常 BLOCKED拉黑）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`,`friend_user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_user_id` (`friend_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- ----------------------------
-- 3. 好友申请表
-- ----------------------------
DROP TABLE IF EXISTS `im_friend_request`;
CREATE TABLE `im_friend_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '申请人用户ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '被申请人用户ID',
  `message` varchar(200) DEFAULT NULL COMMENT '申请消息',
  `status` varchar(10) NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING待处理 APPROVED已同意 REJECTED已拒绝）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handled_time` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

-- ----------------------------
-- 4. 群组表
-- ----------------------------
DROP TABLE IF EXISTS `im_group`;
CREATE TABLE `im_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `name` varchar(50) NOT NULL COMMENT '群组名称',
  `owner_id` bigint(20) NOT NULL COMMENT '群主用户ID',
  `announcement` varchar(2000) DEFAULT NULL COMMENT '群公告',
  `avatar` varchar(200) DEFAULT NULL COMMENT '群头像',
  `status` varchar(10) NOT NULL DEFAULT 'NORMAL' COMMENT '状态（NORMAL正常 DISMISSED已解散）',
  `member_count` int(11) NOT NULL DEFAULT '0' COMMENT '成员数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

-- ----------------------------
-- 5. 群组成员表
-- ----------------------------
DROP TABLE IF EXISTS `im_group_member`;
CREATE TABLE `im_group_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` bigint(20) NOT NULL COMMENT '群组ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role` varchar(10) NOT NULL DEFAULT 'MEMBER' COMMENT '角色（OWNER群主 ADMIN管理员 MEMBER普通成员）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '群内昵称',
  `mute_until` datetime DEFAULT NULL COMMENT '禁言截止时间',
  `joined_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='群组成员表';

-- ----------------------------
-- 6. 会话表
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation`;
CREATE TABLE `im_conversation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `type` varchar(10) NOT NULL COMMENT '会话类型（PRIVATE私聊 GROUP群聊）',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID（私聊为好友ID，群聊为群组ID）',
  `last_message_id` bigint(20) DEFAULT NULL COMMENT '最后一条消息ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_target` (`type`,`target_id`),
  KEY `idx_last_message` (`last_message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- ----------------------------
-- 7. 会话成员表
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation_member`;
CREATE TABLE `im_conversation_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `last_read_message_id` bigint(20) DEFAULT NULL COMMENT '最后已读消息ID',
  `pinned` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶（0否 1是）',
  `muted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否免打扰（0否 1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_user` (`conversation_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='会话成员表';

-- ----------------------------
-- 8. 消息表
-- ----------------------------
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message` (
  `id` bigint(20) NOT NULL COMMENT '消息ID（雪花算法）',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `type` varchar(10) NOT NULL COMMENT '消息类型（TEXT文本 FILE文件 NOTICE通知 RECALL撤回 REPLY回复 FORWARD转发）',
  `content` text COMMENT '消息内容（JSON格式）',
  `reply_to_message_id` bigint(20) DEFAULT NULL COMMENT '回复的消息ID',
  `status` varchar(10) NOT NULL DEFAULT 'SENT' COMMENT '消息状态（SENT已发送 DELIVERED已送达 READ已读 REVOKED已撤回）',
  `sensitive_level` varchar(10) NOT NULL DEFAULT 'NONE' COMMENT '敏感级别（NONE无 WARN警告 BLOCK拦截）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `revoked_time` datetime DEFAULT NULL COMMENT '撤回时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_reply_to` (`reply_to_message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ----------------------------
-- 9. 文件资产表
-- ----------------------------
DROP TABLE IF EXISTS `im_file_asset`;
CREATE TABLE `im_file_asset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `uploader_id` bigint(20) NOT NULL COMMENT '上传者用户ID',
  `filename` varchar(255) NOT NULL COMMENT '文件名',
  `ext` varchar(10) NOT NULL COMMENT '文件扩展名',
  `mime` varchar(100) NOT NULL COMMENT 'MIME类型',
  `size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
  `md5` varchar(32) NOT NULL COMMENT '文件MD5值',
  `storage_path` varchar(500) NOT NULL COMMENT '存储路径',
  `watermark` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否需要水印（0否 1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_md5` (`md5`),
  KEY `idx_uploader_id` (`uploader_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='文件资产表';

-- ----------------------------
-- 10. 敏感词表
-- ----------------------------
DROP TABLE IF EXISTS `im_sensitive_word`;
CREATE TABLE `im_sensitive_word` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `word` varchar(100) NOT NULL COMMENT '敏感词',
  `level` varchar(10) NOT NULL COMMENT '敏感级别（WARN警告 BLOCK拦截）',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（0否 1是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_level` (`level`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- ----------------------------
-- 11. 敏感词事件表
-- ----------------------------
DROP TABLE IF EXISTS `im_sensitive_event`;
CREATE TABLE `im_sensitive_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `message_id` bigint(20) NOT NULL COMMENT '消息ID',
  `word_id` bigint(20) NOT NULL COMMENT '敏感词ID',
  `level` varchar(10) NOT NULL COMMENT '敏感级别',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_message_id` (`message_id`),
  KEY `idx_word_id` (`word_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='敏感词事件表';

-- ----------------------------
-- 12. 审计导出申请表
-- ----------------------------
DROP TABLE IF EXISTS `im_audit_export_request`;
CREATE TABLE `im_audit_export_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `requested_by` bigint(20) NOT NULL COMMENT '申请人用户ID',
  `reason` varchar(500) NOT NULL COMMENT '申请理由',
  `scope` text NOT NULL COMMENT '导出范围（JSON格式）',
  `status` varchar(10) NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING待审批 APPROVED已批准 REJECTED已拒绝 DONE已完成）',
  `file_path` varchar(500) DEFAULT NULL COMMENT '导出文件路径',
  `approved_by` bigint(20) DEFAULT NULL COMMENT '审批人用户ID',
  `approved_time` datetime DEFAULT NULL COMMENT '审批时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_requested_by` (`requested_by`),
  KEY `idx_approved_by` (`approved_by`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='审计导出申请表';

-- ----------------------------
-- 13. 系统配置表（扩展）
-- ----------------------------
DROP TABLE IF EXISTS `im_system_config`;
CREATE TABLE `im_system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `config_type` varchar(20) NOT NULL DEFAULT 'STRING' COMMENT '配置类型（STRING字符串 NUMBER数字 BOOLEAN布尔 JSON对象）',
  `description` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='IM系统配置表';

-- ----------------------------
-- 初始化系统配置数据
-- ----------------------------
INSERT INTO `im_system_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('im.message.recallWindowSeconds', '120', 'NUMBER', '消息撤回时间窗口（秒）'),
('im.file.maxSizeMb', '20', 'NUMBER', '文件上传最大大小（MB）'),
('im.file.allowExt', '["pdf","docx","xlsx","png","jpg","txt"]', 'JSON', '允许上传的文件扩展名'),
('im.file.enablePreviewWatermark', 'true', 'BOOLEAN', '是否启用预览水印'),
('im.sensitive.enabled', 'true', 'BOOLEAN', '是否启用敏感词检测'),
('im.sensitive.onBlock', 'REJECT', 'STRING', '敏感词拦截处理方式（REJECT拒绝 MASK掩码 WARN警告）'),
('im.export.requireApproval', 'true', 'BOOLEAN', '导出是否需要审批'),
('im.export.linkExpireMinutes', '60', 'NUMBER', '导出链接过期时间（分钟）'),
('im.security.corsAllowOrigins', '[]', 'JSON', 'CORS允许的源'),
('im.security.ipWhitelist', '[]', 'JSON', 'IP白名单');

-- ----------------------------
-- 初始化敏感词数据
-- ----------------------------
INSERT INTO `im_sensitive_word` (`word`, `level`, `category`) VALUES
('暴力', 'WARN', '违法违规'),
('色情', 'BLOCK', '违法违规'),
('赌博', 'BLOCK', '违法违规'),
('毒品', 'BLOCK', '违法违规'),
('反动', 'BLOCK', '政治敏感'),
('恐怖', 'BLOCK', '违法违规');