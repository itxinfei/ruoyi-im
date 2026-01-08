-- 添加群组全员禁言字段
ALTER TABLE `im_group` ADD COLUMN `all_muted` INT(11) DEFAULT 0 COMMENT '全员禁言：0=否，1=是' AFTER `type`;
