# DTO/VO 组织规范文档

## 一、概述

本项目已经建立了完善的DTO（Data Transfer Object）和VO（View Object）组织结构，按照业务模块进行分包管理。

## 二、目录结构

```
com.ruoyi.im.dto/
├── announcement/      # 公告相关DTO
│   ├── ImAnnouncementCreateRequest.java
│   ├── ImAnnouncementQueryRequest.java
│   └── ImAnnouncementUpdateRequest.java
├── ai/               # AI相关DTO
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   ├── SummaryRequest.java
│   └── SummaryResponse.java
├── approval/         # 审批相关DTO
│   ├── ConditionBranch.java
│   └── ConditionOperand.java
├── attendance/       # 考勤相关DTO
│   └── ImAttendanceGroupCreateRequest.java
├── cloud/            # 云盘相关DTO
│   ├── ImCloudFileMoveRequest.java
│   ├── ImCloudFileShareRequest.java
│   └── ImCloudFolderCreateRequest.java
├── contact/          # 联系人相关DTO
│   ├── ExternalContactCreateRequest.java
│   ├── ImFriendAddRequest.java
│   ├── ImFriendQueryRequest.java
│   └── ImFriendUpdateRequest.java
├── conversation/     # 会话相关DTO
│   ├── ImConversationCreateRequest.java
│   ├── ImConversationMemberUpdateRequest.java
│   ├── ImConversationUpdateRequest.java
│   ├── ImGroupConversationCreateRequest.java
│   └── ImPrivateConversationCreateRequest.java
├── ding/             # 钉钉相关DTO
│   ├── DingQueryRequest.java
│   └── DingSendRequest.java
├── document/         # 文档相关DTO
│   ├── ImDocumentCollaboratorRequest.java
│   ├── ImDocumentCommentRequest.java
│   ├── ImDocumentCreateRequest.java
│   ├── ImDocumentShareRequest.java
│   └── ImDocumentUpdateRequest.java
├── file/             # 文件相关DTO
│   ├── ImFileChunkMergeRequest.java
│   ├── ImFileChunkUploadInitRequest.java
│   ├── ImFileChunkUploadRequest.java
│   ├── ImFileShareRequest.java
│   └── ImFileUploadRequest.java
├── group/            # 群组相关DTO
│   ├── ImGroupAnnouncementCreateRequest.java
│   ├── ImGroupCreateRequest.java
│   ├── ImGroupFileQueryRequest.java
│   ├── ImGroupFileUpdateRequest.java
│   ├── ImGroupFileUploadRequest.java
│   ├── ImGroupQueryRequest.java
│   └── ImGroupUpdateRequest.java
├── groupbot/         # 群机器人相关DTO
│   ├── BotCreateRequest.java
│   ├── BotRuleRequest.java
│   └── BotUpdateRequest.java
├── meeting/          # 会议相关DTO
│   ├── ImMeetingBookingRequest.java
│   ├── ImMeetingRoomCreateRequest.java
│   ├── ImMeetingRoomQueryRequest.java
│   ├── ImMeetingRoomUpdateRequest.java
│   ├── ImVideoMeetingCreateRequest.java
│   └── ImVideoMeetingUpdateRequest.java
├── mention/          # 提醒相关DTO
│   └── ImMentionInfo.java
├── message/          # 消息相关DTO
│   ├── ImMessageBatchForwardRequest.java
│   ├── ImMessageSendRequest.java
│   ├── ImMessageBatchReadStatusRequest.java
│   ├── ImMessageMarkReadRequest.java
│   ├── ImMessageForwardRequest.java
│   ├── ImMessageRecallRequest.java
│   ├── ImMessageReplyRequest.java
│   ├── ImMessageSearchRequest.java
│   └── MessageEditRequest.java
├── organization/     # 组织架构相关DTO
│   ├── ImDepartmentCreateRequest.java
│   ├── ImDepartmentMemberAddRequest.java
│   └── ImDepartmentUpdateRequest.java
├── reaction/         # 反应相关DTO
│   └── ImMessageReactionAddRequest.java
├── schedule/         # 日程相关DTO
│   ├── ScheduleEventCreateRequest.java
│   └── ScheduleEventQueryRequest.java
├── search/           # 搜索相关DTO
│   └── GlobalSearchRequest.java
├── session/          # 会话相关DTO
│   └── ImSessionUpdateRequest.java
├── setting/          # 设置相关DTO
│   ├── UserSettingsBatchUpdateRequest.java
│   └── UserSettingUpdateRequest.java
├── task/             # 任务相关DTO
│   ├── ImTaskCreateRequest.java
│   ├── ImTaskQueryRequest.java
│   └── ImTaskUpdateRequest.java
├── translation/      # 翻译相关DTO
│   ├── LanguageInfo.java
│   ├── TranslationRequest.java
│   └── TranslationResponse.java
├── user/             # 用户相关DTO
│   ├── ImLoginRequest.java
│   ├── ImRegisterRequest.java
│   ├── ImUserStatusUpdateRequest.java
│   └── ImUserUpdateRequest.java
└── workreport/       # 工作报告相关DTO
    ├── WorkReportCreateRequest.java
    └── WorkReportQueryRequest.java

com.ruoyi.im.vo/
├── announcement/      # 公告相关VO
│   ├── ImAnnouncementDetailVO.java
│   └── ImAnnouncementVO.java
├── app/               # 应用相关VO
│   └── ImApplicationVO.java
├── attendance/        # 考勤相关VO
│   ├── ImAttendanceVO.java
│   ├── ImAttendanceGroupVO.java
│   └── ImAttendanceShiftVO.java
├── audit/             # 审计相关VO
│   └── ImAuditLogVO.java
├── cloud/             # 云盘相关VO
│   ├── ImCloudFileShareVO.java
│   ├── ImCloudFileVO.java
│   ├── ImCloudFolderVO.java
│   └── ImCloudStorageQuotaVO.java
├── contact/           # 联系人相关VO
│   ├── ImContactGroupVO.java
│   ├── ImExternalContactGroupVO.java
│   ├── ImExternalContactVO.java
│   └── ImFriendVO.java
├── conversation/      # 会话相关VO
│   ├── ImConversationMemberVO.java
│   └── ImConversationVO.java
├── ding/              # 钉钉相关VO
│   ├── DingDetailVO.java
│   ├── DingMessageVO.java
│   ├── DingReadUserVO.java
│   └── DingReceiptVO.java
├── document/          # 文档相关VO
│   ├── ImDocumentCollaboratorVO.java
│   ├── ImDocumentCommentVO.java
│   ├── ImDocumentOperationLogVO.java
│   ├── ImDocumentVersionVO.java
│   └── ImDocumentVO.java
├── favorite/          # 收藏相关VO
│   └── FavoriteMessageVO.java
├── file/              # 文件相关VO
│   ├── ImFileChunkUploadInitVO.java
│   ├── ImFilePreviewInfoVO.java
│   ├── ImFileShareVO.java
│   ├── ImFileStatisticsVO.java
│   └── ImFileVO.java
├── group/             # 群组相关VO
│   ├── ImGroupAnnouncementVO.java
│   ├── ImGroupBotVO.java
│   ├── ImGroupFileStatisticsVO.java
│   ├── ImGroupFileVO.java
│   ├── ImGroupMemberVO.java
│   └── ImGroupVO.java
├── marker/            # 标记相关VO
│   └── ImMessageMarkerVO.java
├── meeting/           # 会议相关VO
│   ├── ImMeetingBookingVO.java
│   ├── ImMeetingRoomScheduleVO.java
│   ├── ImMeetingRoomVO.java
│   ├── ImVideoMeetingDetailVO.java
│   └── ImVideoMeetingVO.java
├── message/           # 消息相关VO
│   ├── ImMessageReadDetailVO.java
│   ├── ImMessageReadStatusVO.java
│   ├── ImMessageReferenceVO.java
│   ├── ImMessageSearchResultVO.java
│   ├── ImMessageVO.java
│   └── MessageEditHistoryVO.java
├── organization/      # 组织架构相关VO
│   ├── ImDepartmentMemberVO.java
│   ├── ImDepartmentTreeVO.java
│   └── ImDepartmentVO.java
├── reaction/          # 反应相关VO
│   └── ImMessageReactionVO.java
├── schedule/          # 日程相关VO
│   ├── ScheduleEventDetailVO.java
│   └── ScheduleParticipantVO.java
├── search/            # 搜索相关VO
│   └── GlobalSearchResultVO.java
├── session/           # 会话相关VO
│   └── ImSessionVO.java
├── setting/           # 设置相关VO
│   └── UserSettingVO.java
├── task/              # 任务相关VO
│   ├── ImTaskDetailVO.java
│   └── ImTaskVO.java
├── transcript/        # 转录相关VO
│   └── ImVoiceTranscriptVO.java
├── user/              # 用户相关VO
│   ├── ImLoginVO.java
│   └── ImUserVO.java
└── workreport/        # 工作报告相关VO
    ├── WorkReportCommentVO.java
    ├── WorkReportDetailVO.java
    └── WorkReportLikeUserVO.java
```

## 三、命名规范

### 3.1 DTO命名规范

**请求DTO（Request）：**
- 格式：`{模块名}{操作名}Request`
- 示例：
  - `ImMessageSendRequest` - 发送消息请求
  - `ImUserUpdateRequest` - 更新用户请求
  - `ImGroupCreateRequest` - 创建群组请求

**响应DTO（Response）：**
- 格式：`{模块名}{操作名}Response`
- 示例：
  - `ChatResponse` - 聊天响应
  - `TranslationResponse` - 翻译响应

**查询DTO（Query）：**
- 格式：`{模块名}QueryRequest`
- 示例：
  - `ImMessageSearchRequest` - 消息搜索请求
  - `ImGroupQueryRequest` - 群组查询请求

**其他DTO：**
- 格式：`{业务名}Info` 或 `{业务名}Data`
- 示例：
  - `ImMentionInfo` - 提醒信息
  - `ConditionOperand` - 条件操作数

### 3.2 VO命名规范

**基础VO：**
- 格式：`{模块名}VO`
- 示例：
  - `ImMessageVO` - 消息视图对象
  - `ImUserVO` - 用户视图对象
  - `ImGroupVO` - 群组视图对象

**详情VO（Detail）：**
- 格式：`{模块名}DetailVO`
- 示例：
  - `ImAnnouncementDetailVO` - 公告详情
  - `ImVideoMeetingDetailVO` - 视频会议详情

**列表VO：**
- 格式：`{模块名}ListVO`
- 示例：
  - `ImDepartmentTreeVO` - 部门树形列表

**统计VO（Statistics）：**
- 格式：`{模块名}StatisticsVO`
- 示例：
  - `ImFileStatisticsVO` - 文件统计
  - `ImGroupFileStatisticsVO` - 群组文件统计

## 四、最佳实践

### 4.1 DTO设计原则

1. **单一职责**：一个DTO只负责一个具体的业务操作
2. **字段精简**：只包含必要的字段，避免冗余
3. **验证注解**：使用`@Valid`和`@NotNull`等注解进行参数校验
4. **文档注释**：添加清晰的JavaDoc注释

**示例：**
```java
/**
 * 消息发送请求DTO
 *
 * @author ruoyi
 */
@Data
public class ImMessageSendRequest {
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000字符")
    private String content;

    @Size(max = 100, message = "类型标识不能超过100字符")
    private String type;

    private Long replyToMessageId;
    private String clientMsgId;
}
```

### 4.2 VO设计原则

1. **视图隔离**：VO只包含前端需要展示的字段
2. **数据脱敏**：敏感字段进行脱敏处理
3. **格式化**：日期、金额等字段使用注解格式化
4. **关联数据**：包含必要的关联数据（如用户名、头像等）

**示例：**
```java
/**
 * 消息视图对象
 *
 * @author ruoyi
 */
@Data
public class ImMessageVO {
    private Long id;
    private Long conversationId;
    private Long senderId;

    /** 发送者名称（关联数据） */
    private String senderName;

    /** 发送者头像（关联数据） */
    private String senderAvatar;

    /** 消息内容 */
    private String content;

    /** 消息类型 */
    private String type;

    /** 是否为自己发送的 */
    private Boolean isSelf;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
}
```

### 4.3 DTO与VO转换

使用MapStruct或BeanUtils进行对象转换：

```java
// 使用BeanUtils
ImMessageVO vo = new ImMessageVO();
BeanUtils.copyProperties(message, vo);
vo.setSenderName(user.getNickname());
vo.setSenderAvatar(user.getAvatar());

// 或使用MapStruct（推荐）
@Mapper
public interface MessageMapper {
    ImMessageVO toVO(ImMessage message, ImUser user);
}
```

## 五、统计信息

- **DTO总数**：83个
- **VO总数**：67个
- **业务模块**：25个

## 六、注意事项

1. **不要滥用DTO/VO**：简单场景可以直接使用Domain对象
2. **避免循环依赖**：DTO和VO之间不要相互引用
3. **版本兼容**：API升级时保持DTO/VO的向后兼容性
4. **性能考虑**：避免在VO中包含大量不必要的数据
5. **安全性**：VO中不要包含敏感信息（如密码、Token等）

## 七、扩展建议

1. **统一校验**：创建统一的校验注解和校验器
2. **分组校验**：使用`@Validated`进行分组校验
3. **API文档**：在DTO/VO上添加Swagger注解
4. **自动化工具**：考虑使用Lombok的@Builder简化对象创建
5. **版本管理**：为DTO/VO添加版本号，支持API版本演进