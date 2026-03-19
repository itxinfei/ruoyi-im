package com.ruoyi.im.common;

import lombok.Data;

/**
 * 分页查询请求基类
 *
 * @author ruoyi
 */
@Data

public class PageRequest {

    
    private Integer pageNum = 1;

    
    private Integer pageSize = 10;

    
    private String orderBy;

    
    private String orderDirection;

    /**
     * 设置排序方向，自动验证并转换为大写
     * 防止SQL注入攻击
     */
    public void setOrderDirection(String orderDirection) {
        if (orderDirection != null && !orderDirection.isEmpty()) {
            String upper = orderDirection.toUpperCase();
            if (!"ASC".equals(upper) && !"DESC".equals(upper)) {
                throw new IllegalArgumentException("无效的排序方向: " + orderDirection + "，仅支持ASC或DESC");
            }
            this.orderDirection = upper;
        } else {
            this.orderDirection = null;
        }
    }

    /**
     * 设置排序字段，安全过滤
     * 防止SQL注入攻击
     */
    public void setOrderBy(String orderBy) {
        if (orderBy != null) {
            // 过滤SQL关键字和特殊字符
            String filtered = orderBy.replaceAll("[^a-zA-Z0-9_]", "");
            this.orderBy = filtered.isEmpty() ? null : filtered;
        } else {
            this.orderBy = null;
        }
    }
}

