# 70 钉钉 DING 消息组件规范 (DING Message Spec)

> **用途**：定义"DING 消息"这一**钉钉独有**功能的视觉与交互标准，是项目区别于通用 IM 的核心特征。
> **权威依据**：`docs/00-钉钉8.2像素级权威基线.md` §2.6 / 视觉资产 `#14-ding-message.png` `#15-ding-button.png`
> **优先级**：⭐⭐⭐ 最高（用户测试反馈"看起来不像钉钉"，DING 是核心识别要素）

---

## 1. 什么是 DING 消息

### 1.1 业务定义
DING 消息（钉一下）是钉钉的核心特色功能，用于**强提醒**：
- **应用消息 DING**：被 DING 的人会在客户端弹出全屏提醒（红色弹窗），无法忽略
- **短信 DING**：发送 DING 同时触发短信通知（电话号码）
- **电话 DING**：触发 AI 电话拨打

### 1.2 视觉识别特征（钉钉味的核心）
| 元素 | 必备特征 |
|-----|---------|
| 主色 | **橙色 `#FF7D00`**（与品牌蓝形成强对比） |
| 图标 | 喇叭图标（`Bell` / `Bullhorn`），不是普通通知 icon |
| 文字 | "DING" 大写英文 + 中文"钉一下" |
| 形状 | 圆形按钮（不是方形）— **唯一允许的胶囊/圆形按钮** |

### 1.3 与普通消息的区别

```
普通消息：    [蓝色气泡] 内容文字
DING 消息：   ⚠️ [橙色边框 + 喇叭] [DING] 内容文字  + 接收回执
```

---

## 2. DING 按钮（输入区右侧）

### 2.1 视觉规范

| 属性 | 值 | CSS Token |
|-----|----|-----|
| 形状 | 圆形 | `border-radius: 50%` |
| 尺寸 | 36px × 36px | — |
| 位置 | 输入区右下角，紧邻"发送"按钮左侧 | — |
| 默认背景 | `#FF7D00` | `--dt-ding-btn-bg` |
| Hover 背景 | `#E66B00` | `--dt-ding-btn-hover` |
| Active 背景 | `#CC5C00` | — |
| 图标 | `Bell` 18px 白色 | — |
| 文字（按钮内） | 无（仅图标） | — |
| Tooltip | "DING 一下"（hover 200ms 后浮现） | — |

### 2.2 完整 HTML/CSS 实现

```vue
<template>
  <button
    class="ding-btn"
    title="DING 一下（强提醒）"
    @click="handleOpenDingDialog"
  >
    <el-icon><Bell /></el-icon>
  </button>
</template>

<style scoped lang="scss">
.ding-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;        // 唯一允许的圆形按钮
  border: none;
  background: var(--dt-ding-btn-bg);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--dt-transition-fast);
  box-shadow: 0 2px 6px rgba(255, 125, 0, 0.25);

  .el-icon {
    font-size: 18px;
  }

  &:hover {
    background: var(--dt-ding-btn-hover);
  }

  &:active {
    background: #CC5C00;
    transform: scale(0.96);
  }
}
</style>
```

### 2.3 状态机

| 状态 | 视觉 | 说明 |
|-----|-----|------|
| 默认 | 橙色圆形 + 喇叭图标 | 正常态 |
| Hover | 深橙 + 阴影增强 | tooltip 浮现 |
| Active | 更深橙 + scale 0.96 | 点击瞬间反馈 |
| Disabled | 灰色 50% 透明 | 无网络/未选会话 |
| 禁用群 DING | 隐藏按钮 | 群被管理员禁止 DING 时 |

---

## 3. DING 消息气泡

### 3.1 视觉规范

```
┌─────────────────────────────────────┐
│ ⚠️ [橙边框] [喇叭] DING              │
│                                      │
│  XX 通过 DING 发来一条强提醒：        │
│  ─────────────────                   │
│  原始消息内容                         │
│                                      │
│  📱 已通过 [应用]/[短信]/[电话] 通知   │
└─────────────────────────────────────┘
```

| 属性 | 值 |
|-----|----|
| 气泡边框 | 2px solid `#FF7D00` |
| 气泡背景 | `#FFF7E6`（浅橙底） |
| 圆角 | 8px（不区分发送/接收方，统一） |
| 顶部标题 | "DING" 字样，14px/700，橙色 `#FF7D00` |
| 内容字号 | 14px，`#171A1D` |
| 通知方式标记 | 12px，`#FF7D00`，带 icon |
| 最大宽度 | 70%（同普通气泡） |

### 3.2 完整组件结构

```vue
<template>
  <div class="ding-bubble">
    <div class="ding-bubble-header">
      <el-icon class="ding-icon"><Bell /></el-icon>
      <span class="ding-label">DING</span>
      <span class="ding-method">{{ methodText }}</span>
    </div>

    <div class="ding-bubble-body">
      <div class="ding-sender">{{ senderName }} 发来一条强提醒：</div>
      <div class="ding-content">{{ content }}</div>
    </div>

    <div class="ding-bubble-footer">
      <el-icon><Phone v-if="method === 'phone'" /><ChatLineRound v-else /></el-icon>
      <span>已通过 {{ methodText }} 通知 {{ recipientCount }} 人</span>
    </div>

    <div v-if="isOwn" class="ding-receipt">
      <el-icon class="check-read"><Check /></el-icon>
      <span>{{ readCount }}/{{ totalCount }} 已读</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.ding-bubble {
  border: 2px solid var(--dt-ding-border);
  background: #FFF7E6;
  border-radius: var(--dt-radius-lg);
  padding: 12px 16px;
  max-width: 70%;
  font-size: var(--dt-font-size-base);
  line-height: 1.5;
}

.ding-bubble-header {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding-bottom: var(--dt-spacing-sm);
  border-bottom: 1px dashed rgba(255, 125, 0, 0.3);
  margin-bottom: var(--dt-spacing-sm);

  .ding-icon { font-size: 16px; color: var(--dt-ding-border); }
  .ding-label { font-size: 14px; font-weight: 700; color: var(--dt-ding-border); letter-spacing: 0.5px; }
  .ding-method { font-size: 12px; color: var(--dt-text-tertiary); margin-left: auto; }
}

.ding-bubble-body {
  .ding-sender { font-size: 12px; color: var(--dt-text-tertiary); margin-bottom: var(--dt-spacing-xs); }
  .ding-content { font-size: 14px; color: var(--dt-text-primary); word-break: break-word; }
}

.ding-bubble-footer {
  margin-top: var(--dt-spacing-sm);
  padding-top: var(--dt-spacing-sm);
  border-top: 1px dashed rgba(255, 125, 0, 0.3);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);
  font-size: 12px;
  color: var(--dt-ding-border);
}

.ding-receipt {
  margin-top: var(--dt-spacing-xs);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);
  font-size: 11px;
  color: var(--dt-success-color);
  .check-read { font-size: 12px; }
}
</style>
```

### 3.3 通知方式枚举

| 类型 | 值 | 中文文案 | 图标 |
|-----|----|---------|------|
| 应用消息 | `app` | "应用消息" | `ChatLineRound` |
| 短信 | `sms` | "短信" | `Message` |
| 电话 | `phone` | "电话" | `Phone` |

---

## 4. DING 创建对话框

### 4.1 触发流程

```
用户点击 DING 按钮
   ↓
弹出 DING 创建对话框（el-dialog 520px）
   ↓
选择：①接收人 ②通知方式 ③消息内容
   ↓
确认发送 → 接收方收到 DING 消息 + 触发对应通知
```

### 4.2 对话框结构

```vue
<el-dialog
  v-model="visible"
  title="DING 一下（强提醒）"
  width="520px"
  class="ding-dialog"
>
  <el-form label-position="top">
    <el-form-item label="接收人">
      <MemberPicker v-model="recipients" />
    </el-form-item>

    <el-form-item label="通知方式">
      <el-radio-group v-model="method">
        <el-radio value="app">应用消息（默认）</el-radio>
        <el-radio value="sms">应用消息 + 短信</el-radio>
        <el-radio value="phone">应用消息 + 电话（仅紧急时使用）</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="DING 内容">
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
        :maxlength="200"
        show-word-limit
        placeholder="请输入需要强提醒的内容（200 字以内）"
      />
    </el-form-item>
  </el-form>

  <template #footer>
    <el-button @click="visible = false">取消</el-button>
    <el-button
      type="warning"
      :loading="sending"
      style="background: var(--dt-ding-btn-bg); border-color: var(--dt-ding-btn-bg);"
      @click="handleSendDing"
    >
      <el-icon><Bell /></el-icon> DING 一下
    </el-button>
  </template>
</el-dialog>
```

### 4.3 状态清单

| 状态 | 表现 |
|-----|------|
| 默认 | 接收人为空、通知方式默认"app"、内容空 |
| 已选接收人 | 头像组 + 数量显示 |
| 内容超限 | 显示红色字数提示 |
| 发送中 | DING 按钮 loading + 禁用其他操作 |
| 发送成功 | toast "DING 已发送" + 关闭对话框 |
| 发送失败 | toast 错误原因 + 保留输入内容 |

---

## 5. DING 消息接收方表现

### 5.1 接收方立刻触发的视觉反馈

| 场景 | 表现 |
|-----|------|
| **应用全屏提醒** | 全屏覆盖式弹窗（橙色背景，无法忽略），需手动关闭 |
| **会话列表前缀** | 会话列表对应项显示 `[DING]` 红色加粗前缀 |
| **未读数高亮** | 未读 Badge 由红色变橙色（区别于普通未读） |
| **桌面通知** | 系统通知中心显示，标题带 "DING" 前缀 |
| **声音** | 钉钉特色"叮"声（与普通消息提示音不同） |

### 5.2 全屏提醒弹窗规范

```scss
.ding-fullscreen-alert {
  position: fixed;
  inset: 0;
  z-index: var(--dt-z-max);
  background: rgba(255, 125, 0, 0.95);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;

  .alert-icon {
    font-size: 80px;
    margin-bottom: var(--dt-spacing-xl);
    animation: ding-shake 0.4s ease-in-out 3;
  }

  .alert-title { font-size: 24px; font-weight: 700; margin-bottom: var(--dt-spacing-md); }
  .alert-content { font-size: 16px; max-width: 600px; text-align: center; }
  .alert-actions { margin-top: var(--dt-spacing-2xl); display: flex; gap: var(--dt-spacing-lg); }
}

@keyframes ding-shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-8px); }
  75% { transform: translateX(8px); }
}
```

---

## 6. 验收清单 (AC)

### 6.1 视觉
- [ ] DING 按钮位于输入区右侧，紧邻发送按钮，36×36 圆形，橙色 `#FF7D00`
- [ ] DING 消息气泡有明显橙色边框，不与普通气泡混淆
- [ ] 接收方收到 DING 时**必须**触发全屏提醒（不能只是普通消息）
- [ ] 全屏提醒**唯一允许使用**抖动动画

### 6.2 交互
- [ ] 点击 DING 按钮立刻弹出 DING 对话框（不超过 100ms）
- [ ] 对话框关闭使用 ESC 或外部点击
- [ ] 字数超限时输入框边框变红
- [ ] 发送成功后自动关闭对话框 + 滚动到最新消息

### 6.3 钉钉味自检
- [ ] 全应用范围内**只有 DING 按钮**使用了 `border-radius: 50%`（圆形按钮规则的唯一例外）
- [ ] 全应用范围内**只有 DING 提醒**使用了抖动动画（动画规则的唯一例外）
- [ ] 橙色 `#FF7D00` 在全应用中**只用于 DING 相关 + @ 提醒** 两处场景

---

## 7. 反例（禁止）

| 反例 | 错误示范 | 正确做法 |
|-----|---------|---------|
| 用蓝色 | DING 按钮用品牌蓝 `#277EFB` | 必须用橙色 `#FF7D00` |
| 方形按钮 | DING 按钮 `border-radius: 8px` | 必须圆形 `border-radius: 50%` |
| 普通气泡 | DING 消息和普通文本一样的气泡 | 必须橙色边框 + 喇叭图标 |
| 静默提醒 | DING 消息只显示在会话列表 | 必须全屏提醒 |
| 滥用动画 | 普通消息也加抖动 | 仅 DING 全屏提醒允许抖动 |

---

## 8. 当前实现状态

| 组件 | 文件 | 状态 |
|-----|------|------|
| DING 按钮 | `ChatInputArea.vue` 工具栏 | ⏳ 未实现，需新增 |
| DING 气泡 | `ChatMessageBubble.vue` 新增分支 | ⏳ 未实现 |
| DING 对话框 | `components/ding/SendDingDialog.vue` | 🔄 已存在但需重构 |
| DING 全屏提醒 | `components/ding/DingFullScreenAlert.vue` | ⏳ 未实现，需新增 |

---

## 9. 变更记录

| 日期 | 变更内容 |
|-----|---------|
| 2026-05-06 | 初始版本，定义 DING 消息全套视觉与交互规范 |
