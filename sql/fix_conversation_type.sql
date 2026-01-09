-- 修复会话类型不匹配问题
-- 问题：数据库使用 'SINGLE'，但Java代码使用 'PRIVATE'
-- 解决方案：将数据库中的 'SINGLE' 更新为 'PRIVATE'

-- 更新 im_conversation 表
UPDATE `im_conversation` SET `type` = 'PRIVATE' WHERE `type` = 'SINGLE';

-- 验证结果
SELECT id, type, name, target_id FROM `im_conversation` LIMIT 20;

-- 预期结果应该显示：
-- id | type   | name | target_id
-- 1  | PRIVATE | 李四  | 3
-- 2  | PRIVATE | 王五  | 4
-- ...
-- 6  | GROUP   | 技术交流群 | 1
