/*
 Navicat Premium Dump SQL

 Source Server         : 172.168.20.222
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44-log)
 Source Host           : 172.168.20.222:3306
 Source Schema         : im

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44-log)
 File Encoding         : 65001

 Date: 08/01/2026 12:17:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for im_user
-- ----------------------------
DROP TABLE IF EXISTS `im_user`;
CREATE TABLE `im_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '???BCrypt???',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '??',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??URL',
  `gender` tinyint(1) NULL DEFAULT 0 COMMENT '???0?? 1? 2??',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '???0?? 1???',
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '??????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_mobile`(`mobile`) USING BTREE,
  INDEX `idx_email`(`email`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_last_online_time`(`last_online_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'IM???' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of im_user
-- ----------------------------
INSERT INTO `im_user` VALUES (2, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC2', 1, '13800000002', 'zhangsan@example.com', 1, '努力工作，快乐生活', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (3, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC3', 1, '13800000003', 'lisi@example.com', 1, '学无止境', '2026-01-08 12:16:27', '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (4, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC4', 1, '13800000004', 'wangwu@example.com', 1, '代码改变世界', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (5, 'zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC5', 0, '13800000005', 'zhaoliu@example.com', 1, '保持热爱', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (6, 'sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC6', 1, '13800000006', 'sunqi@example.com', 1, '今天也要加油', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (7, 'zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC7', 0, '13800000007', 'zhouba@example.com', 1, '未来可期', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (8, 'wujiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '吴九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC8', 1, '13800000008', 'wujiu@example.com', 1, '行稳致远', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (9, 'zhengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '郑十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fC9', 1, '13800000009', 'zhengshi@example.com', 1, '不忘初心', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (10, 'chenyi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCA', 0, '13800000010', 'chenyi@example.com', 1, '砥砺前行', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (11, 'liner', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '林二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCB', 1, '13800000011', 'liner@example.com', 1, '天天向上', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (12, 'huangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '黄三', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCC', 1, '13800000012', 'huangsan@example.com', 1, '追求卓越', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (13, 'liusi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘四', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCD', 0, '13800000013', 'liusi@example.com', 1, '梦想成真', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (14, 'hewu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '何五', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCE', 1, '13800000014', 'hewu@example.com', 1, '珍惜当下', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (15, 'luoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '罗六', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCF', 1, '13800000015', 'luoliu@example.com', 1, '勇往直前', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (16, 'liangqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '梁七', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCG', 0, '13800000016', 'liangqi@example.com', 1, '脚踏实地', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (17, 'songba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '宋八', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCH', 1, '13800000017', 'songba@example.com', 1, '天道酬勤', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (18, 'tangjiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '唐九', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCI', 1, '13800000018', 'tangjiu@example.com', 1, '感恩有你', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (19, 'fengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '冯十', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCJ', 0, '13800000019', 'fengshi@example.com', 1, '知足常乐', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (20, 'yushiyi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '于十一', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCK', 1, '13800000020', 'yushiyi@example.com', 1, '生活美好', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');
INSERT INTO `im_user` VALUES (21, 'dongshier', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '董十二', 'https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83qJZ8fCL', 1, '13800000021', 'dongshier@example.com', 1, '越努力越幸运', NULL, '2026-01-08 12:12:34', '2026-01-08 12:16:13');

SET FOREIGN_KEY_CHECKS = 1;
