# 异常处理改进指南

**改进日期**: 2026-03-02
**改进状态**: 进行中

---

## 🎯 异常处理最佳实践

### 1. 不要使用过于宽泛的异常捕获

**❌ 不好的实践**:
```java
try {
    // 业务逻辑
} catch (Exception e) {
    // 吞掉所有异常
}
```

**✅ 好的实践**:
```java
try {
    // 业务逻辑
} catch (BusinessException e) {
    // 处理业务异常
    log.error("业务异常: {}", e.getMessage(), e);
    throw e;  // 重新抛出或转换为合适的异常
} catch (RuntimeException e) {
    // 处理运行时异常
    log.error("运行时异常: {}", e.getMessage(), e);
    throw new BusinessException("系统繁忙，请稍后重试");
} catch (Exception e) {
    // 处理其他异常
    log.error("未知异常: {}", e.getMessage(), e);
    throw new BusinessException("系统错误，请联系管理员");
}
```

### 2. 不要吞掉异常

**❌ 不好的实践**:
```java
try {
    userService.save(user);
} catch (Exception e) {
    // 异常被吞掉，无法追踪问题
}
```

**✅ 好的实践**:
```java
try {
    userService.save(user);
} catch (Exception e) {
    log.error("保存用户失败: userId={}", user.getId(), e);
    throw new BusinessException("保存用户失败", e);
}
```

### 3. 使用具体的异常类型

**❌ 不好的实践**:
```java
throw new Exception("用户不存在");
```

**✅ 好的实践**:
```java
throw new BusinessException(ImErrorCode.USER_NOT_FOUND, "用户不存在");
throw new BusinessException(ImErrorCode.INVALID_PARAMETER, "参数错误: userId不能为空");
```

### 4. 提供有意义的错误信息

**❌ 不好的实践**:
```java
throw new RuntimeException("Error");
```

**✅ 好的实践**:
```java
throw new BusinessException(
    ImErrorCode.USER_NOT_FOUND, 
    "用户不存在: userId=" + userId
);
```

### 5. 记录完整的异常信息

**❌ 不好的实践**:
```java
catch (Exception e) {
    log.error("错误: {}", e.getMessage());
}
```

**✅ 好的实践**:
```java
catch (Exception e) {
    log.error("处理请求失败: url={}, params={}", url, params, e);
}
```

---

## 🔍 当前代码中的异常处理问题

### 发现的问题

1. **过于宽泛的异常捕获**
   - 部分代码使用 `catch (Exception e)` 捕获所有异常
   - 可能隐藏具体问题

2. **异常信息不完整**
   - 部分异常日志缺少关键信息
   - 难以追踪问题

3. **异常转换不完整**
   - 部分异常被转换为通用异常
   - 丢失了原始异常信息

---

## 📋 异常处理改进建议

### 1. 创建统一的异常处理类

```java
@Component
public class ExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);
    
    /**
     * 处理业务异常
     */
    public void handleBusinessException(BusinessException e, String context) {
        log.error("业务异常[{}]: {} - {}", context, e.getCode(), e.getMessage(), e);
        throw e;
    }
    
    /**
     * 处理运行时异常
     */
    public void handleRuntimeException(RuntimeException e, String context) {
        log.error("运行时异常[{}]: {}", context, e.getMessage(), e);
        throw new BusinessException("系统繁忙，请稍后重试", e);
    }
    
    /**
     * 处理检查异常
     */
    public void handleIllegalArgumentException(IllegalArgumentException e, String context) {
        log.warn("参数验证失败[{}]: {}", context, e.getMessage());
        throw new BusinessException(ImErrorCode.INVALID_PARAMETER, e.getMessage());
    }
}
```

### 2. 使用自定义异常类

```java
public class BusinessException extends RuntimeException {
    
    private final String code;
    private final Object[] args;
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.args = null;
    }
    
    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.args = null;
    }
    
    public BusinessException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }
    
    public String getCode() {
        return code;
    }
    
    public Object[] getArgs() {
        return args;
    }
}
```

### 3. 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().stream()
            .map(error -> error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        log.warn("参数验证失败: {}", errorMsg);
        return Result.error(ImErrorCode.INVALID_PARAMETER, errorMsg);
    }
    
    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝: {}", e.getMessage());
        return Result.error(ImErrorCode.FORBIDDEN, "没有访问权限");
    }
    
    /**
     * 处理未认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.error(ImErrorCode.UNAUTHORIZED, "未登录或Token已过期");
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(ImErrorCode.SYSTEM_ERROR, "系统错误，请联系管理员");
    }
}
```

---

## 📊 改进优先级

### 高优先级（立即改进）
1. WebSocket 相关的异常处理
2. 消息发送/接收的异常处理
3. 文件上传/下载的异常处理

### 中优先级（近期改进）
1. 用户管理的异常处理
2. 权限验证的异常处理
3. 数据访问层的异常处理

### 低优先级（可选改进）
1. 日志记录的异常处理
2. 定时任务的异常处理
3. 系统配置的异常处理

---

## ✅ 改进清单

- [ ] 创建统一的异常处理类
- [ ] 创建自定义异常类
- [ ] 创建全局异常处理器
- [ ] 改进 WebSocket 异常处理
- [ ] 改进消息服务异常处理
- [ ] 改进文件服务异常处理
- [ ] 改进用户服务异常处理
- [ ] 改进权限验证异常处理
- [ ] 改进数据访问层异常处理

---

## 🎯 预期效果

1. **更好的错误追踪**: 详细的异常日志便于问题排查
2. **更好的用户体验**: 友好的错误提示
3. **更好的系统稳定性**: 不会因为异常被吞掉而导致隐藏问题
4. **更好的代码可维护性**: 统一的异常处理模式

---

**改进完成时间**: 待定
**改进人员**: AI Code Reviewer