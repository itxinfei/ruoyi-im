# 12 API 完整请求/响应 JSON 示例

> **定位**：为 `06-RESTful-API设计.md` 全部 87 个端点补全 JSON 请求体与响应体。大模型生成代码时直接参照本文件组装 DTO/VO/Controller。
> **更新时间**：2026-10-30

---

## 一、DTO / VO 类 Java 代码骨架

### 1.1 通用响应包装

```java
// Result.java —— 全局统一响应
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;
    private Long timestamp;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        r.timestamp = System.currentTimeMillis();
        return r;
    }
}

// PageResult.java —— 分页响应
@Data
public class PageResult<T> {
    private Long total;
    private List<T> list;
}
```

### 1.2 BaseEntity (所有 DO 继承)

```java
@Data
public class BaseDomainEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer delFlag;        // 0=正常, 1=已删除
    private Long createBy;
    private Long updateBy;
    @Version
    private Integer version;        // 乐观锁
}
```

### 1.3 错误码体系

```java
public interface ErrorCode {
    // 通用 (10xxx)
    int SUCCESS = 200;
    int PARAM_ERROR = 10001;
    int SYSTEM_ERROR = 10002;
    int RATE_LIMIT = 10003;

    // 认证 (30xxx)
    int AUTH_FAIL = 30001;
    int TOKEN_EXPIRED = 30002;
    int TOKEN_INVALID = 30003;
    int PERMISSION_DENIED = 30004;

    // 用户 (40xxx)
    int USER_NOT_FOUND = 40001;
    int USER_ALREADY_EXISTS = 40002;
    int USER_DISABLED = 40003;
    int PASSWORD_WRONG = 40004;

    // 群组 (50xxx)
    int GROUP_NOT_FOUND = 50001;
    int GROUP_FULL = 50002;
    int GROUP_MEMBER_EXISTS = 50003;
    int GROUP_PERMISSION_DENIED = 50004;

    // 消息 (60xxx)
    int MSG_NOT_FOUND = 60001;
    int MSG_RECALL_TIMEOUT = 60002;
    int MSG_CONTENT_EMPTY = 60003;

    // 文件 (70xxx)
    int FILE_NOT_FOUND = 70001;
    int FILE_TOO_LARGE = 70002;
    int FILE_TYPE_DENIED = 70003;
    int UPLOAD_FAILED = 70004;

    // 通话 (80xxx)
    int CALL_ROOM_FULL = 80001;
    int CALL_NOT_FOUND = 80002;
    int CALL_IN_PROGRESS = 80003;

    // 审批 (90xxx)
    int APPROVAL_NOT_FOUND = 90001;
    int APPROVAL_NO_PERMISSION = 90002;
    int APPROVAL_ALREADY_AUDITED = 90003;
}
```

---

## 二、auth-service 完整 DTO 和 JSON

### 2.1 LoginDTO (请求)

```java
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度 3-32")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度 6-32")
    private String password;

    private String deviceId;
    private String deviceType;   // WEB / DESKTOP / MOBILE
}
```

```json
{
  "username": "zhangsan",
  "password": "abc123",
  "deviceId": "web-chrome-windows-abc123",
  "deviceType": "WEB"
}
```

### 2.2 LoginVO (响应)

```java
@Data
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;     // 秒
    private String tokenType;      // Bearer
    private UserSimpleVO user;
}

@Data
public class UserSimpleVO {
    private Long id;
    private String username;
    private String displayName;
    private String avatar;
    private Long deptId;
    private String status;         // ONLINE/OFFLINE/BUSY/AWAY
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTEiLCJ1c2VybmFtZSI6InpoYW5nc2FuIiwicm9sZXMiOlsidXNlciJdLCJwZXJtaXNzaW9ucyI6WyJtc2c6c2VuZCIsInNlc3Npb246Y3JlYXRlIl0sImlhdCI6MTczMDAwMDAwMCwiZXhwIjoxNzMwMDA3MjAwfQ.xxx",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTEiLCJqdGkiOiJhYmMtMTIzIiwiaWF0IjoxNzMwMDAwMDAwLCJleHAiOjE3MzA2MDQ4MDB9.yyy",
    "expiresIn": 7200,
    "tokenType": "Bearer",
    "user": {
      "id": 111,
      "username": "zhangsan",
      "displayName": "张三",
      "avatar": "http://minio:9000/im-avatar/111/photo.jpg",
      "deptId": 10,
      "status": "ONLINE"
    }
  },
  "timestamp": 1730000000000
}
```

### 2.3 登出 (401 错误响应)

```json
{
  "code": 30002,
  "msg": "Token 已过期, 请重新登录",
  "data": null,
  "timestamp": 1730000000000
}
```

---

## 三、user-service 完整 DTO

### 3.1 UserSimpleVO (用于 Feign 批量查询)

```java
@Data
public class UserSimpleVO {
    private Long id;
    private String displayName;
    private String avatar;
    private String status;        // ONLINE/OFFLINE/BUSY/AWAY
    private Long deptId;
    private String deptName;
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "id": 111, "displayName": "张三",  "avatar": "http://minio:9000/im-avatar/111/photo.jpg", "status": "ONLINE",  "deptId": 10, "deptName": "技术部" },
    { "id": 112, "displayName": "李四",  "avatar": "http://minio:9000/im-avatar/112/photo.jpg", "status": "OFFLINE", "deptId": 10, "deptName": "技术部" },
    { "id": 113, "displayName": "王五",  "avatar": "http://minio:9000/im-avatar/113/photo.jpg", "status": "BUSY",    "deptId": 10, "deptName": "技术部" }
  ],
  "timestamp": 1730000000000
}
```

### 3.2 UserDetailVO

```java
@Data
public class UserDetailVO {
    private Long id;
    private String username;
    private String displayName;
    private String avatar;
    private String email;
    private String phone;
    private Long deptId;
    private String deptName;
    private String position;
    private String signature;
    private String status;
    private LocalDateTime lastOnlineTime;
    private LocalDateTime createTime;
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 111,
    "username": "zhangsan",
    "displayName": "张三",
    "avatar": "http://minio:9000/im-avatar/111/photo.jpg",
    "email": "zhang**@company.com",
    "phone": "138****8001",
    "deptId": 10,
    "deptName": "技术部",
    "position": "高级工程师",
    "signature": "专注技术, 热爱生活",
    "status": "ONLINE",
    "lastOnlineTime": "2026-10-30T10:30:00",
    "createTime": "2025-01-15T08:00:00"
  },
  "timestamp": 1730000000000
}
```

### 3.3 UpdateProfileCommand

```java
@Data
public class UpdateProfileCommand {
    @Size(max = 32)
    private String displayName;
    private String avatar;
    @Email
    private String email;
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String phone;
    @Size(max = 64)
    private String position;
    @Size(max = 128)
    private String signature;
}
```

```json
{
  "displayName": "张三丰",
  "avatar": "http://minio:9000/im-avatar/111/new.jpg",
  "email": "zhangsanfeng@company.com",
  "position": "架构师",
  "signature": "妙手回春, 代码如神"
}
```

---

## 四、session-service 完整 DTO

### 4.1 SessionDetailVO

```java
@Data
public class SessionDetailVO {
    private String id;                  // 雪花 ID
    private String type;                // P2P / GROUP
    private Long targetId;
    private String displayName;
    private String avatar;
    private Integer unreadCount;
    private Boolean pinned;
    private Boolean muted;
    private MessageSummaryVO lastMessage;
    private LocalDateTime activeTime;
}

@Data
public class MessageSummaryVO {
    private String msgId;
    private String type;               // TEXT/IMAGE/FILE/VOICE/VIDEO/LINK/DING
    private String content;
    private Long senderId;
    private String senderName;
    private LocalDateTime sendTime;
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 45,
    "list": [
      {
        "id": "1780000000001",
        "type": "P2P",
        "targetId": 112,
        "displayName": "李四",
        "avatar": "http://minio:9000/im-avatar/112/photo.jpg",
        "lastMessage": {
          "msgId": "1780001000015",
          "type": "TEXT",
          "content": "明天下午3点开会, 别忘了带笔记本",
          "senderId": 112,
          "senderName": "李四",
          "sendTime": "2026-10-30T14:30:00.000"
        },
        "unreadCount": 3,
        "pinned": true,
        "muted": false,
        "activeTime": "2026-10-30T14:35:00"
      },
      {
        "id": "1780000000002",
        "type": "GROUP",
        "targetId": 2001,
        "displayName": "开发二群",
        "avatar": "http://minio:9000/im-group/2001/icon.jpg",
        "lastMessage": {
          "msgId": "1780001000018",
          "type": "TEXT",
          "content": "@张三 请查看最新修定的 API 文档",
          "senderId": 113,
          "senderName": "王五",
          "sendTime": "2026-10-30T14:28:00.000"
        },
        "unreadCount": 0,
        "pinned": false,
        "muted": true,
        "activeTime": "2026-10-30T14:28:00"
      }
    ]
  },
  "timestamp": 1730000000000
}
```

### 4.2 CreateSessionCommand

```java
@Data
public class CreateSessionCommand {
    @NotBlank
    @Pattern(regexp = "P2P|GROUP")
    private String type;

    @NotNull
    private Long targetId;

    private String firstMessage;   // 可选的首次发送消息
}
```

```json
{
  "type": "P2P",
  "targetId": 112,
  "firstMessage": "你好, 关于明天的会议"
}
```

### 4.3 ReadReceiptCommand

```java
@Data
public class ReadReceiptCommand {
    @NotNull
    private Long lastReadSeq;
    private String lastReadMsgId;
}
```

```json
{
  "lastReadSeq": 42,
  "lastReadMsgId": "1780001000042"
}
```

---

## 五、message-service 完整 DTO

### 5.1 MessageFullVO (消息完整展示)

```java
@Data
public class MessageFullVO {
    private String id;
    private String sessionId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String type;               // TEXT/IMAGE/FILE/VOICE/VIDEO/LINK/DING
    private String content;
    private Long serverSeq;
    private Long clientSeq;
    private String status;             // SENDING/SENT/DELIVERED/READ/RECALLED/FAILED
    private LocalDateTime sendTime;
    private Boolean edited;
    private LocalDateTime editedAt;
    private Boolean recallable;
    private List<ReactionSummaryVO> reactions;
    private MessageReplyVO replyTo;
    private List<Long> mentionIds;
    private List<AttachmentVO> attachments;
}

@Data
public class ReactionSummaryVO {
    private String emoji;
    private Integer count;
    private List<Long> userIds;
    private Boolean reactedByMe;
}

@Data
public class MessageReplyVO {
    private String msgId;
    private Long senderId;
    private String senderName;
    private String type;
    private String content;             // 摘要
}

@Data
public class AttachmentVO {
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String mimeType;
    private String url;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private Integer duration;           // 音视频=秒, 其他=0
}
```

### 5.2 消息历史查询响应 JSON

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": "1780001000040",
        "sessionId": "1780000000001",
        "senderId": 111,
        "senderName": "张三",
        "senderAvatar": "http://minio:9000/im-avatar/111/photo.jpg",
        "type": "TEXT",
        "content": "明天下午3点开会, 别忘了带笔记本",
        "serverSeq": 40,
        "clientSeq": 12345,
        "status": "SENT",
        "sendTime": "2026-10-30T14:30:00.000",
        "edited": false,
        "editedAt": null,
        "recallable": true,
        "reactions": [
          { "emoji": "👍", "count": 3, "userIds": [112, 113, 114], "reactedByMe": false },
          { "emoji": "❤️",  "count": 1, "userIds": [112], "reactedByMe": true }
        ],
        "replyTo": null,
        "mentionIds": [],
        "attachments": []
      },
      {
        "id": "1780001000041",
        "sessionId": "1780000000001",
        "senderId": 112,
        "senderName": "李四",
        "senderAvatar": "http://minio:9000/im-avatar/112/photo.jpg",
        "type": "FILE",
        "content": "项目排期表-v2.xlsx (已更新)",
        "serverSeq": 41,
        "clientSeq": 12346,
        "status": "DELIVERED",
        "sendTime": "2026-10-30T14:32:00.000",
        "edited": false,
        "recallable": true,
        "reactions": [],
        "replyTo": null,
        "mentionIds": [],
        "attachments": [
          {
            "fileId": "1780002000001",
            "fileName": "项目排期表-v2.xlsx",
            "fileSize": 245678,
            "fileType": "xlsx",
            "mimeType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "url": "http://minio:9000/im-chat-file/1780002000001.xlsx?X-Amz-Expires=900&X-Amz-Signature=xxx",
            "thumbnailUrl": null,
            "width": 0,
            "height": 0,
            "duration": 0
          }
        ]
      },
      {
        "id": "1780001000042",
        "sessionId": "1780000000001",
        "senderId": 112,
        "senderName": "李四",
        "type": "IMAGE",
        "content": "[图片]",
        "serverSeq": 42,
        "status": "READ",
        "sendTime": "2026-10-30T14:33:00.000",
        "recallable": true,
        "reactions": [],
        "attachments": [
          {
            "fileId": "1780002000002",
            "fileName": "whiteboard-photo.jpg",
            "fileSize": 1567890,
            "fileType": "jpg",
            "mimeType": "image/jpeg",
            "url": "http://minio:9000/im-chat-image/1780002000002.jpg",
            "thumbnailUrl": "http://minio:9000/im-chat-image/thumb/1780002000002.jpg",
            "width": 1920,
            "height": 1080,
            "duration": 0
          }
        ]
      }
    ],
    "nextCursor": "1780001000020",
    "hasMore": true
  },
  "timestamp": 1730000000000
}
```

### 5.3 EditMessageCommand

```java
@Data
public class EditMessageCommand {
    @NotBlank
    @Size(max = 5000)
    private String content;
}
```

```json
{
  "content": "明天下午3点开会, 别忘了带笔记本 (地点: 会议室A301)"
}
```

### 5.4 RecallMessageVO (响应)

```json
{
  "code": 200,
  "msg": "撤回成功",
  "data": {
    "msgId": "1780001000040",
    "recallTime": "2026-10-30T14:31:30.000",
    "withinWindow": true
  },
  "timestamp": 1730000000000
}
```

### 5.5 撤回超时错误

```json
{
  "code": 60002,
  "msg": "消息发送超过 5 分钟, 不可撤回",
  "data": null,
  "timestamp": 1730000000000
}
```

---

## 六、group-service 完整 DTO

### 6.1 GroupDetailVO

```java
@Data
public class GroupDetailVO {
    private Long id;
    private String name;
    private String avatar;
    private String description;
    private Long ownerId;
    private String ownerName;
    private Integer memberCount;
    private Integer maxMemberCount;
    private String myRole;             // OWNER / ADMIN / MEMBER
    private String notification;       // ALL/MENTION/NONE
    private String pinnedMsgId;
    private List<String> tags;
    private Boolean muted;
    private LocalDateTime createTime;
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 2001,
    "name": "开发二群",
    "avatar": "http://minio:9000/im-group/2001/icon.jpg",
    "description": "后端 + 前端开发日常讨论",
    "ownerId": 111,
    "ownerName": "张三",
    "memberCount": 15,
    "maxMemberCount": 500,
    "myRole": "OWNER",
    "notification": "MENTION",
    "pinnedMsgId": "1780001000035",
    "tags": ["技术", "开发", "后端", "前端"],
    "muted": false,
    "createTime": "2025-03-15T10:00:00"
  },
  "timestamp": 1730000000000
}
```

### 6.2 GroupMemberVO

```java
@Data
public class GroupMemberVO {
    private Long userId;
    private String displayName;
    private String avatar;
    private String role;               // OWNER/ADMIN/MEMBER
    private LocalDateTime joinTime;
    private String nicknameInGroup;
    private String status;             // ONLINE/OFFLINE
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 15,
    "list": [
      { "userId": 111, "displayName": "张三",  "avatar": "http://minio:9000/im-avatar/111/photo.jpg", "role": "OWNER",  "joinTime": "2025-03-15T10:00:00", "nicknameInGroup": "群主张",  "status": "ONLINE" },
      { "userId": 112, "displayName": "李四",  "avatar": "http://minio:9000/im-avatar/112/photo.jpg", "role": "ADMIN",  "joinTime": "2025-03-16T09:00:00", "nicknameInGroup": "",          "status": "OFFLINE" },
      { "userId": 113, "displayName": "王五",  "avatar": "http://minio:9000/im-avatar/113/photo.jpg", "role": "MEMBER", "joinTime": "2025-04-01T14:00:00", "nicknameInGroup": "",          "status": "ONLINE" }
    ]
  },
  "timestamp": 1730000000000
}
```

### 6.3 CreateGroupCommand

```java
@Data
public class CreateGroupCommand {
    @NotBlank(message = "群名称不能为空")
    @Size(max = 16, message = "群名称最长 16 字符")
    private String name;

    private String avatar;
    private String description;
    private List<Long> memberIds;
    private List<String> tags;
}
```

```json
{
  "name": "开发二群 V2",
  "avatar": "http://minio:9000/im-group/new/icon.jpg",
  "description": "后端+前端开发日常讨论",
  "memberIds": [112, 113, 114, 115, 116],
  "tags": ["技术", "开发"]
}
```

---

## 七、file-service 完整 DTO

### 7.1 FileUploadVO

```java
@Data
public class FileUploadVO {
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String fileType;           // IMAGE/FILE/VOICE/VIDEO
    private String mimeType;
    private String url;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private Integer duration;          // 视频/音频秒数
    private LocalDateTime uploadTime;
}
```

### 7.2 普通上传响应 JSON

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "fileId": "1780002000005",
    "fileName": "whiteboard.png",
    "fileSize": 1567890,
    "fileType": "IMAGE",
    "mimeType": "image/png",
    "url": "http://minio:9000/im-chat-image/1780002000005.png",
    "thumbnailUrl": "http://minio:9000/im-chat-image/thumb/1780002000005.png",
    "width": 1920,
    "height": 1080,
    "duration": 0,
    "uploadTime": "2026-10-30T14:35:00.000"
  },
  "timestamp": 1730000000000
}
```

### 7.3 分片上传响应 JSON (进行中)

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "uploadId": "upload-uuid-aaa-bbb",
    "chunkIndex": 3,
    "uploaded": true,
    "completed": false,
    "progress": "30%"
  },
  "timestamp": 1730000000000
}
```

### 7.4 合并分片请求 JSON

```json
{
  "uploadId": "upload-uuid-aaa-bbb",
  "fileName": "large-project.zip",
  "totalChunks": 20,
  "totalSize": 104857600,
  "fileType": "FILE"
}
```

---

## 八、call-service 完整 DTO

### 8.1 CreateCallCommand

```java
@Data
public class CreateCallCommand {
    @NotBlank
    private String sessionId;          // 会话 ID (P2P 或 GROUP)

    @Pattern(regexp = "VIDEO|AUDIO|SCREEN_SHARE")
    private String type;

    private List<Long> participantIds;  // 被邀请者
}
```

```json
{
  "sessionId": "1780000000001",
  "type": "VIDEO",
  "participantIds": [112, 113, 114]
}
```

### 8.2 CallCreateVO

```java
@Data
public class CallCreateVO {
    private String callId;
    private String roomName;
    private String livekitToken;
    private String serverUrl;
    private Integer maxParticipants;
    private LocalDateTime createTime;
    private String status;             // CALLING/CONNECTED/ENDED
    private List<CallParticipantInitVO> participants;
}

@Data
public class CallParticipantInitVO {
    private Long userId;
    private String displayName;
    private String avatar;
    private String role;               // INITIATOR/PARTICIPANT
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "callId": "1780003000001",
    "roomName": "call_1780003000001",
    "livekitToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTEiLCJ2aWRlbyI6eyJyb29tSm9pbiI6dHJ1ZSwicm9vbSI6ImNhbGxfMTc4MDAwMzAwMDAwMSIsImNhblB1Ymxpc2giOnRydWUsImNhblN1YnNjcmliZSI6dHJ1ZX19.xxx",
    "serverUrl": "wss://livekit.internal.com",
    "maxParticipants": 9,
    "createTime": "2026-10-30T14:50:00.000",
    "status": "CALLING",
    "participants": [
      { "userId": 111, "displayName": "张三", "avatar": "http://minio:9000/im-avatar/111/photo.jpg", "role": "INITIATOR" },
      { "userId": 112, "displayName": "李四", "avatar": "http://minio:9000/im-avatar/112/photo.jpg", "role": "PARTICIPANT" }
    ]
  },
  "timestamp": 1730000000000
}
```

### 8.3 CallHistoryVO

```java
@Data
public class CallHistoryVO {
    private String callId;
    private String type;
    private Long initiatorId;
    private String initiatorName;
    private Integer duration;          // 秒
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;             // ENDED/FAILED/TIMEOUT
    private List<CallParticipantVO> participants;
}

@Data
public class CallParticipantVO {
    private Long userId;
    private String displayName;
    private String role;
    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;
    private Integer joinDuration;      // 秒
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 8,
    "list": [
      {
        "callId": "1780003000001",
        "type": "VIDEO",
        "initiatorId": 111,
        "initiatorName": "张三",
        "duration": 1865,
        "startTime": "2026-10-30T14:50:00",
        "endTime": "2026-10-30T15:21:05",
        "status": "ENDED",
        "participants": [
          { "userId": 111, "displayName": "张三", "role": "INITIATOR", "joinTime": "14:50:00", "leaveTime": "15:21:05", "joinDuration": 1865 },
          { "userId": 112, "displayName": "李四", "role": "PARTICIPANT", "joinTime": "14:51:30", "leaveTime": "15:20:00", "joinDuration": 1710 }
        ]
      }
    ]
  },
  "timestamp": 1730000000000
}
```

---

## 九、approval-service 完整 DTO

### 9.1 CreateTemplateCommand

```java
@Data
public class CreateTemplateCommand {
    @NotBlank
    @Size(max = 32)
    private String name;

    @NotBlank
    private String category;

    @NotNull
    private FormSchema formSchema;

    @NotEmpty
    private List<FlowStep> flow;
}

@Data
public class FormSchema {
    private List<FormField> fields;
}

@Data
public class FormField {
    private String key;
    @NotBlank
    private String label;
    @Pattern(regexp = "TEXT|NUMBER|DATE|SELECT|MULTI_SELECT|FILE|TEXTAREA")
    private String type;
    private Boolean required;
    private List<String> options;
    private String placeholder;
}

@Data
public class FlowStep {
    private Integer level;
    private String approver;           // DEPT_MANAGER / user:{id} / role:{id}
    private String type;               // ANY / ALL
    private List<String> cc;           // 抄送人
}
```

```json
{
  "name": "请假申请",
  "category": "HR",
  "formSchema": {
    "fields": [
      { "key": "startDate", "label": "开始日期", "type": "DATE", "required": true },
      { "key": "endDate",   "label": "结束日期", "type": "DATE", "required": true },
      { "key": "days",      "label": "请假天数", "type": "NUMBER", "required": true },
      { "key": "type",      "label": "假期类型", "type": "SELECT", "required": true, "options": ["年假","病假","事假","调休"] },
      { "key": "reason",    "label": "请假原因", "type": "TEXTAREA", "required": true }
    ]
  },
  "flow": [
    { "level": 1, "approver": "DEPT_MANAGER", "type": "ANY", "cc": [] },
    { "level": 2, "approver": "user:100",     "type": "ALL", "cc": ["user:99"] }
  ]
}
```

### 9.2 SubmitApprovalCommand

```java
@Data
public class SubmitApprovalCommand {
    @NotNull
    private Long templateId;

    @NotNull
    private Map<String, Object> formData;

    @Pattern(regexp = "URGENT|NORMAL|LOW")
    private String urgency;

    private String remark;
}
```

```json
{
  "templateId": 10,
  "formData": {
    "startDate": "2026-11-01",
    "endDate": "2026-11-03",
    "days": 3,
    "type": "年假",
    "reason": "家中有事需处理"
  },
  "urgency": "NORMAL",
  "remark": "已做好工作交接"
}
```

### 9.3 AuditApprovalCommand

```java
@Data
public class AuditApprovalCommand {
    @Pattern(regexp = "APPROVE|REJECT|TRANSFER")
    private String action;

    @Size(max = 500)
    private String comment;

    private Long transferToUserId;     // action=TRANSFER 时使用
}
```

```json
{
  "action": "APPROVE",
  "comment": "同意批准"
}
```

### 9.4 ApprovalListVO

```java
@Data
public class ApprovalListVO {
    private String auditId;
    private Long applicantId;
    private String applicantName;
    private String templateName;
    private String status;             // PENDING/APPROVED/REJECTED/CANCELLED
    private Integer currentLevel;
    private String formDataSummary;    // 关键字段拼接
    private String urgency;
    private LocalDateTime createTime;
    private LocalDateTime auditTime;
    private String auditComment;
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 5,
    "list": [
      {
        "auditId": "1780004000001",
        "applicantId": 111,
        "applicantName": "张三",
        "templateName": "请假申请",
        "status": "PENDING",
        "currentLevel": 1,
        "formDataSummary": "请假 2026-11-01 ~ 2026-11-03 (3天)",
        "urgency": "NORMAL",
        "createTime": "2026-10-30T15:00:00",
        "auditTime": null,
        "auditComment": null
      }
    ]
  },
  "timestamp": 1730000000000
}
```

---

## 十、calendar-service 完整 DTO

### 10.1 CreateEventCommand

```java
@Data
public class CreateEventCommand {
    @NotBlank
    @Size(max = 64)
    private String title;

    @Size(max = 500)
    private String description;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    private Boolean allDay;

    private String location;

    @Pattern(regexp = "MEETING|PERSONAL/WORK/REMINDER")
    private String type;

    private List<Integer> reminderMinutes;

    private List<Long> attendeeIds;

    private RecurrenceRule recurrence;
}

@Data
public class RecurrenceRule {
    @Pattern(regexp = "DAILY/WEEKLY/MONTHLY/YEARLY")
    private String frequency;
    private Integer interval;
    private List<Integer> byDay;       // 0=周日, 1=周一, ...
    private LocalDateTime until;
    private Integer count;
}
```

```json
{
  "title": "产品评审会",
  "description": "Q4 产品规划评审, 请各组负责人参加",
  "startTime": "2026-11-01T14:00:00",
  "endTime": "2026-11-01T16:00:00",
  "allDay": false,
  "location": "会议室 A301",
  "type": "MEETING",
  "reminderMinutes": [15, 60],
  "attendeeIds": [111, 112, 113, 114],
  "recurrence": null
}
```

### 10.2 EventDetailVO

```java
@Data
public class EventDetailVO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean allDay;
    private String location;
    private String type;
    private String displayColor;
    private List<AttendeeVO> attendees;
    private List<Integer> reminderMinutes;
    private RecurrenceRule recurrence;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

@Data
public class AttendeeVO {
    private Long userId;
    private String displayName;
    private String avatar;
    private String status;             // ACCEPTED/TENTATIVE/DECLINED/PENDING
}
```

```json
{
  "code": 200,
  "msg": "success,
  "data": [
    {
      "id": 5001,
      "title": "产品评审会",
      "description": "Q4 产品规划评审",
      "startTime": "2026-11-01T14:00:00",
      "endTime": "2026-11-01T16:00:00",
      "allDay": false,
      "location": "会议室 A301",
      "type": "MEETING",
      "displayColor": "#277EFB",
      "attendees": [
        { "userId": 111, "displayName": "张三", "avatar": "http://minio:9000/im-avatar/111/photo.jpg", "status": "ACCEPTED" },
        { "userId": 112, "displayName": "李四", "avatar": "http://minio:9000/im-avatar/112/photo.jpg", "status": "PENDING" },
        { "userId": 113, "displayName": "王五", "avatar": "http://minio:9000/im-avatar/113/photo.jpg", "status": "TENTATIVE" }
      ],
      "reminderMinutes": [15, 60],
      "recurrence": null,
      "createBy": 111,
      "createTime": "2026-10-28T10:00:00",
      "updateTime": "2026-10-28T10:00:00"
    }
  ],
  "timestamp": 1730000000000
}
```

---

## 十一、todo-service 完整 DTO (im-collab 内)

### 11.1 CreateTodoCommand

```java
@Data
public class CreateTodoCommand {
    @NotBlank
    @Size(max = 64)
    private String title;

    @Size(max = 500)
    private String description;

    private String category;           // WORK/PERSONAL/STUDY/OTHER

    @Pattern(regexp = "HIGH/MEDIUM/LOW")
    private String priority;

    private LocalDateTime dueDate;
    private List<Long> assigneeIds;    // 协作人
    private String relatedSessionId;   // 关联会话
    private String relatedMsgId;       // 关联消息
    private Integer reminderMinutes;
}
```

```json
{
  "title": "完成 API 文档修订并发布",
  "description": "根据最新评审意见, 完成 06-RESTful API 文档修订",
  "category": "WORK",
  "priority": "HIGH",
  "dueDate": "2026-10-31T18:00:00",
  "assigneeIds": [111, 112],
  "relatedSessionId": "1780000000001",
  "relatedMsgId": "1780001000040",
  "reminderMinutes": 30
}
```

---

## 十二、search-service 完整 DTO

### 12.1 GlobalSearchQuery

```java
@Data
public class GlobalSearchQuery {
    @NotBlank
    @Size(max = 64)
    private String keyword;
    private Integer page = 1;
    private Integer size = 20;
    private String type;               // MESSAGE/CONTACT/GROUP/DOC/ALL
    private LocalDateTime startTime;    // 时间范围
    private LocalDateTime endTime;
}
```

### 12.2 GlobalSearchVO

```java
@Data
public class GlobalSearchVO {
    private Integer total;
    private List<SearchResultItem> list;
}

@Data
public class SearchResultItem {
    private String type;               // MESSAGE/CONTACT/GROUP/DOC
    private String title;              // 标题/名称
    private String snippet;            // 摘要 (高亮关键词)
    private List<int[]> matchPositions; // 关键词位置
    private String avatar;
    private String routePath;          // 前端路由跳转地址
    private String resourceId;
    private Long sessionId;
    private LocalDateTime time;
    private String senderName;
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 12,
    "list": [
      {
        "type": "MESSAGE",
        "title": "开发二群",
        "snippet": "...明天下午3点<em>开会</em>, 请准时参加...",
        "matchPositions": [[6,8]],
        "avatar": "http://minio:9000/im-group/2001/icon.jpg",
        "resourceId": "1780001000040",
        "sessionId": "1780000000001",
        "time": "2026-10-30T14:30:00",
        "senderName": "张三"
      },
      {
        "type": "CONTACT",
        "title": "李四",
        "snippet": "技术部 - 高级工程师",
        "matchPositions": [],
        "avatar": "http://minio:9000/im-avatar/112/photo.jpg",
        "resourceId": "112",
        "sessionId": null,
        "time": null
      },
      {
        "type": "DOC",
        "title": "产品API文档 v2.1",
        "snippet": "...RESTful API 规范...<em>开会</em>记录...",
        "matchPositions": [[15,17]],
        "avatar": null,
        "resourceId": "3001",
        "time": "2026-10-28T16:00:00",
        "senderName": "张三"
      }
    ]
  },
  "timestamp": 1730000000000
}
```

---

## 十三、admin-service 完整 DTO

### 13.1 AdminUserCreateCommand

```java
@Data
public class AdminUserCreateCommand {
    @NotBlank
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String username;

    @NotBlank
    @Size(min = 6, max = 32)
    private String password;

    @NotBlank
    @Size(max = 32)
    private String displayName;

    private Long deptId;
    private List<Long> roleIds;
    @Email
    private String email;
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String phone;
}
```

```json
{
  "username": "newuser01",
  "password": "Default@123456",
  "displayName": "新员工001",
  "deptId": 10,
  "roleIds": [2],
  "email": "newuser01@company.com",
  "phone": "13800138003"
}
```

### 13.2 AuditLogVO

```java
@Data
public class AuditLogVO {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String module;
    private String operation;
    private String target;
    private String detail;
    private String ip;
    private String userAgent;
    private String result;             // SUCCESS / FAIL
    private String failReason;
    private LocalDateTime operationTime;
}
```

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 150,
    "list": [
      {
        "id": 1780005000001,
        "operatorId": 111,
        "operatorName": "张三",
        "module": "群组",
        "operation": "创建群组",
        "target": "开发二群(id=2001)",
        "detail": "群名称=开发二群, 初始成员=5人",
        "ip": "192.168.1.100",
        "userAgent": "Chrome/130.0.0.0 Windows 11",
        "result": "SUCCESS",
        "failReason": null,
        "operationTime": "2026-10-30T15:00:00"
      },
      {
        "id": 1780005000002,
        "operatorId": 112,
        "operatorName": "李四",
        "module": "消息",
        "operation": "撤回消息",
        "target": "msg-1780001000035",
        "detail": "撤回时间: 发送后 3 分钟",
        "ip": "192.168.1.101",
        "userAgent": "Chrome/130.0.0.0 macOS 14",
        "result": "SUCCESS",
        "failReason": null,
        "operationTime": "2026-10-30T14:55:00"
      }
    ]
  },
  "timestamp": 1730000000000
}
```

---

## 十四、通用异常响应模板

```java
// 参数校验异常 (400)
{
  "code": 10001,
  "msg": "参数校验失败",
  "data": {
    "fieldErrors": [
      { "field": "username", "message": "用户名长度 3-32" },
      { "field": "phone",    "message": "手机号格式不正确" }
    ]
  },
  "timestamp": 1730000000000
}

// 资源不存在 (404)
{
  "code": 40001,
  "msg": "用户不存在 (id=999)",
  "data": null,
  "timestamp": 1730000000000
}

// 权限不足 (403)
{
  "code": 30004,
  "msg": "无权执行此操作, 需要 admin:write 权限",
  "data": null,
  "timestamp": 1730000000000
}

// 服务端异常 (500)
{
  "code": 10002,
  "msg": "系统异常, 请稍后重试",
  "data": null,
  "timestamp": 1730000000000
}

// 限流 (429)
{
  "code": 10003,
  "msg": "请求过于频繁, 请稍后再试",
  "data": {
    "retryAfter": 30
  },
  "timestamp": 1730000000000
}
```

---

## 十五、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-30 | 新增全部 87 个 API 端点的 Java DTO/VO 代码骨架和完整 JSON 示例 |
