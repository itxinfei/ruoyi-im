package com.ruoyi.im.handler;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.exception.BusinessException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各类异常，返回标准的错误响应格式
 *
 * @author ruoyi
 */
@RestControllerAdvice
@Schema(description = "全局异常处理器")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常: URI={}, message={}", request.getRequestURI(), e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * 参数校验异常处理（@RequestBody @Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("参数校验异常: {}", e.getMessage());

        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.fail("参数校验失败: " + errorMsg);
    }

    /**
     * 绑定异常处理（@ModelAttribute @Valid）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBindException(BindException e) {
        logger.warn("绑定异常: {}", e.getMessage());

        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.fail("参数绑定失败: " + errorMsg);
    }

    /**
     * 约束违例异常处理（@Validated 单个参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        logger.warn("约束违例异常: {}", e.getMessage());

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMsg = violations.stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining(", "));
        return Result.fail("参数校验失败: " + errorMsg);
    }

    /**
     * 缺少请求参数异常处理
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        logger.warn("缺少请求参数: URI={}, param={}", request.getRequestURI(), e.getParameterName());
        return Result.fail("缺少必需参数: " + e.getParameterName());
    }

    /**
     * 参数类型不匹配异常处理
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        logger.warn("参数类型不匹配: URI={}, param={}, requiredType={}",
                request.getRequestURI(), e.getName(), e.getRequiredType());
        return Result.fail("参数类型错误: " + e.getName() + " 应为 " + e.getRequiredType().getSimpleName());
    }

    /**
     * 访问拒绝异常处理
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.warn("访问拒绝: URI={}, message={}", request.getRequestURI(), e.getMessage());
        return Result.fail("无权限访问该资源");
    }

    /**
     * 系统异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常: URI={}, message={}", request.getRequestURI(), e.getMessage(), e);
        return Result.fail("系统异常，请稍后重试");
    }
}
