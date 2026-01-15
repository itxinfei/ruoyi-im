# 钉钉 7.6.x UI 设计复刻方案

**参考版本**: 钉钉 PC 客户端 7.6.x (2024-2025)
**创建日期**: 2025-01-15
**目标**: 100% 复刻钉钉 PC 客户端的 UI 和交互体验

---

## 一、版本概述

### 钉钉 7.6.x 版本特性

根据钉钉官方文档，7.6.x 版本（2024-2025）的核心特性包括：

| 版本 | 发布日期 | 核心特性 |
|------|----------|----------|
| 7.6.50 | 2025-02-24 | AI助理优化、性能提升 |
| 7.6.45 | 2025-02-07 | 新增AI助理入口、信息检索 |
| 7.6.40 | 2024-12-30 | 个性化封面、动态头像 |
| 7.6.35 | 2024-12-09 | 界面优化、响应式改进 |
| 7.6.30 | 2024-11-18 | 导航栏拖动调整宽度 |
| 7.6.00 | 2024-06-26 | 大版本更新，UI重构 |

### 2025 B端设计趋势（钉钉官方）

1. **多元化** - 支持多种使用场景和个性化需求
2. **智能化** - AI助理集成，智能信息检索
3. **人性化** - 动态头像、个性化封面、情感化设计

---

## 二、整体布局结构

### 2.1 三栏布局（钉钉 PC 标准结构）

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 🔍 搜索消息、联系人、群组...                     📊  🔔  👤 张三 ▼          │ ← 顶部栏 (48px)
├──────┬──────────────────────────────────┬────────────────────────────────────┤
│      │                                  │                                    │
│ 💬   │ ┌─────────────────────────────┐  │  ┌─────────────────────────────┐  │
│ 📋   │ │ ┌──┐ 张三            14:30  │  │  │  张三                      │  │
│ 📁   │ │ │📷│ 昨天开会讨论...    [2]  │  │  ─────────────────────────  │  │
│ 📅   │ │ └──┘                     │  │  │                            │  │
│ ⏰   │ │ ┌──┐ 产品群 (5)     10:15  │  │  │  你好！                   │  │
│ 🤖   │ │ │👥│ 李四: 这个需求...      │  │  │  收到，我马上处理。        │  │
│ ⚙️   │ │ └──┘                     │  │  │                            │  │
│      │ │ ┌──┐ 设计组         09:00  │  │  │                            │  │
│      │ │ │👥│ 工作安排...           │  │  │  💬💬💬💬💬              │  │
│      │ │ └──┘                     │  │  │                            │  │
│      │ └─────────────────────────────┘  │  └─────────────────────────────┘  │
│ 导航  │          会话列表              │  │            聊天区域            │  │
│ 68px │           280-360px             │  │              flex: 1            │  │
└──────┴──────────────────────────────────┴────────────────────────────────────┘
```

### 2.2 布局尺寸规范

| 区域 | 钉钉 7.6 规范 | 变量名 |
|------|---------------|--------|
| 顶部栏高度 | 48px | `--dt-header-height` |
| 左侧导航栏宽度 | 68px (可拖动调整 56-80px) | `--dt-nav-width` |
| 导航项高度 | 48px | `--dt-nav-item-height` |
| 中间会话列表宽度 | 280-360px (可拖动) | `--dt-session-width` |
| 会话项高度 | 64px | `--dt-session-item-height` |
| 右侧聊天区域 | flex: 1 | - |
| 输入区高度 | 120px | `--dt-input-height` |

---

## 三、设计 Token 系统（钉钉 7.6 风格）

### 3.1 颜色系统

```scss
:root {
  // === 品牌色（钉钉 7.6） ===
  --dt-color-primary: #0089FF;
  --dt-color-primary-hover: #0077E0;
  --dt-color-primary-active: #0066C2;
  --dt-color-primary-light: rgba(0, 137, 255, 0.1);

  // === 功能色 ===
  --dt-color-success: #00C853;  // 成功/在线
  --dt-color-warning: #FAAD14;  // 警告/离开
  --dt-color-error: #F5222D;    // 错误/忙碌
  --dt-color-info: #0089FF;

  // === 文字色 ===
  --dt-color-text-primary: #171A1A;     // 标题色
  --dt-color-text-secondary: #5F6468;   // 正文色
  --dt-color-text-tertiary: #858B8F;     // 次要文字
  --dt-color-text-quaternary: #B3B8BD;   // 辅助文字
  --dt-color-text-placeholder: #D2D7DA;

  // === 背景色 ===
  --dt-color-bg-primary: #FFFFFF;
  --dt-color-bg-secondary: #F5F7FA;     // 页面整体背景
  --dt-color-bg-tertiary: #EBECED;
  --dt-color-bg-elevated: #FFFFFF;
  --dt-color-bg-hover: rgba(0, 0, 0, 0.04);
  --dt-color-bg-active: rgba(0, 137, 255, 0.1);

  // === 边框色 ===
  --dt-color-border: #E5E8EB;
  --dt-color-border-light: #F0F2F5;
  --dt-color-border-lighter: #F7F8FA;

  // === 阴影 ===
  --dt-shadow-xs: 0 1px 2px rgba(0, 0, 0, 0.04);
  --dt-shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.06);
  --dt-shadow-md: 0 4px 8px rgba(0, 0, 0, 0.08);
  --dt-shadow-lg: 0 8px 16px rgba(0, 0, 0, 0.1);
  --dt-shadow-xl: 0 12px 24px rgba(0, 0, 0, 0.12);

  // === 消息气泡 ===
  --dt-bubble-sent-bg: #0089FF;
  --dt-bubble-sent-text: #FFFFFF;
  --dt-bubble-received-bg: #FFFFFF;
  --dt-bubble-received-text: #171A1A;
  --dt-bubble-received-border: #E5E8EB;
}
```

### 3.2 圆角系统

```scss
:root {
  --dt-radius-xs: 2px;
  --dt-radius-sm: 4px;
  --dt-radius-md: 6px;
  --dt-radius-lg: 8px;
  --dt-radius-xl: 12px;
  --dt-radius-2xl: 16px;
  --dt-radius-full: 50%;

  // 消息气泡特殊圆角
  --dt-radius-bubble: 12px;
  --dt-radius-bubble-tail: 4px;
}
```

### 3.3 字体系统

```scss
:root {
  // 字号（钉钉 7.6 优化了大字体显示）
  --dt-font-size-xs: 11px;    // 极小文字
  --dt-font-size-sm: 12px;    // 辅助文字/时间戳
  --dt-font-size-base: 14px;  // 正文
  --dt-font-size-lg: 16px;    // 小标题
  --dt-font-size-xl: 18px;    // 大标题
  --dt-font-size-2xl: 20px;   // 特大标题

  // 字重
  --dt-font-weight-normal: 400;
  --dt-font-weight-medium: 500;
  --dt-font-weight-semibold: 600;

  // 行高
  --dt-line-height-tight: 1.25;
  --dt-line-height-base: 1.5;
  --dt-line-height-loose: 1.75;

  // 字体
  --dt-font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
                   "Helvetica Neue", Arial, "PingFang SC", "Hiragino Sans GB",
                   "Microsoft YaHei", sans-serif;
}
```

### 3.4 间距系统

```scss
:root {
  --dt-spacing-0: 0;
  --dt-spacing-1: 4px;
  --dt-spacing-2: 8px;
  --dt-spacing-3: 12px;
  --dt-spacing-4: 16px;
  --dt-spacing-5: 20px;
  --dt-spacing-6: 24px;
  --dt-spacing-8: 32px;
  --dt-spacing-10: 40px;
  --dt-spacing-12: 48px;
}
```

---

## 四、组件设计规范

### 4.1 左侧导航栏（钉钉 7.6 新特性）

**关键特性**：
- 支持**拖动调整宽度**（56px - 80px）
- 导航项尺寸：48px × 48px
- 图标尺寸：24px × 24px
- 新增：🤖 AI助理入口（7.6.45+）

**状态规范**：
| 状态 | 背景色 | 图标色 | 指示条 |
|------|--------|--------|--------|
| 默认 | transparent | #858B8F | 无 |
| Hover | rgba(0, 0, 0, 0.04) | #5F6468 | 无 |
| 激活 | rgba(0, 137, 255, 0.1) | #0089FF | 左侧 3px |

```scss
.dt-nav-sidebar {
  width: var(--dt-nav-width);
  min-width: 56px;
  max-width: 80px;
  background: var(--dt-color-bg-primary);
  border-right: 1px solid var(--dt-color-border);
  display: flex;
  flex-direction: column;
  position: relative;

  // 拖动调整宽度的手柄
  &::after {
    content: '';
    position: absolute;
    right: -2px;
    top: 0;
    bottom: 0;
    width: 4px;
    cursor: col-resize;
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover::after {
    opacity: 1;
  }

  .nav-item {
    height: var(--dt-nav-item-height);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    cursor: pointer;
    position: relative;
    transition: all 0.15s ease;

    .nav-icon {
      font-size: 22px;
      color: var(--dt-color-text-tertiary);
      transition: color 0.15s ease;
    }

    .nav-label {
      font-size: 11px;
      color: var(--dt-color-text-tertiary);
    }

    &:hover {
      background: var(--dt-color-bg-hover);

      .nav-icon, .nav-label {
        color: var(--dt-color-text-secondary);
      }
    }

    &.active {
      background: var(--dt-color-bg-active);

      .nav-icon, .nav-label {
        color: var(--dt-color-primary);
      }

      // 左侧指示条
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        background: var(--dt-color-primary);
        border-radius: 0 2px 2px 0;
      }
    }

    // 徽标
    .nav-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      min-width: 16px;
      height: 16px;
      padding: 0 4px;
      background: #F5222D;
      color: #FFFFFF;
      font-size: 10px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
    }
  }

  // 底部分隔线
  .nav-divider {
    height: 1px;
    background: var(--dt-color-border);
    margin: 4px 12px;
  }
}
```

### 4.2 会话列表（可拖动调整宽度）

**关键特性**：
- 支持**拖动调整宽度**（240px - 400px）
- 会话项高度：64px
- 头像尺寸：40px × 40px
- 置顶/免打扰图标优化

```scss
.dt-session-panel {
  width: var(--dt-session-width);
  min-width: 240px;
  max-width: 400px;
  background: var(--dt-color-bg-secondary);
  border-right: 1px solid var(--dt-color-border);
  display: flex;
  flex-direction: column;

  // 拖动手柄
  &::after {
    content: '';
    position: absolute;
    right: -2px;
    top: 0;
    bottom: 0;
    width: 4px;
    cursor: col-resize;
  }

  .session-search {
    padding: 12px 16px;

    .search-input {
      width: 100%;
      height: 36px;
      padding: 0 12px 0 36px;
      background: var(--dt-color-bg-primary);
      border: 1px solid var(--dt-color-border);
      border-radius: 6px;
      font-size: 14px;

      &:focus {
        outline: none;
        border-color: var(--dt-color-primary);
        box-shadow: 0 0 0 2px var(--dt-color-primary-light);
      }
    }
  }

  .session-list {
    flex: 1;
    overflow-y: auto;

    .session-item {
      height: var(--dt-session-item-height);
      padding: 10px 12px;
      display: flex;
      gap: 10px;
      cursor: pointer;
      position: relative;
      transition: background 0.15s ease;

      &:hover {
        background: var(--dt-color-bg-hover);
      }

      &.active {
        background: var(--dt-color-bg-active);
      }

      // 置顶标识
      &.pinned::before {
        content: '';
        position: absolute;
        left: 0;
        top: 12px;
        bottom: 12px;
        width: 3px;
        background: var(--dt-color-primary);
        border-radius: 0 2px 2px 0;
      }

      .session-avatar {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        flex-shrink: 0;
        position: relative;

        .online-indicator {
          position: absolute;
          bottom: -1px;
          right: -1px;
          width: 10px;
          height: 10px;
          border: 2px solid var(--dt-color-bg-secondary);
          border-radius: 50%;

          &.online { background: var(--dt-color-success); }
          &.offline { background: var(--dt-color-text-quaternary); }
        }
      }

      .session-content {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        justify-content: center;
        gap: 4px;

        .session-top {
          display: flex;
          align-items: center;
          justify-content: space-between;

          .session-name {
            font-size: 14px;
            font-weight: 500;
            color: var(--dt-color-text-primary);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
          }

          .session-meta {
            display: flex;
            align-items: center;
            gap: 4px;
            flex-shrink: 0;

            .session-time {
              font-size: 12px;
              color: var(--dt-color-text-quaternary);
            }

            .mute-icon {
              font-size: 14px;
              color: var(--dt-color-text-tertiary);
            }
          }
        }

        .session-bottom {
          display: flex;
          align-items: center;
          justify-content: space-between;

          .session-preview {
            font-size: 13px;
            color: var(--dt-color-text-tertiary);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
          }

          .unread-badge {
            min-width: 18px;
            height: 18px;
            padding: 0 5px;
            background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
            color: #FFFFFF;
            font-size: 11px;
            font-weight: 600;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 9px;
            flex-shrink: 0;
          }
        }
      }
    }
  }
}
```

### 4.3 聊天区域

```scss
.dt-chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--dt-color-bg-secondary);

  // === 聊天头部 ===
  .chat-header {
    height: var(--dt-header-height);
    background: var(--dt-color-bg-primary);
    border-bottom: 1px solid var(--dt-color-border);
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-shrink: 0;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .chat-title {
        font-size: 16px;
        font-weight: 500;
        color: var(--dt-color-text-primary);

        .member-count {
          font-size: 14px;
          color: var(--dt-color-text-tertiary);
          margin-left: 4px;
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 8px;

      .header-btn {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: var(--dt-color-text-tertiary);
        border-radius: 4px;

        &:hover {
          background: var(--dt-color-bg-hover);
          color: var(--dt-color-text-secondary);
        }
      }
    }
  }

  // === 消息区域 ===
  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .message-item {
      display: flex;
      gap: 8px;

      &.sent {
        flex-direction: row-reverse;
      }

      .message-avatar {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        flex-shrink: 0;
      }

      .message-content {
        max-width: 60%;
        min-width: 0;

        .sender-name {
          font-size: 12px;
          color: var(--dt-color-text-tertiary);
          margin-bottom: 4px;
          margin-left: 2px;
        }

        // === 消息气泡（带 Tail） ===
        .message-bubble {
          position: relative;
          padding: 10px 14px;
          font-size: 14px;
          line-height: 1.6;
          word-break: break-word;

          // 接收方气泡
          &.received {
            background: var(--dt-bubble-received-bg);
            border: 1px solid var(--dt-bubble-received-border);
            border-radius: var(--dt-radius-bubble);
            border-bottom-left-radius: var(--dt-radius-bubble-tail);
            color: var(--dt-bubble-received-text);

            // Tail
            &::before {
              content: '';
              position: absolute;
              left: -7px;
              top: 14px;
              width: 0;
              height: 0;
              border-style: solid;
              border-width: 6px 7px 6px 0;
              border-color: transparent var(--dt-bubble-received-bg) transparent transparent;
            }

            &::after {
              content: '';
              position: absolute;
              left: -8px;
              top: 14px;
              width: 0;
              height: 0;
              border-style: solid;
              border-width: 6px 7px 6px 0;
              border-color: transparent var(--dt-bubble-received-border) transparent transparent;
            }
          }

          // 发送方气泡
          &.sent {
            background: var(--dt-bubble-sent-bg);
            border-radius: var(--dt-radius-bubble);
            border-bottom-right-radius: var(--dt-radius-bubble-tail);
            color: var(--dt-bubble-sent-text);

            // Tail
            &::before {
              content: '';
              position: absolute;
              right: -7px;
              top: 14px;
              width: 0;
              height: 0;
              border-style: solid;
              border-width: 6px 0 6px 7px;
              border-color: transparent transparent transparent var(--dt-bubble-sent-bg);
            }
          }
        }
      }

      .message-status {
        display: flex;
        align-items: flex-end;
        gap: 4px;
        margin-left: 4px;

        .status-icon {
          font-size: 14px;
          color: var(--dt-color-text-quaternary);

          &.read { color: var(--dt-color-primary); }
        }

        .send-time {
          font-size: 12px;
          color: var(--dt-color-text-quaternary);
        }
      }
    }
  }

  // === 输入区域 ===
  .chat-input-area {
    height: var(--dt-input-height);
    background: var(--dt-color-bg-primary);
    border-top: 1px solid var(--dt-color-border);
    flex-shrink: 0;
    display: flex;
    flex-direction: column;

    .input-toolbar {
      height: 36px;
      padding: 0 12px;
      display: flex;
      align-items: center;
      gap: 4px;

      .toolbar-btn {
        width: 28px;
        height: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: var(--dt-color-text-tertiary);
        border-radius: 4px;

        &:hover {
          background: var(--dt-color-bg-hover);
        }
      }
    }

    .input-content {
      flex: 1;
      padding: 0 12px;

      .message-input {
        width: 100%;
        height: 100%;
        border: none;
        outline: none;
        font-size: 14px;
        line-height: 1.6;
        color: var(--dt-color-text-primary);
        resize: none;
        background: transparent;

        &::placeholder {
          color: var(--dt-color-text-placeholder);
        }
      }
    }

    .input-footer {
      height: 36px;
      padding: 0 12px;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .input-hint {
        font-size: 12px;
        color: var(--dt-color-text-quaternary);
      }

      .send-btn {
        padding: 6px 16px;
        background: var(--dt-color-primary);
        color: #FFFFFF;
        font-size: 14px;
        border: none;
        border-radius: 4px;
        cursor: pointer;

        &:hover {
          background: var(--dt-color-primary-hover);
        }
      }
    }
  }
}
```

### 4.4 AI助理入口（钉钉 7.6.45+ 新增）

```scss
// AI助理按钮
.nav-item.ai-assistant {
  .nav-icon {
    background: linear-gradient(135deg, #722ED1 0%, #9254DE 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  &.active::before {
    background: linear-gradient(135deg, #722ED1 0%, #9254DE 100%);
  }
}

// AI助理面板
.dt-ai-panel {
  width: 320px;
  background: var(--dt-color-bg-primary);
  border-left: 1px solid var(--dt-color-border);

  .ai-header {
    padding: 16px;
    border-bottom: 1px solid var(--dt-color-border);

    .ai-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: var(--dt-color-text-primary);

      .ai-icon {
        width: 24px;
        height: 24px;
        background: linear-gradient(135deg, #722ED1 0%, #9254DE 100%);
        border-radius: 6px;
      }
    }
  }

  .ai-content {
    padding: 16px;

    .ai-welcome {
      margin-bottom: 20px;

      .welcome-text {
        font-size: 14px;
        color: var(--dt-color-text-secondary);
        line-height: 1.6;
      }
    }

    .ai-quick-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .quick-action {
        padding: 6px 12px;
        background: var(--dt-color-bg-secondary);
        border-radius: 16px;
        font-size: 13px;
        color: var(--dt-color-text-secondary);
        cursor: pointer;

        &:hover {
          background: var(--dt-color-bg-tertiary);
        }
      }
    }
  }
}
```

---

## 五、响应式设计

钉钉 7.6 强调**响应式设计**，适配各种屏幕尺寸：

```scss
// 断点定义
$dt-breakpoint-sm: 1280px;
$dt-breakpoint-md: 1440px;
$dt-breakpoint-lg: 1680px;

@media (max-width: $dt-breakpoint-sm) {
  .dt-session-panel {
    width: 240px;
  }
}

@media (max-width: 1366px) {
  .dt-nav-sidebar {
    width: 56px;

    .nav-label {
      display: none;
    }
  }
}
```

---

## 六、动效规范

钉钉 7.6 的动效强调**微妙**和**流畅**：

```scss
// 过渡时长
$dt-duration-fast: 150ms;
$dt-duration-base: 200ms;
$dt-duration-slow: 300ms;

// 缓动函数
$dt-ease-out: cubic-bezier(0, 0, 0.2, 1);
$dt-ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);

// 通用过渡
* {
  transition-property: color, background-color, border-color;
  transition-timing-function: $dt-ease-out;
  transition-duration: $dt-duration-fast;
}
```

---

## 七、实施计划

### 阶段 0：设计 Token 统一（1-2 天）
- [ ] 更新 design-tokens.scss 为钉钉 7.6 风格
- [ ] 新增 AI助理相关 Token
- [ ] 统一命名前缀为 `--dt-*`

### 阶段 1：导航栏重构（2-3 天）
- [ ] 实现可拖动调整宽度功能
- [ ] 新增 AI助理入口
- [ ] 优化导航项状态样式

### 阶段 2：会话列表优化（2-3 天）
- [ ] 实现可拖动调整宽度功能
- [ ] 优化会话项样式
- [ ] 添加置顶/免打扰图标

### 阶段 3：消息气泡完美复刻（2-3 天）
- [ ] 实现 Tail 效果（CSS 伪元素三角形）
- [ ] 优化气泡圆角和阴影
- [ ] 添加消息 hover 快捷操作条

### 阶段 4：整体布局调整（3-5 天）
- [ ] 调整整体布局结构
- [ ] 修复响应式问题
- [ ] 优化滚动条样式

---

## 八、验收标准

### UI 还原度要求
- 主框架布局：**100%** 一致
- 导航栏样式：**95%+** 一致
- 消息气泡：**100%** 一致（含 Tail）
- 整体色彩：**100%** 一致
- 交互细节：**90%+** 一致

### 功能要求
- [ ] 导航栏可拖动调整宽度
- [ ] 会话列表可拖动调整宽度
- [ ] AI助理入口
- [ ] 消息 hover 快捷操作条

---

## 参考资料

- [钉钉 7.6.45 版本新功能](https://alidocs.dingtalk.com/i/p/Y7kmbokZp3pgGLq2/docs/93NwLYZXWyglyKrXCoMB5XzkJkyEqBQm)
- [钉钉 7.6 版本新功能](https://alidocs.dingtalk.com/i/p/Y7kmbokZp3pgGLq2/docs/P7QG4Yx2Jp7NaGO4sz6qRPZlV9dEq3XD)
- [2025 B端设计趋势｜风格&质感](https://page.dingtalk.com/wow/dingtalk/default/dingtalk/9ijIiD4sqjwY0126)
- [2025 B端设计趋势｜个性化](https://page.dingtalk.com/wow/dingtalk/default/dingtalk/JVNYHDarV0121)
- [钉钉官网下载页面](https://www.dingtalk.com/download)
