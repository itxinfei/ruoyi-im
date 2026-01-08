-- ----------------------------
-- Table structure for im_backup
-- 数据备份记录表
-- ----------------------------
DROP TABLE IF EXISTS `im_backup`;
CREATE TABLE `im_backup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '备份ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备份文件名',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备份描述',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备份文件路径',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '备份文件大小（字节）',
  `backup_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'full' COMMENT '备份类型：full=全量, incremental=增量, user=用户数据',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '备份状态：pending=待执行, in_progress=进行中, completed=已完成, failed=失败',
  `creator_id` bigint(20) NULL DEFAULT NULL COMMENT '备份人ID',
  `creator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备份人名称',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息（失败时记录）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_backup_type`(`backup_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据备份记录表' ROW_FORMAT = DYNAMIC;
