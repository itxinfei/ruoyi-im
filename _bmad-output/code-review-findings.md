# Code Review Findings - 2026-03-03

## 审查文件
- 19 个 Java 文件 (169 行新增, 74 行删除)

---

## 🔴 HIGH 严重问题

### 1. ImFileController.java - 文件下载未验证用户权限
- **位置:** `downloadFile` 和 `downloadFileByPath` 方法
- **问题:** 用户可以下载任意文件，未验证文件所有权
- **风险:** 可能导致敏感文件泄露

### 2. ImFileController.java - downloadFileByPath 路径验证不完整
- **位置:** 第 188-192 行
- **问题:** 验证 token 后未使用解析出的 userId，匿名用户仍可访问

---

## 🟡 MEDIUM 中等问题

### 3. ImPushDeviceServiceImpl.java - 日志泄露风险
- **位置:** 第 119 行
- **问题:** 异常日志中仍记录完整 deviceToken

### 4. ImFriendServiceImpl.java - 分布式锁事务问题
- **位置:** 第 328-335 行
- **问题:** 锁在事务外部获取但执行方法内部有 @Transactional

### 5. JwtUtils.java - 密钥强度验证不足
- **位置:** 第 37-40 行
- **问题:** 只检查长度，未检查密钥复杂度

---

## 🟢 LOW 改进建议

### 6. ImFileController.java - Content-Type 映射不完整
- 添加更多文件类型映射 (svg, json, xml 等)

### 7. 通用 - 缺少请求体大小限制配置检查

---

## ✅ 代码亮点

1. PermissionAspect.java - 使用 ErrorCode 枚举
2. JwtUtils.java - 配置缺失时抛出异常
3. ImPushDeviceServiceImpl.java - 设备令牌脱敏处理完善
4. ImFileController.java - 路径遍历防护完整
