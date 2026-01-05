package com.ruoyi.common.core.controller;

import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.utils.SecurityUtils;
import com.ruoyi.im.utils.ServletUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 基础Controller
 * 
 * @author ruoyi
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
}