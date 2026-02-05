-- 创建测试群组并添加成员
SET NAMES utf8mb4;

-- 1. 创建群组1
INSERT INTO im_conversation (type, target_id, name, avatar, create_time, update_time)
VALUES ('GROUP', 1001, 'Test Group', '/default/group.png', NOW(), NOW());

SET @group1_id = LAST_INSERT_ID();

-- 2. 创建群组2
INSERT INTO im_conversation (type, target_id, name, avatar, create_time, update_time)
VALUES ('GROUP', 1002, 'Tech Discussion', '/default/group.png', NOW(), NOW());

SET @group2_id = LAST_INSERT_ID();

-- 3. 创建群组3
INSERT INTO im_conversation (type, target_id, name, avatar, create_time, update_time)
VALUES ('GROUP', 1003, 'Project Team', '/default/group.png', NOW(), NOW());

SET @group3_id = LAST_INSERT_ID();

-- 获取用户ID
SET @zhangsan_id = 23;
SET @lisi_id = 24;

-- 为群组1添加成员 (zhangsan作为群主)
INSERT INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
VALUES 
(@group1_id, @zhangsan_id, NULL, 'OWNER', NOW(), NOW()),
(@group1_id, @lisi_id, NULL, 'MEMBER', NOW(), NOW()),
(@group1_id, 9, NULL, 'MEMBER', NOW(), NOW()),
(@group1_id, 10, NULL, 'MEMBER', NOW(), NOW()),
(@group1_id, 11, NULL, 'MEMBER', NOW(), NOW()),
(@group1_id, 25, NULL, 'MEMBER', NOW(), NOW());

-- 为群组2添加成员
INSERT INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
VALUES 
(@group2_id, @zhangsan_id, NULL, 'OWNER', NOW(), NOW()),
(@group2_id, @lisi_id, NULL, 'MEMBER', NOW(), NOW()),
(@group2_id, 12, NULL, 'MEMBER', NOW(), NOW()),
(@group2_id, 26, NULL, 'MEMBER', NOW(), NOW()),
(@group2_id, 27, NULL, 'MEMBER', NOW(), NOW());

-- 为群组3添加成员 (lisi作为群主)
INSERT INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
VALUES 
(@group3_id, @lisi_id, NULL, 'OWNER', NOW(), NOW()),
(@group3_id, @zhangsan_id, NULL, 'MEMBER', NOW(), NOW()),
(@group3_id, 28, NULL, 'MEMBER', NOW(), NOW()),
(@group3_id, 29, NULL, 'MEMBER', NOW(), NOW()),
(@group3_id, 30, NULL, 'MEMBER', NOW(), NOW());
