-- =============================================
-- IM系统数据库迁移脚本 v2 -> v3
-- 版本: v2_to_v3
-- 日期: 2026-01-07
-- 说明: 支持ruoyi-im-api和ruoyi-im-admin独立部署
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 迁移说明
-- =============================================
/*
1. 迁移目标：
   - 恢复 im_session 作为视图，用于管理后台
   - 优化数据库用户权限，支持两个项目独立部署
   - 保持 ruoyi-im-api 使用 im_conversation_member 表

2. 迁移步骤：
   - 创建 im_session 视图（基于 im_conversation_member）
   - 创建数据库用户（im_api 和 im_admin）
   - 设置用户权限
   - 验证迁移结果

3. 回滚方案：
   - 删除 im_session 视图
   - 删除数据库用户
   - 恢复原有权限

4. 注意事项：
   - 执行前请备份数据库
   - 建议在业务低峰期执行
   - 执行后验证功能是否正常
*/

-- =============================================
-- 1. 创建 im_session 视图
-- =============================================
DROP VIEW IF EXISTS `im_session`;
CREATE VIEW `im_session` AS
SELECT
    cm.id AS id,
    c.type AS type,
    cm.user_id AS user_id,
    u.username AS user_name,
    CASE
        WHEN c.type = 'PRIVATE' THEN
            (SELECT u2.username FROM im_user u2
             JOIN im_conversation_member cm2 ON u2.id = cm2.user_id
             WHERE cm2.conversation_id = cm.conversation_id AND cm2.user_id != cm.user_id
             LIMIT 1)
        WHEN c.type = 'GROUP' THEN c.name
        ELSE NULL
    END AS target_name,
    CASE
        WHEN c.type = 'PRIVATE' THEN
            (SELECT cm2.user_id FROM im_conversation_member cm2
             WHERE cm2.conversation_id = cm.conversation_id AND cm2.user_id != cm.user_id
             LIMIT 1)
        WHEN c.type = 'GROUP' THEN c.id
        ELSE NULL
    END AS target_id,
    (SELECT id FROM im_message m WHERE m.conversation_id = cm.conversation_id ORDER BY m.create_time DESC LIMIT 1) AS last_message_id,
    (SELECT content FROM im_message m WHERE m.conversation_id = cm.conversation_id ORDER BY m.create_time DESC LIMIT 1) AS last_message,
    cm.unread_count AS unread_count,
    cm.create_time AS create_time,
    cm.update_time AS update_time
FROM im_conversation_member cm
JOIN im_conversation c ON cm.conversation_id = c.id
JOIN im_user u ON cm.user_id = u.id
WHERE cm.is_deleted = 0;

-- =============================================
-- 2. 创建数据库用户
-- =============================================

-- 创建ruoyi-im-api用户
CREATE USER IF NOT EXISTS 'im_api'@'%' IDENTIFIED BY 'ImApi@2024';

-- 创建ruoyi-im-admin用户
CREATE USER IF NOT EXISTS 'im_admin'@'%' IDENTIFIED BY 'ImAdmin@2024';

-- =============================================
-- 3. 设置用户权限
-- =============================================

-- ruoyi-im-api 用户权限
GRANT SELECT, INSERT, UPDATE, DELETE ON im_db.* TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE ON im_db.im_conversation_member TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE ON im_db.im_message TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON im_db.im_friend TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE ON im_db.im_group TO 'im_api'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON im_db.im_group_member TO 'im_api'@'%';
GRANT SELECT ON im_db.im_session TO 'im_api'@'%';

-- ruoyi-im-admin 用户权限
GRANT SELECT, INSERT, UPDATE, DELETE ON im_db.* TO 'im_admin'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- =============================================
-- 4. 验证迁移结果
-- =============================================

-- 验证 im_session 视图是否创建成功
SELECT '验证 im_session 视图' AS '检查项',
       COUNT(*) AS '结果'
FROM information_schema.views
WHERE table_schema = DATABASE()
  AND table_name = 'im_session';

-- 验证数据库用户是否创建成功
SELECT '验证 im_api 用户' AS '检查项',
       COUNT(*) AS '结果'
FROM mysql.user
WHERE user = 'im_api';

SELECT '验证 im_admin 用户' AS '检查项',
       COUNT(*) AS '结果'
FROM mysql.user
WHERE user = 'im_admin';

-- 验证 im_session 视图数据
SELECT '验证 im_session 视图数据' AS '检查项',
       COUNT(*) AS '结果'
FROM im_session;

-- 验证 im_conversation_member 表数据
SELECT '验证 im_conversation_member 表数据' AS '检查项',
       COUNT(*) AS '结果'
FROM im_conversation_member;

-- =============================================
-- 5. 迁移完成提示
-- =============================================
SELECT '========================================' AS '';
SELECT '数据库迁移 v2 -> v3 完成' AS '';
SELECT '========================================' AS '';
SELECT '1. im_session 视图已创建' AS '';
SELECT '2. 数据库用户已创建' AS '';
SELECT '   - im_api: 用于 ruoyi-im-api' AS '';
SELECT '   - im_admin: 用于 ruoyi-im-admin' AS '';
SELECT '3. 用户权限已设置' AS '';
SELECT '4. 请验证应用功能是否正常' AS '';
SELECT '========================================' AS '';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 回滚脚本（如需回滚，请执行以下SQL）
-- =============================================
/*
-- 删除 im_session 视图
DROP VIEW IF EXISTS `im_session`;

-- 删除数据库用户
DROP USER IF EXISTS 'im_api'@'%';
DROP USER IF EXISTS 'im_admin'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 提示回滚完成
SELECT '========================================' AS '';
SELECT '数据库回滚 v2 -> v3 完成' AS '';
SELECT '========================================' AS '';
*/
