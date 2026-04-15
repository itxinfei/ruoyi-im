---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - "docs/需求文档.md"
  - "docs/UI对标设计规范.md"
  - "docs/项目总规划-钉钉对齐V1.md"
  - "docs/plans/2025-01-25-full-architecture.md"
  - "docs/Java+Vue双端开发规范.md"
project: im
author: Itxinfei
date: 2026-03-04
status: completed
focus: IM组件对齐
lastStep: 8
---

# UX Design Specification for Cloud Document Feature

**Project:** RuoYi-IM
**Author:** Itxinfei
**Date:** 2026-03-04
**Status:** In Progress

---

## Document Overview

This UX design specification focuses on optimizing and enhancing the Cloud Document feature within the RuoYi-IM system. The goal is to create a fluid, intuitive, and practical document collaboration experience deeply integrated with the instant messaging platform.

### Design Philosophy

> "Documents are not just files - they are extensions of conversations."

In an IM context, documents serve as carriers for messages, starting points for collaboration, and repositories for knowledge. The design should make users feel that documents and chat are seamlessly integrated, with no sense of fragmentation when switching between them.

### Key Design Goals

1. **流畅 (Fluid)** - Seamless interactions, instant feedback, zero friction
2. **好用 (Usable)** - Low learning curve, intuitive navigation, smart defaults
3. **实用 (Practical)** - Office-focused features, real collaboration value

---

## Executive Summary

### Project Vision

RuoYi-IM is an enterprise-grade instant messaging and collaboration platform with deep integration between chat and document management. The vision is to create a fluid, intuitive document collaboration experience that feels like a natural extension of conversations, not a separate tool. By leveraging the existing IM infrastructure, the platform aims to provide real-time collaboration, seamless integration with messaging, and practical document management features for office scenarios.

### Target Users

**Primary Users:**
- Project Managers: Need to coordinate teams, track progress, and review documents quickly
- Team Members: Daily office work requiring real-time document editing, @mentions, and quick feedback
- Department Managers: Approval workflows, report reviewing, and data security
- Mobile Workers: Need to view documents anytime, anywhere with simple editing capabilities

**User Characteristics:**
- Desktop-first usage (PC office scenarios)
- Intermediate technical proficiency
- Expect "out-of-the-box" usability with low learning curve
- Work in teams of 2-100 people

### Key Design Challenges

1. **Fluid Real-time Collaboration Perception**
   - Conflict resolution during multi-user editing
   - Natural presentation of real-time cursors and edit states
   - Graceful degradation under network latency

2. **Seamless IM-Document Integration**
   - Zero-friction switching between chat and documents
   - Natural presentation of document comments in IM
   - Smooth conversion of messages to documents

3. **Efficient Large-Scale Document Management**
   - Intelligent classification and search
   - Flexible folder structure organization
   - Clear version history tracing

4. **Consistent Multi-Platform Experience**
   - Balance between desktop full features and mobile lightweight
   - Real-time and accurate state synchronization
   - Fluid cross-device collaboration

### Design Opportunities

1. **Intelligent Collaboration Awareness**
   - Predict user intent and pre-load relevant content
   - Smart recommendation of collaborators and documents
   - Adaptive interface (single-user editing vs. multi-user collaboration)

2. **Emotional Design**
   - Micro-interactions to increase collaboration delight
   - Instant feedback for successful operations
   - Friendly error handling and recovery

3. **Efficiency Enhancement**
   - Comprehensive keyboard shortcut system
   - Optimized batch operations
   - Intelligent search and filtering capabilities

---

## Core User Experience

### Defining Experience

The core user experience for RuoYi-IM Cloud Documents is defined by seamless integration between instant messaging and document collaboration. The primary user action is **quickly viewing and commenting on documents directly within IM conversations** without leaving the chat context.

This core action serves as the foundation because:
- It connects the two primary systems (IM and Documents)
- It's the most frequent user interaction
- If this experience is poor, the value of other features diminishes significantly

The experience follows a simple flow: **Receive document link in IM → Click → Document opens in side panel within 0.5 seconds → Add/view comments → Continue conversation** - all without context switching.

### Platform Strategy

**Primary Platform:**
- Desktop web browser (PC office scenarios)
- Mouse/keyboard optimized interaction
- Chrome/Edge primary support, Safari/Firefox secondary

**Desktop-Only Focus:** No mobile adaptation required - pure desktop-first approach for maximum efficiency.

**Platform Requirements:**
- Browser-based deployment (no desktop app needed)
- Drag-and-drop file upload support
- Comprehensive keyboard shortcut system
- WebSocket support for real-time collaboration

**Input Methods:**
- Mouse: Primary for navigation and selection
- Keyboard: Primary for text editing and shortcuts

### Effortless Interactions

**Interactions that should feel completely natural:**

| Interaction | Current Friction | Effortless Experience |
|-------------|------------------|----------------------|
| **Open document from IM** | Switch windows, login to separate system | Click link → Document opens in side panel in 0.5s |
| **Add comments** | Enter document editor | Add comments directly in IM preview panel |
| **Collaboration awareness** | No visibility of others' edits | Real-time cursors and avatars visible |
| **File upload** | Multi-step process | Drag file to chat → Auto-create document |
| **Search documents** | Navigate to document management page | Search documents directly in IM |

**Automatic operations:**
- Auto-save on every edit
- Instant comment notifications to stakeholders
- Real-time online status updates
- Automatic version history tracking

### Critical Success Moments

**"This is better" moments:**
1. **Lightning-fast document access** - Click document link in IM, document opens instantly in side panel without leaving conversation
2. **Instant comment collaboration** - Add comment, colleague receives notification and can quickly jump to document location
3. **Face-to-face collaboration feeling** - See real-time cursors of colleagues during multi-user editing

**User success moments:**
- Quick discovery of needed documents
- Successful collaborative editing completion
- Timely response to comments
- Clear and traceable version history

**Experience-breaking failures:**
- Slow or laggy document loading
- Comment synchronization failure or data loss
- Conflicts during multi-user editing
- Confusing or lost version history

**First-time user success:**
- First document opened from IM with successful comment added
- First real-time collaborative editing session
- First successful version history restoration

### Experience Principles

Guiding principles that will inform all UX decisions:

1. **Seamless Integration Principle**
   - Documents and IM are completely integrated
   - No fragmentation when switching between systems
   - Context is preserved across transitions

2. **Instant Feedback Principle**
   - Every action has immediate feedback
   - Users feel in control at all times
   - Loading states are clear and communicative

3. **Collaboration Awareness Principle**
   - Users are aware of others' presence during collaboration
   - Real-time visibility of remote actions
   - Natural social cues in digital collaboration

4. **Efficiency Priority Principle**
   - Common actions accessible within 2 clicks
   - Reduce operational steps wherever possible
   - Keyboard shortcuts for power users

---

## Desired Emotional Response

### Primary Emotional Goals

**"Flow, Efficiency, Connection"**

The primary emotional goal for RuoYi-IM Cloud Documents is to create a fluid, efficient, and connected collaboration experience where users feel the tool is a natural extension of their workflow rather than a burden. Users should feel empowered, productive, and seamlessly connected to their team.

**Key Emotional States:**

| Emotion | Description | Why It Matters |
|---------|-------------|----------------|
| **Efficiency** | "I completed the task faster than expected" | Core value: improving workplace productivity |
| **Control** | "I know what I'm doing, everything goes as expected" | Eliminates anxiety, builds trust |
| **Connection** | "I'm working closely with my team, like we're in the same room" | Core value of deep IM integration |
| **Delight** | "Wow, this feature is so convenient!" | Creates differentiated experience |
| **Security** | "My data is safe, nothing will be lost" | Fundamental requirement for enterprise applications |

**What users will tell friends:**
"This system's documents and chat are seamlessly integrated. I can quickly view and comment on documents without switching windows. It's so convenient!"

### Emotional Journey Mapping

| Stage | User Scenario | Desired Emotion | Design Support |
|-------|---------------|-----------------|----------------|
| **Discovery** | First seeing document link in IM | Curiosity + Anticipation | Clear visual cues, guided actions |
| **Core Experience** | Click link, open document, add comment | Flow + Efficiency | Fast loading, intuitive operations |
| **Task Completion** | Comment added, colleague notified | Accomplishment + Connection | Instant feedback, collaboration awareness |
| **Exception Handling** | Network disconnected, document failed to load | Security + Trust | Friendly prompts, recovery guidance |
| **Re-engagement** | Second time using document features | Familiarity + Efficiency | Remember preferences, quick access |

### Micro-Emotions

**Critical micro-emotional states for success:**

| Micro-emotion | Selection | Rationale |
|---------------|-----------|-----------|
| **Confidence vs. Confusion** | Confidence | Clear interface, intuitive operations |
| **Trust vs. Skepticism** | Trust | Data security, reliable operations |
| **Excitement vs. Anxiety** | Excitement | Discovering new features, pleasant collaboration |
| **Accomplishment vs. Frustration** | Accomplishment | Tasks completed successfully |
| **Delight vs. Satisfaction** | Delight | Exceeds expectations, surprisingly easy to use |
| **Belonging vs. Isolation** | Belonging | Tight team collaboration |

**Most critical emotional states:**
1. **Control** - Users feel everything is under their control
2. **Efficiency** - Tasks completed faster than expected
3. **Connection** - Seamless collaboration with team

### Design Implications

**Emotion-Design Connections:**

- **Control** →
  - Clear visual hierarchy and navigation
  - Instant operation feedback
  - Reversible action design
  - Progress indicators and status prompts

- **Efficiency** →
  - Fast loading (document opens in < 1s)
  - Intelligent search and filtering
  - Batch operation support
  - Comprehensive keyboard shortcut system

- **Connection** →
  - Real-time collaboration awareness (cursors, online status)
  - Instant comment notifications
  - @mentions and quick replies
  - Collaborator avatars and names display

- **Delight** →
  - Smart recommendations (documents, collaborators)
  - Micro-interactions and animation effects
  - Discovery of hidden advanced features
  - Unexpectedly convenient operations

- **Security** →
  - Auto-save prompts
  - Traceable version history
  - Clear permission controls
  - Friendly error recovery

**Interactions that create negative emotions:**
- ❌ Slow loading → Anxiety, frustration
- ❌ Operation failure → Skepticism, distrust
- ❌ Complex flows → Confusion, abandonment
- ❌ Data loss → Anger, fear

**Moments to add delight and surprise:**
- ✨ Smooth animation when document opens
- ✨ Instant feedback when comment sent successfully
- ✨ Smart suggestions when @mentioning
- ✨ Visual prompts when collaborators come online

### Emotional Design Principles

Guiding principles for emotional design decisions:

1. **Positive Reinforcement Principle**
   - Celebrate user successes with visual and emotional feedback
   - Make accomplishments visible and rewarding
   - Create moments of delight in routine tasks

2. **Trust Building Principle**
   - Be transparent about system state and actions
   - Provide clear error messages and recovery paths
   - Design for reliability and consistency

3. **Connection Enhancement Principle**
   - Make remote collaboration feel personal and immediate
   - Humanize digital interactions with social cues
   - Foster team belonging through shared experiences

4. **Efficiency Celebration Principle**
   - Highlight time saved and tasks completed
   - Reward efficiency with faster workflows
   - Make productivity improvements visible

5. **Anxiety Reduction Principle**
   - Eliminate uncertainty through clear communication
   - Provide guidance at decision points
   - Design for graceful failure and easy recovery

---

## UX Pattern Analysis & Inspiration

### Inspiring Products Analysis

**Reference Product: 钉钉 (DingTalk)**

钉钉是国内企业 IM 领域的 UX 标杆产品，其消息模块设计值得深入借鉴。

**钉钉消息模块的核心 UX 优势：**

| 维度 | 钉钉做得好的是什么 |
|------|------------------|
| **会话切换** | 侧边栏 + 多窗口/面板设计，切换对话不丢上下文 |
| **消息分组** | 按日期/时间分组，查找历史记录清晰 |
| **未读处理** | 红点 + 数字角标混合，未读数过多时显示"99+" |
| **快速操作** | 左滑快捷操作（置顶、免打扰、删除），高效 |
| **输入体验** | 表情、文件、截图工具随手可得，不打断思路 |
| **消息状态** | 发送中/已发送/已读/失败，状态明确 |

### Transferable UX Patterns

#### 1. 高效会话列表

**信息密度设计：**
```
┌─────────────────────────────────────────────┐
│ [头像] 联系人/群名              [时间]      │
│        最新消息预览...        [未读数徽标]  │
└─────────────────────────────────────────────┘
```

**设计智慧：**
- **黄金比例布局**：左侧头像占 12%，右侧内容占 88%
- **垂直密度**：每行高度固定 64-72px，内容越多行高不变
- **时间归类**：当日消息显示具体时间，跨日显示"昨天/周一"等语义化时间
- **未读层级**：数字角标 > 红点 > 无标识，递减的信息优先级

#### 2. 呼吸感气泡

**气泡设计特点：**

| 元素 | 钉钉做法 | 效果 |
|------|----------|------|
| **内边距** | 上下 8px，左右 12px | 文字不贴边 |
| **圆角** | 8px（接收）/ 4px 8px 8px 8px（发送） | 区分方向，柔和感 |
| **间距** | 气泡间距 4px，连续消息压缩到 2px | 不拥挤，有节奏 |
| **文字** | 14px，行高 1.5 | 易读不累 |
| **最长宽度** | 70-75% 屏幕宽度 | 避免单行过长 |

**连续消息处理：**
```
[A]: 消息A
[A]: 消息B（头像重叠，间距压缩）
[A]: 消息C（头像只显示一次）
```

#### 3. 克制快捷操作

**左滑操作设计：**
- **操作数量**：最多 3 个，避免选择困难
- **操作类型**：高频刚需（置顶、免打扰、删除）+ 可扩展
- **触发方式**：左滑而非长按，减少误触
- **视觉反馈**：滑动过半才显示操作按钮，克制但明确

#### 4. 消息状态可视化

**状态流转清晰：**
- **发送中**：灰色转圈，用户知道"正在努力发送"
- **已发送**：单灰色勾，内容安全到达服务器
- **已读**：双蓝色勾，阅读者看到了（社交确认感）

#### 5. 智能分类组织

**会话列表分类：**
- **固定分类 + 动态数量**：每个分类下实时显示未读总数
- **折叠/展开**：用户可收起不需要的分类
- **分类优先级**：置顶 > 单聊 > 群聊 > 订阅号（符合工作场景）

#### 6. 搜索即时满足

**搜索体验：**
- **搜索即所见**：输入关键词同时显示候选结果
- **历史搜索**：快速复用，提升效率
- **分类联想**：同时联想人名、群名、文档

#### 7. 工具箱思维

**输入区工具栏：**
- **固定 6 个高频工具**：表情、文件、图片、点赞、文档 + 可扩展
- **图标按下态**：轻微缩放 + 颜色变化
- **发送按钮突出**：尺寸最大、颜色最醒目

#### 8. 优雅降级

**网络状态处理：**
- **非侵入式提示**：顶部黄条，不打断
- **发送失败标识**：气泡右下角红色感叹号
- **自动重连**：透明进度，用户无感修复

### Anti-Patterns to Avoid

**RuoYi-IM 应避免的 UX 坑：**

| Anti-pattern | 问题 | 解决方案 |
|-------------|------|----------|
| **会话列表高度不固定** | 忽高忽低，扫描不稳定 | 固定 56px 行高 |
| **图标大小不一** | 视觉不统一，粗糙感 | 统一 20px 图标 |
| **间距随机** | 7px、10px、13px 等乱用 | 强制 8px 栅格 |
| **缺少 min-width: 0** | Flex 撑爆布局 | 必须添加 |
| **气泡过宽** | 单行文字过长，阅读疲劳 | 最大 70% |
| **连续消息无压缩** | 对话太松散 | 压缩到 4px |
| **移动端布局通吃** | 触摸目标太小 | 桌面端 Only |

### Design Inspiration Strategy

**借鉴策略总结：**

| # | 模式名称 | 核心价值 | 应用场景 |
|---|----------|----------|----------|
| 1 | **高效会话列表** | 快速定位、扫读无压力 | SessionList 组件 |
| 2 | **呼吸感气泡** | 阅读舒适、区分你我 | ChatMessageBubble |
| 3 | **克制快捷操作** | 高效不杂乱、减少误触 | 会话项右滑操作 |
| 4 | **消息状态可视化** | 降低焦虑，预期明确 | ChatMessageBubble 状态图标 |
| 5 | **智能分类组织** | 快速定位会话 | SessionList 分类设计 |
| 6 | **搜索即时满足** | 减少操作步骤 | SearchPanel 交互优化 |
| 7 | **工具箱思维** | 高效不拥挤 | ChatInputArea 工具栏 |
| 8 | **优雅降级** | 建立信任感 | 网络状态处理机制 |

**What to Adopt:**
- 56px 固定行高系统
- 8px 栅格对齐
- 40px 头像 + 12px 间距
- 气泡 8px 12px padding
- 连续消息 4px 压缩

**What to Adapt:**
- 钉钉移动端布局 → RuoYi-IM 桌面端 Only
- 钉钉深色模式 → 直接复用其颜色变量

**What to Avoid:**
- 钉钉部分功能的过度动画
- 钉钉复杂的深色模式切换动效

---

## Desktop Layout System (Desktop-Only)

### Core Layout Specifications

**桌面端布局规格（基于钉钉标准）：**

```
┌─────────────────────────────────────────────────────────┐
│                    桌面端布局                            │
├─────────────────────────────────────────────────────────┤
│  会话项高度:     56px (固定)                            │
│  头像尺寸:       40×40px, 圆角 4px                     │
│  头像右边距:     12px                                  │
│  会话项内边距:   16px (左/右)                          │
│  信息密度:       中等偏紧                               │
│  栅格系统:       8px (强制)                            │
│  连续消息:       头像折叠 + 间距压缩到 4px             │
│  气泡最大宽度:   70%                                   │
│  工具栏高度:     48px                                  │
│  工具栏图标:     20px, 间距 8px                        │
│  侧边导航宽度:   64px                                  │
└─────────────────────────────────────────────────────────┘
```

### Layout Checklist

| 检查项 | 标准值 | 依据 |
|--------|--------|------|
| 会话项高度 | **56px** | 钉钉标准 |
| 头像尺寸 | **40×40px** | 钉钉标准 |
| 头像圆角 | **4px** | `--dt-radius-sm` |
| 头像右边距 | **12px** | 钉钉标准 |
| 会话项 padding | **16px** | 栅格对齐 |
| 气泡 padding | **8px 12px** | 钉钉标准 |
| 气泡最大宽度 | **70%** | 钉钉标准 |
| 发送方圆角 | **4px 8px 8px 8px** | 右上小角 |
| 接收方圆角 | **8px 4px 8px 8px** | 左上小角 |
| 侧边导航宽度 | **64px** | 钉钉标准 |
| 导航项高度 | **48px** | 钉钉标准 |
| 工具栏高度 | **48px** | 钉钉标准 |
| 工具栏图标 | **20px** | 钉格标准 |
| 连续消息间距 | **4px** | 钉钉标准 |
| 间距只允许 | **4/8/12/16/24/32** | 8px 栅格 |

### Design Token Quick Reference

```scss
// 布局间距
--dt-spacing-xs: 4px;
--dt-spacing-sm: 8px;
--dt-spacing-md: 12px;  // 注意：非标准，优先用 12
--dt-spacing-lg: 16px;
--dt-spacing-xl: 24px;
--dt-spacing-xxl: 32px;

// 头像
--dt-avatar-size-lg: 40px;   // 会话列表
--dt-avatar-size-md: 36px;   // 消息气泡/侧边栏
--dt-radius-sm: 4px;

// 高度
--dt-header-height: 56px;
--dt-session-item-height: 56px;
--dt-toolbar-height: 48px;
--dt-nav-sidebar-width: 64px;

// 组件专用
--dt-bubble-padding-v: 8px;
--dt-bubble-padding-h: 12px;
--dt-bubble-max-width: 70%;
```

### Component Layout Specifications

#### SessionList (会话列表)

```scss
.session-item {
  display: flex;
  align-items: center;     // 垂直居中
  height: 56px;            // 固定高度
  padding: 0 16px;          // 左右边距
  gap: 12px;               // 头像右边距
}

.session-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;          // 头像不压缩
  border-radius: var(--dt-radius-sm);
}

.session-info {
  flex: 1;
  min-width: 0;            // 防止文字撑爆
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.session-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;          // 时间/徽标不压缩
}
```

#### ChatMessageBubble (消息气泡)

```scss
// 连续消息压缩
.message--compact {
  margin-top: -4px;         // 抵消 4px 间距
}

// 气泡样式
.message-bubble {
  padding: 8px 12px;
  max-width: 70%;
  border-radius: 8px;      // 接收方
}

.message-bubble--sent {
  border-radius: 4px 8px 8px 8px;  // 发送方：右上小角
}

.message-bubble--received {
  border-radius: 8px 4px 8px 8px;  // 接收方：左上小角
}
```

#### Input Toolbar (输入工具栏)

```scss
.input-toolbar {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 12px;
  gap: 8px;
}

.toolbar-icon {
  width: 32px;
  height: 32px;             // 图标容器
  padding: 6px;             // 图标实际 20×20
  border-radius: 8px;
  cursor: pointer;
}

.send-button {
  width: 64px;
  height: 32px;
  border-radius: 4px;
}
```

### Party Mode Insights (Party Mode 智能体讨论)

**参与智能体：** Winston (架构师), Amelia (开发者), Sally (UX设计师)

**三方共识：**

| 议题 | Winston | Amelia | Sally | 共识 |
|------|---------|--------|-------|------|
| CSS 变量化 | ✅ 强烈推荐 | ✅ 赞同 | ✅ 赞同 | **✅ 三方共识** |
| 布局间距 | 固定值 OK | 变量化优先 | 56px 固定 | **✅ 56px + 变量** |
| 深色模式 | 颜色变量化 | 实现方案 OK | 担忧视觉 | **✅ 布局不变** |
| 迁移策略 | 渐进式 | 不要一次性重写 | - | **✅ 渐进迁移** |

**实现建议：**

1. **先盘点**：检查 design-tokens.scss 现有变量
2. **补缺失**：添加 RuoYi-IM 专用布局变量
3. **渐进式**：新组件用变量，旧组件逐步迁移
4. **验证**：确保深色模式下布局一致性

**深色模式建议：**

| 元素 | 浅色模式 | 深色模式建议 |
|------|----------|--------------|
| 背景色 | #FFFFFF | #1F1F1F（参考钉钉） |
| 文字色 | #171A1D | #E6E6E6 |
| 次要文字 | #666666 | #999999 |
| 边框色 | #E5E7EB | #3A3A3A |
| 会话 hover | #F5F5F5 | #2A2A2A |
| 会话 active | #E6F2FF | #1A3A5C |

---

## Design System Foundation

### Design System Choice

**Element Plus + Custom Design Tokens 混合方案**

基于 RuoYi-IM 项目现状（已有 Element Plus + design-tokens.scss），选择深化现有技术栈的方案，而非从头构建全新设计系统。

### Rationale for Selection

| 考量因素 | 结论 | 原因 |
|----------|------|------|
| **开发速度** | Element Plus | 开箱即用，减少重复造轮子 |
| **钉钉对齐** | Custom Tokens | 已有 design-tokens.scss 基础 |
| **IM 核心体验** | Custom 组件 | ChatBubble, SessionList 等需要深度定制 |
| **维护成本** | CSS 变量化 | 便于迭代和主题切换 |
| **团队效率** | 渐进式迁移 | 新组件用变量，旧组件逐步迁移 |

### Implementation Approach

```
┌─────────────────────────────────────────────────────────┐
│              RuoYi-IM 设计系统层次                       │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Layer 1: Element Plus (基础组件库)                     │
│  ├─ el-button, el-input, el-dialog                     │
│  ├─ el-dropdown, el-tabs, el-table                     │
│  └─ 通过 SCSS 变量覆盖主题                              │
│                                                         │
│  Layer 2: IM 专用组件 (自建)                           │
│  ├─ ChatWindow / ChatMessageBubble                    │
│  ├─ SessionList / SessionItem                          │
│  ├─ ChatInputArea / Toolbar                           │
│  └─ 钉钉风格深度定制                                    │
│                                                         │
│  Layer 3: Design Tokens (钉钉 8.2 Token)               │
│  ├─ Colors: --dt-brand-color, --dt-success-color...    │
│  ├─ Spacing: --dt-spacing-sm/lg/xl...                 │
│  ├─ Typography: --dt-font-size-*, --dt-radius-*...     │
│  └─ Shadows: --dt-shadow-*                             │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Component Strategy

| 组件类型 | 来源 | 定制程度 | 示例 |
|----------|------|----------|------|
| **通用组件** | Element Plus | 主题覆盖 | Button, Input, Dialog |
| **IM 核心组件** | Custom | 100% 自建 | MessageBubble, SessionItem |
| **IM 容器组件** | Element Plus + Custom | 混合 | ChatWindow, ChatSidebar |
| **业务组件** | Custom | 按需自建 | DocumentCard, ApprovalCard |

### Design Token 优先级

**第一优先级（已有基础，强化）：**

```scss
--dt-brand-color: #277EFB;           // 品牌蓝
--dt-success-color: #00B42A;          // 成功绿
--dt-warning-color: #FF7D00;          // 警告黄
--dt-error-color: #F53F3F;            // 危险红
```

**第二优先级（补充缺失）：**

```scss
--dt-session-item-height: 56px;        // 会话项高度
--dt-avatar-session: 40px;             // 会话头像
--dt-bubble-padding-v: 8px;             // 气泡垂直padding
--dt-bubble-padding-h: 12px;           // 气泡水平padding
```

**第三优先级（按需添加）：**

```scss
--dt-layout-header-height: 56px;
--dt-layout-nav-width: 64px;
--dt-toolbar-height: 48px;
```

---

## Implementation Roadmap

### Phase 1: Design Token 完善

**任务清单：**

- [ ] 检查 design-tokens.scss 现有变量
- [ ] 补充缺失的布局变量
- [ ] 验证所有颜色变量已定义
- [ ] 创建深色模式变量覆盖

### Phase 2: 核心组件对齐 ✅

**任务清单：**

- [x] SessionList 组件 56px 行高改造
- [x] ChatMessageBubble 气泡样式对齐
- [x] ChatInputArea 工具栏规范化
- [x] 侧边导航 ImSideNavNew 验证

### Phase 3: IM 体验优化 ✅

**任务清单：**

- [x] 消息状态可视化（发送中/已发送/已读）
- [x] 连续消息压缩效果
- [x] 会话列表分类折叠
- [x] 搜索面板即时反馈

---

<!-- Phase 3: IM体验优化 - 全部完成 (2026-04-10) -->
<!-- - 消息状态可视化 ✅ -->
<!-- - 连续消息压缩效果 ✅ -->
<!-- - 会话列表分类折叠 ✅ -->
<!-- - 搜索面板即时反馈 ✅ -->
