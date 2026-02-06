package com.ruoyi.im.exception;

import com.ruoyi.im.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 全局异常处理器
 *
 * 统一处理系统中的各种异常，返回标准化的错误响应
 * 支持错误追踪ID，便于日志关联和问题排查
 *
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 错误追踪ID的MDC键名 */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 生成错误追踪ID
     * 用于关联日志和错误响应，便于问题追踪
     *
     * @return 追踪ID
     */
    private String generateTraceId() {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID_KEY, traceId);
        return traceId;
    }

    /**
     * 清除MDC中的追踪ID
     */
    private void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 构建包含追踪ID的错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @return 包含追踪ID的响应对象
     */
    private <T> Result<T> buildErrorResult(int code, String message) {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null) {
            return Result.error(code, message + " (追踪ID: " + traceId + ")");
        }
        return Result.error(code, message);
    }

    /**
     * 处理限流异常
     *
     * 捕获并处理接口限流异常，返回429状态码
     * 提示用户请求过于频繁，需稍后重试
     *
     * @param e 限流异常对象，包含限流key和限制次数
     * @param request HTTP请求对象，用于记录请求路径
     * @return 包含429状态码和限流提示的响应对象
     */
    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleRateLimitException(RateLimitException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        try {
            log.warn("接口限流[{}]: key={}, count={}, uri={}",
                     traceId, e.getLimitKey(), e.getLimitCount(), request.getRequestURI());
            return Result.error(429, "请求过于频繁，请稍后重试");
        } finally {
            clearTraceId();
        }
    }

    /**
     * 处理业务异常
     *
     * 捕获并处理系统中抛出的业务异常，返回标准化的错误响应
     * 包含错误追踪ID，便于日志关联和问题排查
     *
     * @param e 业务异常对象，包含错误码和错误信息
     * @param request HTTP请求对象，用于记录请求路径
     * @return 包含错误码、错误信息和追踪ID的响应对象
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        try {
            log.warn("业务异常[{}]: {} - URI={}, Code={}, ErrorCode={}",
                     traceId, e.getMessage(), request.getRequestURI(), e.getCode(), e.getErrorCode());

            // 返回包含追踪ID的错误响应
            if (e.getErrorCode() != null) {
                return Result.error(e.getCode(), e.getMessage() + " [追踪ID:" + traceId + "]");
            }
            return buildErrorResult(e.getCode(), e.getMessage());
        } finally {
            clearTraceId();
        }
    }

    /**
     * 处理参数校验异常
     *
     * 捕获并处理方法参数校验失败的异常，返回包含字段级错误信息的响应
     *
     * @param e 方法参数校验异常对象，包含字段错误信息
     * @return 包含字段名和错误信息映射的响应对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return Result.error(400, "参数校验失败", errors);
    }

    /**
     * 处理数据绑定异常
     *
     * 捕获并处理数据绑定失败的异常，返回包含字段级错误信息的响应
     *
     * @param e 数据绑定异常对象，包含绑定错误信息
     * @return 包含字段名和错误信息映射的响应对象
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e) {
        log.warn("数据绑定失败: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return Result.error(400, "数据绑定失败", errors);
    }

    /**
     * 处理约束违规异常
     *
     * 捕获并处理@Validated注解在方法参数上的验证失败异常
     * 用于处理@PathVariable、@RequestParam等参数上的验证注解
     *
     * @param e 约束违规异常对象，包含验证错误信息
     * @return 包含字段名和错误信息映射的响应对象
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("参数验证失败: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String propertyName = violation.getPropertyPath().toString();
            // 获取最后一个节点作为字段名
            String fieldName = propertyName.contains(".")
                ? propertyName.substring(propertyName.lastIndexOf('.') + 1)
                : propertyName;
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        return Result.error(400, "参数验证失败", errors);
    }

    /**
     * 处理认证异常
     *
     * 捕获并处理用户认证失败的异常，提示用户重新登录
     *
     * @param e 认证异常对象，包含认证失败信息
     * @return 包含401错误码和提示信息的响应对象
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.error(401, "认证失败，请重新登录");
    }

    /**
     * 处理权限异常
     *
     * 捕获并处理用户权限不足的异常，提示用户无权访问资源
     *
     * @param e 权限异常对象，包含权限拒绝信息
     * @return 包含403错误码和提示信息的响应对象
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足，无法访问此资源");
    }

    /**
     * 处理非法参数异常
     *
     * 捕获并处理非法参数异常，返回参数错误信息
     *
     * @param e 非法参数异常对象，包含参数错误信息
     * @return 包含400错误码和参数错误信息的响应对象
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        String traceId = generateTraceId();
        try {
            log.warn("非法参数[{}]: {}", traceId, e.getMessage());
            return buildErrorResult(400, "参数错误: " + e.getMessage());
        } finally {
            clearTraceId();
        }
    }

    /**
     * 处理空指针异常
     *
     * 捕获并处理空指针异常，通常是代码逻辑问题
     *
     * @param e 空指针异常对象
     * @param request HTTP请求对象
     * @return 包含500错误码的响应对象
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        try {
            log.error("空指针异常[{}]: {} - {}", traceId, e.getMessage(), request.getRequestURI(), e);
            return buildErrorResult(500, "系统内部错误 [追踪ID:" + traceId + "]");
        } finally {
            clearTraceId();
        }
    }

    /**
     * 处理数据库异常
     *
     * 捕获并处理数据库操作异常
     *
     * @param e 数据库异常对象
     * @param request HTTP请求对象
     * @return 包含500错误码的响应对象
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleDataAccessException(org.springframework.dao.DataAccessException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        try {
            log.error("数据库异常[{}]: {} - {}", traceId, e.getMessage(), request.getRequestURI(), e);
            return buildErrorResult(500, "数据操作失败，请稍后重试 [追踪ID:" + traceId + "]");
        } finally {
            clearTraceId();
        }
    }

    /**
     * 处理数据完整性违反异常
     *
     * 捕获并处理违反数据完整性约束的异常（如唯一键冲突）
     *
     * @param e 数据完整性异常对象
     * @param request HTTP请求对象
     * @return 包含409错误码的响应对象
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        String traceId = generateTraceId();
        try {
            log.warn("数据完整性违反[{}]: {} - {}", traceId, e.getMessage(), request.getRequestURI());
            // 判断是否为唯一键冲突
            String message = e.getMessage();
            if (message != null && message.contains("Duplicate entry")) {
                return buildErrorResult(409, "数据已存在，请勿重复提交");
            }
            return buildErrorResult(409, "数据完整性约束违反");
        } finally {
            clearTraceId();
        }
    }

    /**
     * 处理缺失请求参数异常
     *
     * 捕获并处理缺少必需的请求参数异常
     *
     * @param e 缺失请求参数异常对象
     * @return 包含400错误码的响应对象
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺失请求参数: 参数名={}, 参数类型={}", e.getParameterName(), e.getParameterType());
        return Result.error(400, "缺少必需参数: " + e.getParameterName());
    }

    /**
     * 处理参数类型不匹配异常
     *
     * 捕获并处理请求参数类型不匹配异常
     *
     * @param e 参数类型不匹配异常对象
     * @return 包含400错误码的响应对象
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: 参数名={}, 期望类型={}", e.getName(), e.getRequiredType());
        return Result.error(400, "参数类型错误: " + e.getName());
    }

    /**
     * 处理请求体不可读异常
     *
     * 捕获并处理请求体格式错误（如JSON格式错误）
     *
     * @param e 请求体不可读异常对象
     * @return 包含400错误码的响应对象
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return Result.error(400, "请求体格式错误，请检查JSON格式");
    }

    /**
     * 处理404异常
     *
     * 捕获并处理请求路径不存在的异常
     *
     * @param e 404异常对象
     * @param request HTTP请求对象
     * @return 包含404错误码的响应对象
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("请求路径不存在: {}", request.getRequestURI());
        return Result.error(404, "请求的资源不存在");
    }

    /**
     * 处理系统异常
     *
     * 捕获并处理所有未明确处理的系统异常，返回通用的错误响应
     * 此方法作为异常处理的最后防线，确保所有异常都能被正确处理
     *
     * @param e 系统异常对象，包含异常堆栈信息
     * @param request HTTP请求对象，用于记录请求路径
     * @return 包含500错误码和通用错误信息的响应对象
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        String traceId = generateTraceId();
        try {
            log.error("系统异常[{}]: {} - {}", traceId, e.getMessage(), request.getRequestURI(), e);
            return buildErrorResult(500, "系统内部错误 [追踪ID:" + traceId + "]");
        } finally {
            clearTraceId();
        }
    }
}