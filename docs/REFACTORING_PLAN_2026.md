# 重构实施计划 (Refactoring Implementation Plan)

**日期**: 2026-02-10

## 1. 立即执行的任务 (Immediate Actions)

### 1.1 标记废弃类
- **目标**: `com.ruoyi.im.controller.ImSessionController`
- **操作**: 添加 `@Deprecated` 注解，并在类注释中说明替代类为 `ImConversationController`。

### 1.2 优化未读数更新 (临时优化)
- **目标**: `ImMessageServiceImpl.updateUnreadCount`
- **操作**: 虽然尚未引入 MQ，但可以使用 `CompletableFuture.runAsync` 将未读数更新和 Redis 清除操作放入线程池执行，避免阻塞主线程。

## 2. 代码重构示例

### 2.1 引入消息处理策略模式
创建 `com.ruoyi.im.strategy.message` 包：
```java
public interface MessageHandler {
    boolean supports(String msgType);
    void handle(ImMessage message);
}
```
这将消除 `ImMessageServiceImpl` 中大量的 `if-else` 判断。

### 2.2 统一异常处理
确保 `GlobalExceptionHandler` 覆盖所有自定义异常，并返回统一的 `Result` 结构。

## 3. 验证标准

1.  **单元测试**: 核心 Service 的测试覆盖率需达到 80% 以上。
2.  **性能测试**: 使用 JMeter 模拟 500 并发发送消息，RT < 200ms。
3.  **代码扫描**: SonarQube 扫描无 Blocker/Critical 级别漏洞。
