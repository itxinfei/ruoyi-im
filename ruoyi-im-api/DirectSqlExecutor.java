import java.sql.*;

/**
 * 好友关系数据初始化工具（独立版）
 * 不需要 Spring Boot，直接执行 SQL
 *
 * 编译：javac DirectSqlExecutor.java
 * 运行：java DirectSqlExecutor
 *
 * @author ruoyi
 */
public class DirectSqlExecutor {

    // 数据库配置
    private static final String DB_URL = "jdbc:mysql://172.168.20.222:3306/im?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "im";
    private static final String DB_PASSWORD = "123456";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  好友关系数据初始化工具");
        System.out.println("========================================");

        Connection conn = null;
        try {
            // 加载数据库驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 建立连接
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("数据库连接成功！");

            // 获取张三和李四的用户ID
            Long zhangsanId = getUserId(conn, "zhangsan");
            Long lisiId = getUserId(conn, "lisi");

            if (zhangsanId == null) {
                System.out.println("警告: 未找到用户 'zhangsan'");
            } else {
                System.out.println("张三ID: " + zhangsanId);
            }

            if (lisiId == null) {
                System.out.println("警告: 未找到用户 'lisi'");
            } else {
                System.out.println("李四ID: " + lisiId);
            }

            if (zhangsanId != null) {
                // 为张三添加好友
                int count = addFriendsForUser(conn, zhangsanId);
                System.out.println("为张三添加了 " + count + " 个好友");

                // 反向添加（让其他人能看到张三）
                int reverseCount = addReverseFriends(conn, zhangsanId, "张三");
                System.out.println("反向添加张三: " + reverseCount + " 条");
            }

            if (lisiId != null) {
                // 为李四添加好友
                int count = addFriendsForUser(conn, lisiId);
                System.out.println("为李四添加了 " + count + " 个好友");

                // 反向添加（让其他人能看到李四）
                int reverseCount = addReverseFriends(conn, lisiId, "李四");
                System.out.println("反向添加李四: " + reverseCount + " 条");
            }

            // 打印统计信息
            printStatistics(conn);

            System.out.println("========================================");
            System.out.println("初始化完成！");
            System.out.println("请在前端调用清除缓存API:");
            System.out.println("POST /api/im/contact/cache/clear");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据用户名获取用户ID
     */
    private static Long getUserId(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM im_user WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }

    /**
     * 为指定用户添加所有其他用户为好友
     */
    private static int addFriendsForUser(Connection conn, Long userId) throws SQLException {
        int count = 0;

        // 检查是否已存在
        String checkSql = "SELECT COUNT(*) FROM im_friend WHERE user_id = ? AND friend_id = ? AND is_deleted = 0";

        // 插入好友关系
        String insertSql = "INSERT INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time) " +
                          "SELECT ?, ?, u.nickname, NULL, 0, NOW(), NOW() FROM im_user u WHERE u.id = ?";

        // 获取所有用户ID
        String getAllUsersSql = "SELECT id FROM im_user WHERE id != ? ORDER BY id";

        try (PreparedStatement ps = conn.prepareStatement(getAllUsersSql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long friendId = rs.getLong("id");

                    // 检查是否已存在
                    try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                        checkPs.setLong(1, userId);
                        checkPs.setLong(2, friendId);
                        try (ResultSet checkRs = checkPs.executeQuery()) {
                            if (checkRs.next() && checkRs.getInt(1) > 0) {
                                continue; // 已存在
                            }
                        }
                    }

                    // 插入
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setLong(1, userId);
                        insertPs.setLong(2, friendId);
                        insertPs.setLong(3, friendId);
                        int rows = insertPs.executeUpdate();
                        if (rows > 0) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    /**
     * 反向添加好友关系
     */
    private static int addReverseFriends(Connection conn, Long userId, String remarkName) throws SQLException {
        int count = 0;

        String checkSql = "SELECT COUNT(*) FROM im_friend WHERE user_id = ? AND friend_id = ? AND is_deleted = 0";
        String insertSql = "INSERT INTO im_friend (user_id, friend_id, remark, group_name, is_deleted, create_time, update_time) " +
                          "VALUES (?, ?, ?, NULL, 0, NOW(), NOW())";
        String getAllUsersSql = "SELECT id FROM im_user WHERE id != ? ORDER BY id";

        try (PreparedStatement ps = conn.prepareStatement(getAllUsersSql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long otherUserId = rs.getLong("id");

                    // 检查是否已存在
                    try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                        checkPs.setLong(1, otherUserId);
                        checkPs.setLong(2, userId);
                        try (ResultSet checkRs = checkPs.executeQuery()) {
                            if (checkRs.next() && checkRs.getInt(1) > 0) {
                                continue; // 已存在
                            }
                        }
                    }

                    // 插入
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setLong(1, otherUserId);
                        insertPs.setLong(2, userId);
                        insertPs.setString(3, remarkName);
                        int rows = insertPs.executeUpdate();
                        if (rows > 0) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    /**
     * 打印统计信息
     */
    private static void printStatistics(Connection conn) throws SQLException {
        System.out.println("\n========== 好友关系统计 ==========");

        String sql = "SELECT f.user_id, u.username, u.nickname, COUNT(*) as friend_count " +
                    "FROM im_friend f " +
                    "JOIN im_user u ON f.user_id = u.id " +
                    "WHERE f.is_deleted = 0 " +
                    "GROUP BY f.user_id, u.username, u.nickname " +
                    "ORDER BY friend_count DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(String.format("用户: %s (%s), 好友数: %d",
                    rs.getString("username"),
                    rs.getString("nickname"),
                    rs.getInt("friend_count")));
            }
        }

        // 显示张三和李四的好友列表
        printFriendList(conn, "zhangsan");
        printFriendList(conn, "lisi");

        System.out.println("====================================\n");
    }

    /**
     * 打印指定用户的好友列表
     */
    private static void printFriendList(Connection conn, String username) throws SQLException {
        String sql = "SELECT f.friend_id, u.username, u.nickname, f.remark " +
                    "FROM im_friend f " +
                    "JOIN im_user u ON f.friend_id = u.id " +
                    "WHERE f.user_id = (SELECT id FROM im_user WHERE username = ?) " +
                    "AND f.is_deleted = 0";

        System.out.println("---------- " + username + " 的好友列表 ----------");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println(String.format("  %d. %s (%s)",
                        count,
                        rs.getString("nickname"),
                        rs.getString("username")));
                }
                System.out.println("  共 " + count + " 个好友");
            }
        }
        System.out.println("---------------------------------------");
    }
}
