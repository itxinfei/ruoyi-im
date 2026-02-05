-- ============================================
-- 测试数据初始化脚本
-- 功能：为张三、李四添加所有用户为好友并加入所有群组
-- 执行方式：mysql -u用户名 -p密码 数据库名 < test-data-init.sql
-- 更新：2026-02-05 改为使用用户名匹配，避免硬编码ID
-- ============================================

-- ========== 好友关系初始化 ==========

-- 为"张三"添加所有其他用户为好友
INSERT INTO im_friend (user_id, friend_id, remark, group_id, status, created_at, updated_at)
SELECT
    zhangsan.id as user_id,
    u.id as friend_id,
    u.nickname as remark,
    NULL as group_id,
    1 as status,
    NOW() as created_at,
    NOW() as updated_at
FROM im_user u
CROSS JOIN (SELECT id FROM im_user WHERE username = 'zhangsan') zhangsan
WHERE u.id != zhangsan.id
  AND NOT EXISTS (
    SELECT 1 FROM im_friend f
    WHERE f.user_id = zhangsan.id AND f.friend_id = u.id
  );

-- 为"李四"添加所有其他用户为好友
INSERT INTO im_friend (user_id, friend_id, remark, group_id, status, created_at, updated_at)
SELECT
    lisi.id as user_id,
    u.id as friend_id,
    u.nickname as remark,
    NULL as group_id,
    1 as status,
    NOW() as created_at,
    NOW() as updated_at
FROM im_user u
CROSS JOIN (SELECT id FROM im_user WHERE username = 'lisi') lisi
WHERE u.id != lisi.id
  AND NOT EXISTS (
    SELECT 1 FROM im_friend f
    WHERE f.user_id = lisi.id AND f.friend_id = u.id
  );

-- ========== 群组加入初始化 ==========

-- 为"张三"加入所有群组（通过会话成员表）
INSERT INTO im_conversation_member (conversation_id, user_id, nickname, role, join_time, created_at, updated_at)
SELECT
    cm.conversation_id,
    zhangsan.id as user_id,
    NULL as nickname,
    2 as role, -- 2=普通成员
    NOW() as join_time,
    NOW() as created_at,
    NOW() as updated_at
FROM (SELECT DISTINCT conversation_id FROM im_conversation_member WHERE user_id != zhangsan.id) cm
CROSS JOIN (SELECT id FROM im_user WHERE username = 'zhangsan') zhangsan
WHERE NOT EXISTS (
    SELECT 1 FROM im_conversation_member existing
    WHERE existing.conversation_id = cm.conversation_id AND existing.user_id = zhangsan.id
)
AND EXISTS (
    -- 确保是群组会话（type=2）
    SELECT 1 FROM im_conversation c
    WHERE c.id = cm.conversation_id AND c.type = 2
);

-- 为"李四"加入所有群组
INSERT INTO im_conversation_member (conversation_id, user_id, nickname, role, join_time, created_at, updated_at)
SELECT
    cm.conversation_id,
    lisi.id as user_id,
    NULL as nickname,
    2 as role, -- 2=普通成员
    NOW() as join_time,
    NOW() as created_at,
    NOW() as updated_at
FROM (SELECT DISTINCT conversation_id FROM im_conversation_member WHERE user_id != lisi.id) cm
CROSS JOIN (SELECT id FROM im_user WHERE username = 'lisi') lisi
WHERE NOT EXISTS (
    SELECT 1 FROM im_conversation_member existing
    WHERE existing.conversation_id = cm.conversation_id AND existing.user_id = lisi.id
)
AND EXISTS (
    -- 确保是群组会话（type=2）
    SELECT 1 FROM im_conversation c
    WHERE c.id = cm.conversation_id AND c.type = 2
);

-- ========== 验证查询 ==========

-- 验证好友关系
SELECT f.user_id, u.nickname as username, COUNT(*) as friend_count
FROM im_friend f
JOIN im_user u ON f.user_id = u.id
GROUP BY f.user_id, u.nickname;

-- 验证群组加入
SELECT cm.user_id, u.nickname, COUNT(*) as group_count
FROM im_conversation_member cm
JOIN im_user u ON cm.user_id = u.id
JOIN im_conversation c ON cm.conversation_id = c.id AND c.type = 2
GROUP BY cm.user_id, u.nickname;
