-- ============================================
-- DING消息表
-- 用于重要通知、强提醒功能
-- ============================================

DROP TABLE IF EXISTS `im_ding_message`;
DROP TABLE IF EXISTS `im_ding_receipt`;

-- DING消息表
CREATE TABLE `im_ding_message` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'DING ID',
  `sender_id` BIGINT(20) NOT NULL COMMENT '发送者ID',
  `content` TEXT NOT NULL COMMENT 'DING内容',
  `ding_type` VARCHAR(20) NOT NULL DEFAULT 'APP' COMMENT 'DING类型（APP应用 SMS短信 PHONE电话）',
  `is_urgent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否紧急（0否 1是）',
  `schedule_time` DATETIME NULL DEFAULT NULL COMMENT '定时发送时间（为空则立即发送）',
  `send_time` DATETIME NOT NULL COMMENT '实际发送时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态（DRAFT草稿 SENDING发送中 SENT已发送 CANCELLED已取消 FAILED失败）',
  `receipt_required` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否需要回执（0否 1是）',
  `total_count` INT(11) NOT NULL DEFAULT 0 COMMENT '总接收人数',
  `read_count` INT(11) NOT NULL DEFAULT 0 COMMENT '已读人数',
  `confirmed_count` INT(11) NOT NULL DEFAULT 0 COMMENT '已确认人数',
  `attachment` VARCHAR(500) NULL DEFAULT NULL COMMENT '附件URL',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_id` (`sender_id`) USING BTREE,
  INDEX `idx_send_time` (`send_time`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  INDEX `idx_schedule_time` (`schedule_time`) USING BTREE,
  CONSTRAINT `fk_ding_sender` FOREIGN KEY (`sender_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'DING消息表' ROW_FORMAT = DYNAMIC;

-- DING回执表
CREATE TABLE `im_ding_receipt` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '回执ID',
  `ding_id` BIGINT(20) NOT NULL COMMENT 'DING ID',
  `receiver_id` BIGINT(20) NOT NULL COMMENT '接收者ID',
  `read_time` DATETIME NULL DEFAULT NULL COMMENT '阅读时间',
  `confirmed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否确认（0未确认 1已确认）',
  `confirm_time` DATETIME NULL DEFAULT NULL COMMENT '确认时间',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ding_receiver` (`ding_id`, `receiver_id`) USING BTREE,
  INDEX `idx_receiver_id` (`receiver_id`) USING BTREE,
  INDEX `idx_read_time` (`read_time`) USING BTREE,
  CONSTRAINT `fk_receipt_ding` FOREIGN KEY (`ding_id`) REFERENCES `im_ding_message` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_receipt_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'DING回执表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- DING模板表
-- ============================================

CREATE TABLE `im_ding_template` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `content` TEXT NOT NULL COMMENT '模板内容',
  `category` VARCHAR(50) NOT NULL COMMENT '分类（MEETING会议 WORK工作 NOTICE通知 REMIND提醒）',
  `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统模板（0否 1是）',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE启用 DISABLED停用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category` (`category`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'DING模板表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 初始化DING模板数据
-- ============================================

INSERT INTO `im_ding_template` (`name`, `content`, `category`, `is_system`, `sort_order`) VALUES
('会议提醒', '【会议提醒】尊敬的{name}，您有一个会议将于{time}在{location}召开，请准时参加。', 'MEETING', 1, 1),
('工作安排', '【工作安排】{content}，请于{deadline}前完成。', 'WORK', 1, 2),
('放假通知', '【放假通知】根据国家法定节假日安排，{startDate}至{endDate}放假，共{days}天。', 'NOTICE', 1, 3),
('紧急通知', '【紧急通知】{content}，请立即处理！', 'REMIND', 1, 4),
('打卡提醒', '【打卡提醒】请记得完成上下班打卡。', 'REMIND', 1, 5),
('任务提醒', '【任务提醒】您有一个任务《{taskName}》将于{deadline}到期，请及时处理。', 'WORK', 1, 6),
('加班通知', '【加班通知】因{reason}，请于{time}到岗加班。', 'WORK', 1, 7),
('生日祝福', '【生日祝福】祝{name}生日快乐！', 'REMIND', 1, 8);

-- ============================================
-- 初始化测试DING消息（使用实际存在的用户ID）
-- ============================================

INSERT INTO `im_ding_message` (`sender_id`, `content`, `ding_type`, `is_urgent`, `send_time`, `status`, `total_count`, `read_count`, `confirmed_count`)
VALUES
  (2, '【会议提醒】今天下午3点在会议室A召开项目进度会议，请大家准时参加。', 'APP', 1, NOW(), 'SENT', 3, 2, 1),
  (2, '【工作安排】请各位在本周五前完成月度工作总结。', 'APP', 0, NOW(), 'SENT', 3, 1, 0);

-- 添加回执记录（使用实际存在的用户ID 3, 4, 5）
INSERT INTO `im_ding_receipt` (`ding_id`, `receiver_id`, `read_time`, `confirmed`, `confirm_time`)
VALUES
  (1, 3, NOW(), 1, NOW()),
  (1, 4, NOW(), 0, NULL),
  (2, 3, NULL, 0, NULL);

SELECT 'DING消息表创建完成！' AS message;
SELECT COUNT(*) AS ding_count FROM `im_ding_message`;
SELECT COUNT(*) AS template_count FROM `im_ding_template`;
