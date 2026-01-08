-- 快速修复：将所有用户的头像更新为本地路径
UPDATE `im_user` SET `avatar` = '/avatar/1.jpg' WHERE `id` % 3 = 2;
UPDATE `im_user` SET `avatar` = '/avatar/2.jpg' WHERE `id` % 3 = 0;
UPDATE `im_user` SET `avatar` = '/avatar/3.jpg' WHERE `id` % 3 = 1;

-- 确认更新结果
SELECT id, username, nickname, avatar FROM `im_user`;
