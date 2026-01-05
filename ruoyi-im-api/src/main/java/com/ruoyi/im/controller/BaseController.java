package com.ruoyi.im.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.utils.SecurityUtils;
import com.ruoyi.im.utils.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Web层通用数据处理基类
 * 
 * 提供统一的分页处理、参数验证、错误处理、响应格式化等功能
 * 
 * @author ruoyi
 * @since 2024-01-05
 */
public class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取当前登录用户ID
     * 
     * @return 当前用户ID，如果未登录返回null
     */
    protected Long getCurrentUserId() {
        try {
            return SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            logger.warn("获取当前用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 设置请求分页数据
     * 
     * @param pageNum 页码，从1开始
     * @param pageSize 每页数量，默认20
     */
    protected void startPage(Integer pageNum, Integer pageSize) {
        if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
            PageHelper.startPage(pageNum, pageSize);
        } else {
            // 设置默认值
            PageHelper.startPage(1, 20);
        }
    }
    
    /**
     * 设置请求分页数据（使用默认值）
     */
    protected void startPage() {
        PageHelper.startPage(1, 20);
    }
    
    /**
     * 清理分页的线程变量
     */
    protected void clearPage() {
        PageHelper.clearPage();
    }

    /**
     * 响应请求分页数据
     * 
     * @param list 数据列表
     * @return 分页响应结果
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T> Result<PageResult<T>> getDataTable(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setRows(list);
        pageResult.setPageNum(pageInfo.getPageNum());
        pageResult.setPageSize(pageInfo.getPageSize());
        pageResult.setPages(pageInfo.getPages());
        pageResult.setHasNextPage(pageInfo.isHasNextPage());
        pageResult.setHasPreviousPage(pageInfo.isHasPreviousPage());
        return Result.success(pageResult);
    }

    /**
     * 处理参数验证错误
     * 
     * @param bindingResult 验证结果
     * @return 错误响应
     */
    protected Result<Void> handleValidationError(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = bindingResult.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            
            logger.warn("参数验证失败: {}", errorMap);
            return Result.error(400, "参数验证失败: " + errorMap.toString());
        }
        return null;
    }

    /**
     * 获取请求参数并分页
     * 
     * @param request HTTP请求
     * @return 包含分页参数的Map
     */
    protected Map<String, Object> getPageParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        
        // 基础分页参数
        Integer pageNum = ServletUtils.getParameterToInt(request, "pageNum");
        Integer pageSize = ServletUtils.getParameterToInt(request, "pageSize");
        
        // 设置默认值
        params.put("pageNum", pageNum != null && pageNum > 0 ? pageNum : 1);
        params.put("pageSize", pageSize != null && pageSize > 0 ? Math.min(pageSize, 100) : 20);
        
        return params;
    }

    /**
     * 统一成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public <T> Result<T> success(T data) {
        return Result.success(data);
    }

    /**
     * 统一成功响应（无数据）
     * 
     * @return 成功响应
     */
    public Result<Void> success() {
        return Result.success();
    }

    /**
     * 统一错误响应
     * 
     * @param message 错误消息
     * @return 错误响应
     */
    public Result<Void> error(String message) {
        return Result.error(500, message);
    }

    /**
     * 统一错误响应（自定义状态码）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @return 错误响应
     */
    public Result<Void> error(int code, String message) {
        return Result.error(code, message);
    }

    /**
     * 统一业务异常响应
     * 
     * @param message 错误消息
     * @return 业务异常响应
     */
    public Result<Void> businessError(String message) {
        return Result.error(400, message);
    }

    /**
     * 统一未授权响应
     * 
     * @param message 错误消息
     * @return 未授权响应
     */
    public Result<Void> unauthorized(String message) {
        return Result.error(401, message != null ? message : "未授权访问");
    }

    /**
     * 统一禁止访问响应
     * 
     * @param message 错误消息
     * @return 禁止访问响应
     */
    public Result<Void> forbidden(String message) {
        return Result.error(403, message != null ? message : "禁止访问");
    }

    /**
     * 统一资源不存在响应
     * 
     * @param message 错误消息
     * @return 资源不存在响应
     */
    public Result<Void> notFound(String message) {
        return Result.error(404, message != null ? message : "资源不存在");
    }

    /**
     * 统一请求错误响应
     * 
     * @param message 错误消息
     * @return 请求错误响应
     */
    public Result<Void> badRequest(String message) {
        return Result.error(400, message != null ? message : "请求错误");
    }
}