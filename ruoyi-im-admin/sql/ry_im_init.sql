-- ===========================
-- IM系统初始化脚本
-- ===========================
-- 执行前请确保已执行ry_20240601.sql（若依基础表）

USE `im`;

-- ----------------------------
-- 1. 好友关系表
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
-- 2. 好友申请表
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
-- 3. 群组表
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
-- 4. 群组成员表
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
-- 5. 会话表
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
-- 6. 会话成员表
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation_member`;
CREATE TABLE `im_conversation_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `unread_count` int(11) NOT NULL DEFAULT '0' COMMENT '未读消息数',
  `last_read_message_id` bigint(20) DEFAULT NULL COMMENT '最后阅读消息ID',
  `is_pinned` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶',
  `is_muted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否免打扰',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_user` (`conversation_id`,`user_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='会话成员表';

-- ----------------------------
-- 7. 消息表
-- ----------------------------
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message` (
  `id` bigint(20) NOT NULL COMMENT '消息ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者用户ID',
  `type` varchar(20) NOT NULL COMMENT '消息类型（TEXT文本 FILE文件 NOTICE通知 RECALL撤回 REPLY回复 FORWARD转发 IMAGE图片 VOICE语音）',
  `content` text COMMENT '消息内容（JSON格式）',
  `reply_to_message_id` bigint(20) DEFAULT NULL COMMENT '回复的消息ID',
  `forward_from_message_id` bigint(20) DEFAULT NULL COMMENT '转发的消息ID',
  `status` varchar(20) NOT NULL DEFAULT 'SENT' COMMENT '消息状态（SENT已发送 DELIVERED已送达 READ已读 REVOKED已撤回）',
  `sensitive_level` varchar(10) NOT NULL DEFAULT 'NONE' COMMENT '敏感级别（NONE无 WARN警告 BLOCK拦截）',
  `revoked_time` datetime DEFAULT NULL COMMENT '撤回时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_reply_to_message_id` (`reply_to_message_id`),
  KEY `idx_forward_from_message_id` (`forward_from_message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ----------------------------
-- 8. 文件资源表
-- ----------------------------
DROP TABLE IF EXISTS `im_file_asset`;
CREATE TABLE `im_file_asset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `file_name` varchar(200) NOT NULL COMMENT '文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) NOT NULL COMMENT '文件类型',
  `file_ext` varchar(20) NOT NULL COMMENT '文件扩展名',
  `md5` varchar(32) DEFAULT NULL COMMENT '文件MD5值',
  `uploader_id` bigint(20) NOT NULL COMMENT '上传者用户ID',
  `download_count` int(11) NOT NULL DEFAULT '0' COMMENT '下载次数',
  `download_expire_time` datetime DEFAULT NULL COMMENT '下载链接过期时间',
  `status` varchar(10) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE正常 DELETED已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_md5` (`md5`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='文件资源表';

-- ----------------------------
-- 9. 敏感词表
-- ----------------------------
DROP TABLE IF EXISTS `im_sensitive_word`;
CREATE TABLE `im_sensitive_word` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `word` varchar(100) NOT NULL COMMENT '敏感词',
  `level` varchar(10) NOT NULL DEFAULT 'WARN' COMMENT '级别（WARN警告 BLOCK拦截）',
  `status` varchar(10) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE启用 DISABLED停用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- ----------------------------
-- 10. 敏感事件表
-- ----------------------------
DROP TABLE IF EXISTS `im_sensitive_event`;
CREATE TABLE `im_sensitive_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `conversation_id` bigint(20) DEFAULT NULL COMMENT '会话ID',
  `message_id` bigint(20) DEFAULT NULL COMMENT '消息ID',
  `detected_word` varchar(100) NOT NULL COMMENT '检测到的敏感词',
  `action` varchar(20) NOT NULL COMMENT '处理动作（REJECTED拒绝 MASKED屏蔽 WARNED警告）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='敏感事件表';

-- ----------------------------
-- 11. 系统配置表
-- ----------------------------
DROP TABLE IF EXISTS `im_system_config`;
CREATE TABLE `im_system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) NOT NULL COMMENT '配置值',
  `config_type` varchar(20) NOT NULL DEFAULT 'STRING' COMMENT '配置类型（STRING字符串 NUMBER数字 BOOLEAN布尔）',
  `description` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `status` varchar(10) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE启用 DISABLED停用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ----------------------------
-- 12. 会话分组表
-- ----------------------------
DROP TABLE IF EXISTS `im_session_group`;
CREATE TABLE `im_session_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `group_name` varchar(50) NOT NULL COMMENT '分组名称',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='会话分组表';

-- ----------------------------
-- 13. 审计导出申请表
-- ----------------------------
DROP TABLE IF EXISTS `im_audit_export_request`;
CREATE TABLE `im_audit_export_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '申请人用户ID',
  `export_type` varchar(20) NOT NULL COMMENT '导出类型（MESSAGE消息 FILE文件 USER用户）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `reason` varchar(500) NOT NULL COMMENT '申请理由',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING待审批 APPROVED已同意 REJECTED已拒绝 EXPIRED已过期）',
  `approver_id` bigint(20) DEFAULT NULL COMMENT '审批人用户ID',
  `approved_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approval_remark` varchar(500) DEFAULT NULL COMMENT '审批备注',
  `export_file_path` varchar(500) DEFAULT NULL COMMENT '导出文件路径',
  `download_count` int(11) NOT NULL DEFAULT '0' COMMENT '下载次数',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COMMENT='审计导出申请表';

-- ----------------------------
-- 插入初始数据
-- ----------------------------

-- 插入默认系统配置
INSERT INTO `im_system_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('message.recall.window.seconds', '120', 'NUMBER', '消息撤回时间窗口（秒）'),
('message.max.length', '2000', 'NUMBER', '消息最大长度'),
('file.max.size.mb', '20', 'NUMBER', '文件最大大小（MB）'),
('file.download.expire.minutes', '60', 'NUMBER', '文件下载链接过期时间（分钟）'),
('sensitive.enabled', 'true', 'BOOLEAN', '是否启用敏感词检测'),
('websocket.heartbeat.interval', '30', 'NUMBER', 'WebSocket心跳间隔（秒）');

-- 插入默认敏感词
INSERT INTO `im_sensitive_word` (`word`, `level`, `description`) VALUES
('敏感词示例', 'WARN', '测试用敏感词');

COMMIT;
