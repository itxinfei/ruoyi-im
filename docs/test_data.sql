-- =============================================
-- 测试数据生成脚本
-- 为各核心表生成至少20条测试数据
-- =============================================

-- =============================================
-- 1. 用户测试数据（20个用户）
-- =============================================
INSERT INTO `im_user` (`id`, `username`, `password`, `nickname`, `avatar`, `gender`, `mobile`, `email`, `status`, `signature`, `create_time`, `update_time`) VALUES
(2, 'user002', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '张三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC2', 1, '13800000002', 'zhangsan@example.com', 1, '努力工作，快乐生活', NOW(), NOW()),
(3, 'user003', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC3', 1, '13800000003', 'lisi@example.com', 1, '学无止境', NOW(), NOW()),
(4, 'user004', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC4', 1, '13800000004', 'wangwu@example.com', 1, '代码改变世界', NOW(), NOW()),
(5, 'user005', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC5', 0, '13800000005', 'zhaoliu@example.com', 1, '保持热爱', NOW(), NOW()),
(6, 'user006', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '孙七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC6', 1, '13800000006', 'sunqi@example.com', 1, '今天也要加油', NOW(), NOW()),
(7, 'user007', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '周八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC7', 0, '13800000007', 'zhouba@example.com', 1, '未来可期', NOW(), NOW()),
(8, 'user008', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '吴九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC8', 1, '13800000008', 'wujiu@example.com', 1, '行稳致远', NOW(), NOW()),
(9, 'user009', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '郑十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC9', 1, '13800000009', 'zhengshi@example.com', 1, '不忘初心', NOW(), NOW()),
(10, 'user010', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '陈一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCA', 0, '13800000010', 'chenyi@example.com', 1, '砥砺前行', NOW(), NOW()),
(11, 'user011', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '林二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCB', 1, '13800000011', 'liner@example.com', 1, '天天向上', NOW(), NOW()),
(12, 'user012', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '黄三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCC', 1, '13800000012', 'huangsan@example.com', 1, '追求卓越', NOW(), NOW()),
(13, 'user013', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '刘四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCD', 0, '13800000013', 'liusi@example.com', 1, '梦想成真', NOW(), NOW()),
(14, 'user014', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '何五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCE', 1, '13800000014', 'hewu@example.com', 1, '珍惜当下', NOW(), NOW()),
(15, 'user015', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '罗六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCF', 1, '13800000015', 'luoliu@example.com', 1, '勇往直前', NOW(), NOW()),
(16, 'user016', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '梁七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG', 0, '13800000016', 'liangqi@example.com', 1, '脚踏实地', NOW(), NOW()),
(17, 'user017', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '宋八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCH', 1, '13800000017', 'songba@example.com', 1, '天道酬勤', NOW(), NOW()),
(18, 'user018', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '唐九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCI', 1, '13800000018', 'tangjiu@example.com', 1, '感恩有你', NOW(), NOW()),
(19, 'user019', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '冯十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCJ', 0, '13800000019', 'fengshi@example.com', 1, '知足常乐', NOW(), NOW()),
(20, 'user020', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '于十一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCK', 1, '13800000020', 'yushiyi@example.com', 1, '生活美好', NOW(), NOW()),
(21, 'user021', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/T/33C8pQO6q', '董十二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCL', 1, '13800000021', 'dongshier@example.com', 1, '越努力越幸运', NOW(), NOW());

-- =============================================
-- 2. 群组测试数据（10个群组）
-- =============================================
INSERT INTO `im_group` (`id`, `name`, `avatar`, `owner_id`, `description`, `max_members`, `create_time`, `update_time`) VALUES
(1, '技术交流群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG1', 2, '欢迎大家交流技术问题', 500, NOW(), NOW()),
(2, '产品讨论群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG2', 3, '产品需求讨论', 200, NOW(), NOW()),
(3, '项目开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG3', 4, '项目进度同步', 50, NOW(), NOW()),
(4, '公司全员群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG4', 2, '公司通知发布', 1000, NOW(), NOW()),
(5, '篮球爱好者', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG5', 5, '每周组织篮球活动', 100, NOW(), NOW()),
(6, '读书分享会', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG6', 6, '分享读书心得', 200, NOW(), NOW()),
(7, '前端开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG7', 7, '前端技术交流', 300, NOW(), NOW()),
(8, '后端开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG8', 8, '后端技术交流', 300, NOW(), NOW()),
(9, '设计团队', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG9', 9, 'UI/UX设计讨论', 50, NOW(), NOW()),
(10, '测试团队', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCGA', 10, '质量保证交流', 100, NOW(), NOW());

-- =============================================
-- 3. 群组成员测试数据（35条记录）
-- =============================================
INSERT INTO `im_group_member` (`group_id`, `user_id`, `nickname`, `role`, `is_muted`, `create_time`, `update_time`) VALUES
-- 群组1成员（技术交流群）
(1, 2, '张三', 'OWNER', 0, NOW(), NOW()),
(1, 3, '李四', 'ADMIN', 0, NOW(), NOW()),
(1, 4, '王五', 'MEMBER', 0, NOW(), NOW()),
(1, 5, '赵六', 'MEMBER', 0, NOW(), NOW()),
(1, 6, '孙七', 'MEMBER', 0, NOW(), NOW()),
-- 群组2成员（产品讨论群）
(2, 3, '李四', 'OWNER', 0, NOW(), NOW()),
(2, 7, '周八', 'ADMIN', 0, NOW(), NOW()),
(2, 8, '吴九', 'MEMBER', 0, NOW(), NOW()),
-- 群组3成员（项目开发群）
(3, 4, '王五', 'OWNER', 0, NOW(), NOW()),
(3, 9, '郑十', 'ADMIN', 0, NOW(), NOW()),
(3, 10, '陈一', 'MEMBER', 0, NOW(), NOW()),
(3, 11, '林二', 'MEMBER', 0, NOW(), NOW()),
-- 群组4成员（公司全员群）
(4, 2, '张三', 'OWNER', 0, NOW(), NOW()),
(4, 3, '李四', 'ADMIN', 0, NOW(), NOW()),
(4, 4, '王五', 'MEMBER', 0, NOW(), NOW()),
(4, 5, '赵六', 'MEMBER', 0, NOW(), NOW()),
(4, 6, '孙七', 'MEMBER', 0, NOW(), NOW()),
(4, 7, '周八', 'MEMBER', 0, NOW(), NOW()),
(4, 8, '吴九', 'MEMBER', 0, NOW(), NOW()),
(4, 9, '郑十', 'MEMBER', 0, NOW(), NOW()),
(4, 10, '陈一', 'MEMBER', 0, NOW(), NOW()),
-- 群组5成员（篮球爱好者）
(5, 5, '赵六', 'OWNER', 0, NOW(), NOW()),
(5, 11, '林二', 'MEMBER', 0, NOW(), NOW()),
(5, 12, '黄三', 'MEMBER', 0, NOW(), NOW()),
-- 群组6成员（读书分享会）
(6, 6, '孙七', 'OWNER', 0, NOW(), NOW()),
(6, 13, '刘四', 'MEMBER', 0, NOW(), NOW()),
-- 群组7成员（前端开发群）
(7, 7, '周八', 'OWNER', 0, NOW(), NOW()),
(7, 14, '何五', 'MEMBER', 0, NOW(), NOW()),
-- 群组8成员（后端开发群）
(8, 8, '吴九', 'OWNER', 0, NOW(), NOW()),
(8, 15, '罗六', 'MEMBER', 0, NOW(), NOW()),
-- 群组9成员（设计团队）
(9, 9, '郑十', 'OWNER', 0, NOW(), NOW()),
(9, 16, '梁七', 'MEMBER', 0, NOW(), NOW()),
(9, 17, '宋八', 'MEMBER', 0, NOW(), NOW()),
-- 群组10成员（测试团队）
(10, 10, '陈一', 'OWNER', 0, NOW(), NOW()),
(10, 18, '唐九', 'MEMBER', 0, NOW(), NOW()),
(10, 19, '冯十', 'MEMBER', 0, NOW(), NOW()),
(10, 20, '于十一', 'MEMBER', 0, NOW(), NOW()),
(10, 21, '董十二', 'MEMBER', 0, NOW(), NOW());

-- =============================================
-- 4. 会话测试数据（15个会话）
-- =============================================
INSERT INTO `im_conversation` (`id`, `type`, `target_id`, `name`, `avatar`, `last_message_time`, `create_time`, `update_time`) VALUES
(1, 'SINGLE', 3, '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC3', NOW(), NOW(), NOW()),
(2, 'SINGLE', 4, '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC4', NOW(), NOW(), NOW()),
(3, 'SINGLE', 5, '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC5', NOW(), NOW(), NOW()),
(4, 'SINGLE', 6, '孙七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC6', NOW(), NOW(), NOW()),
(5, 'SINGLE', 7, '周八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC7', NOW(), NOW(), NOW()),
(6, 'GROUP', 1, '技术交流群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG1', NOW(), NOW(), NOW()),
(7, 'GROUP', 2, '产品讨论群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG2', NOW(), NOW(), NOW()),
(8, 'GROUP', 3, '项目开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG3', NOW(), NOW(), NOW()),
(9, 'GROUP', 4, '公司全员群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG4', NOW(), NOW(), NOW()),
(10, 'SINGLE', 8, '吴九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC8', NOW(), NOW(), NOW()),
(11, 'SINGLE', 9, '郑十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC9', NOW(), NOW(), NOW()),
(12, 'GROUP', 5, '篮球爱好者', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG5', NOW(), NOW(), NOW()),
(13, 'SINGLE', 10, '陈一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCA', NOW(), NOW(), NOW()),
(14, 'GROUP', 6, '读书分享会', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG6', NOW(), NOW(), NOW()),
(15, 'SINGLE', 11, '林二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCB', NOW(), NOW(), NOW());

-- =============================================
-- 5. 消息测试数据（30条消息）
-- =============================================
INSERT INTO `im_message` (`id`, `conversation_id`, `sender_id`, `message_type`, `content`, `create_time`) VALUES
(1, 1, 2, 'TEXT', '你好，最近怎么样？', '2024-01-01 09:00:00'),
(2, 1, 3, 'TEXT', '挺好的，谢谢关心！你呢？', '2024-01-01 09:01:00'),
(3, 1, 2, 'TEXT', '我也不错，最近在学习新技术', '2024-01-01 09:02:00'),
(4, 2, 2, 'TEXT', '王五，那个项目进展如何？', '2024-01-01 10:00:00'),
(5, 2, 4, 'TEXT', '进展顺利，预计下周完成', '2024-01-01 10:05:00'),
(6, 3, 2, 'TEXT', '赵六，明天开会吗？', '2024-01-01 11:00:00'),
(7, 3, 5, 'TEXT', '是的，下午2点', '2024-01-01 11:01:00'),
(8, 4, 2, 'TEXT', '孙七，文件发给我一下', '2024-01-01 12:00:00'),
(9, 4, 6, 'TEXT', '好的，稍等', '2024-01-01 12:01:00'),
(10, 5, 2, 'TEXT', '周八，设计稿看完了吗？', '2024-01-01 13:00:00'),
(11, 5, 7, 'TEXT', '看完了，有一些修改意见', '2024-01-01 13:10:00'),
(12, 6, 2, 'TEXT', '大家好，欢迎加入技术交流群', '2024-01-01 14:00:00'),
(13, 6, 3, 'TEXT', '感谢群主邀请', '2024-01-01 14:01:00'),
(14, 6, 4, 'TEXT', '向大家学习', '2024-01-01 14:02:00'),
(15, 7, 3, 'TEXT', '产品需求已经更新', '2024-01-01 15:00:00'),
(16, 7, 7, 'TEXT', '收到，我会查看', '2024-01-01 15:01:00'),
(17, 8, 4, 'TEXT', '项目进度汇报', '2024-01-01 16:00:00'),
(18, 8, 9, 'TEXT', '前端开发完成了80%', '2024-01-01 16:10:00'),
(19, 9, 2, 'TEXT', '公司通知：本周五下午开会', '2024-01-01 17:00:00'),
(20, 9, 3, 'TEXT', '收到', '2024-01-01 17:01:00'),
(21, 10, 2, 'TEXT', '吴九，后端接口文档发我', '2024-01-02 09:00:00'),
(22, 10, 8, 'TEXT', '已经发送到你邮箱', '2024-01-02 09:05:00'),
(23, 11, 2, 'TEXT', '郑十，测试用例写好了吗？', '2024-01-02 10:00:00'),
(24, 11, 9, 'TEXT', '写好了，正在执行测试', '2024-01-02 10:01:00'),
(25, 12, 5, 'TEXT', '本周篮球活动照常进行', '2024-01-02 11:00:00'),
(26, 12, 11, 'TEXT', '我会参加', '2024-01-02 11:01:00'),
(27, 13, 2, 'TEXT', '陈一，UI验收通过了吗？', '2024-01-02 12:00:00'),
(28, 13, 10, 'TEXT', '通过了，可以开发', '2024-01-02 12:01:00'),
(29, 14, 6, 'TEXT', '本周读书分享：《深入理解计算机系统》', '2024-01-02 13:00:00'),
(30, 15, 2, 'TEXT', '林二，代码review一下', '2024-01-02 14:00:00');

-- =============================================
-- 6. 审批模板测试数据
-- =============================================
INSERT INTO `im_approval_template` (`id`, `name`, `code`, `category`, `description`, `form_schema`, `flow_config`, `create_time`) VALUES
(1, '请假申请', 'leave', 'HR', '员工请假审批流程', '{"fields":[{"name":"reason","label":"请假原因","type":"textarea","required":true},{"name":"startDate","label":"开始日期","type":"date","required":true},{"name":"endDate","label":"结束日期","type":"date","required":true},{"name":"days","label":"请假天数","type":"number","required":true}]}', '[{"key":"manager","name":"部门经理审批","type":"ANY","approvers":[3]},{"key":"hr","name":"人事审批","type":"ANY","approvers":[7]}]', NOW()),
(2, '报销申请', 'expense', 'FINANCE', '费用报销审批流程', '{"fields":[{"name":"amount","label":"报销金额","type":"number","required":true},{"name":"type","label":"报销类型","type":"select","options":["差旅","餐饮","交通","办公"],"required":true},{"name":"description","label":"报销说明","type":"textarea","required":true}]}', '[{"key":"manager","name":"部门经理审批","type":"ANY","approvers":[3]},{"key":"finance","name":"财务审批","type":"ANY","approvers":[8]}]', NOW()),
(3, '出差申请', 'business_trip', 'GENERAL', '出差审批流程', '{"fields":[{"name":"destination","label":"出差地点","type":"text","required":true},{"name":"startDate","label":"开始日期","type":"date","required":true},{"name":"endDate","label":"结束日期","type":"date","required":true},{"name":"reason","label":"出差原因","type":"textarea","required":true}]}', '[{"key":"manager","name":"部门经理审批","type":"ANY","approvers":[3]}]', NOW());

-- =============================================
-- 7. 审批测试数据（20条）
-- =============================================
INSERT INTO `im_approval` (`id`, `template_id`, `title`, `applicant_id`, `status`, `current_node_id`, `form_data`, `apply_time`, `create_time`, `update_time`) VALUES
(1, 1, '请假申请-张三', 2, 'PENDING', NULL, '{"reason":"家中有事","startDate":"2024-01-10","endDate":"2024-01-12","days":3}', '2024-01-05 09:00:00', NOW(), NOW()),
(2, 1, '请假申请-李四', 3, 'APPROVED', 2, '{"reason":"身体不适","startDate":"2024-01-08","endDate":"2024-01-09","days":2}', '2024-01-05 10:00:00', NOW(), NOW()),
(3, 1, '请假申请-王五', 4, 'PENDING', NULL, '{"reason":"个人事务","startDate":"2024-01-15","endDate":"2024-01-16","days":2}', '2024-01-05 11:00:00', NOW(), NOW()),
(4, 2, '报销申请-赵六', 5, 'APPROVED', 2, '{"amount":500,"type":"差旅","description":"北京出差交通费"}', '2024-01-04 09:00:00', NOW(), NOW()),
(5, 2, '报销申请-孙七', 6, 'PENDING', NULL, '{"amount":200,"type":"餐饮","description":"客户招待费"}', '2024-01-05 14:00:00', NOW(), NOW()),
(6, 2, '报销申请-周八', 7, 'REJECTED', 2, '{"amount":1000,"type":"办公","description":"购买办公用品"}', '2024-01-04 10:00:00', NOW(), NOW()),
(7, 3, '出差申请-吴九', 8, 'PENDING', NULL, '{"destination":"上海","startDate":"2024-01-20","endDate":"2024-01-22","reason":"参加技术会议"}', '2024-01-05 15:00:00', NOW(), NOW()),
(8, 1, '请假申请-郑十', 9, 'APPROVED', 2, '{"reason":"探亲","startDate":"2024-01-25","endDate":"2024-01-26","days":2}', '2024-01-05 16:00:00', NOW(), NOW()),
(9, 2, '报销申请-陈一', 10, 'PENDING', NULL, '{"amount":350,"type":"交通","description":"打车费用"}', '2024-01-05 17:00:00', NOW(), NOW()),
(10, 3, '出差申请-林二', 11, 'APPROVED', 2, '{"destination":"深圳","startDate":"2024-01-18","endDate":"2024-01-19","reason":"客户拜访"}', '2024-01-04 14:00:00', NOW(), NOW()),
(11, 1, '请假申请-黄三', 12, 'PENDING', NULL, '{"reason":"搬家","startDate":"2024-01-30","endDate":"2024-01-30","days":1}', '2024-01-05 18:00:00', NOW(), NOW()),
(12, 2, '报销申请-刘四', 13, 'APPROVED', 2, '{"amount":150,"type":"餐饮","description":"团队聚餐"}', '2024-01-04 15:00:00', NOW(), NOW()),
(13, 3, '出差申请-何五', 14, 'PENDING', NULL, '{"destination":"杭州","startDate":"2024-02-01","endDate":"2024-02-03","reason":"项目调研"}', '2024-01-05 19:00:00', NOW(), NOW()),
(14, 1, '请假申请-罗六', 15, 'APPROVED', 2, '{"reason":"看病","startDate":"2024-01-22","endDate":"2024-01-22","days":1}', '2024-01-04 16:00:00', NOW(), NOW()),
(15, 2, '报销申请-梁七', 16, 'PENDING', NULL, '{"amount":800,"type":"差旅","description":"南京出差"}', '2024-01-05 20:00:00', NOW(), NOW()),
(16, 3, '出差申请-宋八', 17, 'APPROVED', 2, '{"destination":"成都","startDate":"2024-02-05","endDate":"2024-02-07","reason":"技术交流"}', '2024-01-04 17:00:00', NOW(), NOW()),
(17, 1, '请假申请-唐九', 18, 'PENDING', NULL, '{"reason":"婚假","startDate":"2024-02-10","endDate":"2024-02-17","days":5}', '2024-01-05 21:00:00', NOW(), NOW()),
(18, 2, '报销申请-冯十', 19, 'APPROVED', 2, '{"amount":120,"type":"办公","description":"打印资料"}', '2024-01-04 18:00:00', NOW(), NOW()),
(19, 3, '出差申请-于十一', 20, 'PENDING', NULL, '{"destination":"武汉","startDate":"2024-02-15","endDate":"2024-02-16","reason":"培训"}', '2024-01-05 22:00:00', NOW(), NOW()),
(20, 1, '请假申请-董十二', 21, 'APPROVED', 2, '{"reason":"年假","startDate":"2024-03-01","endDate":"2024-03-05","days":5}', '2024-01-04 19:00:00', NOW(), NOW());

-- =============================================
-- 8. 审批节点测试数据（用于模板定义）
-- =============================================
INSERT INTO `im_approval_node` (`id`, `template_id`, `node_key`, `node_name`, `node_type`, `approvers`, `sort_order`, `create_time`) VALUES
(1, 1, 'manager', '部门经理审批', 'ANY', '3', 1, NOW()),
(2, 1, 'hr', '人事审批', 'ANY', '7', 2, NOW()),
(3, 2, 'manager', '部门经理审批', 'ANY', '3', 1, NOW()),
(4, 2, 'finance', '财务审批', 'ANY', '8', 2, NOW()),
(5, 3, 'manager', '部门经理审批', 'ANY', '3', 1, NOW());

-- =============================================
-- 9. 审批记录测试数据（30条）
-- =============================================
INSERT INTO `im_approval_record` (`id`, `approval_id`, `node_id`, `approver_id`, `action`, `comment`, `action_time`) VALUES
(1, 2, 1, 3, 'APPROVE', '同意请假', '2024-01-05 10:30:00'),
(2, 2, 2, 7, 'APPROVE', '人事确认无误', '2024-01-05 14:00:00'),
(3, 4, 3, 3, 'APPROVE', '报销合理', '2024-01-04 10:00:00'),
(4, 4, 4, 8, 'APPROVE', '财务审核通过', '2024-01-04 14:00:00'),
(5, 6, 3, 3, 'APPROVE', '部门同意', '2024-01-04 11:00:00'),
(6, 6, 4, 8, 'REJECT', '超出预算', '2024-01-04 14:30:00'),
(7, 8, 1, 3, 'APPROVE', '同意', '2024-01-05 17:00:00'),
(8, 8, 2, 7, 'APPROVE', '人事确认', '2024-01-05 18:00:00'),
(9, 10, 5, 3, 'APPROVE', '同意出差', '2024-01-04 15:00:00'),
(10, 12, 3, 3, 'APPROVE', '同意报销', '2024-01-04 16:00:00'),
(11, 12, 4, 8, 'APPROVE', '财务通过', '2024-01-04 17:00:00'),
(12, 14, 1, 3, 'APPROVE', '同意', '2024-01-04 17:00:00'),
(13, 14, 2, 7, 'APPROVE', '人事确认', '2024-01-04 18:00:00'),
(14, 16, 5, 3, 'APPROVE', '同意出差', '2024-01-04 18:00:00'),
(15, 18, 3, 3, 'APPROVE', '同意', '2024-01-04 19:00:00'),
(16, 18, 4, 8, 'APPROVE', '财务审核通过', '2024-01-04 20:00:00'),
(17, 20, 1, 3, 'APPROVE', '同意年假', '2024-01-04 20:00:00'),
(18, 20, 2, 7, 'APPROVE', '人事确认', '2024-01-04 21:00:00'),
(19, 1, 1, 3, 'APPROVE', '同意请假', '2024-01-05 23:00:00'),
(20, 3, 1, 3, 'APPROVE', '同意', '2024-01-05 23:30:00'),
(21, 5, 3, 3, 'APPROVE', '部门同意', '2024-01-05 23:45:00'),
(22, 7, 5, 3, 'APPROVE', '同意出差', '2024-01-06 00:00:00'),
(23, 9, 3, 3, 'APPROVE', '同意报销', '2024-01-06 00:15:00'),
(24, 11, 1, 3, 'APPROVE', '同意', '2024-01-06 00:30:00'),
(25, 13, 5, 3, 'APPROVE', '同意出差', '2024-01-06 00:45:00'),
(26, 15, 3, 3, 'APPROVE', '部门同意', '2024-01-06 01:00:00'),
(27, 17, 1, 3, 'APPROVE', '同意婚假', '2024-01-06 01:15:00'),
(28, 19, 5, 3, 'APPROVE', '同意培训', '2024-01-06 01:30:00'),
(29, 2, 1, 3, 'APPROVE', '补充审批', '2024-01-06 01:45:00'),
(30, 4, 3, 3, 'APPROVE', '补充确认', '2024-01-06 02:00:00');

-- =============================================
-- 10. 考勤测试数据（30条）
-- =============================================
INSERT INTO `im_attendance` (`id`, `user_id`, `attendance_date`, `check_in_time`, `check_out_time`, `check_in_status`, `check_out_status`, `work_minutes`, `attendance_type`, `check_in_location`, `device_info`, `create_time`, `update_time`) VALUES
(1, 2, '2024-01-02', '2024-01-02 08:55:00', '2024-01-02 18:05:00', 'NORMAL', 'NORMAL', 550, 'WORK', '公司', 'iPhone 14', NOW(), NOW()),
(2, 2, '2024-01-03', '2024-01-03 09:05:00', '2024-01-03 18:00:00', 'LATE', 'NORMAL', 535, 'WORK', '公司', 'iPhone 14', NOW(), NOW()),
(3, 2, '2024-01-04', '2024-01-04 08:50:00', '2024-01-04 17:55:00', 'NORMAL', 'EARLY', 545, 'WORK', '公司', 'iPhone 14', NOW(), NOW()),
(4, 2, '2024-01-05', '2024-01-05 09:00:00', '2024-01-05 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', '公司', 'iPhone 14', NOW(), NOW()),
(5, 3, '2024-01-02', '2024-01-02 08:45:00', '2024-01-02 18:10:00', 'NORMAL', 'NORMAL', 565, 'WORK', '公司', 'Android', NOW(), NOW()),
(6, 3, '2024-01-03', '2024-01-03 09:15:00', '2024-01-03 18:00:00', 'LATE', 'NORMAL', 525, 'WORK', '公司', 'Android', NOW(), NOW()),
(7, 3, '2024-01-04', '2024-01-04 08:55:00', '2024-01-04 18:00:00', 'NORMAL', 'NORMAL', 545, 'WORK', '公司', 'Android', NOW(), NOW()),
(8, 4, '2024-01-02', '2024-01-02 09:00:00', '2024-01-02 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', '公司', 'Windows', NOW(), NOW()),
(9, 4, '2024-01-03', '2024-01-03 08:50:00', '2024-01-03 18:05:00', 'NORMAL', 'NORMAL', 555, 'WORK', '公司', 'Windows', NOW(), NOW()),
(10, 4, '2024-01-04', '2024-01-04 09:20:00', '2024-01-04 17:50:00', 'LATE', 'EARLY', 510, 'WORK', '公司', 'Windows', NOW(), NOW()),
(11, 5, '2024-01-02', '2024-01-02 08:58:00', '2024-01-02 18:02:00', 'NORMAL', 'NORMAL', 544, 'WORK', '公司', 'iPhone 13', NOW(), NOW()),
(12, 5, '2024-01-03', '2024-01-03 09:00:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', '公司', 'iPhone 13', NOW(), NOW()),
(13, 5, '2024-01-04', '2024-01-04 08:55:00', '2024-01-04 18:00:00', 'NORMAL', 'NORMAL', 545, 'WORK', '公司', 'iPhone 13', NOW(), NOW()),
(14, 6, '2024-01-02', '2024-01-02 09:10:00', '2024-01-02 18:00:00', 'LATE', 'NORMAL', 530, 'WORK', '公司', 'Android', NOW(), NOW()),
(15, 6, '2024-01-03', '2024-01-03 08:50:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 550, 'WORK', '公司', 'Android', NOW(), NOW()),
(16, 6, '2024-01-04', '2024-01-04 09:00:00', '2024-01-04 17:45:00', 'NORMAL', 'EARLY', 525, 'WORK', '公司', 'Android', NOW(), NOW()),
(17, 7, '2024-01-02', '2024-01-02 08:55:00', '2024-01-02 18:00:00', 'NORMAL', 'NORMAL', 545, 'WORK', '公司', 'iPhone', NOW(), NOW()),
(18, 7, '2024-01-03', '2024-01-03 09:00:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', '公司', 'iPhone', NOW(), NOW()),
(19, 7, '2024-01-04', '2024-01-04 08:50:00', '2024-01-04 18:05:00', 'NORMAL', 'NORMAL', 555, 'WORK', '公司', 'iPhone', NOW(), NOW()),
(20, 8, '2024-01-02', '2024-01-02 09:05:00', '2024-01-02 18:00:00', 'LATE', 'NORMAL', 535, 'WORK', '公司', 'Windows', NOW(), NOW()),
(21, 8, '2024-01-03', '2024-01-03 08:55:00', '2024-01-03 17:55:00', 'NORMAL', 'EARLY', 540, 'WORK', '公司', 'Windows', NOW(), NOW()),
(22, 8, '2024-01-04', '2024-01-04 09:00:00', '2024-01-04 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', '公司', 'Windows', NOW(), NOW()),
(23, 9, '2024-01-02', '2024-01-02 08:50:00', '2024-01-02 18:10:00', 'NORMAL', 'NORMAL', 560, 'WORK', '公司', 'MacBook', NOW(), NOW()),
(24, 9, '2024-01-03', '2024-01-03 09:00:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', '公司', 'MacBook', NOW(), NOW()),
(25, 9, '2024-01-04', '2024-01-04 08:55:00', '2024-01-04 18:00:00', 'NORMAL', 'NORMAL', 545, 'WORK', '公司', 'MacBook', NOW(), NOW()),
(26, 10, '2024-01-02', '2024-01-02 09:00:00', '2024-01-02 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', '公司', 'iPad', NOW(), NOW()),
(27, 10, '2024-01-03', '2024-01-03 08:58:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 542, 'WORK', '公司', 'iPad', NOW(), NOW()),
(28, 10, '2024-01-04', '2024-01-04 09:15:00', '2024-01-04 17:50:00', 'LATE', 'EARLY', 515, 'WORK', '公司', 'iPad', NOW(), NOW()),
(29, 11, '2024-01-02', '2024-01-02 08:55:00', '2024-01-02 18:05:00', 'NORMAL', 'NORMAL', 550, 'WORK', '公司', 'iPhone', NOW(), NOW()),
(30, 11, '2024-01-03', '2024-01-03 09:00:00', '2024-01-03 18:00:00', 'NORMAL', 'NORMAL', 540, 'WORK', '公司', 'iPhone', NOW(), NOW());

-- =============================================
-- 11. 审计日志测试数据（20条）
-- =============================================
INSERT INTO `im_audit_log` (`id`, `user_id`, `operation_type`, `target_type`, `target_id`, `operation_result`, `error_message`, `ip_address`, `user_agent`, `create_time`) VALUES
(1, 2, 'LOGIN', 'USER', 2, 'SUCCESS', NULL, '192.168.1.100', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', NOW()),
(2, 2, 'SEND_MESSAGE', 'MESSAGE', 1, 'SUCCESS', NULL, '192.168.1.100', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', NOW()),
(3, 3, 'LOGIN', 'USER', 3, 'SUCCESS', NULL, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW()),
(4, 3, 'SEND_MESSAGE', 'MESSAGE', 2, 'SUCCESS', NULL, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW()),
(5, 4, 'LOGIN', 'USER', 4, 'SUCCESS', NULL, '192.168.1.102', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', NOW()),
(6, 4, 'CREATE_GROUP', 'GROUP', 1, 'SUCCESS', NULL, '192.168.1.102', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', NOW()),
(7, 5, 'LOGIN', 'USER', 5, 'SUCCESS', NULL, '192.168.1.103', 'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X)', NOW()),
(8, 5, 'SUBMIT_APPROVAL', 'APPROVAL', 4, 'SUCCESS', NULL, '192.168.1.103', 'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X)', NOW()),
(9, 6, 'LOGIN', 'USER', 6, 'SUCCESS', NULL, '192.168.1.104', 'Mozilla/5.0 (Linux; Android 12)', NOW()),
(10, 6, 'APPROVE_APPROVAL', 'APPROVAL', 4, 'SUCCESS', NULL, '192.168.1.104', 'Mozilla/5.0 (Linux; Android 12)', NOW()),
(11, 7, 'LOGIN', 'USER', 7, 'SUCCESS', NULL, '192.168.1.105', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', NOW()),
(12, 7, 'UPLOAD_FILE', 'FILE', 1, 'SUCCESS', NULL, '192.168.1.105', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', NOW()),
(13, 8, 'LOGIN', 'USER', 8, 'SUCCESS', NULL, '192.168.1.106', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW()),
(14, 8, 'SEND_MESSAGE', 'MESSAGE', 22, 'SUCCESS', NULL, '192.168.1.106', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW()),
(15, 9, 'LOGIN', 'USER', 9, 'SUCCESS', NULL, '192.168.1.107', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', NOW()),
(16, 9, 'APPROVE_APPROVAL', 'APPROVAL', 8, 'SUCCESS', NULL, '192.168.1.107', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', NOW()),
(17, 10, 'LOGIN', 'USER', 10, 'SUCCESS', NULL, '192.168.1.108', 'Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X)', NOW()),
(18, 10, 'CHECK_IN', 'ATTENDANCE', 26, 'SUCCESS', NULL, '192.168.1.108', 'Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X)', NOW()),
(19, 11, 'LOGIN', 'USER', 11, 'SUCCESS', NULL, '192.168.1.109', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', NOW()),
(20, 11, 'SEND_MESSAGE', 'MESSAGE', 30, 'SUCCESS', NULL, '192.168.1.109', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X)', NOW());

-- =============================================
-- 数据统计摘要
-- =============================================
-- 用户数据: 20条 (im_user)
-- 群组数据: 10条 (im_group)
-- 群组成员: 35条 (im_group_member)
-- 会话数据: 15条 (im_conversation)
-- 消息数据: 30条 (im_message)
-- 审批模板: 3条 (im_approval_template)
-- 审批数据: 20条 (im_approval)
-- 审批节点: 5条 (im_approval_node)
-- 审批记录: 30条 (im_approval_record)
-- 考勤数据: 30条 (im_attendance)
-- 审计日志: 20条 (im_audit_log)
-- 总计: 218条测试数据
