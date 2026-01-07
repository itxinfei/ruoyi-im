-- ----------------------------
-- Records of im_user
-- ----------------------------
-- 密码统一使用 BCrypt 加密，明文密码为: 123456
-- 加密后的密码: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO `im_user` (`username`, `password`, `nickname`, `avatar`, `gender`, `mobile`, `email`, `status`, `signature`, `last_online_time`, `create_time`, `update_time`) VALUES
('zhangsan', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张三', 'https://example.com/avatar/zhangsan.jpg', 1, '13800138001', 'zhangsan@example.com', 1, '热爱生活，热爱编程', '2026-01-07 10:00:00', '2026-01-01 09:00:00', '2026-01-07 10:00:00'),
('lisi', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李四', 'https://example.com/avatar/lisi.jpg', 1, '13800138002', 'lisi@example.com', 1, '代码改变世界', '2026-01-07 09:30:00', '2026-01-01 10:00:00', '2026-01-07 09:30:00'),
('wangwu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王五', 'https://example.com/avatar/wangwu.jpg', 1, '13800138003', 'wangwu@example.com', 1, '简单就是美', '2026-01-07 08:45:00', '2026-01-02 11:00:00', '2026-01-07 08:45:00'),
('zhaoliu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '赵六', 'https://example.com/avatar/zhaoliu.jpg', 2, '13800138004', 'zhaoliu@example.com', 1, '保持学习，保持进步', '2026-01-06 23:00:00', '2026-01-03 12:00:00', '2026-01-06 23:00:00'),
('sunqi', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '孙七', 'https://example.com/avatar/sunqi.jpg', 1, '13800138005', 'sunqi@example.com', 1, '技术改变生活', '2026-01-07 07:20:00', '2026-01-04 13:00:00', '2026-01-07 07:20:00'),
('zhouba', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '周八', 'https://example.com/avatar/zhouba.jpg', 2, '13800138006', 'zhouba@example.com', 1, '每天进步一点点', '2026-01-06 22:15:00', '2026-01-05 14:00:00', '2026-01-06 22:15:00'),
('wujiu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '吴九', 'https://example.com/avatar/wujiu.jpg', 1, '13800138007', 'wujiu@example.com', 1, '追求卓越', '2026-01-07 06:50:00', '2026-01-06 15:00:00', '2026-01-07 06:50:00'),
('zhengshi', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '郑十', 'https://example.com/avatar/zhengshi.jpg', 2, '13800138008', 'zhengshi@example.com', 1, '永不放弃', '2026-01-06 21:30:00', '2026-01-07 16:00:00', '2026-01-06 21:30:00'),
('alice', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice', 'https://example.com/avatar/alice.jpg', 2, '13900139001', 'alice@example.com', 1, 'Hello World!', '2026-01-07 05:40:00', '2026-01-01 17:00:00', '2026-01-07 05:40:00'),
('bob', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'https://example.com/avatar/bob.jpg', 1, '13900139002', 'bob@example.com', 1, 'Code is poetry', '2026-01-06 20:00:00', '2026-01-02 18:00:00', '2026-01-06 20:00:00'),
('charlie', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Charlie', 'https://example.com/avatar/charlie.jpg', 1, '13900139003', 'charlie@example.com', 1, 'Stay hungry, stay foolish', '2026-01-07 04:30:00', '2026-01-03 19:00:00', '2026-01-07 04:30:00'),
('david', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'David', 'https://example.com/avatar/david.jpg', 1, '13900139004', 'david@example.com', 1, 'Make it happen', '2026-01-06 19:45:00', '2026-01-04 20:00:00', '2026-01-06 19:45:00'),
('emma', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Emma', 'https://example.com/avatar/emma.jpg', 2, '13900139005', 'emma@example.com', 1, 'Dream big', '2026-01-07 03:20:00', '2026-01-05 21:00:00', '2026-01-07 03:20:00'),
('frank', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Frank', 'https://example.com/avatar/frank.jpg', 1, '13900139006', 'frank@example.com', 1, 'Work hard, play hard', '2026-01-06 18:30:00', '2026-01-06 22:00:00', '2026-01-06 18:30:00'),
('grace', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Grace', 'https://example.com/avatar/grace.jpg', 2, '13900139007', 'grace@example.com', 1, 'Be yourself', '2026-01-07 02:10:00', '2026-01-07 23:00:00', '2026-01-07 02:10:00'),
('henry', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Henry', 'https://example.com/avatar/henry.jpg', 1, '13900139008', 'henry@example.com', 1, 'Never give up', '2026-01-06 17:00:00', '2026-01-01 08:00:00', '2026-01-06 17:00:00'),
('iris', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Iris', 'https://example.com/avatar/iris.jpg', 2, '13700137001', 'iris@example.com', 1, 'Live life to the fullest', '2026-01-07 01:00:00', '2026-01-02 09:00:00', '2026-01-07 01:00:00'),
('jack', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jack', 'https://example.com/avatar/jack.jpg', 1, '13700137002', 'jack@example.com', 1, 'Just do it', '2026-01-06 16:15:00', '2026-01-03 10:00:00', '2026-01-06 16:15:00'),
('kate', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Kate', 'https://example.com/avatar/kate.jpg', 2, '13700137003', 'kate@example.com', 1, 'Believe in yourself', '2026-01-06 23:50:00', '2026-01-04 11:00:00', '2026-01-06 23:50:00'),
('leo', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Leo', 'https://example.com/avatar/leo.jpg', 1, '13700137004', 'leo@example.com', 1, 'Keep moving forward', '2026-01-06 15:40:00', '2026-01-05 12:00:00', '2026-01-06 15:40:00');
