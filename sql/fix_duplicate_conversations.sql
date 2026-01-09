-- ============================================
-- 清理重复会话的SQL脚本
-- 问题描述：由于之前的SQL查询BUG，可能创建了多个同一对用户的私聊会话
-- ============================================

-- 1. 查看重复的私聊会话（target_id相同意味着是同一对用户的会话）
SELECT
    type,
    target_id,
    COUNT(*) as count,
    GROUP_CONCAT(id ORDER BY id) as conversation_ids
FROM im_conversation
WHERE type = 'PRIVATE' AND is_deleted = 0
GROUP BY type, target_id
HAVING COUNT(*) > 1;

-- 2. 查看重复会话的详细信息（包括会话成员）
SELECT
    c.id as conversation_id,
    c.type,
    c.target_id,
    c.create_time,
    GROUP_CONCAT(cm.user_id) as members
FROM im_conversation c
LEFT JOIN im_conversation_member cm ON c.id = cm.conversation_id
WHERE c.type = 'PRIVATE' AND c.is_deleted = 0
GROUP BY c.id
HAVING COUNT(*) > 1;

-- 3. 清理重复的私聊会话（保留最早创建的会话，删除其他重复的）
-- 第一步：为每个target_id找到要保留的会话ID（最早创建的）
CREATE TEMPORARY TABLE temp_keep_conversations AS
SELECT
    MIN(id) as keep_id
FROM im_conversation
WHERE type = 'PRIVATE' AND is_deleted = 0
GROUP BY target_id
HAVING COUNT(*) > 1;

-- 第二步：软删除重复的会话（保留最早创建的那个）
UPDATE im_conversation
SET is_deleted = 1,
    deleted_time = NOW()
WHERE type = 'PRIVATE'
  AND is_deleted = 0
  AND id NOT IN (SELECT keep_id FROM temp_keep_conversations)
  AND target_id IN (SELECT target_id FROM im_conversation WHERE type = 'PRIVATE' AND is_deleted = 0 GROUP BY target_id HAVING COUNT(*) > 1);

-- 第三步：软删除重复会话对应的会话成员记录
UPDATE im_conversation_member cm
SET is_deleted = 1,
    update_time = NOW()
WHERE EXISTS (
    SELECT 1 FROM im_conversation c
    WHERE c.id = cm.conversation_id
      AND c.type = 'PRIVATE'
      AND c.is_deleted = 1
);

-- 4. 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_keep_conversations;

-- 5. 验证清理结果
SELECT
    type,
    COUNT(*) as count
FROM im_conversation
WHERE is_deleted = 0
GROUP BY type;

-- ============================================
-- 注意事项：
-- 1. 此脚本使用软删除（is_deleted=1），不会物理删除数据
-- 2. 清理前请先备份数据库
-- 3. 建议先在测试环境执行验证
-- ============================================
