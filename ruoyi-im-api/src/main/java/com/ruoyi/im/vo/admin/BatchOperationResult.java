package com.ruoyi.im.vo.admin;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作结果 VO
 * 用于统一返回批量操作的执行结果
 *
 * @author ruoyi
 */
public class BatchOperationResult {

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 失败项明细（可选，包含失败原因）
     */
    private List<FailedItem> failedItems;

    /**
     * 总数量
     */
    private Integer totalCount;

    public BatchOperationResult() {
        this.successCount = 0;
        this.failCount = 0;
        this.totalCount = 0;
        this.failedItems = new ArrayList<>();
    }

    public BatchOperationResult(Integer successCount, Integer failCount) {
        this.successCount = successCount;
        this.failCount = failCount;
        this.totalCount = successCount + failCount;
        this.failedItems = new ArrayList<>();
    }

    /**
     * 添加失败项
     *
     * @param id 失败项 ID
     * @param reason 失败原因
     */
    public void addFailedItem(Long id, String reason) {
        this.failedItems.add(new FailedItem(id, reason));
        this.failCount = this.failedItems.size();
        this.totalCount = this.successCount + this.failCount;
    }

    // Getters and Setters

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
        this.totalCount = this.successCount + this.failCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
        this.totalCount = this.successCount + this.failCount;
    }

    public List<FailedItem> getFailedItems() {
        return failedItems;
    }

    public void setFailedItems(List<FailedItem> failedItems) {
        this.failedItems = failedItems;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 失败项内部类
     */
    public static class FailedItem {
        private Long id;
        private String reason;

        public FailedItem() {
        }

        public FailedItem(Long id, String reason) {
            this.id = id;
            this.reason = reason;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
