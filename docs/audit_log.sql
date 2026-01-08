-- =============================================
-- 操作审计日志相关表
-- =============================================

-- 操作审计日志表
DROP TABLE IF EXISTS `im_audit_log`;
CREATE TABLE `im_audit_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
    `user_name` varchar(100) DEFAULT NULL COMMENT '操作用户名',
    `module` varchar(50) DEFAULT NULL COMMENT '操作模块（LOGIN登录、MESSAGE消息、GROUP群组、USER用户、FILE文件、APPROVAL审批等）',
    `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型（CREATE新增、UPDATE修改、DELETE删除、QUERY查询、EXPORT导出、IMPORT导入等）',
    `description` varchar(500) DEFAULT NULL COMMENT '操作描述',
    `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法（GET、POST、PUT、DELETE等）',
    `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
    `request_params` text COMMENT '请求参数',
    `response_data` text COMMENT '响应结果',
    `status` varchar(20) DEFAULT NULL COMMENT '操作状态（SUCCESS成功、FAILURE失败）',
    `error_msg` varchar(1000) DEFAULT NULL COMMENT '错误信息',
    `execution_time` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
    `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
    `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
    `operation_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_status` (`status`),
    KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';
