-- =============================================
-- 考勤打卡相关表
-- =============================================

-- 考勤打卡表
DROP TABLE IF EXISTS `im_attendance`;
CREATE TABLE `im_attendance` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `attendance_date` date NOT NULL COMMENT '打卡日期',
    `check_in_time` datetime DEFAULT NULL COMMENT '上班打卡时间',
    `check_out_time` datetime DEFAULT NULL COMMENT '下班打卡时间',
    `check_in_status` varchar(20) DEFAULT 'NORMAL' COMMENT '上班打卡状态（NORMAL正常 LATE迟到 ABSENT缺卡）',
    `check_out_status` varchar(20) DEFAULT 'NORMAL' COMMENT '下班打卡状态（NORMAL正常 EARLY早退 ABSENT缺卡）',
    `work_minutes` int DEFAULT NULL COMMENT '工作时长（分钟）',
    `attendance_type` varchar(20) DEFAULT 'WORK' COMMENT '打卡类型（WORK工作日 OVERTIME加班 WEEKEND周末）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `check_in_location` varchar(200) DEFAULT NULL COMMENT '上班打卡位置（JSON格式）',
    `check_out_location` varchar(200) DEFAULT NULL COMMENT '下班打卡位置（JSON格式）',
    `device_info` varchar(200) DEFAULT NULL COMMENT '设备信息',
    `approve_status` varchar(20) DEFAULT NULL COMMENT '审批状态（PENDING待审批 APPROVED已通过 REJECTED已拒绝）',
    `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
    `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
    `approve_comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `attendance_date`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_attendance_date` (`attendance_date`),
    KEY `idx_approve_status` (`approve_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤打卡表';

-- 考勤统计表
DROP TABLE IF EXISTS `im_attendance_statistics`;
CREATE TABLE `im_attendance_statistics` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `statistics_month` char(7) NOT NULL COMMENT '统计年月（yyyy-MM）',
    `attendance_days` int DEFAULT 0 COMMENT '出勤天数',
    `leave_days` int DEFAULT 0 COMMENT '请假天数',
    `overtime_days` int DEFAULT 0 COMMENT '加班天数',
    `late_count` int DEFAULT 0 COMMENT '迟到次数',
    `early_count` int DEFAULT 0 COMMENT '早退次数',
    `absent_count` int DEFAULT 0 COMMENT '缺卡次数',
    `actual_work_hours` decimal(10,2) DEFAULT 0.00 COMMENT '实际工作时长（小时）',
    `standard_work_hours` decimal(10,2) DEFAULT 0.00 COMMENT '标准工作时长（小时）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_month` (`user_id`, `statistics_month`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_statistics_month` (`statistics_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤统计表';
