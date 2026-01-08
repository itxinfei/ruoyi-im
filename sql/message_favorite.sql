-- ============================================
-- 消息收藏表
-- 用于用户收藏重要消息
-- ============================================

DROP TABLE IF EXISTS `im_message_favorite`;

CREATE TABLE `im_message_favorite` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `message_id` BIGINT(20) NOT NULL COMMENT '消息ID',
  `conversation_id` BIGINT(20) NOT NULL COMMENT '会话ID',
  `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '收藏备注',
  `tags` VARCHAR(200) NULL DEFAULT NULL COMMENT '标签（多个标签用逗号分隔）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`ID`) USING BTREE,
  UNIQUE INDEX `uk_user_message` (`user_id`, `message_id`) USING BTREE COMMENT '同一用户对同一消息只能收藏一次',
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_conversation_id` (`conversation_id`) USING BTREE,
  INDEX `idx_create_time` (`create_time`) USING BTREE,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `im_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_message` FOREIGN KEY (`message_id`) REFERENCES `im_message` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `im_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '消息收藏表' ROW_FORMAT = DYNAMIC;

-- ============================================
-- 初始化测试数据
-- ============================================

-- 用户1收藏一些消息（假设已有消息数据）
INSERT INTO `im_message_favorite` (`user_id`, `message_id`, `conversation_id`, `remark`, `tags`)
VALUES
  (1, 1, 1, '重要通知', '工作,通知'),
  (1, 6, 3, '会议提醒', '会议,重要')
ON DUPLICATE KEY UPDATE `remark` = VALUES(`remark`);

SELECT '消息收藏表创建完成！' AS message;
SELECT COUNT(*) AS favorite_count FROM `im_message_favorite`;
