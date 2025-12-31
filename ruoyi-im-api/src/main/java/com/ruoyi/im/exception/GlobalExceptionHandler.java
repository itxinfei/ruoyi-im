package com.ruoyi.im.exception;

import com.ruoyi.im.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: URI={}, 错误码={}, 消息={}", request.getRequestURI(), e.getErrorCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数校验异常: URI={}, 错误={}", request.getRequestURI(), errorMsg);
        return Result.error(400, errorMsg);
    }
    
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数绑定异常: URI={}, 错误={}", request.getRequestURI(), errorMsg);
        return Result.error(400, errorMsg);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.error("参数类型异常: URI={}, 参数名={}, 期望类型={}", 
                request.getRequestURI(), e.getName(), e.getRequiredType());
        return Result.error(400, "参数类型错误: " + e.getName());
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error("接口不存在: URI={}", request.getRequestURI());
        return Result.error(404, "接口不存在: " + e.getRequestURL());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("非法参数异常: URI={}, 消息={}", request.getRequestURI(), e.getMessage());
        return Result.error(400, e.getMessage());
    }
    
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: URI={}, 异常信息={}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统内部错误");
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: URI={}, 异常类型={}, 消息={}", 
                request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e);
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }
}
