-- ============================================
-- 测试数据初始化脚本
-- 功能：为张三、李四添加所有用户为好友并加入所有群组
-- 执行方式：mysql -u用户名 -p密码 数据库名 < test-data-init.sql
-- 更新：2026-02-05 修复列名匹配实际表结构
-- ============================================

-- ========== 好友关系初始化 ==========

-- 为"张三"添加所有其他用户为好友
INSERT INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time)
SELECT
    zhangsan.id as user_id,
    u.id as friend_id,
    u.nickname as remark,
    NULL as group_name,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM im_user u
CROSS JOIN (SELECT id FROM im_user WHERE username = 'zhangsan') zhangsan
WHERE u.id != zhangsan.id
  AND NOT EXISTS (
    SELECT 1 FROM im_friend f
    WHERE f.user_id = zhangsan.id AND f.friend_id = u.id AND f.is_deleted = 0
  );

-- 为"李四"添加所有其他用户为好友
INSERT INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time)
SELECT
    lisi.id as user_id,
    u.id as friend_id,
    u.nickname as remark,
    NULL as group_name,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM im_user u
CROSS JOIN (SELECT id FROM im_user WHERE username = 'lisi') lisi
WHERE u.id != lisi.id
  AND NOT EXISTS (
    SELECT 1 FROM im_friend f
    WHERE f.user_id = lisi.id AND f.friend_id = u.id AND f.is_deleted = 0
  );

-- ========== 双向好友关系（可选，确保双方都能看到对方）==========

-- 为所有用户添加"张三"为好友（反向关系）
INSERT INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time)
SELECT
    u.id as user_id,
    zhangsan.id as friend_id,
    '张三' as remark,
    NULL as group_name,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM im_user u
CROSS JOIN (SELECT id FROM im_user WHERE username = 'zhangsan') zhangsan
WHERE u.id != zhangsan.id
  AND NOT EXISTS (
    SELECT 1 FROM im_friend f
    WHERE f.user_id = u.id AND f.friend_id = zhangsan.id AND f.is_deleted = 0
  );

-- 为所有用户添加"李四"为好友（反向关系）
INSERT INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time)
SELECT
    u.id as user_id,
    lisi.id as friend_id,
    '李四' as remark,
    NULL as group_name,
    0 as is_deleted,
    NOW() as create_time,
    NOW() as update_time
FROM im_user u
CROSS JOIN (SELECT id FROM im_user WHERE username = 'lisi') lisi
WHERE u.id != lisi.id
  AND NOT EXISTS (
    SELECT 1 FROM im_friend f
    WHERE f.user_id = u.id AND f.friend_id = lisi.id AND f.is_deleted = 0
  );

-- ========== 验证查询 ==========

-- 验证好友关系
SELECT '好友关系统计:' as info;
SELECT f.user_id, u.username, u.nickname, COUNT(*) as friend_count
FROM im_friend f
JOIN im_user u ON f.user_id = u.id
WHERE f.is_deleted = 0
GROUP BY f.user_id, u.username, u.nickname
ORDER BY friend_count DESC;

-- 显示张三的好友列表
SELECT '张三的好友列表:' as info;
SELECT f.id, f.friend_id, u.username, u.nickname, f.remark, f.group_name
FROM im_friend f
JOIN im_user u ON f.friend_id = u.id
WHERE f.user_id = (SELECT id FROM im_user WHERE username = 'zhangsan')
  AND f.is_deleted = 0;

-- 显示李四的好友列表
SELECT '李四的好友列表:' as info;
SELECT f.id, f.friend_id, u.username, u.nickname, f.remark, f.group_name
FROM im_friend f
JOIN im_user u ON f.friend_id = u.id
WHERE f.user_id = (SELECT id FROM im_user WHERE username = 'lisi')
  AND f.is_deleted = 0;
