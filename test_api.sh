#!/bin/bash
# API测试脚本

BASE_URL="http://localhost:8080"
results=()

echo "# 后端API接口测试报告"
echo ""
echo "测试时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "服务器地址: $BASE_URL"
echo ""

# 测试函数
test_api() {
    local method=$1
    local url=$2
    local desc=$3
    local full_url="$BASE_URL$url"
    
    echo "### $method $url"
    echo "- 说明: $desc"
    
    response=$(curl -s -w "\n%{http_code}" -X $method "$full_url" 2>/dev/null)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    echo "- 状态码: $http_code"
    
    if [ "$http_code" = "200" ]; then
        echo "- 结果: ✅ 通过"
    elif [ "$http_code" = "401" ]; then
        echo "- 结果: 🔒 需要认证"
    elif [ "$http_code" = "404" ]; then
        echo "- 结果: ❌ 接口不存在"
    elif [ "$http_code" = "400" ]; then
        echo "- 结果: ⚠️ 请求参数错误"
    else
        echo "- 结果: ❌ 错误 (HTTP $http_code)"
    fi
    
    echo "- 响应: $(echo "$body" | head -c 200)"
    echo ""
}

# 测试所有接口
echo "## 1. 系统模块"
test_api "GET" "/" "系统首页"
test_api "GET" "/health" "健康检查"

echo "## 2. 认证模块 /api/auth"
test_api "POST" "/api/auth/login" "用户登录"
test_api "GET" "/api/auth/getInfo" "获取用户信息"
test_api "POST" "/api/auth/logout" "退出登录"
test_api "POST" "/api/auth/register" "用户注册"

echo "## 3. 用户模块 /api/im/user"
test_api "GET" "/api/im/user/list" "获取用户列表"
test_api "GET" "/api/im/user/info" "获取当前用户信息"
test_api "GET" "/api/im/user/1" "获取指定用户"
test_api "GET" "/api/im/user/search?keyword=test" "搜索用户"
test_api "GET" "/api/im/user/batch?ids=1,2,3" "批量获取用户"

echo "## 4. 联系人模块 /api/im/contact"
test_api "GET" "/api/im/contact/list" "获取好友列表"
test_api "GET" "/api/im/contact/search?keyword=test" "搜索联系人"
test_api "GET" "/api/im/contact/request/received" "获取收到的好友申请"
test_api "GET" "/api/im/contact/request/sent" "获取发送的好友申请"

echo "## 5. 会话模块 /api/im/conversation"
test_api "GET" "/api/im/conversation/list" "获取会话列表"
test_api "GET" "/api/im/conversation/unreadCount" "获取未读消息数"
test_api "GET" "/api/im/conversation/search?keyword=test" "搜索会话"

echo "## 6. Session模块 /api/im/session"
test_api "GET" "/api/im/session/list" "获取当前用户会话列表"

echo "## 7. 会话成员模块 /im/conversationMember"
test_api "GET" "/im/conversationMember/myList" "获取当前用户会话列表"
test_api "GET" "/im/conversationMember/list" "查询会话成员列表"

echo "## 8. 消息模块 /api/im/message"
test_api "GET" "/api/im/message/list/1" "获取会话消息列表"
test_api "GET" "/api/im/message/unread/count" "获取未读消息数"

echo "## 9. 群组模块 /api/im/group"
test_api "GET" "/api/im/group/list" "获取用户的群组列表"
test_api "GET" "/api/im/group/1" "获取群组详情"

echo "## 10. Groups模块 /api/im/groups"
test_api "GET" "/api/im/groups/1" "获取群组详情"

echo "## 11. 文件模块 /api/im/file"
test_api "GET" "/api/im/file/list" "获取用户文件列表"
test_api "GET" "/api/im/file/statistics" "获取文件统计信息"

echo "## 12. 通知模块 /api/im/notification"
test_api "GET" "/api/im/notification/list" "获取通知列表"
test_api "GET" "/api/im/notification/unread/count" "获取未读通知数"

echo "## 13. 工作台模块 /api/im/workbench"
test_api "GET" "/api/im/workbench/overview" "获取工作台数据概览"
test_api "GET" "/api/im/workbench/todos" "获取待办列表"

echo "## 14. 审批模块 /api/im/approval"
test_api "GET" "/api/im/approval/pending" "获取待我审批列表"
test_api "GET" "/api/im/approval/my" "获取我发起的审批列表"
test_api "GET" "/api/im/approval/processed" "获取我已审批列表"
test_api "GET" "/api/im/approval/templates" "获取审批模板列表"
test_api "GET" "/api/im/approval/templates/active" "获取启用的审批模板列表"

echo "## 15. 考勤模块 /api/im/attendance"
test_api "GET" "/api/im/attendance/today" "获取今日打卡状态"
test_api "GET" "/api/im/attendance/list" "获取打卡记录列表"
test_api "GET" "/api/im/attendance/statistics" "获取月度统计"

echo "## 16. DING消息模块 /api/im/ding"
test_api "GET" "/api/im/ding/received" "获取接收的DING列表"
test_api "GET" "/api/im/ding/sent" "获取发送的DING列表"
test_api "GET" "/api/im/ding/templates" "获取DING模板列表"

echo "## 17. 工作日志模块 /api/im/work-report"
test_api "GET" "/api/im/work-report/my" "获取我的日志列表"
test_api "GET" "/api/im/work-report/statistics" "获取统计信息"

echo "## 18. 日程模块 /api/im/schedule"
test_api "GET" "/api/im/schedule/range?start=2026-01-01&end=2026-12-31" "获取指定时间范围内的日程"

echo "## 19. 敏感词模块 /api/im/sensitiveWord"
test_api "GET" "/api/im/sensitiveWord/count" "获取敏感词数量"

echo "## 20. 审计模块 /api/im/audit"
test_api "GET" "/api/im/audit/list" "获取审计日志列表"
test_api "GET" "/api/im/audit/statistics" "获取审计统计信息"

echo "## 21. 配置模块 /api/im/config"
test_api "GET" "/api/im/config/notification" "获取通知设置"
test_api "GET" "/api/im/config/privacy" "获取隐私设置"
test_api "GET" "/api/im/config/" "获取通用设置"
test_api "GET" "/api/im/config/blocked" "获取黑名单"

echo "## 22. 备份模块 /api/im/backup"
test_api "GET" "/api/im/backup/list" "获取备份列表"
test_api "GET" "/api/im/backup/statistics" "获取备份统计信息"

echo "## 23. 应用模块 /api/im/app"
test_api "GET" "/api/im/app/list" "获取应用列表"
test_api "GET" "/api/im/app/visible" "获取可见应用列表"

echo "## 24. 组织架构模块 /api/im/organization"
test_api "GET" "/api/im/organization/department/tree" "获取部门树形结构"

echo "## 25. 群组公告模块 /api/im/group/announcement"
test_api "GET" "/api/im/group/announcement/list/1" "获取群组公告列表"
test_api "GET" "/api/im/group/announcement/latest/1" "获取群组最新公告"

echo "## 26. 群组文件模块 /api/im/group/file"
test_api "GET" "/api/im/group/file/statistics/1" "获取群组文件统计信息"

echo "## 27. 外部联系人模块 /api/im/external-contact"
test_api "GET" "/api/im/external-contact/list" "获取联系人列表"
test_api "GET" "/api/im/external-contact/group/list" "获取分组列表"

echo "## 28. 消息收藏模块 /api/im/message/favorite"
test_api "GET" "/api/im/message/favorite/list" "获取用户收藏的消息列表"

echo "## 29. 文件预览模块 /api/im/file/preview"
test_api "GET" "/api/im/file/preview/support/pdf" "检查PDF是否支持预览"

echo "## 30. Swagger文档"
test_api "GET" "/swagger-ui.html" "Swagger UI"
test_api "GET" "/v3/api-docs" "OpenAPI 3.0 文档"
