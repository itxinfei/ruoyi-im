package com.ruoyi.im.common;

import java.io.Serializable;
import java.util.List;

public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private long total;
    
    private List<T> rows;
    
    private int pageNum;
    
    private int pageSize;
    
    private int pages;
    
    public PageResult() {
    }
    
    public PageResult(long total, List<T> rows, int pageNum, int pageSize) {
        this.total = total;
        this.rows = rows;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) ((total + pageSize - 1) / pageSize);
    }
    
    public static <T> PageResult<T> of(long total, List<T> rows, int pageNum, int pageSize) {
        return new PageResult<>(total, rows, pageNum, pageSize);
    }
    
    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(0, null, pageNum, pageSize);
    }
    
    public long getTotal() {
        return total;
    }
    
    public void setTotal(long total) {
        this.total = total;
    }
    
    public List<T> getRows() {
        return rows;
    }
    
    public void setRows(List<T> rows) {
        this.rows = rows;
    }
    
    public int getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public int getPages() {
        return pages;
    }
    
    public void setPages(int pages) {
        this.pages = pages;
    }
    
    @Override
    public String toString() {
        return "PageResult{" +
                "total=" + total +
                ", rows=" + rows +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", pages=" + pages +
                '}';
    }
}
