-- 会话成员测试数据
-- 私聊会话成员（张三与李四）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(1, 2, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与王五）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(2, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(2, 3, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与赵六）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(3, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(3, 4, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与孙七）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(4, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(4, 5, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与周八）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(5, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(5, 6, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与吴九）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(6, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(6, 7, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与郑十）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(7, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(7, 8, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与钱十一）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(8, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(8, 9, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与孙十二）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(9, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(9, 10, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 私聊会话成员（张三与李十三）
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(10, 1, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(10, 11, NULL, 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 群聊会话成员 - 技术交流群
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(11, 1, '张三', 'OWNER', 0, 1, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 2, '李四', 'ADMIN', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 3, '王五', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 4, '赵六', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 5, '孙七', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 6, '周八', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 7, '吴九', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 8, '郑十', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 9, '钱十一', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 10, '孙十二', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 11, '李十三', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 12, '王十四', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 13, '赵十五', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 14, '孙十六', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(11, 15, '周十七', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 群聊会话成员 - 家庭群
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(12, 1, '张三', 'OWNER', 0, 1, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(12, 9, '钱十一', 'ADMIN', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(12, 16, '张爸爸', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(12, 17, '张妈妈', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(12, 18, '张弟弟', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(12, 19, '张妹妹', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 群聊会话成员 - 大学同学群
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(13, 2, '李四', 'OWNER', 0, 1, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(13, 1, '张三', 'ADMIN', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(13, 3, '王五', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(13, 4, '赵六', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(13, 11, '李十三', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(13, 12, '王十四', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(13, 13, '赵十五', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(13, 14, '孙十六', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 群聊会话成员 - 游戏开黑群
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(14, 7, '吴九', 'OWNER', 0, 1, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(14, 1, '张三', 'ADMIN', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(14, 5, '孙七', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(14, 10, '孙十二', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(14, 15, '周十七', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(14, 20, '陈二十', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 群聊会话成员 - 篮球爱好者
INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `nickname`, `role`, `unread_count`, `is_pinned`, `is_muted`, `is_deleted`, `create_time`, `update_time`) VALUES
(15, 6, '周八', 'OWNER', 0, 1, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(15, 1, '张三', 'ADMIN', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(15, 3, '王五', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(15, 8, '郑十', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(15, 12, '王十四', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(15, 17, '张妈妈', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(15, 19, '张妹妹', 'MEMBER', 0, 0, 0, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');
