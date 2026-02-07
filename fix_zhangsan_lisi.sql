-- =============================================
-- 确保 zhangsan (ID=23) 和 lisi (ID=24)
-- 添加所有用户为好友，加入所有群组
-- =============================================

SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

-- 1. 确认当前用户数量
SELECT '=== 当前用户总数 ===' as info;
SELECT COUNT(*) as user_count FROM im_user;

-- 2. 确保 zhangsan 和 lisi 的好友关系完整
-- zhangsan (ID=23) 添加所有其他用户为好友
INSERT IGNORE INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time)
SELECT
    23 as user_id,
    u.id as friend_id,
    '' as remark,
    '默认分组' as group_name,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM im_user u
WHERE u.id != 23
  AND NOT EXISTS (
    SELECT 1 FROM im_friend f
    WHERE f.user_id = 23 AND f.friend_id = u.id AND f.is_deleted = 0
  );

-- lisi (ID=24) 添加所有其他用户为好友
INSERT IGNORE INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time)
SELECT
    24 as user_id,
    u.id as friend_id,
    '' as remark,
    '默认分组' as group_name,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM im_user u
WHERE u.id != 24
  AND NOT EXISTS (
    SELECT 1 FROM im_friend f
    WHERE f.user_id = 24 AND f.friend_id = u.id AND f.is_deleted = 0
  );

-- 3. 确保 zhangsan 和 lisi 加入所有群组
-- zhangsan (ID=23) 加入所有群组
INSERT IGNORE INTO im_conversation_member (conversation_id, user_id, nickname, role, unread_count, is_pinned, is_muted, is_deleted, create_time, update_time)
SELECT
    c.id as conversation_id,
    23 as user_id,
    NULL as nickname,
    'MEMBER' as role,
    0 as unread_count,
    0 as is_pinned,
    0 as is_muted,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM im_conversation c
WHERE c.type = 'GROUP'
  AND c.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM im_conversation_member m
    WHERE m.user_id = 23 AND m.conversation_id = c.id AND m.is_deleted = 0
  );

-- lisi (ID=24) 加入所有群组
INSERT IGNORE INTO im_conversation_member (conversation_id, user_id, nickname, role, unread_count, is_pinned, is_muted, is_deleted, create_time, update_time)
SELECT
    c.id as conversation_id,
    24 as user_id,
    NULL as nickname,
    'MEMBER' as role,
    0 as unread_count,
    0 as is_pinned,
    0 as is_muted,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM im_conversation c
WHERE c.type = 'GROUP'
  AND c.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM im_conversation_member m
    WHERE m.user_id = 24 AND m.conversation_id = c.id AND m.is_deleted = 0
  );

-- 4. 验证结果
SELECT '=== zhangsan 好友数 ===' as info;
SELECT COUNT(*) as count FROM im_friend WHERE user_id = 23 AND is_deleted = 0;

SELECT '=== lisi 好友数 ===' as info;
SELECT COUNT(*) as count FROM im_friend WHERE user_id = 24 AND is_deleted = 0;

SELECT '=== zhangsan 加入的群组数 ===' as info;
SELECT COUNT(*) as count
FROM im_conversation_member m
JOIN im_conversation c ON m.conversation_id = c.id
WHERE m.user_id = 23 AND c.type = 'GROUP' AND c.is_deleted = 0 AND m.is_deleted = 0;

SELECT '=== lisi 加入的群组数 ===' as info;
SELECT COUNT(*) as count
FROM im_conversation_member m
JOIN im_conversation c ON m.conversation_id = c.id
WHERE m.user_id = 24 AND c.type = 'GROUP' AND c.is_deleted = 0 AND m.is_deleted = 0;

SELECT '=== 总群组数 ===' as info;
SELECT COUNT(*) as count FROM im_conversation WHERE type = 'GROUP' AND is_deleted = 0;

-- =============================================
-- 注意：执行此脚本后，需要清除 Redis 缓存
-- 方法1: 访问 http://localhost:8888/api/im/contact/cache/clear
-- 方法2: 重新登录 zhangsan 或 lisi 账号
-- =============================================
