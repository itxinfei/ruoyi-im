# RuoYi-IM 完整架构文档

**版本**: 1.0
**更新日期**: 2025-01-25
**状态**: 当前架构

---

## 一、项目概述

RuoYi-IM 是一个**内网环境部署**的企业级即时通讯与办公协作系统，采用 Java 后端和 Vue 3 前端架构。

### 核心特性
- 即时通讯（单聊、群聊、富媒体消息）
- 企业办公一体化（待办、审批、邮件、文档、日程）
- 组织架构管理（部门、职位、权限）
- 管理后台（基于角色的权限控制）

### 部署环境
- **部署方式**: 内网部署
- **数据安全**: 高
- **网络安全**: 中等

---

## 二、技术架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        ruoyi-im-web                          │
│                   (Vue 3 + Vite, :5173)                     │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐ │
│  │   聊天界面   │   工作台     │   联系人     │  管理后台    │ │
│  └─────────────┴─────────────┴─────────────┴─────────────┘ │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │   REST API + WebSocket    │
                    └─────────────┬─────────────┘
                                  │
┌─────────────────────────────────┴───────────────────────────┐
│                       ruoyi-im-api                           │
│                  (Spring Boot 2.7.18, :8080)                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Spring Security + JWT                    │  │
│  │  ┌────────────────┬────────────────────────────────┐ │  │
│  │  │  /api/im/*      │   /api/admin/*                 │ │  │
│  │  │  用户功能API     │   管理员API                    │ │  │
│  │  └────────────────┴────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │         MySQL             │
                    │       (utf8mb4)           │
                    └───────────────────────────┘
```

### 2.2 技术栈

#### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| Spring Security | 5.7.x | 认证授权 |
| MyBatis-Plus | 3.5.3 | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| Redis | 3.0+ | 缓存/会话 |
| WebSocket | - | 实时通信 |
| JWT | 0.11.5 | Token 认证 |
| Hutool | 5.8.18 | 工具类 |

#### 前端
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.3.11 | 前端框架 |
| Vite | 5.0.7 | 构建工具 |
| Element Plus | 2.4.4 | UI 组件库 |
| Vuex | 4.1.0 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.2 | HTTP 客户端 |
| Day.js | 1.11.10 | 日期处理 |

---

## 三、模块设计

### 3.1 功能模块总览

```
RuoYi-IM
├── 即时通讯 (IM)
│   ├── 单聊
│   ├── 群聊
│   ├── 消息类型：文本/图片/文件/语音/视频
│   └── 消息操作：撤回/编辑/转发/引用/收藏/表情回应
│
├── 联系人 (Contacts)
│   ├── 好友管理：添加/删除/备注/分组
│   ├── 组织架构：部门树/职位筛选
│   └── 外部联系人
│
├── 工作台 (Workbench)
│   ├── 待办事项 (Todo)
│   ├── 审批流程 (Approval)
│   ├── 邮件 (Mail)
│   ├── AI 助理 (Assistant)
│   ├── 文档管理 (Documents)
│   └── 日程日历 (Calendar)
│
└── 管理后台 (Admin)
    ├── 数据概览 (Dashboard)
    ├── 用户管理 (UserManagement)
    ├── 群组管理 (GroupManagement)
    └── 消息管理 (MessageManagement)
```

### 3.2 后端模块结构

```
com.ruoyi.im/
├── ImApplication.java              # 启动类
│
├── controller/                     # 控制器层
│   ├── ImAuthController.java       # 认证：登录/注册
│   ├── ImMessageController.java    # 消息：发送/查询/撤回/编辑
│   ├── ImConversationController.java # 会话管理
│   ├── ImContactController.java    # 联系人
│   ├── ImUserController.java       # 用户信息
│   ├── ImGroupController.java      # 群组
│   ├── ImSessionController.java    # 会话列表
│   ├── ImTodoController.java       # 待办事项
│   ├── ImApprovalController.java   # 审批流程
│   ├── ImMailController.java       # 邮件
│   ├── ImDocumentController.java   # 文档
│   ├── ImAnnouncementController.java # 公告
│   └── admin/                      # 管理员控制器
│       ├── ImUserAdminController.java
│       ├── ImGroupAdminController.java
│       ├── ImMessageAdminController.java
│       └── ImStatsController.java
│
├── service/                        # 服务层
│   ├── ImMessageService.java
│   ├── ImConversationService.java
│   ├── ImUserService.java
│   ├── ImGroupService.java
│   ├── ImWebSocketBroadcastService.java # WebSocket 广播
│   └── impl/                       # 服务实现
│
├── mapper/                         # 数据访问层
│   ├── ImMessageMapper.java
│   ├── ImConversationMapper.java
│   ├── ImUserMapper.java
│   └── ...
│
├── domain/                         # 实体类 (40+)
│   ├── ImUser.java                 # 用户
│   ├── ImMessage.java              # 消息
│   ├── ImConversation.java         # 会话
│   ├── ImConversationMember.java   # 会话成员
│   ├── ImGroup.java                # 群组
│   ├── ImGroupMember.java          # 群组成员
│   ├── ImFriend.java               # 好友关系
│   ├── ImTodoItem.java             # 待办事项
│   ├── ImApproval.java             # 审批
│   ├── ImApprovalNode.java         # 审批节点
│   ├── ImApprovalRecord.java       # 审批记录
│   ├── ImMail.java                 # 邮件
│   ├── ImDocument.java             # 文档
│   ├── ImAnnouncement.java         # 公告
│   ├── ImDepartment.java           # 部门
│   └── ...
│
├── dto/                            # 数据传输对象
│   └── vo/                         # 视图对象
│
├── websocket/                      # WebSocket 处理
│   ├── ImWebSocketHandler.java     # 消息处理
│   └── ImWebSocketManager.java     # 连接管理
│
├── security/                       # 安全配置
│   ├── SecurityConfig.java         # Spring Security 配置
│   ├── JwtAuthenticationTokenFilter.java # JWT 过滤器
│   └── ...
│
├── config/                         # 配置类
│   ├── WebSocketConfig.java
│   ├── RedisConfig.java
│   └── ...
│
├── common/                         # 公共类
│   ├── BaseEntity.java             # 基础实体
│   ├── Result.java                 # 统一响应
│   └── BusinessException.java      # 业务异常
│
└── constant/                       # 常量
    ├── UserRole.java               # 角色常量
    └── MessageType.java            # 消息类型常量
```

### 3.3 前端模块结构

```
ruoyi-im-web/src/
├── main.js                         # 入口文件
├── App.vue                         # 根组件
│
├── api/                            # API 封装
│   ├── request.js                  # Axios 实例
│   ├── im/                         # IM API
│   │   ├── index.js                # 统一导出
│   │   ├── message.js
│   │   ├── conversation.js
│   │   ├── contact.js
│   │   ├── user.js
│   │   ├── group.js
│   │   ├── auth.js
│   │   ├── todo.js
│   │   ├── approval.js
│   │   └── ...
│   └── admin.js                    # 管理员 API
│
├── views/                          # 页面视图
│   ├── auth/
│   │   └── LoginPage.vue           # 登录页
│   ├── MainPage.vue                # 主页面容器
│   ├── ChatPanel.vue               # 聊天面板
│   ├── ContactsPanel.vue           # 联系人面板
│   ├── SessionPanel.vue            # 会话列表面板
│   ├── WorkbenchPanel.vue          # 工作台面板（入口）
│   ├── TodoPanel.vue               # 待办事项
│   ├── ApprovalPanel.vue           # 审批流程
│   ├── MailPanel.vue               # 邮件
│   ├── AssistantPanel.vue          # AI 助理
│   ├── DocumentsPanel.vue          # 文档管理
│   ├── CalendarPanel.vue           # 日程日历
│   └── admin/                      # 管理后台
│       ├── AdminLayout.vue         # 管理后台布局
│       ├── Dashboard.vue           # 数据概览
│       ├── UserManagement.vue      # 用户管理
│       ├── GroupManagement.vue     # 群组管理
│       └── MessageManagement.vue   # 消息管理
│
├── components/                     # 组件
│   ├── Chat/                       # 聊天组件
│   │   ├── ChatHeader.vue
│   │   ├── MessageList.vue
│   │   ├── MessageInput.vue
│   │   └── MessageBubble.vue       # 消息气泡
│   ├── Contacts/                   # 联系人组件
│   │   ├── ContactList.vue
│   │   ├── ContactDetail.vue
│   │   ├── OrganizationTree.vue    # 组织架构树
│   │   └── GroupsView.vue
│   ├── Common/                     # 通用组件
│   │   ├── DingtalkAvatar.vue      # 钉钉风格头像
│   │   ├── PersonalProfileDialog.vue
│   │   └── ...
│   └── Workbench/                  # 工作台组件
│       ├── TodoItem.vue
│       ├── ApprovalCard.vue
│       └── ...
│
├── store/                          # Vuex 状态管理
│   ├── index.js
│   └── modules/
│       ├── im.js                   # IM 状态
│       ├── user.js                 # 用户状态
│       └── chat.js                 # 聊天状态
│
├── router/                         # 路由配置
│   └── index.js                    # 含权限守卫
│
├── composables/                    # 组合式函数
│   ├── useImWebSocket.js           # WebSocket
│   └── ...
│
├── utils/                          # 工具函数
│   ├── websocket/
│   │   └── imWebSocket.js          # WebSocket 单例
│   └── ...
│
└── styles/                         # 样式文件
    ├── variables.scss              # 变量
    └── common.scss                 # 通用样式
```

---

## 四、数据库设计

### 4.1 核心表分类

#### 即时通讯 (IM)
| 表名 | 说明 |
|------|------|
| `im_user` | 用户表 |
| `im_message` | 消息表 |
| `im_message_edit_history` | 消息编辑历史 |
| `im_message_read` | 消息已读记录 |
| `im_message_favorite` | 消息收藏 |
| `im_message_mention` | 消息@提醒 |
| `im_message_reaction` | 消息表情回应 |
| `im_conversation` | 会话表 |
| `im_conversation_member` | 会话成员表 |
| `im_session` | 会话列表 |
| `im_group` | 群组表 |
| `im_group_member` | 群组成员 |
| `im_group_announcement` | 群公告 |
| `im_group_blacklist` | 群黑名单 |
| `im_group_file` | 群文件 |
| `im_friend` | 好友关系表 |
| `im_friend_request` | 好友申请 |

#### 联系人
| 表名 | 说明 |
|------|------|
| `im_department` | 部门表 |
| `im_department_member` | 部门成员 |
| `im_external_contact` | 外部联系人 |
| `im_external_contact_group` | 外部联系人分组 |

#### 工作台
| 表名 | 说明 |
|------|------|
| `im_todo_item` | 待办事项 |
| `im_task` | 任务表 |
| `im_task_comment` | 任务评论 |
| `im_task_attachment` | 任务附件 |
| `im_approval` | 审批表 |
| `im_approval_template` | 审批模板 |
| `im_approval_node` | 审批节点 |
| `im_approval_record` | 审批记录 |
| `im_approval_form_data` | 审批表单数据 |
| `im_mail` | 邮件表 |
| `im_document` | 文档表 |
| `im_document_comment` | 文档评论 |
| `im_document_share` | 文档分享 |
| `im_document_version` | 文档版本 |
| `im_announcement` | 公告表 |
| `im_announcement_read` | 公告已读 |
| `im_announcement_like` | 公告点赞 |
| `im_announcement_comment` | 公告评论 |
| `im_schedule_event` | 日程事件 |
| `im_schedule_participant` | 日程参与者 |
| `im_work_report` | 工作报告 |
| `im_work_report_comment` | 工作报告评论 |
| `im_work_report_like` | 工作报告点赞 |

#### 系统功能
| 表名 | 说明 |
|------|------|
| `im_user_setting` | 用户设置 |
| `im_user_config` | 用户配置 |
| `im_system_notification` | 系统通知 |
| `im_audit_log` | 审计日志 |
| `im_sensitive_word` | 敏感词 |
| `im_file_asset` | 文件资产 |
| `im_file_share` | 文件分享 |
| `im_file_chunk_upload` | 分片上传 |
| `im_file_chunk_detail` | 分片详情 |
| `im_backup` | 备份记录 |
| `im_attendance` | 考勤记录 |
| `im_attendance_statistics` | 考勤统计 |
| `im_meeting_room` | 会议室 |
| `im_meeting_booking` | 会议室预订 |
| `im_video_call` | 视频通话 |
| `im_ding_message` | 钉消息 |
| `im_ding_template` | 钉消息模板 |
| `im_ding_receipt` | 钉消息回执 |

### 4.2 核心表结构

#### im_user (用户表)
```sql
CREATE TABLE im_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    gender TINYINT DEFAULT 0 COMMENT '性别:0=未知,1=男,2=女',
    mobile VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态:0=禁用,1=启用',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色:USER/ADMIN/SUPER_ADMIN',
    signature VARCHAR(200) COMMENT '签名',
    department VARCHAR(100) COMMENT '部门',
    position VARCHAR(100) COMMENT '职位',
    last_online_time DATETIME COMMENT '最后在线时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### im_message (消息表)
```sql
CREATE TABLE im_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    group_id BIGINT COMMENT '群组ID',
    receiver_id BIGINT COMMENT '接收者ID(私聊)',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型:TEXT/IMAGE/FILE/VOICE/VIDEO',
    content TEXT COMMENT '消息内容(JSON)',
    reply_to_message_id BIGINT COMMENT '回复的消息ID',
    forward_from_message_id BIGINT COMMENT '转发来源消息ID',
    is_revoked TINYINT DEFAULT 0 COMMENT '是否撤回',
    is_edited TINYINT DEFAULT 0 COMMENT '是否编辑过',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_conversation (conversation_id),
    INDEX idx_sender (sender_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 五、权限设计

### 5.1 角色定义

| 角色 | 代码 | 权限范围 |
|------|------|----------|
| 普通用户 | `USER` | 聊天、联系人、群组、个人设置、工作台 |
| 管理员 | `ADMIN` | 所有用户权限 + 用户管理、群组管理、消息管理、数据统计 |
| 超级管理员 | `SUPER_ADMIN` | 所有权限 + 角色管理、系统配置、数据备份 |

### 5.2 API 权限控制

```java
// Spring Security 配置
http.authorizeRequests()
    // 公开接口
    .antMatchers("/api/im/auth/**").permitAll()
    // 管理员接口
    .antMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
    // 用户接口
    .antMatchers("/api/im/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
    // WebSocket
    .antMatchers("/ws/im").authenticated();
```

### 5.3 方法级权限控制

```java
// Controller 方法级
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public Result<Void> deleteUser(@PathVariable Long id) { ... }

@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public Result<Void> updateRole(@PathVariable Long id, @RequestParam String role) { ... }
```

---

## 六、通信设计

### 6.1 WebSocket 消息类型

| 类型 | 方向 | 描述 |
|------|------|------|
| `auth` | 客户端→服务端 | 身份认证 |
| `message` | 双向 | 聊天消息 |
| `ping/pong` | 双向 | 心跳检测 (30秒间隔) |
| `read` | 客户端→服务端 | 已读回执 |
| `typing` | 客户端→服务端 | 输入状态提示 |
| `presence` | 服务端→客户端 | 在线状态变更 |
| `notification` | 服务端→客户端 | 系统通知 |

### 6.2 消息流转流程

```
┌─────────┐                    ┌─────────┐
│  用户A   │                    │  用户B   │
└────┬────┘                    └────┬────┘
     │                              │
     │  1. 发送消息                  │
     ├─────────────────────────────>│
     │                              │
     │  2. 乐观UI显示                │
     │  (立即显示在聊天窗口)          │
     │                              │
     │  3. WebSocket 推送            │
     ├─────────────────────────────>│
     │                              │
     │                    4. 保存到数据库
     │                    5. 广播给其他成员
     │                              │
     │  6. 确认回执                  │
     │<─────────────────────────────┤
     │                              │
```

---

## 七、部署架构

### 7.1 开发环境

```
┌─────────────────────────────────────────────────────┐
│                     开发机                           │
│  ┌──────────────────┐      ┌──────────────────┐    │
│  │  ruoyi-im-web    │      │  浏览器          │    │
│  │  :5173 (dev)     │<────►│  :5173           │    │
│  └────────┬─────────┘      └──────────────────┘    │
│           │                                          │
│           │ Vite 代理                                │
│           ▼                                          │
│  ┌──────────────────┐      ┌──────────────────┐    │
│  │  ruoyi-im-api    │      │  MySQL           │    │
│  │  :8080           │<────►│  172.168.20.222  │    │
│  └────────┬─────────┘      └──────────────────┘    │
│           │                                          │
│           ▼                                          │
│  ┌──────────────────┐                               │
│  │  Redis           │                               │
│  │  172.168.20.222  │                               │
│  └──────────────────┘                               │
└─────────────────────────────────────────────────────┘
```

### 7.2 生产环境

```
┌─────────────────────────────────────────────────────┐
│                      Nginx                           │
│         反向代理 /api/* → :8080, / → :80             │
└─────────────────────┬────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
┌──────────────────┐      ┌──────────────────┐
│  ruoyi-im-api    │      │  ruoyi-im-web    │
│   :8080          │◄────►│   :80            │
│  (JAR 包部署)     │      │  (静态文件)       │
└────────┬─────────┘      └──────────────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌────────┐
│ MySQL  │ │ Redis  │
│ :3306  │ │ :6379  │
└────────┘ └────────┘
```

---

## 八、配置说明

### 8.1 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://172.168.20.222:3306/im?useUnicode=true&characterEncoding=utf8mb4
    username: im
    password: 123456
  redis:
    host: 172.168.20.222
    port: 6379
    password: 123456

websocket:
  endpoint: /ws/im
  allowed-origins: "*"
```

### 8.2 前端配置 (vite.config.js)

```javascript
export default {
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
}
```

---

## 九、已知限制与待优化

### 9.1 已知限制
- WebSocket 连接数受服务器配置限制
- 大文件上传需要分片处理
- 群组成员数量建议不超过 500 人

### 9.2 待优化
- 消息搜索功能增强
- 文件预览功能
- 视频会议功能
- 移动端适配

---

**文档结束**
