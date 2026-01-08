/*
 Navicat Premium Dump SQL

 Source Server         : 172.168.20.222
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44-log)
 Source Host           : 172.168.20.222:3306
 Source Schema         : im

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44-log)
 File Encoding         : 65001

 Date: 08/01/2026 13:30:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for QRTZ_BLOB_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `blob_data` blob NULL COMMENT '?????Trigger??',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Blob???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_BLOB_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CALENDARS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `calendar` blob NOT NULL COMMENT '?????calendar??',
  PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_CALENDARS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CRON_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron???',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Cron???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_CRON_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_FIRED_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??????',
  `fired_time` bigint(13) NOT NULL COMMENT '?????',
  `sched_time` bigint(13) NOT NULL COMMENT '????????',
  `priority` int(11) NOT NULL COMMENT '???',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????????',
  PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '????????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_FIRED_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_JOB_DETAILS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???????',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??????',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????????',
  `job_data` blob NULL COMMENT '?????job??',
  PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_JOB_DETAILS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_LOCKS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????',
  PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_LOCKS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SCHEDULER_STATE
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `last_checkin_time` bigint(13) NOT NULL COMMENT '??????',
  `checkin_interval` bigint(13) NOT NULL COMMENT '??????',
  PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '??????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_SCHEDULER_STATE
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `repeat_count` bigint(7) NOT NULL COMMENT '???????',
  `repeat_interval` bigint(12) NOT NULL COMMENT '???????',
  `times_triggered` bigint(10) NOT NULL COMMENT '???????',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_SIMPLE_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_name???',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers?trigger_group???',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String???trigger??????',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String???trigger??????',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String???trigger??????',
  `int_prop_1` int(11) NULL DEFAULT NULL COMMENT 'int???trigger??????',
  `int_prop_2` int(11) NULL DEFAULT NULL COMMENT 'int???trigger??????',
  `long_prop_1` bigint(20) NULL DEFAULT NULL COMMENT 'long???trigger??????',
  `long_prop_2` bigint(20) NULL DEFAULT NULL COMMENT 'long???trigger??????',
  `dec_prop_1` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal???trigger??????',
  `dec_prop_2` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal???trigger??????',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Boolean???trigger??????',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Boolean???trigger??????',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '????????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_SIMPROP_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??????',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????????',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details?job_name???',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details?job_group???',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `next_fire_time` bigint(13) NULL DEFAULT NULL COMMENT '???????????',
  `prev_fire_time` bigint(13) NULL DEFAULT NULL COMMENT '???????????-1??????',
  `priority` int(11) NULL DEFAULT NULL COMMENT '???',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??????',
  `start_time` bigint(13) NOT NULL COMMENT '????',
  `end_time` bigint(13) NULL DEFAULT NULL COMMENT '????',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?????',
  `misfire_instr` smallint(2) NULL DEFAULT NULL COMMENT '???????',
  `job_data` blob NULL COMMENT '?????job??',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  INDEX `sched_name`(`sched_name`, `job_name`, `job_group`) USING BTREE,
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `QRTZ_JOB_DETAILS` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '????????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of QRTZ_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作 sub主子表操作）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `form_col_num` int(1) NULL DEFAULT 1 COMMENT '表单布局（单列 双列 三列）',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint(20) NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------

-- ----------------------------
-- Table structure for im_app_shortcut
-- ----------------------------
DROP TABLE IF EXISTS `im_app_shortcut`;
CREATE TABLE `im_app_shortcut`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `app_id` bigint(20) NOT NULL COMMENT '??ID',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '??',
  `is_pinned` tinyint(1) NOT NULL DEFAULT 0 COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_app`(`user_id`, `app_id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_app_shortcut
-- ----------------------------

-- ----------------------------
-- Table structure for im_application
-- ----------------------------
DROP TABLE IF EXISTS `im_application`;
CREATE TABLE `im_application`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `icon` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???OFFICE??/DATA??/TOOLS??/CUSTOM???',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `app_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???ROUTE??/IFRAME??/LINK????',
  `app_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `is_system` tinyint(1) NOT NULL DEFAULT 0 COMMENT '??????',
  `is_visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '????',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '??',
  `permissions` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????(JSON)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_application
-- ----------------------------
INSERT INTO `im_application` VALUES (1, '??', 'approval', 'approval', 'OFFICE', '?????????????', 'ROUTE', '/im/approval', 1, 1, 1, NULL, '', '2026-01-06 13:03:37', '', '2026-01-06 13:03:37');
INSERT INTO `im_application` VALUES (2, '???', 'workbench', 'workbench', 'OFFICE', '?????????????', 'ROUTE', '/im/workbench', 1, 1, 2, NULL, '', '2026-01-06 13:03:37', '', '2026-01-06 13:03:37');
INSERT INTO `im_application` VALUES (3, '???', 'contacts', 'contacts', 'OFFICE', '?????', 'ROUTE', '/im/contacts', 1, 1, 3, NULL, '', '2026-01-06 13:03:37', '', '2026-01-06 13:03:37');
INSERT INTO `im_application` VALUES (4, '????', 'app-center', 'app-center', 'OFFICE', '??????', 'ROUTE', '/im/app-center', 1, 1, 4, NULL, '', '2026-01-06 13:03:37', '', '2026-01-06 13:03:37');
INSERT INTO `im_application` VALUES (5, '????', 'files', 'files', 'OFFICE', '??????', 'ROUTE', '/im/file', 1, 1, 5, NULL, '', '2026-01-06 13:03:37', '', '2026-01-06 13:03:37');
INSERT INTO `im_application` VALUES (6, '??', 'settings', 'settings', 'TOOLS', '????', 'ROUTE', '/im/settings', 1, 1, 6, NULL, '', '2026-01-06 13:03:37', '', '2026-01-06 13:03:37');

-- ----------------------------
-- Table structure for im_approval
-- ----------------------------
DROP TABLE IF EXISTS `im_approval`;
CREATE TABLE `im_approval`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `template_id` bigint(20) NOT NULL COMMENT '??ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `applicant_id` bigint(20) NOT NULL COMMENT '???ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '???PENDING???/APPROVED???/REJECTED???/CANCELLED???',
  `current_node_id` bigint(20) NULL DEFAULT NULL COMMENT '????ID',
  `form_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '????(JSON)',
  `attachments` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????(JSON)',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_applicant`(`applicant_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_template`(`template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_approval
-- ----------------------------
INSERT INTO `im_approval` VALUES (1, 1, '请假申请-张三', 2, 'PENDING', NULL, '{\"reason\":\"家中有事\",\"startDate\":\"2024-01-10\",\"endDate\":\"2024-01-12\",\"days\":3}', NULL, '2024-01-05 09:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (2, 1, '请假申请-李四', 3, 'APPROVED', 2, '{\"reason\":\"身体不适\",\"startDate\":\"2024-01-08\",\"endDate\":\"2024-01-09\",\"days\":2}', NULL, '2024-01-05 10:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (3, 1, '请假申请-王五', 4, 'PENDING', NULL, '{\"reason\":\"个人事务\",\"startDate\":\"2024-01-15\",\"endDate\":\"2024-01-16\",\"days\":2}', NULL, '2024-01-05 11:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (4, 2, '报销申请-赵六', 5, 'APPROVED', 2, '{\"amount\":500,\"type\":\"差旅\",\"description\":\"北京出差交通费\"}', NULL, '2024-01-04 09:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (5, 2, '报销申请-孙七', 6, 'PENDING', NULL, '{\"amount\":200,\"type\":\"餐饮\",\"description\":\"客户招待费\"}', NULL, '2024-01-05 14:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (6, 2, '报销申请-周八', 7, 'REJECTED', 2, '{\"amount\":1000,\"type\":\"办公\",\"description\":\"购买办公用品\"}', NULL, '2024-01-04 10:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (7, 3, '出差申请-吴九', 8, 'PENDING', NULL, '{\"destination\":\"上海\",\"startDate\":\"2024-01-20\",\"endDate\":\"2024-01-22\",\"reason\":\"参加技术会议\"}', NULL, '2024-01-05 15:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (8, 1, '请假申请-郑十', 9, 'APPROVED', 2, '{\"reason\":\"探亲\",\"startDate\":\"2024-01-25\",\"endDate\":\"2024-01-26\",\"days\":2}', NULL, '2024-01-05 16:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (9, 2, '报销申请-陈一', 10, 'PENDING', NULL, '{\"amount\":350,\"type\":\"交通\",\"description\":\"打车费用\"}', NULL, '2024-01-05 17:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (10, 3, '出差申请-林二', 11, 'APPROVED', 2, '{\"destination\":\"深圳\",\"startDate\":\"2024-01-18\",\"endDate\":\"2024-01-19\",\"reason\":\"客户拜访\"}', NULL, '2024-01-04 14:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (11, 1, '请假申请-黄三', 12, 'PENDING', NULL, '{\"reason\":\"搬家\",\"startDate\":\"2024-01-30\",\"endDate\":\"2024-01-30\",\"days\":1}', NULL, '2024-01-05 18:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (12, 2, '报销申请-刘四', 13, 'APPROVED', 2, '{\"amount\":150,\"type\":\"餐饮\",\"description\":\"团队聚餐\"}', NULL, '2024-01-04 15:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (13, 3, '出差申请-何五', 14, 'PENDING', NULL, '{\"destination\":\"杭州\",\"startDate\":\"2024-02-01\",\"endDate\":\"2024-02-03\",\"reason\":\"项目调研\"}', NULL, '2024-01-05 19:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (14, 1, '请假申请-罗六', 15, 'APPROVED', 2, '{\"reason\":\"看病\",\"startDate\":\"2024-01-22\",\"endDate\":\"2024-01-22\",\"days\":1}', NULL, '2024-01-04 16:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (15, 2, '报销申请-梁七', 16, 'PENDING', NULL, '{\"amount\":800,\"type\":\"差旅\",\"description\":\"南京出差\"}', NULL, '2024-01-05 20:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (16, 3, '出差申请-宋八', 17, 'APPROVED', 2, '{\"destination\":\"成都\",\"startDate\":\"2024-02-05\",\"endDate\":\"2024-02-07\",\"reason\":\"技术交流\"}', NULL, '2024-01-04 17:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (17, 1, '请假申请-唐九', 18, 'PENDING', NULL, '{\"reason\":\"婚假\",\"startDate\":\"2024-02-10\",\"endDate\":\"2024-02-17\",\"days\":5}', NULL, '2024-01-05 21:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (18, 2, '报销申请-冯十', 19, 'APPROVED', 2, '{\"amount\":120,\"type\":\"办公\",\"description\":\"打印资料\"}', NULL, '2024-01-04 18:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (19, 3, '出差申请-于十一', 20, 'PENDING', NULL, '{\"destination\":\"武汉\",\"startDate\":\"2024-02-15\",\"endDate\":\"2024-02-16\",\"reason\":\"培训\"}', NULL, '2024-01-05 22:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);
INSERT INTO `im_approval` VALUES (20, 1, '请假申请-董十二', 21, 'APPROVED', 2, '{\"reason\":\"年假\",\"startDate\":\"2024-03-01\",\"endDate\":\"2024-03-05\",\"days\":5}', NULL, '2024-01-04 19:00:00', NULL, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35', NULL);

-- ----------------------------
-- Table structure for im_approval_form_data
-- ----------------------------
DROP TABLE IF EXISTS `im_approval_form_data`;
CREATE TABLE `im_approval_form_data`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `approval_id` bigint(20) NOT NULL COMMENT '??ID',
  `field_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???',
  `field_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '???',
  `field_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_approval`(`approval_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_approval_form_data
-- ----------------------------

-- ----------------------------
-- Table structure for im_approval_node
-- ----------------------------
DROP TABLE IF EXISTS `im_approval_node`;
CREATE TABLE `im_approval_node`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `template_id` bigint(20) NOT NULL COMMENT '??ID',
  `node_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `node_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????APPROVE??/CC??/CONDITION??/PARALLEL??',
  `approvers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '?????(JSON)',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '??',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template`(`template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_approval_node
-- ----------------------------
INSERT INTO `im_approval_node` VALUES (1, 1, 'manager', '部门经理审批', 'ANY', '3', 1, '2026-01-08 12:12:35');
INSERT INTO `im_approval_node` VALUES (2, 1, 'hr', '人事审批', 'ANY', '7', 2, '2026-01-08 12:12:35');
INSERT INTO `im_approval_node` VALUES (3, 2, 'manager', '部门经理审批', 'ANY', '3', 1, '2026-01-08 12:12:35');
INSERT INTO `im_approval_node` VALUES (4, 2, 'finance', '财务审批', 'ANY', '8', 2, '2026-01-08 12:12:35');
INSERT INTO `im_approval_node` VALUES (5, 3, 'manager', '部门经理审批', 'ANY', '3', 1, '2026-01-08 12:12:35');

-- ----------------------------
-- Table structure for im_approval_record
-- ----------------------------
DROP TABLE IF EXISTS `im_approval_record`;
CREATE TABLE `im_approval_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `approval_id` bigint(20) NOT NULL COMMENT '??ID',
  `node_id` bigint(20) NOT NULL COMMENT '??ID',
  `approver_id` bigint(20) NOT NULL COMMENT '???ID',
  `action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???APPROVE??/REJECT??/TRANSFER??/ADD_SIGN??',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `attachments` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??(JSON)',
  `action_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_approval`(`approval_id`) USING BTREE,
  INDEX `idx_approver`(`approver_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_approval_record
-- ----------------------------
INSERT INTO `im_approval_record` VALUES (1, 2, 1, 3, 'APPROVE', '同意请假', NULL, '2024-01-05 10:30:00');
INSERT INTO `im_approval_record` VALUES (2, 2, 2, 7, 'APPROVE', '人事确认无误', NULL, '2024-01-05 14:00:00');
INSERT INTO `im_approval_record` VALUES (3, 4, 3, 3, 'APPROVE', '报销合理', NULL, '2024-01-04 10:00:00');
INSERT INTO `im_approval_record` VALUES (4, 4, 4, 8, 'APPROVE', '财务审核通过', NULL, '2024-01-04 14:00:00');
INSERT INTO `im_approval_record` VALUES (5, 6, 3, 3, 'APPROVE', '部门同意', NULL, '2024-01-04 11:00:00');
INSERT INTO `im_approval_record` VALUES (6, 6, 4, 8, 'REJECT', '超出预算', NULL, '2024-01-04 14:30:00');
INSERT INTO `im_approval_record` VALUES (7, 8, 1, 3, 'APPROVE', '同意', NULL, '2024-01-05 17:00:00');
INSERT INTO `im_approval_record` VALUES (8, 8, 2, 7, 'APPROVE', '人事确认', NULL, '2024-01-05 18:00:00');
INSERT INTO `im_approval_record` VALUES (9, 10, 5, 3, 'APPROVE', '同意出差', NULL, '2024-01-04 15:00:00');
INSERT INTO `im_approval_record` VALUES (10, 12, 3, 3, 'APPROVE', '同意报销', NULL, '2024-01-04 16:00:00');
INSERT INTO `im_approval_record` VALUES (11, 12, 4, 8, 'APPROVE', '财务通过', NULL, '2024-01-04 17:00:00');
INSERT INTO `im_approval_record` VALUES (12, 14, 1, 3, 'APPROVE', '同意', NULL, '2024-01-04 17:00:00');
INSERT INTO `im_approval_record` VALUES (13, 14, 2, 7, 'APPROVE', '人事确认', NULL, '2024-01-04 18:00:00');
INSERT INTO `im_approval_record` VALUES (14, 16, 5, 3, 'APPROVE', '同意出差', NULL, '2024-01-04 18:00:00');
INSERT INTO `im_approval_record` VALUES (15, 18, 3, 3, 'APPROVE', '同意', NULL, '2024-01-04 19:00:00');
INSERT INTO `im_approval_record` VALUES (16, 18, 4, 8, 'APPROVE', '财务审核通过', NULL, '2024-01-04 20:00:00');
INSERT INTO `im_approval_record` VALUES (17, 20, 1, 3, 'APPROVE', '同意年假', NULL, '2024-01-04 20:00:00');
INSERT INTO `im_approval_record` VALUES (18, 20, 2, 7, 'APPROVE', '人事确认', NULL, '2024-01-04 21:00:00');
INSERT INTO `im_approval_record` VALUES (19, 1, 1, 3, 'APPROVE', '同意请假', NULL, '2024-01-05 23:00:00');
INSERT INTO `im_approval_record` VALUES (20, 3, 1, 3, 'APPROVE', '同意', NULL, '2024-01-05 23:30:00');
INSERT INTO `im_approval_record` VALUES (21, 5, 3, 3, 'APPROVE', '部门同意', NULL, '2024-01-05 23:45:00');
INSERT INTO `im_approval_record` VALUES (22, 7, 5, 3, 'APPROVE', '同意出差', NULL, '2024-01-06 00:00:00');
INSERT INTO `im_approval_record` VALUES (23, 9, 3, 3, 'APPROVE', '同意报销', NULL, '2024-01-06 00:15:00');
INSERT INTO `im_approval_record` VALUES (24, 11, 1, 3, 'APPROVE', '同意', NULL, '2024-01-06 00:30:00');
INSERT INTO `im_approval_record` VALUES (25, 13, 5, 3, 'APPROVE', '同意出差', NULL, '2024-01-06 00:45:00');
INSERT INTO `im_approval_record` VALUES (26, 15, 3, 3, 'APPROVE', '部门同意', NULL, '2024-01-06 01:00:00');
INSERT INTO `im_approval_record` VALUES (27, 17, 1, 3, 'APPROVE', '同意婚假', NULL, '2024-01-06 01:15:00');
INSERT INTO `im_approval_record` VALUES (28, 19, 5, 3, 'APPROVE', '同意培训', NULL, '2024-01-06 01:30:00');
INSERT INTO `im_approval_record` VALUES (29, 2, 1, 3, 'APPROVE', '补充审批', NULL, '2024-01-06 01:45:00');
INSERT INTO `im_approval_record` VALUES (30, 4, 3, 3, 'APPROVE', '补充确认', NULL, '2024-01-06 02:00:00');

-- ----------------------------
-- Table structure for im_approval_template
-- ----------------------------
DROP TABLE IF EXISTS `im_approval_template`;
CREATE TABLE `im_approval_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????/??/??/??',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `form_schema` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????(JSON)',
  `flow_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????(JSON)',
  `icon` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `is_system` tinyint(1) NOT NULL DEFAULT 0 COMMENT '??????',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '???ACTIVE??/DISABLED??',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '??',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_approval_template
-- ----------------------------
INSERT INTO `im_approval_template` VALUES (1, '请假申请', 'leave', 'HR', '员工请假审批流程', '{\"fields\":[{\"name\":\"reason\",\"label\":\"请假原因\",\"type\":\"textarea\",\"required\":true},{\"name\":\"startDate\",\"label\":\"开始日期\",\"type\":\"date\",\"required\":true},{\"name\":\"endDate\",\"label\":\"结束日期\",\"type\":\"date\",\"required\":true},{\"name\":\"days\",\"label\":\"请假天数\",\"type\":\"number\",\"required\":true}]}', '[{\"key\":\"manager\",\"name\":\"部门经理审批\",\"type\":\"ANY\",\"approvers\":[3]},{\"key\":\"hr\",\"name\":\"人事审批\",\"type\":\"ANY\",\"approvers\":[7]}]', NULL, 0, 'ACTIVE', 0, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35');
INSERT INTO `im_approval_template` VALUES (2, '报销申请', 'expense', 'FINANCE', '费用报销审批流程', '{\"fields\":[{\"name\":\"amount\",\"label\":\"报销金额\",\"type\":\"number\",\"required\":true},{\"name\":\"type\",\"label\":\"报销类型\",\"type\":\"select\",\"options\":[\"差旅\",\"餐饮\",\"交通\",\"办公\"],\"required\":true},{\"name\":\"description\",\"label\":\"报销说明\",\"type\":\"textarea\",\"required\":true}]}', '[{\"key\":\"manager\",\"name\":\"部门经理审批\",\"type\":\"ANY\",\"approvers\":[3]},{\"key\":\"finance\",\"name\":\"财务审批\",\"type\":\"ANY\",\"approvers\":[8]}]', NULL, 0, 'ACTIVE', 0, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35');
INSERT INTO `im_approval_template` VALUES (3, '出差申请', 'business_trip', 'GENERAL', '出差审批流程', '{\"fields\":[{\"name\":\"destination\",\"label\":\"出差地点\",\"type\":\"text\",\"required\":true},{\"name\":\"startDate\",\"label\":\"开始日期\",\"type\":\"date\",\"required\":true},{\"name\":\"endDate\",\"label\":\"结束日期\",\"type\":\"date\",\"required\":true},{\"name\":\"reason\",\"label\":\"出差原因\",\"type\":\"textarea\",\"required\":true}]}', '[{\"key\":\"manager\",\"name\":\"部门经理审批\",\"type\":\"ANY\",\"approvers\":[3]}]', NULL, 0, 'ACTIVE', 0, '', '2026-01-08 12:12:35', '', '2026-01-08 12:12:35');

-- ----------------------------
-- Table structure for im_attendance
-- ----------------------------
DROP TABLE IF EXISTS `im_attendance`;
CREATE TABLE `im_attendance`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `attendance_date` date NOT NULL COMMENT '打卡日期',
  `check_in_time` datetime NULL DEFAULT NULL COMMENT '上班打卡时间',
  `check_out_time` datetime NULL DEFAULT NULL COMMENT '下班打卡时间',
  `check_in_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NORMAL' COMMENT '上班打卡状态（NORMAL正常 LATE迟到 ABSENT缺卡）',
  `check_out_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NORMAL' COMMENT '下班打卡状态（NORMAL正常 EARLY早退 ABSENT缺卡）',
  `work_minutes` int(11) NULL DEFAULT NULL COMMENT '工作时长（分钟）',
  `attendance_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'WORK' COMMENT '打卡类型（WORK工作日 OVERTIME加班 WEEKEND周末）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `check_in_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上班打卡位置（JSON格式）',
  `check_out_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下班打卡位置（JSON格式）',
  `device_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备信息',
  `approve_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审批状态（PENDING待审批 APPROVED已通过 REJECTED已拒绝）',
  `approver_id` bigint(20) NULL DEFAULT NULL COMMENT '审批人ID',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审批意见',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id`, `attendance_date`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_attendance_date`(`attendance_date`) USING BTREE,
  INDEX `idx_approve_status`(`approve_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '考勤打卡表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_attendance
-- ----------------------------
INSERT INTO `im_attendance` VALUES (1, 2, '2024-01-02', '2024-01-02 08:55:00', '2024-01-02 18:05:00', 'NORMAL', 'NORMAL', 550, 'WORK', NULL, '公司', NULL, 'iPhone 14', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (2, 2, '2024-01-03', '2024-01-03 09:05:00', '2024-01-03 18:00:00', 'LATE', 'NORMAL', 535, 'WORK', NULL, '公司', NULL, 'iPhone 14', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (3, 2, '2024-01-04', '2024-01-04 08:50:00', '2024-01-04 17:55:00', 'NORMAL', 'EARLY', 545, 'WORK', NULL, '公司', NULL, 'iPhone 14', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (4, 2, '2024-01-05', '2024-01-05 09:00:00', '2024-01-05 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', NULL, '公司', NULL, 'iPhone 14', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (5, 3, '2024-01-02', '2024-01-02 08:45:00', '2024-01-02 18:10:00', 'NORMAL', 'NORMAL', 565, 'WORK', NULL, '公司', NULL, 'Android', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (6, 3, '2024-01-03', '2024-01-03 09:15:00', '2024-01-03 18:00:00', 'LATE', 'NORMAL', 525, 'WORK', NULL, '公司', NULL, 'Android', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (7, 3, '2024-01-04', '2024-01-04 08:55:00', '2024-01-04 18:00:00', 'NORMAL', 'NORMAL', 545, 'WORK', NULL, '公司', NULL, 'Android', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (8, 4, '2024-01-02', '2024-01-02 09:00:00', '2024-01-02 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', NULL, '公司', NULL, 'Windows', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (9, 4, '2024-01-03', '2024-01-03 08:50:00', '2024-01-03 18:05:00', 'NORMAL', 'NORMAL', 555, 'WORK', NULL, '公司', NULL, 'Windows', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (10, 4, '2024-01-04', '2024-01-04 09:20:00', '2024-01-04 17:50:00', 'LATE', 'EARLY', 510, 'WORK', NULL, '公司', NULL, 'Windows', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (11, 5, '2024-01-02', '2024-01-02 08:58:00', '2024-01-02 18:02:00', 'NORMAL', 'NORMAL', 544, 'WORK', NULL, '公司', NULL, 'iPhone 13', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (12, 5, '2024-01-03', '2024-01-03 09:00:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', NULL, '公司', NULL, 'iPhone 13', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (13, 5, '2024-01-04', '2024-01-04 08:55:00', '2024-01-04 18:00:00', 'NORMAL', 'NORMAL', 545, 'WORK', NULL, '公司', NULL, 'iPhone 13', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (14, 6, '2024-01-02', '2024-01-02 09:10:00', '2024-01-02 18:00:00', 'LATE', 'NORMAL', 530, 'WORK', NULL, '公司', NULL, 'Android', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (15, 6, '2024-01-03', '2024-01-03 08:50:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 550, 'WORK', NULL, '公司', NULL, 'Android', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (16, 6, '2024-01-04', '2024-01-04 09:00:00', '2024-01-04 17:45:00', 'NORMAL', 'EARLY', 525, 'WORK', NULL, '公司', NULL, 'Android', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (17, 7, '2024-01-02', '2024-01-02 08:55:00', '2024-01-02 18:00:00', 'NORMAL', 'NORMAL', 545, 'WORK', NULL, '公司', NULL, 'iPhone', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (18, 7, '2024-01-03', '2024-01-03 09:00:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', NULL, '公司', NULL, 'iPhone', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (19, 7, '2024-01-04', '2024-01-04 08:50:00', '2024-01-04 18:05:00', 'NORMAL', 'NORMAL', 555, 'WORK', NULL, '公司', NULL, 'iPhone', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (20, 8, '2024-01-02', '2024-01-02 09:05:00', '2024-01-02 18:00:00', 'LATE', 'NORMAL', 535, 'WORK', NULL, '公司', NULL, 'Windows', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (21, 8, '2024-01-03', '2024-01-03 08:55:00', '2024-01-03 17:55:00', 'NORMAL', 'EARLY', 540, 'WORK', NULL, '公司', NULL, 'Windows', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (22, 8, '2024-01-04', '2024-01-04 09:00:00', '2024-01-04 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', NULL, '公司', NULL, 'Windows', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (23, 9, '2024-01-02', '2024-01-02 08:50:00', '2024-01-02 18:10:00', 'NORMAL', 'NORMAL', 560, 'WORK', NULL, '公司', NULL, 'MacBook', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (24, 9, '2024-01-03', '2024-01-03 09:00:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', NULL, '公司', NULL, 'MacBook', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (25, 9, '2024-01-04', '2024-01-04 08:55:00', '2024-01-04 18:00:00', 'NORMAL', 'NORMAL', 545, 'WORK', NULL, '公司', NULL, 'MacBook', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (26, 10, '2024-01-02', '2024-01-02 09:00:00', '2024-01-02 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', NULL, '公司', NULL, 'iPad', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (27, 10, '2024-01-03', '2024-01-03 08:58:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 542, 'WORK', NULL, '公司', NULL, 'iPad', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (28, 10, '2024-01-04', '2024-01-04 09:15:00', '2024-01-04 17:50:00', 'LATE', 'EARLY', 515, 'WORK', NULL, '公司', NULL, 'iPad', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (29, 11, '2024-01-02', '2024-01-02 08:55:00', '2024-01-02 18:05:00', 'NORMAL', 'NORMAL', 550, 'WORK', NULL, '公司', NULL, 'iPhone', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');
INSERT INTO `im_attendance` VALUES (30, 11, '2024-01-03', '2024-01-03 09:00:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', NULL, '公司', NULL, 'iPhone', NULL, NULL, NULL, NULL, '2026-01-08 12:12:35', '2026-01-08 12:12:35');

-- ----------------------------
-- Table structure for im_attendance_statistics
-- ----------------------------
DROP TABLE IF EXISTS `im_attendance_statistics`;
CREATE TABLE `im_attendance_statistics`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `statistics_month` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '统计年月（yyyy-MM）',
  `attendance_days` int(11) NULL DEFAULT 0 COMMENT '出勤天数',
  `leave_days` int(11) NULL DEFAULT 0 COMMENT '请假天数',
  `overtime_days` int(11) NULL DEFAULT 0 COMMENT '加班天数',
  `late_count` int(11) NULL DEFAULT 0 COMMENT '迟到次数',
  `early_count` int(11) NULL DEFAULT 0 COMMENT '早退次数',
  `absent_count` int(11) NULL DEFAULT 0 COMMENT '缺卡次数',
  `actual_work_hours` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '实际工作时长（小时）',
  `standard_work_hours` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '标准工作时长（小时）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_month`(`user_id`, `statistics_month`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_statistics_month`(`statistics_month`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '考勤统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_attendance_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for im_audit_export_request
-- ----------------------------
DROP TABLE IF EXISTS `im_audit_export_request`;
CREATE TABLE `im_audit_export_request`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '?????ID',
  `export_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????MESSAGE?? FILE?? USER???',
  `start_time` datetime NOT NULL COMMENT '????',
  `end_time` datetime NOT NULL COMMENT '????',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '???PENDING??? APPROVED??? REJECTED??? EXPIRED????',
  `approver_id` bigint(20) NULL DEFAULT NULL COMMENT '?????ID',
  `approved_time` datetime NULL DEFAULT NULL COMMENT '????',
  `approval_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `export_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??????',
  `download_count` int(11) NOT NULL DEFAULT 0 COMMENT '????',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_approver_id`(`approver_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_audit_export_request_approver` FOREIGN KEY (`approver_id`) REFERENCES `im_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_audit_export_request_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_audit_export_request
-- ----------------------------

-- ----------------------------
-- Table structure for im_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `im_audit_log`;
CREATE TABLE `im_audit_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '????ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????LOGIN?? LOGOUT?? SEND_MESSAGE???? DELETE_MESSAGE???? CREATE_GROUP???? JOIN_GROUP???? LEAVE_GROUP???? ADD_FRIEND???? DELETE_FRIEND?????',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?????USER MESSAGE GROUP CONVERSATION FRIEND?',
  `target_id` bigint(20) NULL DEFAULT NULL COMMENT '??ID',
  `operation_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'SUCCESS' COMMENT '?????SUCCESS?? FAILED???',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP??',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_operation_type`(`operation_type`) USING BTREE,
  INDEX `idx_target_type`(`target_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_operation_result`(`operation_result`) USING BTREE,
  CONSTRAINT `fk_audit_log_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1002 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_audit_log
-- ----------------------------
INSERT INTO `im_audit_log` VALUES (1, 2, 'LOGIN', 'USER', 2, 'SUCCESS', NULL, '192.168.1.100', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (2, 2, 'SEND_MESSAGE', 'MESSAGE', 1, 'SUCCESS', NULL, '192.168.1.100', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (3, 3, 'LOGIN', 'USER', 3, 'SUCCESS', NULL, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (4, 3, 'SEND_MESSAGE', 'MESSAGE', 2, 'SUCCESS', NULL, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (5, 4, 'LOGIN', 'USER', 4, 'SUCCESS', NULL, '192.168.1.102', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (6, 4, 'CREATE_GROUP', 'GROUP', 1, 'SUCCESS', NULL, '192.168.1.102', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (7, 5, 'LOGIN', 'USER', 5, 'SUCCESS', NULL, '192.168.1.103', 'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (8, 5, 'SUBMIT_APPROVAL', 'APPROVAL', 4, 'SUCCESS', NULL, '192.168.1.103', 'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (9, 6, 'LOGIN', 'USER', 6, 'SUCCESS', NULL, '192.168.1.104', 'Mozilla/5.0 (Linux; Android 12)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (10, 6, 'APPROVE_APPROVAL', 'APPROVAL', 4, 'SUCCESS', NULL, '192.168.1.104', 'Mozilla/5.0 (Linux; Android 12)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (11, 7, 'LOGIN', 'USER', 7, 'SUCCESS', NULL, '192.168.1.105', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (12, 7, 'UPLOAD_FILE', 'FILE', 1, 'SUCCESS', NULL, '192.168.1.105', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (13, 8, 'LOGIN', 'USER', 8, 'SUCCESS', NULL, '192.168.1.106', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (14, 8, 'SEND_MESSAGE', 'MESSAGE', 22, 'SUCCESS', NULL, '192.168.1.106', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (15, 9, 'LOGIN', 'USER', 9, 'SUCCESS', NULL, '192.168.1.107', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (16, 9, 'APPROVE_APPROVAL', 'APPROVAL', 8, 'SUCCESS', NULL, '192.168.1.107', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (17, 10, 'LOGIN', 'USER', 10, 'SUCCESS', NULL, '192.168.1.108', 'Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (18, 10, 'CHECK_IN', 'ATTENDANCE', 26, 'SUCCESS', NULL, '192.168.1.108', 'Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (19, 11, 'LOGIN', 'USER', 11, 'SUCCESS', NULL, '192.168.1.109', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', '2026-01-08 12:12:35');
INSERT INTO `im_audit_log` VALUES (20, 11, 'SEND_MESSAGE', 'MESSAGE', 30, 'SUCCESS', NULL, '192.168.1.109', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', '2026-01-08 12:12:35');

-- ----------------------------
-- Table structure for im_conversation
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation`;
CREATE TABLE `im_conversation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????PRIVATE?? GROUP???',
  `target_id` bigint(20) NULL DEFAULT NULL COMMENT '??ID?????????ID???????ID?',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `last_message_id` bigint(20) NULL DEFAULT NULL COMMENT '??????ID',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '????????',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_target_id`(`target_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_conversation
-- ----------------------------
INSERT INTO `im_conversation` VALUES (1, 'SINGLE', 3, '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC3', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 13:29:33');
INSERT INTO `im_conversation` VALUES (2, 'SINGLE', 4, '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC4', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 13:29:34');
INSERT INTO `im_conversation` VALUES (3, 'SINGLE', 5, '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC5', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 13:29:34');
INSERT INTO `im_conversation` VALUES (4, 'SINGLE', 6, '孙七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC6', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 13:29:34');
INSERT INTO `im_conversation` VALUES (5, 'SINGLE', 7, '周八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC7', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 13:29:34');
INSERT INTO `im_conversation` VALUES (6, 'GROUP', 1, '技术交流群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG1', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (7, 'GROUP', 2, '产品讨论群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG2', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (8, 'GROUP', 3, '项目开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG3', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (9, 'GROUP', 4, '公司全员群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG4', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (10, 'SINGLE', 8, '吴九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC8', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (11, 'SINGLE', 9, '郑十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC9', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (12, 'GROUP', 5, '篮球爱好者', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG5', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (13, 'SINGLE', 10, '陈一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCA', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (14, 'GROUP', 6, '读书分享会', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG6', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_conversation` VALUES (15, 'SINGLE', 11, '林二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCB', NULL, '2026-01-08 12:12:34', 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');

-- ----------------------------
-- Table structure for im_conversation_member
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation_member`;
CREATE TABLE `im_conversation_member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MEMBER' COMMENT '???OWNER??? MEMBER?????',
  `unread_count` int(11) NOT NULL DEFAULT 0 COMMENT '?????',
  `is_pinned` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `is_muted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '??????0? 1??',
  `last_read_message_id` bigint(20) NULL DEFAULT NULL COMMENT '??????ID',
  `last_read_time` datetime NULL DEFAULT NULL COMMENT '??????',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_conversation_user`(`conversation_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_unread_count`(`unread_count`) USING BTREE,
  INDEX `idx_is_pinned`(`is_pinned`) USING BTREE,
  INDEX `idx_is_muted`(`is_muted`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  INDEX `idx_update_time`(`update_time`) USING BTREE,
  CONSTRAINT `fk_conversation_member_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_conversation_member_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_conversation_member
-- ----------------------------
INSERT INTO `im_conversation_member` VALUES (1, 1, 2, NULL, 'MEMBER', 1, 0, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (2, 1, 3, NULL, 'MEMBER', 0, 0, 0, 2, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (3, 2, 2, NULL, 'MEMBER', 1, 0, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (4, 2, 4, NULL, 'MEMBER', 0, 0, 0, 5, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (5, 3, 2, NULL, 'MEMBER', 1, 0, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (6, 3, 5, NULL, 'MEMBER', 0, 0, 0, 7, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (7, 4, 2, NULL, 'MEMBER', 1, 0, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (8, 4, 6, NULL, 'MEMBER', 0, 0, 0, 9, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (9, 5, 2, NULL, 'MEMBER', 1, 0, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (10, 5, 7, NULL, 'MEMBER', 0, 0, 0, 11, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (11, 6, 2, NULL, 'MEMBER', 2, 0, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (12, 6, 3, NULL, 'MEMBER', 0, 0, 0, 14, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (13, 6, 4, NULL, 'MEMBER', 0, 0, 0, 14, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (14, 6, 5, NULL, 'MEMBER', 0, 0, 0, 14, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (15, 6, 6, NULL, 'MEMBER', 0, 0, 0, 14, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (16, 7, 3, NULL, 'MEMBER', 1, 0, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (17, 7, 7, NULL, 'MEMBER', 0, 0, 0, 16, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (18, 7, 8, NULL, 'MEMBER', 0, 0, 0, 16, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (19, 8, 4, NULL, 'MEMBER', 1, 0, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (20, 8, 9, NULL, 'MEMBER', 0, 0, 0, 18, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (21, 8, 10, NULL, 'MEMBER', 0, 0, 0, 18, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (22, 8, 11, NULL, 'MEMBER', 0, 0, 0, 18, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (23, 9, 2, NULL, 'MEMBER', 1, 1, 0, NULL, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (24, 9, 3, NULL, 'MEMBER', 0, 0, 0, 20, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (25, 9, 4, NULL, 'MEMBER', 0, 0, 0, 20, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (26, 9, 5, NULL, 'MEMBER', 0, 0, 0, 20, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (27, 9, 6, NULL, 'MEMBER', 0, 0, 0, 20, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (28, 9, 7, NULL, 'MEMBER', 0, 0, 0, 20, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (29, 9, 8, NULL, 'MEMBER', 0, 0, 0, 20, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (30, 9, 9, NULL, 'MEMBER', 0, 0, 0, 20, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');
INSERT INTO `im_conversation_member` VALUES (31, 9, 10, NULL, 'MEMBER', 0, 0, 0, 20, NULL, 0, NULL, '2026-01-08 12:28:05', '2026-01-08 12:28:05');

-- ----------------------------
-- Table structure for im_file_asset
-- ----------------------------
DROP TABLE IF EXISTS `im_file_asset`;
CREATE TABLE `im_file_asset`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `file_size` bigint(20) NOT NULL COMMENT '????????',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `file_ext` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????',
  `md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??MD5?',
  `uploader_id` bigint(20) NOT NULL COMMENT '?????ID',
  `download_count` int(11) NOT NULL DEFAULT 0 COMMENT '????',
  `download_expire_time` datetime NULL DEFAULT NULL COMMENT '????????',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '???ACTIVE?? DELETED????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uploader_id`(`uploader_id`) USING BTREE,
  INDEX `idx_md5`(`md5`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1003 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_file_asset
-- ----------------------------
INSERT INTO `im_file_asset` VALUES (1000, 'bbb.pdf', '/files/test_document.pdf', 1024000, 'document', 'pdf', NULL, 1, 0, NULL, 'ACTIVE', '2025-12-31 15:08:20');
INSERT INTO `im_file_asset` VALUES (1001, 'aa.jpg', '/files/example_image.jpg', 512000, 'image', 'jpg', NULL, 2, 0, NULL, 'ACTIVE', '2025-12-31 15:08:20');
INSERT INTO `im_file_asset` VALUES (1002, 'bbb.xlsx', '/files/project_plan.xlsx', 2048000, 'document', 'xlsx', NULL, 1, 0, NULL, 'ACTIVE', '2025-12-31 15:08:20');

-- ----------------------------
-- Table structure for im_file_chunk_detail
-- ----------------------------
DROP TABLE IF EXISTS `im_file_chunk_detail`;
CREATE TABLE `im_file_chunk_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '上传ID',
  `chunk_number` int(11) NOT NULL COMMENT '分片序号（从1开始）',
  `chunk_size` int(11) NOT NULL COMMENT '分片大小（字节）',
  `chunk_md5` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分片MD5值',
  `chunk_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分片文件存储路径',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待上传、UPLOADING上传中、COMPLETED已完成',
  `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_upload_chunk`(`upload_id`, `chunk_number`) USING BTREE,
  INDEX `idx_upload_id`(`upload_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分片文件详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_file_chunk_detail
-- ----------------------------

-- ----------------------------
-- Table structure for im_file_chunk_upload
-- ----------------------------
DROP TABLE IF EXISTS `im_file_chunk_upload`;
CREATE TABLE `im_file_chunk_upload`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '上传ID（唯一标识一次上传任务）',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原始文件名',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
  `file_md5` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件MD5值',
  `chunk_size` int(11) NOT NULL COMMENT '分片大小（字节）',
  `total_chunks` int(11) NOT NULL COMMENT '总分片数',
  `uploaded_chunks` int(11) NOT NULL DEFAULT 0 COMMENT '已上传分片数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'UPLOADING' COMMENT '状态：UPLOADING上传中、PAUSED已暂停、COMPLETED已完成、CANCELLED已取消',
  `user_id` bigint(20) NOT NULL COMMENT '上传用户ID',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最终文件路径',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最终文件URL',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '错误信息',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_upload_id`(`upload_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_md5`(`file_md5`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分片上传记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_file_chunk_upload
-- ----------------------------

-- ----------------------------
-- Table structure for im_friend
-- ----------------------------
DROP TABLE IF EXISTS `im_friend`;
CREATE TABLE `im_friend`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `friend_id` bigint(20) NOT NULL COMMENT '??ID',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id`, `friend_id`) USING BTREE,
  INDEX `idx_friend_id`(`friend_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_friend
-- ----------------------------
INSERT INTO `im_friend` VALUES (1, 2, 3, '李四', '我的好友', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (2, 2, 4, '王五', '我的好友', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (3, 2, 5, '赵六', '我的好友', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (4, 2, 6, '孙七', '同事', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (5, 2, 7, '周八', '同事', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (6, 3, 2, '张三', '我的好友', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (7, 3, 4, '王五', '我的好友', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (8, 3, 5, '赵六', '我的好友', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (9, 3, 8, '吴九', '同事', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (10, 4, 2, '张三', '我的好友', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (11, 4, 3, '李四', '我的好友', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (12, 4, 9, '郑十', '同事', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');
INSERT INTO `im_friend` VALUES (13, 4, 10, '陈一', '同事', 0, NULL, '2026-01-08 12:28:04', '2026-01-08 12:28:04');

-- ----------------------------
-- Table structure for im_friend_request
-- ----------------------------
DROP TABLE IF EXISTS `im_friend_request`;
CREATE TABLE `im_friend_request`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '?????ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '??????ID',
  `message` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '???PENDING??? APPROVED??? REJECTED????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `handled_time` datetime NULL DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_user`(`from_user_id`) USING BTREE,
  INDEX `idx_to_user`(`to_user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  CONSTRAINT `fk_friend_request_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_friend_request_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_friend_request
-- ----------------------------

-- ----------------------------
-- Table structure for im_group
-- ----------------------------
DROP TABLE IF EXISTS `im_group`;
CREATE TABLE `im_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `owner_id` bigint(20) NOT NULL COMMENT '??ID',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `max_members` int(11) NOT NULL DEFAULT 500 COMMENT '?????',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  CONSTRAINT `fk_group_owner` FOREIGN KEY (`owner_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_group
-- ----------------------------
INSERT INTO `im_group` VALUES (1, '技术交流群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG1', 2, '欢迎大家交流技术问题', 500, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (2, '产品讨论群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG2', 3, '产品需求讨论', 200, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (3, '项目开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG3', 4, '项目进度同步', 50, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (4, '公司全员群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG4', 2, '公司通知发布', 1000, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (5, '篮球爱好者', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG5', 5, '每周组织篮球活动', 100, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (6, '读书分享会', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG6', 6, '分享读书心得', 200, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (7, '前端开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG7', 7, '前端技术交流', 300, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (8, '后端开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG8', 8, '后端技术交流', 300, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (9, '设计团队', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG9', 9, 'UI/UX设计讨论', 50, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group` VALUES (10, '测试团队', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCGA', 10, '质量保证交流', 100, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');

-- ----------------------------
-- Table structure for im_group_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `im_group_blacklist`;
CREATE TABLE `im_group_blacklist`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `group_id` bigint(20) NOT NULL COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '???/????ID',
  `operator_id` bigint(20) NOT NULL COMMENT '???ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MUTE' COMMENT '???MUTE?? BLACKLIST????',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '?????NULL?????',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '?????0? 1??',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user_type`(`group_id`, `user_id`, `type`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_is_active`(`is_active`) USING BTREE,
  INDEX `idx_expire_time`(`expire_time`) USING BTREE,
  INDEX `fk_group_blacklist_operator`(`operator_id`) USING BTREE,
  CONSTRAINT `fk_group_blacklist_group` FOREIGN KEY (`group_id`) REFERENCES `im_group` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_group_blacklist_operator` FOREIGN KEY (`operator_id`) REFERENCES `im_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_group_blacklist_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????/???' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_group_blacklist
-- ----------------------------

-- ----------------------------
-- Table structure for im_group_file
-- ----------------------------
DROP TABLE IF EXISTS `im_group_file`;
CREATE TABLE `im_group_file`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `group_id` bigint(20) NOT NULL COMMENT '群组ID',
  `file_id` bigint(20) NOT NULL COMMENT '文件资产ID（关联im_file_asset）',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名称',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件类型：image/video/audio/document/other',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（字节）',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'default' COMMENT '文件分类：default/document/image/video/audio',
  `permission` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ALL' COMMENT '下载权限：ALL=所有人, ADMIN=仅管理员',
  `uploader_id` bigint(20) NOT NULL COMMENT '上传者ID',
  `uploader_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上传者名称',
  `download_count` int(11) NOT NULL DEFAULT 0 COMMENT '下载次数',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态：1=正常, 0=已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_id`(`group_id`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '群组文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_group_file
-- ----------------------------

-- ----------------------------
-- Table structure for im_group_member
-- ----------------------------
DROP TABLE IF EXISTS `im_group_member`;
CREATE TABLE `im_group_member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `group_id` bigint(20) NOT NULL COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MEMBER' COMMENT '???OWNER??? MEMBER?????',
  `is_muted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `deleted_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_role`(`role`) USING BTREE,
  INDEX `idx_is_muted`(`is_muted`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted`) USING BTREE,
  CONSTRAINT `fk_group_member_group` FOREIGN KEY (`group_id`) REFERENCES `im_group` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_group_member_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_group_member
-- ----------------------------
INSERT INTO `im_group_member` VALUES (39, 1, 2, '张三', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (40, 1, 3, '李四', 'ADMIN', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (41, 1, 4, '王五', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (42, 1, 5, '赵六', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (43, 1, 6, '孙七', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (44, 2, 3, '李四', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (45, 2, 7, '周八', 'ADMIN', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (46, 2, 8, '吴九', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (47, 3, 4, '王五', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (48, 3, 9, '郑十', 'ADMIN', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (49, 3, 10, '陈一', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (50, 3, 11, '林二', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (51, 4, 2, '张三', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (52, 4, 3, '李四', 'ADMIN', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (53, 4, 4, '王五', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (54, 4, 5, '赵六', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (55, 4, 6, '孙七', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (56, 4, 7, '周八', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (57, 4, 8, '吴九', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (58, 4, 9, '郑十', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (59, 4, 10, '陈一', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (60, 5, 5, '赵六', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (61, 5, 11, '林二', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (62, 5, 12, '黄三', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (63, 6, 6, '孙七', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (64, 6, 13, '刘四', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (65, 7, 7, '周八', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (66, 7, 14, '何五', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (67, 8, 8, '吴九', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (68, 8, 15, '罗六', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (69, 9, 9, '郑十', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (70, 9, 16, '梁七', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (71, 9, 17, '宋八', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (72, 10, 10, '陈一', 'OWNER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (73, 10, 18, '唐九', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (74, 10, 19, '冯十', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (75, 10, 20, '于十一', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');
INSERT INTO `im_group_member` VALUES (76, 10, 21, '董十二', 'MEMBER', 0, 0, NULL, '2026-01-08 12:12:34', '2026-01-08 12:12:34');

-- ----------------------------
-- Table structure for im_message
-- ----------------------------
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '??ID',
  `sender_id` bigint(20) NOT NULL COMMENT '???ID',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????TEXT?? IMAGE?? VIDEO?? AUDIO?? FILE???',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '????',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??URL',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '????????',
  `sensitive_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'NORMAL' COMMENT '?????NORMAL?? SENSITIVE?? HIGH?????',
  `is_revoked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `revoked_time` datetime NULL DEFAULT NULL COMMENT '????',
  `revoker_id` bigint(20) NULL DEFAULT NULL COMMENT '???ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_message_type`(`message_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_is_revoked`(`is_revoked`) USING BTREE,
  INDEX `idx_conversation_time`(`conversation_id`, `create_time`) USING BTREE,
  CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_message
-- ----------------------------
INSERT INTO `im_message` VALUES (1, 1, 2, 'TEXT', '你好，最近怎么样？', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 09:00:00');
INSERT INTO `im_message` VALUES (2, 1, 3, 'TEXT', '挺好的，谢谢关心！你呢？', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 09:01:00');
INSERT INTO `im_message` VALUES (3, 1, 2, 'TEXT', '我也不错，最近在学习新技术', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 09:02:00');
INSERT INTO `im_message` VALUES (4, 2, 2, 'TEXT', '王五，那个项目进展如何？', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 10:00:00');
INSERT INTO `im_message` VALUES (5, 2, 4, 'TEXT', '进展顺利，预计下周完成', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 10:05:00');
INSERT INTO `im_message` VALUES (6, 3, 2, 'TEXT', '赵六，明天开会吗？', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 11:00:00');
INSERT INTO `im_message` VALUES (7, 3, 5, 'TEXT', '是的，下午2点', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 11:01:00');
INSERT INTO `im_message` VALUES (8, 4, 2, 'TEXT', '孙七，文件发给我一下', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 12:00:00');
INSERT INTO `im_message` VALUES (9, 4, 6, 'TEXT', '好的，稍等', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 12:01:00');
INSERT INTO `im_message` VALUES (10, 5, 2, 'TEXT', '周八，设计稿看完了吗？', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 13:00:00');
INSERT INTO `im_message` VALUES (11, 5, 7, 'TEXT', '看完了，有一些修改意见', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 13:10:00');
INSERT INTO `im_message` VALUES (12, 6, 2, 'TEXT', '大家好，欢迎加入技术交流群', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 14:00:00');
INSERT INTO `im_message` VALUES (13, 6, 3, 'TEXT', '感谢群主邀请', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 14:01:00');
INSERT INTO `im_message` VALUES (14, 6, 4, 'TEXT', '向大家学习', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 14:02:00');
INSERT INTO `im_message` VALUES (15, 7, 3, 'TEXT', '产品需求已经更新', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 15:00:00');
INSERT INTO `im_message` VALUES (16, 7, 7, 'TEXT', '收到，我会查看', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 15:01:00');
INSERT INTO `im_message` VALUES (17, 8, 4, 'TEXT', '项目进度汇报', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 16:00:00');
INSERT INTO `im_message` VALUES (18, 8, 9, 'TEXT', '前端开发完成了80%', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 16:10:00');
INSERT INTO `im_message` VALUES (19, 9, 2, 'TEXT', '公司通知：本周五下午开会', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 17:00:00');
INSERT INTO `im_message` VALUES (20, 9, 3, 'TEXT', '收到', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-01 17:01:00');
INSERT INTO `im_message` VALUES (21, 10, 2, 'TEXT', '吴九，后端接口文档发我', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 09:00:00');
INSERT INTO `im_message` VALUES (22, 10, 8, 'TEXT', '已经发送到你邮箱', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 09:05:00');
INSERT INTO `im_message` VALUES (23, 11, 2, 'TEXT', '郑十，测试用例写好了吗？', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 10:00:00');
INSERT INTO `im_message` VALUES (24, 11, 9, 'TEXT', '写好了，正在执行测试', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 10:01:00');
INSERT INTO `im_message` VALUES (25, 12, 5, 'TEXT', '本周篮球活动照常进行', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 11:00:00');
INSERT INTO `im_message` VALUES (26, 12, 11, 'TEXT', '我会参加', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 11:01:00');
INSERT INTO `im_message` VALUES (27, 13, 2, 'TEXT', '陈一，UI验收通过了吗？', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 12:00:00');
INSERT INTO `im_message` VALUES (28, 13, 10, 'TEXT', '通过了，可以开发', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 12:01:00');
INSERT INTO `im_message` VALUES (29, 14, 6, 'TEXT', '本周读书分享：《深入理解计算机系统》', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 13:00:00');
INSERT INTO `im_message` VALUES (30, 15, 2, 'TEXT', '林二，代码review一下', NULL, NULL, NULL, 'NORMAL', 0, NULL, NULL, '2024-01-02 14:00:00');

-- ----------------------------
-- Table structure for im_message_favorite
-- ----------------------------
DROP TABLE IF EXISTS `im_message_favorite`;
CREATE TABLE `im_message_favorite`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `message_id` bigint(20) NOT NULL COMMENT '消息ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收藏备注',
  `tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签（多个标签用逗号分隔）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_message`(`user_id`, `message_id`) USING BTREE COMMENT '同一用户对同一消息只能收藏一次',
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `fk_favorite_message`(`message_id`) USING BTREE,
  CONSTRAINT `fk_favorite_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_message_favorite
-- ----------------------------

-- ----------------------------
-- Table structure for im_message_mention
-- ----------------------------
DROP TABLE IF EXISTS `im_message_mention`;
CREATE TABLE `im_message_mention`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` bigint(20) NOT NULL COMMENT '消息ID',
  `mentioned_user_id` bigint(20) NOT NULL COMMENT '被@的用户ID',
  `mentioned_by` bigint(20) NOT NULL COMMENT '@操作者ID',
  `mention_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'USER' COMMENT '提及类型：USER用户、ALL所有人',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读：0未读、1已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_id`(`message_id`) USING BTREE,
  INDEX `idx_mentioned_user_id`(`mentioned_user_id`) USING BTREE,
  INDEX `idx_mentioned_by`(`mentioned_by`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息@提及记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of im_message_mention
-- ----------------------------

-- ----------------------------
-- Table structure for im_message_reaction
-- ----------------------------
DROP TABLE IF EXISTS `im_message_reaction`;
CREATE TABLE `im_message_reaction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `message_id` bigint(20) NOT NULL COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '????ID',
  `reaction_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????LIKE?? DISLIKE? HEART?? LAUGH?? CRY?? SURPRISE???',
  `emoji` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user_reaction`(`message_id`, `user_id`, `reaction_type`) USING BTREE,
  INDEX `idx_message_id`(`message_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_reaction_type`(`reaction_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_reaction_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_reaction_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_message_reaction
-- ----------------------------

-- ----------------------------
-- Table structure for im_message_read
-- ----------------------------
DROP TABLE IF EXISTS `im_message_read`;
CREATE TABLE `im_message_read`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `message_id` bigint(20) NOT NULL COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '????ID',
  `conversation_id` bigint(20) NOT NULL COMMENT '??ID?????????????',
  `read_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `read_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'READ' COMMENT '?????READ?? RECEIPT?????',
  `device_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?????WEB MOBILE PC?',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id`, `user_id`) USING BTREE,
  INDEX `idx_user_read_time`(`user_id`, `read_time`) USING BTREE,
  INDEX `idx_conversation_user`(`conversation_id`, `user_id`) USING BTREE,
  CONSTRAINT `fk_message_read_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_read_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_read_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_message_read
-- ----------------------------

-- ----------------------------
-- Table structure for im_message_read_receipt
-- ----------------------------
DROP TABLE IF EXISTS `im_message_read_receipt`;
CREATE TABLE `im_message_read_receipt`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `message_id` bigint(20) NOT NULL COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `read_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_user`(`message_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_read_time`(`read_time`) USING BTREE,
  CONSTRAINT `fk_read_receipt_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_read_receipt_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '???????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_message_read_receipt
-- ----------------------------

-- ----------------------------
-- Table structure for im_sensitive_event
-- ----------------------------
DROP TABLE IF EXISTS `im_sensitive_event`;
CREATE TABLE `im_sensitive_event`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `conversation_id` bigint(20) NULL DEFAULT NULL COMMENT '??ID',
  `message_id` bigint(20) NULL DEFAULT NULL COMMENT '??ID',
  `detected_word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???????',
  `action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????REJECTED?? MASKED?? WARNED???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_message_id`(`message_id`) USING BTREE,
  CONSTRAINT `fk_sensitive_event_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_sensitive_event_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_sensitive_event_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_sensitive_event
-- ----------------------------

-- ----------------------------
-- Table structure for im_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `im_sensitive_word`;
CREATE TABLE `im_sensitive_word`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???',
  `level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'WARN' COMMENT '???WARN?? BLOCK???',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '???ACTIVE?? DISABLED???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_word`(`word`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_sensitive_word
-- ----------------------------

-- ----------------------------
-- Table structure for im_session_group
-- ----------------------------
DROP TABLE IF EXISTS `im_session_group`;
CREATE TABLE `im_session_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `group_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '??',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1003 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_session_group
-- ----------------------------
INSERT INTO `im_session_group` VALUES (1000, 1, '??', 0, '2025-12-31 15:08:20', '2025-12-31 15:08:20');
INSERT INTO `im_session_group` VALUES (1001, 1, '??', 0, '2025-12-31 15:08:20', '2025-12-31 15:08:20');
INSERT INTO `im_session_group` VALUES (1002, 1, '??', 0, '2025-12-31 15:08:20', '2025-12-31 15:08:20');

-- ----------------------------
-- Table structure for im_system_config
-- ----------------------------
DROP TABLE IF EXISTS `im_system_config`;
CREATE TABLE `im_system_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'STRING' COMMENT '?????STRING??? NUMBER?? BOOLEAN???',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '???ACTIVE?? DISABLED???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1006 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_system_config
-- ----------------------------
INSERT INTO `im_system_config` VALUES (1000, 'message.recall.window.seconds', '120', 'NUMBER', '???????????', 'ACTIVE', '2025-12-31 14:52:31', '2025-12-31 14:52:31');
INSERT INTO `im_system_config` VALUES (1001, 'message.max.length', '2000', 'NUMBER', '??????', 'ACTIVE', '2025-12-31 14:52:31', '2025-12-31 14:52:31');
INSERT INTO `im_system_config` VALUES (1002, 'file.max.size.mb', '20', 'NUMBER', '???????MB?', 'ACTIVE', '2025-12-31 14:52:31', '2025-12-31 14:52:31');
INSERT INTO `im_system_config` VALUES (1003, 'file.download.expire.minutes', '60', 'NUMBER', '??????????????', 'ACTIVE', '2025-12-31 14:52:31', '2025-12-31 14:52:31');
INSERT INTO `im_system_config` VALUES (1004, 'sensitive.enabled', 'true', 'BOOLEAN', '?????????', 'ACTIVE', '2025-12-31 14:52:31', '2025-12-31 14:52:31');
INSERT INTO `im_system_config` VALUES (1005, 'websocket.heartbeat.interval', '30', 'NUMBER', 'WebSocket???????', 'ACTIVE', '2025-12-31 14:52:31', '2025-12-31 14:52:31');

-- ----------------------------
-- Table structure for im_system_notification
-- ----------------------------
DROP TABLE IF EXISTS `im_system_notification`;
CREATE TABLE `im_system_notification`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '?????ID',
  `sender_id` bigint(20) NULL DEFAULT NULL COMMENT '?????ID??????NULL?',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????SYSTEM?? APPROVAL?? MESSAGE?? GROUP?? FRIEND???',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '????',
  `related_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?????USER MESSAGE GROUP CONVERSATION APPROVAL?',
  `related_id` bigint(20) NULL DEFAULT NULL COMMENT '??ID',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0? 1??',
  `read_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `fk_system_notification_sender`(`sender_id`) USING BTREE,
  CONSTRAINT `fk_system_notification_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_system_notification_sender` FOREIGN KEY (`sender_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_system_notification
-- ----------------------------

-- ----------------------------
-- Table structure for im_todo_item
-- ----------------------------
DROP TABLE IF EXISTS `im_todo_item`;
CREATE TABLE `im_todo_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '????',
  `priority` tinyint(1) NOT NULL DEFAULT 1 COMMENT '????1? 2? 3??',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '???PENDING??? IN_PROGRESS??? COMPLETED??? CANCELLED????',
  `due_date` date NULL DEFAULT NULL COMMENT '????',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_priority`(`priority`) USING BTREE,
  INDEX `idx_due_date`(`due_date`) USING BTREE,
  CONSTRAINT `fk_todo_item_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_todo_item
-- ----------------------------

-- ----------------------------
-- Table structure for im_user
-- ----------------------------
DROP TABLE IF EXISTS `im_user`;
CREATE TABLE `im_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???BCrypt???',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??URL',
  `gender` tinyint(1) NULL DEFAULT 0 COMMENT '???0?? 1? 2??',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '???0?? 1???',
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '??????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_mobile`(`mobile`) USING BTREE,
  INDEX `idx_email`(`email`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_last_online_time`(`last_online_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IM???' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_user
-- ----------------------------
INSERT INTO `im_user` VALUES (2, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC2', 1, '13800000002', 'zhangsan@example.com', 1, '努力工作，快乐生活', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (3, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC3', 1, '13800000003', 'lisi@example.com', 1, '学无止境', '2026-01-08 12:16:27', '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (4, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC4', 1, '13800000004', 'wangwu@example.com', 1, '代码改变世界', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (5, 'zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC5', 0, '13800000005', 'zhaoliu@example.com', 1, '保持热爱', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (6, 'sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC6', 1, '13800000006', 'sunqi@example.com', 1, '今天也要加油', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (7, 'zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC7', 0, '13800000007', 'zhouba@example.com', 1, '未来可期', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (8, 'wujiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '吴九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC8', 1, '13800000008', 'wujiu@example.com', 1, '行稳致远', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (9, 'zhengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '郑十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC9', 1, '13800000009', 'zhengshi@example.com', 1, '不忘初心', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (10, 'chenyi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCA', 0, '13800000010', 'chenyi@example.com', 1, '砥砺前行', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (11, 'liner', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '林二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCB', 1, '13800000011', 'liner@example.com', 1, '天天向上', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (12, 'huangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '黄三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCC', 1, '13800000012', 'huangsan@example.com', 1, '追求卓越', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (13, 'liusi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCD', 0, '13800000013', 'liusi@example.com', 1, '梦想成真', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (14, 'hewu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '何五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCE', 1, '13800000014', 'hewu@example.com', 1, '珍惜当下', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (15, 'luoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '罗六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCF', 1, '13800000015', 'luoliu@example.com', 1, '勇往直前', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (16, 'liangqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '梁七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG', 0, '13800000016', 'liangqi@example.com', 1, '脚踏实地', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (17, 'songba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '宋八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCH', 1, '13800000017', 'songba@example.com', 1, '天道酬勤', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (18, 'tangjiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '唐九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCI', 1, '13800000018', 'tangjiu@example.com', 1, '感恩有你', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (19, 'fengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '冯十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCJ', 0, '13800000019', 'fengshi@example.com', 1, '知足常乐', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (20, 'yushiyi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '于十一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCK', 1, '13800000020', 'yushiyi@example.com', 1, '生活美好', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (21, 'dongshier', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '董十二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCL', 1, '13800000021', 'dongshier@example.com', 1, '越努力越幸运', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');

-- ----------------------------
-- Table structure for im_user_backup
-- ----------------------------
DROP TABLE IF EXISTS `im_user_backup`;
CREATE TABLE `im_user_backup`  (
  `id` bigint(20) NOT NULL DEFAULT 0 COMMENT '??ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '???ACTIVE?? OFFLINE???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_user_backup
-- ----------------------------

-- ----------------------------
-- Table structure for im_user_device
-- ----------------------------
DROP TABLE IF EXISTS `im_user_device`;
CREATE TABLE `im_user_device`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `device_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '?????PC MOBILE WEB?',
  `device_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??????',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `os_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??????',
  `app_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP??',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ONLINE' COMMENT '???ONLINE?? OFFLINE?? AWAY?? BUSY???',
  `last_active_time` datetime NULL DEFAULT NULL COMMENT '??????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_device`(`user_id`, `device_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_device_type`(`device_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_last_active_time`(`last_active_time`) USING BTREE,
  CONSTRAINT `fk_user_device_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_user_device
-- ----------------------------

-- ----------------------------
-- Table structure for im_user_setting
-- ----------------------------
DROP TABLE IF EXISTS `im_user_setting`;
CREATE TABLE `im_user_setting`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint(20) NOT NULL COMMENT '??ID',
  `setting_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '????',
  `setting_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????JSON???',
  `setting_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NOTIFICATION' COMMENT '?????NOTIFICATION?? PRIVACY?? DISPLAY???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_setting_key`(`user_id`, `setting_key`) USING BTREE,
  INDEX `idx_setting_type`(`setting_type`) USING BTREE,
  CONSTRAINT `fk_user_setting_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_user_setting
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int(5) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2026-01-07 18:18:30', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2026-01-07 18:18:30', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2026-01-07 18:18:30', '', NULL, '深黑主题theme-dark，浅色主题theme-light，深蓝主题theme-blue');
INSERT INTO `sys_config` VALUES (4, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2026-01-07 18:18:30', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '用户管理-密码字符范围', 'sys.account.chrtype', '0', 'Y', 'admin', '2026-01-07 18:18:30', '', NULL, '默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');
INSERT INTO `sys_config` VALUES (6, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2026-01-07 18:18:30', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (7, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2026-01-07 18:18:30', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (8, '主框架页-菜单导航显示风格', 'sys.index.menuStyle', 'default', 'Y', 'admin', '2026-01-07 18:18:31', '', NULL, '菜单导航显示风格（default为左侧导航菜单，topnav为顶部导航菜单）');
INSERT INTO `sys_config` VALUES (9, '主框架页-是否开启页脚', 'sys.index.footer', 'true', 'Y', 'admin', '2026-01-07 18:18:31', '', NULL, '是否开启底部页脚显示（true显示，false隐藏）');
INSERT INTO `sys_config` VALUES (10, '主框架页-是否开启页签', 'sys.index.tagsView', 'true', 'Y', 'admin', '2026-01-07 18:18:31', '', NULL, '是否开启菜单多页签显示（true显示，false隐藏）');
INSERT INTO `sys_config` VALUES (11, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2026-01-07 18:18:31', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int(4) NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 200 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:57', '', NULL);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:57', '', NULL);
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:57', '', NULL);
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:58', '', NULL);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:58', '', NULL);
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:58', '', NULL);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:58', '', NULL);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:58', '', NULL);
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:58', '', NULL);
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-07 18:17:59', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int(4) NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2026-01-07 18:18:26', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-01-07 18:18:26', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-01-07 18:18:26', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2026-01-07 18:18:26', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:26', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2026-01-07 18:18:26', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:26', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2026-01-07 18:18:27', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:27', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2026-01-07 18:18:27', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2026-01-07 18:18:27', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2026-01-07 18:18:27', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:27', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2026-01-07 18:18:27', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-01-07 18:18:28', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:29', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-01-07 18:18:29', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:29', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2026-01-07 18:18:29', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2026-01-07 18:18:29', '', NULL, '停用状态');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2026-01-07 18:18:24', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2026-01-07 18:18:24', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2026-01-07 18:18:24', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2026-01-07 18:18:24', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2026-01-07 18:18:24', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2026-01-07 18:18:25', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2026-01-07 18:18:25', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2026-01-07 18:18:25', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2026-01-07 18:18:25', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2026-01-07 18:18:25', '', NULL, '登录状态列表');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2026-01-07 18:18:33', '', NULL, '');
INSERT INTO `sys_job` VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2026-01-07 18:18:33', '', NULL, '');
INSERT INTO `sys_job` VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2026-01-07 18:18:34', '', NULL, '');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `job_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '异常信息',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `login_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status`) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 105 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (100, 'admin', '127.0.0.1', '内网IP', 'Downloading Tool', 'Unknown', '0', '登录成功', '2026-01-07 18:19:14');
INSERT INTO `sys_logininfor` VALUES (101, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-07 18:19:36');
INSERT INTO `sys_logininfor` VALUES (102, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 09:21:05');
INSERT INTO `sys_logininfor` VALUES (103, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '1', '密码输入错误1次', '2026-01-08 11:09:31');
INSERT INTO `sys_logininfor` VALUES (104, 'admin', '127.0.0.1', '内网IP', 'Chrome 14', 'Windows 10', '0', '登录成功', '2026-01-08 11:09:43');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(4) NULL DEFAULT 0 COMMENT '显示顺序',
  `url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '请求地址',
  `target` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '打开方式（menuItem页签 menuBlank新窗口）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `is_refresh` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '是否刷新（0刷新 1不刷新）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2083 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, '#', '', 'M', '0', '1', '', 'fa fa-gear', 'admin', '2026-01-07 18:18:03', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 2, '#', '', 'M', '0', '1', '', 'fa fa-video-camera', 'admin', '2026-01-07 18:18:03', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 3, '#', 'menuItem', 'M', '1', '1', '', 'fa fa-bars', 'admin', '2026-01-07 18:18:03', 'admin', '2026-01-08 09:27:50', '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, '若依官网', 0, 4, 'http://ruoyi.vip', 'menuBlank', 'C', '1', '1', '', 'fa fa-location-arrow', 'admin', '2026-01-07 18:18:03', 'admin', '2026-01-08 09:22:53', '若依官网地址');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, '/system/user', '', 'C', '0', '1', 'system:user:view', 'fa fa-user-o', 'admin', '2026-01-07 18:18:03', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, '/system/role', '', 'C', '0', '1', 'system:role:view', 'fa fa-user-secret', 'admin', '2026-01-07 18:18:03', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, '/system/menu', '', 'C', '0', '1', 'system:menu:view', 'fa fa-th-list', 'admin', '2026-01-07 18:18:03', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, '/system/dept', '', 'C', '0', '1', 'system:dept:view', 'fa fa-outdent', 'admin', '2026-01-07 18:18:03', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, '/system/post', '', 'C', '0', '1', 'system:post:view', 'fa fa-address-card-o', 'admin', '2026-01-07 18:18:04', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, '/system/dict', '', 'C', '0', '1', 'system:dict:view', 'fa fa-bookmark-o', 'admin', '2026-01-07 18:18:04', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, '/system/config', '', 'C', '0', '1', 'system:config:view', 'fa fa-sun-o', 'admin', '2026-01-07 18:18:04', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, '/system/notice', '', 'C', '0', '1', 'system:notice:view', 'fa fa-bullhorn', 'admin', '2026-01-07 18:18:04', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108, '日志管理', 1, 9, '#', '', 'M', '0', '1', '', 'fa fa-pencil-square-o', 'admin', '2026-01-07 18:18:04', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, '/monitor/online', '', 'C', '0', '1', 'monitor:online:view', 'fa fa-user-circle', 'admin', '2026-01-07 18:18:04', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 2, 2, '/monitor/job', '', 'C', '0', '1', 'monitor:job:view', 'fa fa-tasks', 'admin', '2026-01-07 18:18:04', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` VALUES (111, '数据监控', 2, 3, '/monitor/data', '', 'C', '0', '1', 'monitor:data:view', 'fa fa-bug', 'admin', '2026-01-07 18:18:04', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` VALUES (112, '服务监控', 2, 4, '/monitor/server', '', 'C', '0', '1', 'monitor:server:view', 'fa fa-server', 'admin', '2026-01-07 18:18:04', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (113, '缓存监控', 2, 5, '/monitor/cache', '', 'C', '0', '1', 'monitor:cache:view', 'fa fa-cube', 'admin', '2026-01-07 18:18:04', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` VALUES (114, '表单构建', 3, 1, '/tool/build', '', 'C', '0', '1', 'tool:build:view', 'fa fa-wpforms', 'admin', '2026-01-07 18:18:05', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (115, '代码生成', 3, 2, '/tool/gen', '', 'C', '0', '1', 'tool:gen:view', 'fa fa-code', 'admin', '2026-01-07 18:18:05', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (116, '系统接口', 3, 3, '/tool/swagger', '', 'C', '0', '1', 'tool:swagger:view', 'fa fa-gg', 'admin', '2026-01-07 18:18:05', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, '/monitor/operlog', '', 'C', '0', '1', 'monitor:operlog:view', 'fa fa-address-book', 'admin', '2026-01-07 18:18:05', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, '/monitor/logininfor', '', 'C', '0', '1', 'monitor:logininfor:view', 'fa fa-file-image-o', 'admin', '2026-01-07 18:18:05', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` VALUES (1000, '用户查询', 100, 1, '#', '', 'F', '0', '1', 'system:user:list', '#', 'admin', '2026-01-07 18:18:05', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1001, '用户新增', 100, 2, '#', '', 'F', '0', '1', 'system:user:add', '#', 'admin', '2026-01-07 18:18:05', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户修改', 100, 3, '#', '', 'F', '0', '1', 'system:user:edit', '#', 'admin', '2026-01-07 18:18:05', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户删除', 100, 4, '#', '', 'F', '0', '1', 'system:user:remove', '#', 'admin', '2026-01-07 18:18:05', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户导出', 100, 5, '#', '', 'F', '0', '1', 'system:user:export', '#', 'admin', '2026-01-07 18:18:05', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导入', 100, 6, '#', '', 'F', '0', '1', 'system:user:import', '#', 'admin', '2026-01-07 18:18:05', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '重置密码', 100, 7, '#', '', 'F', '0', '1', 'system:user:resetPwd', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '角色查询', 101, 1, '#', '', 'F', '0', '1', 'system:role:list', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1008, '角色新增', 101, 2, '#', '', 'F', '0', '1', 'system:role:add', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色修改', 101, 3, '#', '', 'F', '0', '1', 'system:role:edit', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色删除', 101, 4, '#', '', 'F', '0', '1', 'system:role:remove', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色导出', 101, 5, '#', '', 'F', '0', '1', 'system:role:export', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '菜单查询', 102, 1, '#', '', 'F', '0', '1', 'system:menu:list', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1013, '菜单新增', 102, 2, '#', '', 'F', '0', '1', 'system:menu:add', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1014, '菜单修改', 102, 3, '#', '', 'F', '0', '1', 'system:menu:edit', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1015, '菜单删除', 102, 4, '#', '', 'F', '0', '1', 'system:menu:remove', '#', 'admin', '2026-01-07 18:18:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1016, '部门查询', 103, 1, '#', '', 'F', '0', '1', 'system:dept:list', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1017, '部门新增', 103, 2, '#', '', 'F', '0', '1', 'system:dept:add', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1018, '部门修改', 103, 3, '#', '', 'F', '0', '1', 'system:dept:edit', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1019, '部门删除', 103, 4, '#', '', 'F', '0', '1', 'system:dept:remove', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1020, '岗位查询', 104, 1, '#', '', 'F', '0', '1', 'system:post:list', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1021, '岗位新增', 104, 2, '#', '', 'F', '0', '1', 'system:post:add', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1022, '岗位修改', 104, 3, '#', '', 'F', '0', '1', 'system:post:edit', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1023, '岗位删除', 104, 4, '#', '', 'F', '0', '1', 'system:post:remove', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1024, '岗位导出', 104, 5, '#', '', 'F', '0', '1', 'system:post:export', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1025, '字典查询', 105, 1, '#', '', 'F', '0', '1', 'system:dict:list', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1026, '字典新增', 105, 2, '#', '', 'F', '0', '1', 'system:dict:add', '#', 'admin', '2026-01-07 18:18:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1027, '字典修改', 105, 3, '#', '', 'F', '0', '1', 'system:dict:edit', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1028, '字典删除', 105, 4, '#', '', 'F', '0', '1', 'system:dict:remove', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1029, '字典导出', 105, 5, '#', '', 'F', '0', '1', 'system:dict:export', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1030, '参数查询', 106, 1, '#', '', 'F', '0', '1', 'system:config:list', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1031, '参数新增', 106, 2, '#', '', 'F', '0', '1', 'system:config:add', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1032, '参数修改', 106, 3, '#', '', 'F', '0', '1', 'system:config:edit', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1033, '参数删除', 106, 4, '#', '', 'F', '0', '1', 'system:config:remove', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1034, '参数导出', 106, 5, '#', '', 'F', '0', '1', 'system:config:export', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1035, '公告查询', 107, 1, '#', '', 'F', '0', '1', 'system:notice:list', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1036, '公告新增', 107, 2, '#', '', 'F', '0', '1', 'system:notice:add', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1037, '公告修改', 107, 3, '#', '', 'F', '0', '1', 'system:notice:edit', '#', 'admin', '2026-01-07 18:18:08', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1038, '公告删除', 107, 4, '#', '', 'F', '0', '1', 'system:notice:remove', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1039, '操作查询', 500, 1, '#', '', 'F', '0', '1', 'monitor:operlog:list', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1040, '操作删除', 500, 2, '#', '', 'F', '0', '1', 'monitor:operlog:remove', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1041, '详细信息', 500, 3, '#', '', 'F', '0', '1', 'monitor:operlog:detail', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1042, '日志导出', 500, 4, '#', '', 'F', '0', '1', 'monitor:operlog:export', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1043, '登录查询', 501, 1, '#', '', 'F', '0', '1', 'monitor:logininfor:list', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1044, '登录删除', 501, 2, '#', '', 'F', '0', '1', 'monitor:logininfor:remove', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1045, '日志导出', 501, 3, '#', '', 'F', '0', '1', 'monitor:logininfor:export', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1046, '账户解锁', 501, 4, '#', '', 'F', '0', '1', 'monitor:logininfor:unlock', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1047, '在线查询', 109, 1, '#', '', 'F', '0', '1', 'monitor:online:list', '#', 'admin', '2026-01-07 18:18:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1048, '批量强退', 109, 2, '#', '', 'F', '0', '1', 'monitor:online:batchForceLogout', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1049, '单条强退', 109, 3, '#', '', 'F', '0', '1', 'monitor:online:forceLogout', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1050, '任务查询', 110, 1, '#', '', 'F', '0', '1', 'monitor:job:list', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1051, '任务新增', 110, 2, '#', '', 'F', '0', '1', 'monitor:job:add', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1052, '任务修改', 110, 3, '#', '', 'F', '0', '1', 'monitor:job:edit', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1053, '任务删除', 110, 4, '#', '', 'F', '0', '1', 'monitor:job:remove', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1054, '状态修改', 110, 5, '#', '', 'F', '0', '1', 'monitor:job:changeStatus', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1055, '任务详细', 110, 6, '#', '', 'F', '0', '1', 'monitor:job:detail', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1056, '任务导出', 110, 7, '#', '', 'F', '0', '1', 'monitor:job:export', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1057, '生成查询', 115, 1, '#', '', 'F', '0', '1', 'tool:gen:list', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1058, '生成修改', 115, 2, '#', '', 'F', '0', '1', 'tool:gen:edit', '#', 'admin', '2026-01-07 18:18:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1059, '生成删除', 115, 3, '#', '', 'F', '0', '1', 'tool:gen:remove', '#', 'admin', '2026-01-07 18:18:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1060, '预览代码', 115, 4, '#', '', 'F', '0', '1', 'tool:gen:preview', '#', 'admin', '2026-01-07 18:18:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1061, '生成代码', 115, 5, '#', '', 'F', '0', '1', 'tool:gen:code', '#', 'admin', '2026-01-07 18:18:11', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2000, '即时通讯', 0, 5, '/im', '', 'M', '0', '1', '', 'fa fa-comments', 'admin', '2026-01-08 09:27:20', '', NULL, '即时通讯用户端目录');
INSERT INTO `sys_menu` VALUES (2001, '工作台', 2000, 1, '/im/workbench', '', 'C', '0', '1', 'im:workbench:view', 'fa fa-dashboard', 'admin', '2026-01-08 09:27:20', '', NULL, '工作台菜单');
INSERT INTO `sys_menu` VALUES (2002, '聊天', 2000, 2, '/im/chat', '', 'C', '0', '1', 'im:chat:view', 'fa fa-commenting', 'admin', '2026-01-08 09:27:20', '', NULL, '聊天菜单');
INSERT INTO `sys_menu` VALUES (2003, '联系人', 2000, 3, '/im/contacts', '', 'C', '0', '1', 'im:contacts:view', 'fa fa-address-book', 'admin', '2026-01-08 09:27:20', '', NULL, '联系人菜单');
INSERT INTO `sys_menu` VALUES (2004, '群组', 2000, 4, '/im/group', '', 'C', '0', '1', 'im:group:view', 'fa fa-users', 'admin', '2026-01-08 09:27:20', '', NULL, '群组菜单');
INSERT INTO `sys_menu` VALUES (2005, '钉盘', 2000, 5, '/im/drive', '', 'C', '0', '1', 'im:drive:view', 'fa fa-cloud', 'admin', '2026-01-08 09:27:20', '', NULL, '钉盘菜单');
INSERT INTO `sys_menu` VALUES (2006, '审批中心', 2000, 6, '/im/approval', '', 'C', '0', '1', 'im:approval:view', 'fa fa-check-circle', 'admin', '2026-01-08 09:27:20', '', NULL, '审批中心菜单');
INSERT INTO `sys_menu` VALUES (2007, '应用中心', 2000, 7, '/im/app-center', '', 'C', '0', '1', 'im:appcenter:view', 'fa fa-th', 'admin', '2026-01-08 09:27:20', '', NULL, '应用中心菜单');
INSERT INTO `sys_menu` VALUES (2008, '设置', 2000, 8, '/im/settings', '', 'C', '0', '1', 'im:settings:view', 'fa fa-cog', 'admin', '2026-01-08 09:27:20', '', NULL, '设置菜单');
INSERT INTO `sys_menu` VALUES (2010, '工作台查询', 2001, 1, '#', '', 'F', '0', '1', 'im:workbench:list', '#', 'admin', '2026-01-08 09:27:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2011, '工作台操作', 2001, 2, '#', '', 'F', '0', '1', 'im:workbench:operate', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2020, '发送消息', 2002, 1, '#', '', 'F', '0', '1', 'im:chat:send', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2021, '接收消息', 2002, 2, '#', '', 'F', '0', '1', 'im:chat:receive', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2022, '消息撤回', 2002, 3, '#', '', 'F', '0', '1', 'im:chat:revoke', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2030, '联系人查询', 2003, 1, '#', '', 'F', '0', '1', 'im:contacts:list', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2031, '添加联系人', 2003, 2, '#', '', 'F', '0', '1', 'im:contacts:add', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2032, '删除联系人', 2003, 3, '#', '', 'F', '0', '1', 'im:contacts:remove', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2040, '创建群组', 2004, 1, '#', '', 'F', '0', '1', 'im:group:add', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2041, '加入群组', 2004, 2, '#', '', 'F', '0', '1', 'im:group:join', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2042, '退出群组', 2004, 3, '#', '', 'F', '0', '1', 'im:group:leave', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2043, '群组管理', 2004, 4, '#', '', 'F', '0', '1', 'im:group:manage', '#', 'admin', '2026-01-08 09:27:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2050, '文件上传', 2005, 1, '#', '', 'F', '0', '1', 'im:drive:upload', '#', 'admin', '2026-01-08 09:27:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2051, '文件下载', 2005, 2, '#', '', 'F', '0', '1', 'im:drive:download', '#', 'admin', '2026-01-08 09:27:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2052, '文件删除', 2005, 3, '#', '', 'F', '0', '1', 'im:drive:remove', '#', 'admin', '2026-01-08 09:27:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2053, '文件分享', 2005, 4, '#', '', 'F', '0', '1', 'im:drive:share', '#', 'admin', '2026-01-08 09:27:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2060, '发起审批', 2006, 1, '#', '', 'F', '0', '1', 'im:approval:create', '#', 'admin', '2026-01-08 09:27:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2061, '审批处理', 2006, 2, '#', '', 'F', '0', '1', 'im:approval:handle', '#', 'admin', '2026-01-08 09:27:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2062, '审批查询', 2006, 3, '#', '', 'F', '0', '1', 'im:approval:list', '#', 'admin', '2026-01-08 09:27:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2070, '应用安装', 2007, 1, '#', '', 'F', '0', '1', 'im:appcenter:install', '#', 'admin', '2026-01-08 09:27:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2071, '应用卸载', 2007, 2, '#', '', 'F', '0', '1', 'im:appcenter:uninstall', '#', 'admin', '2026-01-08 09:27:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2080, '个人信息', 2008, 1, '#', '', 'F', '0', '1', 'im:settings:profile', '#', 'admin', '2026-01-08 09:27:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2081, '账号安全', 2008, 2, '#', '', 'F', '0', '1', 'im:settings:security', '#', 'admin', '2026-01-08 09:27:23', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2082, '通知设置', 2008, 3, '#', '', 'F', '0', '1', 'im:settings:notification', '#', 'admin', '2026-01-08 09:27:23', '', NULL, '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int(4) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int(2) NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int(1) NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int(1) NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint(20) NULL DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type`) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status`) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (100, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.editSave()', 'POST', 1, 'admin', '研发部门', '/system/menu/edit', '127.0.0.1', '内网IP', '{\"menuId\":[\"4\"],\"parentId\":[\"0\"],\"menuType\":[\"C\"],\"menuName\":[\"若依官网\"],\"url\":[\"http://ruoyi.vip\"],\"target\":[\"menuBlank\"],\"perms\":[\"\"],\"orderNum\":[\"4\"],\"icon\":[\"fa fa-location-arrow\"],\"visible\":[\"1\"],\"isRefresh\":[\"1\"]}', '{\"msg\":\"操作成功\",\"code\":0}', 0, NULL, '2026-01-08 09:22:53', 150);
INSERT INTO `sys_oper_log` VALUES (101, '重置密码', 2, 'com.ruoyi.web.controller.system.SysProfileController.resetPwd()', 'POST', 1, 'admin', '研发部门', '/system/user/profile/resetPwd', '127.0.0.1', '内网IP', '{\"userId\":[\"1\"],\"loginName\":[\"admin\"]}', '{\"msg\":\"操作成功\",\"code\":0}', 0, NULL, '2026-01-08 09:23:07', 112);
INSERT INTO `sys_oper_log` VALUES (102, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.editSave()', 'POST', 1, 'admin', '研发部门', '/system/menu/edit', '127.0.0.1', '内网IP', '{\"menuId\":[\"3\"],\"parentId\":[\"0\"],\"menuType\":[\"M\"],\"menuName\":[\"系统工具\"],\"url\":[\"#\"],\"target\":[\"menuItem\"],\"perms\":[\"\"],\"orderNum\":[\"3\"],\"icon\":[\"fa fa-bars\"],\"visible\":[\"1\"],\"isRefresh\":[\"1\"]}', '{\"msg\":\"操作成功\",\"code\":0}', 0, NULL, '2026-01-08 09:27:50', 98);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int(4) NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2026-01-07 18:18:00', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2026-01-07 18:18:00', '', NULL, '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2026-01-07 18:18:01', '', NULL, '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2026-01-07 18:18:01', '', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(4) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', '0', '0', 'admin', '2026-01-07 18:18:01', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', '0', '0', 'admin', '2026-01-07 18:18:02', '', NULL, '普通角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (2, 100);
INSERT INTO `sys_role_dept` VALUES (2, 101);
INSERT INTO `sys_role_dept` VALUES (2, 105);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (2, 2);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 4);
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (2, 103);
INSERT INTO `sys_role_menu` VALUES (2, 104);
INSERT INTO `sys_role_menu` VALUES (2, 105);
INSERT INTO `sys_role_menu` VALUES (2, 106);
INSERT INTO `sys_role_menu` VALUES (2, 107);
INSERT INTO `sys_role_menu` VALUES (2, 108);
INSERT INTO `sys_role_menu` VALUES (2, 109);
INSERT INTO `sys_role_menu` VALUES (2, 110);
INSERT INTO `sys_role_menu` VALUES (2, 111);
INSERT INTO `sys_role_menu` VALUES (2, 112);
INSERT INTO `sys_role_menu` VALUES (2, 113);
INSERT INTO `sys_role_menu` VALUES (2, 114);
INSERT INTO `sys_role_menu` VALUES (2, 115);
INSERT INTO `sys_role_menu` VALUES (2, 116);
INSERT INTO `sys_role_menu` VALUES (2, 500);
INSERT INTO `sys_role_menu` VALUES (2, 501);
INSERT INTO `sys_role_menu` VALUES (2, 1000);
INSERT INTO `sys_role_menu` VALUES (2, 1001);
INSERT INTO `sys_role_menu` VALUES (2, 1002);
INSERT INTO `sys_role_menu` VALUES (2, 1003);
INSERT INTO `sys_role_menu` VALUES (2, 1004);
INSERT INTO `sys_role_menu` VALUES (2, 1005);
INSERT INTO `sys_role_menu` VALUES (2, 1006);
INSERT INTO `sys_role_menu` VALUES (2, 1007);
INSERT INTO `sys_role_menu` VALUES (2, 1008);
INSERT INTO `sys_role_menu` VALUES (2, 1009);
INSERT INTO `sys_role_menu` VALUES (2, 1010);
INSERT INTO `sys_role_menu` VALUES (2, 1011);
INSERT INTO `sys_role_menu` VALUES (2, 1012);
INSERT INTO `sys_role_menu` VALUES (2, 1013);
INSERT INTO `sys_role_menu` VALUES (2, 1014);
INSERT INTO `sys_role_menu` VALUES (2, 1015);
INSERT INTO `sys_role_menu` VALUES (2, 1016);
INSERT INTO `sys_role_menu` VALUES (2, 1017);
INSERT INTO `sys_role_menu` VALUES (2, 1018);
INSERT INTO `sys_role_menu` VALUES (2, 1019);
INSERT INTO `sys_role_menu` VALUES (2, 1020);
INSERT INTO `sys_role_menu` VALUES (2, 1021);
INSERT INTO `sys_role_menu` VALUES (2, 1022);
INSERT INTO `sys_role_menu` VALUES (2, 1023);
INSERT INTO `sys_role_menu` VALUES (2, 1024);
INSERT INTO `sys_role_menu` VALUES (2, 1025);
INSERT INTO `sys_role_menu` VALUES (2, 1026);
INSERT INTO `sys_role_menu` VALUES (2, 1027);
INSERT INTO `sys_role_menu` VALUES (2, 1028);
INSERT INTO `sys_role_menu` VALUES (2, 1029);
INSERT INTO `sys_role_menu` VALUES (2, 1030);
INSERT INTO `sys_role_menu` VALUES (2, 1031);
INSERT INTO `sys_role_menu` VALUES (2, 1032);
INSERT INTO `sys_role_menu` VALUES (2, 1033);
INSERT INTO `sys_role_menu` VALUES (2, 1034);
INSERT INTO `sys_role_menu` VALUES (2, 1035);
INSERT INTO `sys_role_menu` VALUES (2, 1036);
INSERT INTO `sys_role_menu` VALUES (2, 1037);
INSERT INTO `sys_role_menu` VALUES (2, 1038);
INSERT INTO `sys_role_menu` VALUES (2, 1039);
INSERT INTO `sys_role_menu` VALUES (2, 1040);
INSERT INTO `sys_role_menu` VALUES (2, 1041);
INSERT INTO `sys_role_menu` VALUES (2, 1042);
INSERT INTO `sys_role_menu` VALUES (2, 1043);
INSERT INTO `sys_role_menu` VALUES (2, 1044);
INSERT INTO `sys_role_menu` VALUES (2, 1045);
INSERT INTO `sys_role_menu` VALUES (2, 1046);
INSERT INTO `sys_role_menu` VALUES (2, 1047);
INSERT INTO `sys_role_menu` VALUES (2, 1048);
INSERT INTO `sys_role_menu` VALUES (2, 1049);
INSERT INTO `sys_role_menu` VALUES (2, 1050);
INSERT INTO `sys_role_menu` VALUES (2, 1051);
INSERT INTO `sys_role_menu` VALUES (2, 1052);
INSERT INTO `sys_role_menu` VALUES (2, 1053);
INSERT INTO `sys_role_menu` VALUES (2, 1054);
INSERT INTO `sys_role_menu` VALUES (2, 1055);
INSERT INTO `sys_role_menu` VALUES (2, 1056);
INSERT INTO `sys_role_menu` VALUES (2, 1057);
INSERT INTO `sys_role_menu` VALUES (2, 1058);
INSERT INTO `sys_role_menu` VALUES (2, 1059);
INSERT INTO `sys_role_menu` VALUES (2, 1060);
INSERT INTO `sys_role_menu` VALUES (2, 1061);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
  `login_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户 01注册用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '头像路径',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `salt` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '盐加密',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime NULL DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', 'ba7b2e83c7111de023b1b283e34fa1a2', '7fa6c0', '0', '0', '127.0.0.1', '2026-01-08 11:09:46', '2026-01-08 09:23:09', 'admin', '2026-01-07 18:17:59', '', '2026-01-08 11:09:43', '管理员');
INSERT INTO `sys_user` VALUES (2, 105, 'ry', '若依', '00', 'ry@qq.com', '15666666666', '1', '', '8e6d98b90472783cc73c17047ddccf36', '222222', '0', '0', '127.0.0.1', NULL, NULL, 'admin', '2026-01-07 18:18:00', '', NULL, '测试员');

-- ----------------------------
-- Table structure for sys_user_online
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_online`;
CREATE TABLE `sys_user_online`  (
  `sessionId` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户会话id',
  `login_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录账号',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '在线状态on_line在线off_line离线',
  `start_timestamp` datetime NULL DEFAULT NULL COMMENT 'session创建时间',
  `last_access_time` datetime NULL DEFAULT NULL COMMENT 'session最后访问时间',
  `expire_time` int(5) NULL DEFAULT 0 COMMENT '超时时间，单位为分钟',
  PRIMARY KEY (`sessionId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '在线用户记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_online
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);
INSERT INTO `sys_user_post` VALUES (2, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

-- ----------------------------
-- View structure for im_session
-- ----------------------------
DROP VIEW IF EXISTS `im_session`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `im_session` AS select `cm`.`id` AS `id`,`c`.`type` AS `type`,`cm`.`user_id` AS `user_id`,`u`.`username` AS `user_name`,(case when (`c`.`type` = 'PRIVATE') then (select `u2`.`username` from (`im_user` `u2` join `im_conversation_member` `cm2` on((`u2`.`id` = `cm2`.`user_id`))) where ((`cm2`.`conversation_id` = `cm`.`conversation_id`) and (`cm2`.`user_id` <> `cm`.`user_id`)) limit 1) when (`c`.`type` = 'GROUP') then `c`.`name` else NULL end) AS `target_name`,(case when (`c`.`type` = 'PRIVATE') then (select `cm2`.`user_id` from `im_conversation_member` `cm2` where ((`cm2`.`conversation_id` = `cm`.`conversation_id`) and (`cm2`.`user_id` <> `cm`.`user_id`)) limit 1) when (`c`.`type` = 'GROUP') then `c`.`id` else NULL end) AS `target_id`,(select `m`.`id` from `im_message` `m` where (`m`.`conversation_id` = `cm`.`conversation_id`) order by `m`.`create_time` desc limit 1) AS `last_message_id`,(select `m`.`content` from `im_message` `m` where (`m`.`conversation_id` = `cm`.`conversation_id`) order by `m`.`create_time` desc limit 1) AS `last_message`,`cm`.`unread_count` AS `unread_count`,`cm`.`create_time` AS `create_time`,`cm`.`update_time` AS `update_time` from ((`im_conversation_member` `cm` join `im_conversation` `c` on((`cm`.`conversation_id` = `c`.`id`))) join `im_user` `u` on((`cm`.`user_id` = `u`.`id`))) where (`cm`.`is_deleted` = 0);

SET FOREIGN_KEY_CHECKS = 1;
