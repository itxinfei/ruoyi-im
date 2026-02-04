# 钉钉聊天界面对齐实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 对齐IM系统聊天界面与通用IM（钉钉/微信）的设计规范，包括消息气泡样式、聊天头部、输入框、详情面板等核心组件。

**架构:** 前端Vue 3组件样式调整，基于现有design-tokens.scss设计系统，使用CSS变量实现主题化。不涉及后端API修改。

**Tech Stack:** Vue 3 (Composition API), SCSS, Element Plus, Vite

---

## 前置检查

### 现有文件位置
- 设计令牌: `ruoyi-im-web/src/styles/design-tokens.scss`
- 消息项组件: `ruoyi-im-web/src/components/Chat/MessageItemRefactored.vue`
- 消息气泡组件: `ruoyi-im-web/src/components/Chat/MessageBubbleRefactored.vue`
- 聊天头部: `ruoyi-im-web/src/components/Chat/ChatHeader.vue`
- 输入框组件: `ruoyi-im-web/src/components/Chat/MessageInputRefactored.vue`
- 群组详情弹窗: `ruoyi-im-web/src/components/Chat/GroupDetailDialog.vue`
- 用户详情弹窗: `ruoyi-im-web/src/components/Chat/UserDetailDialog.vue`

### 关键设计变量（已存在）
```scss
--dt-bubble-left-bg: #FFFFFF;
--dt-bubble-left-border: #E4E7ED;
--dt-bubble-right-bg: #E8F4FF;
--dt-avatar-size: 48px;
--dt-chat-header-height: 52px;
--dt-message-max-width: 520px;
```

---

## 实施阶段

### 阶段一：消息气泡核心样式调整 (P0)

#### Task 1: 调整头像与气泡间距为0

**Files:**
- Modify: `ruoyi-im-web/src/components/Chat/MessageItemRefactored.vue:214-231`

**Step 1: 修改头像区域margin为0**

找到 `.avatar-wrapper` 样式，将 `margin: 0 4px;` 改为 `margin: 0;`

```scss
// 头像区域
.avatar-wrapper {
  margin: 0; // 之前是 0 4px，改为0让气泡紧挨头像
  flex-shrink: 0;
  cursor: pointer;
  // ... 其他样式保持不变
}
```

**Step 2: 保存并验证变化**

运行: `npm run dev` (如果未启动)
预期: 头像和气泡之间无间隙

**Step 3: Commit**

```bash
git add ruoyi-im-web/src/components/Chat/MessageItemRefactored.vue
git commit -m "fix(chat): 调整头像与气泡间距为0，实现紧贴效果"
```

---

#### Task 2: 调整气泡圆角为钉钉风格

**Files:**
- Modify: `ruoyi-im-web/src/components/Chat/MessageBubbleRefactored.vue:333-358`

**Step 1: 修改对方消息圆角**

对方消息左侧应该更尖锐（模仿钉钉），右侧圆角更大：

```scss
// 对方消息样式
.message-bubble:not(.is-own) {
  .bubble-content {
    background: var(--dt-bubble-left-bg);
    border: 1px solid var(--dt-bubble-left-border);
    // 钉钉风格：左上4px，左下4px，右上12px，右下12px
    border-radius: 4px 12px 12px 4px;
  }
  // ... hover样式保持不变
}
```

**Step 2: 修改自己消息圆角**

自己消息右侧应该更尖锐：

```scss
// 自己消息样式
.message-bubble.is-own {
  flex-direction: row-reverse;

  .bubble-content {
    background: var(--dt-bubble-right-bg);
    border: none;
    // 钉钉风格：左上12px，左下12px，右上4px，右下4px
    border-radius: 12px 4px 4px 12px;
  }
  // ... hover样式保持不变
}
```

**Step 3: 保存并验证效果**

预期: 对方消息左尖右圆，自己消息左圆右尖

**Step 4: Commit**

```bash
git add ruoyi-im-web/src/components/Chat/MessageBubbleRefactored.vue
git commit -m "style(chat): 调整气泡圆角为钉钉风格 - 对方左尖右圆，自己左圆右尖"
```

---

#### Task 3: 调整消息头像尺寸统一为48px

**Files:**
- Modify: `ruoyi-im-web/src/components/Chat/MessageItemRefactored.vue:37-44`

**Step 1: 确认头像size属性**

```vue
<DingtalkAvatar
  :src="message.senderAvatar"
  :name="message.senderName"
  :user-id="message.senderId"
  :size="48" // 确保是48
  shape="square"
  custom-class="message-avatar"
/>
```

**Step 2: 移除头像边框和阴影（钉钉风格）**

修改 `.avatar-wrapper .message-avatar` 样式：

```scss
.avatar-wrapper {
  margin: 0;
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity var(--dt-transition-base);

  &:hover {
    opacity: 0.85;
  }

  .message-avatar {
    border-radius: 4px; // 钉钉方形头像小圆角
    // 移除边框和阴影
    // border: 1px solid var(--dt-brand-color); // 删除
    // box-shadow: 0 0 6px rgba(22, 119, 255, 0.2); // 删除
  }
}
```

**Step 3: 保存并验证**

预期: 头像无蓝色边框和光晕，简洁风格

**Step 4: Commit**

```bash
git add ruoyi-im-web/src/components/Chat/MessageItemRefactored.vue
git commit -m "style(chat): 移除头像边框和阴影，采用简洁钉钉风格"
```

---

#### Task 4: 调整消息气泡颜色（确保与钉钉一致）

**Files:**
- Modify: `ruoyi-im-web/src/styles/design-tokens.scss:50-58`

**Step 1: 确认气泡颜色变量**

```scss
// === DingTalk Special (钉钉特殊颜色) ===
--dt-bubble-left-bg: #FFFFFF; // 接收消息背景（白色）
--dt-bubble-left-border: #E4E7ED; // 接收消息边框（浅灰）
--dt-bubble-right-bg: #0089FF; // 发送消息背景（钉钉蓝） - 修改为纯蓝
--dt-bubble-right-bg-hover: #0077E6; // 发送消息悬停
```

**Step 2: 更新自己消息的文字颜色为白色**

在 `MessageBubbleRefactored.vue` 中添加文字颜色：

```scss
// 自己消息样式
.message-bubble.is-own {
  flex-direction: row-reverse;

  .bubble-content {
    background: var(--dt-bubble-right-bg);
    color: #FFFFFF; // 自己消息文字为白色
    border: none;
    border-radius: 12px 4px 4px 12px;
  }

  &:hover .bubble-content {
    background: var(--dt-bubble-right-bg-hover);
  }
}
```

**Step 3: 保存并验证**

预期: 自己发送的消息气泡为蓝色背景+白色文字

**Step 4: Commit**

```bash
git add ruoyi-im-web/src/styles/design-tokens.scss
git add ruoyi-im-web/src/components/Chat/MessageBubbleRefactored.vue
git commit -m "style(chat): 更新气泡颜色为钉钉蓝(#0089FF)，自己消息白色文字"
```

---

### 阶段二：聊天头部布局调整 (P1)

#### Task 5: 调整ChatHeader高度为56px

**Files:**
- Modify: `ruoyi-im-web/src/components/Chat/ChatHeader.vue`

**Step 1: 添加头部高度样式**

在 `<style scoped>` 中添加：

```scss
.chat-header {
  height: 56px; // 钉钉标准高度
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #E8E8E8;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;
    cursor: pointer;
  }

  .header-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
    flex: 1;
  }

  .header-name {
    font-size: 16px;
    font-weight: 600;
    color: #333333;
    margin: 0;
  }

  .meta-info {
    font-size: 12px;
    color: #999999;
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .action-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    cursor: pointer;
    color: #666666;
    transition: all 0.2s;

    &:hover {
      background: #F5F5F5;
      color: #0089FF;
    }
  }
}
```

**Step 2: 保存并验证**

预期: 头部高度56px，布局紧凑

**Step 3: Commit**

```bash
git add ruoyi-im-web/src/components/Chat/ChatHeader.vue
git commit -m "style(chat): 调整ChatHeader高度为56px，优化布局"
```

---

#### Task 6: 调整头部头像尺寸

**Files:**
- Modify: `ruoyi-im-web/src/components/Chat/ChatHeader.vue:14-16`

**Step 1: 修改头像size**

```vue
<DingtalkAvatar
  v-else
  :src="session?.avatar"
  :name="session?.name"
  :user-id="session?.targetId"
  :size="40" // 从42改为40
  shape="square"
  custom-class="header-avatar"
/>
```

**Step 2: 保存并验证**

**Step 3: Commit**

```bash
git add ruoyi-im-web/src/components/Chat/ChatHeader.vue
git commit -m "style(chat): 调整头部头像尺寸为40px"
```

---

### 阶段三：详情面板尺寸调整 (P1)

#### Task 7: 调整GroupDetailDialog宽度为280px

**Files:**
- Modify: `ruoyi-im-web/src/components/Chat/GroupDetailDialog.vue:2-10`

**Step 1: 修改弹窗宽度**

```vue
<el-dialog
  v-model="visible"
  :width="280"
  class="group-detail-dialog dingtalk-detail-panel"
  :close-on-click-modal="true"
  append-to-body
  destroy-on-close
  align-center
>
```

**Step 2: 添加钉钉风格详情面板样式**

在文件末尾 `<style>` 中添加：

```scss
.dingtalk-detail-panel {
  // 头部区域
  .left-section {
    padding: 20px 16px;
    text-align: center;
    border-bottom: 1px solid #E8E8E8;
  }

  .group-avatar {
    width: 48px;
    height: 48px;
    margin: 0 auto 12px;
    border-radius: 8px;
    background: #F5F5F5;
  }

  .group-name {
    font-size: 16px;
    font-weight: 600;
    color: #333333;
    margin: 0 0 4px;
  }

  .group-tags {
    display: flex;
    justify-content: center;
    gap: 8px;
  }

  // 成员区域
  .members-preview {
    padding: 16px;
    border-bottom: 6px solid #F5F5F5;
  }

  .members-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  .members-title {
    font-size: 14px;
    color: #666666;
  }

  .members-count {
    font-size: 12px;
    color: #999999;
  }

  .members-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .member-item {
    width: 40px;
    height: 40px;
  }

  // 菜单分组
  .info-list {
    padding: 0 16px;
  }

  .info-item {
    display: flex;
    align-items: center;
    height: 44px;
    padding: 0 12px;
    border-bottom: 1px solid #F5F5F5;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #F5F5F5;
    }

    &:last-child {
      border-bottom: none;
    }
  }
}
```

**Step 3: 保存并验证**

预期: 面板宽度280px，菜单项高度44px

**Step 4: Commit**

```bash
git add ruoyi-im-web/src/components/Chat/GroupDetailDialog.vue
git commit -m "style(chat): 调整群组详情面板宽度为280px，添加钉钉风格样式"
```

---

#### Task 8: 调整UserDetailDialog宽度为280px

**Files:**
- Modify: `ruoyi-im-web/src/components/Chat/UserDetailDialog.vue`

**Step 1: 查找并修改弹窗宽度**

找到 `<el-dialog>` 组件，修改 `:width` 为 280

**Step 2: 添加统一的详情面板样式类**

复用上面的 `.dingtalk-detail-panel` 样式

**Step 3: 保存并验证**

**Step 4: Commit**

```bash
git add ruoyi-im-web/src/components/Chat/UserDetailDialog.vue
git commit -m "style(chat): 调整用户详情面板宽度为280px，统一样式"
```

---

### 阶段四：输入框区域调整 (P1)

#### Task 9: 调整输入框工具栏样式

**Files:**
- Modify: `ruoyi-im-web/src/components/Chat/MessageInputRefactored.vue`

**Step 1: 优化工具栏布局**

调整工具栏高度和图标间距：

```scss
.chat-input-container {
  // 工具栏样式
  .input-toolbar {
    height: 40px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    gap: 16px;
    border-bottom: 1px solid #E8E8E8;
  }

  .toolbar-btn {
    width: 20px;
    height: 20px;
    color: #666666;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #0089FF;
    }
  }
}
```

**Step 2: 保存并验证**

**Step 3: Commit**

```bash
git add ruoyi-im-web/src/components/Chat/MessageInputRefactored.vue
git commit -m "style(chat): 调整输入框工具栏样式，高度40px"
```

---

### 阶段五：设计变量更新 (P2)

#### Task 10: 新增钉钉对齐专用设计变量

**Files:**
- Modify: `ruoyi-im-web/src/styles/design-tokens.scss`

**Step 1: 在 `:root` 中添加新变量**

在 `// === Layout Dimensions (布局尺寸) ===` 部分添加：

```scss
// === Layout Dimensions (布局尺寸) ===
// ... 现有变量保持不变 ...

// === 钉钉聊天界面专用 ===
--dt-detail-panel-width: 280px;
--dt-detail-menu-item-height: 44px;
--dt-detail-group-spacing: 6px;
--dt-bubble-avatar-gap: 0px;
--dt-chat-header-height-new: 56px;
--dt-input-toolbar-height: 40px;
```

**Step 2: 在气泡颜色部分更新**

```scss
// === DingTalk Special (钉钉特殊颜色) ===
--dt-bubble-left-bg: #FFFFFF;
--dt-bubble-left-border: #E8E8E8;
--dt-bubble-right-bg: #0089FF;
--dt-bubble-right-bg-hover: #0077E6;
--dt-bubble-right-text: #FFFFFF;
--dt-bubble-left-text: #333333;
```

**Step 3: 保存并验证**

**Step 4: Commit**

```bash
git add ruoyi-im-web/src/styles/design-tokens.scss
git commit -m "style(tokens): 新增钉钉聊天界面专用设计变量"
```

---

## 验证清单

完成所有任务后，请验证以下项目：

### 视觉验证
- [ ] 消息气泡与头像紧贴，无间隙
- [ ] 对方消息气泡：左上左下4px，右上右下12px
- [ ] 自己消息气泡：左上左下12px，右上右下4px
- [ ] 对方消息：白色背景，深色文字
- [ ] 自己消息：蓝色背景(#0089FF)，白色文字
- [ ] 头像无蓝色边框和光晕
- [ ] 聊天头部高度56px
- [ ] 详情面板宽度280px
- [ ] 详情菜单项高度44px
- [ ] 输入框工具栏高度40px

### 功能验证
- [ ] 消息发送正常
- [ ] 点击头像查看详情正常
- [ ] 长按消息弹出菜单正常
- [ ] 表情回复功能正常
- [ ] 多选消息功能正常

### 兼容性验证
- [ ] 暗色模式适配正常
- [ ] 响应式布局正常
- [ ] 无控制台错误或警告

---

## 回滚方案

如果需要回滚所有更改：

```bash
# 查看提交历史
git log --oneline -10

# 回滚到实施前的提交
git revert <commit-hash>...HEAD

# 或者硬重置（慎用）
git reset --hard <before-commit-hash>
```

---

## 参考资料

- 现有设计令牌: `ruoyi-im-web/src/styles/design-tokens.scss`
- Ant Design X Bubble组件: https://x.ant.design/components/bubble-cn
- 通用IM设计规范（微信/企业微信/钉钉）
