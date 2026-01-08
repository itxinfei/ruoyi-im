-- ============================================
-- 外部联系人表
-- 用于管理企业外部的联系人（客户、合作伙伴等）
-- ============================================

DROP TABLE IF EXISTS `im_external_contact`;
DROP TABLE IF EXISTS `im_external_contact_group`;

-- 外部联系人分组表
CREATE TABLE `im_external_contact_group` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分组名称',
  `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_sort_order` (`sort_order`) USING BTREE,
  CONSTRAINT `fk_external_group_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '外部联系人分组表' ROW_FORMAT = DYNAMIC;

-- 外部联系人表
CREATE TABLE `im_external_contact` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
  `group_id` BIGINT(20) NULL DEFAULT NULL COMMENT '分组ID',
  `name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
  `company` VARCHAR(100) NULL DEFAULT NULL COMMENT '公司名称',
  `position` VARCHAR(50) NULL DEFAULT NULL COMMENT '职位',
  `mobile` VARCHAR(20) NULL DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
  `wechat` VARCHAR(50) NULL DEFAULT NULL COMMENT '微信号',
  `address` VARCHAR(200) NULL DEFAULT NULL COMMENT '地址',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
  `tags` VARCHAR(200) NULL DEFAULT NULL COMMENT '标签（多个标签用逗号分隔）',
  `avatar` VARCHAR(200) NULL DEFAULT NULL COMMENT '头像',
  `is_starred` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否星标（0否 1是）',
  `source` VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '来源（MANUAL手动 IMPORT导入 SHARE分享）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_group_id` (`group_id`) USING BTREE,
  INDEX `idx_mobile` (`mobile`) USING BTREE,
  INDEX `idx_email` (`email`) USING BTREE,
  INDEX `idx_is_starred` (`is_starred`) USING BTREE,
  CONSTRAINT `fk_external_contact_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_external_contact_group` FOREIGN KEY (`group_id`) REFERENCES `im_external_contact_group` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '外部联系人表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 初始化测试数据
-- ============================================

-- 创建默认分组
INSERT INTO `im_external_contact_group` (`user_id`, `name`, `sort_order`)
VALUES
  (2, '客户', 1),
  (2, '供应商', 2),
  (2, '合作伙伴', 3),
  (3, '客户', 1);

-- 添加测试联系人
INSERT INTO `im_external_contact` (`user_id`, `group_id`, `name`, `company`, `position`, `mobile`, `email`, `tags`, `is_starred`)
VALUES
  (2, 1, '张三', 'ABC科技有限公司', '产品经理', '13800138001', 'zhangsan@abc.com', '重要客户', 1),
  (2, 1, '李四', 'XYZ集团', '采购总监', '13800138002', 'lisi@xyz.com', '潜在客户', 0),
  (2, 2, '王五', '供应商A公司', '销售经理', '13800138003', 'wangwu@supplier.com', '供应商', 0),
  (2, 3, '赵六', '战略合作伙伴', 'CEO', '13800138004', 'zhaoliu@partner.com', '合作伙伴,VIP', 1);

SELECT '外部联系人表创建完成！' AS message;
SELECT COUNT(*) AS group_count FROM `im_external_contact_group`;
SELECT COUNT(*) AS contact_count FROM `im_external_contact`;
