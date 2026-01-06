-- =============================================
-- 基于若依的Web版钉钉复刻系统 - 数据库表
-- =============================================

-- ----------------------------
-- 1. 审批相关表
-- ----------------------------

-- 审批实例表
CREATE TABLE `im_approval` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '审批ID',
  `template_id` BIGINT(20) NOT NULL COMMENT '模板ID',
  `title` VARCHAR(200) NOT NULL COMMENT '审批标题',
  `applicant_id` BIGINT(20) NOT NULL COMMENT '申请人ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待审批/APPROVED已通过/REJECTED已驳回/CANCELLED已取消',
  `current_node_id` BIGINT(20) NULL COMMENT '当前节点ID',
  `form_data` TEXT NULL COMMENT '表单数据(JSON)',
  `attachments` VARCHAR(1000) NULL COMMENT '附件列表(JSON)',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `finish_time` DATETIME NULL COMMENT '完成时间',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_applicant` (`applicant_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';

-- 审批模板表
CREATE TABLE `im_approval_template` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `code` VARCHAR(50) NOT NULL COMMENT '模板编码',
  `category` VARCHAR(50) NOT NULL COMMENT '分类：请假/报销/采购/通用',
  `description` VARCHAR(500) NULL COMMENT '模板描述',
  `form_schema` TEXT NOT NULL COMMENT '表单结构(JSON)',
  `flow_config` TEXT NOT NULL COMMENT '流程配置(JSON)',
  `icon` VARCHAR(200) NULL COMMENT '模板图标',
  `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统模板',
  `status` VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE启用/DISABLED停用',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_category` (`category`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板表';

-- 审批节点表
CREATE TABLE `im_approval_node` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `template_id` BIGINT(20) NOT NULL COMMENT '模板ID',
  `node_key` VARCHAR(50) NOT NULL COMMENT '节点标识',
  `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `node_type` VARCHAR(20) NOT NULL COMMENT '节点类型：APPROVE审批/CC抄送/CONDITION条件/PARALLEL并行',
  `approvers` TEXT NULL COMMENT '审批人配置(JSON)',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点表';

-- 审批记录表
CREATE TABLE `im_approval_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `approval_id` BIGINT(20) NOT NULL COMMENT '审批ID',
  `node_id` BIGINT(20) NOT NULL COMMENT '节点ID',
  `approver_id` BIGINT(20) NOT NULL COMMENT '审批人ID',
  `action` VARCHAR(20) NOT NULL COMMENT '操作：APPROVE通过/REJECT驳回/TRANSFER转审/ADD_SIGN加签',
  `comment` VARCHAR(500) NULL COMMENT '审批意见',
  `attachments` VARCHAR(1000) NULL COMMENT '附件(JSON)',
  `action_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  INDEX `idx_approval` (`approval_id`),
  INDEX `idx_approver` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 审批表单数据表
CREATE TABLE `im_approval_form_data` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `approval_id` BIGINT(20) NOT NULL COMMENT '审批ID',
  `field_key` VARCHAR(50) NOT NULL COMMENT '字段键',
  `field_value` TEXT NULL COMMENT '字段值',
  `field_type` VARCHAR(20) NOT NULL COMMENT '字段类型',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_approval` (`approval_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批表单数据表';

-- ----------------------------
-- 2. 工作台相关表
-- ----------------------------

-- 应用快捷方式表
CREATE TABLE `im_app_shortcut` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `app_id` BIGINT(20) NOT NULL COMMENT '应用ID',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `is_pinned` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否固定',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_app` (`user_id`, `app_id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用快捷方式表';

-- 待办事项表
CREATE TABLE `im_todo_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '待办标题',
  `description` VARCHAR(500) NULL COMMENT '描述',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：APPROVAL审批/MESSAGE消息/TASK任务',
  `related_id` BIGINT(20) NULL COMMENT '关联ID',
  `priority` VARCHAR(10) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：HIGH高/NORMAL普通/LOW低',
  `due_date` DATETIME NULL COMMENT '截止日期',
  `is_completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
  `completed_time` DATETIME NULL COMMENT '完成时间',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_status` (`is_completed`),
  INDEX `idx_due_date` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';

-- ----------------------------
-- 3. 应用中心相关表
-- ----------------------------

-- 应用表
CREATE TABLE `im_application` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '应用ID',
  `name` VARCHAR(100) NOT NULL COMMENT '应用名称',
  `code` VARCHAR(50) NOT NULL COMMENT '应用编码',
  `icon` VARCHAR(200) NULL COMMENT '应用图标',
  `category` VARCHAR(50) NOT NULL COMMENT '分类：OFFICE办公/DATA数据/TOOLS工具/CUSTOM自定义',
  `description` VARCHAR(500) NULL COMMENT '应用描述',
  `app_type` VARCHAR(20) NOT NULL COMMENT '类型：ROUTE路由/IFRAME嵌入/LINK外部链接',
  `app_url` VARCHAR(500) NOT NULL COMMENT '应用地址',
  `is_system` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统应用',
  `is_visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `permissions` VARCHAR(500) NULL COMMENT '所需权限(JSON)',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

-- ----------------------------
-- 4. 通知相关表
-- ----------------------------

-- 系统通知表
CREATE TABLE `im_system_notification` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `receiver_id` BIGINT(20) NOT NULL COMMENT '接收者ID',
  `type` VARCHAR(20) NOT NULL COMMENT '通知类型：SYSTEM系统/APPROVAL审批/MESSAGE消息/REMINDER提醒',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NULL COMMENT '通知内容',
  `related_id` BIGINT(20) NULL COMMENT '关联ID',
  `related_type` VARCHAR(50) NULL COMMENT '关联类型',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `read_time` DATETIME NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_receiver` (`receiver_id`),
  INDEX `idx_read` (`is_read`),
  INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- ----------------------------
-- 初始化系统数据
-- ----------------------------

-- 插入系统应用数据
INSERT INTO `im_application` (`name`, `code`, `icon`, `category`, `description`, `app_type`, `app_url`, `is_system`, `is_visible`, `sort_order`) VALUES
('审批', 'approval', 'approval', 'OFFICE', '审批中心，处理各类审批事项', 'ROUTE', '/im/approval', 1, 1, 1),
('工作台', 'workbench', 'workbench', 'OFFICE', '工作台，查看待办事项和应用', 'ROUTE', '/im/workbench', 1, 1, 2),
('通讯录', 'contacts', 'contacts', 'OFFICE', '企业通讯录', 'ROUTE', '/im/contacts', 1, 1, 3),
('应用中心', 'app-center', 'app-center', 'OFFICE', '应用管理中心', 'ROUTE', '/im/app-center', 1, 1, 4),
('文件管理', 'files', 'files', 'OFFICE', '文件管理中心', 'ROUTE', '/im/file', 1, 1, 5),
('设置', 'settings', 'settings', 'TOOLS', '系统设置', 'ROUTE', '/im/settings', 1, 1, 6);

-- 插入默认审批模板
INSERT INTO `im_approval_template` (`name`, `code`, `category`, `description`, `form_schema`, `flow_config`, `icon`, `is_system`, `sort_order`) VALUES
('请假申请', 'leave', '请假', '员工请假审批模板', '[{\"field\":\"leaveType\",\"label\":\"请假类型\",\"type\":\"select\",\"options\":{\"年假\":\"年假\",\"事假\":\"事假\",\"病假\":\"病假\"}},{\"field\":\"startDate\",\"label\":\"开始日期\",\"type\":\"date\"},{\"field\":\"endDate\",\"label\":\"结束日期\",\"type\":\"date\"},{\"field\":\"reason\",\"label\":\"请假原因\",\"type\":\"textarea\"}]', '[{\"nodeKey\":\"node1\",\"nodeName\":\"直属领导审批\",\"nodeType\":\"APPROVE\",\"approvers\":[\"leader\"]}]', 'leave', 1, 1),
('报销申请', 'expense', '报销', '费用报销审批模板', '[{\"field\":\"expenseType\",\"label\":\"报销类型\",\"type\":\"select\",\"options\":{\"差旅费\":\"差旅费\",\"交通费\":\"交通费\",\"办公用品\":\"办公用品\"}},{\"field\":\"amount\",\"label\":\"报销金额\",\"type\":\"number\"},{\"field\":\"description\",\"label\":\"费用说明\",\"type\":\"textarea\"}]', '[{\"nodeKey\":\"node1\",\"nodeName\":\"直属领导审批\",\"nodeType\":\"APPROVE\",\"approvers\":[\"leader\"]}]', 'expense', 1, 2),
('通用审批', 'general', '通用', '通用审批模板', '[{\"field\":\"title\",\"label\":\"标题\",\"type\":\"input\"},{\"field\":\"content\",\"label\":\"内容\",\"type\":\"textarea\"}]', '[{\"nodeKey\":\"node1\",\"nodeName\":\"审批人\",\"nodeType\":\"APPROVE\",\"approvers\":[\"approver\"]}]', 'general', 1, 3);
