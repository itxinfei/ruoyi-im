-- 张三的会话测试数据
-- 私聊会话
INSERT INTO `im_conversation` (`id`, `type`, `name`, `avatar`, `owner_id`, `description`, `max_members`, `is_deleted`, `create_time`, `update_time`) VALUES
(1, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(2, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(3, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(4, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(5, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(6, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(7, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(8, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(9, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(10, 'PRIVATE', NULL, NULL, NULL, NULL, 2, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');

-- 群聊会话
INSERT INTO `im_conversation` (`id`, `type`, `name`, `avatar`, `owner_id`, `description`, `max_members`, `is_deleted`, `create_time`, `update_time`) VALUES
(11, 'GROUP', '技术交流群', 'https://example.com/group/tech.jpg', 1, '技术交流学习群', 500, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(12, 'GROUP', '家庭群', 'https://example.com/group/family.jpg', 1, '温馨家庭群', 50, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(13, 'GROUP', '大学同学群', 'https://example.com/group/classmates.jpg', 2, '大学同学聚会群', 200, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(14, 'GROUP', '游戏开黑群', 'https://example.com/group/game.jpg', 7, '游戏开黑群', 100, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00'),
(15, 'GROUP', '篮球爱好者', 'https://example.com/group/basketball.jpg', 6, '篮球运动交流群', 100, 0, '2026-01-07 10:00:00', '2026-01-07 10:00:00');
