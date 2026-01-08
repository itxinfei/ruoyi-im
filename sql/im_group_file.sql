-- 群组文件表
CREATE TABLE IF NOT EXISTS `im_group_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `group_id` bigint(20) NOT NULL COMMENT '群组ID',
  `file_id` bigint(20) NOT NULL COMMENT '文件资产ID（关联im_file_asset）',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `file_type` varchar(50) NOT NULL COMMENT '文件类型：image/video/audio/document/other',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
  `category` varchar(50) DEFAULT 'default' COMMENT '文件分类：default/document/image/video/audio',
  `permission` varchar(20) NOT NULL DEFAULT 'ALL' COMMENT '下载权限：ALL=所有人, ADMIN=仅管理员',
  `uploader_id` bigint(20) NOT NULL COMMENT '上传者ID',
  `uploader_name` varchar(100) DEFAULT NULL COMMENT '上传者名称',
  `download_count` int(11) NOT NULL DEFAULT '0' COMMENT '下载次数',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态：1=正常, 0=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组文件表';
