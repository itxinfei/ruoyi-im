package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 测试数据初始化工具
 * 用于初始化张三、李四的好友关系数据
 *
 * 使用方式：
 * 1. 在 Spring Boot 应用中注入此工具
 * 2. 调用 initFriendData() 方法
 * 3. 或通过 API: POST /api/im/test-data/init-friends
 *
 * @author ruoyi
 */
@Component
public class TestDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(TestDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final ImRedisUtil imRedisUtil;

    public TestDataInitializer(JdbcTemplate jdbcTemplate, ImRedisUtil imRedisUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.imRedisUtil = imRedisUtil;
    }

    /**
     * 初始化好友关系数据
     * 为张三、李四添加所有其他用户为好友
     */
    public void initFriendData() {
        log.info("========== 开始初始化好友关系测试数据 ==========");

        try {
            // 获取张三和李四的用户ID
            Long zhangsanId = getUserIdByUsername("zhangsan");
            Long lisiId = getUserIdByUsername("lisi");

            if (zhangsanId == null) {
                log.warn("未找到用户 zhangsan，跳过初始化");
                return;
            }
            if (lisiId == null) {
                log.warn("未找到用户 lisi，跳过初始化");
                return;
            }

            log.info("张三ID: {}, 李四ID: {}", zhangsanId, lisiId);

            // 获取所有用户ID
            List<Long> allUserIds = getAllUserIds();
            log.info("系统共有 {} 个用户", allUserIds.size());

            // 为张三添加好友
            int zhangsanCount = addFriendsForUser(zhangsanId, allUserIds);
            log.info("为张三添加了 {} 个好友", zhangsanCount);

            // 为李四添加好友
            int lisiCount = addFriendsForUser(lisiId, allUserIds);
            log.info("为李四添加了 {} 个好友", lisiCount);

            // 双向添加：让其他用户也能看到张三和李四
            int reverseZhangsan = addReverseFriends(zhangsanId, "张三", allUserIds);
            log.info("为其他用户添加张三为好友: {} 条", reverseZhangsan);

            int reverseLisi = addReverseFriends(lisiId, "李四", allUserIds);
            log.info("为其他用户添加李四为好友: {} 条", reverseLisi);

            // 清除所有用户的缓存
            clearAllFriendCache(allUserIds);

            log.info("========== 好友关系测试数据初始化完成！ ==========");

        } catch (Exception e) {
            log.error("初始化好友关系测试数据失败", e);
        }
    }

    /**
     * 根据用户名获取用户ID
     */
    private Long getUserIdByUsername(String username) {
        String sql = "SELECT id FROM im_user WHERE username = ?";
        List<Long> ids = jdbcTemplate.queryForList(sql, Long.class, username);
        return ids.isEmpty() ? null : ids.get(0);
    }

    /**
     * 获取所有用户ID列表
     */
    private List<Long> getAllUserIds() {
        String sql = "SELECT id FROM im_user ORDER BY id";
        return jdbcTemplate.queryForList(sql, Long.class);
    }

    /**
     * 为指定用户添加所有其他用户为好友
     */
    private int addFriendsForUser(Long userId, List<Long> allUserIds) {
        int count = 0;
        String checkSql = "SELECT COUNT(*) FROM im_friend WHERE user_id = ? AND friend_id = ? AND is_deleted = 0";
        String insertSql = "INSERT INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time) " +
                          "SELECT ?, ?, u.nickname, NULL, 0, NOW(), NOW() FROM im_user u WHERE u.id = ?";

        for (Long friendId : allUserIds) {
            if (friendId.equals(userId)) {
                continue;
            }

            Integer existCount = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);
            if (existCount != null && existCount > 0) {
                continue;
            }

            try {
                int rows = jdbcTemplate.update(insertSql, userId, friendId, friendId);
                if (rows > 0) {
                    count++;
                    log.debug("添加好友关系: userId={}, friendId={}", userId, friendId);
                }
            } catch (Exception e) {
                log.warn("添加好友关系失败: userId={}, friendId={}, error={}", userId, friendId, e.getMessage());
            }
        }

        return count;
    }

    /**
     * 反向添加好友关系（让其他用户也能看到指定用户）
     */
    private int addReverseFriends(Long userId, String remarkName, List<Long> allUserIds) {
        int count = 0;
        String checkSql = "SELECT COUNT(*) FROM im_friend WHERE user_id = ? AND friend_id = ? AND is_deleted = 0";
        String insertSql = "INSERT INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time) " +
                          "VALUES (?, ?, ?, NULL, 0, NOW(), NOW())";

        for (Long otherUserId : allUserIds) {
            if (otherUserId.equals(userId)) {
                continue;
            }

            Integer existCount = jdbcTemplate.queryForObject(checkSql, Integer.class, otherUserId, userId);
            if (existCount != null && existCount > 0) {
                continue;
            }

            try {
                int rows = jdbcTemplate.update(insertSql, otherUserId, userId, remarkName);
                if (rows > 0) {
                    count++;
                    log.debug("反向添加好友关系: userId={}, friendId={}", otherUserId, userId);
                }
            } catch (Exception e) {
                log.warn("反向添加好友关系失败: userId={}, friendId={}, error={}", otherUserId, userId, e.getMessage());
            }
        }

        return count;
    }

    /**
     * 清除所有用户的好友列表缓存
     */
    private void clearAllFriendCache(List<Long> userIds) {
        int clearedCount = 0;
        for (Long userId : userIds) {
            try {
                String cacheKey = "contact:list:" + userId;
                imRedisUtil.delete(cacheKey);
                clearedCount++;
            } catch (Exception e) {
                log.warn("清除缓存失败: userId={}", userId, e);
            }
        }
        log.info("清除了 {} 个用户的缓存", clearedCount);
    }

    /**
     * 验证并打印好友关系统计
     */
    public void printFriendStatistics() {
        String sql = "SELECT f.user_id, u.username, u.nickname, COUNT(*) as friend_count " +
                    "FROM im_friend f " +
                    "JOIN im_user u ON f.user_id = u.id " +
                    "WHERE f.is_deleted = 0 " +
                    "GROUP BY f.user_id, u.username, u.nickname " +
                    "ORDER BY friend_count DESC";

        List<java.util.Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        log.info("========== 好友关系统计 ==========");
        for (java.util.Map<String, Object> row : results) {
            log.info("用户: {} ({}), 好友数: {}", row.get("username"), row.get("nickname"), row.get("friend_count"));
        }
        log.info("================================");

        printUserFriendList("zhangsan");
        printUserFriendList("lisi");
    }

    /**
     * 打印指定用户的好友列表
     */
    private void printUserFriendList(String username) {
        String sql = "SELECT f.id, f.friend_id, u.username, u.nickname, f.remark, f.group_name " +
                    "FROM im_friend f " +
                    "JOIN im_user u ON f.friend_id = u.id " +
                    "WHERE f.user_id = (SELECT id FROM im_user WHERE username = ?) " +
                    "AND f.is_deleted = 0";

        List<java.util.Map<String, Object>> friends = jdbcTemplate.queryForList(sql, username);

        log.info("========== {} 的好友列表 ({}) ==========", username, friends.size());
        for (java.util.Map<String, Object> friend : friends) {
            log.info("  - {} ({})", friend.get("nickname"), friend.get("username"));
        }
        log.info("=====================================");
    }
}
