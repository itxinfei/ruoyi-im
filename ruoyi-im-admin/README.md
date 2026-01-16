# RuoYi IM 系统

基于若依(RuoYi)框架开发的即时通讯(IM)管理系统，提供完整的即时通讯功能和后台管理。

## 项目概述

RuoYi IM 是一个基于若依(RuoYi)快速开发框架构建的即时通讯管理系统。项目采用前后端分离架构，后端使用Spring Boot + MyBatis，前端使用Vue.js，提供完整的即时通讯功能和后台管理界面。

## 功能特性

### 核心功能
- **用户管理** - 完整的用户体系管理
- **会话管理** - 私聊和群聊会话管理
- **群组管理** - 群组创建、成员管理
- **消息管理** - 消息发送、接收、存储
- **好友管理** - 好友添加、关系管理
- **公告管理** - 群公告发布管理
- **文件管理** - 文件上传、下载、分享
- **审批管理** - 工作流审批系统
- **DING消息** - 重要消息提醒
- **邮箱管理** - 内置邮箱系统
- **应用管理** - 第三方应用集成
- **Session管理** - 会话状态管理

### 系统功能
- **审计日志** - 操作日志记录与审计
- **部门管理** - 组织架构管理
- **文件分享** - 文件共享管理
- **好友请求** - 好友申请管理
- **成员管理** - 群组成员管理
- **日程事件** - 日程安排管理
- **敏感词管理** - 内容过滤管理
- **系统配置** - 系统参数设置
- **待办事项** - 个人任务管理
- **用户设备** - 设备管理
- **用户设置** - 个人偏好设置
- **工作报表** - 工作统计报表

## 技术栈

### 后端技术
- Spring Boot
- Spring Security
- MyBatis
- MyBatis-Plus
- MySQL
- Redis
- Maven

### 前端技术
- Vue.js
- Element UI
- Axios
- Webpack

### 开发工具
- JDK 8+
- Maven
- MySQL 5.7+
- Redis

## 目录结构

```
ruoyi-im-admin/
├── ruoyi-admin/          # 系统管理模块
├── ruoyi-common/         # 通用模块
├── ruoyi-framework/      # 框架模块
├── ruoyi-generator/      # 代码生成模块
├── ruoyi-system/         # 系统模块
└── src/
    └── main/
        ├── java/
        │   └── com.ruoyi/
        │       └── web/
        │           ├── controller/    # 控制器
        │           ├── domain/        # 实体类
        │           ├── mapper/        # 数据访问层
        │           ├── service/       # 业务逻辑层
        │           └── utils/         # 工具类
        └── resources/
            ├── mapper/               # MyBatis映射文件
            ├── static/               # 静态资源
            ├── templates/            # Thymeleaf模板
            └── application.yml       # 配置文件
```

## 部署说明

### 环境要求
- JDK 8+
- Maven 3.6+
- MySQL 5.7+
- Redis

### 部署步骤

1. **数据库初始化**
   ```bash
   # 导入数据库脚本
   im_email.sql
   ```

2. **修改配置**
   ```bash
   # 修改 application-druid.yml 配置数据库连接
   # 修改 application.yml 配置Redis连接
   ```

3. **编译打包**
   ```bash
   mvn clean package -DskipTests
   ```

4. **启动服务**
   ```bash
   java -jar ruoyi-admin.jar
   ```

## 登录信息

- **访问地址**: http://localhost:8080
- **用户名**: admin
- **密码**: 123456

## 开发规范

- 代码规范：遵循阿里巴巴Java开发手册
- 注释规范：关键方法和类需添加JavaDoc注释
- 提交规范：遵循Git提交信息规范

## 许可证

本项目基于 Apache License 2.0 许可证。

## 联系方式

- 项目地址：[项目链接]
- 问题反馈：[联系方式]