# 邮箱系统优化 - 快速启动指南

## 项目结构

```
.kiro/specs/email-system-optimization/
├── requirements.md          # 需求文档（10个需求）
├── design.md               # 设计文档（10个正确性属性）
├── tasks.md                # 任务列表（15个任务组）
├── IMPLEMENTATION_PLAN.md  # 实现计划（3个阶段）
└── QUICK_START.md          # 本文件
```

## 第一阶段快速启动（第1周）

### 任务1: 清理mail.js中的废弃接口

**目标**: 清理前端API接口，提高代码质量

**当前状态**: mail.js中有多个邮件模板相关的接口

**需要清理的接口**:
- getEmailTemplates()
- getEmailTemplate()
- previewEmailTemplate()
- createEmailTemplate()
- updateEmailTemplate()
- deleteEmailTemplate()
- setEmailTemplateEnabled()
- copyEmailTemplate()
- getEmailTemplateVariables()

**保留的核心接口**:
- getMailList() - 获取邮件列表
- getMailDetail() - 获取邮件详情
- sendMail() - 发送邮件
- saveDraft() - 保存草稿
- deleteMail() - 删除邮件
- markAsRead() - 标记已读
- markAsUnread() - 标记未读
- moveToFolder() - 移动邮件
- starMail() - 星标邮件
- uploadAttachment() - 上传附件
- downloadAttachment() - 下载附件
- deleteAttachment() - 删除附件
- permanentlyDeleteMail() - 永久删除
- searchMail() - 搜索邮件
- getUnreadCount() - 获取未读数量
- getFolderStats() - 获取文件夹统计

**实现步骤**:
1. 在mail.js中注释掉邮件模板相关接口
2. 检查是否有代码调用这些接口
3. 更新API文档
4. 提交代码审查

### 任务2: 创建权限验证装饰器

**目标**: 在后端添加统一的权限验证机制

**需要创建的文件**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/annotation/RequireEmailPermission.java`

**装饰器功能**:
- 验证用户是否有权访问邮件
- 验证用户是否有权修改邮件
- 验证用户是否有权删除邮件

**实现步骤**:
1. 创建@RequireEmailPermission注解
2. 创建对应的AOP切面
3. 在ImEmailController中应用装饰器
4. 编写单元测试

### 任务3: 添加统一的错误处理

**目标**: 统一邮件系统的错误返回格式

**需要创建的文件**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/exception/EmailErrorHandler.java`

**错误类型**:
- 邮件不存在 (404)
- 权限不足 (403)
- 参数无效 (400)
- 服务器错误 (500)

**实现步骤**:
1. 创建EmailErrorHandler
2. 定义邮件相关的异常类
3. 在GlobalExceptionHandler中集成
4. 编写错误处理测试

### 任务4: 添加API请求参数验证

**目标**: 验证所有邮件API的输入参数

**需要验证的参数**:
- 邮件ID (不能为空，必须是正整数)
- 用户ID (不能为空，必须是正整数)
- 邮件主题 (不能为空，长度1-255)
- 邮件内容 (不能为空，长度1-10000)
- 文件夹名称 (必须是有效的文件夹类型)
- 收件人ID列表 (不能为空，最多100个)

**实现步骤**:
1. 创建邮件DTO类（如果不存在）
2. 添加@Valid注解
3. 创建自定义验证器
4. 编写参数验证测试

## 第一阶段快速启动（第2周）

### 任务5: 添加XSS防护

**目标**: 防止邮件内容中的恶意脚本

**需要修改的文件**:
- `ruoyi-im-web/src/components/Chat/MailDetailDialog.vue`

**实现步骤**:
1. 安装DOMPurify: `npm install dompurify`
2. 在MailDetailDialog.vue中导入DOMPurify
3. 在显示邮件内容时使用DOMPurify.sanitize()
4. 编写XSS防护测试

**代码示例**:
```javascript
import DOMPurify from 'dompurify'

// 显示邮件内容时
const cleanContent = DOMPurify.sanitize(email.content)
```

### 任务6: 文件类型验证

**目标**: 防止上传危险文件类型

**需要修改的文件**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImEmailController.java`

**允许的文件类型**:
- 文档: pdf, doc, docx, xls, xlsx, ppt, pptx
- 图片: jpg, jpeg, png, gif, bmp
- 压缩: zip, rar, 7z
- 其他: txt, csv

**禁止的文件类型**:
- 可执行文件: exe, bat, cmd, sh, com
- 脚本文件: js, vbs, ps1
- 系统文件: dll, sys, drv

**实现步骤**:
1. 定义允许和禁止的文件类型列表
2. 在上传时检查文件扩展名
3. 检查文件MIME类型
4. 编写文件验证测试

### 任务7: 权限检查

**目标**: 确保用户只能访问自己的邮件

**需要修改的文件**:
- `ruoyi-im-api/src/main/java/com/ruoyi/im/controller/ImEmailController.java`
- `ruoyi-im-api/src/main/java/com/ruoyi/im/service/impl/ImEmailServiceImpl.java`

**检查点**:
- 获取邮件时验证所有权
- 修改邮件时验证所有权
- 删除邮件时验证所有权
- 访问附件时验证所有权

**实现步骤**:
1. 在每个邮件操作前添加权限检查
2. 如果权限不足，返回403错误
3. 记录权限检查日志
4. 编写权限检查测试

## 第一阶段快速启动（第3周）

### 任务8: 数据库迁移 - 添加draft_status字段

**目标**: 支持邮件草稿功能

**需要执行的SQL**:
```sql
ALTER TABLE im_email ADD COLUMN draft_status VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE im_email ADD COLUMN is_read BOOLEAN DEFAULT FALSE;
ALTER TABLE im_email ADD COLUMN is_starred BOOLEAN DEFAULT FALSE;
```

**实现步骤**:
1. 创建迁移脚本: `sql/migrations/20260212_add_email_draft_support.sql`
2. 更新ImEmail实体类
3. 更新MyBatis映射文件
4. 测试迁移脚本

### 任务9: 前端自动保存逻辑

**目标**: 每30秒自动保存邮件草稿

**需要修改的文件**:
- `ruoyi-im-web/src/components/Chat/ComposeMailDialog.vue`

**实现步骤**:
1. 添加自动保存定时器（30秒）
2. 显示保存状态提示
3. 处理保存失败情况
4. 编写自动保存测试

**代码示例**:
```javascript
// 自动保存定时器
const autoSaveInterval = setInterval(() => {
  if (isDirty) {
    saveDraft()
  }
}, 30000)
```

### 任务10: 草稿恢复功能

**目标**: 打开编辑器时加载已保存的草稿

**需要修改的文件**:
- `ruoyi-im-web/src/components/Chat/ComposeMailDialog.vue`

**实现步骤**:
1. 打开编辑器时检查是否有草稿
2. 如果有草稿，显示恢复提示
3. 用户可以选择恢复或新建
4. 编写草稿恢复测试

## 第一阶段快速启动（第4周）

### 任务11: 虚拟滚动实现

**目标**: 使用虚拟滚动优化大列表性能

**需要修改的文件**:
- `ruoyi-im-web/src/components/Chat/MailPanel.vue`

**实现步骤**:
1. 安装vue-virtual-scroller: `npm install vue-virtual-scroller`
2. 在MailPanel.vue中导入虚拟滚动组件
3. 替换普通列表为虚拟列表
4. 配置缓冲区大小（建议50-100）
5. 编写虚拟滚动测试

**代码示例**:
```vue
<virtual-scroller
  :items="emails"
  :item-size="60"
  :buffer="50"
>
  <template #default="{ item }">
    <mail-item :email="item" />
  </template>
</virtual-scroller>
```

### 任务12: 分页加载实现

**目标**: 实现无限滚动加载邮件

**需要修改的文件**:
- `ruoyi-im-web/src/api/im/mail.js`
- `ruoyi-im-web/src/components/Chat/MailPanel.vue`

**实现步骤**:
1. 修改getMailList()支持分页参数
2. 在MailPanel中实现无限滚动
3. 当滚动到底部时加载下一页
4. 显示加载中状态
5. 编写分页加载测试

## 执行检查清单

### 代码质量检查
- [ ] 所有代码通过ESLint检查
- [ ] 所有代码通过SonarQube检查
- [ ] 代码覆盖率 > 80%
- [ ] 没有安全漏洞

### 功能测试检查
- [ ] 所有需求验收标准通过
- [ ] 所有单元测试通过
- [ ] 所有集成测试通过
- [ ] 性能测试通过

### 部署检查
- [ ] 数据库迁移脚本已验证
- [ ] 回滚脚本已准备
- [ ] 部署文档已更新
- [ ] 监控告警已配置

## 常见问题

### Q: 如何运行单元测试？
A: 使用命令 `mvn test` 运行所有单元测试

### Q: 如何运行前端测试？
A: 使用命令 `npm run test` 运行前端测试

### Q: 如何检查代码质量？
A: 使用命令 `mvn sonar:sonar` 进行代码质量检查

### Q: 如何部署到测试环境？
A: 参考部署文档中的灰度发布步骤

## 联系方式

- 项目经理: [待填写]
- 技术负责人: [待填写]
- 测试负责人: [待填写]

## 相关文档

- [需求文档](./requirements.md)
- [设计文档](./design.md)
- [任务列表](./tasks.md)
- [实现计划](./IMPLEMENTATION_PLAN.md)
