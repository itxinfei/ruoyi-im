# RuoYi-IM API 接口文档

> **文档版本**: v1.0
> **更新日期**: 2026-01-13
> **基础路径**: `/api/im`

---

## 目录

1. [认证授权](#1-认证授权)
2. [用户管理](#2-用户管理)
3. [消息管理](#3-消息管理)
4. [会话管理](#4-会话管理)
5. [联系人管理](#5-联系人管理)
6. [群组管理](#6-群组管理)
7. [DING消息](#7-ding消息)
8. [工作台](#8-工作台)
9. [审批管理](#9-审批管理)
10. [考勤管理](#10-考勤管理)
11. [日程管理](#11-日程管理)
12. [文件管理](#12-文件管理)
13. [邮箱管理](#13-邮箱管理)
14. [视频通话](#14-视频通话)
15. [文档管理](#15-文档管理)
16. [应用管理](#16-应用管理)
17. [组织架构](#17-组织架构)
18. [通知管理](#18-通知管理)
19. [会议室管理](#19-会议室管理)
20. [@提及功能](#20-提及功能)
21. [外部联系人](#21-外部联系人)

---

## 1. 认证授权

### 1.1 用户登录
```
POST /api/im/auth/login
```
**请求参数**:
```json
{
  "username": "string",
  "password": "string"
}
```
**响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "string",
    "userId": "long",
    "userInfo": {}
  }
}
```

### 1.2 用户登出
```
POST /api/im/auth/logout
```

### 1.3 刷新Token
```
POST /api/im/auth/refresh
```

### 1.4 验证Token
```
POST /api/im/auth/validateToken
```

---

## 2. 用户管理

### 2.1 获取用户信息
```
GET /api/im/user/profile
```

### 2.2 更新用户信息
```
PUT /api/im/user/profile
```

### 2.3 上传头像
```
POST /api/im/user/avatar/upload
```

### 2.4 获取在线状态
```
GET /api/im/user/online/status
```

### 2.5 搜索用户
```
GET /api/im/user/search
```
**请求参数**:
- `keyword`: 搜索关键词

---

## 3. 消息管理

### 3.1 发送消息
```
POST /api/im/message/send
```
**请求参数**:
```json
{
  "sessionId": "long",
  "content": "string",
  "messageType": "TEXT|IMAGE|FILE|VOICE|VIDEO",
  "replyToMessageId": "long"
}
```

### 3.2 获取消息列表
```
GET /api/im/message/list/{conversationId}
```

### 3.3 撤回消息
```
DELETE /api/im/message/{messageId}/recall
```

### 3.4 编辑消息
```
PUT /api/im/message/{messageId}/edit
```

### 3.5 转发消息
```
POST /api/im/message/forward
```

### 3.6 回复消息
```
POST /api/im/message/reply
```

### 3.7 搜索消息
```
GET /api/im/message/search
```

### 3.8 标记已读
```
POST /api/im/message/read/{messageId}
```

### 3.9 消息收藏
```
POST /api/im/message/favorite/{messageId}
```

### 3.10 取消收藏
```
DELETE /api/im/message/favorite/{messageId}
```

### 3.11 添加表情回应
```
POST /api/im/message/{messageId}/reaction
```

### 3.12 移除表情回应
```
DELETE /api/im/message/{messageId}/reaction
```

---

## 4. 会话管理

### 4.1 获取会话列表
```
GET /api/im/conversation/list
```

### 4.2 创建会话
```
POST /api/im/conversation/create
```

### 4.3 更新会话
```
PUT /api/im/conversation/{conversationId}
```

### 4.4 删除会话
```
DELETE /api/im/conversation/{conversationId}
```

### 4.5 置顶/取消置顶
```
PUT /api/im/conversation/{conversationId}/pin
```

### 4.6 免打扰/取消免打扰
```
PUT /api/im/conversation/{conversationId}/mute
```

### 4.7 标记已读
```
PUT /api/im/conversation/{conversationId}/markAsRead
```

### 4.8 获取未读数量
```
GET /api/im/conversation/unreadCount
```

---

## 5. 联系人管理

### 5.1 获取联系人列表
```
GET /api/im/contact/list
```

### 5.2 添加联系人
```
POST /api/im/contact/request/send
```

### 5.3 删除联系人
```
DELETE /api/im/contact/{friendId}
```

### 5.4 更新联系人备注
```
PUT /api/im/contact/{friendId}/remark
```

### 5.5 搜索联系人
```
GET /api/im/contact/search
```

### 5.6 获取好友申请列表
```
GET /api/im/contact/request/received
```

### 5.7 处理好友申请
```
POST /api/im/contact/request/{requestId}/handle
```

---

## 6. 群组管理

### 6.1 创建群组
```
POST /api/im/group/create
```

### 6.2 获取群组详情
```
GET /api/im/group/{groupId}
```

### 6.3 更新群组
```
PUT /api/im/group/{groupId}
```

### 6.4 解散群组
```
DELETE /api/im/group/{groupId}
```

### 6.5 添加成员
```
POST /api/im/group/{groupId}/members
```

### 6.6 移除成员
```
DELETE /api/im/group/{groupId}/members
```

### 6.7 更新成员角色
```
PUT /api/im/group/{groupId}/members/{memberId}/role
```

### 6.8 获取群公告
```
GET /api/im/group/{groupId}/announcement
```

### 6.9 更新群公告
```
PUT /api/im/group/{groupId}/announcement
```

---

## 7. DING消息

### 7.1 发送DING
```
POST /api/im/ding/send
```
**请求参数**:
```json
{
  "content": "string",
  "receiverType": "USER|GROUP",
  "receiverIds": ["long"],
  "remindType": "APP|SMS|CALL",
  "scheduledTime": "datetime"
}
```

### 7.2 获取接收的DING列表
```
GET /api/im/ding/received
```

### 7.3 获取发送的DING列表
```
GET /api/im/ding/sent
```

### 7.4 获取DING详情
```
GET /api/im/ding/{dingId}
```

### 7.5 标记已读
```
PUT /api/im/ding/{dingId}/read
```

### 7.6 确认DING
```
PUT /api/im/ding/{dingId}/confirm
```

### 7.7 取消定时DING
```
PUT /api/im/ding/{dingId}/cancel
```

### 7.8 获取DING回执
```
GET /api/im/ding/{dingId}/receipts
```

### 7.9 获取DING模板
```
GET /api/im/ding/templates
```

---

## 8. 工作台

### 8.1 获取工作台概览
```
GET /api/im/workbench/overview
```

### 8.2 获取待办列表
```
GET /api/im/workbench/todos
```

### 8.3 创建待办
```
POST /api/im/workbench/todos
```

### 8.4 完成待办
```
PUT /api/im/workbench/todos/{id}/complete
```

### 8.5 删除待办
```
DELETE /api/im/workbench/todos/{id}
```

### 8.6 更新待办
```
PUT /api/im/workbench/todos/{id}
```

---

## 9. 审批管理

### 9.1 创建审批
```
POST /api/im/approval/create
```

### 9.2 获取待我审批列表
```
GET /api/im/approval/pending
```

### 9.3 获取我发起的审批列表
```
GET /api/im/approval/my
```

### 9.4 获取已审批列表
```
GET /api/im/approval/processed
```

### 9.5 通过审批
```
POST /api/im/approval/{id}/approve
```

### 9.6 驳回审批
```
POST /api/im/approval/{id}/reject
```

### 9.7 撤回审批
```
POST /api/im/approval/{id}/cancel
```

### 9.8 获取审批模板
```
GET /api/im/approval/templates
```

---

## 10. 考勤管理

### 10.1 上班打卡
```
POST /api/im/attendance/checkIn
```

### 10.2 下班打卡
```
POST /api/im/attendance/checkOut
```

### 10.3 获取今日打卡状态
```
GET /api/im/attendance/today
```

### 10.4 获取打卡记录列表
```
GET /api/im/attendance/list
```

### 10.5 获取考勤统计
```
GET /api/im/attendance/statistics
```

### 10.6 补卡申请
```
POST /api/im/attendance/{id}/supplement
```

---

## 11. 日程管理

### 11.1 获取日程列表
```
GET /api/im/schedule/list
```

### 11.2 创建日程
```
POST /api/im/schedule/create
```

### 11.3 更新日程
```
PUT /api/im/schedule/{id}
```

### 11.4 删除日程
```
DELETE /api/im/schedule/{id}
```

### 11.5 获取日程详情
```
GET /api/im/schedule/{id}
```

### 11.6 获取时间范围内的日程
```
GET /api/im/schedule/range
```

### 11.7 添加参与者
```
POST /api/im/schedule/{scheduleId}/participants
```

### 11.8 移除参与者
```
DELETE /api/im/schedule/{scheduleId}/participants/{userId}
```

### 11.9 回复参与邀请
```
POST /api/im/schedule/{scheduleId}/response
```

---

## 12. 文件管理

### 12.1 上传文件
```
POST /api/im/file/upload
```
**Content-Type**: `multipart/form-data`

### 12.2 上传图片
```
POST /api/im/file/upload/image
```

### 12.3 获取文件列表
```
GET /api/im/file/list
```

### 12.4 获取文件详情
```
GET /api/im/file/{fileId}
```

### 12.5 下载文件
```
GET /api/im/file/download/{fileId}
```

### 12.6 删除文件
```
DELETE /api/im/file/{fileId}
```

### 12.7 批量删除文件
```
DELETE /api/im/file/batch
```

### 12.8 获取文件预览URL
```
GET /api/im/file/preview/{fileId}
```

### 12.9 获取文件统计
```
GET /api/im/file/statistics
```

### 12.10 获取存储使用情况
```
GET /api/im/file/storage
```

---

## 13. 邮箱管理

### 13.1 获取邮件列表
```
GET /api/im/email/list
```

### 13.2 获取邮件详情
```
GET /api/im/email/{emailId}
```

### 13.3 发送邮件
```
POST /api/im/email/send
```

### 13.4 保存草稿
```
POST /api/im/email/draft
```

### 13.5 标记已读
```
PUT /api/im/email/{emailId}/read
```

### 13.6 标记星标
```
PUT /api/im/email/{emailId}/star
```

### 13.7 移至垃圾箱
```
PUT /api/im/email/{emailId}/trash
```

### 13.8 永久删除
```
DELETE /api/im/email/{emailId}
```

### 13.9 获取未读数量
```
GET /api/im/email/unread/count
```

---

## 14. 视频通话

### 14.1 发起通话
```
POST /api/im/video-call/initiate
```

### 14.2 接听通话
```
POST /api/im/video-call/{callId}/accept
```

### 14.3 拒绝通话
```
POST /api/im/video-call/{callId}/reject
```

### 14.4 结束通话
```
POST /api/im/video-call/{callId}/end
```

### 14.5 获取通话信息
```
GET /api/im/video-call/{callId}
```

### 14.6 获取当前通话
```
GET /api/im/video-call/active
```

### 14.7 发送WebRTC信令
```
POST /api/im/video-call/signal
```

### 14.8 获取通话历史
```
GET /api/im/video-call/history
```

---

## 15. 文档管理

### 15.1 创建文档
```
POST /api/im/document/create
```

### 15.2 获取文档列表
```
GET /api/im/document/list
```

### 15.3 获取文档详情
```
GET /api/im/document/{documentId}
```

### 15.4 更新文档
```
PUT /api/im/document/update
```

### 15.5 删除文档
```
DELETE /api/im/document/{documentId}
```

### 15.6 永久删除文档
```
DELETE /api/im/document/{documentId}/permanent
```

### 15.7 恢复文档
```
POST /api/im/document/{documentId}/restore
```

### 15.8 搜索文档
```
GET /api/im/document/search
```

### 15.9 收藏文档
```
POST /api/im/document/{documentId}/star
```

### 15.10 取消收藏
```
PUT /api/im/document/{documentId}/unstar
```

### 15.11 分享文档
```
POST /api/im/document/share
```

### 15.12 添加评论
```
POST /api/im/document/{documentId}/comments
```

### 15.13 获取版本历史
```
GET /api/im/document/{documentId}/versions
```

### 15.14 恢复版本
```
POST /api/im/document/{documentId}/versions/{versionId}/restore
```

---

## 16. 应用管理

### 16.1 获取应用列表
```
GET /api/im/app/list
```

### 16.2 获取可见应用
```
GET /api/im/app/visible
```

### 16.3 按分类获取应用
```
GET /api/im/app/category
```

### 16.4 获取应用详情
```
GET /api/im/app/{id}
```

### 16.5 创建应用
```
POST /api/im/app/create
```

### 16.6 更新应用
```
PUT /api/im/app/{id}
```

### 16.7 删除应用
```
DELETE /api/im/app/{id}
```

### 16.8 设置应用可见性
```
PUT /api/im/app/{id}/visibility
```

---

## 17. 组织架构

### 17.1 获取部门树
```
GET /api/im/organization/department/tree
```

### 17.2 获取部门详情
```
GET /api/im/organization/department/{departmentId}
```

### 17.3 创建部门
```
POST /api/im/organization/department
```

### 17.4 更新部门
```
PUT /api/im/organization/department
```

### 17.5 删除部门
```
DELETE /api/im/organization/department/{departmentId}
```

### 17.6 获取部门成员
```
GET /api/im/organization/department/{departmentId}/members
```

### 17.7 添加部门成员
```
POST /api/im/organization/department/member
```

### 17.8 移除部门成员
```
DELETE /api/im/organization/department/{departmentId}/member/{userId}
```

---

## 18. 通知管理

### 18.1 获取通知列表
```
GET /api/im/notification/list
```

### 18.2 获取未读通知
```
GET /api/im/notification/unread
```

### 18.3 标记已读
```
PUT /api/im/notification/{id}/read
```

### 18.4 删除通知
```
DELETE /api/im/notification/{id}
```

### 18.5 全部标记已读
```
PUT /api/im/notification/read/all
```

### 18.6 获取未读数量
```
GET /api/im/notification/unread/count
```

---

## 19. 会议室管理

### 19.1 创建会议室
```
POST /api/im/meeting-room/create
```

### 19.2 更新会议室
```
PUT /api/im/meeting-room
```

### 19.3 删除会议室
```
DELETE /api/im/meeting-room/{roomId}
```

### 19.4 获取会议室详情
```
GET /api/im/meeting-room/{roomId}
```

### 19.5 分页查询会议室
```
POST /api/im/meeting-room/page
```

### 19.6 获取可用会议室
```
GET /api/im/meeting-room/available
```

### 19.7 预订会议室
```
POST /api/im/meeting-room/book
```

### 19.8 取消预订
```
POST /api/im/meeting-room/booking/{bookingId}/cancel
```

### 19.9 确认预订
```
POST /api/im/meeting-room/booking/{bookingId}/confirm
```

### 19.10 签到
```
POST /api/im/meeting-room/booking/{bookingId}/check-in
```

### 19.11 签退
```
POST /api/im/meeting-room/booking/{bookingId}/check-out
```

### 19.12 获取会议室日程
```
GET /api/im/meeting-room/{roomId}/schedule
```

### 19.13 检查可用性
```
GET /api/im/meeting-room/{roomId}/availability
```

### 19.14 提交反馈
```
POST /api/im/meeting-room/booking/{bookingId}/feedback
```

---

## 20. @提及功能

### 20.1 获取可@用户列表
```
GET /api/im/mention/users/{conversationId}
```

### 20.2 检查是否可@所有人
```
GET /api/im/mention/can-mention-all/{conversationId}
```

### 20.3 获取未读@提及
```
GET /api/im/mention/unread
```

### 20.4 获取未读@提及数量
```
GET /api/im/mention/unread/count
```

### 20.5 标记@提及已读
```
PUT /api/im/mention/{messageId}/read
```

### 20.6 批量标记已读
```
PUT /api/im/mention/read/batch
```

---

## 21. 外部联系人

### 21.1 创建外部联系人
```
POST /api/im/external-contact
```

### 21.2 更新外部联系人
```
PUT /api/im/external-contact/{contactId}
```

### 21.3 删除外部联系人
```
DELETE /api/im/external-contact/{contactId}
```

### 21.4 获取外部联系人详情
```
GET /api/im/external-contact/{contactId}
```

### 21.5 获取外部联系人列表
```
GET /api/im/external-contact/list
```

### 21.6 获取分组下的联系人
```
GET /api/im/external-contact/group/{groupId}
```

### 21.7 获取星标联系人
```
GET /api/im/external-contact/starred
```

### 21.8 搜索外部联系人
```
GET /api/im/external-contact/search
```

### 21.9 切换星标状态
```
PUT /api/im/external-contact/{contactId}/star
```

### 21.10 创建分组
```
POST /api/im/external-contact/group
```

### 21.11 更新分组
```
PUT /api/im/external-contact/group/{groupId}
```

### 21.12 删除分组
```
DELETE /api/im/external-contact/group/{groupId}
```

### 21.13 获取分组列表
```
GET /api/im/external-contact/group/list
```

---

## 通用响应格式

所有接口返回格式如下：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

**状态码说明**:
- `200`: 操作成功
- `400`: 请求参数错误
- `401`: 未授权，需要登录
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器内部错误

## 认证方式

使用JWT Token认证，在请求头中添加：

```
Authorization: Bearer {token}
```

---

**文档维护**: RuoYi-IM 开发团队
