-- 分片上传记录表
CREATE TABLE IF NOT EXISTS `im_file_chunk_upload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `upload_id` varchar(64) NOT NULL COMMENT '上传ID（唯一标识一次上传任务）',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
  `file_md5` varchar(64) DEFAULT NULL COMMENT '文件MD5值',
  `chunk_size` int(11) NOT NULL COMMENT '分片大小（字节）',
  `total_chunks` int(11) NOT NULL COMMENT '总分片数',
  `uploaded_chunks` int(11) NOT NULL DEFAULT '0' COMMENT '已上传分片数',
  `status` varchar(20) NOT NULL DEFAULT 'UPLOADING' COMMENT '状态：UPLOADING上传中、PAUSED已暂停、COMPLETED已完成、CANCELLED已取消',
  `user_id` bigint(20) NOT NULL COMMENT '上传用户ID',
  `file_path` varchar(500) DEFAULT NULL COMMENT '最终文件路径',
  `file_url` varchar(500) DEFAULT NULL COMMENT '最终文件URL',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_id` (`upload_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_md5` (`file_md5`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传记录表';

-- 分片文件详情表
CREATE TABLE IF NOT EXISTS `im_file_chunk_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `upload_id` varchar(64) NOT NULL COMMENT '上传ID',
  `chunk_number` int(11) NOT NULL COMMENT '分片序号（从1开始）',
  `chunk_size` int(11) NOT NULL COMMENT '分片大小（字节）',
  `chunk_md5` varchar(64) DEFAULT NULL COMMENT '分片MD5值',
  `chunk_path` varchar(500) DEFAULT NULL COMMENT '分片文件存储路径',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待上传、UPLOADING上传中、COMPLETED已完成',
  `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_chunk` (`upload_id`, `chunk_number`),
  KEY `idx_upload_id` (`upload_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片文件详情表';
