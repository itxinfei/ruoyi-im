-- ============================================
-- 会话测试数据初始化脚本
-- 用于解决会话列表显示"暂无消息"的问题
-- ============================================

-- 清理旧数据（可选）
-- DELETE FROM im_conversation_member WHERE user_id IN (1, 2, 3);
-- DELETE FROM im_conversation WHERE id IN (1, 2, 3, 4, 5);

-- ============================================
-- 1. 创建会话数据
-- ============================================

-- 单聊会话1：用户1和用户2
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (1, 'PRIVATE', 2, '张三', '/avatar/user2.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 单聊会话2：用户1和用户3
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (2, 'PRIVATE', 3, '李四', '/avatar/user3.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 群聊会话1：项目讨论组
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (3, 'GROUP', 1, '项目讨论组', '/avatar/group1.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 群聊会话2：技术交流群
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (4, 'GROUP', 2, '技术交流群', '/avatar/group2.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 单聊会话3：用户1和用户4
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (5, 'PRIVATE', 4, '王五', '/avatar/user4.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 2. 创建会话成员数据
-- ============================================

-- 用户1参与的会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (1, 1, 'OWNER', 2, 1, 0, NOW(), NOW()),
  (2, 1, 'OWNER', 0, 0, 0, NOW(), NOW()),
  (3, 1, 'ADMIN', 5, 1, 0, NOW(), NOW()),
  (4, 1, 'MEMBER', 1, 0, 1, NOW(), NOW()),
  (5, 1, 'OWNER', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 用户2参与的会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (1, 2, 'MEMBER', 0, 0, 0, NOW(), NOW()),
  (3, 2, 'OWNER', 0, 1, 0, NOW(), NOW()),
  (4, 2, 'ADMIN', 2, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 用户3参与的会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (2, 3, 'MEMBER', 1, 0, 0, NOW(), NOW()),
  (3, 3, 'MEMBER', 0, 0, 0, NOW(), NOW()),
  (4, 3, 'MEMBER', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 用户4参与的会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (5, 4, 'MEMBER', 0, 0, 0, NOW(), NOW()),
  (3, 4, 'MEMBER', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 3. 创建一些测试消息
-- ============================================

INSERT INTO `im_message` (`id`, `conversation_id`, `sender_id`, `content`, `message_type`, `status`, `send_time`, `create_time`, `update_time`)
VALUES
  (1, 1, 2, '你好，最近怎么样？', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (2, 1, 1, '挺好的，谢谢关心！你呢？', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (3, 1, 2, '我也不错，今天天气真好', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (4, 1, 1, '是啊，适合出去走走', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (5, 2, 3, '项目进度怎么样了？', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (6, 3, 2, '@所有人 今天下午3点开会', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (7, 3, 1, '收到，我会准时参加', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (8, 4, 2, '这个技术方案怎么样？', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (9, 5, 4, '文件已经发给你了，请查收', 'TEXT', 'SENT', NOW(), NOW(), NOW()),
  (10, 3, 3, '会议延期到明天上午', 'TEXT', 'SENT', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 4. 更新会话的最后消息信息
-- ============================================

UPDATE `im_conversation` SET
  `last_message_id` = 4,
  `last_message_time` = (SELECT `send_time` FROM (SELECT `send_time` FROM `im_message` WHERE `id` = 4) AS tmp),
  `update_time` = NOW()
WHERE `id` = 1;

UPDATE `im_conversation` SET
  `last_message_id` = 5,
  `last_message_time` = (SELECT `send_time` FROM (SELECT `send_time` FROM `im_message` WHERE `id` = 5) AS tmp),
  `update_time` = NOW()
WHERE `id` = 2;

UPDATE `im_conversation` SET
  `last_message_id` = 10,
  `last_message_time` = (SELECT `send_time` FROM (SELECT `send_time` FROM `im_message` WHERE `id` = 10) AS tmp),
  `update_time` = NOW()
WHERE `id` = 3;

UPDATE `im_conversation` SET
  `last_message_id` = 8,
  `last_message_time` = (SELECT `send_time` FROM (SELECT `send_time` FROM `im_message` WHERE `id` = 8) AS tmp),
  `update_time` = NOW()
WHERE `id` = 4;

UPDATE `im_conversation` SET
  `last_message_id` = 9,
  `last_message_time` = (SELECT `send_time` FROM (SELECT `send_time` FROM `im_message` WHERE `id` = 9) AS tmp),
  `update_time` = NOW()
WHERE `id` = 5;

-- ============================================
-- 5. 设置会话成员的最后已读消息ID
-- ============================================

UPDATE `im_conversation_member` SET
  `last_read_message_id` = 2,
  `last_read_time` = (SELECT `send_time` FROM (SELECT `send_time` FROM `im_message` WHERE `id` = 2) AS tmp)
WHERE `conversation_id` = 1 AND `user_id` = 1;

UPDATE `im_conversation_member` SET
  `last_read_message_id` = 4,
  `last_read_time` = (SELECT `send_time` FROM (SELECT `send_time` FROM `im_message` WHERE `id` = 4) AS tmp)
WHERE `conversation_id` = 1 AND `user_id` = 2;

-- ============================================
-- 执行完成提示
-- ============================================

SELECT '会话测试数据初始化完成！' AS message;
SELECT COUNT(*) AS conversation_count FROM `im_conversation`;
SELECT COUNT(*) AS member_count FROM `im_conversation_member`;
SELECT COUNT(*) AS message_count FROM `im_message`;
