-- 方案2：修改admin用户ID，让其与测试数据兼容
-- 注意：这个方案需要谨慎使用，可能影响现有的权限系统

-- 1. 先备份原admin用户
CREATE TABLE `im_user_backup` AS SELECT * FROM `im_user` WHERE id = 1;

-- 2. 删除原admin用户
DELETE FROM `im_user` WHERE id = 1;

-- 3. 插入新的admin用户，使用ID=1000（避免与测试数据冲突）
INSERT INTO `im_user` (`id`, `username`, `password`, `nickname`, `email`, `phone`, `avatar`, `status`, `create_time`, `update_time`) VALUES
(1000, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', '13800000000', '/avatar/admin.jpg', 'ACTIVE', '2024-01-01 00:00:00', '2024-12-31 10:00:00');

-- 4. 为新admin添加与测试用户的关联
INSERT INTO `im_friend` (`id`, `user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(3001, 1000, 1001, '张三', '测试用户1', 'NORMAL', '2024-12-31 10:00:00', '2024-12-31 10:00:00'),
(3002, 1000, 1002, '李四', '测试用户2', 'NORMAL', '2024-12-31 10:00:00', '2024-12-31 10:00:00'),
(3003, 1000, 1003, '王五', '测试用户3', 'NORMAL', '2024-12-31 10:00:00', '2024-12-31 10:00:00');

-- 反向关系
INSERT INTO `im_friend` (`id`, `user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(3004, 1001, 1000, '管理员', '系统管理员', 'NORMAL', '2024-12-31 10:00:00', '2024-12-31 10:00:00'),
(3005, 1002, 1000, '管理员', '系统管理员', 'NORMAL', '2024-12-31 10:00:00', '2024-12-31 10:00:00'),
(3006, 1003, 1000, '管理员', '系统管理员', 'NORMAL', '2024-12-31 10:00:00', '2024-12-31 10:00:00');