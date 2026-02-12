# 邮箱系统优化设计文档

## 概述

基于当前项目的邮箱系统，本设计文档定义了实用的优化方案。重点关注短期能快速实现的改进，包括性能优化、安全加固、功能完善和代码清理。

## 优化方向和实现策略

### 第一阶段：基础优化（短期）

#### 1. 代码清理和质量改进

**目标**: 清理废弃代码，完善错误处理

**实现方案**:
- 清理mail.js中已废弃的API接口
- 在ImEmailController中添加统一的权限验证装饰器
- 完善错误处理，统一返回错误格式
- 添加API请求参数验证

**涉及文件**:
- mail.js - 清理废弃接口
- ImEmailController.java - 添加权限验证
- ImEmailService.java - 完善错误处理

#### 2. 安全性加固

**目标**: 防止XSS攻击，验证附件类型

**实现方案**:
- 在MailDetailDialog.vue中使用DOMPurify清理HTML内容
- 在附件上传时验证文件类型和大小
- 在ImEmailController中添加权限检查，确保用户只能访问自己的邮件
- 对所有用户输入进行转义处理

**涉及文件**:
- MailDetailDialog.vue - 添加XSS防护
- ComposeMailDialog.vue - 添加文件验证
- ImEmailController.java - 添加权限验证

#### 3. 草稿自动保存

**目标**: 防止用户编写的邮件丢失

**实现方案**:
- 在ComposeMailDialog.vue中添加自动保存逻辑，每30秒保存一次
- 在im_email表中添加draft_status字段标记草稿
- 用户打开编辑器时加载已保存的草稿
- 用户关闭编辑器时保留草稿内容

**涉及文件**:
- ComposeMailDialog.vue - 实现自动保存
- ImEmailService.java - 添加草稿保存接口
- 数据库 - 添加draft_status字段

#### 4. 虚拟滚动优化

**目标**: 提升大列表渲染性能

**实现方案**:
- 在MailPanel.vue中使用vue-virtual-scroller替代普通列表
- 每次只渲染可见的50-100个邮件项
- 实现邮件数据的分页加载

**涉及文件**:
- MailPanel.vue - 实现虚拟滚动
- mail.js - 添加分页接口

### 第二阶段：功能增强（中期）

#### 5. 邮件搜索和过滤

**目标**: 帮助用户快速找到邮件

**实现方案**:
- 在MailPanel.vue中添加搜索栏，支持按发件人、收件人、主题、日期搜索
- 在ImEmailService中实现搜索逻辑，使用数据库索引优化查询
- 在im_email表中添加索引：user_id, folder_id, created_at, subject

**涉及文件**:
- MailPanel.vue - 添加搜索UI
- ImEmailService.java - 实现搜索逻辑
- 数据库 - 添加索引

#### 6. 邮件标签和分类

**目标**: 支持用户自定义邮件分类

**实现方案**:
- 在im_email表中添加labels字段（JSON格式）
- 在MailPanel.vue中添加标签管理UI
- 支持为邮件添加多个标签
- 支持按标签过滤邮件

**涉及文件**:
- MailPanel.vue - 添加标签UI
- MailDetailDialog.vue - 显示和编辑标签
- ImEmailService.java - 实现标签管理

#### 7. 邮件规则和自动处理

**目标**: 支持用户定义规则自动处理邮件

**实现方案**:
- 创建im_email_rule表存储用户规则
- 在邮件接收时应用规则（标记、分类、删除等）
- 在前端提供规则管理界面

**涉及文件**:
- 数据库 - 创建im_email_rule表
- ImEmailService.java - 实现规则应用逻辑
- 新建RulePanel.vue - 规则管理界面

#### 8. 附件上传优化

**目标**: 支持大文件上传和断点续传

**实现方案**:
- 实现分片上传：将文件分成5MB的分片
- 支持并发上传最多3个分片
- 支持断点续传：记录已上传的分片
- 显示上传进度

**涉及文件**:
- ComposeMailDialog.vue - 实现分片上传UI
- ImEmailController.java - 实现分片上传接口
- 数据库 - 创建im_upload_session表

### 第三阶段：高级功能（长期）

#### 9. 邮件加密

**目标**: 保护敏感邮件内容

**实现方案**:
- 使用AES-256加密邮件内容
- 加密密钥存储在用户会话中
- 用户可选择是否加密邮件
- 查看加密邮件时自动解密

**涉及文件**:
- ComposeMailDialog.vue - 添加加密选项
- MailDetailDialog.vue - 显示加密状态
- ImEmailService.java - 实现加密解密逻辑

#### 10. 邮件导出和备份

**目标**: 支持用户导出和备份邮件

**实现方案**:
- 支持导出为EML或MBOX格式
- 支持定期自动备份
- 支持从备份恢复邮件

**涉及文件**:
- MailPanel.vue - 添加导出功能
- ImEmailService.java - 实现导出逻辑
- 后台任务 - 实现自动备份

## 数据库变更

### 邮件表扩展

```sql
ALTER TABLE im_email ADD COLUMN (
  is_read BOOLEAN DEFAULT FALSE,
  is_starred BOOLEAN DEFAULT FALSE,
  labels JSON,
  draft_status VARCHAR(20),
  is_encrypted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_email_user_folder ON im_email(user_id, folder_id);
CREATE INDEX idx_email_created_at ON im_email(created_at);
CREATE INDEX idx_email_subject ON im_email(subject);
```

### 新增表

```sql
-- 邮件规则表
CREATE TABLE im_email_rule (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  conditions JSON NOT NULL,
  actions JSON NOT NULL,
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES im_user(id),
  INDEX idx_rule_user_id (user_id)
);

-- 上传会话表
CREATE TABLE im_upload_session (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_size BIGINT NOT NULL,
  uploaded_chunks JSON,
  status VARCHAR(20) DEFAULT 'uploading',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES im_user(id),
  INDEX idx_upload_user_id (user_id)
);
```

## 前端组件改进

### MailPanel.vue

**新增功能**:
- 虚拟滚动列表
- 搜索栏（发件人、收件人、主题、日期）
- 标签过滤
- 快捷操作按钮（标记、删除、归档）
- 批量操作支持

### ComposeMailDialog.vue

**新增功能**:
- 自动保存草稿（每30秒）
- 分片上传附件
- 上传进度显示
- 邮件加密选项
- 草稿恢复提示

### MailDetailDialog.vue

**新增功能**:
- XSS防护的内容显示
- 标签显示和编辑
- 加密状态显示
- 快捷操作按钮

## 后端服务改进

### ImEmailService

**新增方法**:
- searchEmails(criteria) - 搜索邮件
- saveDraft(email) - 保存草稿
- applyRules(email) - 应用规则
- encryptEmail(email, key) - 加密邮件
- decryptEmail(email, key) - 解密邮件
- validateAttachment(file) - 验证附件

### ImEmailController

**新增端点**:
- POST /api/email/search - 搜索邮件
- POST /api/email/draft - 保存草稿
- POST /api/email/upload/init - 初始化分片上传
- POST /api/email/upload/chunk - 上传分片
- POST /api/email/upload/complete - 完成上传
- POST /api/email/rule - 创建规则
- GET /api/email/rule - 获取规则列表

## 性能优化

### 数据库优化

- 添加必要的索引，避免全表扫描
- 使用分页查询，避免一次加载过多数据
- 定期分析慢查询日志

### 缓存策略

- 缓存用户最近的邮件列表
- 缓存用户的规则和标签
- 设置合理的缓存过期时间

### 前端优化

- 虚拟滚动减少DOM节点
- 图片懒加载
- 请求去重和合并

## 安全性

### 权限验证

- 所有API端点都需要验证用户权限
- 用户只能访问自己的邮件
- 用户只能修改自己的规则和标签

### 输入验证

- 验证所有用户输入
- 对HTML内容进行XSS防护
- 验证附件类型和大小

### 数据保护

- 敏感数据加密存储
- 删除邮件时安全清理附件
- 记录所有操作日志

## 正确性属性

### 属性 1: 虚拟滚动完整性

*对于任何邮件列表，虚拟滚动渲染的邮件集合应该与完整列表的对应部分相同*

**验证: 需求 1.1**

### 属性 2: 邮件缓存一致性

*对于任何邮件，从缓存中获取的邮件数据应该与从数据库中获取的数据相同*

**验证: 需求 1.3**

### 属性 3: 草稿自动保存

*对于任何正在编写的邮件，每30秒应该自动保存一次，用户关闭编辑器后草稿应该仍然存在*

**验证: 需求 2.1, 2.2**

### 属性 4: 权限验证完整性

*对于任何邮件访问请求，系统应该验证用户权限，未授权的用户不能访问他人的邮件*

**验证: 需求 4.2, 7.3**

### 属性 5: XSS防护有效性

*对于任何包含HTML的邮件内容，清理后的内容不应该包含任何脚本标签或事件处理器*

**验证: 需求 4.1**

### 属性 6: 搜索结果准确性

*对于任何搜索条件，返回的所有邮件都应该满足搜索条件*

**验证: 需求 5.1**

### 属性 7: 规则应用一致性

*对于任何满足规则条件的邮件，应该应用相应的规则操作，且多次应用同一规则应该产生相同的结果*

**验证: 需求 5.2, 5.3**

### 属性 8: 附件上传完整性

*对于任何分片上传的文件，所有分片上传完成后合并的文件应该与原始文件相同*

**验证: 需求 3.1, 3.2**

### 属性 9: 加密解密往返

*对于任何邮件内容，加密后再解密应该得到原始内容*

**验证: 需求 6.2, 6.3**

### 属性 10: 附件类型验证

*对于任何上传的附件，系统应该验证文件类型，不允许上传危险文件类型*

**验证: 需求 3.3, 4.3**

