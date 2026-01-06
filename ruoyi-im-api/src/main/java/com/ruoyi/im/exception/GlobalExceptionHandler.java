package com.ruoyi.im.exception;

import com.ruoyi.im.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 
 * 统一处理系统中的各种异常，返回标准化的错误响应
 * 
 * @author ruoyi
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     *
     * 捕获并处理系统中抛出的业务异常，返回标准化的错误响应
     *
     * @param e 业务异常对象，包含错误码和错误信息
     * @param request HTTP请求对象，用于记录请求路径
     * @return 包含错误码和错误信息的响应对象
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", e.getMessage(), request.getRequestURI());
        
        // 如果异常包含错误码，则使用错误码
        if (e.getErrorCode() != null) {
            return Result.error(e.getCode(), e.getMessage() + " [" + e.getErrorCode() + "]");
        }
        
        return Result.error(e.getCode(), e.getMessage());
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
        log.warn("非法参数: {}", e.getMessage());
        return Result.error(400, "参数错误: " + e.getMessage());
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
        log.error("系统异常: {} - {}", e.getMessage(), request.getRequestURI(), e);
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}