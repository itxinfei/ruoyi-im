# RuoYi-IM 前端代码重复与未使用代码分析报告

> **项目**: ruoyi-im-web
> **技术栈**: Vue 3 + Element Plus + Vite
> **分析日期**: 2026-02-06
> **版本**: v1.0

---

## 目录

1. [执行摘要](#执行摘要)
2. [重复代码分析](#重复代码分析)
3. [功能重复分析](#功能重复分析)
4. [未使用代码分析](#未使用代码分析)
5. [重构建议](#重构建议)
6. [优先级排序](#优先级排序)

---

## 执行摘要

### 关键发现

| 类型 | 数量 | 优先级 |
|------|------|--------|
| 重复函数 | 2 处 | 中 |
| 重复功能模块 | 3 处 | 高 |
| 未使用代码 | 待确认 | 低 |
| 可优化点 | 5+ 处 | 低 |

### 潜在收益

- **代码减少**: 预计可减少约 400-600 行重复代码
- **维护成本**: 统一修改只需 1 处而非 2-3 处
- **一致性**: 消除逻辑差异导致的行为不一致

---

## 重复代码分析

### 1. formatRelativeTime 函数重复 ⚠️

**位置**:
- `src/utils/format.js:84-102` (19 行)
- `src/utils/message.js:216-233` (18 行)

**代码对比**:

```javascript
// format.js 版本
export function formatRelativeTime(timestamp) {
  if (!timestamp) {return ''}
  const date = new Date(timestamp)
  const now = new Date()
  if (isNaN(date.getTime())) {return ''}

  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) {return '刚刚'}
  if (diffMins < 60) {return `${diffMins}分钟前`}
  if (diffHours < 24) {return `${diffHours}小时前`}
  if (diffDays < 7) {return `${diffDays}天前`}

  return formatDate(date)
}
```

```javascript
// message.js 版本
export function formatRelativeTime(timestamp) {
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) {return '刚刚'}
  if (diffMins < 60) {return `${diffMins}分钟前`}
  if (diffHours < 24) {return `${diffHours}小时前`}
  if (diffDays < 7) {return `${diffDays}天前`}

  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}
```

**差异点**:
- `format.js` 版本有空值检查和日期有效性检查
- `message.js` 版本缺少边界检查

**建议**: 删除 `message.js` 中的实现，改为从 `format.js` 重新导出

---

### 2. formatFileSize 已正确处理 ✅

**位置**: `src/utils/message.js:6`

```javascript
// 从 format.js 重新导出 formatFileSize，避免重复定义
export { formatFileSize } from './format.js'
```

**状态**: 这是正确的做法，无需修改

---

## 功能重复分析

### 1. 文件上传功能重复 ⚠️ 高优先级

**重复模块**:
- `src/composables/useFileUpload.js` (360 行)
- `src/composables/useChat/useChatUpload.js` (243 行)

**功能对比**:

| 功能 | useFileUpload | useChatUpload |
|------|---------------|---------------|
| 图片上传 | ✅ | ✅ |
| 文件上传 | ✅ | ✅ |
| 视频上传 | ✅ | ✅ |
| 语音上传 | ✅ | ❌ |
| 截图上传 | ✅ | ❌ |
| 上传队列 | ❌ | ✅ |
| 进度追踪 | ❌ | ✅ |
| 重试机制 | ❌ | ✅ |
| 乐观更新 | ✅ | ❌ |
| 临时消息 | ✅ | ❌ |

**代码对比** (图片上传):

```javascript
// useFileUpload.js
const uploadImageFile = async (fileOrFormData) => {
  const file = fileOrFormData instanceof FormData
    ? fileOrFormData.get('file')
    : fileOrFormData

  if (!file) return

  await handleUpload({
    file,
    category: 'image',
    uploadApi: uploadImage,
    sendApi: options.sendMessage,
    messageType: 'IMAGE',
    processContent: (data) => JSON.stringify({
      fileId: data.fileId,
      imageUrl: data.imageUrl
    })
  })
}
```

```javascript
// useChatUpload.js
const uploadImage = async (file, conversationId) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    throw new Error('文件类型错误')
  }

  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 10MB')
    throw new Error('文件过大')
  }

  return uploadSingleFile(file, conversationId, 'IMAGE')
}
```

**分析**:
- 两个模块实现了相同的核心功能，但设计理念不同
- `useFileUpload` 专注于消息发送的乐观更新
- `useChatUpload` 专注于上传队列管理和进度追踪

**建议**: 合并为一个统一的文件上传 Composable，同时支持两种使用场景

---

### 2. 消息预览格式化逻辑重复 ⚠️

**位置**:
- `src/utils/message.js:24-83` - `formatMessagePreview`
- `src/utils/message.js:90-209` - `formatMessagePreviewFromObject`

**问题**: 两个函数处理相似的逻辑，存在大量重复的 switch-case 结构

**代码行数**: 约 120 行

**建议**: 可以重构为统一的消息预览格式化函数

---

### 3. 时间格式化函数功能重叠 ⚠️

**位置**: `src/utils/format.js`

| 函数 | 行数 | 功能 | 重叠度 |
|------|------|------|--------|
| `formatTime` | 14-21 | HH:mm | 独立 |
| `formatTimeFull` | 28-36 | HH:mm:ss | 独立 |
| `formatDate` | 43-50 | MM月DD日 | 与 formatRelativeTime 最后部分重复 |
| `formatChatTime` | 109-134 | 智能聊天时间 | 与 formatListItemTime 重复 |
| `formatListItemTime` | 306-327 | 列表时间 | 与 formatChatTime 重复 |
| `formatRelativeTime` | 84-102 | 相对时间 | 独立 |

**对比**:

```javascript
// formatChatTime - 109-134 行
export function formatChatTime(timestamp) {
  if (!timestamp) {return ''}
  const date = new Date(timestamp)
  const now = new Date()
  if (isNaN(date.getTime())) {return ''}

  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.floor((today - targetDate) / 86400000)

  if (diffDays === 0) {
    return formatTime(date)
  } else if (diffDays === 1) {
    return `昨天 ${formatTime(date)}`
  } else if (diffDays < 7) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const weekday = weekdays[date.getDay()]
    return `${weekday} ${formatTime(date)}`
  } else {
    return `${formatDate(date)} ${formatTime(date)}`
  }
}
```

```javascript
// formatListItemTime - 306-327 行
export function formatListItemTime(time) {
  if (!time) {return ''}
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 86400000) {
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${hours}:${minutes}`
  } else if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  } else {
    const month = date.toLocaleDateString('zh-CN', { month: 'short' })
    const day = date.getDate()
    return `${month}${day}日`
  }
}
```

**差异**:
- `formatChatTime`: 今天显示时间，昨天显示"昨天"，一周内显示星期
- `formatListItemTime`: 24小时内显示时间，7天内显示"X天前"

**建议**: 可以合并为一个通用函数，通过参数控制显示风格

---

## 未使用代码分析

### 需要进一步检查的项目

1. **已删除的文件** (根据 git status):
   - `ruoyi-im-web/src/composables/useResponsive.js` - 已删除
   - `ruoyi-im-web/src/styles/contacts-mobile-optimization.scss` - 已删除
   - `ruoyi-im-web/src/components/Common/GlobalSearchDialog.vue.backup` - 已删除

2. **可能未使用的 Composables** (需要检查引用):
   - `src/composables/useContactBatch.js` (新文件)
   - `src/composables/useHighlightText.js` (新文件)

3. **可能冗余的 API 函数**:
   - `src/api/im/file.js` vs `src/api/im/groupFile.js`
   - 需要检查是否有重复的上传/下载接口

---

## 重构建议

### 优先级 1: 统一文件上传模块

**目标**: 将 `useFileUpload` 和 `useChatUpload` 合并

**方案**:
```javascript
// src/composables/useFileUploadUnified.js
export function useFileUploadUnified(options = {}) {
  // 合并两个模块的所有功能
  return {
    // 基础上传
    uploadImage,
    uploadFile,
    uploadVideo,
    uploadVoice,

    // 队列管理
    uploadQueue,
    uploadProgress,

    // 状态管理
    uploading,
    cancelUpload,
    retryUpload,

    // 乐观更新（可选）
    withOptimisticUpdate,
    withoutOptimisticUpdate
  }
}
```

**预期收益**: 减少约 400 行代码，统一上传逻辑

---

### 优先级 2: 消除 formatRelativeTime 重复

**方案**:
```javascript
// src/utils/message.js
// 删除本地实现，改为重新导出
export { formatRelativeTime } from './format.js'
```

**预期收益**: 减少约 18 行代码

---

### 优先级 3: 统一时间格式化

**方案**:
```javascript
// src/utils/format.js
export function formatTimeSmart(timestamp, options = {}) {
  const {
    style = 'chat', // 'chat' | 'list' | 'relative' | 'full'
    showWeekday = true,
    showTime = true
  } = options

  // 统一实现，通过参数控制输出格式
}
```

**预期收益**: 减少约 60 行代码

---

### 优先级 4: 统一消息预览格式化

**方案**:
```javascript
// src/utils/message.js
export function formatMessagePreview(message, options = {}) {
  const {
    maxLength = 30,
    showRecaller = true,
    style = 'short' // 'short' | 'full'
  } = options

  // 合并 formatMessagePreview 和 formatMessagePreviewFromObject
}
```

**预期收益**: 减少约 80 行代码

---

## 优先级排序

| 优先级 | 项目 | 难度 | 收益 | 工作量 |
|--------|------|------|------|--------|
| P0 | 统一文件上传模块 | 高 | 高 | 2-3 天 |
| P1 | 消除 formatRelativeTime 重复 | 低 | 低 | 30 分钟 |
| P1 | 检查未使用的代码 | 中 | 中 | 1 天 |
| P2 | 统一时间格式化 | 中 | 中 | 1 天 |
| P2 | 统一消息预览格式化 | 中 | 中 | 半天 |
| P3 | 检查 API 重复 | 低 | 低 | 半天 |

---

## 附录：文件清单

### 涉及的重复代码文件

1. `src/utils/format.js` - 格式化工具函数 (343 行)
2. `src/utils/message.js` - 消息格式化工具 (276 行)
3. `src/composables/useFileUpload.js` - 文件上传 (360 行)
4. `src/composables/useChat/useChatUpload.js` - 聊天文件上传 (243 行)

### 新增文件 (需确认使用情况)

1. `src/composables/useContactBatch.js`
2. `src/composables/useHighlightText.js`
3. `ruoyi-im-web/nul.map` - 可能是错误生成的文件

---

## 总结

本报告分析了 ruoyi-im-web 前端项目中的代码重复和未使用代码情况。主要发现:

1. **重复函数**: `formatRelativeTime` 在两个文件中重复定义
2. **功能重复**: 文件上传逻辑存在两个独立的 Composable
3. **时间格式化**: 多个函数功能存在重叠
4. **未使用代码**: 需要进一步检查新引入的文件和已删除的文件

建议按优先级逐步进行重构，优先处理文件上传模块的统一，这将带来最大的收益。

---

*报告生成时间: 2026-02-06*
*分析工具: Claude Code*
*项目版本: v1.0*
