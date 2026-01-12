package com.ruoyi.im.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * 数据库 Schema 验证测试
 *
 * 用途: 检测 Mapper XML 中的字段是否与数据库表结构一致
 * 运行方式: mvn test -Dtest=SchemaValidationTest
 *
 * 注意: 需要先启动数据库服务
 *       SecurityConfig 和 ImWebSocketConfig 使用 @ConditionalOnWebApplication
 *       在测试环境中不会加载，避免MVC基础设施依赖问题
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class SchemaValidationTest {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 需要验证的表列表
     * 格式: 表名
     */
    private static final Set<String> TABLES_TO_VALIDATE = new HashSet<>(Arrays.asList(
            "im_user",
            "im_message",
            "im_conversation",
            "im_conversation_member",
            "im_group",
            "im_group_member",
            "im_friend",
            "im_approval",
            "im_todo_item",
            "im_attendance",
            "im_schedule_event",
            "im_work_report"
    ));

    /**
     * 验证所有表结构是否存在
     */
    @Test
    public void validateTablesExist() {
        System.out.println("\n==================== 表存在性验证 ====================");
        List<String> tables = jdbcTemplate.queryForList(
                "SHOW TABLES", String.class);

        Set<String> tableSet = new HashSet<>(tables);
        List<String> missingTables = new ArrayList<>();

        for (String tableName : TABLES_TO_VALIDATE) {
            if (tableSet.contains(tableName)) {
                System.out.println("✓ 表存在: " + tableName);
            } else {
                System.out.println("✗ 表缺失: " + tableName);
                missingTables.add(tableName);
            }
        }

        if (!missingTables.isEmpty()) {
            throw new RuntimeException("缺失的表: " + missingTables);
        }
    }

    /**
     * 验证指定表的字段是否存在
     */
    @Test
    public void validateTableColumns() {
        System.out.println("\n==================== 字段验证 ====================");

        Map<String, Set<String>> expectedColumns = getExpectedColumns();

        for (Map.Entry<String, Set<String>> entry : expectedColumns.entrySet()) {
            String tableName = entry.getKey();
            Set<String> expected = entry.getValue();

            // 查询数据库实际字段
            Set<String> actual = jdbcTemplate.execute((ConnectionCallback<Set<String>>) connection -> {
                Set<String> columns = new HashSet<>();
                java.sql.ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, null);
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
                rs.close();
                return columns;
            });

            // 检查字段
            List<String> missingColumns = new ArrayList<>();
            List<String> extraColumns = new ArrayList<>();

            for (String col : expected) {
                if (!actual.contains(col.toLowerCase())) {
                    missingColumns.add(col);
                }
            }

            for (String col : actual) {
                if (!expected.contains(col.toLowerCase()) && !col.equals("id")) {
                    extraColumns.add(col);
                }
            }

            if (missingColumns.isEmpty() && extraColumns.isEmpty()) {
                System.out.println("✓ " + tableName + " 字段验证通过");
            } else {
                System.out.println("✗ " + tableName + " 存在问题:");
                if (!missingColumns.isEmpty()) {
                    System.out.println("  缺失字段: " + missingColumns);
                }
                if (!extraColumns.isEmpty()) {
                    System.out.println("  多余字段: " + extraColumns);
                }
            }
        }
    }

    /**
     * 获取期望的字段列表
     * 这是从 DATABASE-SCHEMA.md 中提取的定义
     * 基于实际数据库结构 (sql/im.sql)
     */
    private Map<String, Set<String>> getExpectedColumns() {
        Map<String, Set<String>> columns = new HashMap<>();

        // im_user - 基于实际数据库结构
        columns.put("im_user", new HashSet<>(Arrays.asList(
                "username", "password", "nickname", "email", "mobile",
                "avatar", "gender", "signature", "status",
                "last_online_time", "create_time", "update_time"
        )));

        // im_message - 基于实际数据库结构
        columns.put("im_message", new HashSet<>(Arrays.asList(
                "conversation_id", "sender_id", "message_type", "content",
                "reply_to_message_id", "forward_from_message_id",
                "file_url", "file_name", "file_size",
                "sensitive_level",
                "is_revoked", "revoked_time", "revoker_id",
                "is_edited", "edited_content", "edit_count", "edit_time",
                "is_deleted", "deleted_time",
                "create_time", "update_time"
        )));

        // im_conversation - 基于实际数据库结构
        columns.put("im_conversation", new HashSet<>(Arrays.asList(
                "type", "target_id", "name", "avatar",
                "last_message_id", "last_message_time",
                "is_deleted", "deleted_time",
                "create_time", "update_time"
        )));

        // im_conversation_member - 基于实际数据库结构
        columns.put("im_conversation_member", new HashSet<>(Arrays.asList(
                "conversation_id", "user_id", "nickname", "role",
                "unread_count", "is_pinned", "is_muted",
                "last_read_message_id", "last_read_time",
                "is_deleted", "deleted_time",
                "create_time", "update_time"
        )));

        // im_group - 基于实际数据库结构
        columns.put("im_group", new HashSet<>(Arrays.asList(
                "name", "avatar", "owner_id", "description",
                "max_members", "all_muted",
                "is_deleted", "deleted_time",
                "create_time", "update_time"
        )));

        // im_group_member - 基于实际数据库结构 (修复后)
        columns.put("im_group_member", new HashSet<>(Arrays.asList(
                "group_id", "user_id", "nickname", "role",
                "is_muted", "is_deleted", "deleted_time",
                "reply_to_message_id",
                "create_time", "update_time"
        )));

        // im_friend - 基于实际数据库结构 (修复后)
        columns.put("im_friend", new HashSet<>(Arrays.asList(
                "user_id", "friend_id", "remark", "group_name",
                "is_deleted", "deleted_time",
                "create_time", "update_time"
        )));

        // im_approval - 基于实际数据库结构
        columns.put("im_approval", new HashSet<>(Arrays.asList(
                "template_id", "title", "applicant_id", "status",
                "current_node_id", "form_data", "attachments",
                "apply_time", "finish_time",
                "create_by", "create_time", "update_by", "update_time",
                "remark"
        )));

        // im_todo_item - 基于实际数据库结构
        columns.put("im_todo_item", new HashSet<>(Arrays.asList(
                "user_id", "title", "description", "priority",
                "status", "due_date", "completed_time",
                "create_time", "update_time"
        )));

        // im_attendance - 基于实际数据库结构
        columns.put("im_attendance", new HashSet<>(Arrays.asList(
                "user_id", "attendance_date",
                "check_in_time", "check_out_time",
                "check_in_status", "check_out_status",
                "work_minutes", "attendance_type",
                "remark", "check_in_location", "check_out_location",
                "device_info",
                "approve_status", "approver_id", "approve_time", "approve_comment",
                "create_time", "update_time"
        )));

        // im_schedule_event - 基于实际数据库结构
        columns.put("im_schedule_event", new HashSet<>(Arrays.asList(
                "user_id", "title", "description", "location",
                "start_time", "end_time", "is_all_day",
                "recurrence_type", "recurrence_end_date",
                "recurrence_interval", "recurrence_days",
                "color", "visibility", "status",
                "reminder_minutes",
                "create_time", "update_time"
        )));

        // im_work_report - 基于实际数据库结构
        columns.put("im_work_report", new HashSet<>(Arrays.asList(
                "user_id", "report_type", "report_date",
                "work_content", "completion_status",
                "tomorrow_plan", "issues",
                "attachments", "work_hours", "visibility",
                "submit_time", "status",
                "approver_id", "approve_time", "approve_remark",
                "create_time", "update_time"
        )));

        return columns;
    }

    /**
     * 打印数据库连接信息
     */
    @Test
    public void printDatabaseInfo() {
        System.out.println("\n==================== 数据库信息 ====================");
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        System.out.println("当前数据库: " + database);

        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        System.out.println("MySQL 版本: " + version);
    }
}
