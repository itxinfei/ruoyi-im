-- ============================================
-- 工作日志表
-- 用于员工提交日报、周报、月报
-- ============================================

DROP TABLE IF EXISTS `im_work_report`;
DROP TABLE IF EXISTS `im_work_report_comment`;
DROP TABLE IF EXISTS `im_work_report_like`;

-- 工作日志主表
CREATE TABLE `im_work_report` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '提交人ID',
  `report_type` VARCHAR(20) NOT NULL COMMENT '日志类型（DAILY日报 WEEKLY周报 MONTHLY月报）',
  `report_date` DATE NOT NULL COMMENT '报告日期',
  `work_content` TEXT NOT NULL COMMENT '工作内容',
  `completion_status` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT '完成状态（COMPLETED已完成 IN_PROGRESS进行中 PENDING待处理）',
  `tomorrow_plan` TEXT NULL DEFAULT NULL COMMENT '明日计划',
  `issues` TEXT NULL DEFAULT NULL COMMENT '遇到的问题',
  `attachments` VARCHAR(1000) NULL DEFAULT NULL COMMENT '附件（多个附件用逗号分隔）',
  `work_hours` DECIMAL(4,2) NULL DEFAULT NULL COMMENT '工作时长（小时）',
  `visibility` VARCHAR(20) NOT NULL DEFAULT 'DEPARTMENT' COMMENT '可见范围（PRIVATE私有 DEPARTMENT部门 PUBLIC公开）',
  `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态（DRAFT草稿 SUBMITTED已提交 APPROVED已审批 REJECTED已退回）',
  `approver_id` BIGINT(20) NULL DEFAULT NULL COMMENT '审批人ID',
  `approve_time` DATETIME NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '审批备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_report_type` (`report_type`) USING BTREE,
  INDEX `idx_report_date` (`report_date`) USING BTREE,
  INDEX `idx_submit_time` (`submit_time`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  CONSTRAINT `fk_work_report_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_work_report_approver` FOREIGN KEY (`approver_id`) REFERENCES `im_user` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工作日志表' ROW_FORMAT = DYNAMIC;

-- 工作日志评论表
CREATE TABLE `im_work_report_comment` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `report_id` BIGINT(20) NOT NULL COMMENT '日志ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '评论人ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `parent_id` BIGINT(20) NULL DEFAULT NULL COMMENT '父评论ID（回复）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_report_id` (`report_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_parent_id` (`parent_id`) USING BTREE,
  CONSTRAINT `fk_work_comment_report` FOREIGN KEY (`report_id`) REFERENCES `im_work_report` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_work_comment_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工作日志评论表' ROW_FORMAT = DYNAMIC;

-- 工作日志点赞表
CREATE TABLE `im_work_report_like` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `report_id` BIGINT(20) NOT NULL COMMENT '日志ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '点赞人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_report_user` (`report_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  CONSTRAINT `fk_work_like_report` FOREIGN KEY (`report_id`) REFERENCES `im_work_report` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_work_like_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工作日志点赞表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 初始化测试数据
-- ============================================

INSERT INTO `im_work_report` (`user_id`, `report_type`, `report_date`, `work_content`, `completion_status`, `tomorrow_plan`, `issues`, `work_hours`, `visibility`, `submit_time`)
VALUES
  (2, 'DAILY', CURDATE(), '1. 完成用户登录模块开发\n2. 修复消息推送bug\n3. 参加产品需求评审会议', 'COMPLETED', '1. 开始开发用户注册模块\n2. 优化消息推送性能', '无', 8.5, 'DEPARTMENT', NOW()),
  (3, 'DAILY', CURDATE(), '1. 完成前端页面优化\n2. 协助测试工程师进行功能测试', 'COMPLETED', '1. 继续前端优化工作\n2. 修复测试发现的问题', '接口响应偶尔较慢', 8.0, 'DEPARTMENT', NOW()),
  (4, 'WEEKLY', CURDATE(), '本周工作总结：\n1. 完成IM核心功能开发\n2. 完成数据库设计\n3. 完成接口文档编写', 'COMPLETED', '下周计划：\n1. 开始单元测试编写\n2. 进行代码审查', '无', 40.0, 'PUBLIC', NOW());

INSERT INTO `im_work_report_comment` (`report_id`, `user_id`, `content`)
VALUES
  (1, 3, '辛苦了，登录模块完成得不错！'),
  (1, 4, '消息推送问题后面可以一起优化'),
  (3, 2, '本周工作完成得很扎实，下周加油！');

INSERT INTO `im_work_report_like` (`report_id`, `user_id`)
VALUES
  (1, 3),
  (1, 4),
  (3, 2);

SELECT '工作日志表创建完成！' AS message;
SELECT COUNT(*) AS report_count FROM `im_work_report`;
SELECT COUNT(*) AS comment_count FROM `im_work_report_comment`;
SELECT COUNT(*) AS like_count FROM `im_work_report_like`;
