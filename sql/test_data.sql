-- =============================================
-- 测试数据生成脚本
-- 为各核心表生成至少20条测试数据
-- =============================================

-- 清空旧测试数据（可选，谨慎使用）
-- DELETE FROM im_attendance WHERE id > 0;
-- DELETE FROM im_approval WHERE id > 0;
-- DELETE FROM im_approval_node WHERE id > 0;
-- DELETE FROM im_message WHERE id > 0;
-- DELETE FROM im_conversation WHERE id > 0;
-- DELETE FROM im_group WHERE id > 0;
-- DELETE FROM im_group_member WHERE id > 0;
-- DELETE FROM im_user WHERE id > 1;

-- =============================================
-- 1. 用户测试数据（20个用户）
-- =============================================
INSERT INTO `im_user` (`id`, `user_id`, `nickname`, `avatar`, `gender`, `phone`, `email`, `status`, `create_time`) VALUES
(2, 1002, '张三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC2', '1', '13800000002', 'zhangsan@example.com', '1', NOW()),
(3, 1003, '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC3', '1', '13800000003', 'lisi@example.com', '1', NOW()),
(4, 1004, '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC4', '1', '13800000004', 'wangwu@example.com', '1', NOW()),
(5, 1005, '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC5', '0', '13800000005', 'zhaoliu@example.com', '1', NOW()),
(6, 1006, '孙七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC6', '1', '13800000006', 'sunqi@example.com', '1', NOW()),
(7, 1007, '周八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC7', '0', '13800000007', 'zhouba@example.com', '1', NOW()),
(8, 1008, '吴九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC8', '1', '13800000008', 'wujiu@example.com', '1', NOW()),
(9, 1009, '郑十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC9', '1', '13800000009', 'zhengshi@example.com', '1', NOW()),
(10, 1010, '陈一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCA', '0', '13800000010', 'chenyi@example.com', '1', NOW()),
(11, 1011, '林二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCB', '1', '13800000011', 'liner@example.com', '1', NOW()),
(12, 1012, '黄三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCC', '1', '13800000012', 'huangsan@example.com', '1', NOW()),
(13, 1013, '刘四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCD', '0', '13800000013', 'liusi@example.com', '1', NOW()),
(14, 1014, '何五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCE', '1', '13800000014', 'hewu@example.com', '1', NOW()),
(15, 1015, '罗六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCF', '1', '13800000015', 'luoliu@example.com', '1', NOW()),
(16, 1016, '梁七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG', '0', '13800000016', 'liangqi@example.com', '1', NOW()),
(17, 1017, '宋八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCH', '1', '13800000017', 'songba@example.com', '1', NOW()),
(18, 1018, '唐九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCI', '1', '13800000018', 'tangjiu@example.com', '1', NOW()),
(19, 1019, '冯十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCJ', '0', '13800000019', 'fengshi@example.com', '1', NOW()),
(20, 1020, '于十一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCK', '1', '13800000020', 'yushiyi@example.com', '1', NOW()),
(21, 1021, '董十二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCL', '1', '13800000021', 'dongshier@example.com', '1', NOW());

-- =============================================
-- 2. 群组测试数据（10个群组）
-- =============================================
INSERT INTO `im_group` (`id`, `group_id`, `group_name`, `avatar`, `owner_id`, `max_members`, `notice`, `introduction`, `create_time`) VALUES
(1, 1001, '技术交流群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG1', 2, 500, '欢迎大家交流技术问题', '技术爱好者的聚集地', NOW()),
(2, 1002, '产品讨论群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG2', 3, 200, '产品需求讨论', '产品经理和设计师的交流群', NOW()),
(3, 1003, '项目开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG3', 4, 50, '项目进度同步', '核心开发团队', NOW()),
(4, 1004, '公司全员群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG4', 2, 1000, '公司通知发布', '全体员工', NOW()),
(5, 1005, '篮球爱好者', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG5', 5, 100, '每周组织篮球活动', '运动使人快乐', NOW()),
(6, 1006, '读书分享会', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG6', 6, 200, '分享读书心得', '阅读改变人生', NOW()),
(7, 1007, '美食分享群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG7', 7, 300, '分享美食探店', '唯有美食不可辜负', NOW()),
(8, 1008, '摄影俱乐部', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG8', 8, 150, '摄影作品交流', '记录美好瞬间', NOW()),
(9, 1009, '户外运动群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG9', 9, 200, '周末户外活动', '亲近自然', NOW()),
(10, 1010, '音乐分享群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCGA', 10, 500, '分享好听的音乐', '音乐治愈心灵', NOW());

-- =============================================
-- 3. 群组成员测试数据
-- =============================================
INSERT INTO `im_group_member` (`group_id`, `user_id`, `nickname`, `role`, `join_time`, `mute_end_time`) VALUES
-- 技术交流群成员
(1001, 2, '张三', 'OWNER', NOW(), NULL),
(1001, 3, '李四', 'ADMIN', NOW(), NULL),
(1001, 4, '王五', 'MEMBER', NOW(), NULL),
(1001, 5, '赵六', 'MEMBER', NOW(), NULL),
(1001, 6, '孙七', 'MEMBER', NOW(), NULL),
-- 产品讨论群成员
(1002, 3, '李四', 'OWNER', NOW(), NULL),
(1002, 7, '周八', 'ADMIN', NOW(), NULL),
(1002, 8, '吴九', 'MEMBER', NOW(), NULL),
(1002, 9, '郑十', 'MEMBER', NOW(), NULL),
-- 项目开发群成员
(1003, 4, '王五', 'OWNER', NOW(), NULL),
(1003, 10, '陈一', 'ADMIN', NOW(), NULL),
(1003, 11, '林二', 'MEMBER', NOW(), NULL),
(1003, 12, '黄三', 'MEMBER', NOW(), NULL),
-- 公司全员群成员（所有人）
(1004, 2, '张三', 'ADMIN', NOW(), NULL),
(1004, 3, '李四', 'MEMBER', NOW(), NULL),
(1004, 4, '王五', 'MEMBER', NOW(), NULL),
(1004, 5, '赵六', 'MEMBER', NOW(), NULL),
(1004, 6, '孙七', 'MEMBER', NOW(), NULL),
(1004, 7, '周八', 'MEMBER', NOW(), NULL),
(1004, 8, '吴九', 'MEMBER', NOW(), NULL),
(1004, 9, '郑十', 'MEMBER', NOW(), NULL),
(1004, 10, '陈一', 'MEMBER', NOW(), NULL),
(1004, 11, '林二', 'MEMBER', NOW(), NULL),
(1004, 12, '黄三', 'MEMBER', NOW(), NULL),
-- 其他群成员
(1005, 5, '赵六', 'OWNER', NOW(), NULL),
(1005, 13, '刘四', 'MEMBER', NOW(), NULL),
(1005, 14, '何五', 'MEMBER', NOW(), NULL),
(1006, 6, '孙七', 'OWNER', NOW(), NULL),
(1006, 15, '罗六', 'MEMBER', NOW(), NULL),
(1006, 16, '梁七', 'MEMBER', NOW(), NULL),
(1007, 7, '周八', 'OWNER', NOW(), NULL),
(1007, 17, '宋八', 'MEMBER', NOW(), NOW()),
(1007, 18, '唐九', 'MEMBER', NOW(), NOW()),
(1008, 8, '吴九', 'OWNER', NOW(), NULL),
(1008, 19, '冯十', 'MEMBER', NOW(), NULL),
(1008, 20, '于十一', 'MEMBER', NOW(), NOW()),
(1009, 9, '郑十', 'OWNER', NOW(), NOW()),
(1009, 21, '董十二', 'MEMBER', NOW(), NOW()),
(1010, 10, '陈一', 'OWNER', NOW(), NOW()),
(1010, 2, '张三', 'MEMBER', NOW(), NOW()),
(1010, 3, '李四', 'MEMBER', NOW(), NOW());

-- =============================================
-- 4. 会话测试数据
-- =============================================
INSERT INTO `im_conversation` (`type`, `target_id`, `user_id`, `name`, `avatar`, `unread_count`, `is_pinned`, `is_muted`, `sort_order`, `create_time`) VALUES
('PRIVATE', 1003, 2, '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC4', 0, 0, 0, 1, NOW()),
('PRIVATE', 1002, 2, '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC3', 3, 0, 0, 2, NOW()),
('PRIVATE', 1004, 2, '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC5', 0, 0, 0, 3, NOW()),
('PRIVATE', 1005, 2, '孙七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC6', 0, 0, 0, 4, NOW()),
('PRIVATE', 1006, 2, '周八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC7', 0, 0, 0, 5, NOW()),
('GROUP', 1001, 2, '技术交流群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG1', 10, 1, 0, 10, NOW()),
('GROUP', 1002, 2, '产品讨论群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG2', 5, 0, 0, 11, NOW()),
('GROUP', 1003, 2, '项目开发群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG3', 2, 0, 0, 12, NOW()),
('GROUP', 1004, 2, '公司全员群', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG4', 50, 1, 0, 13, NOW()),
('PRIVATE', 1007, 2, '周八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC7', 0, 0, 0, 6, NOW());

-- =============================================
-- 5. 消息测试数据（30条）
-- =============================================
INSERT INTO `im_message` (`conversation_id`, `conversation_type`, `sender_id`, `content`, `msg_type`, `send_time`, `is_revoked`, `create_time`) VALUES
-- 单聊消息
(1, 'PRIVATE', 2, '你好，在吗？', 'TEXT', NOW() - INTERVAL 5 DAY, 0, NOW() - INTERVAL 5 DAY),
(1, 'PRIVATE', 1004, '在的，有什么事吗？', 'TEXT', NOW() - INTERVAL 5 DAY + INTERVAL 1 MINUTE, 0, NOW() - INTERVAL 5 DAY + INTERVAL 1 MINUTE),
(1, 'PRIVATE', 2, '想请教一个技术问题', 'TEXT', NOW() - INTERVAL 5 DAY + INTERVAL 2 MINUTE, 0, NOW() - INTERVAL 5 DAY + INTERVAL 2 MINUTE),
(1, 'PRIVATE', 1004, '请说', 'TEXT', NOW() - INTERVAL 5 DAY + INTERVAL 3 MINUTE, 0, NOW() - INTERVAL 5 DAY + INTERVAL 3 MINUTE),
(2, 'PRIVATE', 2, '项目进展如何？', 'TEXT', NOW() - INTERVAL 4 DAY, 0, NOW() - INTERVAL 4 DAY),
(2, 'PRIVATE', 1003, '一切顺利，按计划进行', 'TEXT', NOW() - INTERVAL 4 DAY + INTERVAL 1 HOUR, 0, NOW() - INTERVAL 4 DAY + INTERVAL 1 HOUR),
(3, 'PRIVATE', 2, '周末有空吗？一起打球', 'TEXT', NOW() - INTERVAL 3 DAY, 0, NOW() - INTERVAL 3 DAY),
(3, 'PRIVATE', 1005, '好啊，什么时候？', 'TEXT', NOW() - INTERVAL 3 DAY + INTERVAL 30 MINUTE, 0, NOW() - INTERVAL 3 DAY + INTERVAL 30 MINUTE),
(3, 'PRIVATE', 2, '周六下午怎么样？', 'TEXT', NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR, 0, NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR),
-- 群聊消息
(6, 'GROUP', 3, '大家注意，明天开会', 'TEXT', NOW() - INTERVAL 2 DAY, 0, NOW() - INTERVAL 2 DAY),
(6, 'GROUP', 4, '收到，几点开始？', 'TEXT', NOW() - INTERVAL 2 DAY + INTERVAL 10 MINUTE, 0, NOW() - INTERVAL 2 DAY + INTERVAL 10 MINUTE),
(6, 'GROUP', 3, '上午10点，会议室A', 'TEXT', NOW() - INTERVAL 2 DAY + INTERVAL 20 MINUTE, 0, NOW() - INTERVAL 2 DAY + INTERVAL 20 MINUTE),
(7, 'GROUP', 2, '新版本已发布，请大家测试', 'TEXT', NOW() - INTERVAL 1 DAY, 0, NOW() - INTERVAL 1 DAY),
(7, 'GROUP', 7, '收到，马上测试', 'TEXT', NOW() - INTERVAL 1 DAY + INTERVAL 5 MINUTE, 0, NOW() - INTERVAL 1 DAY + INTERVAL 5 MINUTE),
(7, 'GROUP', 8, '已提交测试报告', 'TEXT', NOW() - INTERVAL 1 DAY + INTERVAL 2 HOUR, 0, NOW() - INTERVAL 1 DAY + INTERVAL 2 HOUR),
(8, 'GROUP', 4, '本周五有团建活动', 'TEXT', NOW() - INTERVAL 12 HOUR, 0, NOW() - INTERVAL 12 HOUR),
(8, 'GROUP', 5, '太好了，期待！', 'TEXT', NOW() - INTERVAL 12 HOUR + INTERVAL 15 MINUTE, 0, NOW() - INTERVAL 12 HOUR + INTERVAL 15 MINUTE),
(9, 'GROUP', 2, '公司放假通知：春节放假7天', 'TEXT', NOW() - INTERVAL 6 HOUR, 0, NOW() - INTERVAL 6 HOUR),
(9, 'GROUP', 10, '收到通知，谢谢！', 'TEXT', NOW() - INTERVAL 6 HOUR + INTERVAL 30 MINUTE, 0, NOW() - INTERVAL 6 HOUR + INTERVAL 30 MINUTE),
-- 更多消息
(1, 'PRIVATE', 1004, '问题已解决，谢谢！', 'TEXT', NOW() - INTERVAL 1 HOUR, 0, NOW() - INTERVAL 1 HOUR),
(2, 'PRIVATE', 2, '好的，保持联系', 'TEXT', NOW() - INTERVAL 30 MINUTE, 0, NOW() - INTERVAL 30 MINUTE),
(6, 'GROUP', 6, '会议推迟到下午2点', 'TEXT', NOW() - INTERVAL 1 HOUR, 0, NOW() - INTERVAL 1 HOUR),
(6, 'GROUP', 7, '好的，收到', 'TEXT', NOW() - INTERVAL 1 HOUR + INTERVAL 5 MINUTE, 0, NOW() - INTERVAL 1 HOUR + INTERVAL 5 MINUTE),
(10, 'GROUP', 10, '分享一首好听的歌给大家', 'TEXT', NOW() - INTERVAL 30 MINUTE, 0, NOW() - INTERVAL 30 MINUTE),
(6, 'GROUP', 2, '这个项目的技术栈是什么？', 'TEXT', NOW() - INTERVAL 15 MINUTE, 0, NOW() - INTERVAL 15 MINUTE),
(6, 'GROUP', 11, 'Spring Boot + Vue3', 'TEXT', NOW() - INTERVAL 15 MINUTE + INTERVAL 1 MINUTE, 0, NOW() - INTERVAL 15 MINUTE + INTERVAL 1 MINUTE),
(7, 'GROUP', 12, '界面很漂亮，点赞！', 'TEXT', NOW() - INTERVAL 10 MINUTE, 0, NOW() - INTERVAL 10 MINUTE),
(8, 'GROUP', 13, '大家记得提交周报', 'TEXT', NOW() - INTERVAL 5 MINUTE, 0, NOW() - INTERVAL 5 MINUTE),
(9, 'GROUP', 14, '已提交', 'TEXT', NOW() - INTERVAL 3 MINUTE, 0, NOW() - INTERVAL 3 MINUTE),
(6, 'GROUP', 2, '@所有人 明天上午9点全员大会', 'TEXT', NOW(), 0, NOW());

-- =============================================
-- 6. 审批测试数据（20条审批记录）
-- =============================================
INSERT INTO `im_approval` (`id`, `template_id`, `title`, `applicant_id`, `status`, `current_node_id`, `create_time`, `update_time`) VALUES
(1, 1, '张三的请假申请', 2, 'APPROVED', 2, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY),
(2, 1, '李四的请假申请', 3, 'APPROVED', 2, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 8 DAY),
(3, 1, '王五的请假申请', 4, 'PENDING', NULL, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
(4, 2, '赵六的报销申请', 5, 'APPROVED', 2, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY),
(5, 2, '孙七的报销申请', 6, 'REJECTED', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(6, 2, '周八的报销申请', 7, 'PENDING', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(7, 3, '吴九的出差申请', 8, 'APPROVED', 2, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 3 DAY),
(8, 3, '郑十的出差申请', 9, 'PENDING', NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(9, 4, '陈一的采购申请', 10, 'APPROVED', 3, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(10, 4, '林二的采购申请', 11, 'PENDING', NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(11, 5, '黄三的用印申请', 12, 'APPROVED', 2, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 11 HOUR),
(12, 5, '刘四的用印申请', 13, 'PENDING', NULL, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 6 HOUR),
(13, 6, '何五的加班申请', 14, 'APPROVED', 2, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 1 HOUR),
(14, 6, '罗六的加班申请', 15, 'PENDING', NULL, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR),
(15, 7, '梁七的转正申请', 16, 'APPROVED', 2, NOW(), NOW() - INTERVAL 30 MINUTE),
(16, 7, '宋八的转正申请', 17, 'PENDING', NULL, NOW(), NOW()),
(17, 8, '唐九的公文发布', 18, 'APPROVED', 2, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 20 MINUTE),
(18, 8, '冯十的公文发布', 19, 'PENDING', NULL, NOW() - INTERVAL 15 MINUTE, NOW() - INTERVAL 15 MINUTE),
(19, 1, '于十一的请假申请', 20, 'APPROVED', 2, NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 5 MINUTE),
(20, 1, '董十二的请假申请', 21, 'PENDING', NULL, NOW() - INTERVAL 5 MINUTE, NOW() - INTERVAL 5 MINUTE);

-- =============================================
-- 7. 审批节点测试数据
-- =============================================
INSERT INTO `im_approval_node` (`approval_id`, `node_name`, `node_type`, `approver_ids`, `approve_type`, `sort_order`, `status`, `process_time`, `processor_id`) VALUES
(1, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'APPROVED', NOW() - INTERVAL 10 DAY + INTERVAL 5 MINUTE, 3),
(1, '人事审批', 'APPROVE', '4', 'ANY', 2, 'APPROVED', NOW() - INTERVAL 9 DAY, 4),
(2, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'APPROVED', NOW() - INTERVAL 9 DAY + INTERVAL 5 MINUTE, 3),
(2, '人事审批', 'APPROVE', '4', 'ANY', 2, 'APPROVED', NOW() - INTERVAL 8 DAY, 4),
(3, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'APPROVED', NOW() - INTERVAL 8 DAY + INTERVAL 10 MINUTE, 3),
(3, '人事审批', 'APPROVE', '4', 'ANY', 2, 'PENDING', NULL, NULL),
(4, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'APPROVED', NOW() - INTERVAL 7 DAY + INTERVAL 1 HOUR, 3),
(4, '财务审批', 'APPROVE', '5', 'ANY', 2, 'APPROVED', NOW() - INTERVAL 6 DAY, 5),
(5, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'APPROVED', NOW() - INTERVAL 6 DAY + INTERVAL 30 MINUTE, 3),
(5, '财务审批', 'APPROVE', '5', 'ANY', 2, 'REJECTED', NOW() - INTERVAL 6 DAY + INTERVAL 2 HOUR, 5),
(6, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'PENDING', NULL, NULL),
(6, '财务审批', 'APPROVE', '5', 'ANY', 2, 'PENDING', NULL, NULL),
(7, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'APPROVED', NOW() - INTERVAL 4 DAY + INTERVAL 2 HOUR, 3),
(7, '总经理审批', 'APPROVE', '2', 'ANY', 2, 'APPROVED', NOW() - INTERVAL 3 DAY, 2),
(8, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'PENDING', NULL, NULL),
(8, '总经理审批', 'APPROVE', '2', 'ANY', 2, 'PENDING', NULL, NULL),
(9, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'APPROVED', NOW() - INTERVAL 2 DAY + INTERVAL 4 HOUR, 3),
(9, '财务审批', 'APPROVE', '5', 'ANY', 2, 'APPROVED', NOW() - INTERVAL 1 DAY, 5),
(9, '总经理审批', 'APPROVE', '2', 'ANY', 3, 'APPROVED', NOW() - INTERVAL 1 DAY, 2),
(10, '部门主管审批', 'APPROVE', '3', 'ANY', 1, 'PENDING', NULL, NULL),
(10, '财务审批', 'APPROVE', '5', 'ANY', 2, 'PENDING', NULL, NULL),
(10, '总经理审批', 'APPROVE', '2', 'ANY', 3, 'PENDING', NULL, NULL);

-- =============================================
-- 8. 审批记录测试数据
-- =============================================
INSERT INTO `im_approval_record` (`approval_id`, `node_id`, `approver_id`, `action`, `comment`, `action_time`) VALUES
(1, 1, 3, 'APPROVE', '同意', NOW() - INTERVAL 10 DAY + INTERVAL 5 MINUTE),
(1, 2, 4, 'APPROVE', '批准', NOW() - INTERVAL 9 DAY),
(2, 3, 3, 'APPROVE', '没问题', NOW() - INTERVAL 9 DAY + INTERVAL 5 MINUTE),
(2, 4, 4, 'APPROVE', '可以', NOW() - INTERVAL 8 DAY),
(3, 5, 3, 'APPROVE', '同意请假', NOW() - INTERVAL 8 DAY + INTERVAL 10 MINUTE),
(4, 6, 3, 'APPROVE', '费用合理', NOW() - INTERVAL 7 DAY + INTERVAL 1 HOUR),
(4, 7, 5, 'APPROVE', '批准报销', NOW() - INTERVAL 6 DAY),
(5, 8, 3, 'APPROVE', '可以先垫付', NOW() - INTERVAL 6 DAY + INTERVAL 30 MINUTE),
(5, 9, 5, 'REJECT', '缺少发票', NOW() - INTERVAL 6 DAY + INTERVAL 2 HOUR),
(7, 10, 3, 'APPROVE', '同意出差', NOW() - INTERVAL 4 DAY + INTERVAL 2 HOUR),
(7, 11, 2, 'APPROVE', '批准', NOW() - INTERVAL 3 DAY),
(9, 12, 3, 'APPROVE', '可以采购', NOW() - INTERVAL 2 DAY + INTERVAL 4 HOUR),
(9, 13, 5, 'APPROVE', '预算内', NOW() - INTERVAL 1 DAY),
(9, 14, 2, 'APPROVE', '同意', NOW() - INTERVAL 1 DAY),
(11, 15, 3, 'APPROVE', '可以', NOW() - INTERVAL 12 HOUR),
(13, 16, 3, 'APPROVE', '加班属实', NOW() - INTERVAL 2 HOUR),
(15, 17, 3, 'APPROVE', '同意转正', NOW() - INTERVAL 30 MINUTE),
(15, 18, 2, 'APPROVE', '欢迎加入', NOW() - INTERVAL 25 MINUTE),
(17, 19, 3, 'APPROVE', '同意发布', NOW() - INTERVAL 30 MINUTE),
(17, 20, 2, 'APPROVE', '批准', NOW() - INTERVAL 20 MINUTE),
(19, 21, 3, 'APPROVE', '同意', NOW() - INTERVAL 10 MINUTE),
(19, 22, 4, 'APPROVE', '可以', NOW() - INTERVAL 5 MINUTE);

-- =============================================
-- 9. 考勤打卡测试数据（30条）
-- =============================================
INSERT INTO `im_attendance` (`user_id`, `attendance_date`, `check_in_time`, `check_out_time`, `check_in_status`, `check_out_status`, `work_minutes`, `attendance_type`, `remark`, `create_time`, `update_time`) VALUES
-- 张三的打卡记录
(2, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 17 HOUR, 'NORMAL', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(2, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(2, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 17 HOUR, 'NORMAL', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(2, CURDATE() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 4 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(2, CURDATE() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 3 DAY - INTERVAL 17 HOUR, 'NORMAL', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
-- 李四的打卡记录
(3, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 8.5 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 17 HOUR, 'LATE', 'NORMAL', 510, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(3, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 18 HOUR, 'NORMAL', 'NORMAL', 600, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(3, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(3, CURDATE() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 4 DAY - INTERVAL 18 HOUR, 'NORMAL', 'NORMAL', 600, 'WORK', NULL, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(3, CURDATE() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY - INTERVAL 8.5 HOUR, NOW() - INTERVAL 3 DAY - INTERVAL 17 HOUR, 'LATE', 'NORMAL', 510, 'WORK', NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
-- 王五的打卡记录
(4, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 17 HOUR, 'NORMAL', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(4, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(4, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 17 HOUR, 'NORMAL', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(4, CURDATE() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 4 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(4, CURDATE() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 3 DAY - INTERVAL 18 HOUR, 'NORMAL', 'NORMAL', 600, 'WORK', NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
-- 其他用户打卡记录
(5, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 9 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 18 HOUR, 'LATE', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(5, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8.5 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 17 HOUR, 'LATE', 'NORMAL', 510, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(5, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(6, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 18 HOUR, 'NORMAL', 'NORMAL', 600, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(6, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(6, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 17 HOUR, 'NORMAL', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(7, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 8.5 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 17.5 HOUR, 'LATE', 'NORMAL', 570, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(7, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 18 HOUR, 'NORMAL', 'NORMAL', 600, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(7, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(8, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 9 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 18 HOUR, 'LATE', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(8, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8.5 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 17.5 HOUR, 'LATE', 'NORMAL', 570, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(8, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 18 HOUR, 'NORMAL', 'NORMAL', 600, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(9, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 17 HOUR, 'NORMAL', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(9, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 18 HOUR, 'NORMAL', 'NORMAL', 600, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(9, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 17.5 HOUR, 'NORMAL', 'EARLY', 570, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(10, CURDATE() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY - INTERVAL 8.5 HOUR, NOW() - INTERVAL 7 DAY - INTERVAL 17.5 HOUR, 'LATE', 'NORMAL', 570, 'WORK', NULL, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(10, CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 6 DAY - INTERVAL 18 HOUR, 'NORMAL', 'NORMAL', 600, 'WORK', NULL, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(10, CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 8 HOUR, NOW() - INTERVAL 5 DAY - INTERVAL 17 HOUR, 'NORMAL', 'NORMAL', 540, 'WORK', NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY);

-- =============================================
-- 10. 审计日志测试数据（20条）
-- =============================================
INSERT INTO `im_audit_log` (`user_id`, `user_name`, `module`, `operation_type`, `description`, `request_method`, `request_url`, `status`, `client_ip`, `operation_time`) VALUES
(2, '张三', 'LOGIN', 'LOGIN', '用户登录', 'POST', '/api/im/auth/login', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL 7 DAY),
(2, '张三', 'MESSAGE', 'SEND', '发送消息', 'POST', '/api/im/message/send', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL 7 DAY + INTERVAL 5 MINUTE),
(3, '李四', 'LOGIN', 'LOGIN', '用户登录', 'POST', '/api/im/auth/login', 'SUCCESS', '192.168.1.101', NOW() - INTERVAL 7 DAY),
(4, '王五', 'GROUP', 'CREATE', '创建群组', 'POST', '/api/im/group/create', 'SUCCESS', '192.168.1.102', NOW() - INTERVAL 6 DAY),
(5, '赵六', 'MESSAGE', 'SEND', '发送图片消息', 'POST', '/api/im/message/send', 'SUCCESS', '192.168.1.103', NOW() - INTERVAL 6 DAY),
(6, '孙七', 'APPROVAL', 'CREATE', '发起审批', 'POST', '/api/im/approval/create', 'SUCCESS', '192.168.1.104', NOW() - INTERVAL 5 DAY),
(7, '周八', 'APPROVAL', 'APPROVE', '审批通过', 'POST', '/api/im/approval/1/approve', 'SUCCESS', '192.168.1.105', NOW() - INTERVAL 5 DAY),
(8, '吴九', 'APPROVAL', 'REJECT', '审批驳回', 'POST', '/api/im/approval/2/reject', 'SUCCESS', '192.168.1.106', NOW() - INTERVAL 4 DAY),
(9, '郑十', 'FILE', 'UPLOAD', '上传文件', 'POST', '/api/im/file/upload', 'SUCCESS', '192.168.1.107', NOW() - INTERVAL 4 DAY),
(10, '陈一', 'GROUP', 'JOIN', '加入群组', 'POST', '/api/im/group/1001/join', 'SUCCESS', '192.168.1.108', NOW() - INTERVAL 3 DAY),
(11, '林二', 'MESSAGE', 'RECALL', '撤回消息', 'DELETE', '/api/im/message/123/recall', 'SUCCESS', '192.168.1.109', NOW() - INTERVAL 3 DAY),
(12, '黄三', 'ATTENDANCE', 'CHECK_IN', '上班打卡', 'POST', '/api/im/attendance/checkIn', 'SUCCESS', '192.168.1.110', NOW() - INTERVAL 2 DAY),
(13, '刘四', 'ATTENDANCE', 'CHECK_OUT', '下班打卡', 'POST', '/api/im/attendance/checkOut', 'SUCCESS', '192.168.1.111', NOW() - INTERVAL 2 DAY),
(14, '何五', 'APPROVAL', 'TRANSFER', '转交审批', 'POST', '/api/im/approval/3/transfer', 'SUCCESS', '192.168.1.112', NOW() - INTERVAL 1 DAY),
(15, '罗六', 'GROUP', 'CREATE', '创建群组', 'POST', '/api/im/group/create', 'SUCCESS', '192.168.1.113', NOW() - INTERVAL 1 DAY),
(16, '梁七', 'MESSAGE', 'SEND', '发送文件消息', 'POST', '/api/im/message/send', 'SUCCESS', '192.168.1.114', NOW() - INTERVAL 12 HOUR),
(17, '宋八', 'APPROVAL', 'DELEGATE', '委托审批', 'POST', '/api/im/approval/4/delegate', 'SUCCESS', '192.168.1.115', NOW() - INTERVAL 12 HOUR),
(18, '唐九', 'APPROVAL', 'CREATE', '发起审批', 'POST', '/api/im/approval/create', 'SUCCESS', '192.168.1.116', NOW() - INTERVAL 6 HOUR),
(19, '冯十', 'MESSAGE', 'FORWARD', '转发消息', 'POST', '/api/im/message/forward', 'SUCCESS', '192.168.1.117', NOW() - INTERVAL 5 HOUR),
(20, '于十一', 'LOGIN', 'LOGIN', '用户登录', 'POST', '/api/im/auth/login', 'SUCCESS', '192.168.1.118', NOW() - INTERVAL 4 HOUR),
(21, '董十二', 'GROUP', 'QUIT', '退出群组', 'DELETE', '/api/im/group/1001/leave', 'SUCCESS', '192.168.1.119', NOW() - INTERVAL 3 HOUR),
(2, '张三', 'LOGOUT', 'LOGOUT', '用户登出', 'POST', '/api/im/auth/logout', 'SUCCESS', '192.168.1.100', NOW() - INTERVAL 2 HOUR);

-- =============================================
-- 查询统计信息
-- =============================================
SELECT '用户数据' as table_name, COUNT(*) as count FROM im_user;
SELECT '群组数据' as table_name, COUNT(*) as count FROM im_group;
SELECT '群组成员' as table_name, COUNT(*) as count FROM im_group_member;
SELECT '会话数据' as table_name, COUNT(*) as count FROM im_conversation;
SELECT '消息数据' as table_name, COUNT(*) as count FROM im_message;
SELECT '审批数据' as table_name, COUNT(*) as count FROM im_approval;
SELECT '审批节点' as table_name, COUNT(*) as count FROM im_approval_node;
SELECT '考勤数据' as table_name, COUNT(*) as count FROM im_attendance;
SELECT '审计日志' as table_name, COUNT(*) as count FROM im_audit_log;
