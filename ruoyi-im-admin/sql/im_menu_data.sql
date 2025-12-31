im-- ----------------------------
-- IM模块菜单数据
-- ----------------------------

-- IM主菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `url`, `menu_type`, `visible`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('IM管理', '0', '5', '#', 'M', '0', '', 'fa fa-comments', 'admin', NOW(), '', NOW(), 'IM即时通讯管理');

-- 获取IM主菜单ID
SET @im_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = 'IM管理' ORDER BY create_time DESC LIMIT 1);

-- IM文件管理菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `url`, `menu_type`, `visible`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('文件管理', @im_menu_id, '1', '/im/file', 'C', '0', 'im:file:view', '#', 'admin', NOW(), '', NOW(), 'IM文件管理菜单');

-- 获取文件管理菜单ID
SET @file_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '文件管理' AND parent_id = @im_menu_id ORDER BY create_time DESC LIMIT 1);

-- 文件管理按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `url`, `menu_type`, `visible`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('文件查询', @file_menu_id, '1', '#', 'F', '0', 'im:file:list', '#', 'admin', NOW(), '', NOW(), '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `url`, `menu_type`, `visible`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('文件新增', @file_menu_id, '2', '#', 'F', '0', 'im:file:add', '#', 'admin', NOW(), '', NOW(), '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `url`, `menu_type`, `visible`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('文件修改', @file_menu_id, '3', '#', 'F', '0', 'im:file:edit', '#', 'admin', NOW(), '', NOW(), '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `url`, `menu_type`, `visible`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('文件删除', @file_menu_id, '4', '#', 'F', '0', 'im:file:remove', '#', 'admin', NOW(), '', NOW(), '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `url`, `menu_type`, `visible`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('文件上传', @file_menu_id, '5', '#', 'F', '0', 'im:file:upload', '#', 'admin', NOW(), '', NOW(), '');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `url`, `menu_type`, `visible`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('文件下载', @file_menu_id, '6', '#', 'F', '0', 'im:file:download', '#', 'admin', NOW(), '', NOW(), '');