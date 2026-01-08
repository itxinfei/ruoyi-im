package com.ruoyi.web.exception;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.html.EscapeUtil;
import com.ruoyi.common.utils.security.PermissionUtils;
import com.ruoyi.system.exception.ImException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * IM系统异常处理器
 *
 * @author ruoyi
 */
@RestControllerAdvice
@Order(1) // 设置较低优先级，让GlobalExceptionHandler优先处理通用异常
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
     * 权限校验异常（未认证）
     * 处理Shiro的UnauthenticatedException，用户未登录时返回正确提示
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public Object handleUnauthenticatedException(UnauthenticatedException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        logger.error("请求地址'{}',未认证访问: {}", requestURI, e.getMessage());
        if (ServletUtils.isAjaxRequest(request))
        {
            AjaxResult result = new AjaxResult();
            result.put("code", 401);
            result.put("msg", "您尚未登录，请先登录");
            return result;
        }
        else
        {
            return new ModelAndView("redirect:/login");
        }
    }

    /**
     * 权限校验异常（无权限）
     * 处理Shiro的AuthorizationException，用户无权限时返回正确提示
     */
    @ExceptionHandler(AuthorizationException.class)
    public Object handleAuthorizationException(AuthorizationException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        logger.error("请求地址'{}',权限校验失败'{}'", requestURI, e.getMessage());
        if (ServletUtils.isAjaxRequest(request))
        {
            AjaxResult result = new AjaxResult();
            result.put("code", 403);
            result.put("msg", PermissionUtils.getMsg(e.getMessage()));
            return result;
        }
        else
        {
            return new ModelAndView("error/unauth");
        }
    }

    /**
     * 通用异常处理（兜底处理）
     * 只处理未被上述方法捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        // 如果是权限相关异常，不应该被这里捕获，但由于优先级问题可能到这里
        // 所以需要特殊处理
        if (e instanceof AuthorizationException)
        {
            return (AjaxResult) handleAuthorizationException((AuthorizationException) e, request);
        }
        logger.error("请求地址'{}',发生系统异常: {}", requestURI, e.getMessage(), e);
        return AjaxResult.error("系统异常: " + EscapeUtil.clean(e.getMessage()));
    }
}
