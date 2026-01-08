-- ============================================
-- 日程管理表
-- 用于个人和团队日程安排
-- ============================================

DROP TABLE IF EXISTS `im_schedule_event`;
DROP TABLE IF EXISTS `im_schedule_participant`;
DROP TABLE IF EXISTS `im_schedule_reminder`;

-- 日程事件主表
CREATE TABLE `im_schedule_event` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日程ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '创建人ID',
  `title` VARCHAR(200) NOT NULL COMMENT '日程标题',
  `description` TEXT NULL DEFAULT NULL COMMENT '日程描述',
  `location` VARCHAR(200) NULL DEFAULT NULL COMMENT '地点',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `is_all_day` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否全天（0否 1是）',
  `recurrence_type` VARCHAR(20) NULL DEFAULT NULL COMMENT '重复类型（NONE无 DAILY每天 WEEKLY每周 MONTHLY每月 CUSTOM自定义）',
  `recurrence_end_date` DATE NULL DEFAULT NULL COMMENT '重复结束日期',
  `recurrence_interval` INT(11) NULL DEFAULT NULL COMMENT '重复间隔（每N天/周/月）',
  `recurrence_days` VARCHAR(20) NULL DEFAULT NULL COMMENT '重复的星期几（1-7，逗号分隔）',
  `color` VARCHAR(20) NULL DEFAULT NULL COMMENT '显示颜色',
  `visibility` VARCHAR(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '可见范围（PRIVATE私有 DEPARTMENT部门 PUBLIC公开）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态（SCHEDULED已安排 CANCELLED已取消 COMPLETED已完成）',
  `reminder_minutes` INT(11) NULL DEFAULT NULL COMMENT '提醒时间（分钟，事件开始前多少分钟提醒）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_start_time` (`start_time`) USING BTREE,
  INDEX `idx_end_time` (`end_time`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  CONSTRAINT `fk_schedule_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '日程事件表' ROW_FORMAT = DYNAMIC;

-- 日程参与人表
CREATE TABLE `im_schedule_participant` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `event_id` BIGINT(20) NOT NULL COMMENT '日程ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '参与人ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '参与状态（PENDING待确认 ACCEPTED已接受 DECLINED已拒绝）',
  `response_time` DATETIME NULL DEFAULT NULL COMMENT '回复时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_event_user` (`event_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  CONSTRAINT `fk_participant_event` FOREIGN KEY (`event_id`) REFERENCES `im_schedule_event` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_participant_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '日程参与人表' ROW_FORMAT = DYNAMIC;

-- 日程提醒记录表
CREATE TABLE `im_schedule_reminder` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
  `event_id` BIGINT(20) NOT NULL COMMENT '日程ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `reminder_time` DATETIME NOT NULL COMMENT '提醒时间',
  `is_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送（0否 1是）',
  `sent_time` DATETIME NULL DEFAULT NULL COMMENT '发送时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_id` (`event_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_reminder_time` (`reminder_time`) USING BTREE,
  INDEX `idx_is_sent` (`is_sent`) USING BTREE,
  CONSTRAINT `fk_reminder_event` FOREIGN KEY (`event_id`) REFERENCES `im_schedule_event` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reminder_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '日程提醒记录表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 初始化测试数据
-- ============================================

-- 个人日程
INSERT INTO `im_schedule_event` (`user_id`, `title`, `description`, `location`, `start_time`, `end_time`, `is_all_day`, `reminder_minutes`)
VALUES
  (2, '项目评审会议', '评审本周完成的功能模块', '会议室A', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 2 HOUR), 0, 15),
  (2, '提交周报', '撰写本周工作总结', NULL, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 1 HOUR), 0, 30),
  (3, '客户拜访', '拜访重要客户A', '客户公司', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 3 HOUR), 0, 60);

-- 团队日程（包含参与人）
INSERT INTO `im_schedule_event` (`user_id`, `title`, `description`, `location`, `start_time`, `end_time`, `is_all_day`, `visibility`, `reminder_minutes`)
VALUES
  (2, '团队周会', '全体成员参加的周例会', '大会议室', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 1 HOUR), 0, 'DEPARTMENT', 30);

-- 添加参与人
INSERT INTO `im_schedule_participant` (`event_id`, `user_id`, `status`, `response_time`)
VALUES
  (4, 3, 'ACCEPTED', NOW()),
  (4, 4, 'PENDING', NULL),
  (4, 5, 'ACCEPTED', NOW());

SELECT '日程管理表创建完成！' AS message;
SELECT COUNT(*) AS event_count FROM `im_schedule_event`;
SELECT COUNT(*) AS participant_count FROM `im_schedule_participant`;
