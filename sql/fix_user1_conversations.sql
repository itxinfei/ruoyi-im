-- 修复用户1（admin）的会话成员数据
-- 用户1应该是多个会话的成员，这样才能在前端看到这些会话

-- 首先删除用户1现有的会话成员数据（除了conversation_id=1）
DELETE FROM `im_conversation_member` WHERE `user_id` = 1 AND `conversation_id` != 1;

-- 为用户1添加私聊会话成员数据（会话2-5, 10-15）
-- 会话2: 与用户4（王五）的私聊
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (2, 1, 4, NULL, 'MEMBER', 0, 0, 5, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话3: 与用户5（赵六）的私聊
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (3, 1, 5, NULL, 'MEMBER', 0, 0, 7, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话4: 与用户6（孙七）的私聊
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (4, 1, 6, NULL, 'MEMBER', 0, 0, 9, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话5: 与用户7（周八）的私聊
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (5, 1, 7, NULL, 'MEMBER', 0, 0, 11, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话10: 与用户8（吴九）的私聊
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (10, 1, 8, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话11: 与用户9（郑十）的私聊
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (11, 1, 9, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话13: 与用户10（陈一）的私聊
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (13, 1, 10, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话15: 与用户11（林二）的私聊
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (15, 1, 11, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

-- 为用户1添加群聊会话成员数据（会话6-9, 12, 14）
-- 会话6: 技术交流群
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (6, 1, NULL, NULL, 'MEMBER', 0, 0, 14, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话7: 产品讨论群
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (7, 1, NULL, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话8: 项目开发群
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (8, 1, NULL, NULL, 'MEMBER', 0, 0, 18, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话9: 公司全员群
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (9, 1, NULL, NULL, 'MEMBER', 0, 0, 20, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话12: 篮球爱好者
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (12, 1, NULL, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

-- 会话14: 读书分享会
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `peer_user_id`, `alias`, `role`, `is_pinned`, `is_muted`, `unread_count`, `last_read_message_id`, `last_read_time`, `mention_unread_count`, `last_ack_message_id`, `create_time`, `update_time`)
VALUES (14, 1, NULL, NULL, 'MEMBER', 0, 0, 0, NULL, NULL, 0, NULL, NOW(), NOW());

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
