# 24 RESTful API 端点清单

> **定位**：后端 62 个 Controller 全部 API 端点的完整目录。
> **数据来源**：backend controller + frontend api/ 目录交叉统计
> **实现状态**：每个端点标注前端是否已封装

---

## 一、消息模块 — `ImMessageController`（14 端点）

| # | 方法 | 路径 | 前端封装 | 说明 |
|---|------|------|---------|------|
| 1 | POST | `/im/message/send` | `message.sendMessage` | 发送消息 |
| 2 | GET | `/im/message/list` | `message.getMessages` | 历史消息 |
| 3 | POST | `/im/message/recall` | `message.recallMessage` | 撤回（2分钟内）|
| 4 | PUT | `/im/message/edit` | `message.editMessage` | 编辑（5分钟内）|
| 5 | DELETE | `/im/message/delete` | `message.deleteMessage` | 删除 |
| 6 | POST | `/im/message/forward` | `message.forwardMessage` | 转发 |
| 7 | GET | `/im/message/search` | `message.searchMessages` | 消息搜索 |
| 8 | GET | `/im/message/detail` | — | 消息详情 |
| 9 | POST | `/im/message/markRead` | `message.markAsRead` | 标记已读 |
| 10 | GET | `/im/message/readUsers` | `message.getMessageReadUsers` | 已读列表 |
| 11 | GET | `/im/message/unreadCount` | — | 未读数 |
| 12 | POST | `/im/message/reaction` | — | 消息反应 |
| 13 | DELETE | `/im/message/reaction` | — | 取消反应 |
| 14 | POST | `/im/message/clear` | `message.clearConversationMessages` | 清空会话消息 |

## 二、会话模块 — `ImSessionController`（14 端点）

| # | 方法 | 路径 | 前端封装 | 说明 |
|---|------|------|---------|------|
| 1 | GET | `/im/session/list` | `conversation.getConversations` | 会话列表 |
| 2 | GET | `/im/session/{id}` | `conversation.getConversation` | 会话详情 |
| 3 | POST | `/im/session/create` | `conversation.createConversation` | 创建会话 |
| 4 | DELETE | `/im/session/{id}` | `conversation.deleteConversation` | 删除会话 |
| 5 | PUT | `/im/session/pin` | `conversation.pinConversation` | 置顶 |
| 6 | PUT | `/im/session/mute` | `conversation.muteConversation` | 免打扰 |
| 7 | POST | `/im/session/markRead` | `conversation.markConversationAsRead` | 会话全部已读 |
| 8 | POST | `/im/session/draft` | `conversation.saveDraft` | 保存草稿 |
| 9-14 | — | 会话分组 CRUD | `conversation.getSessionGroups/createSessionGroup/...` | 会话分组管理 |

## 三、联系人模块 — `ImContactController`（16 端点）

| # | 方法 | 路径 | 前端封装 | 说明 |
|---|------|------|---------|------|
| 1 | GET | `/im/contact/list` | `contact.getContacts` | 联系人列表 |
| 2 | GET | `/im/contact/search` | `contact.searchContacts` | 搜索联系人 |
| 3 | GET | `/im/contact/{id}` | `contact.getContact` | 联系人详情 |
| 4 | POST | `/im/contact/request/send` | `contact.sendFriendRequest` | 发送好友请求 |
| 5 | POST | `/im/contact/request/handle` | `contact.handleFriendRequest` | 处理好友请求 |
| 6 | GET | `/im/contact/request/list` | `contact.getFriendRequests` | 好友请求列表 |
| 7 | PUT | `/im/contact/remark` | `contact.updateContactRemark` | 修改备注 |
| 8 | DELETE | `/im/contact/{id}` | `contact.deleteContact` | 删除联系人 |
| 9 | PUT | `/im/contact/group` | `contact.moveContactToGroup` | 移动分组 |
| 10-16 | — | 组织架构/部门/成员 | `organization.*` | 11 个 API |

## 四、群组模块 — `ImGroupController`（14 端点）

| # | 方法 | 路径 | 前端封装 | 说明 |
|---|------|------|---------|------|
| 1 | GET | `/im/group/list` | `group.getGroups` | 群组列表 |
| 2 | GET | `/im/group/{id}` | `group.getGroup` | 群组详情 |
| 3 | POST | `/im/group/create` | `group.createGroup` | 创建群组 |
| 4 | POST | `/im/group/dismiss` | `group.dismissGroup` | 解散群组 |
| 5 | POST | `/im/group/leave` | `group.leaveGroup` | 退出群组 |
| 6 | POST | `/im/group/members/add` | `group.addGroupMembers` | 添加成员 |
| 7 | DELETE | `/im/group/members/{id}` | `group.removeGroupMember` | 移除成员 |
| 8 | PUT | `/im/group/admin` | `group.setGroupAdmin` | 设置管理员 |
| 9 | PUT | `/im/group/owner` | `group.transferGroupOwner` | 转让群主 |
| 10 | PUT | `/im/group/update` | `group.updateGroup` | 修改群信息 |
| 11 | GET | `/im/group/members` | `group.getGroupMembers` | 成员列表 |
| 12 | PUT | `/im/group/mute/member` | `group.muteGroupMember` | 禁言 |
| 13 | PUT | `/im/group/mute/member/cancel` | `group.unmuteGroupMember` | 取消禁言 |
| 14 | — | 群公告/群文件 | `groupAnnouncement.*` / `groupFile.*` | 20 个 API |

## 五、用户模块 — `ImUserController`（13 端点）

| # | 方法 | 路径 | 前端封装 | 说明 |
|---|------|------|---------|------|
| 1 | GET | `/im/user/info` | `user.getUserInfo` | 当前用户信息 |
| 2 | PUT | `/im/user/info` | `user.updateUserInfo` | 修改用户信息 |
| 3 | GET | `/im/user/settings` | — | 用户设置 |
| 4 | PUT | `/im/user/settings` | — | 修改设置 |
| 5 | GET | `/im/user/byUsername` | — | 按用户名查找 |
| 6 | GET | `/im/user/departmentMembers` | — | 部门成员 |
| 7-13 | — | 管理后台/创建/删除/列表/状态 | `user.*` | 7 个 API |

## 六、认证模块 — `ImAuthController`（7 端点）

| # | 方法 | 路径 | 前端封装 | 说明 |
|---|------|------|---------|------|
| 1 | POST | `/im/auth/login` | `auth.login` | 登录 |
| 2 | POST | `/im/auth/logout` | `auth.logout` | 登出 |
| 3 | GET | `/im/auth/captcha` | `auth.getCaptcha` | 获取验证码 |
| 4 | POST | `/im/auth/refreshToken` | `auth.refreshToken` | 刷新 Token |
| 5 | POST | `/im/auth/qrcode/generate` | — | 生成登录二维码 |
| 6 | GET | `/im/auth/qrcode/status` | — | 二维码状态轮询 |
| 7 | GET | `/im/auth/sms/code` | — | 短信验证码 |

## 七、文件模块 — `ImFileController`（11 端点）+ 分片上传（8 端点）

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | POST | `/im/file/upload` | 单文件上传 |
| 2 | GET | `/im/file/download/{id}` | 文件下载 |
| 3 | DELETE | `/im/file/{id}` | 删除文件 |
| 4 | GET | `/im/file/info/{id}` | 文件信息 |
| 5-11 | — | 文件分类/批量操作 | 7 个 |
| 12-19 | — | 分片上传 | 分片初始化/上传列表/合并 — 8 个 |

## 八、云盘模块 — `ImCloudDriveController`（25 端点）

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | `/im/cloud/list` | 文件列表 |
| 2 | POST | `/im/cloud/folder/create` | 创建文件夹 |
| 3 | POST | `/im/cloud/upload` | 文件上传 |
| 4 | GET | `/im/cloud/download/{id}` | 下载 |
| 5 | DELETE | `/im/cloud/{id}` | 删除 |
| 6 | PUT | `/im/cloud/rename` | 重命名 |
| 7 | POST | `/im/cloud/move` | 移动 |
| 8 | POST | `/im/cloud/copy` | 复制 |
| 9 | POST | `/im/cloud/share` | 分享 |
| 10 | GET | `/im/cloud/share/list` | 分享列表 |
| 11 | GET | `/im/cloud/recycle/list` | 回收站 |
| 12 | POST | `/im/cloud/recycle/restore` | 恢复 |
| 13-25 | — | 版本/协作/权限等 | 13 个 |

## 九、审批模块 — `ImApprovalController`（13 端点）

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | POST | `/im/approval/submit` | 提交审批 |
| 2 | GET | `/im/approval/pending` | 待我审批 |
| 3 | GET | `/im/approval/submitted` | 我发起的 |
| 4 | GET | `/im/approval/cc` | 抄送我的 |
| 5-8 | — | 审批通过/驳回/撤回/加签 | 4 个 |
| 9-13 | — | 模板管理 | 5 个 |

## 十、DING 模块 — `ImDingMessageController`（11 端点）

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | POST | `/im/ding/send` | 发送 DING |
| 2 | GET | `/im/ding/received` | 我收到的 |
| 3 | GET | `/im/ding/sent` | 我发送的 |
| 4 | GET | `/im/ding/{id}` | DING 详情 |
| 5 | POST | `/im/ding/markRead` | 标记已读 |
| 6 | POST | `/im/ding/confirm` | 确认收到 |
| 7 | GET | `/im/ding/receipts` | 回执列表 |
| 8 | DELETE | `/im/ding/{id}` | 撤回 DING |
| 9-11 | — | 模板管理 | 3 个 |

## 十一、日历/日程模块 — `ImScheduleEventController`（10 端点）

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | `/im/schedule/list` | 日程列表 |
| 2 | POST | `/im/schedule/create` | 创建日程 |
| 3 | PUT | `/im/schedule/{id}/update` | 更新日程 |
| 4 | DELETE | `/im/schedule/{id}` | 删除日程 |
| 5 | GET | `/im/schedule/{id}` | 日程详情 |
| 6 | POST | `/im/schedule/{id}/reminder` | 设置提醒 |
| 7-10 | — | 重复事件/忙闲状态/共享日历 | 4 个 |

## 十二、待办模块 — `ImTodoItemController`（7 端点）

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | `/im/todo/list` | 待办列表 |
| 2 | POST | `/im/todo/create` | 创建待办 |
| 3 | PUT | `/im/todo/{id}/update` | 更新待办 |
| 4 | PUT | `/im/todo/{id}/complete` | 完成待办 |
| 5 | DELETE | `/im/todo/{id}` | 删除待办 |
| 6 | GET | `/im/todo/categories` | 分类列表 |
| 7 | PUT | `/im/todo/sort` | 排序 |

## 十三、邮件模块 — `ImEmailController`（16 端点）

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | GET | `/im/email/list` | 邮件列表 |
| 2 | GET | `/im/email/{id}` | 邮件详情 |
| 3 | POST | `/im/email/send` | 发邮件 |
| 4 | POST | `/im/reply` | 回复 |
| 5 | POST | `/im/email/forward` | 转发 |
| 6 | DELETE | `/im/email/{id}` | 删除 |
| 7 | POST | `/im/email/star` | 标星 |
| 8 | POST | `/im/email/move` | 移动到文件夹 |
| 9 | GET | `/im/email/folders` | 邮箱文件夹 |
| 10-16 | — | 附件/草稿/搜索等 | 7 个 |

## 十四、通话模块 — `ImCallController`（15 端点）+ 会议（18 端点）

| 模块 | 端点数 | 说明 |
|------|--------|------|
| 视频通话 | 15 | `initiateCall` / `accept` / `reject` / `end` / `getInfo` / `getActive` / `sendSignal` / `getCallHistory` / `initiateGroupCall` 等 |
| 视频会议 | 18 | 会议室预约/会议创建/加入/离开/录制等 |

## 十五、工作台模块 — `ImWorkbenchController`（16 端点）

| # | 说明 |
|---|------|
| 1-8 | 工作台布局/首页数据/应用入口/常用联系人 |
| 9-16 | 数据统计/快捷操作/常用功能 |

## 十六、文档协作模块 — `ImDocumentController`（17 端点）+ `ImDocumentCollaborationController`（11 端点）

| 模块 | 端点数 | 说明 |
|------|--------|------|
| 文档 CRUD | 17 | 创建/编辑/删除/分享/历史/权限 |
| 协同编辑 | 11 | ONLYOFFICE 协作/实时光标/版本 |

## 十七、其余模块

| Controller | 端点数 | 模块 |
|-----------|--------|------|
| `ImAttendanceController` | 10 | 考勤打卡 |
| `ImAttendanceGroupController` | 15 | 考勤组 |
| `ImWorkReportController` | 15 | 工作报告 |
| `ImTaskController` | 19 | 任务管理 |
| `ImMail` — 已统计 | — | — |
| `ImConfigController` | 12 | 系统配置 |
| `ImNotificationController` | 10 | 系统通知 |
| `ImBackupController` | 8 | 数据备份 |
| `ImTranslationController` | 4 | 消息翻译 |
| `ImAIController` | 5 | AI 助手 |
| `ImUrlMetadataController` | 3 | 链接卡片 |
| `ImVoiceTranscriptController` | 6 | 语音转文字 |
| `ImMentionController` | 7 | @提及 |
| `ImMessageMarkerController` | 9 | 消息标记 |
| `ImMessageReadController` | 3 | 已读详情 |
| `ImMessageEditHistoryController` | 3 | 编辑历史 |
| `ImMessageFavoriteController` | 8 | 消息收藏 |
| `ImMessageReactionController` | 5 | 消息表情回复 |
| `ImGroupMuteController` | 8 | 群禁言 |
| `ImGlobalSearchController` | 8 | 全局搜索 |
| `ImFilePreviewController` | 5 | 文件预览 |
| `ImFileChunkUploadController` | 8 | 分片上传 |
| `ImExternalContactController` | 14 | 外部联系人 |
| `ImAuditController` | 6 | 审计日志 |
| `ImSensitiveWordController` | 5 | 敏感词 |
| `ImHealthCheckController` | 5 | 健康检查 |
| `ImMonitorController` | 9 | 监控 |
| `ImAppController` | 10 | 应用管理 |
| `ImOrganizationController` | 12 | 组织架构 |
| `ImGroupsController` | 7 | 群组 (旧版) |
| `ImSessionController` | 8 | 会话 (旧版) |
| admin/* | 5 Controller | 管理后台 |
| 合计 | **约 350+ 端点** | 62 个 Controller |

## 十八、变更记录

| 日期 | 变更 |
|------|------|
| 2026-10-28 | 输出全量端点清单：62 Controller、350+ 端点按模块归类，标注前端封装情况 |
