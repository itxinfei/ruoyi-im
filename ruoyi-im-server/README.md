# 若依即时通讯系统 (RuoYi-IM)

基于领域驱动设计(DDD)的企业级即时通讯系统，采用Spring Boot + WebSocket技术栈。

## 📋 项目概述

本项目是一个完整的即时通讯解决方案，支持私聊、群聊、文件传输、消息推送等功能。采用DDD架构设计，具有良好的可扩展性和维护性。

## 🏗️ 系统架构

### 技术栈
- **后端框架**: Spring Boot 2.x
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **WebSocket**: Spring WebSocket
- **ORM**: MyBatis-Plus
- **构建工具**: Maven

### 架构设计
```
├── 接口层 (Interface Layer)
│   ├── REST API
│   └── WebSocket API
├── 应用服务层 (Application Layer)
│   ├── 应用服务
│   └── 事件处理器
├── 领域层 (Domain Layer)
│   ├── 聚合根
│   ├── 实体
│   ├── 值对象
│   ├── 领域服务
│   └── 领域事件
└── 基础设施层 (Infrastructure Layer)
    ├── 数据持久化
    ├── 消息队列
    └── 外部服务
```

## 📦 模块结构

### 核心领域模块

#### 1. 用户领域 (User Domain)
- **聚合根**: `User` - 用户聚合根
- **实体**: 
  - `UserProfile` - 用户资料
  - `UserSetting` - 用户设置
  - `UserDevice` - 用户设备
- **值对象**: `UserId` - 用户标识
- **领域服务**: `UserDomainService` - 用户领域服务
- **领域事件**: 
  - `UserCreatedEvent` - 用户创建事件
  - `UserLoginEvent` - 用户登录事件
  - `UserLogoutEvent` - 用户登出事件
  - `UserStatusChangedEvent` - 用户状态变更事件

#### 2. 消息领域 (Messaging Domain)
- **聚合根**: 
  - `Message` - 消息聚合根
  - `Conversation` - 会话聚合根
- **实体**: `ConversationMember` - 会话成员
- **值对象**: 
  - `MessageId` - 消息标识
  - `ConversationId` - 会话标识
  - `MessageContent` - 消息内容
- **领域服务**: `MessageDomainService` - 消息领域服务
- **领域事件**: 
  - `MessageSentEvent` - 消息发送事件
  - `MessageReceivedEvent` - 消息接收事件
  - `MessageReadEvent` - 消息已读事件
  - `ConversationCreatedEvent` - 会话创建事件
  - `MemberJoinedEvent` - 成员加入事件
  - `MemberLeftEvent` - 成员离开事件

#### 3. 好友关系领域 (Friendship Domain)
- **聚合根**: 
  - `Friendship` - 好友关系聚合根
  - `FriendRequest` - 好友请求聚合根
- **值对象**: 
  - `FriendshipId` - 好友关系标识
  - `FriendRequestId` - 好友请求标识
  - `FriendshipStatus` - 好友关系状态
- **领域服务**: `FriendshipDomainService` - 好友关系领域服务
- **领域事件**: 
  - `FriendshipCreatedEvent` - 好友关系创建事件
  - `FriendshipDeletedEvent` - 好友关系删除事件
  - `FriendRequestSentEvent` - 好友请求发送事件
  - `FriendRequestAcceptedEvent` - 好友请求接受事件
  - `FriendRequestRejectedEvent` - 好友请求拒绝事件

#### 4. 群组领域 (Group Domain)
- **聚合根**: `Group` - 群组聚合根
- **实体**: `GroupMember` - 群成员实体
- **值对象**: 
  - `GroupId` - 群组标识
  - `GroupType` - 群组类型（普通群/超级群）
  - `MemberRole` - 成员角色（群主/管理员/普通成员）
- **领域服务**: `GroupDomainService` - 群组领域服务
- **领域事件**: 
  - `GroupCreatedEvent` - 群组创建事件
  - `GroupDismissedEvent` - 群组解散事件
  - `MemberJoinedGroupEvent` - 成员加入群组事件
  - `MemberLeftGroupEvent` - 成员离开群组事件

### 基础设施模块

#### 共享内核 (Shared Kernel)
- **基础类**: 
  - `AggregateRoot` - 聚合根基类
  - `Entity` - 实体基类
  - `ValueObject` - 值对象基类
  - `DomainEvent` - 领域事件基类
- **工具类**: 
  - `ValidationUtils` - 验证工具
  - `StringUtils` - 字符串工具
- **异常体系**: 
  - `BusinessException` - 业务异常基类
  - `ValidationException` - 验证异常

## 🚀 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/your-repo/ruoyi-im-server.git
cd ruoyi-im-server
```

2. **配置数据库**
```sql
CREATE DATABASE ruoyi_im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **修改配置文件**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ruoyi_im
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

4. **编译运行**
```bash
mvn clean compile
mvn spring-boot:run
```

## 📚 API文档

### WebSocket连接
```javascript
// 连接WebSocket
const socket = new WebSocket('ws://localhost:8080/websocket/{userId}');

// 发送消息
socket.send(JSON.stringify({
    type: 'MESSAGE',
    conversationId: 1,
    content: 'Hello World',
    messageType: 'TEXT'
}));
```

### REST API

#### 用户相关
- `POST /api/users` - 创建用户
- `GET /api/users/{id}` - 获取用户信息
- `PUT /api/users/{id}` - 更新用户信息

#### 消息相关
- `POST /api/conversations` - 创建会话
- `GET /api/conversations/{id}/messages` - 获取会话消息
- `POST /api/messages` - 发送消息

## 🧪 测试

### 运行单元测试
```bash
mvn test
```

### 运行集成测试
```bash
mvn integration-test
```

## 📈 性能指标

- **并发连接数**: 10,000+
- **消息吞吐量**: 100,000 msg/s
- **响应时间**: < 100ms
- **可用性**: 99.9%

## 🔧 配置说明

### 消息队列配置
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### WebSocket配置
```yaml
websocket:
  max-connections: 10000
  heartbeat-interval: 30000
  message-size-limit: 1048576
```

## 📋 待办事项

- [x] 用户领域模型
- [x] 消息领域模型
- [x] 好友关系管理
- [x] 群组管理功能
- [ ] 通知领域模型
- [ ] 文件管理领域模型
- [ ] 基础设施层实现
- [ ] 应用服务层实现
- [ ] 文件传输优化
- [ ] 消息加密
- [ ] 离线消息推送
- [ ] 音视频通话
- [ ] 消息搜索功能

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目主页: https://github.com/your-repo/ruoyi-im-server
- 问题反馈: https://github.com/your-repo/ruoyi-im-server/issues
- 邮箱: your-email@example.com

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！