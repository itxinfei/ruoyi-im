package com.ruoyi.web.exception;

import com.ruoyi.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理各类异常，返回标准格式的错误信息
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public AjaxResult handleBusinessException(BusinessException e) {
        logger.error("业务异常：{}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        logger.warn("参数验证失败：{}", message);
        return AjaxResult.error("参数验证失败：" + message);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public AjaxResult handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        
        logger.warn("约束验证失败：{}", message);
        return AjaxResult.error("参数验证失败：" + message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        logger.warn("参数绑定失败：{}", message);
        return AjaxResult.error("参数绑定失败：" + message);
    }

    /**
     * 处理文件上传大小异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public AjaxResult handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        logger.warn("文件上传大小超限：{}", e.getMessage());
        return AjaxResult.error("上传文件大小超过限制：" + e.getMessage());
    }

    /**
     * 处理数据访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    public AjaxResult handleDataAccessException(DataAccessException e) {
        logger.error("数据访问异常", e);
        return AjaxResult.error("系统繁忙，请稍后重试");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public AjaxResult handleNullPointerException(NullPointerException e) {
        logger.error("空指针异常", e);
        return AjaxResult.error("系统内部错误，请联系管理员");
    }

    /**
     * 处理类转换异常
     */
    @ExceptionHandler(ClassCastException.class)
    public AjaxResult handleClassCastException(ClassCastException e) {
        logger.error("类型转换异常", e);
        return AjaxResult.error("数据类型错误");
    }

    /**
     * 处理数组越界异常
     */
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public AjaxResult handleArrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException e) {
        logger.error("数组越界异常", e);
        return AjaxResult.error("数据访问错误");
    }

    /**
     * 处理数字格式异常
     */
    @ExceptionHandler(NumberFormatException.class)
    public AjaxResult handleNumberFormatException(NumberFormatException e) {
        logger.warn("数字格式异常：{}", e.getMessage());
        return AjaxResult.error("数字格式错误");
    }

    /**
     * 处理算术异常
     */
    @ExceptionHandler(ArithmeticException.class)
    public AjaxResult handleArithmeticException(ArithmeticException e) {
        logger.error("算术异常", e);
        return AjaxResult.error("计算错误");
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public AjaxResult handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数异常：{}", e.getMessage());
        return AjaxResult.error("参数错误：" + e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常", e);
        return AjaxResult.error("系统运行异常：" + e.getMessage());
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        logger.error("未处理的异常，请求ID: {}", requestId, e);
        
        // 敏感环境隐藏详细错误信息
        String errorMessage = "系统异常，请稍后重试";
        if (isDevelopmentEnvironment()) {
            errorMessage = e.getMessage();
        }
        
        return AjaxResult.error(errorMessage);
    }

    /**
     * 处理自定义IM异常
     */
    @ExceptionHandler(ImException.class)
    public AjaxResult handleImException(ImException e) {
        logger.error("IM业务异常：{}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 处理消息发送异常
     */
    @ExceptionHandler(MessageSendException.class)
    public AjaxResult handleMessageSendException(MessageSendException e) {
        logger.error("消息发送异常：{}", e.getMessage());
        return AjaxResult.error("消息发送失败：" + e.getMessage());
    }

    /**
     * 处理敏感词过滤异常
     */
    @ExceptionHandler(SensitiveWordException.class)
    public AjaxResult handleSensitiveWordException(SensitiveWordException e) {
        logger.warn("敏感词检测异常：{}", e.getMessage());
        return AjaxResult.error("消息内容包含敏感词：" + e.getMessage());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public AjaxResult handleAuthenticationException(AuthenticationException e) {
        logger.warn("认证异常：{}", e.getMessage());
        return AjaxResult.error(401, "认证失败：" + e.getMessage());
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    public AjaxResult handleAuthorizationException(AuthorizationException e) {
        logger.warn("授权异常：{}", e.getMessage());
        return AjaxResult.error(403, "权限不足：" + e.getMessage());
    }

    /**
     * 判断是否为开发环境
     */
    private boolean isDevelopmentEnvironment() {
        String env = System.getProperty("spring.profiles.active");
        return "dev".equals(env) || "test".equals(env);
    }
}