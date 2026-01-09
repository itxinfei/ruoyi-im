-- 修复 im_group 表结构，添加缺少的字段
-- 这些字段是 Java 代码期望的，但数据库表中不存在

ALTER TABLE `im_group`
ADD COLUMN `notice` VARCHAR(500) NULL DEFAULT NULL COMMENT '群公告' AFTER `description`,
ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1=正常, 0=禁用' AFTER `notice`,
ADD COLUMN `member_count` INT(11) NOT NULL DEFAULT 0 COMMENT '成员数量' AFTER `status`,
ADD COLUMN `member_limit` INT(11) NOT NULL DEFAULT 500 COMMENT '成员上限' AFTER `member_count`,
ADD COLUMN `type` VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' COMMENT '群组类型：PUBLIC=公开群, PRIVATE=私密群' AFTER `member_limit`;

-- 验证修改
DESCRIBE `im_group`;
