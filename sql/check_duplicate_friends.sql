-- ============================================
-- 检查和修复重复好友记录的SQL脚本
-- ============================================

-- 1. 检查是否存在重复的好友记录（同一对用户有多条is_deleted=0的记录）
SELECT
    user_id,
    friend_id,
    COUNT(*) as count,
    GROUP_CONCAT(id ORDER BY id) as record_ids,
    GROUP_CONCAT(group_name ORDER BY id) as group_names
FROM im_friend
WHERE is_deleted = 0
GROUP BY user_id, friend_id
HAVING COUNT(*) > 1;

-- 2. 查看完整的重复记录详情
SELECT
    f1.id,
    f1.user_id,
    f1.friend_id,
    f1.group_name,
    f1.remark,
    f1.create_time,
    u1.username as user_name,
    u2.username as friend_name
FROM im_friend f1
INNER JOIN im_user u1 ON f1.user_id = u1.id
INNER JOIN im_user u2 ON f1.friend_id = u2.id
WHERE f1.is_deleted = 0
  AND EXISTS (
    SELECT 1
    FROM im_friend f2
    WHERE f2.user_id = f1.user_id
      AND f2.friend_id = f1.friend_id
      AND f2.is_deleted = 0
      AND f2.id < f1.id
  )
ORDER BY f1.user_id, f1.friend_id, f1.id;

-- 3. 修复重复记录：保留最早创建的记录，将其他重复记录软删除
UPDATE im_friend f1
INNER JOIN (
    SELECT
        user_id,
        friend_id,
        MIN(id) as keep_id
    FROM im_friend
    WHERE is_deleted = 0
    GROUP BY user_id, friend_id
    HAVING COUNT(*) > 1
) f2 ON f1.user_id = f2.user_id AND f1.friend_id = f2.friend_id
SET f1.is_deleted = 1,
    f1.deleted_time = NOW()
WHERE f1.is_deleted = 0
  AND f1.id != f2.keep_id;

-- 4. 验证修复结果
SELECT
    user_id,
    friend_id,
    COUNT(*) as count
FROM im_friend
WHERE is_deleted = 0
GROUP BY user_id, friend_id
HAVING COUNT(*) > 1;

-- ============================================
-- 说明：
-- 1. 第一个查询找出所有重复的好友关系
-- 2. 第二个查询查看重复记录的详细信息
-- 3. 第三个UPDATE语句软删除重复记录（保留最早的）
-- 4. 第四个查询验证修复后是否还有重复
-- ============================================
