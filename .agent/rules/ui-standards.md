# 工业级 UI 视觉实现规则 (Frontend Aesthetics)

> 适用范围：所有 ruoyi-im-web 下的 .vue, .scss 文件。
> 核心依据：`src/styles/design-tokens.scss` (钉钉 8.2 工业级 Design Token)
> 完整规范：`.claude/rules/dingtalk-ui-prompt.md`

---

## 0. 先行必读：Token 体系速查

**唯一合法样式源**：`src/styles/design-tokens.scss` 中的所有 `--dt` CSS 变量

### 配色系统（钉钉官方标准）
| 用途 | 色值 | CSS 变量 |
|------|------|----------|
| 品牌蓝/主色/按钮 | `#277EFB` | `var(--dt-brand-color)` |
| 成功绿 | `#00B42A` | `var(--dt-success-color)` |
| 警告黄 | `#FF7D00` | `var(--dt-warning-color)` |
| 危险红 | `#F53F3F` | `var(--dt-error-color)` |
| 主文字 | `#171A1D` | `var(--dt-text-primary)` |
| 次要文字 | `#5F6670` | `var(--dt-text-secondary)` |
| 辅助文字 | `#8A9099` | `var(--dt-text-tertiary)` |
| 页面背景 | `#F5F6F7` | `var(--dt-bg-body)` |
| 卡片白底 | `#FFFFFF` | `var(--dt-bg-card)` |

### 间距系统（8px 栅格）
| 变量 | 值 | 用途 |
|------|-----|------|
| `--dt-spacing-sm` | 8px | 组件内间距 |
| `--dt-spacing-md` | 12px | 表单间距 |
| `--dt-spacing-lg` | 16px | 页面边距 |
| `--dt-spacing-xl` | 24px | 模块间距 |

### 圆角系统
| 变量 | 值 | 用途 |
|------|-----|------|
| `--dt-radius-sm` | 4px | 按钮/输入框 |
| `--dt-radius-lg` | 8px | 卡片/弹窗 |
| `--dt-radius-full` | 9999px | 头像/徽章 |

### SCSS Mixins（优先调用）
```scss
@include flex-center;     // 水平垂直居中
@include flex-between;    // 水平两端对齐
@include text-ellipsis;   // 单行文字截断
@include button-reset;    // 按钮样式重置
@include respond-to(md);  // 响应式断点
```

---

## 1. 审美红线

### 1.1 颜色强制规则
- ❌ **禁止硬编码任何 Hex 色值**，必须使用 `--dt-*` 变量
- ❌ **禁止使用 `#0089FF`、`#1677FF`、`#1890ff`** 等非标蓝色（已废弃）
- ❌ **禁止自定义渐变色**（仅允许 `var(--dt-brand-gradient)`）
- ❌ **禁止高饱和度颜色**（`#FF4D4F` 仅用于未读计数）
- ✅ 所有颜色必须通过 Design Token 语义化调用

### 1.2 圆角强制规则
- 按钮圆角：`var(--dt-radius-sm)` (4px)
- 输入框圆角：`var(--dt-radius-sm)` (4px)
- 卡片圆角：`var(--dt-radius-lg)` (8px)
- 弹窗圆角：`var(--dt-radius-lg)` (8px)
- 头像圆角：`var(--dt-radius-sm)` (4px)
- 聊天气泡：接收方 `var(--dt-bubble-radius-received)`，发送方 `var(--dt-bubble-radius-sent)`

### 1.3 阴影规范
- 卡片悬浮：`var(--dt-shadow-2)` → `0 2px 8px rgba(0,0,0,0.08)`
- 弹窗：`var(--dt-shadow-modal)` → `0 12px 48px rgba(0,0,0,0.2)`
- 品牌阴影：`var(--dt-shadow-brand)` → `0 2px 8px rgba(39,126,251,0.25)`

---

## 2. 布局红线

### 2.1 固定尺寸（严格调用 Token）
| 用途 | 变量 | 值 |
|------|------|-----|
| 侧边栏宽度 | `--dt-nav-sidebar-width` | 64px |
| 会话面板宽度 | `--dt-session-panel-width` | 250px |
| 联系人面板宽度 | `--dt-contact-panel-width` | 240px |
| 头部高度 | `--dt-header-height` | 56px |
| 聊天头部高度 | `--dt-chat-header-height` | 60px |
| 头像尺寸(中) | `--dt-avatar-size-md` | 36px |
| 聊天区最小宽度 | `--dt-chat-min-width` | 480px |

### 2.2 布局规范
- ✅ **必须使用 `display: flex` 或 `display: grid`**
- ❌ **禁止使用 `float` 做整体布局**
- ❌ **禁止使用 `position: absolute` 做主布局**
- ✅ Flex 容器必须声明 `min-width: 0`（防崩防爆）
- ✅ 同一行表单标签宽度必须完全一致
- ✅ 按钮组左对齐/右对齐必须统一

### 2.3 间距规范（8px 栅格）
- 所有间距必须是 `4/8/12/16/24/32px` 之一
- ❌ 禁止使用 `7px`、`13px`、`15px`、`17px` 等非栅格值

---

## 3. 交互红线

### 3.1 动效规范
- 所有点击元素必须包含 `transition: var(--dt-transition-fast)`
- 必须实现 `:hover` 和 `:active` (scale 0.98) 反馈
- 过渡时间优先使用 `var(--dt-transition-fast)` (0.15s)

### 3.2 状态反馈
- 按钮禁用态：`opacity: 0.6; cursor: not-allowed`
- 加载态：使用 `el-button` 的 `loading` 属性
- 错误提示：使用 `ElMessage.error()`

---

## 4. IM 专属规范（聊天气泡/群组头像）

### 4.1 聊天气泡
```scss
// 接收方气泡
background: var(--dt-bubble-left-bg);
border-radius: var(--dt-bubble-radius-received); // 4px 8px 8px 8px

// 发送方气泡
background: var(--dt-bubble-right-bg);
border-radius: var(--dt-bubble-radius-sent); // 8px 4px 8px 8px
border-radius: var(--dt-bubble-radius); // 8px (通用)

// 最大宽度
max-width: var(--dt-bubble-max-width); // 70%
```

### 4.2 会话列表交互态
```scss
// Hover态
background: var(--dt-bg-session-hover);

// 选中态
background: var(--dt-bg-session-active);
```

### 4.3 消息状态
```scss
// 未读计数
color: var(--dt-unread-color);

// 消息状态图标
color: var(--dt-message-status-color);
color: var(--dt-message-status-read-color);
```

---

## 5. 暗色模式强制规则

- ✅ 所有暗色样式必须通过 `html.dark` 类继承 Token 变量
- ❌ **禁止单独写硬编码暗色色值**
- ❌ 禁止为暗色模式单独写重复布局

```scss
// 正确示范
html.dark {
  --dt-brand-color: #4493FA;
  --dt-bg-body: var(--dt-bg-body-dark);
}

// 错误示范
.dark-mode {
  .button { color: #82B5FC; } // 硬编码禁止
}
```

---

## 6. 绝对禁止项（违反直接拒绝生成）

1. ❌ 硬编码任何 px 值、色值、百分比
2. ❌ 使用用户 Token 未定义的渐变色、动画、阴影
3. ❌ 修改 Token 里已定义的组件尺寸
4. ❌ 添加钉钉原生没有的装饰性元素
5. ❌ 使用 `!important` 覆盖样式（除非全局重置已定义）
6. ❌ 使用 `el-row/el-col` 构建页面级骨架

---

## 7. 生成后自检清单

```
生成代码前，必须先对照以下清单完成自检：
1. ✅ 所有样式 100% 调用了 --dt 变量，无任何硬编码色值/px 值
2. ✅ 变量调用严格遵循语义化规则（背景用 --dt-bg-*，文字用 --dt-text-*）
3. ✅ 所有基础样式优先调用了 SCSS Mixins
4. ✅ 布局只用了 Flex/Grid，无 float、inline-block 整体布局
5. ✅ 所有元素对齐统一，无错位、偏移问题
6. ✅ 完整支持暗色模式，通过 html.dark 类实现
7. ✅ 无任何自定义渐变色、动画、装饰性元素
```

---

## 8. 快速修复指令

当发现代码有样式问题时，发送以下指令触发修复：

```
帮我排查并修复这段代码的样式问题：
1. 所有硬编码色值/px 值替换为 --dt 变量
2. 修复布局错位、元素不对齐
3. 修复颜色语义错误、跳变
4. 补充完整的暗色模式适配
5. 移除冗余、非标样式
```
