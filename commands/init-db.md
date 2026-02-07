---
name: init-db
description: 初始化 RuoYi-IM 数据库（创建数据库和导入初始数据）
---

# RuoYi-IM 数据库初始化命令

初始化 RuoYi-IM 项目所需的数据库结构和基础数据。

## 执行步骤

1. **创建数据库**
   ```sql
   CREATE DATABASE IF NOT EXISTS im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```

2. **导入数据库脚本**
   ```bash
   mysql -u root -p im < im.sql
   ```

3. **验证初始化结果**
   - 检查表结构是否完整
   - 验证初始数据是否导入成功
   - 确认索引和约束是否正确创建

## 数据库表说明

- `im_user`: 用户表
- `im_friend`: 好友关系表
- `im_group`: 群组表
- `im_group_member`: 群组成员表
- `im_message`: 消息表
- `im_conversation`: 会话表
- `im_file`: 文件表

## 初始数据

- 默认管理员账号: admin/123456
- 系统基础配置
- 默认角色权限

## 注意事项

- 执行前备份现有数据库（如果存在）
- 确保 MySQL 版本为 5.7+ 或 8.0+
- 字符集必须为 utf8mb4 以支持表情符号