# 贡献指南

感谢你对 RuoYi-IM 项目的关注！我们欢迎所有形式的贡献。

## 如何参与贡献

### 报告 Bug

1. 在 [Issues](https://gitee.com/your-username/ruoyi-im/issues) 中搜索是否已有相同问题
2. 如果没有，创建一个新的 Issue，包含：
   - 清晰的标题和描述
   - 复现步骤
   - 期望行为 vs 实际行为
   - 环境信息（OS、浏览器、Node/Java 版本）
   - 截图或错误日志（如有）

### 提交功能建议

1. 在 [Issues](https://gitee.com/your-username/ruoyi-im/issues) 中创建 Feature Request
2. 说明使用场景和期望效果
3. 如果可能，提供设计稿或参考实现

### 提交代码

#### 1. Fork & Clone

```bash
# Fork 仓库后克隆
git clone https://gitee.com/your-username/ruoyi-im.git
cd ruoyi-im

# 添加上游远程
git remote add upstream https://gitee.com/original/ruoyi-im.git
```

#### 2. 创建分支

```bash
# 同步上游代码
git fetch upstream
git checkout -b feature/your-feature upstream/main
```

#### 3. 开发

- 遵循项目的编码规范（见下文）
- 确保代码能通过编译：`mvn compile`（后端）/ `npm run build`（前端）
- 添加必要的注释和文档

#### 4. 提交

```bash
git add .
git commit -m "feat: 你的功能描述"
```

#### 5. 推送 & PR

```bash
git push origin feature/your-feature
```

然后在 Gitee 上创建 Pull Request。

---

## 编码规范

### Git Commit 规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/)：

```
<type>: <description>

类型：
  feat     - 新功能
  fix      - Bug 修复
  refactor - 代码重构
  docs     - 文档更新
  test     - 测试相关
  chore    - 构建/工具/配置
  perf     - 性能优化
  ci       - CI/CD 相关
```

示例：
```
feat: 添加消息撤回功能
fix: 修复会话列表未读数显示错误
refactor: 重构消息推送服务
docs: 更新 API 接口文档
```

### 后端规范 (Java)

- **JDK 版本**：严格使用 Java 1.8，禁止 Java 9+ 语法
- **三层架构**：Controller → Service → Mapper，单向依赖
- **查询方式**：必须使用 LambdaQueryWrapper，禁止硬编码列名字符串
- **异常处理**：禁止吞异常，所有 catch 块必须记录日志或重新抛出
- **常量定义**：禁止魔法值，所有字面量必须提取为常量
- **日志安全**：禁止在日志中打印消息内容、Token、密码等敏感信息

### 前端规范 (Vue)

- **Design Token**：所有颜色、间距、圆角必须使用 CSS 变量（`var(--dt-*)`）
- **8px 栅格**：间距值只能是 4/8/12/16/24/32px
- **字号规范**：12/14/16/18/20/24px，禁止 10/11/13/15px 等非标值
- **圆角规范**：4px（按钮）/ 8px（卡片）/ 9999px（胶囊）
- **禁止行为**：
  - 硬编码颜色（如 `#0089FF`、`rgba(0,0,0,0.5)`）
  - 复杂动画（`@keyframes`、`filter: blur`、`backdrop-filter`）
  - 渐变背景（除品牌按钮外）
  - 悬浮位移效果（`transform: translate/scale`）

### UI 设计规范

- 严格对齐钉钉 Windows 8.2 设计规范
- 参考 [Design Token 体系](docs/35-前端全局样式与主题配置.md)
- 参考 [钉钉 UI 设计蓝图](docs/27-PC版钉钉UI设计蓝图.md)

---

## 代码审查

所有 PR 都需要通过代码审查：

1. **自动化检查**：确保编译通过、无语法错误
2. **规范检查**：符合上述编码规范
3. **功能验证**：确保功能正常、无回归
4. **安全审查**：无敏感信息泄露、无安全漏洞

---

## 行为准则

- 尊重每一位贡献者
- 建设性地提出意见和建议
- 专注于技术讨论，避免人身攻击
- 遵守开源社区的基本礼仪

---

## 联系方式

- **Issues**：[提交问题](https://gitee.com/your-username/ruoyi-im/issues)
- **Discussions**：[参与讨论](https://gitee.com/your-username/ruoyi-im/discussions)

---

再次感谢你的贡献！🎉
