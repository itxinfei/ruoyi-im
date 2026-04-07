# 钉钉风格 Web IM 设计规范

> 本规范基于钉钉 8.2 设计系统，为私有化部署的 Web IM 应用提供统一的视觉和交互标准。

---

## 目录

1. [设计原则](#1-设计原则)
2. [色彩系统](#2-色彩系统)
3. [字体系统](#3-字体系统)
4. [间距系统](#4-间距系统)
5. [圆角系统](#5-圆角系统)
6. [阴影系统](#6-阴影系统)
7. [动效规范](#7-动效规范)
8. [组件规范](#8-组件规范)
9. [布局规范](#9-布局规范)

---

## 1. 设计原则

### 1.1 核心原则

| 原则 | 说明 | 应用场景 |
|------|------|----------|
| **一致性** | 整个应用使用统一的设计语言 | 按钮、卡片、输入框等组件风格统一 |
| **层次感** | 通过颜色、间距、大小区分信息层级 | 标题与正文、标签与内容 |
| **呼吸感** | 合理留白，避免拥挤 | 列表项间距、卡片内边距 |
| **即时反馈** | 每个交互都有明确的视觉反馈 | hover、active、loading 状态 |
| **渐进式披露** | 信息分层次展示 | 收起/展开、更多操作 |

### 1.2 设计哲学

```
钉钉 = 效率 + 专业 + 简洁

不做花哨的动画
不做多余的装饰
不做复杂的交互
```

---

## 2. 色彩系统

### 2.1 品牌色

```scss
--dt-brand-color: #277EFB;      // 品牌蓝
--dt-brand-hover: #1169E0;       // 悬停
--dt-brand-active: #0656C6;      // 点击
--dt-brand-light: #4493FA;      // 浅色
--dt-brand-lighter: #E8F2FE;     // 极浅背景
```

**使用规范**：
- 主要按钮：使用 `brand-color`
- 悬停状态：使用 `brand-hover`
- 激活状态：使用 `brand-active`
- 背景色：使用 `brand-bg` 或 `brand-lighter`

### 2.2 功能色

```scss
// 成功
--dt-success-color: #00B42A;
--dt-success-bg: #E8FFEA;

// 警告
--dt-warning-color: #FF7D00;
--dt-warning-bg: #FFF7E6;

// 错误
--dt-error-color: #F53F3F;
--dt-error-bg: #FFECEC;

// 信息
--dt-info-color: #165DFF;
--dt-info-bg: #E8F2FE;
```

### 2.3 中性色

```scss
// 文字色
--dt-text-primary: #171a1d;      // 标题
--dt-text-secondary: #5f6670;   // 正文
--dt-text-tertiary: #8a9099;    // 辅助
--dt-text-quaternary: #b1b6bd;  // 禁用
--dt-text-icon: #ADB1B8;         // 图标默认

// 背景色
--dt-bg-body: #f5f6f7;           // 页面背景
--dt-bg-card: #ffffff;           // 卡片背景
--dt-bg-chat: #f5f6f7;           // 聊天背景
--dt-bg-hover: rgba(23,26,29,0.04);  // 悬停背景
```

### 2.4 文件类型色

```scss
--dt-file-pdf: #FF4D4F;    // PDF
--dt-file-word: #2B7DFF;   // Word
--dt-file-excel: #22AB5C;  // Excel
--dt-file-ppt: #FF7A00;    // PowerPoint
--dt-file-other: #ADB1B8;  // 其他
```

### 2.5 色彩使用示例

```vue
<!-- 品牌色应用 -->
<el-button type="primary">主按钮</el-button>

<!-- 功能色应用 -->
<el-tag type="success">成功</el-tag>
<el-tag type="warning">警告</el-tag>
<el-tag type="danger">错误</el-tag>

<!-- 中性色应用 -->
<span class="title">标题</span>
<span class="subtitle">正文</span>
<span class="hint">辅助信息</span>
```

---

## 3. 字体系统

### 3.1 字体栈

```scss
--dt-font-family: 
  -apple-system,           // Safari (Mac)
  BlinkMacSystemFont,      // Chrome (Mac)
  'Segoe UI',              // Windows
  Roboto,                  // Android
  'Helvetica Neue',         // 旧 Mac
  Arial,                   // 基础
  'PingFang SC',           // 苹方 (Mac)
  'Microsoft YaHei',       // 微软雅黑 (Windows)
  sans-serif;
```

### 3.2 字号规范

```scss
--dt-font-size-xs: 11px;      // 辅助信息、徽章
--dt-font-size-sm: 12px;     // 次要文字、时间戳
--dt-font-size-base: 14px;   // 正文（默认）
--dt-font-size-md: 15px;     // 重要正文
--dt-font-size-lg: 16px;     // 小标题
--dt-font-size-xl: 18px;     // 大标题
--dt-font-size-2xl: 24px;    // 页面标题
```

### 3.3 字重规范

```scss
--dt-font-weight-light: 300;      // 细体（特殊场景）
--dt-font-weight-normal: 400;     // 常规（正文）
--dt-font-weight-medium: 500;      // 中等（标签）
--dt-font-weight-semibold: 600;   // 半粗（导航）
--dt-font-weight-bold: 700;       // 粗体（标题）
```

### 3.4 字体使用规范

```vue
<!-- 标题 -->
<h1 class="page-title">24px / Bold</h1>
<h2 class="section-title">18px / Bold</h2>
<h3 class="card-title">16px / Semibold</h3>

<!-- 正文 -->
<p class="content">14px / Normal</p>
<p class="secondary">14px / Normal / Secondary Color</p>

<!-- 辅助 -->
<span class="hint">12px / Tertiary Color</span>
<span class="badge">11px / Bold / Badge</span>
```

---

## 4. 间距系统

### 4.1 8px 栅格

```scss
--dt-spacing-xs: 4px;     // 紧凑间距
--dt-spacing-sm: 8px;    // 小间距
--dt-spacing-md: 12px;    // 中间距
--dt-spacing-lg: 16px;    // 标准间距
--dt-spacing-xl: 24px;    // 大间距
--dt-spacing-2xl: 32px;   // 特大间距
```

### 4.2 间距使用场景

| 间距 | 数值 | 使用场景 |
|------|------|----------|
| xs | 4px | 徽章内边距、图标与文字间距 |
| sm | 8px | 标签内间距、列表项小间距 |
| md | 12px | 卡片内元素间距 |
| lg | 16px | 卡片内边距、表单项间距 |
| xl | 24px | 区块间间距、面板内边距 |
| 2xl | 32px | 大区块间间距 |

### 4.3 布局尺寸

```scss
// 导航
--dt-nav-sidebar-width: 64px;    // 左侧导航宽度

// 头像
--dt-avatar-size-sm: 24px;       // 小头像
--dt-avatar-size-md: 36px;      // 中头像
--dt-avatar-size-lg: 44px;      // 大头像

// 面板
--dt-session-panel-width: 250px;  // 会话列表宽度
--dt-contact-panel-width: 240px;  // 通讯录宽度

// 高度
--dt-header-height: 56px;         // 头部高度
--dt-chat-header-height: 60px;    // 聊天头部
--dt-toolbar-height: 32px;        // 工具栏高度
```

---

## 5. 圆角系统

### 5.1 圆角规范

```scss
--dt-radius-xs: 2px;      // 微圆角（分隔线）
--dt-radius-sm: 4px;       // 小圆角（标签）
--dt-radius-md: 6px;       // 中圆角（按钮）
--dt-radius-lg: 8px;       // 大圆角（卡片）
--dt-radius-xl: 12px;      // 特大圆角（面板）
--dt-radius-2xl: 16px;     // 超大圆角（模态框）
--dt-radius-full: 9999px;  // 圆形（头像、徽章）
```

### 5.2 圆角使用规范

| 元素 | 圆角 | 说明 |
|------|------|------|
| 按钮 | 6px | 标准按钮 |
| 输入框 | 6px | 与按钮保持一致 |
| 卡片 | 8px | 页面卡片 |
| 面板 | 12px | 大面板 |
| 对话框 | 16px | 模态框 |
| 头像 | 4px | 钉钉风格方形头像 |
| 徽章 | 10px | 胶囊形 |
| 圆形头像 | 50% | 群成员 |

### 5.3 消息气泡圆角

```scss
// 接收的消息（左侧）- 右下圆角
border-radius: 8px 4px 8px 8px;

// 发送的消息（右侧）- 左下圆角
border-radius: 4px 8px 8px 8px;
```

---

## 6. 阴影系统

### 6.1 阴影规范

```scss
--dt-shadow-1: 0 1px 2px rgba(0,0,0,0.04);      // 轻微
--dt-shadow-2: 0 2px 8px rgba(0,0,0,0.08);      // 标准
--dt-shadow-3: 0 4px 16px rgba(0,0,0,0.12);     // 悬浮
--dt-shadow-4: 0 8px 32px rgba(0,0,0,0.16);     // 强调
--dt-shadow-float: 0 6px 24px rgba(0,0,0,0.15); // 浮动
--dt-shadow-modal: 0 12px 48px rgba(0,0,0,0.2); // 模态框
--dt-shadow-brand: 0 2px 8px rgba(0,102,204,0.15); // 品牌阴影
```

### 6.2 阴影使用规范

| 场景 | 阴影 | 说明 |
|------|------|------|
| 卡片悬停 | shadow-2 | 轻微提升感 |
| 下拉菜单 | shadow-2 | 浮出效果 |
| 卡片激活 | shadow-3 | 选中态 |
| 模态框 | shadow-modal | 强聚焦 |
| 头像悬停 | shadow-brand | 品牌强调 |

---

## 7. 动效规范

### 7.1 时长规范

```scss
--dt-transition-fast: 0.15s;     // 快速（hover、反馈）
--dt-transition-base: 0.25s;      // 标准（展开、收起）
--dt-transition-slow: 0.4s;       // 缓慢（页面过渡）
```

### 7.2 缓动函数

```scss
// 标准缓动
cubic-bezier(0.4, 0, 0.2, 1)

// 强调缓动（回弹）
cubic-bezier(0.34, 1.56, 0.64, 1)

// 减速缓动（进入）
cubic-bezier(0, 0, 0.2, 1)

// 加速缓动（退出）
cubic-bezier(0.4, 0, 1, 1)
```

### 7.3 动效使用规范

| 交互 | 时长 | 缓动 | 示例 |
|------|------|------|------|
| hover | 150ms | ease | 按钮颜色变化 |
| active | 100ms | ease | 按钮缩放 |
| 展开/收起 | 250ms | ease | 下拉菜单 |
| 模态框 | 300ms | ease-out | 弹窗出现 |
| 页面切换 | 400ms | ease | 面板切换 |

### 7.4 微交互规范

```scss
// 按钮缩放
transform: scale(0.95);

// 卡片悬停提升
transform: translateY(-2px);
box-shadow: var(--dt-shadow-3);

// 图标缩放
transform: scale(1.1);

// 透明度渐变
opacity: 0 → 1;
```

---

## 8. 组件规范

### 8.1 按钮规范

```scss
// 尺寸
--dt-btn-height-sm: 32px;    // 小按钮
--dt-btn-height-md: 36px;    // 中按钮
--dt-btn-height-lg: 40px;    // 大按钮
--dt-btn-min-width: 80px;    // 最小宽度
```

**按钮状态**：
- Default：品牌色背景，白色文字
- Hover：hover色背景
- Active：active色背景，scale(0.95)
- Disabled：灰色背景，禁止光标
- Loading：显示loading动画

### 8.2 输入框规范

```scss
// 高度
--dt-input-min-height: 96px;   // 多行输入
--dt-input-max-height: 300px;   // 最大高度
```

**输入框状态**：
- Default：浅灰背景，1px边框
- Focus：品牌色边框，浅蓝背景
- Error：红色边框，错误提示
- Disabled：禁用背景，禁止光标

### 8.3 卡片规范

```vue
<!-- 基础卡片 -->
<div class="card">
  <div class="card-header">标题</div>
  <div class="card-body">内容</div>
</div>

<style scoped>
.card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  border: 1px solid var(--dt-border-light);
  padding: var(--dt-spacing-lg);
  transition: all var(--dt-transition-base);
}

.card:hover {
  box-shadow: var(--dt-shadow-2);
  border-color: var(--dt-brand-color);
}
</style>
```

### 8.4 列表项规范

```vue
<!-- 列表项 -->
<div class="list-item">
  <div class="item-avatar"></div>
  <div class="item-content">
    <div class="item-title">标题</div>
    <div class="item-subtitle">副标题</div>
  </div>
  <div class="item-extra">额外信息</div>
</div>

<style scoped>
.list-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);
}

.list-item:hover {
  background: var(--dt-bg-hover);
}

.list-item:active {
  background: var(--dt-bg-session-active);
}
</style>
```

---

## 9. 布局规范

### 9.1 页面结构

```
┌─────────────────────────────────────────────────────────────┐
│ 左侧导航 (64px)  │          主内容区                         │
│                  │  ┌─────────────────────────────────────┐ │
│  [Logo]          │  │ 顶部栏 (56px)                       │ │
│  [导航图标列表]   │  ├─────────────────────────────────────┤ │
│  [分割线]        │  │                                     │ │
│  [工具图标]       │  │  内容区                              │ │
│  [用户头像]       │  │                                     │ │
│                  │  │                                     │ │
│                  │  └─────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 聊天布局

```
┌──────────────────┬──────────────────────────────────────────┐
│ 会话列表 (250px) │              聊天窗口                    │
│                  │  ┌──────────────────────────────────────┐ │
│ [搜索框]         │  │ 头部 (56px): 名称 + 操作按钮         │ │
│ [Tab切换]        │  ├──────────────────────────────────────┤ │
│ [会话项]         │  │                                      │ │
│  - 头像          │  │ 消息列表                              │ │
│  - 名称          │  │  - 时间分割线                         │ │
│  - 预览          │  │  - 消息气泡                           │ │
│  - 时间          │  │                                      │ │
│ [会话项]         │  ├──────────────────────────────────────┤ │
│ [会话项]         │  │ 输入区 (高度自适应)                   │ │
│                  │  │  - 工具栏                             │ │
│                  │  │  - 富文本输入                         │ │
│                  │  │  - 发送栏                             │ │
└──────────────────┴──┴──────────────────────────────────────┘

> **注意**: 本应用为桌面端设计，暂不支持移动端响应式适配。

---

## 10. 图标规范

### 10.1 图标来源

使用 Element Plus Icons 作为主要图标库。

```vue
<script setup>
import { ChatDotRound, Search, User, Setting } from '@element-plus/icons-vue'
</script>

<template>
  <el-icon><ChatDotRound /></el-icon>
</template>
```

### 10.2 图标尺寸

```scss
--dt-icon-size-xs: 14px;   // 表格内图标
--dt-icon-size-sm: 16px;   // 小图标
--dt-icon-size-md: 18px;   // 中图标
--dt-icon-size-lg: 20px;    // 大图标
--dt-icon-size-xl: 24px;   // 特大图标
```

### 10.3 图标使用规范

| 场景 | 尺寸 | 说明 |
|------|------|------|
| 工具栏图标 | 20px | 输入区工具栏 |
| 导航图标 | 20px | 侧边导航 |
| 列表图标 | 16px | 列表项前缀 |
| 按钮图标 | 16px | 按钮内图标 |
| 状态图标 | 14px | 小状态指示 |

### 10.4 图标颜色

```scss
// 默认状态
.icon { color: var(--dt-text-icon); }

// 悬停状态
.icon:hover { color: var(--dt-text-primary); }

// 激活状态
.icon.active { color: var(--dt-brand-color); }

// 白色图标（深色背景）
.icon-on-dark { color: rgba(255,255,255,0.75); }
.icon-on-dark:hover { color: rgba(255,255,255,0.95); }
```

---

## 附录

### A. 设计令牌引用

所有设计令牌都在 `src/styles/design-tokens.scss` 中定义。

```scss
// 引用方式
import '@/styles/design-tokens.scss';

// 或在组件中使用
style="color: var(--dt-brand-color)"
```

### B. 主题切换

支持亮色和暗色主题，通过 `html.dark` 选择器切换。

```scss
html.dark {
  --dt-bg-body: #0f172a;
  --dt-text-primary: #f1f5f9;
  // ...
}
```

### C. 组件库结构

```
src/components/
├── common/           # 通用组件
│   ├── Button/
│   ├── Input/
│   └── Dialog/
├── im/              # IM相关组件
│   ├── ChatSessionList/
│   ├── ChatWindow/
│   ├── ChatMessageBubble/
│   └── ChatInputArea/
├── contacts/         # 通讯录组件
│   ├── OrganizationTree/
│   └── UserCard/
└── layout/           # 布局组件
    ├── SideNav/
    └── Header/
```

---

**版本**: v1.0.0  
**更新日期**: 2026-04-02  
**基于**: 钉钉 8.2 设计系统
