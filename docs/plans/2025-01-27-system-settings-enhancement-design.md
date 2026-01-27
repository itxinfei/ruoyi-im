# 系统设置增强功能设计文档

**创建日期**: 2025-01-27  
**作者**: Claude  
**状态**: 设计完成，待实施

---

## 1. 概述

本文档描述了对 RuoYi-IM 系统设置对话框的全面增强设计，包括：
- 完善编辑资料功能
- 新增聊天设置、文件管理、存储与数据、数据保留等设置分类
- UI/UX 优化
- 后端集成与持久化

---

## 2. 现有基础设施

| 组件 | 状态 | 说明 |
|------|------|------|
| `ImUser` 实体 | ✅ 已有 | 包含 nickname, avatar, signature, gender, email, mobile |
| `ImUserUpdateRequest` DTO | ✅ 已有 | 支持更新所有基本资料字段 |
| `/api/im/user/{id}` PUT | ✅ 已有 | 更新用户信息接口 |
| `/api/im/user/avatar` POST | ✅ 已有 | 上传头像接口 |
| `ImUserSetting` 实体 | ✅ 已有 | 用于存储用户偏好设置（键值对模式） |

---

## 3. 功能设计

### 3.1 编辑资料功能

#### 3.1.1 前端组件结构

```
SystemSettingsDialog.vue
└── EditProfileDialog.vue (新建)
    ├── 头像上传区
    │   ├── 预览当前头像
    │   ├── 上传按钮
    │   └── 裁剪预览 (使用 vue-cropper 或 element-plus 的图片裁剪)
    ├── 基本信息表单
    │   ├── 昵称输入框 (必填，2-50字符)
    │   ├── 个性签名输入框 (最多200字符)
    │   ├── 性别选择 (单选: 未知/男/女)
    │   ├── 生日选择器 (日期选择)
    │   └── 邮箱输入框 (格式校验)
    └── 操作按钮
        ├── 取消
        └── 保存
```

#### 3.1.2 数据流

```
用户操作 → 前端校验 → API 调用 → Vuex 更新 → 界面刷新

PUT /api/im/user/{userId}
Request Body: {
  "nickname": "新昵称",
  "signature": "个性签名",
  "gender": 1,
  "email": "user@example.com",
  "birthday": "1990-01-01"
}
```

#### 3.1.3 后端需要新增

`ImUser` 实体新增 `birthday` 字段（`LocalDate` 类型），对应数据库 `birthday` DATE 列。

```sql
ALTER TABLE im_user ADD COLUMN birthday DATE DEFAULT NULL COMMENT '生日';
```

#### 3.1.4 头像上传流程

```
选择图片 → 前端裁剪 → 压缩(最大2MB) → FormData上传 → 返回URL → 更新用户
                                            ↓
                        POST /api/im/user/avatar (已有接口)
```

---

### 3.2 新增设置分类

#### 3.2.1 聊天设置

| 设置项 | 类型 | 可选值 | 存储键名 |
|--------|------|--------|----------|
| 消息字体大小 | 单选 | 小(14px) / 中(16px) / 大(18px) / 特大(20px) | `chat.fontSize` |
| 聊天背景 | 选择 | 默认 / 自定义图片 / 纯色 | `chat.background` |
| 气泡样式 | 单选 | 默认 / 紧凑 / 宽松 | `chat.bubbleStyle` |
| 消息发送快捷键 | 单选 | Enter / Ctrl+Enter | `chat.sendShortcut` |

#### 3.2.2 文件管理

| 设置项 | 类型 | 说明 | 存储键名 |
|--------|------|------|----------|
| 自动下载图片 | 开关 | 自动下载接收的图片 | `file.autoDownloadImage` |
| 自动下载文件 | 开关 | 自动下载接收的文件 | `file.autoDownloadFile` |
| 默认保存路径 | 路径选择 | 本地文件保存目录 | `file.savePath` |
| 文件大小限制 | 开关 | 超过指定大小提示确认 | `file.sizeWarning` |

#### 3.2.3 存储与数据

| 设置项 | 类型 | 说明 | 实现方式 |
|--------|------|------|----------|
| 缓存大小 | 只读 | 显示当前缓存占用 | 读取浏览器 IndexedDB/LocalStorage |
| 清理缓存 | 按钮 | 清除图片/文件缓存 | 清除浏览器缓存 |
| 导出聊天记录 | 按钮 | 导出为 JSON/CSV | 前端生成下载文件 |
| 存储占用统计 | 展示 | 按会话分组显示大小 | 遍历 IndexedDB 统计 |

#### 3.2.4 数据保留

| 设置项 | 类型 | 说明 | 存储键名 |
|--------|------|------|----------|
| 退出保留聊天记录 | 开关 | 退出登录后不清除本地数据 | `data.keepOnLogout` |

---

## 4. UI/UX 设计

### 4.1 对话框结构优化

```
┌────────────────────────────────────────────────────────┐
│                    系统设置                              │
│  ┌────┬────┬────┬────┬────┬────┬────┬────┐            │
│  │账号│通知│隐私│通用│聊天│文件│存储│关于│ ← 横向滚动   │
│  └────┴────┴────┴────┴────┴────┴────┴────┘            │
│  ┌────────────────────────────────────────────────┐   │
│  │                                                │   │
│  │              内容区域                           │   │
│  │                                                │   │
│  └────────────────────────────────────────────────┘   │
│                                    [确定] [取消]       │
└────────────────────────────────────────────────────────┘
```

**改进点：**
- 添加底部操作栏（确定/取消按钮）
- 设置项修改后实时同步到 Vuex，关闭时无需额外保存
- 支持键盘 ESC 关闭

### 4.2 设置卡片交互优化

| 状态 | 视觉反馈 |
|------|----------|
| hover | 边框变为主题色，轻微上移 1px |
| 修改中 | 显示保存指示器（小圆点） |
| 已保存 | 显示 ✓ 成功提示（1秒后消失） |
| 保存失败 | 显示 ✗ 并提示错误原因 |

### 4.3 新增前端组件

```
ruoyi-im-web/src/components/Common/
├── SystemSettingsDialog.vue    # 主对话框（重构）
├── EditProfileDialog.vue       # 编辑资料对话框（新增）
├── StorageUsageCard.vue        # 存储占用展示卡片（新增）
├── ChatBackgroundPicker.vue    # 聊天背景选择器（新增）
└── AvatarCropper.vue           # 头像裁剪组件（新增）
```

---

## 5. 后端设计

### 5.1 用户设置 API（新增）

```
GET    /api/im/user/settings         获取当前用户所有设置
PUT    /api/im/user/settings         批量更新用户设置
PATCH  /api/im/user/settings/{key}   更新单个设置项
DELETE /api/im/user/settings/{key}   删除设置项
```

### 5.2 存储占用统计 API（新增）

```
GET /api/im/user/storage
Response: {
  "totalSize": 104857600,      // 总字节
  "messages": 52428800,         // 消息占用
  "files": 52428800,            // 文件占用
  "cache": 0                    // 缓存占用（前端统计）
}
```

### 5.3 新增后端类

```
com.ruoyi.im/
├── controller/
│   └── ImUserSettingController.java    # 用户设置控制器（新增）
├── service/
│   ├── IImUserSettingService.java      # 用户设置服务接口（新增）
│   └── impl/
│       └── ImUserSettingServiceImpl.java  # 用户设置服务实现（新增）
├── dto/
│   └── setting/
│       ├── UserSettingQueryRequest.java  # 设置查询请求（新增）
│       └── UserSettingUpdateRequest.java  # 设置更新请求（新增）
└── vo/
    └── setting/
        └── UserSettingVO.java            # 设置返回VO（新增）
```

### 5.4 前端 API 封装

```javascript
// ruoyi-im-web/src/api/im/user_setting.js (新增)
export function getUserSettings() { ... }
export function updateUserSettings(settings) { ... }
export function updateSetting(key, value) { ... }
export function getStorageUsage() { ... }
```

---

## 6. 数据存储策略

**两级存储：**

| 设置类型 | 存储位置 | 原因 |
|----------|----------|------|
| **本地偏好** | Vuex + LocalStorage | 主题、字体大小等无需同步的UI偏好 |
| **用户数据** | 后端 im_user_setting 表 | 通知开关、隐私设置等需要跨设备同步 |

**同步策略：**
- 登录后从后端拉取用户设置，合并到本地状态
- 用户修改同步设置项时，立即调用后端 API 更新
- 网络失败时暂存本地，恢复后重试

---

## 7. 工作量估算

| 模块 | 工作量 | 涉及文件 |
|------|--------|----------|
| 编辑资料功能 | 中 | 后端: ImUser.java, ImUserService<br>前端: EditProfileDialog.vue, user.js |
| 聊天设置 | 小 | 前端: SystemSettingsDialog.vue |
| 文件管理 | 小 | 前端: SystemSettingsDialog.vue |
| 存储与数据 | 中 | 前端: StorageUsageCard.vue, API 封装 |
| 后端设置 API | 中 | Controller + Service + DTO/VO |
| 后端存储 API | 小 | Controller + Service |

---

## 8. 开发计划

### 阶段一：编辑资料功能
1. 后端：添加 birthday 字段到 ImUser
2. 后端：确认 ImUserUpdateRequest 包含所有必要字段
3. 前端：创建 EditProfileDialog.vue
4. 前端：集成头像裁剪功能

### 阶段二：新增设置分类
1. 前端：扩展 SystemSettingsDialog.vue 添加新标签页
2. 前端：实现各设置项的 UI 和交互
3. 前端：创建 StorageUsageCard.vue

### 阶段三：后端设置 API
1. 后端：创建 ImUserSettingController
2. 后端：创建 ImUserSettingService
3. 后端：创建相关 DTO 和 VO
4. 前端：创建 user_setting.js API 封装

### 阶段四：同步与持久化
1. 前端：实现设置同步逻辑
2. 前端：添加网络失败重试机制
3. 测试：跨设备设置同步验证
