-- ============================================
-- 企业级Web钉钉数据库优化方案
-- 目标：支持500人并发使用
-- 版本：1.0
-- 日期：2026-01-08
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;

-- ============================================
-- 第一部分：索引优化
-- ============================================

-- 1. 消息表索引优化
-- 添加消息查询和分页的联合索引
ALTER TABLE `im_message`
ADD INDEX IF NOT EXISTS `idx_conversation_time` (`conversation_id`, `create_time` DESC) COMMENT '会话消息分页查询',
ADD INDEX IF NOT EXISTS `idx_sender_time` (`sender_id`, `create_time` DESC) COMMENT '用户消息历史查询',
ADD INDEX IF NOT EXISTS `idx_create_time` (`create_time` DESC) COMMENT '按时间查询消息',
ADD INDEX IF NOT EXISTS `idx_message_type` (`message_type`) COMMENT '按消息类型查询';

-- 2. 会话成员表索引优化
-- 添加用户会话列表查询的联合索引
ALTER TABLE `im_conversation_member`
ADD INDEX IF NOT EXISTS `idx_user_pinned_time` (`user_id`, `is_pinned` DESC, `update_time` DESC) COMMENT '用户会话列表查询（置顶优先）',
ADD INDEX IF NOT EXISTS `idx_user_unread` (`user_id`, `unread_count`) COMMENT '用户未读消息查询';

-- 3. 会话表索引优化
ALTER TABLE `im_conversation`
ADD INDEX IF NOT EXISTS `idx_last_message_time` (`last_message_time` DESC) COMMENT '会话按最后消息时间排序',
ADD INDEX IF NOT EXISTS `idx_type_target` (`type`, `target_id`) COMMENT '会话类型和目标ID查询';

-- 4. 群组成员表索引优化
ALTER TABLE `im_group_member`
ADD INDEX IF NOT EXISTS `idx_group_user` (`group_id`, `user_id`) COMMENT '群组成员查询',
ADD INDEX IF NOT EXISTS `idx_user_role` (`user_id`, `role`) COMMENT '用户群组角色查询';

-- 5. 用户表索引优化
ALTER TABLE `im_user`
ADD INDEX IF NOT EXISTS `idx_status_online` (`status`, `last_online_time`) COMMENT '在线用户查询';

-- ============================================
-- 第二部分：表结构优化
-- ============================================

-- 1. 消息表字段优化
-- 添加消息序号字段，支持游标分页
ALTER TABLE `im_message`
ADD COLUMN IF NOT EXISTS `seq_no` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '消息序号（用于游标分页）' AFTER `id`,
ADD INDEX IF NOT EXISTS `idx_seq_no` (`seq_no`);

-- 2. 会话成员表字段优化
ALTER TABLE `im_conversation_member`
ADD COLUMN IF NOT EXISTS `mute_until` DATETIME NULL DEFAULT NULL COMMENT '免打扰截止时间（NULL表示永久免打扰）' AFTER `is_muted`,
ADD COLUMN IF NOT EXISTS `last_active_time` DATETIME NULL DEFAULT NULL COMMENT '最后活跃时间' AFTER `update_time`,
ADD INDEX IF NOT EXISTS `idx_last_active_time` (`last_active_time` DESC);

-- 3. 用户表安全字段增强
ALTER TABLE `im_user`
ADD COLUMN IF NOT EXISTS `login_failed_count` INT(11) NOT NULL DEFAULT 0 COMMENT '登录失败次数' AFTER `last_online_time`,
ADD COLUMN IF NOT EXISTS `locked_until` DATETIME NULL DEFAULT NULL COMMENT '账户锁定截止时间' AFTER `login_failed_count`,
ADD COLUMN IF NOT EXISTS `password_update_time` DATETIME NULL DEFAULT NULL COMMENT '密码最后更新时间' AFTER `locked_until`,
ADD INDEX IF NOT EXISTS `idx_locked_until` (`locked_until`);

-- 4. 审计表扩展（如果使用简化版审计表，需要扩展）
-- 检查im_audit_log表是否需要添加更多字段
ALTER TABLE `im_audit_log`
ADD COLUMN IF NOT EXISTS `request_params` TEXT NULL DEFAULT NULL COMMENT '请求参数（JSON）' AFTER `description`,
ADD COLUMN IF NOT EXISTS `response_data` TEXT NULL DEFAULT NULL COMMENT '响应数据' AFTER `request_params`,
ADD COLUMN IF NOT EXISTS `execution_time` BIGINT(20) NULL DEFAULT NULL COMMENT '执行时长（毫秒）' AFTER `response_data`;

-- ============================================
-- 第三部分：分区表设计（针对大数据量表）
-- ============================================

-- 消息表按月分区（适用于大量消息场景）
-- 注意：需要先删除主键中的自增，再添加分区
-- ALTER TABLE `im_message` DROP PRIMARY KEY;
-- ALTER TABLE `im_message` ADD PRIMARY KEY (`id`, `create_time`);
-- ALTER TABLE `im_message` PARTITION BY RANGE (YEAR(create_time) * 100 + MONTH(create_time)) (
--     PARTITION p202401 VALUES LESS THAN (202402),
--     PARTITION p202402 VALUES LESS THAN (202403),
--     PARTITION p202403 VALUES LESS THAN (202404),
--     PARTITION p202404 VALUES LESS THAN (202405),
--     PARTITION p202405 VALUES LESS THAN (202406),
--     PARTITION p202406 VALUES LESS THAN (202407),
--     PARTITION p202407 VALUES LESS THAN (202408),
--     PARTITION p202408 VALUES LESS THAN (202409),
--     PARTITION p202409 VALUES LESS THAN (202410),
--     PARTITION p202410 VALUES LESS THAN (202411),
--     PARTITION p202411 VALUES LESS THAN (202412),
--     PARTITION p202412 VALUES LESS THAN (202501),
--     PARTITION pmax VALUES LESS THAN MAXVALUE
-- );

-- 审计日志表按月分区
-- ALTER TABLE `im_audit_log` PARTITION BY RANGE (YEAR(operation_time) * 100 + MONTH(operation_time)) (
--     PARTITION p202401 VALUES LESS THAN (202402),
--     PARTITION p202402 VALUES LESS THAN (202403),
--     PARTITION p202403 VALUES LESS THAN (202404),
--     PARTITION p202404 VALUES LESS THAN (202405),
--     PARTITION p202405 VALUES LESS THAN (202406),
--     PARTITION p202406 VALUES LESS THAN (202407),
--     PARTITION p202407 VALUES LESS THAN (202408),
--     PARTITION p202408 VALUES LESS THAN (202409),
--     PARTITION p202409 VALUES LESS THAN (202410),
--     PARTITION p202410 VALUES LESS THAN (202411),
--     PARTITION p202411 VALUES LESS THAN (202412),
--     PARTITION p202412 VALUES LESS THAN (202501),
--     PARTITION pmax VALUES LESS THAN MAXVALUE
-- );

-- ============================================
-- 第四部分：创建性能优化视图
-- ============================================

-- 用户会话列表视图（优化查询性能）
CREATE OR REPLACE VIEW `v_user_conversation_list` AS
SELECT
    cm.id,
    cm.conversation_id,
    cm.user_id,
    cm.unread_count,
    cm.is_pinned,
    cm.is_muted,
    cm.mute_until,
    cm.last_active_time,
    c.type,
    c.target_id,
    c.name,
    c.avatar,
    c.last_message_id,
    c.last_message_time,
    m.content AS last_message_content,
    m.message_type AS last_message_type,
    m.sender_id AS last_message_sender_id,
    u.nickname AS last_message_sender_name,
    CASE WHEN m.sender_id = cm.user_id THEN 1 ELSE 0 END AS is_own_message
FROM `im_conversation_member` cm
INNER JOIN `im_conversation` c ON cm.conversation_id = c.id AND c.is_deleted = 0
LEFT JOIN `im_message` m ON c.last_message_id = m.id
LEFT JOIN `im_user` u ON m.sender_id = u.id
WHERE cm.is_deleted = 0
ORDER BY cm.is_pinned DESC, c.last_message_time DESC;

-- 用户会话未读统计视图
CREATE OR REPLACE VIEW `v_user_unread_stats` AS
SELECT
    user_id,
    COUNT(*) AS total_unread_conversations,
    SUM(unread_count) AS total_unread_messages
FROM `im_conversation_member`
WHERE is_deleted = 0 AND unread_count > 0
GROUP BY user_id;

-- 群组信息扩展视图（包含成员统计）
CREATE OR REPLACE VIEW `v_group_info` AS
SELECT
    g.*,
    u.nickname AS owner_name,
    u.avatar AS owner_avatar,
    COUNT(gm.id) AS member_count,
    SUM(CASE WHEN gm.is_muted = 1 THEN 1 ELSE 0 END) AS muted_count
FROM `im_group` g
LEFT JOIN `im_user` u ON g.owner_id = u.id
LEFT JOIN `im_group_member` gm ON g.id = gm.group_id AND gm.is_deleted = 0
WHERE g.is_deleted = 0
GROUP BY g.id;

-- ============================================
-- 第五部分：创建存储过程优化性能
-- ============================================

-- 批量更新未读消息数的存储过程
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS `sp_update_unread_count`(
    IN p_user_id BIGINT,
    IN p_conversation_id BIGINT
)
BEGIN
    DECLARE v_last_read_id BIGINT;
    DECLARE v_unread_count INT DEFAULT 0;

    -- 获取最后已读消息ID
    SELECT `last_read_message_id` INTO v_last_read_id
    FROM `im_conversation_member`
    WHERE user_id = p_user_id AND conversation_id = p_conversation_id;

    -- 计算未读消息数
    IF v_last_read_id IS NOT NULL THEN
        SELECT COUNT(*) INTO v_unread_count
        FROM `im_message`
        WHERE conversation_id = p_conversation_id
          AND id > v_last_read_id
          AND sender_id != p_user_id
          AND is_revoked = 0;
    ELSE
        SELECT COUNT(*) INTO v_unread_count
        FROM `im_message`
        WHERE conversation_id = p_conversation_id
          AND sender_id != p_user_id
          AND is_revoked = 0;
    END IF;

    -- 更新未读数
    UPDATE `im_conversation_member`
    SET unread_count = v_unread_count,
        update_time = NOW()
    WHERE user_id = p_user_id AND conversation_id = p_conversation_id;

END$$
DELIMITER ;

-- 批量更新会话最后消息的存储过程
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS `sp_update_conversation_last_message`(
    IN p_conversation_id BIGINT
)
BEGIN
    DECLARE v_last_message_id BIGINT;
    DECLARE v_last_message_time DATETIME;

    -- 获取最后一条消息
    SELECT id, create_time INTO v_last_message_id, v_last_message_time
    FROM `im_message`
    WHERE conversation_id = p_conversation_id
    ORDER BY create_time DESC
    LIMIT 1;

    -- 更新会话最后消息
    IF v_last_message_id IS NOT NULL THEN
        UPDATE `im_conversation`
        SET last_message_id = v_last_message_id,
            last_message_time = v_last_message_time,
            update_time = NOW()
        WHERE id = p_conversation_id;
    END IF;

END$$
DELIMITER ;

-- 清理过期数据的存储过程
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS `sp_cleanup_expired_data`()
BEGIN
    DECLARE DECLARE v_retention_days INT DEFAULT 90;

    -- 删除90天前的已撤回消息
    DELETE FROM `im_message_edit_history`
    WHERE edit_time < DATE_SUB(NOW(), INTERVAL v_retention_days DAY);

    -- 删除90天前的审计日志
    DELETE FROM `im_audit_log`
    WHERE operation_time < DATE_SUB(NOW(), INTERVAL v_retention_days DAY);

    -- 删除过期的分片上传记录
    DELETE FROM `im_file_chunk_upload`
    WHERE expire_time < NOW();

    -- 删除过期的分片详情记录
    DELETE FROM `im_file_chunk_detail`
    WHERE upload_id IN (SELECT upload_id FROM `im_file_chunk_upload` WHERE expire_time < NOW());

    SELECT CONCAT('清理完成，删除了 ', ROW_COUNT(), ' 条记录') AS result;

END$$
DELIMITER ;

-- ============================================
-- 第六部分：创建定时任务（需配合Quartz或类似框架）
-- ============================================

-- 创建定时任务配置表（如果不存在）
CREATE TABLE IF NOT EXISTS `im_scheduled_task` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `task_group` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组',
  `cron_expression` VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
  `task_class` VARCHAR(200) NOT NULL COMMENT '任务类',
  `description` VARCHAR(500) NULL DEFAULT NULL COMMENT '任务描述',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_name_group` (`task_name`, `task_group`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '定时任务配置表';

-- 插入默认定时任务
INSERT INTO `im_scheduled_task` (`task_name`, `task_group`, `cron_expression`, `task_class`, `description`)
VALUES
('清理过期数据', 'CLEANUP', '0 0 2 * * ?', 'CleanupExpiredDataJob', '每天凌晨2点执行数据清理'),
('更新未读统计', 'STATS', '0 */5 * * * ?', 'UpdateUnreadStatsJob', '每5分钟更新未读统计'),
('消息序号生成', 'MESSAGE', '0 */10 * * * ?', 'MessageSeqNoJob', '每10分钟生成消息序号')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ============================================
-- 第七部分：数据备份策略表
-- ============================================

-- 备份记录表
CREATE TABLE IF NOT EXISTS `im_backup_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '备份ID',
  `backup_type` VARCHAR(20) NOT NULL COMMENT '备份类型：FULL=全量, INCREMENTAL=增量',
  `backup_file` VARCHAR(500) NOT NULL COMMENT '备份文件路径',
  `backup_size` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '备份文件大小（字节）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS=成功, FAILED=失败',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NULL DEFAULT NULL COMMENT '结束时间',
  `error_message` VARCHAR(1000) NULL DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_backup_type` (`backup_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_start_time` (`start_time`)
) ENGINE = InnoDB COMMENT = '数据备份记录表';

-- ============================================
-- 第八部分：敏感数据加密配置
-- ============================================

-- 创建加密密钥配置表
CREATE TABLE IF NOT EXISTS `im_encryption_key` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '密钥ID',
  `key_name` VARCHAR(50) NOT NULL COMMENT '密钥名称',
  `key_value` VARCHAR(500) NOT NULL COMMENT '密钥值（AES-256）',
  `key_type` VARCHAR(20) NOT NULL DEFAULT 'AES' COMMENT '加密类型',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key_name` (`key_name`)
) ENGINE = InnoDB COMMENT = '加密密钥配置表';

-- ============================================
-- 第九部分：性能监控表
-- ============================================

-- SQL性能监控表
CREATE TABLE IF NOT EXISTS `im_sql_performance_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `sql_text` TEXT NOT NULL COMMENT 'SQL语句',
  `execution_time` BIGINT(20) NOT NULL COMMENT '执行时长（毫秒）',
  `rows_affected` INT(11) NULL DEFAULT NULL COMMENT '影响行数',
  `user_id` BIGINT(20) NULL DEFAULT NULL COMMENT '执行用户ID',
  `request_uri` VARCHAR(500) NULL DEFAULT NULL COMMENT '请求URI',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_execution_time` (`execution_time`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE = InnoDB COMMENT = 'SQL性能监控日志表';

-- 慢查询阈值配置表
CREATE TABLE IF NOT EXISTS `im_slow_query_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `table_name` VARCHAR(100) NOT NULL COMMENT '表名',
  `threshold_ms` INT(11) NOT NULL DEFAULT 1000 COMMENT '慢查询阈值（毫秒）',
  `alert_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用告警',
  `description` VARCHAR(500) NULL DEFAULT NULL COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_name` (`table_name`)
) ENGINE = InnoDB COMMENT = '慢查询阈值配置表';

-- 插入默认慢查询配置
INSERT INTO `im_slow_query_config` (`table_name`, `threshold_ms`, `description`)
VALUES
('im_message', 500, '消息表慢查询阈值500ms'),
('im_conversation_member', 300, '会话成员表慢查询阈值300ms'),
('im_group_member', 300, '群组成员表慢查询阈值300ms'),
('im_user', 200, '用户表慢查询阈值200ms')
ON DUPLICATE KEY UPDATE threshold_ms = VALUES(threshold_ms);

-- ============================================
-- 第十部分：执行完成报告
-- ============================================

SELECT '============================================' AS '';
SELECT '企业级Web钉钉数据库优化完成！' AS message;
SELECT '============================================' AS '';
SELECT '已完成的优化项目：' AS '';
SELECT '1. 添加了15+个性能优化索引' AS item;
SELECT '2. 创建了3个性能优化视图' AS item;
SELECT '3. 创建了3个性能优化存储过程' AS item;
SELECT '4. 创建了定时任务配置表' AS item;
SELECT '5. 创建了备份记录表' AS item;
SELECT '6. 创建了加密密钥配置表' AS item;
SELECT '7. 创建了性能监控表' AS item;
SELECT '============================================' AS '';

-- 显示索引创建结果
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('im_message', 'im_conversation', 'im_conversation_member', 'im_group_member', 'im_user')
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
