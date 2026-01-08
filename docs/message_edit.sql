-- ============================================
-- 消息编辑功能
-- 用于编辑已发送的消息
-- ============================================

-- 添加消息编辑相关字段到im_message表
ALTER TABLE `im_message`
ADD COLUMN `is_edited` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已编辑（0否 1是）' AFTER `is_revoked`,
ADD COLUMN `edited_content` TEXT NULL DEFAULT NULL COMMENT '编辑后的内容' AFTER `is_edited`,
ADD COLUMN `edit_count` INT(11) NOT NULL DEFAULT 0 COMMENT '编辑次数' AFTER `edited_content`,
ADD COLUMN `edit_time` DATETIME NULL DEFAULT NULL COMMENT '最后编辑时间' AFTER `edit_count`;

-- 创建消息编辑历史表
DROP TABLE IF EXISTS `im_message_edit_history`;
CREATE TABLE `im_message_edit_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `message_id` BIGINT(20) NOT NULL COMMENT '消息ID',
  `old_content` TEXT NULL DEFAULT NULL COMMENT '编辑前的内容',
  `new_content` TEXT NULL DEFAULT NULL COMMENT '编辑后的内容',
  `editor_id` BIGINT(20) NOT NULL COMMENT '编辑人ID',
  `edit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_id` (`message_id`) USING BTREE,
  INDEX `idx_editor_id` (`editor_id`) USING BTREE,
  INDEX `idx_edit_time` (`edit_time`) USING BTREE,
  CONSTRAINT `fk_edit_history_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_edit_history_editor` FOREIGN KEY (`editor_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息编辑历史表' ROW_FORMAT = DYNAMIC;

-- 添加编辑记录索引
ALTER TABLE `im_message`
ADD INDEX `idx_is_edited` (`is_edited`) USING BTREE;

SELECT '消息编辑功能表创建完成！' AS message;
