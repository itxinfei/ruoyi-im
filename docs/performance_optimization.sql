-- =============================================
-- 性能优化 - 数据库索引优化
-- =============================================

-- =============================================
-- 消息表索引优化
-- =============================================
-- 查询会话消息的索引
ALTER TABLE `im_message` ADD INDEX `idx_conversation_time` (`conversation_id`, `send_time` DESC);

-- 查询用户消息的索引
ALTER TABLE `im_message` ADD INDEX `idx_sender_time` (`sender_id`, `send_time` DESC);

-- 全文搜索索引（MySQL 5.7+）
ALTER TABLE `im_message` ADD FULLTEXT INDEX `ft_content` (`content`);

-- =============================================
-- 群组表索引优化
-- =============================================
-- 查询用户群的索引
ALTER TABLE `im_group_member` ADD INDEX `idx_user_group` (`user_id`, `group_id`);

-- =============================================
-- 会话表索引优化
-- =============================================
-- 查询用户会话的索引
ALTER TABLE `im_conversation` ADD INDEX `idx_user_type` (`user_id`, `conversation_type`);

-- =============================================
-- 审批表索引优化
-- =============================================
-- 查询待审批的索引
ALTER TABLE `im_approval_node` ADD INDEX `idx_approver_status` (`approver_ids`(100), `status`);

-- 查询审批进度的索引
ALTER TABLE `im_approval` ADD INDEX `idx_status_time` (`status`, `create_time` DESC);

-- =============================================
-- 文件表索引优化
-- =============================================
-- 按类型和上传者查询的索引
ALTER TABLE `im_file_asset` ADD INDEX `idx_type_uploader` (`file_type`, `uploader_id`);

-- =============================================
-- 审计日志表分区优化（可选）
-- =============================================
-- 按月分区（需要先删除主键再重建）
-- ALTER TABLE `im_audit_log` PARTITION BY RANGE (TO_DAYS(operation_time)) (
--     PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
--     PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')),
--     PARTITION p202403 VALUES LESS THAN (TO_DAYS('2024-04-01')),
--     PARTITION pmax VALUES LESS THAN MAXVALUE
-- );

-- =============================================
-- 查询优化建议
-- =============================================
-- 1. 对大表使用LIMIT分页
-- 2. 避免SELECT *，只查询需要的字段
-- 3. 使用覆盖索引减少回表查询
-- 4. 对JOIN操作确保关联字段有索引
-- 5. 对排序字段使用索引
-- 6. 定期ANALYZE TABLE更新统计信息
-- 7. 定期OPTIMIZE TABLE优化表空间
