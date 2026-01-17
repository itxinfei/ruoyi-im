-- ==================== 删除 Session 管理菜单 ====================
-- 1. 删除角色菜单关联（先执行）
DELETE FROM sys_role_menu WHERE menu_id IN (2337, 2338, 2339, 2340, 2341, 2342);

-- 2. 删除 Session 管理的按钮权限
DELETE FROM sys_menu WHERE menu_id IN (2338, 2339, 2340, 2341, 2342);

-- 3. 删除 Session 管理菜单
DELETE FROM sys_menu WHERE menu_id = 2337;

-- 4. 验证删除结果
SELECT menu_id, menu_name, parent_id FROM sys_menu WHERE menu_id IN (2337, 2338, 2339, 2340, 2341, 2342);
SELECT * FROM sys_role_menu WHERE menu_id IN (2337, 2338, 2339, 2340, 2341, 2342);
