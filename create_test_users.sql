-- 新增测试账号
-- 所有账号的密码都是: 123456

INSERT INTO `im_user` (`username`, `password`, `nickname`, `email`, `phone`, `avatar`, `status`, `create_time`, `update_time`) VALUES
('test1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户1', 'test1@example.com', '13900000001', '/avatar/1.jpg', 'ACTIVE', NOW(), NOW()),
('test2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户2', 'test2@example.com', '13900000002', '/avatar/2.jpg', 'ACTIVE', NOW(), NOW()),
('test3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户3', 'test3@example.com', '13900000003', '/avatar/3.jpg', 'ACTIVE', NOW(), NOW()),
('test4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户4', 'test4@example.com', '13900000004', '/avatar/1.jpg', 'ACTIVE', NOW(), NOW()),
('test5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户5', 'test5@example.com', '13900000005', '/avatar/2.jpg', 'ACTIVE', NOW(), NOW());
