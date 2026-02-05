-- 更新群组名称并添加成员
SET NAMES utf8mb4;

-- 1. 更新新建群组为中文
UPDATE im_conversation SET name = '测试交流群' WHERE id = 80;
UPDATE im_conversation SET name = '技术讨论组' WHERE id = 81;
UPDATE im_conversation SET name = '项目协作群' WHERE id = 82;

-- 2. 为所有群组添加 zhangsan 和 lisi
-- 获取用户ID
SET @zhangsan_id = 23;
SET @lisi_id = 24;

-- 为所有现有群组添加 zhangsan 和 lisi（如果他们还不是成员）
INSERT IGNORE INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
SELECT DISTINCT 
    c.id as conversation_id,
    @zhangsan_id as user_id,
    NULL as nickname,
    'MEMBER' as role,
    NOW() as create_time,
    NOW() as update_time
FROM im_conversation c
WHERE c.type = 'GROUP'
  AND NOT EXISTS (
    SELECT 1 FROM im_conversation_member cm 
    WHERE cm.conversation_id = c.id AND cm.user_id = @zhangsan_id
  );

INSERT IGNORE INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
SELECT DISTINCT 
    c.id as conversation_id,
    @lisi_id as user_id,
    NULL as nickname,
    'MEMBER' as role,
    NOW() as create_time,
    NOW() as update_time
FROM im_conversation c
WHERE c.type = 'GROUP'
  AND NOT EXISTS (
    SELECT 1 FROM im_conversation_member cm 
    WHERE cm.conversation_id = c.id AND cm.user_id = @lisi_id
  );

-- 3. 为每个群组随机添加更多成员（从用户ID 9-38 中随机选择）
-- 为测试群组添加成员
INSERT IGNORE INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
SELECT 80, u.id, NULL, 'MEMBER', NOW(), NOW()
FROM (SELECT DISTINCT id FROM im_user WHERE id BETWEEN 9 AND 38 ORDER BY RAND() LIMIT 8) u;

INSERT IGNORE INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
SELECT 81, u.id, NULL, 'MEMBER', NOW(), NOW()
FROM (SELECT DISTINCT id FROM im_user WHERE id BETWEEN 9 AND 38 ORDER BY RAND() LIMIT 10) u;

INSERT IGNORE INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
SELECT 82, u.id, NULL, 'MEMBER', NOW(), NOW()
FROM (SELECT DISTINCT id FROM im_user WHERE id BETWEEN 9 AND 38 ORDER BY RAND() LIMIT 12) u;

-- 为部分现有群组也添加成员（让群组看起来更活跃）
-- 为群组 30-40 添加成员
INSERT IGNORE INTO im_conversation_member (conversation_id, user_id, nickname, role, create_time, update_time)
SELECT 
    g.conversation_id,
    u.id as user_id,
    NULL as nickname,
    CASE WHEN u.id IN (@zhangsan_id, @lisi_id) THEN 'MEMBER' ELSE 'MEMBER' END as role,
    NOW() as create_time,
    NOW() as update_time
FROM (
    SELECT DISTINCT c.id as conversation_id FROM im_conversation c WHERE c.type = 'GROUP' AND c.id BETWEEN 30 AND 40
) g
CROSS JOIN (SELECT DISTINCT id FROM im_user WHERE id BETWEEN 9 AND 35 ORDER BY RAND() LIMIT 15) u;
