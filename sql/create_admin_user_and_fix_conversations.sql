-- 创建管理员用户（ID=1）
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO `im_user` (`id`, `username`, `password`, `nickname`, `avatar`, `sex`, `phone`, `email`, `status`, `sign`, `last_login_time`, `create_time`, `update_time`)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', '/avatar/1.jpg', 1, '13800000001', 'admin@example.com', 1, '系统管理员', NULL, NOW(), NOW());

-- 然后再为用户1添加会话成员数据
-- 首先删除用户1现有的会话成员数据（如果有）
DELETE FROM `im_conversation_member` WHERE `user_id` = 1;

-- 为用户1添加私聊会话成员数据
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (1, 1, NULL, 'MEMBER', 1, 0, 2, NULL, '2024-01-01 09:01:00', 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (2, 1, NULL, 'MEMBER', 0, 0, 5, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (3, 1, NULL, 'MEMBER', 0, 0, 7, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (4, 1, NULL, 'MEMBER', 0, 0, 9, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (5, 1, NULL, 'MEMBER', 0, 0, 11, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (10, 1, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (11, 1, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (13, 1, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (15, 1, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

-- 群聊会话
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (6, 1, NULL, 'MEMBER', 0, 0, 14, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (7, 1, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (8, 1, NULL, 'MEMBER', 0, 0, 18, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (9, 1, NULL, 'MEMBER', 0, 0, 20, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (12, 1, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `is_deleted`, `deleted_time`, `create_time`, `update_time`)
VALUES (14, 1, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

-- 验证数据
SELECT
    cm.conversation_id,
    c.type,
    c.name,
    cm.user_id,
    cm.role,
    cm.unread_count
FROM `im_conversation_member` cm
LEFT JOIN `im_conversation` c ON cm.conversation_id = c.id
WHERE cm.user_id = 1
ORDER BY cm.conversation_id;
