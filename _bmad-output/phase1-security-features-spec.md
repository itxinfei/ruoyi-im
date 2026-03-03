# 第一阶段（MVP+）核心安全功能需求规格

> **版本**: v1.0
> **创建日期**: 2026-03-03
> **项目**: RuoYi-IM 企业即时通讯系统
> **对标产品**: 钉钉、野火IM

---

## 📊 一、功能实现状态总览

| 序号 | 功能 | 后端状态 | 前端状态 | 集成状态 | 完成度 |
|-----|------|---------|---------|---------|-------|
| 1 | 屏幕水印系统 | ✅ 已实现 | ✅ 已实现 | ✅ 已集成 | 95% |
| 2 | 截图/录屏检测 | ✅ 已实现 | ✅ 已实现 | ✅ 已集成 | 90% |
| 3 | 消息防转发管控 | ⚠️ 部分实现 | ⚠️ 部分实现 | ❌ 未集成 | 40% |
| 4 | 消息群发 | ✅ 已实现 | ❌ 未实现 | ❌ 未集成 | 50% |
| 5 | 审批催办 | ⚠️ 部分实现 | ❌ 未实现 | ❌ 未集成 | 30% |
| 6 | 会议共享屏幕 | ✅ 已实现 | ✅ 已实现 | ✅ 已集成 | 90% |

**总体完成度**: **65%**

---

## 🔒 二、功能详细规格

### 2.1 屏幕水印系统

#### 2.1.1 功能描述
在用户屏幕上显示半透明水印，内容包含用户名、时间、工号等信息，即使截图/拍照泄露也能追溯到责任人。

#### 2.1.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-web/src/components/Security/WatermarkOverlay.vue` | 前端组件 | ✅ |
| `ruoyi-im-web/src/components/Settings/WatermarkSettings.vue` | 设置组件 | ✅ |
| `ruoyi-im-web/src/api/im/watermark.js` | API接口 | ✅ |
| `ruoyi-im-api/.../controller/ImWatermarkController.java` | 后端控制器 | ✅ |
| `ruoyi-im-api/.../vo/system/WatermarkConfigVO.java` | 配置VO | ✅ |

#### 2.1.3 已实现功能

- ✅ 水印内容自定义模板（支持 ${username}, ${datetime}, ${employeeId} 变量）
- ✅ 字体大小配置（10-24px）
- ✅ 透明度配置（5%-30%）
- ✅ 旋转角度配置（-90° ~ 90°）
- ✅ 间距配置（横向、纵向）
- ✅ 颜色配置
- ✅ 防删除检测（MutationObserver）
- ✅ 定时刷新时间显示

#### 2.1.4 待增强功能

| 功能项 | 优先级 | 说明 |
|-------|-------|------|
| 水印防篡改增强 | P0 | 检测并恢复被开发者工具修改的水印样式 |
| 水印内容扩展 | P1 | 支持显示 IP 地址、部门名称等 |
| 分场景水印 | P2 | 不同会话/页面显示不同水印策略 |
| 管理后台界面 | P1 | 完善管理后台水印配置界面 |

#### 2.1.5 API 接口

```
GET  /api/im/watermark/settings  - 获取水印配置
GET  /api/im/watermark/enabled   - 获取水印开关状态
PUT  /api/im/watermark/config    - 更新水印配置（管理后台）
```

---

### 2.2 截图/录屏检测

#### 2.2.1 功能描述
检测用户的截图、录屏行为，记录审计日志，支持拦截特定快捷键。

#### 2.2.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-web/src/components/Security/ScreenshotDetector.vue` | 前端组件 | ✅ |
| `ruoyi-im-web/src/utils/screenshot.js` | 工具函数 | ✅ |
| `ruoyi-im-api/.../domain/ImSecurityLog.java` | 日志实体 | ✅ |
| `ruoyi-im-api/.../service/ImSecurityLogService.java` | 日志服务 | ✅ |
| `ruoyi-im-api/.../controller/ImSecurityLogController.java` | 日志控制器 | ✅ |

#### 2.2.3 已实现功能

- ✅ 快捷键拦截（PrintScreen, Ctrl+P, Ctrl+Shift+S 等）
- ✅ DevTools 快捷键拦截（F12, Ctrl+Shift+I 等）
- ✅ 剪贴板变化监控
- ✅ DevTools 打开检测（窗口尺寸差异法）
- ✅ 安全事件日志上报

#### 2.2.4 待增强功能

| 功能项 | 优先级 | 说明 |
|-------|-------|------|
| 录屏检测增强 | P0 | 使用 Screen Capture API 检测屏幕共享 |
| 检测日志管理后台 | P0 | 完善安全日志查询、统计界面 |
| 告警通知 | P1 | 敏感操作触发时通知管理员 |
| 检测规则配置 | P2 | 支持自定义检测规则和响应动作 |

#### 2.2.5 安全日志类型

| 事件类型 | 说明 | 响应动作 |
|---------|------|---------|
| SCREENSHOT_ATTEMPT | 截图尝试 | BLOCKED/RECORDED |
| DEVTOOLS_ATTEMPT | DevTools打开尝试 | BLOCKED/RECORDED |
| DEVTOOLS_OPEN | DevTools已打开 | DETECTED |
| CLIPBOARD_CHANGE | 剪贴板内容变化 | RECORDED |

---

### 2.3 消息防转发管控

#### 2.3.1 功能描述
控制消息的转发权限，支持禁止转发、转发审批、转发次数限制等功能。

#### 2.3.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-api/.../domain/ImForwardRule.java` | 规则实体 | ✅ |
| `ruoyi-im-api/.../domain/ImForwardAuditLog.java` | 审计日志 | ✅ |
| `ruoyi-im-api/.../service/ImForwardRuleService.java` | 规则服务 | ⚠️ 部分实现 |
| `ruoyi-im-web/src/components/Chat/ForwardDialog.vue` | 转发对话框 | ⚠️ 部分实现 |

#### 2.3.3 已实现功能

- ✅ 转发规则数据模型（ImForwardRule）
- ✅ 转发审计日志数据模型（ImForwardAuditLog）
- ✅ 规则类型支持（SESSION/DEPARTMENT/GLOBAL）
- ✅ 转发次数限制字段

#### 2.3.4 待实现功能

| 功能项 | 优先级 | 说明 |
|-------|-------|------|
| 转发规则管理 API | P0 | 完整的 CRUD 接口 |
| 转发拦截中间件 | P0 | 消息发送前检查转发规则 |
| 转发审批流程 | P1 | 转发敏感消息需要审批 |
| 转发水印 | P1 | 转发内容自动添加来源水印 |
| 前端转发控制 | P0 | 根据规则禁用/启用转发按钮 |
| 管理后台界面 | P1 | 转发规则配置界面 |

#### 2.3.5 数据库表结构

```sql
-- 转发规则表
CREATE TABLE im_forward_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) COMMENT '规则名称',
    rule_type VARCHAR(20) COMMENT '规则类型: SESSION/DEPARTMENT/GLOBAL',
    target_id VARCHAR(50) COMMENT '目标ID',
    allow_forward TINYINT(1) DEFAULT 1 COMMENT '是否允许转发',
    require_approval TINYINT(1) DEFAULT 0 COMMENT '是否需要审批',
    forward_limit INT DEFAULT 0 COMMENT '转发次数限制, 0表示不限',
    status VARCHAR(1) DEFAULT '0' COMMENT '状态: 0正常 1禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME COMMENT '更新时间'
);

-- 转发审计日志表
CREATE TABLE im_forward_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT COMMENT '原消息ID',
    from_conversation_id VARCHAR(50) COMMENT '原会话ID',
    to_conversation_ids TEXT COMMENT '目标会话ID列表',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    operation_time DATETIME COMMENT '操作时间',
    result VARCHAR(20) COMMENT '结果: SUCCESS/DENIED/PENDING',
    remark VARCHAR(500) COMMENT '备注'
);
```

---

### 2.4 消息群发

#### 2.4.1 功能描述
管理员可向全员、指定部门、指定用户群发消息，支持定时发送、发送记录查看。

#### 2.4.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-api/.../domain/ImBroadcastTask.java` | 任务实体 | ✅ |
| `ruoyi-im-api/.../domain/ImBroadcastRecord.java` | 发送记录 | ✅ |
| `ruoyi-im-api/.../service/ImBroadcastService.java` | 服务接口 | ✅ |
| `ruoyi-im-api/.../service/impl/ImBroadcastServiceImpl.java` | 服务实现 | ✅ |
| `ruoyi-im-api/.../controller/ImBroadcastController.java` | 控制器 | ✅ |
| `ruoyi-im-api/.../schedule/BroadcastScheduledTask.java` | 定时任务 | ✅ |

#### 2.4.3 已实现功能（后端）

- ✅ 群发任务创建
- ✅ 目标类型支持（全员/部门/指定用户）
- ✅ 定时发送
- ✅ 发送记录跟踪
- ✅ 任务状态管理（待发送/发送中/已完成/已取消）

#### 2.4.4 待实现功能

| 功能项 | 优先级 | 说明 |
|-------|-------|------|
| 前端群发页面 | P0 | 群发任务创建、管理界面 |
| 消息预览 | P1 | 发送前预览消息效果 |
| 发送统计 | P1 | 已读/未读统计 |
| 审批流程 | P2 | 群发消息需要审批 |
| 模板管理 | P2 | 群发消息模板 |

#### 2.4.5 API 接口

```
POST   /api/im/broadcast              - 创建群发任务
GET    /api/im/broadcast/list         - 获取群发任务列表
GET    /api/im/broadcast/{id}         - 获取任务详情
POST   /api/im/broadcast/{id}/cancel  - 取消任务
GET    /api/im/broadcast/{id}/records - 获取发送记录
```

---

### 2.5 审批催办

#### 2.5.1 功能描述
对超时未审批的申请进行催办，支持自动催办、催办记录查看。

#### 2.5.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-api/.../domain/ImApprovalUrge.java` | 催办实体 | ✅ |
| `ruoyi-im-api/.../service/ImApprovalUrgeService.java` | 服务接口 | ⚠️ 部分实现 |
| `ruoyi-im-api/.../mapper/ImApprovalUrgeMapper.java` | Mapper | ✅ |

#### 2.5.3 已实现功能

- ✅ 催办记录数据模型
- ✅ 添加催办记录
- ✅ 查询催办记录

#### 2.5.4 待实现功能

| 功能项 | 优先级 | 说明 |
|-------|-------|------|
| 催办通知发送 | P0 | 催办时发送消息通知审批人 |
| 自动催办任务 | P0 | 定时任务检测超时审批自动催办 |
| 催办次数限制 | P1 | 限制每日催办次数 |
| 催办配置 | P1 | 配置超时时间、催办间隔 |
| 前端催办界面 | P0 | 审批详情页催办按钮 |
| 催办记录查询 | P1 | 催办历史记录查询 |

#### 2.5.5 数据库表结构

```sql
-- 审批催办表
CREATE TABLE im_approval_urge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    approval_id BIGINT COMMENT '审批ID',
    user_id BIGINT COMMENT '催办人ID',
    target_user_id BIGINT COMMENT '被催办人ID',
    content VARCHAR(500) COMMENT '催办内容',
    urge_type VARCHAR(20) DEFAULT 'MANUAL' COMMENT '催办类型: MANUAL/AUTO',
    create_time DATETIME COMMENT '催办时间'
);
```

---

### 2.6 会议共享屏幕

#### 2.6.1 功能描述
视频会议中支持屏幕共享，共享者可将屏幕内容展示给其他参会者。

#### 2.6.2 实现文件

| 文件路径 | 类型 | 状态 |
|---------|------|------|
| `ruoyi-im-web/src/components/Chat/VideoMeetingRoom.vue` | 会议组件 | ✅ |
| `ruoyi-im-api/.../service/ImVideoMeetingService.java` | 服务接口 | ✅ |
| `ruoyi-im-api/.../controller/ImVideoMeetingController.java` | 控制器 | ✅ |

#### 2.6.3 已实现功能

- ✅ 屏幕共享开关按钮
- ✅ 使用 getDisplayMedia API 获取屏幕流
- ✅ 共享状态指示
- ✅ 停止共享自动切换回摄像头
- ✅ 后端共享状态管理

#### 2.6.4 待增强功能

| 功能项 | 优先级 | 说明 |
|-------|-------|------|
| 共享窗口选择 | P1 | 选择特定窗口/应用/标签页共享 |
| 共享标注 | P2 | 在共享屏幕上进行标注 |
| 远程控制 | P2 | 允许其他人远程操作共享屏幕 |
| 共享画质选择 | P2 | 高清/标清切换 |

---

## 📋 三、开发任务清单

### 3.1 P0 优先级任务（第一周）

| 序号 | 任务 | 模块 | 工作量 |
|-----|------|------|-------|
| 1 | 完善消息防转发管控规则API | 后端 | 4h |
| 2 | 实现转发拦截中间件 | 后端 | 4h |
| 3 | 前端转发权限控制 | 前端 | 3h |
| 4 | 消息群发前端页面 | 前端 | 8h |
| 5 | 审批催办通知发送 | 后端 | 4h |
| 6 | 自动催办定时任务 | 后端 | 4h |
| 7 | 审批详情页催办按钮 | 前端 | 2h |

### 3.2 P1 优先级任务（第二周）

| 序号 | 任务 | 模块 | 工作量 |
|-----|------|------|-------|
| 1 | 安全日志管理后台 | 前端 | 6h |
| 2 | 转发审批流程 | 后端 | 8h |
| 3 | 转发水印功能 | 后端+前端 | 4h |
| 4 | 群发发送统计 | 后端+前端 | 4h |
| 5 | 催办配置功能 | 后端+前端 | 4h |
| 6 | 水印防篡改增强 | 前端 | 4h |

---

## 🎯 四、验收标准

### 4.1 屏幕水印

- [ ] 水印正常显示，内容正确
- [ ] 水印无法被轻易删除或修改
- [ ] 配置变更实时生效
- [ ] 不影响用户正常操作

### 4.2 截图检测

- [ ] 快捷键截图被拦截并记录
- [ ] DevTools打开被检测并记录
- [ ] 日志正确上报到后端
- [ ] 管理后台可查看检测日志

### 4.3 消息防转发

- [ ] 敏感会话消息禁止转发
- [ ] 转发行为被审计记录
- [ ] 转发规则可配置
- [ ] 前端转发按钮正确禁用

### 4.4 消息群发

- [ ] 可创建群发任务
- [ ] 定时发送正常工作
- [ ] 发送记录可查询
- [ ] 目标用户正确接收消息

### 4.5 审批催办

- [ ] 手动催办发送通知
- [ ] 自动催办按时触发
- [ ] 催办记录可查询
- [ ] 催办次数可配置

### 4.6 会议共享屏幕

- [ ] 屏幕共享正常工作
- [ ] 共享状态正确显示
- [ ] 停止共享正常切换
- [ ] 其他参会者能看到共享内容

---

## 📊 五、风险评估

| 风险项 | 可能性 | 影响 | 应对措施 |
|-------|-------|------|---------|
| 浏览器兼容性问题 | 中 | 高 | 提前测试主流浏览器，准备降级方案 |
| 性能影响 | 低 | 中 | 水印刷新间隔优化，检测频率控制 |
| 用户接受度 | 低 | 中 | 提供开关配置，透明度可调节 |
| 安全绕过 | 中 | 高 | 多层防护，后端二次校验 |

---

## 📝 六、附录

### 6.1 技术栈

**前端**:
- Vue 3.3 + Vite 5
- Element Plus 2.4
- Vuex 4.1
- SCSS

**后端**:
- Spring Boot 2.7
- MyBatis Plus 3.5
- MySQL 8.0
- Redis

### 6.2 相关文档

- [需求文档.md](../docs/需求文档.md)
- [需求文档_功能差距分析.md](../docs/需求文档_功能差距分析.md)
- [README.md](../README.md)

---

*文档版本: v1.0*
*创建日期: 2026-03-03*
*作者: AI Analyst*