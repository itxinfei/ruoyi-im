package com.ruoyi.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 批量选择状态管理DTO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImBatchSelectionDTO {

    /**
     * 选择操作类型: select-选中, unselect-取消选中, clear-清空, selectAll-全选
     */
    private String operation;

    /**
     * 页面标识，用于区分不同页面的选择状态
     */
    private String pageKey;

    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Integer totalCount;

    /**
     * 当前页的用户ID列表
     */
    private List<Long> currentPageIds;

    /**
     * 要操作的ID列表
     */
    private List<Long> targetIds;

    /**
     * 是否跨页全选
     */
    private Boolean crossPageSelectAll = false;

    /**
     * 过滤条件，用于记忆选择的上下文
     */
    private Map<String, Object> filterConditions;

    /**
     * 排序条件
     */
    private Map<String, String> sortConditions;

    /**
     * 用户会话ID
     */
    private String sessionId;

    /**
     * 操作时间戳
     */
    private Long timestamp;

    /**
     * 验证操作是否有效
     */
    public boolean isValid() {
        if (operation == null || operation.trim().isEmpty()) {
            return false;
        }
        
        switch (operation) {
            case "select":
            case "unselect":
                return targetIds != null && !targetIds.isEmpty();
            case "clear":
            case "selectAll":
                return true;
            default:
                return false;
        }
    }

    /**
     * 判断是否为全选操作
     */
    public boolean isSelectAllOperation() {
        return "selectAll".equals(operation);
    }

    /**
     * 判断是否为清空操作
     */
    public boolean isClearOperation() {
        return "clear".equals(operation);
    }

    /**
     * 获取选择键，用于缓存
     */
    public String getSelectionKey() {
        return String.format("batch_selection_%s_%s", sessionId, pageKey);
    }
}