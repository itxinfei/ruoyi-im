package com.ruoyi.web.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 批量选择状态响应VO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImBatchSelectionStatusVO {

    /**
     * 页面标识
     */
    private String pageKey;

    /**
     * 当前选中的用户ID列表
     */
    private List<Long> selectedIds;

    /**
     * 当前页选中的用户ID列表
     */
    private List<Long> currentPageSelectedIds;

    /**
     * 是否跨页全选
     */
    private Boolean crossPageSelectAll = false;

    /**
     * 当前页面全选状态
     */
    private Boolean currentPageAllSelected = false;

    /**
     * 总选中数量
     */
    private Integer totalSelectedCount = 0;

    /**
     * 当前页数量
     */
    private Integer currentPageCount = 0;

    /**
     * 总记录数
     */
    private Integer totalCount = 0;

    /**
     * 当前页码
     */
    private Integer currentPage = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 过滤条件指纹（用于判断过滤条件是否变化）
     */
    private String filterFingerprint;

    /**
     * 选择状态过期时间
     */
    private Long expireTime;

    /**
     * 是否可以选择
     */
    private Boolean selectable = true;

    /**
     * 选择限制信息
     */
    private String selectionLimit;

    /**
     * 计算选中率
     */
    public double getSelectionRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) totalSelectedCount / totalCount * 100;
    }

    /**
     * 判断是否全选所有记录
     */
    public boolean isAllSelected() {
        return totalCount != null && 
               totalSelectedCount != null && 
               totalCount.equals(totalSelectedCount) &&
               crossPageSelectAll;
    }

    /**
     * 判断当前页是否全选
     */
    public boolean isCurrentPageAllSelected() {
        return currentPageCount != null && 
               currentPageSelectedIds != null &&
               currentPageCount.equals(currentPageSelectedIds.size());
    }

    /**
     * 获取选择摘要信息
     */
    public String getSelectionSummary() {
        if (crossPageSelectAll) {
            return String.format("已全选所有 %d 条记录", totalCount);
        } else if (totalSelectedCount > 0) {
            if (totalSelectedCount.equals(currentPageSelectedIds.size())) {
                return String.format("已选择当前页 %d 条记录", totalSelectedCount);
            } else {
                return String.format("已选择 %d 条记录（跨页选择）", totalSelectedCount);
            }
        } else {
            return "未选择任何记录";
        }
    }

    /**
     * 更新选中数量
     */
    public void updateSelectedCount() {
        if (selectedIds != null) {
            totalSelectedCount = selectedIds.size();
        }
        if (currentPageSelectedIds != null) {
            currentPageCount = currentPageSelectedIds.size();
        }
    }

    /**
     * 添加选中项
     */
    public void addSelection(Long id) {
        if (selectedIds == null) {
            selectedIds = new java.util.ArrayList<>();
        }
        if (!selectedIds.contains(id)) {
            selectedIds.add(id);
            updateSelectedCount();
        }
    }

    /**
     * 移除选中项
     */
    public void removeSelection(Long id) {
        if (selectedIds != null) {
            selectedIds.remove(id);
            updateSelectedCount();
        }
    }

    /**
     * 清空所有选择
     */
    public void clearAllSelections() {
        if (selectedIds != null) {
            selectedIds.clear();
        }
        if (currentPageSelectedIds != null) {
            currentPageSelectedIds.clear();
        }
        crossPageSelectAll = false;
        currentPageAllSelected = false;
        updateSelectedCount();
    }

    /**
     * 设置当前页全选
     */
    public void setCurrentPageAllSelected(List<Long> pageIds) {
        this.currentPageSelectedIds = new java.util.ArrayList<>(pageIds);
        this.currentPageAllSelected = true;
        updateSelectedCount();
    }

    /**
     * 判断选择是否有效
     */
    public boolean isSelectionValid() {
        if (expireTime != null) {
            return System.currentTimeMillis() < expireTime;
        }
        return true;
    }
}