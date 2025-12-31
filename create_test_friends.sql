-- 添加测试账号好友关系
-- 好友关系是双向的，需要同时添加两条记录

-- admin账号添加所有test账号为好友
INSERT INTO `im_friend` (`user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1, 1000, 'Test User 1', 'Test Account', 'NORMAL', NOW(), NOW()),
(1, 1001, 'Test User 2', 'Test Account', 'NORMAL', NOW(), NOW()),
(1, 1002, 'Test User 3', 'Test Account', 'NORMAL', NOW(), NOW()),
(1, 1003, 'Test User 4', 'Test Account', 'NORMAL', NOW(), NOW()),
(1, 1004, 'Test User 5', 'Test Account', 'NORMAL', NOW(), NOW());

-- test账号添加admin为好友
INSERT INTO `im_friend` (`user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1000, 1, 'Admin', 'System Admin', 'NORMAL', NOW(), NOW()),
(1001, 1, 'Admin', 'System Admin', 'NORMAL', NOW(), NOW()),
(1002, 1, 'Admin', 'System Admin', 'NORMAL', NOW(), NOW()),
(1003, 1, 'Admin', 'System Admin', 'NORMAL', NOW(), NOW()),
(1004, 1, 'Admin', 'System Admin', 'NORMAL', NOW(), NOW());

-- test账号之间的好友关系
-- test1 添加 test2, test3 为好友
INSERT INTO `im_friend` (`user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1000, 1001, 'Test User 2', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1000, 1002, 'Test User 3', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1001, 1000, 'Test User 1', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1002, 1000, 'Test User 1', 'Test Partner', 'NORMAL', NOW(), NOW());

-- test2 添加 test3, test4 为好友
INSERT INTO `im_friend` (`user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1001, 1002, 'Test User 3', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1001, 1003, 'Test User 4', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1002, 1001, 'Test User 2', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1003, 1001, 'Test User 2', 'Test Partner', 'NORMAL', NOW(), NOW());

-- test3 添加 test4, test5 为好友
INSERT INTO `im_friend` (`user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1002, 1003, 'Test User 4', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1002, 1004, 'Test User 5', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1003, 1002, 'Test User 3', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1004, 1002, 'Test User 3', 'Test Partner', 'NORMAL', NOW(), NOW());

-- test4 添加 test5 为好友
INSERT INTO `im_friend` (`user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1003, 1004, 'Test User 5', 'Test Partner', 'NORMAL', NOW(), NOW()),
(1004, 1003, 'Test User 4', 'Test Partner', 'NORMAL', NOW(), NOW());

-- 原有账号之间的好友关系补充
-- zhangsan (id=2) 添加所有test账号为好友
INSERT INTO `im_friend` (`user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(2, 1000, 'Test User 1', 'Test Account', 'NORMAL', NOW(), NOW()),
(2, 1001, 'Test User 2', 'Test Account', 'NORMAL', NOW(), NOW()),
(2, 1002, 'Test User 3', 'Test Account', 'NORMAL', NOW(), NOW()),
(2, 1003, 'Test User 4', 'Test Account', 'NORMAL', NOW(), NOW()),
(2, 1004, 'Test User 5', 'Test Account', 'NORMAL', NOW(), NOW());

-- test账号添加zhangsan为好友
INSERT INTO `im_friend` (`user_id`, `friend_user_id`, `alias`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1000, 2, 'Zhangsan', 'Original User', 'NORMAL', NOW(), NOW()),
(1001, 2, 'Zhangsan', 'Original User', 'NORMAL', NOW(), NOW()),
(1002, 2, 'Zhangsan', 'Original User', 'NORMAL', NOW(), NOW()),
(1003, 2, 'Zhangsan', 'Original User', 'NORMAL', NOW(), NOW()),
(1004, 2, 'Zhangsan', 'Original User', 'NORMAL', NOW(), NOW());
