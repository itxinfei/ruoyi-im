package com.ruoyi.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.exception.ImException;

/**
 * IM系统异常处理器
 * 
 * @author ruoyi
 */
@RestControllerAdvice
public class ImExceptionHandler
{
    private static final Logger logger = LoggerFactory.getLogger(ImExceptionHandler.class);

    /**
     * IM业务异常处理
     */
    @ExceptionHandler(ImException.class)
    public AjaxResult handleImException(ImException e)
    {
        logger.error("IM业务异常: {}", e.getMessage(), e);
        return AjaxResult.error(String.valueOf(e.getCode()), e.getMessage());
    }

    /**
     * 参数验证异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        logger.error("参数验证异常: {}", message, e);
        return AjaxResult.error("400", "参数验证失败: " + message);
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e)
    {
        logger.error("系统异常: {}", e.getMessage(), e);
        return AjaxResult.error("500", "系统异常，请稍后重试");
    }
}
