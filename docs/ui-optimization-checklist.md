# 🎨 钉钉 UI 对标优化清单

> 创建时间：2026-01-30
> 目标：像素级对齐钉钉 Web 端 UI 设计
> 策略：增量优化，逐个组件修改

---

## ✅ 已完成优化的组件

### 1. MessageItemRefactored.vue（消息项组件）

#### 问题发现：
- ❌ 头像尺寸：`:size="36"` → 应该是 `40`
- ❌ 头像间距：`margin: 0 4px` → 应该是 `0`（紧贴气泡）
- ❌ 头像边框：有蓝色边框和阴影 → 应该移除（钉钉头像很简洁）

#### 优化代码：
```vue
<!-- 修改前 -->
<DingtalkAvatar
  :src="message.senderAvatar"
  :name="message.senderName"
  :user-id="message.senderId"
  :size="36"                    ❌ 改为 40
  shape="square"
  custom-class="message-avatar"
/>

<!-- 修改后 -->
<DingtalkAvatar
  :src="message.senderAvatar"
  :name="message.senderName"
  :user-id="message.senderId"
  :size="40"                    ✅ 钉钉标准：40px
  shape="square"
  custom-class="message-avatar"
/>
```

#### 样式优化：
```scss
// 修改前
.avatar-wrapper {
  margin: 0 4px;                ❌ 改为 0
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity var(--dt-transition-base);

  &:hover {
    opacity: 0.85;
  }

  .message-avatar {
    border-radius: var(--dt-radius-sm);
    border: 1px solid var(--dt-brand-color);      ❌ 删除边框
    box-shadow: 0 0 6px rgba(0, 137, 255, 0.2);    ❌ 删除阴影
  }
}

// 修改后
.avatar-wrapper {
  margin: 0;                     ✅ 气泡紧贴头像
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity var(--dt-transition-base);

  &:hover {
    opacity: 0.85;
  }

  .message-avatar {
    border-radius: 4px;          ✅ 钉钉方形头像，小圆角
    // 移除边框和阴影               ✅ 简洁风格
  }
}
```

---

### 2. MessageBubbleRefactored.vue（消息气泡组件）

#### 问题发现：
- ❌ 对方消息圆角：`var(--dt-radius-md) var(--dt-radius-md) var(--dt-radius-md) 2px`
  - 应该是：`4px 12px 12px 4px`（左尖右圆）
- ❌ 自己消息圆角：`var(--dt-radius-md) var(--dt-radius-md) 2px var(--dt-radius-md)`
  - 应该是：`12px 4px 4px 12px`（左圆右尖）

#### 优化代码：
```scss
// 修改前
.message-bubble:not(.is-own) {
  .bubble-content {
    background: var(--dt-bubble-left-bg);
    border: 1px solid var(--dt-bubble-left-border);
    border-radius: var(--dt-radius-md) var(--dt-radius-md) var(--dt-radius-md) 2px;
    // 实际值：8px 8px 8px 2px    ❌ 错误
  }
}

.message-bubble.is-own {
  flex-direction: row-reverse;

  .bubble-content {
    background: var(--dt-bubble-right-bg);
    border: none;
    border-radius: var(--dt-radius-md) var(--dt-radius-md) 2px var(--dt-radius-md);
    // 实际值：8px 8px 2px 8px    ❌ 错误
  }
}

// 修改后
.message-bubble:not(.is-own) {
  .bubble-content {
    background: var(--dt-bubble-left-bg);
    border: 1px solid var(--dt-bubble-left-border);
    border-radius: 4px 12px 12px 4px;   ✅ 钉钉标准：左尖右圆
  }
}

.message-bubble.is-own {
  flex-direction: row-reverse;

  .bubble-content {
    background: var(--dt-bubble-right-bg);
    border: none;
    border-radius: 12px 4px 4px 12px;   ✅ 钉钉标准：左圆右尖
  }
}
```

---

## 📋 需要审查的其他组件清单

### 优先级 P0（核心聊天体验）

#### 3. ChatSidebar.vue（左侧导航栏）
- [ ] 宽度：60px
- [ ] 背景：蓝色渐变 `linear-gradient(180deg, #0089FF 0%, #006ECC 100%)`
- [ ] 导航项：40px × 40px、圆角 8px
- [ ] 激活态：白色半透明背景
- [ ] 悬停态：白色透明度 0.12

#### 4. SessionPanel.vue（会话列表）
- [ ] 会话项高度：72px（固定）
- [ ] 头像尺寸：48px × 48px
- [ ] 未读角标：红色圆形、最小宽度 18px
- [ ] 悬停背景：`#F6F8FA`
- [ ] 激活背景：`#EBF2FF`（浅蓝）

#### 5. MessageInputRefactored.vue（输入框）
- [ ] 工具栏高度：40px
- [ ] 工具栏图标：20px × 20px
- [ ] 工具栏间距：16px
- [ ] 文本域：无边框、无背景、透明
- [ ] 发送按钮：蓝色背景、白色文字、4px 圆角
- [ ] 内边距：12px 16px

#### 6. ImageViewerDialog.vue（图片预览）
- [ ] 全屏遮罩：黑色半透明
- [ ] 左右切换按钮
- [ ] 显示当前索引：`3/15`
- [ ] 键盘快捷键：左右箭头、ESC

### 优先级 P1（重要功能）

#### 7. UserDetailDialog.vue（用户详情）
- [ ] 面板宽度：280px
- [ ] 菜单项高度：44px
- [ ] 悬停背景：`#F5F5F5`

#### 8. GroupDetailDrawer.vue（群组详情）
- [ ] 面板宽度：280px
- [ ] 群头像：48px × 48px、圆角 8px
- [ ] 成员网格：8px 间距

### 优先级 P2（增强功能）

#### 9. EmojiPicker.vue（表情选择器）
- [ ] 表格布局：8 列
- [ ] 表情大小：32px × 32px
- [ ] 悬停放大：1.2 倍

#### 10. GlobalSearch.vue（全局搜索）
- [ ] 搜索框高度：40px
- [ ] 圆角：4px
- [ ] 聚焦边框：蓝色 2px

---

## 🔧 快速修复脚本

### 脚本 1：修复头像组件

```bash
# 批量替换头像尺寸
find ruoyi-im-web/src/components/Chat -name "*.vue" -exec sed -i 's/:size="36"/:size="40"/g' {} \;

# 批量替换头像间距为 0
find ruoyi-im-web/src/components/Chat -name "*.vue" -exec sed -i 's/margin: 0 4px;/margin: 0;/g' {} \;
```

### 脚本 2：修复气泡圆角

在 `MessageBubbleRefactored.vue` 中：
```scss
// 对方消息：左尖右圆
border-radius: 4px 12px 12px 4px;

// 自己消息：左圆右尖
border-radius: 12px 4px 4px 12px;
```

### 脚本 3：移除头像边框和阴影

```scss
.message-avatar {
  border-radius: 4px;
  // 移除这两行：
  // border: 1px solid var(--dt-brand-color);
  // box-shadow: 0 0 6px rgba(0, 137, 255, 0.2);
}
```

---

## 🎨 设计变量检查

### 检查 design-tokens.scss

确保以下变量值正确：

```scss
// 圆角（应该定义这些）
--dt-radius-bubble-left: 4px;      // 对方消息左上/下
--dt-radius-bubble-right: 12px;     // 对方消息右上/下
--dt-radius-bubble-own-left: 12px;  // 自己消息左上/下
--dt-radius-bubble-own-right: 4px;  // 自己消息右上/下

// 如果没有，添加到 :root 中
```

---

## 📊 优化前后对比

| 组件 | 优化前 | 优化后 | 状态 |
|------|--------|--------|------|
| 头像尺寸 | 36px | 40px | ⏳ 待修改 |
| 头像间距 | 4px | 0px | ⏳ 待修改 |
| 头像边框 | 有蓝色边框 | 无边框 | ⏳ 待修改 |
| 对方气泡圆角 | 8px 8px 8px 2px | 4px 12px 12px 4px | ⏳ 待修改 |
| 自己气泡圆角 | 8px 8px 2px 8px | 12px 4px 4px 12px | ⏳ 待修改 |

---

## ✅ 验证清单

完成修改后，逐项验证：

### 视觉验证
- [ ] 头像与气泡紧贴（无间隙）
- [ ] 对方消息：左上/下 4px，右上/下 12px
- [ ] 自己消息：左上/下 12px，右上/下 4px
- [ ] 对方消息：白色背景，浅灰边框
- [ ] 自己消息：蓝色背景，无边框
- [ ] 头像无蓝色边框和光晕

### 尺寸验证
- [ ] 头像：40px × 40px
- [ ] 会话列表项高度：72px
- [ ] 会话列表头像：48px × 48px
- [ ] 输入框工具栏高度：40px

---

## 🚀 下一步行动

1. **立即执行**（今天）
   - [ ] 修改 MessageItemRefactored.vue
   - [ ] 修改 MessageBubbleRefactored.vue
   - [ ] 验证修改效果

2. **本周完成**（Week 1）
   - [ ] 审查 ChatSidebar.vue
   - [ ] 审查 SessionPanel.vue
   - [ ] 审查 MessageInputRefactored.vue

3. **下周完成**（Week 2）
   - [ ] 审查其他组件
   - [ ] 全面 UI 对齐验证

---

**更新时间：2026-01-30**
