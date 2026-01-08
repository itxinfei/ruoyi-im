-- 更新用户头像为本地路径
-- 使用现有的 static/avatar 目录下的头像

-- 更新所有用户的头像为本地路径
UPDATE `im_user` SET `avatar` = '/avatar/1.jpg' WHERE `id` IN (2, 5, 8, 11, 14, 17, 20);
UPDATE `im_user` SET `avatar` = '/avatar/2.jpg' WHERE `id` IN (3, 6, 9, 12, 15, 18, 21);
UPDATE `im_user` SET `avatar` = '/avatar/3.jpg' WHERE `id` IN (4, 7, 10, 13, 16, 19);

-- 或者使用随机分配
UPDATE `im_user` SET `avatar` = '/avatar/1.jpg' WHERE `id` % 3 = 2;
UPDATE `im_user` SET `avatar` = '/avatar/2.jpg' WHERE `id` % 3 = 0;
UPDATE `im_user` SET `avatar` = '/avatar/3.jpg' WHERE `id` % 3 = 1;

-- 查看更新结果
SELECT id, username, nickname, avatar FROM `im_user`;
