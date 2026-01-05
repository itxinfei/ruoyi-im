package com.ruoyi.im.common;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 * 
 * <p>该类用于封装分页查询的结果，包含总记录数、分页数据、当前页码、每页大小等信息。</p>
 * 
 * @param <T> 分页数据类型
 * @author ruoyi
 */
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 分页数据列表
     */
    private List<T> rows;
    
    /**
     * 当前页码（从1开始）
     */
    private int pageNum;
    
    /**
     * 每页显示记录数
     */
    private int pageSize;
    
    /**
     * 总页数
     */
    private int pages;
    
    /**
     * 是否有下一页
     */
    private boolean hasNextPage;
    
    /**
     * 是否有上一页
     */
    private boolean hasPreviousPage;
    
    /**
     * 默认构造函数
     */
    public PageResult() {
        this(0, null, 1, 1);
    }
    
    /**
     * 构造函数
     * 
     * @param total 总记录数
     * @param rows 分页数据列表
     * @param pageNum 当前页码
     * @param pageSize 每页显示记录数
     */
    public PageResult(long total, List<T> rows, int pageNum, int pageSize) {
        // 参数验证
        if (pageNum < 1) {
            pageNum = 1;
        }
        
        if (pageSize < 1) {
            pageSize = 10;
        }
        
        // 计算总页数
        this.total = total;
        this.rows = rows;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
        
        // 如果当前页码超出范围，修正为最后一页
        if (this.pageNum > this.pages && this.pages > 0) {
            this.pageNum = this.pages;
        }
        
        // 计算是否有上一页和下一页
        this.hasNextPage = this.pages > 0 && this.pageNum < this.pages;
        this.hasPreviousPage = this.pageNum > 1;
    }
    
    /**
     * 创建分页结果对象
     * 
     * @param <T> 分页数据类型
     * @param total 总记录数
     * @param rows 分页数据列表
     * @param pageNum 当前页码
     * @param pageSize 每页显示记录数
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(long total, List<T> rows, int pageNum, int pageSize) {
        return new PageResult<>(total, rows, pageNum, pageSize);
    }
    
    /**
     * 创建空的分页结果对象
     * 
     * @param <T> 分页数据类型
     * @param pageNum 当前页码
     * @param pageSize 每页显示记录数
     * @return 空的分页结果对象
     */
    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(0, null, pageNum, pageSize);
    }
    
    /**
     * 获取总记录数
     * 
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }
    
    /**
     * 设置总记录数
     * 
     * @param total 总记录数
     */
    public void setTotal(long total) {
        this.total = total;
    }
    
    /**
     * 获取分页数据列表
     * 
     * @return 分页数据列表
     */
    public List<T> getRows() {
        return rows;
    }
    
    /**
     * 设置分页数据列表
     * 
     * @param rows 分页数据列表
     */
    public void setRows(List<T> rows) {
        this.rows = rows;
    }
    
    /**
     * 获取当前页码
     * 
     * @return 当前页码
     */
    public int getPageNum() {
        return pageNum;
    }
    
    /**
     * 设置当前页码
     * 
     * @param pageNum 当前页码
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
    
    /**
     * 获取每页显示记录数
     * 
     * @return 每页显示记录数
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * 设置每页显示记录数
     * 
     * @param pageSize 每页显示记录数
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    /**
     * 获取总页数
     * 
     * @return 总页数
     */
    public int getPages() {
        return pages;
    }
    
    /**
     * 设置总页数
     * 
     * @param pages 总页数
     */
    public void setPages(int pages) {
        this.pages = pages;
    }
    
    /**
     * 判断是否还有下一页
     * 
     * @return 是否有下一页
     */
    public boolean isHasNextPage() {
        return hasNextPage;
    }
    
    /**
     * 设置是否有下一页
     * 
     * @param hasNextPage 是否有下一页
     */
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }
    
    /**
     * 判断是否还有上一页
     * 
     * @return 是否有上一页
     */
    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }
    
    /**
     * 设置是否有上一页
     * 
     * @param hasPreviousPage 是否有上一页
     */
    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }
    
    /**
     * 判断是否为第一页
     * 
     * @return 是否为第一页
     */
    public boolean isFirstPage() {
        return pageNum == 1;
    }
    
    /**
     * 判断是否为最后一页
     * 
     * @return 是否为最后一页
     */
    public boolean isLastPage() {
        return pages == 0 || pageNum >= pages;
    }
    
    /**
     * 获取当前页的起始记录索引（从0开始）
     * 
     * @return 起始记录索引
     */
    public long getStartIndex() {
        return (long) (pageNum - 1) * pageSize;
    }
    
    /**
     * 获取当前页的结束记录索引（从0开始）
     * 
     * @return 结束记录索引
     */
    public long getEndIndex() {
        return Math.min(getStartIndex() + pageSize - 1, total - 1);
    }
    
    /**
     * 获取当前页的记录数
     * 
     * @return 当前页的记录数
     */
    public long getCurrentPageSize() {
        if (rows == null) {
            return 0;
        }
        return rows.size();
    }
    
    /**
     * 获取分页信息的描述字符串
     * 
     * @return 分页信息描述
     */
    public String getPageInfo() {
        return String.format("第%d页/%d页，每页%d条，共%d条记录", pageNum, pages, pageSize, total);
    }
    
    /**
     * 判断是否为空结果集
     * 
     * @return 是否为空结果集
     */
    public boolean isEmpty() {
        return total == 0 || rows == null || rows.isEmpty();
    }
    
    /**
     * 获取当前页索引（0开始）
     * 
     * @return 当前页索引
     */
    public int getPageIndex() {
        return pageNum - 1;
    }
    
    /**
     * 获取总记录数的显示形式（超过10000显示为"10000+"）
     * 
     * @return 总记录数的显示形式
     */
    public String getTotalDisplay() {
        if (total > 10000) {
            return "10000+";
        }
        return String.valueOf(total);
    }
    
    /**
     * 获取分页导航信息
     * 
     * @return 分页导航信息
     */
    public String getNavigationInfo() {
        if (isEmpty()) {
            return "暂无数据";
        }
        
        long start = getStartIndex() + 1;
        long end = Math.min(getEndIndex() + 1, total);
        
        return String.format("显示第 %d - %d 条，共 %d 条记录", start, end, total);
    }
    
    /**
     * 创建带有偏移量和限制的分页结果
     * 
     * @param <T> 分页数据类型
     * @param offset 偏移量
     * @param limit 限制数量
     * @param rows 数据行
     * @return 分页结果对象
     */
    public static <T> PageResult<T> withOffsetLimit(int offset, int limit, List<T> rows) {
        if (limit <= 0) {
            return empty(1, 10);
        }
        
        int pageNum = offset / limit + 1;
        return new PageResult<>(rows.size(), rows, pageNum, limit);
    }
    
    /**
     * 转换为字符串表示
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "PageResult{" +
                "total=" + total +
                ", rows=" + (rows != null ? rows.size() : 0) +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", pages=" + pages +
                ", hasNextPage=" + hasNextPage +
                ", hasPreviousPage=" + hasPreviousPage +
                '}';
    }
}