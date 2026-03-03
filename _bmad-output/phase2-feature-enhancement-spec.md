# 第二阶段 功能增强需求规格

> **版本**: v1.0
> **创建日期**: 2026-03-03
> **项目**: RuoYi-IM 企业即时通讯系统
> **对标产品**: 钉钉、野火IM

---

## 📊 一、功能实现状态总览

| 序号 | 功能 | 后端状态 | 前端状态 | 集成状态 | 完成度 |
|-----|------|---------|---------|---------|-------|
| 1 | 工作台应用管理 | ✅ 已实现 | ✅ 已实现 | ✅ 已集成 | 90% |
| 2 | 回收站功能 | ✅ 已实现 | ✅ 已实现 | ⚠️ 待集成 | 85% |
| 3 | 审批流程设计器 | ✅ 已实现 | ✅ 已实现 | ⚠️ 待集成 | 80% |
| 4 | 日程提醒设置增强 | ✅ 已实现 | ✅ 已实现 | ⚠️ 待集成 | 85% |

**总体完成度**: **85%**

---

## 🔧 二、功能详细规格

### 2.1 工作台应用管理

#### 2.1.1 功能描述
用户可以在工作台管理已安装的应用，包括安装新应用、卸载应用、固定应用到工作台、调整应用顺序等。管理员可以创建、编辑、删除应用，设置应用可见性。

#### 2.1.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-web/src/api/im/application.js` | API接口 | ✅ |
| `ruoyi-im-web/src/views/admin/AppManagePanel.vue` | 管理页面 | ✅ |
| `ruoyi-im-api/.../controller/ImApplicationController.java` | 控制器 | ✅ |
| `ruoyi-im-api/.../service/ImApplicationService.java` | 服务接口 | ✅ |
| `ruoyi-im-api/.../service/ImUserApplicationService.java` | 用户应用服务 | ✅ |
| `ruoyi-im-api/.../domain/ImApplication.java` | 应用实体 | ✅ |
| `ruoyi-im-api/.../domain/ImUserApplication.java` | 用户应用实体 | ✅ |

#### 2.1.3 已实现功能

**用户功能**:
- ✅ 查看已安装应用列表
- ✅ 从应用市场安装新应用
- ✅ 卸载已安装应用
- ✅ 固定/取消固定应用
- ✅ 调整应用顺序
- ✅ 记录应用使用情况

**管理员功能**:
- ✅ 创建新应用
- ✅ 编辑应用信息
- ✅ 删除应用
- ✅ 设置应用可见性
- ✅ 应用分类管理（办公/数据/工具/自定义）
- ✅ 应用类型管理（路由/嵌入/外链）

#### 2.1.4 API 接口

```
# 应用管理
GET    /api/im/applications           - 获取应用列表
GET    /api/im/applications/visible   - 获取可见应用
GET    /api/im/applications/by-category - 按分类获取应用
GET    /api/im/applications/{id}      - 获取应用详情
POST   /api/im/applications           - 创建应用
PUT    /api/im/applications/{id}      - 更新应用
DELETE /api/im/applications/{id}      - 删除应用
PUT    /api/im/applications/{id}/visibility - 设置可见性

# 用户应用
GET    /api/im/user-applications      - 获取用户应用
POST   /api/im/user-applications/install - 安装应用
DELETE /api/im/user-applications/{id} - 卸载应用
PUT    /api/im/user-applications/{id}/pinned - 固定应用
PUT    /api/im/user-applications/sort-order - 更新排序
POST   /api/im/user-applications/{id}/usage - 记录使用
```

#### 2.1.5 数据库表结构

```sql
-- 应用表
CREATE TABLE im_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) COMMENT '应用名称',
    code VARCHAR(50) COMMENT '应用编码',
    icon VARCHAR(500) COMMENT '图标URL',
    category VARCHAR(20) COMMENT '分类: OFFICE/DATA/TOOLS/CUSTOM',
    description VARCHAR(500) COMMENT '描述',
    app_type VARCHAR(20) COMMENT '类型: ROUTE/IFRAME/LINK',
    app_url VARCHAR(500) COMMENT '应用地址',
    is_system TINYINT(1) DEFAULT 0 COMMENT '是否系统应用',
    is_visible TINYINT(1) DEFAULT 1 COMMENT '是否可见',
    sort_order INT DEFAULT 0 COMMENT '排序',
    permissions TEXT COMMENT '所需权限JSON',
    create_time DATETIME,
    update_time DATETIME
);

-- 用户应用表
CREATE TABLE im_user_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID',
    app_id BIGINT COMMENT '应用ID',
    is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否固定',
    sort_order INT DEFAULT 0 COMMENT '排序',
    app_config TEXT COMMENT '应用配置JSON',
    last_used_time DATETIME COMMENT '最后使用时间',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME,
    update_time DATETIME
);
```

---

### 2.2 回收站功能

#### 2.2.1 功能描述
用户删除的消息和文件会先进入回收站，支持在30天内恢复或永久删除。回收站提供搜索、批量操作、清空等功能。

#### 2.2.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-web/src/api/im/recycleBin.js` | API接口 | ✅ |
| `ruoyi-im-web/src/views/admin/RecycleBinPanel.vue` | 管理页面 | ✅ |
| `ruoyi-im-api/.../controller/ImRecycleBinController.java` | 控制器 | ✅ |
| `ruoyi-im-api/.../service/ImRecycleBinService.java` | 服务接口 | ✅ |
| `ruoyi-im-api/.../service/impl/ImRecycleBinServiceImpl.java` | 服务实现 | ✅ |
| `ruoyi-im-api/.../dto/recycle/RecycleBinQueryRequest.java` | 查询DTO | ✅ |

#### 2.2.3 已实现功能

**消息回收站**:
- ✅ 查看已删除消息列表
- ✅ 恢复单条消息
- ✅ 批量恢复消息
- ✅ 永久删除消息
- ✅ 清空消息回收站

**文件回收站**:
- ✅ 查看已删除文件列表
- ✅ 恢复单个文件
- ✅ 批量恢复文件
- ✅ 永久删除文件
- ✅ 清空文件回收站

**统计功能**:
- ✅ 回收站项目统计
- ✅ 过期时间显示（30天后永久删除）

#### 2.2.4 API 接口

```
# 消息回收站
GET    /api/im/recycle-bin/messages        - 获取消息列表
PUT    /api/im/recycle-bin/messages/{id}/restore - 恢复消息
PUT    /api/im/recycle-bin/messages/batch-restore - 批量恢复
DELETE /api/im/recycle-bin/messages/{id}   - 永久删除

# 文件回收站
GET    /api/im/recycle-bin/files           - 获取文件列表
PUT    /api/im/recycle-bin/files/{id}/restore - 恢复文件
PUT    /api/im/recycle-bin/files/batch-restore - 批量恢复
DELETE /api/im/recycle-bin/files/{id}      - 永久删除

# 通用
DELETE /api/im/recycle-bin/empty           - 清空回收站
GET    /api/im/recycle-bin/stats           - 获取统计
```

#### 2.2.5 业务逻辑

```
删除流程:
1. 用户删除消息/文件
2. 设置 is_deleted = 1
3. 记录删除时间

恢复流程:
1. 用户点击恢复
2. 设置 is_deleted = 0
3. 消息/文件回到原位置

永久删除:
1. 用户确认永久删除
2. 物理删除数据库记录
3. 文件存储资源释放

自动清理:
1. 定时任务每日扫描
2. 删除超过30天的回收站项目
```

---

### 2.3 审批流程设计器增强

#### 2.3.1 功能描述
提供可视化的审批流程设计器，支持拖拽式流程编排，包括审批节点、条件分支、通知节点等，支持多级审批、会签/或签、超时处理等高级功能。

#### 2.3.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-web/src/components/Approval/FlowDesigner.vue` | 设计器组件 | ✅ |
| `ruoyi-im-web/src/views/ApprovalPanel.vue` | 审批面板 | ✅ |
| `ruoyi-im-api/.../dto/approval/FlowConfigRequest.java` | 流程配置DTO | ✅ |
| `ruoyi-im-api/.../dto/approval/FlowNodeConfig.java` | 节点配置DTO | ✅ |
| `ruoyi-im-api/.../dto/approval/ConditionBranch.java` | 条件分支DTO | ✅ |
| `ruoyi-im-api/.../dto/approval/ApproverConfig.java` | 审批人配置DTO | ✅ |

#### 2.3.3 已实现功能

**流程设计**:
- ✅ 可视化流程画布
- ✅ 拖拽添加节点
- ✅ 节点类型：审批/条件/通知/抄送
- ✅ 节点连接器
- ✅ 节点属性编辑面板

**审批节点**:
- ✅ 审批人选择
- ✅ 审批方式（会签/或签/依次审批）
- ✅ 超时设置
- ✅ 超时处理（自动通过/自动拒绝/转交上级）

**条件分支**:
- ✅ 条件表达式设置
- ✅ 多分支支持
- ✅ 条件类型（等于/大于/小于/包含）

**通知节点**:
- ✅ 通知人选择
- ✅ 通知方式（站内信/短信/邮件）

#### 2.3.4 节点类型说明

| 节点类型 | 图标 | 颜色 | 功能说明 |
|---------|------|------|---------|
| 审批节点 | approval | 蓝色 | 指定审批人进行审批 |
| 条件分支 | call_split | 橙色 | 根据条件分流到不同分支 |
| 通知节点 | notifications | 绿色 | 发送通知给指定人员 |
| 抄送节点 | mail | 紫色 | 抄送消息给相关人员 |

#### 2.3.5 审批方式说明

| 方式 | 说明 | 通过条件 |
|-----|------|---------|
| 会签(AND) | 所有审批人都需要同意 | 全部同意 |
| 或签(OR) | 任一审批人同意即可 | 任一同意 |
| 依次审批(SEQUENTIAL) | 按顺序逐一审批 | 按序全部同意 |

---

### 2.4 日程提醒设置增强

#### 2.4.1 功能描述
增强日程提醒功能，支持多提醒时间设置、多种提醒方式、重复提醒、默认提醒设置等。

#### 2.4.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-web/src/components/Schedule/ReminderSettings.vue` | 设置组件 | ✅ |
| `ruoyi-im-web/src/api/im/schedule.js` | API接口（增强） | ✅ |
| `ruoyi-im-api/.../controller/ImScheduleReminderController.java` | 控制器 | ✅ |
| `ruoyi-im-api/.../service/ImScheduleReminderService.java` | 服务接口 | ✅ |
| `ruoyi-im-api/.../service/impl/ImScheduleReminderServiceImpl.java` | 服务实现 | ✅ |
| `ruoyi-im-api/.../dto/schedule/ReminderSettingsRequest.java` | 设置请求DTO | ✅ |
| `ruoyi-im-api/.../dto/schedule/ReminderAddRequest.java` | 添加请求DTO | ✅ |

#### 2.4.3 已实现功能

**提醒设置**:
- ✅ 多个提醒时间设置
- ✅ 时间单位支持（分钟/小时/天）
- ✅ 提醒方式选择（站内信/短信/邮件/语音电话）
- ✅ 重复提醒设置
- ✅ 提醒开关控制

**快捷设置**:
- ✅ 15分钟前
- ✅ 30分钟前
- ✅ 1小时前
- ✅ 1天前

**默认设置**:
- ✅ 用户默认提醒配置
- ✅ 设为默认选项

#### 2.4.4 API 接口

```
# 日程提醒
GET    /api/im/schedule-events/{id}/reminders - 获取提醒设置
POST   /api/im/schedule-events/{id}/reminders - 设置提醒
POST   /api/im/schedule-events/{id}/reminders/add - 添加提醒
DELETE /api/im/schedule-events/{id}/reminders/{rid} - 删除提醒
PUT    /api/im/schedule-events/{id}/reminders/toggle - 开关提醒

# 默认设置
GET    /api/im/schedule-events/default-reminders - 获取默认设置
POST   /api/im/schedule-events/default-reminders - 设置默认配置
```

#### 2.4.5 提醒方式说明

| 方式 | 适用场景 | 说明 |
|-----|---------|------|
| 站内信 | 默认方式 | IM系统内推送通知 |
| 短信 | 重要日程 | 发送短信到手机 |
| 邮件 | 重要日程 | 发送邮件提醒 |
| 语音电话 | 紧急日程 | 自动语音电话提醒 |

---

## 📋 三、路由配置

需要在前端路由中添加以下页面：

```javascript
// 管理后台路由
{
  path: '/admin',
  component: AdminLayout,
  children: [
    { path: 'apps', component: AppManagePanel, meta: { title: '应用管理', roles: ['ADMIN'] } },
    { path: 'recycle-bin', component: RecycleBinPanel, meta: { title: '回收站' } },
    { path: 'security-logs', component: SecurityLogPanel, meta: { title: '安全日志', roles: ['ADMIN'] } },
    { path: 'broadcast', component: BroadcastPanel, meta: { title: '消息群发', roles: ['ADMIN'] } }
  ]
}

// 审批流程设计器路由
{
  path: '/approval/designer/:templateId?',
  component: FlowDesigner,
  meta: { title: '流程设计器', roles: ['ADMIN'] }
}
```

---

## 🎯 四、验收标准

### 4.1 工作台应用管理

- [ ] 用户可查看已安装应用
- [ ] 用户可安装/卸载应用
- [ ] 用户可固定/取消固定应用
- [ ] 管理员可创建/编辑/删除应用
- [ ] 应用点击可正常打开

### 4.2 回收站功能

- [ ] 删除的消息进入回收站
- [ ] 可恢复已删除消息
- [ ] 可永久删除消息
- [ ] 30天后自动清理
- [ ] 文件回收站正常工作

### 4.3 审批流程设计器

- [ ] 可视化拖拽设计流程
- [ ] 节点属性可配置
- [ ] 条件分支正常工作
- [ ] 流程可保存和预览
- [ ] 审批按配置流程执行

### 4.4 日程提醒设置

- [ ] 可设置多个提醒时间
- [ ] 提醒按时触发
- [ ] 默认设置正常保存
- [ ] 快捷设置一键配置

---

## 📊 五、后续优化建议

### 5.1 工作台应用管理

| 优化项 | 优先级 | 说明 |
|-------|-------|------|
| 应用权限控制 | P1 | 基于角色的应用访问控制 |
| 应用使用统计 | P2 | 统计应用使用频率和时长 |
| 应用推荐 | P2 | 根据用户角色推荐应用 |

### 5.2 回收站功能

| 优化项 | 优先级 | 说明 |
|-------|-------|------|
| 回收站搜索 | P1 | 支持关键词搜索回收站内容 |
| 回收站容量限制 | P2 | 限制回收站容量，自动清理 |
| 回收站提醒 | P2 | 即将过期项目提醒 |

### 5.3 审批流程设计器

| 优化项 | 优先级 | 说明 |
|-------|-------|------|
| 流程模板库 | P1 | 预置常用审批流程模板 |
| 流程版本管理 | P1 | 流程修改历史和回滚 |
| 流程测试 | P2 | 模拟测试流程执行 |

### 5.4 日程提醒设置

| 优化项 | 优先级 | 说明 |
|-------|-------|------|
| 智能提醒 | P1 | 根据用户习惯智能调整提醒时间 |
| 日程冲突检测 | P1 | 检测并提示日程冲突 |
| 日程共享 | P2 | 支持日程共享给他人 |

---

## 📝 六、附录

### 6.1 新增文件汇总

**前端文件（8个）**:
```
src/api/im/application.js           - 应用管理API
src/api/im/recycleBin.js            - 回收站API
src/views/admin/AppManagePanel.vue  - 应用管理页面
src/views/admin/RecycleBinPanel.vue - 回收站页面
src/components/Approval/FlowDesigner.vue - 流程设计器
src/components/Schedule/ReminderSettings.vue - 提醒设置
```

**后端文件（10个）**:
```
controller/ImApplicationController.java    - 应用控制器
controller/ImRecycleBinController.java     - 回收站控制器
controller/ImScheduleReminderController.java - 提醒控制器
service/ImRecycleBinService.java           - 回收站服务
service/ImScheduleReminderService.java     - 提醒服务
service/impl/ImRecycleBinServiceImpl.java  - 回收站实现
service/impl/ImScheduleReminderServiceImpl.java - 提醒实现
dto/recycle/RecycleBinQueryRequest.java    - 回收站查询DTO
dto/schedule/ReminderSettingsRequest.java  - 提醒设置DTO
dto/schedule/ReminderAddRequest.java       - 添加提醒DTO
```

### 6.2 修改文件汇总

```
src/api/im/schedule.js               - 新增提醒API
src/router/index.js                  - 新增路由
```

---

*文档版本: v1.0*
*创建日期: 2026-03-03*
*作者: AI Developer*