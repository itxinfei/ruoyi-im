-- ============================================
-- 会话测试数据初始化脚本
-- 用于解决会话列表显示"暂无消息"的问题
-- ============================================

-- 清理旧数据（可选）
-- DELETE FROM im_conversation_member WHERE user_id IN (2, 3, 4, 5);
-- DELETE FROM im_conversation WHERE id IN (1, 2, 3, 4, 5);
-- DELETE FROM im_message WHERE id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

-- ============================================
-- 1. 创建会话数据
-- ============================================

-- 单聊会话1：用户2和用户3
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (1, 'PRIVATE', 3, '李四', '/avatar/user3.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 单聊会话2：用户2和用户4
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (2, 'PRIVATE', 4, '王五', '/avatar/user4.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 群聊会话1：项目讨论组
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (3, 'GROUP', 1, '项目讨论组', '/avatar/group1.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 群聊会话2：技术交流群
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (4, 'GROUP', 2, '技术交流群', '/avatar/group2.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 单聊会话3：用户2和用户5
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`)
VALUES (5, 'PRIVATE', 5, '赵六', '/avatar/user5.jpg', NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 2. 创建会话成员数据
-- ============================================

-- 用户2参与的会话（zhangsan作为主要用户）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (1, 2, 'OWNER', 2, 1, 0, NOW(), NOW()),
  (2, 2, 'OWNER', 0, 0, 0, NOW(), NOW()),
  (3, 2, 'ADMIN', 5, 1, 0, NOW(), NOW()),
  (4, 2, 'MEMBER', 1, 0, 1, NOW(), NOW()),
  (5, 2, 'OWNER', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 用户3参与的会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (1, 3, 'MEMBER', 0, 0, 0, NOW(), NOW()),
  (3, 3, 'OWNER', 0, 1, 0, NOW(), NOW()),
  (4, 3, 'ADMIN', 2, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 用户4参与的会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (2, 4, 'MEMBER', 1, 0, 0, NOW(), NOW()),
  (3, 4, 'MEMBER', 0, 0, 0, NOW(), NOW()),
  (4, 4, 'MEMBER', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 用户5参与的会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (5, 5, 'MEMBER', 0, 0, 0, NOW(), NOW()),
  (3, 5, 'MEMBER', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- 用户6参与的会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `unread_count`, `is_pinned`, `is_muted`, `create_time`, `update_time`)
VALUES
  (3, 6, 'MEMBER', 0, 0, 0, NOW(), NOW()),
  (4, 6, 'MEMBER', 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ============================================
-- 3. 创建一些测试消息
-- ============================================

INSERT INTO `im_message` (`id`, `conversation_id`, `sender_id`, `content`, `message_type`, `create_time`)
VALUES
  (1, 1, 3, '你好，最近怎么样？', 'TEXT', NOW()),
  (2, 1, 2, '挺好的，谢谢关心！你呢？', 'TEXT', NOW()),
  (3, 1, 3, '我也不错，今天天气真好', 'TEXT', NOW()),
  (4, 1, 2, '是啊，适合出去走走', 'TEXT', NOW()),
  (5, 2, 4, '项目进度怎么样了？', 'TEXT', NOW()),
  (6, 3, 3, '@所有人 今天下午3点开会', 'TEXT', NOW()),
  (7, 3, 2, '收到，我会准时参加', 'TEXT', NOW()),
  (8, 4, 3, '这个技术方案怎么样？', 'TEXT', NOW()),
  (9, 5, 5, '文件已经发给你了，请查收', 'TEXT', NOW()),
  (10, 3, 4, '会议延期到明天上午', 'TEXT', NOW())
ON DUPLICATE KEY UPDATE `content` = VALUES(`content`);

-- ============================================
-- 4. 更新会话的最后消息信息
-- ============================================

UPDATE `im_conversation` SET
  `last_message_id` = 4,
  `last_message_time` = (SELECT `create_time` FROM (SELECT `create_time` FROM `im_message` WHERE `id` = 4) AS tmp),
  `update_time` = NOW()
WHERE `id` = 1;

UPDATE `im_conversation` SET
  `last_message_id` = 5,
  `last_message_time` = (SELECT `create_time` FROM (SELECT `create_time` FROM `im_message` WHERE `id` = 5) AS tmp),
  `update_time` = NOW()
WHERE `id` = 2;

UPDATE `im_conversation` SET
  `last_message_id` = 10,
  `last_message_time` = (SELECT `create_time` FROM (SELECT `create_time` FROM `im_message` WHERE `id` = 10) AS tmp),
  `update_time` = NOW()
WHERE `id` = 3;

UPDATE `im_conversation` SET
  `last_message_id` = 8,
  `last_message_time` = (SELECT `create_time` FROM (SELECT `create_time` FROM `im_message` WHERE `id` = 8) AS tmp),
  `update_time` = NOW()
WHERE `id` = 4;

UPDATE `im_conversation` SET
  `last_message_id` = 9,
  `last_message_time` = (SELECT `create_time` FROM (SELECT `create_time` FROM `im_message` WHERE `id` = 9) AS tmp),
  `update_time` = NOW()
WHERE `id` = 5;

-- ============================================
-- 5. 设置会话成员的最后已读消息ID
-- ============================================

UPDATE `im_conversation_member` SET
  `last_read_message_id` = 2,
  `last_read_time` = (SELECT `create_time` FROM (SELECT `create_time` FROM `im_message` WHERE `id` = 2) AS tmp)
WHERE `conversation_id` = 1 AND `user_id` = 2;

UPDATE `im_conversation_member` SET
  `last_read_message_id` = 4,
  `last_read_time` = (SELECT `create_time` FROM (SELECT `create_time` FROM `im_message` WHERE `id` = 4) AS tmp)
WHERE `conversation_id` = 1 AND `user_id` = 3;

-- ============================================
-- 执行完成提示
-- ============================================

SELECT '会话测试数据初始化完成！' AS message;
SELECT COUNT(*) AS conversation_count FROM `im_conversation`;
SELECT COUNT(*) AS member_count FROM `im_conversation_member`;
SELECT COUNT(*) AS message_count FROM `im_message`;
