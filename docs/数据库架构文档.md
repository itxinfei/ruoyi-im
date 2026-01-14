# RuoYi-IM 数据库架构定义

> ⚠️ **重要**: 本文件是数据库结构的单一事实来源
> 任何数据库字段变更都必须先更新本文件，然后再修改代码

## 变更规则

1. **数据库优先原则**: 数据库表结构是唯一真实来源
2. **变更流程**: 修改本文件 → 执行SQL → 更新Entity → 更新Mapper
3. **命名规范**: 数据库 `snake_case`，Java `camelCase`
4. **验证方法**: 运行 `SchemaValidationTest` 确保字段映射正确

---

## 表结构定义

### im_user - 用户表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 用户ID | 主键自增 |
| username | username | varchar(50) | 用户名 | 唯一 |
| password | password | varchar(100) | 密码 | BCrypt加密 |
| nickname | nickname | varchar(50) | 昵称 | |
| avatar | avatar | varchar(200) | 头像URL | |
| gender | gender | tinyint(1) | 性别 | 0保密 1男 2女 |
| mobile | mobile | varchar(20) | 手机号 | |
| email | email | varchar(100) | 邮箱 | |
| status | status | tinyint(1) | 状态 | 0禁用 1正常 |
| signature | signature | varchar(200) | 个性签名 | |
| last_online_time | lastOnlineTime | datetime | 最后在线时间 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_message - 消息表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 消息ID | 主键自增 |
| conversation_id | conversationId | bigint(20) | 会话ID | 关联im_conversation |
| sender_id | senderId | bigint(20) | 发送人ID | 关联im_user |
| message_type | messageType | varchar(20) | 消息类型 | TEXT/IMAGE/FILE/AUDIO/VIDEO |
| content | content | text | 消息内容 | |
| reply_to_message_id | replyToMessageId | bigint(20) | 回复消息ID | |
| forward_from_message_id | forwardFromMessageId | bigint(20) | 转发源消息ID | |
| file_url | fileUrl | varchar(500) | 文件URL | |
| file_name | fileName | varchar(200) | 文件名 | |
| file_size | fileSize | bigint(20) | 文件大小(字节) | |
| sensitive_level | sensitiveLevel | varchar(20) | 敏感级别 | NORMAL/SENSITIVE/HIGH |
| is_revoked | isRevoked | tinyint(1) | 是否撤回 | 0否 1是 |
| revoked_time | revokedTime | datetime | 撤回时间 | |
| revoker_id | revokerId | bigint(20) | 撤回人ID | |
| is_edited | isEdited | tinyint(1) | 是否编辑 | 0否 1是 |
| edited_content | editedContent | text | 编辑后内容 | |
| edit_count | editCount | int(11) | 编辑次数 | |
| edit_time | editTime | datetime | 编辑时间 | |
| is_deleted | isDeleted | tinyint(1) | 是否删除 | 0否 1是 |
| deleted_time | deletedTime | datetime | 删除时间 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_conversation - 会话表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 会话ID | 主键自增 |
| type | type | varchar(20) | 会话类型 | PRIVATE私聊 GROUP群聊 |
| target_id | targetId | bigint(20) | 目标ID | 私聊时为对方用户ID,群聊时为群组ID |
| name | name | varchar(100) | 会话名称 | |
| avatar | avatar | varchar(200) | 会话头像 | |
| last_message_id | lastMessageId | bigint(20) | 最后消息ID | |
| last_message_time | lastMessageTime | datetime | 最后消息时间 | |
| is_deleted | isDeleted | tinyint(1) | 是否删除 | 0否 1是 |
| deleted_time | deletedTime | datetime | 删除时间 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_conversation_member - 会话成员表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 成员ID | 主键自增 |
| conversation_id | conversationId | bigint(20) | 会话ID | 关联im_conversation |
| user_id | userId | bigint(20) | 用户ID | 关联im_user |
| nickname | nickname | varchar(50) | 群昵称 | |
| role | role | varchar(20) | 角色 | OWNER所有者 MEMBER成员 |
| unread_count | unreadCount | int(11) | 未读数 | |
| is_pinned | isPinned | tinyint(1) | 是否置顶 | 0否 1是 |
| is_muted | isMuted | tinyint(1) | 是否免打扰 | 0否 1是 |
| last_read_message_id | lastReadMessageId | bigint(20) | 最后已读消息ID | |
| last_read_time | lastReadTime | datetime | 最后已读时间 | |
| is_deleted | isDeleted | tinyint(1) | 是否删除 | 0否 1是 |
| deleted_time | deletedTime | datetime | 删除时间 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_group - 群组表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 群组ID | 主键自增 |
| name | name | varchar(100) | 群名称 | |
| avatar | avatar | varchar(200) | 群头像 | |
| owner_id | ownerId | bigint(20) | 群主ID | 关联im_user |
| description | description | varchar(500) | 群描述 | |
| max_members | maxMembers | int(11) | 最大成员数 | 默认500 |
| all_muted | allMuted | tinyint(1) | 全员禁言 | 0否 1是 |
| is_deleted | isDeleted | tinyint(1) | 是否删除 | 0否 1是 |
| deleted_time | deletedTime | datetime | 删除时间 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_group_member - 群成员表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 成员ID | 主键自增 |
| group_id | groupId | bigint(20) | 群组ID | 关联im_group |
| user_id | userId | bigint(20) | 用户ID | 关联im_user |
| nickname | nickname | varchar(50) | 群昵称 | |
| role | role | varchar(20) | 角色 | OWNER/ADMIN/MEMBER |
| is_muted | isMuted | tinyint(1) | 是否禁言 | 0否 1是 |
| is_deleted | isDeleted | tinyint(1) | 是否删除 | 0否 1是（退群） |
| deleted_time | deletedTime | datetime | 退群时间 | |
| reply_to_message_id | replyToMessageId | int(11) | 回复消息ID | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_friend - 好友关系表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 关系ID | 主键自增 |
| user_id | userId | bigint(20) | 用户ID | 关联im_user |
| friend_id | friendId | bigint(20) | 好友ID | 关联im_user |
| remark | remark | varchar(50) | 备注名 | |
| group_name | groupName | varchar(50) | 分组名 | |
| is_deleted | isDeleted | tinyint(1) | 是否删除 | 0否 1是 |
| deleted_time | deletedTime | datetime | 删除时间 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

---

## 工作台相关表

### im_approval_template - 审批模板表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 模板ID | 主键自增 |
| name | name | varchar(100) | 模板名称 | |
| code | code | varchar(50) | 模板编码 | 唯一 |
| category | category | varchar(50) | 分类 | |
| description | description | varchar(500) | 描述 | |
| form_schema | formSchema | text | 表单结构 | JSON格式 |
| flow_config | flowConfig | text | 流程配置 | JSON格式 |
| icon | icon | varchar(100) | 图标 | |
| is_system | isSystem | tinyint(1) | 是否系统模板 | 0否 1是 |
| status | status | varchar(20) | 状态 | ACTIVE/INACTIVE |
| sort_order | sortOrder | int(11) | 排序 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_approval_node - 审批节点表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 节点ID | 主键自增 |
| template_id | templateId | bigint(20) | 模板ID | 关联im_approval_template |
| node_key | nodeKey | varchar(50) | 节点键 | 唯一标识 |
| node_name | nodeName | varchar(100) | 节点名称 | |
| node_type | nodeType | varchar(20) | 节点类型 | APPROVE/CC/CONDITION/PARALLEL |
| approvers | approvers | text | 审批人列表 | JSON格式 |
| sort_order | sortOrder | int(11) | 排序 | |
| create_time | createTime | datetime | 创建时间 | |

### im_approval_form_data - 审批表单数据表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 数据ID | 主键自增 |
| approval_id | approvalId | bigint(20) | 审批ID | 关联im_approval |
| field_key | fieldKey | varchar(50) | 字段键 | |
| field_value | fieldValue | text | 字段值 | |
| field_type | fieldType | varchar(20) | 字段类型 | TEXT/NUMBER/DATE/SELECT/FILE |
| create_time | createTime | datetime | 创建时间 | |

### im_approval - 审批申请表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 审批ID | 主键自增 |
| template_id | templateId | bigint(20) | 模板ID | 关联im_approval_template |
| title | title | varchar(200) | 申请标题 | |
| applicant_id | applicantId | bigint(20) | 申请人ID | 关联im_user |
| status | status | varchar(20) | 状态 | PENDING/APPROVED/REJECTED/CANCELLED |
| current_node_id | currentNodeId | bigint(20) | 当前节点ID | |
| form_data | formData | text | 表单数据 | JSON格式 |
| attachments | attachments | varchar(1000) | 附件 | JSON格式 |
| apply_time | applyTime | datetime | 申请时间 | |
| finish_time | finishTime | datetime | 完成时间 | |
| create_by | createBy | varchar(64) | 创建者 | |
| create_time | createTime | datetime | 创建时间 | |
| update_by | updateBy | varchar(64) | 更新者 | |
| update_time | updateTime | datetime | 更新时间 | |
| remark | remark | varchar(500) | 备注 | |

### im_todo_item - 待办事项表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 待办ID | 主键自增 |
| user_id | userId | bigint(20) | 用户ID | 关联im_user |
| title | title | varchar(200) | 标题 | |
| description | description | text | 描述 | |
| priority | priority | tinyint(1) | 优先级 | 1低 2中 3高 |
| status | status | varchar(20) | 状态 | PENDING/IN_PROGRESS/COMPLETED/CANCELLED |
| due_date | dueDate | date | 截止日期 | |
| completed_time | completedTime | datetime | 完成时间 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_attendance - 考勤打卡表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 打卡ID | 主键自增 |
| user_id | userId | bigint(20) | 用户ID | 关联im_user |
| attendance_date | attendanceDate | date | 打卡日期 | |
| check_in_time | checkInTime | datetime | 上班打卡时间 | |
| check_out_time | checkOutTime | datetime | 下班打卡时间 | |
| check_in_status | checkInStatus | varchar(20) | 上班状态 | NORMAL/LATE/ABSENT |
| check_out_status | checkOutStatus | varchar(20) | 下班状态 | NORMAL/EARLY/ABSENT |
| work_minutes | workMinutes | int(11) | 工作时长(分钟) | |
| attendance_type | attendanceType | varchar(20) | 打卡类型 | WORK/OVERTIME/WEEKEND |
| remark | remark | varchar(500) | 备注 | |
| check_in_location | checkInLocation | varchar(200) | 上班位置 | JSON格式 |
| check_out_location | checkOutLocation | varchar(200) | 下班位置 | JSON格式 |
| device_info | deviceInfo | varchar(200) | 设备信息 | |
| approve_status | approveStatus | varchar(20) | 审批状态 | PENDING/APPROVED/REJECTED |
| approver_id | approverId | bigint(20) | 审批人ID | |
| approve_time | approveTime | datetime | 审批时间 | |
| approve_comment | approveComment | varchar(500) | 审批意见 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_schedule_event - 日程事件表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 日程ID | 主键自增 |
| user_id | userId | bigint(20) | 创建人ID | 关联im_user |
| title | title | varchar(200) | 日程标题 | |
| description | description | text | 日程描述 | |
| location | location | varchar(200) | 地点 | |
| start_time | startTime | datetime | 开始时间 | |
| end_time | endTime | datetime | 结束时间 | |
| is_all_day | isAllDay | tinyint(1) | 是否全天 | 0否 1是 |
| recurrence_type | recurrenceType | varchar(20) | 重复类型 | NONE/DAILY/WEEKLY/MONTHLY/CUSTOM |
| recurrence_end_date | recurrenceEndDate | date | 重复结束日期 | |
| recurrence_interval | recurrenceInterval | int(11) | 重复间隔 | |
| recurrence_days | recurrenceDays | varchar(20) | 重复星期 | 1-7逗号分隔 |
| color | color | varchar(20) | 显示颜色 | |
| visibility | visibility | varchar(20) | 可见范围 | PRIVATE/DEPARTMENT/PUBLIC |
| status | status | varchar(20) | 状态 | SCHEDULED/CANCELLED/COMPLETED |
| reminder_minutes | reminderMinutes | int(11) | 提前提醒(分钟) | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

### im_work_report - 工作报告表

| 数据库列名 | Java字段名 | 类型 | 说明 | 备注 |
|-----------|-----------|------|------|------|
| id | id | bigint(20) | 报告ID | 主键自增 |
| user_id | userId | bigint(20) | 提交人ID | 关联im_user |
| report_type | reportType | varchar(20) | 报告类型 | DAILY/WEEKLY/MONTHLY |
| report_date | reportDate | date | 报告日期 | |
| work_content | workContent | text | 工作内容 | |
| completion_status | completionStatus | varchar(20) | 完成状态 | COMPLETED/IN_PROGRESS/PENDING |
| tomorrow_plan | tomorrowPlan | text | 明日计划 | |
| issues | issues | text | 遇到的问题 | |
| attachments | attachments | varchar(1000) | 附件 | 逗号分隔 |
| work_hours | workHours | decimal(4,2) | 工作时长(小时) | |
| visibility | visibility | varchar(20) | 可见范围 | PRIVATE/DEPARTMENT/PUBLIC |
| submit_time | submitTime | datetime | 提交时间 | |
| status | status | varchar(20) | 状态 | DRAFT/SUBMITTED/APPROVED/REJECTED |
| approver_id | approverId | bigint(20) | 审批人ID | |
| approve_time | approveTime | datetime | 审批时间 | |
| approve_remark | approveRemark | varchar(500) | 审批备注 | |
| create_time | createTime | datetime | 创建时间 | |
| update_time | updateTime | datetime | 更新时间 | |

---

## 变更日志

### 2026-01-12 (全面验证完成)
- 全面验证所有49个IM核心表结构与Entity/Mapper字段映射一致性
- 修复 ImAuditLog Entity 类：
  - 移除不存在的字段：user_name, module, description, request_method, request_url, request_params, response_data, status, error_msg, execution_time, client_ip
  - 添加缺失的字段：target_type, target_id, operation_result, error_message, ip_address
  - 修正 operation_time 为 create_time
- 修复 ImSensitiveWord Entity 类：
  - 添加缺失的字段：word_type, action, replacement, is_enabled
  - 修正 level 类型从 String 改为 Integer
  - 移除不存在的 status 字段
- 修复 ImGroup.allMuted 字段：从 @TableField(exist = false) 改为 @TableField("all_muted")
- im_user: status 改为 tinyint(1)，gender 改为 tinyint(1)
- im_friend: 添加 group_name, is_deleted, deleted_time；移除不存在的 status
- im_group_member: 添加 is_muted, is_deleted, deleted_time, reply_to_message_id；移除不存在的 join_time, status
- im_message: sensitive_level 改为 varchar(20)
- im_approval_form_data: 修复字段映射 field_name/field_label → field_key
- im_approval_node: 修复字段映射 approver_ids → approvers，添加 node_key，移除不存在的 runtime 字段
- im_conversation_member: 添加 nickname, role, last_read_time 字段映射
- 修复对应 Java Entity 类字段映射
- ImUser.status 改为 Integer 类型
- ImGroupMember 重写以匹配数据库
- ImMessage 添加 messageType, revokerId, isEdited, editedContent, editCount, editTime, fileUrl, fileName, fileSize
- ImGroupMember 添加 replyToMessageId 字段
- 修复所有 Mapper XML 文件的字段映射

### 已验证的所有 Entity 类列表（共38个）

**IM核心表：**
- ImUser, ImMessage, ImConversation, ImConversationMember
- ImGroup, ImGroupMember, ImGroupAnnouncement, ImGroupBlacklist, ImGroupFile
- ImFriend, ImFriendRequest
- ImApplication, ImFileAsset, ImFileShare, ImBackup

**审批相关：**
- ImApproval, ImApprovalTemplate, ImApprovalNode, ImApprovalFormData, ImApprovalRecord

**工作台相关：**
- ImAttendance, ImAttendanceStatistics
- ImScheduleEvent, ImScheduleParticipant
- ImWorkReport, ImWorkReportComment, ImWorkReportLike
- ImTodoItem

**组织架构：**
- ImDepartment, ImDepartmentMember

**其他：**
- ImAuditLog, ImSensitiveWord, ImSystemNotification, ImUserSetting
- ImMessageEditHistory, ImMessageFavorite, ImMessageMention, ImMessageReaction, ImMessageRead
- ImExternalContact, ImExternalContactGroup

### 暂无 Entity 类的表（共11个）

以下表存在于数据库中但暂无对应的Java Entity类，如需使用可后续创建：
- im_app_shortcut, im_audit_export_request, im_ding_message, im_ding_receipt, im_ding_template
- im_file_chunk_detail, im_file_chunk_upload, im_message_read_receipt, im_schedule_reminder
- im_sensitive_event, im_session_group, im_system_config, im_user_backup, im_user_device

### 2026-01-12 (之前)
- 修复 im_approval 表字段映射问题
  - 移除不存在的字段: related_id, related_type, comment, completed_time
  - 统一使用: apply_time, finish_time, remark, form_data, attachments
- 创建 DATABASE-SCHEMA.md 作为单一事实来源
- 创建 SchemaValidationTest 用于自动化验证字段映射
