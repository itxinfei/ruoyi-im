# 钉钉 UI 开发规范 Prompt

> 本规范为 RuoYi-IM 项目强制执行的 UI 设计标准，所有前端代码生成必须严格遵守。
> 版本：v2.2 - 像素级约束版（2026-04-17）

## 〇、IM 核心组件像素级约束（钉钉 8.2 精确值）

### 聊天窗口 (ChatWindow)
| 元素 | 精确值 | CSS 变量 |
|------|--------|----------|
| 头部高度 | 56px | `var(--dt-header-height)` |
| 头部 padding 左右 | 20px | `var(--dt-spacing-xl)` |
| 头部标题字号 | 17px | - |
| 头部标题字重 | 600 | `var(--dt-font-weight-semibold)` |
| 头部图标大小 | 18px | - |
| 头部图标 padding | 10px | - |
| 头部图标圆角 | 8px | `var(--dt-radius-lg)` |
| 消息列表 padding 上下 | 12px | `var(--dt-spacing-md)` |
| 消息列表 padding 左右 | 24px | `var(--dt-spacing-xl)` |

### 消息气泡 (ChatMessageBubble)
| 元素 | 精确值 | CSS 变量 |
|------|--------|----------|
| 气泡 padding 上下 | 8px | `var(--dt-bubble-padding-v)` |
| 气泡 padding 左右 | 12px | `var(--dt-bubble-padding-h)` |
| 气泡最大宽度 | 70% | `var(--dt-bubble-max-width)` |
| 气泡字号 | 14px | `var(--dt-font-size-base)` |
| 气泡行高 | 1.5 | - |
| 发送方气泡圆角 | 4px 8px 8px 8px | 右上小角 |
| 接收方气泡圆角 | 8px 4px 8px 8px | 左上小角 |
| 发送方气泡背景 | #277EFB | `var(--dt-bubble-right-bg)` |
| 接收方气泡背景 | #ffffff | `var(--dt-bubble-left-bg)` |
| 头像尺寸 | 36px | `var(--dt-avatar-size-md)` |
| 头像圆角 | 4px | `var(--dt-radius-sm)` |
| 头像与气泡间距 | 16px | `var(--dt-spacing-lg)` |
| 连续消息压缩间距 | -4px | - |
| 操作条圆角 | 6px | `var(--dt-radius-md)` |
| 操作条阴影 | `var(--dt-shadow-action-bar)` | - |
| 操作条图标尺寸 | 16px | - |
| 操作条图标容器 | 20px × 20px | - |

### 输入区 (ChatInputArea)
| 元素 | 精确值 | CSS 变量 |
|------|--------|----------|
| 工具栏高度 | 48px | - |
| 工具栏图标大小 | 20px | - |
| 工具栏图标 padding | 10px | - |
| 工具栏图标圆角 | 8px | `var(--dt-radius-lg)` |
| 输入区最小高度 | 120px | `var(--dt-input-min-height)` |
| 输入区最大高度 | 320px | `var(--dt-input-max-height)` |
| 输入区字号 | 15px | `var(--dt-font-size-md)` |
| 输入区行高 | 1.7 | - |
| 输入区 padding 上下 | 8px | `var(--dt-spacing-sm)` |
| 输入区 padding 左右 | 24px | `var(--dt-spacing-xl)` |
| 底部发送栏高度 | 48px | - |
| 发送按钮宽度 | 64px | - |
| 发送按钮高度 | 32px | `var(--dt-btn-height-sm)` |
| 发送按钮圆角 | 4px | `var(--dt-radius-sm)` |
| 空状态发送按钮背景 | `var(--dt-send-btn-empty-bg)` | #E5E7EB |
| 表情网格列数 | 8 列 | - |
| 表情尺寸 | 22px | - |
| 图片预览缩略图 | 72px × 72px | - |
| 图片预览圆角 | 10px | `var(--dt-radius-xl)` |

### 侧边导航 (ImSideNavNew)
| 元素 | 精确值 | CSS 变量 |
|------|--------|----------|
| 导航栏宽度 | 64px | `var(--dt-nav-sidebar-width)` |
| Logo 尺寸 | 64px × 64px | `var(--dt-logo-size)` |
| 导航项高度 | 48px | - |
| 导航项图标大小 | 20px | - |
| 导航项圆角 | 8px | `var(--dt-radius-lg)` |
| 导航项激活背景 | `var(--dt-brand-bg)` | #E6F2FF |
| 导航项激活图标色 | `var(--dt-brand-color)` | #277EFB |
| 底部头像尺寸 | 36px | `var(--dt-avatar-size-md)` |

### 会话列表 (SessionList)
| 元素 | 精确值 | CSS 变量 |
|------|--------|----------|
| 会话项高度 | 56px | - |
| 会话项 padding | 12px 16px | `var(--dt-spacing-md) var(--dt-spacing-lg)` |
| 会话项头像 | 40px × 40px | - |
| 会话项头像圆角 | 4px | `var(--dt-radius-sm)` |
| 会话项 hover 背景 | `var(--dt-bg-session-hover)` | - |
| 会话项 active 背景 | `var(--dt-bg-session-active)` | #E6F2FF |
| 未读数角标最小宽 | 18px | - |
| 未读数角标高度 | 18px | - |
| 未读数角标圆角 | 9px | `var(--dt-radius-full)` |
| 未读数角标字号 | 11px | `var(--dt-font-size-xs)` |

### 滚动条 (全局)
| 元素 | 精确值 |
|------|--------|
| 滚动条宽度 | 6px |
| 滚动条轨道 | 透明 |
| 滚动条滑块背景 | rgba(0,0,0,0.2) |
| 滚动条滑块圆角 | 3px |
| 滚动条 hover 背景 | rgba(0,0,0,0.3) |

## 一、配色系统（强制约束）

### 主色与状态色
| 用途 | 色值 | CSS 变量 |
|------|------|----------|
| 品牌蓝/主色/按钮 | `#277EFB` | `var(--dt-brand-color)` |
| 成功绿 | `#00B42A` | `var(--dt-success-color)` |
| 警告黄 | `#FF7D00` | `var(--dt-warning-color)` |
| 危险红 | `#F53F3F` | `var(--dt-error-color)` |
| 链接色 | `#277EFB` | `var(--dt-text-link-color)` |

### 禁止行为
- ❌ 禁止使用 `#0089FF`（旧主色，已废弃）
- ❌ 禁止使用 `#1677FF`、`#1890ff` 等非标蓝色
- ❌ 禁止使用渐变色作为背景（除品牌按钮外）
- ❌ 禁止使用高饱和度颜色（#FF4D4F 除外用于未读计数）

### 中性色（仅使用 CSS 变量）
| 用途 | CSS 变量 |
|------|----------|
| 页面背景 | `var(--dt-bg-body)` |
| 卡片白底 | `var(--dt-bg-card)` |
| 分割线/边框 | `var(--dt-border-light)` |
| 主标题 | `var(--dt-text-primary)` |
| 次要文字 | `var(--dt-text-secondary)` |
| 辅助文字 | `var(--dt-text-tertiary)` |

## 二、间距系统（8px 栅格）

### 允许的间距值
```
4px / 8px / 12px / 16px / 24px / 32px
```

### 禁止行为
- ❌ 禁止使用 `7px`、`13px`、`15px`、`17px` 等非栅格值
- ❌ 禁止使用 `10px`、`14px`（非标准）
- ✅ 所有间距必须是上表中的值

### 典型场景
| 场景 | 间距 | CSS 变量 |
|------|------|----------|
| 组件内边距 | 16px | `var(--dt-spacing-lg)` |
| 模块间距 | 24px | `var(--dt-spacing-xl)` |
| 页面边距 | 16px | `var(--dt-spacing-lg)` |
| 按钮组间距 | 8px | `var(--dt-spacing-sm)` |
| 表单控件间距 | 16px | `var(--dt-spacing-lg)` |

## 三、圆角系统（固定不变）

| 组件 | 圆角 | CSS 变量 |
|------|------|----------|
| 按钮 | 4px | `var(--dt-radius-sm)` |
| 输入框 | 4px | `var(--dt-radius-sm)` |
| 卡片 | 8px | `var(--dt-radius-lg)` |
| 弹窗 | 8px | `var(--dt-radius-lg)` |
| 头像 | 4px | `var(--dt-radius-sm)` |
| 气泡 | 8px | `var(--dt-bubble-radius)` |

## 四、布局规范

### 允许的布局方式
- ✅ `display: flex`（主布局）
- ✅ `display: grid`（网格布局）
- ✅ `display: flex` + `gap`（间距控制）

### 禁止行为
- ❌ 禁止使用 `float`
- ❌ 禁止使用 `position: absolute` 进行主布局
- ❌ 禁止使用 `margin` 硬怼布局

### Flex 对齐规则
```scss
// 水平垂直居中
display: flex;
align-items: center;
justify-content: center;

// 水平两端对齐
display: flex;
align-items: center;
justify-content: space-between;
```

## 五、阴影规范

### 钉钉标准阴影（尽量避免使用）
```scss
// 仅在必要时使用卡片悬浮阴影
box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

// 弹窗
box-shadow: var(--dt-shadow-modal);

// 品牌阴影（按钮、输入框聚焦）
box-shadow: var(--dt-shadow-brand);
```

### 禁止行为
- ❌ 禁止使用 `box-shadow: 0 0 10px rgba(0,0,0,0.5)` 等大阴影
- ❌ 禁止使用彩色阴影
- ❌ 禁止使用复杂多层阴影

## 六、动画与特效规范（禁止复杂特效）

### 原则：克制即是美
- ❌ 禁止使用复杂阴影投影（modal 级别的大阴影）
- ❌ 禁止使用 CSS 动画（`@keyframes`）除非必要
- ❌ 禁止使用 `filter: blur()` 模糊效果
- ❌ 禁止使用 `backdrop-filter` 背景模糊
- ❌ 禁止使用渐变背景（除品牌按钮外）
- ❌ 禁止使用悬浮位移效果（`transform: translate`）
- ❌ 禁止使用悬浮缩放效果（`transform: scale`）

### 允许的简单效果
- ✅ `transition: all var(--dt-transition-fast)` 用于状态变化（如 hover 颜色）
- ✅ 边框颜色变化
- ✅ 背景色轻微变化
- ✅ 透明度变化（仅 0→1 等简单场景）

### 登录页面示例（无特效）
```scss
// ✅ 正确：简洁无特效
.login-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  border: 1px solid var(--dt-border-light);
}

// ❌ 错误：复杂阴影 + 渐变 + 动画
.login-card {
  background: linear-gradient(135deg, #fff 0%, #f5f5f5 100%);
  box-shadow: var(--dt-shadow-modal);
  animation: float 12s ease-in-out infinite;
}
```

### 按钮示例（无特效）
```scss
// ✅ 正确：简洁
.login-button {
  background: var(--dt-brand-color);
  border-radius: var(--dt-radius-sm);

  &:hover {
    background: var(--dt-brand-hover);
  }
}

// ❌ 错误：悬浮阴影 + 位移 + 缩放
.login-button {
  background: var(--dt-brand-color);
  box-shadow: var(--dt-shadow-float);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--dt-shadow-float);
  }
}
```

## 七、字体规范

### 字体栈
```scss
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
```

### 字号层级
| 层级 | 字号 | 字重 | CSS 变量 |
|------|------|------|----------|
| 主标题 | 18px | 600 | `var(--dt-font-size-xl)` + `var(--dt-font-weight-semibold)` |
| 模块副标题 | 16px | 500 | `var(--dt-font-size-lg)` + `var(--dt-font-weight-medium)` |
| 正文/表单 | 14px | 400 | `var(--dt-font-size-base)` |
| 辅助/备注 | 12px | 400 | `var(--dt-font-size-sm)` |

## 八、组件开发规范

### 按钮
```vue
<!-- 主按钮 -->
<el-button type="primary">主操作</el-button>

<!-- 次要按钮 -->
<el-button>次要操作</el-button>

<!-- 危险按钮 -->
<el-button type="danger">危险操作</el-button>
```

### 输入框
```vue
<el-input
  v-model="value"
  placeholder="请输入"
  clearable
/>
```

### 弹窗
```vue
<el-dialog
  v-model="visible"
  title="标题"
  width="500px"
>
  <div>内容区域（padding: 24px）</div>
  <template #footer>
    <el-button @click="visible = false">取消</el-button>
    <el-button type="primary" @click="handleConfirm">确认</el-button>
  </template>
</el-dialog>
```

### 卡片
```vue
<div class="card">
  <div class="card-header">标题</div>
  <div class="card-body">内容</div>
</div>

<style scoped>
.card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px;
}
</style>
```

## 九、校验检查清单

生成代码后，必须检查以下项目：

### 间距检查
- [ ] 所有间距值是否为 4/8/12/16/24/32px
- [ ] 表单标签是否左对齐
- [ ] 按钮组是否对齐统一

### 颜色检查
- [ ] 是否使用 CSS 变量而非硬编码色值
- [ ] 是否使用了禁止的颜色（#0089FF、#1677FF 等）

### 布局检查
- [ ] 是否使用 flex 而非 float
- [ ] 容器是否设置了 `box-sizing: border-box`
- [ ] 卡片/模块边缘是否对齐

### 圆角检查
- [ ] 按钮圆角是否为 4px
- [ ] 卡片圆角是否为 8px
- [ ] 输入框圆角是否为 4px

## 十、复用优先原则

开发新功能时，**必须**按以下顺序复用：

1. **Design Tokens**：`src/styles/design-tokens.scss` 中的 CSS 变量
2. **Mixins**：`src/styles/_mixins.scss` 中的辅助宏
3. **通用组件**：`src/components/Common/` 下的组件
4. **Element Plus**：使用 `el-` 前缀组件，主题变量已映射

## 十一、Element Plus 组件覆盖规范（钉钉风格）

> 以下所有覆盖已在 `src/styles/global.scss` 中实现，生成新组件时如需自定义 Element Plus 样式，必须遵循以下规范。

### Table 表格
- 表头背景：`var(--dt-bg-body)`，字号 14px，字重 500
- 行高：48px（默认），紧凑模式 40px
- 斑马纹背景：`var(--dt-bg-input)`
- hover 行背景：`var(--dt-bg-session-hover)`
- 边框：`var(--dt-border-light)`
- 圆角：外层容器 `var(--dt-radius-lg)`

### Tabs 标签页
- 标签高度：40px
- 标签字号：14px
- 激活态下划线：2px solid `var(--dt-brand-color)`
- 激活态文字色：`var(--dt-brand-color)`
- 未激活文字色：`var(--dt-text-secondary)`
- 内容区 padding：24px

### Switch 开关
- 开启背景：`var(--dt-brand-color)`
- 关闭背景：`var(--dt-border-color)`
- 尺寸：默认 20px 高

### Tag 标签
- 圆角：`var(--dt-radius-sm)` (4px)
- 字号：12px
- padding：2px 8px
- Plain 模式边框：`var(--dt-border-light)`

### Pagination 分页
- 按钮圆角：`var(--dt-radius-sm)` (4px)
- 当前页背景：`var(--dt-brand-color)`，文字白色
- 当前页圆角：`var(--dt-radius-sm)` (4px)
- 字号：14px

### Upload 上传
- 上传区域边框：1px dashed `var(--dt-border-color)`
- 上传区域圆角：`var(--dt-radius-lg)` (8px)
- 上传区域背景：`var(--dt-bg-input)`
- hover 边框：`var(--dt-brand-color)`

### Badge 徽标
- 未读数背景：`var(--dt-error-color)` (#F53F3F)
- 未读数文字：白色
- 最小宽度：18px
- 圆角：9px（完全圆形）

### Radio 单选框 / Checkbox 复选框
- 选中色：`var(--dt-brand-color)`
- 边框：`var(--dt-border-color)`
- 圆角：Checkbox 为 `var(--dt-radius-sm)` (4px)

### Form 表单
- label 对齐：左对齐
- label 字号：14px
- label 颜色：`var(--dt-text-primary)`
- 表单项间距：24px
- 错误提示色：`var(--dt-error-color)`
- 错误提示字号：12px

### Popconfirm 气泡确认框
- 圆角：`var(--dt-radius-lg)` (8px)
- 阴影：`var(--dt-shadow-2)`
- padding：16px

### Empty 空状态
- 图标颜色：`var(--dt-text-quaternary)`
- 描述文字颜色：`var(--dt-text-tertiary)`
- 描述文字字号：14px

### Descriptions 描述列表
- label 颜色：`var(--dt-text-secondary)`
- content 颜色：`var(--dt-text-primary)`
- 字号：14px
- 间距：16px

### InputNumber 数字输入框
- 圆角：`var(--dt-radius-sm)` (4px)
- 高度：32px (small) / 40px (default)

### Progress 进度条
- 轨道背景：`var(--dt-border-lighter)`
- 填充色：`var(--dt-brand-color)`
- 圆角：4px
- 高度：6px

### Divider 分割线
- 颜色：`var(--dt-border-light)`
- 文字颜色：`var(--dt-text-tertiary)`
- 文字字号：12px

### Design Token 快速查询

```scss
// 颜色
--dt-brand-color      // 品牌蓝 #277EFB
--dt-success-color    // 成功绿 #00B42A
--dt-warning-color    // 警告黄 #FF7D00
--dt-error-color      // 危险红 #F53F3F
--dt-text-primary     // 主文字 #171A1D
--dt-text-secondary   // 次要文字
--dt-text-tertiary    // 辅助文字
--dt-bg-body          // 页面背景
--dt-bg-card          // 卡片白底

// 间距
--dt-spacing-sm (8px)
--dt-spacing-md (12px)
--dt-spacing-lg (16px)
--dt-spacing-xl (24px)

// 圆角
--dt-radius-sm (4px)
--dt-radius-lg (8px)

// 阴影
--dt-shadow-2 (0 2px 8px rgba(0,0,0,0.08))
--dt-shadow-card-hover
--dt-shadow-brand
```
