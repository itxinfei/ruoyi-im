# UI规范对比分析报告

> **生成时间**: 2026-01-14
> **项目**: RuoYi-IM 前端UI优化
> **参考标准**: 需求文档第五章 - UI设计规范（钉钉6.5.x）

---

## 一、发现的问题汇总

### 1.1 颜色系统不一致 ⚠️ 重大问题

| 项目 | 需求规范 | 当前实现 | 文件位置 | 状态 |
|------|----------|----------|----------|------|
| 品牌色 | #1890FF | #1677ff | design-tokens.scss:10 | ❌ 不一致 |
| 品牌色-hover | #40A9FF | #4096ff | design-tokens.scss:11 | ❌ 不一致 |
| 品牌色-active | #096DD9 | #0958d9 | design-tokens.scss:12 | ❌ 不一致 |
| 标题色 | #262626 | #1f1f1f | design-tokens.scss:33 | ❌ 不一致 |
| 正文色 | #333333 | #595959 | design-tokens.scss:34 | ❌ 不一致 |
| 次要文字 | #666666 | #8c8c8c | design-tokens.scss:35 | ❌ 不一致 |
| 辅助文字 | #999999 | #bfbfbf | design-tokens.scss:36 | ❌ 不一致 |
| 背景色 | #F5F7FA | #f7f8fa | design-tokens.scss:42 | ❌ 不一致 |
| 边框色 | #E8E8E8 | #e5e7eb | design-tokens.scss:48 | ❌ 不一致 |

**问题说明**: `design-tokens.scss` 使用了飞书的设计颜色（#1677ff），而 `dingtalk-theme.scss` 使用了正确的钉钉颜色（#1890FF），导致项目中颜色不统一。

### 1.2 间距规范不统一

| 组件 | 当前间距值 | 规范要求（4的倍数） | 状态 |
|------|-----------|---------------------|------|
| 工作台导航 padding | 12px 20px | 12px 16px | ⚠️ |
| 工作台导航 gap | 8px | ✅ 符合 | ✅ |
| 概览卡片 gap | 12px | ✅ 符合 | ✅ |
| 联系人面板 padding | 12px 16px | ✅ 符合 | ✅ |
| 分类标签 padding | 6px 12px | 8px 12px | ⚠️ |

### 1.3 字体规范不统一

| 场景 | 需求规范 | 当前实现 | 状态 |
|------|----------|----------|------|
| 页面主标题 | 24px/500 | 24px/600 | ⚠️ |
| 模块标题 | 20px/500 | 18px/500 | ❌ |
| 小标题 | 16px/500 | 15px/500 | ❌ |
| 正文 | 14px/400 | 14px | ✅ |
| 辅助文字 | 12px/400 | 12px/13px混用 | ⚠️ |

### 1.4 布局尺寸检查

| 区域 | 需求规范 | 当前实现 | 状态 |
|------|----------|----------|------|
| 左侧导航栏宽度 | 68px | 68px | ✅ |
| 中间会话区宽度 | 320px | 320px | ✅ |
| 顶部导航栏高度 | 48px | 需确认 | ⚠️ |
| 会话列表项高度 | 64px | 需确认 | ⚠️ |

---

## 二、需要修改的文件清单

### 2.1 样式文件（高优先级）

| 文件 | 修改内容 | 优先级 |
|------|----------|--------|
| `styles/design-tokens.scss` | 统一颜色值为钉钉规范 | P0 |
| `styles/dingtalk-theme.scss` | 验证颜色值正确性 | P0 |
| `styles/im-theme.scss` | 统一主题变量 | P1 |

### 2.2 页面组件

| 文件 | 修改内容 | 优先级 |
|------|----------|--------|
| `views/im/workbench/index.vue` | 间距、字体、颜色 | P1 |
| `views/im/contacts/index.vue` | 字体、间距调整 | P1 |
| `views/im/group/index.vue` | 统一样式 | P1 |
| `views/im/file/index.vue` | 统一样式 | P1 |
| `views/im/approval/index.vue` | 统一样式 | P1 |
| `views/im/ding/index.vue` | 统一样式 | P1 |
| `views/im/email/index.vue` | 统一样式 | P1 |

### 2.3 公共组件

| 文件 | 修改内容 | 优先级 |
|------|----------|--------|
| `components/Chat/MessageBubble.vue` | 消息气泡样式 | P1 |
| `components/Chat/SessionList.vue` | 会话列表项样式 | P1 |

---

## 三、详细修改方案

### 3.1 design-tokens.scss 颜色统一

```scss
// 修改前
--dt-color-primary: #1677ff;
--dt-color-primary-hover: #4096ff;
--dt-color-primary-active: #0958d9;

// 修改后（钉钉规范）
--dt-color-primary: #1890FF;
--dt-color-primary-hover: #40A9FF;
--dt-color-primary-active: #096DD9;
--dt-color-primary-light: #E6F7FF;
```

### 3.2 文字颜色统一

```scss
// 修改前
--dt-color-text-primary: #1f1f1f;
--dt-color-text-secondary: #595959;
--dt-color-text-tertiary: #8c8c8c;

// 修改后（钉钉规范）
--dt-color-text-primary: #262626;      // 标题色
--dt-color-text-secondary: #333333;     // 正文色
--dt-color-text-tertiary: #666666;      // 次要文字
--dt-color-text-quaternary: #999999;    // 辅助文字
```

### 3.3 背景和边框统一

```scss
// 修改后（钉钉规范）
--dt-color-bg-secondary: #F5F7FA;       // 页面背景
--dt-color-border: #E8E8E8;              // 边框色
```

### 3.4 字体变量统一

```scss
// 确保符合需求文档
--dt-font-size-lg: 16px;   // 小标题
--dt-font-size-xl: 18px;   // 模块标题（实际应为20px）
--dt-font-size-2xl: 20px;  // 大标题（实际应为24px）
```

---

## 四、组件样式优化清单

### 4.1 按钮组件

| 属性 | 规范值 | 验证 |
|------|--------|------|
| 主按钮背景 | #1890FF | 需验证 |
| 主按钮文字 | #FFFFFF | 需验证 |
| 圆角 | 4px | 需验证 |
| hover | 色值加深10% | 需验证 |

### 4.2 消息气泡组件

| 气泡类型 | 背景色 | 文字色 | 验证 |
|----------|--------|--------|------|
| 发送方 | #1890FF | #FFFFFF | 需验证 |
| 接收方 | #FFFFFF | #333333 | 需验证 |
| 系统消息 | #F5F7FA | #999999 | 需验证 |

### 4.3 头像组件

| 类型 | 尺寸 | 形状 | 验证 |
|------|------|------|------|
| 默认头像 | 40×40px | 圆形 | 需验证 |
| 大头像 | 64×64px | 圆形 | 需验证 |
| 在线状态点 | 10×10px | 圆形 | 需验证 |

---

## 五、执行计划

### 阶段一：样式文件修复（P0）
1. ✅ 修改 `design-tokens.scss` 颜色系统
2. ✅ 验证 `dingtalk-theme.scss` 正确性
3. ✅ 统一所有导入的样式变量

### 阶段二：核心页面优化（P1）
1. 工作台页面 `workbench/index.vue`
2. 联系人页面 `contacts/index.vue`
3. 群组页面 `group/index.vue`

### 阶段三：其他页面优化（P2）
1. 文件管理页面
2. 审批中心页面
3. DING消息页面
4. 邮箱页面

### 阶段四：组件优化（P1）
1. 消息气泡组件
2. 会话列表组件
3. 输入框组件

---

## 六、验收标准

### 6.1 颜色一致性
- [ ] 所有页面使用统一的主色 #1890FF
- [ ] 文字颜色层级分明且统一
- [ ] 背景色和边框色符合规范

### 6.2 间距规范性
- [ ] 所有间距遵循4的倍数原则
- [ ] padding 和 margin 使用设计变量

### 6.3 字体规范性
- [ ] 标题层级字号正确
- [ ] 字重使用规范
- [ ] 行高符合规范

### 6.4 布局尺寸
- [ ] 左侧导航栏 68px
- [ ] 中间会话区 320px
- [ ] 顶部导航栏 48px

---

## 七、备注

1. 颜色修改后需要全局搜索 `#1677ff` 确保没有遗漏
2. 字号修改需要同时调整对应的行高
3. 建议使用全局CSS变量替代硬编码值
4. 修改后需要进行视觉回归测试

---

**报告版本**: v1.0
**最后更新**: 2026-01-14
