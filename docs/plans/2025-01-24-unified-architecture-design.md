# 统一架构设计方案

## 概述

移除 ruoyi-im-admin（若依后台管理系统），将管理功能集成到 ruoyi-im-api 中，前端复用 ruoyi-im-web，根据用户权限动态展示用户端或管理端界面。

**设计日期**: 2025-01-24
**状态**: 已确认

---

## 一、整体架构

### 1.1 项目结构

```
im/
├── ruoyi-im-api/          # 核心 API 服务 (端口 8080)
│   ├── 用户接口 /api/im/*       # 普通用户功能
│   └── 管理接口 /api/admin/*    # 管理员功能
│
├── ruoyi-im-web/          # 前端界面 (端口 5173)
│   ├── /views/user/            # 用户聊天界面
│   └── /views/admin/           # 管理后台界面
│
└── sql/                    # 数据库脚本
```

### 1.2 核心原则

| 原则 | 说明 |
|------|------|
| 单一后端 | 简化部署，仅需一个 ruoyi-im-api 服务 |
| 统一前端 | ruoyi-im-web 根据角色权限动态渲染 |
| 路径隔离 | `/api/im/*` 面向用户，`/api/admin/*` 面向管理员 |
| 注解权限 | 使用 `@PreAuthorize` 细粒度控制 |

---

## 二、权限与安全设计

### 2.1 Spring Security 配置

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
            // 用户接口 - 需要 USER 角色
            .antMatchers("/api/im/**").hasAnyRole("USER", "ADMIN")
            // 管理接口 - 仅允许 ADMIN
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            // WebSocket - 认证用户即可
            .antMatchers("/ws/im").authenticated()
            // 登录/注册 - 公开
            .antMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
        // ... JWT 配置
}
```

### 2.2 角色定义

| 角色 | 代码 | 权限范围 |
|------|------|----------|
| 普通用户 | `ROLE_USER` | 聊天、联系人、群组、个人设置 |
| 管理员 | `ROLE_ADMIN` | 所有用户权限 + 用户管理、群组管理、数据统计、系统配置 |
| 超级管理员 | `ROLE_SUPER_ADMIN` | 所有权限 + 敏感操作（数据备份、敏感词管理） |

### 2.3 数据库扩展

在 `im_user` 表中增加 `role` 字段：

```sql
ALTER TABLE im_user ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '用户角色: USER/ADMIN/SUPER_ADMIN';
```

---

## 三、管理接口设计

### 3.1 API 路径规划

```
/api/admin/
├── 用户管理
│   ├── GET    /users           # 用户列表（分页、搜索）
│   ├── GET    /users/{id}      # 用户详情
│   ├── PUT    /users/{id}      # 修改用户
│   ├── PUT    /users/{id}/status   # 启用/禁用
│   └── DELETE /users/{id}      # 删除用户
│
├── 群组管理
│   ├── GET    /groups          # 群组列表
│   ├── GET    /groups/{id}     # 群组详情
│   ├── DELETE /groups/{id}     # 解散群组
│   └── PUT    /groups/{id}/mute   # 禁言群组
│
├── 消息管理
│   ├── GET    /messages        # 消息记录查询
│   ├── DELETE /messages/{id}   # 删除消息
│   └── GET    /messages/stats  # 消息统计
│
├── 数据统计
│   ├── GET    /stats/overview  # 概览数据（在线人数、今日消息等）
│   ├── GET    /stats/users     # 用户活跃度统计
│   └── GET    /stats/groups    # 群组活跃度统计
│
└── 系统配置
    ├── GET    /config          # 系统配置
    ├── PUT    /config          # 更新配置
    └── POST   /backup          # 数据备份
```

### 3.2 Controller 结构

按功能拆分为独立控制器：

```
com.ruoyi.im.controller.admin/
├── ImUserAdminController      # 用户管理
├── ImGroupAdminController     # 群组管理
├── ImMessageAdminController   # 消息管理
├── ImStatsController          # 数据统计
└── ImSystemAdminController    # 系统配置
```

每个方法添加 `@PreAuthorize("hasRole('ADMIN')")` 注解。

---

## 四、前端管理界面设计

### 4.1 目录结构

```
ruoyi-im-web/src/
├── views/
│   ├── user/                    # 用户端界面（保留现有）
│   │   ├── chat/
│   │   ├── contacts/
│   │   └── workbench/
│   │
│   └── admin/                   # 管理端界面（新增）
│       ├── Dashboard.vue        # 数据概览
│       ├── UserManagement.vue   # 用户管理
│       ├── GroupManagement.vue  # 群组管理
│       ├── MessageManagement.vue # 消息管理
│       └── SystemConfig.vue     # 系统配置
│
├── router/
│   └── index.js                 # 路由配置（增加权限守卫）
│
└── api/
    └── admin.js                 # 管理接口封装
```

### 4.2 路由权限守卫

```javascript
{
  path: '/admin',
  component: AdminLayout,
  meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] },
  children: [
    { path: 'dashboard', component: Dashboard },
    { path: 'users', component: UserManagement },
    { path: 'groups', component: GroupManagement },
    { path: 'messages', component: MessageManagement },
    { path: 'config', component: SystemConfig }
  ]
}
```

### 4.3 界面风格

- 复用现有的 Element Plus 组件
- 与聊天界面保持一致的视觉风格
- 侧边栏导航 + 顶部栏布局

---

## 五、迁移步骤

### 5.1 后端改造

1. **删除 ruoyi-im-admin**
   ```bash
   git rm -r ruoyi-im-admin
   ```

2. **数据库变更**
   - 删除若依相关的表：`sys_*` 系列表
   - 保留 IM 业务表：`im_*` 表
   - 在 `im_user` 表添加 `role` 字段

3. **ruoyi-im-api 改造**
   - 新增 `controller.admin` 包及相关控制器
   - 配置 Spring Security 角色权限
   - 新增 `/api/admin/**` 路由

### 5.2 前端改造

1. **ruoyi-im-web 改造**
   - 新增 `/views/admin/` 目录及管理页面
   - 配置路由权限守卫
   - 新增 `api/admin.js` 接口封装

2. **导航调整**
   - 根据用户角色显示不同导航菜单
   - 管理员登录后可切换用户端/管理端视图

---

## 六、文档更新

需要更新的文档：

| 文档 | 更新内容 |
|------|----------|
| CLAUDE.md | 移除 ruoyi-im-admin 相关描述，更新架构说明 |
| README.md | 更新项目架构、部署流程 |
| 部署文档 | 简化为两个服务的部署流程 |

---

## 七、部署架构（变更后）

```
┌─────────────────────────────────────────────────────────┐
│                    Nginx (可选)                          │
│         反向代理 /api/* → :8080, / → :5173              │
└─────────────────────┬───────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
┌──────────────────┐      ┌──────────────────┐
│  ruoyi-im-api    │      │  ruoyi-im-web    │
│   :8080          │◄────►│   :5173          │
│  (用户+管理API)   │      │ (用户+管理界面)   │
└──────────────────┘      └──────────────────┘
        │
        ▼
┌──────────────────┐      ┌──────────────────┐
│     MySQL        │      │     Redis        │
│   :3306/im       │      │   :6379          │
└──────────────────┘      └──────────────────┘
```

---

## 八、优势总结

| 方面 | 之前 | 现在 |
|------|------|------|
| 服务数量 | 3个（api/admin/web） | 2个（api/web） |
| 部署复杂度 | 中等 | 低 |
| 技术栈 | Java双框架 | 统一Spring Boot |
| 前端项目 | 1个 | 1个（复用） |
| 权限控制 | 两套系统 | 统一JWT + 角色 |
| 维护成本 | 较高 | 较低 |
