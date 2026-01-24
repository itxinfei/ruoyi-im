package com.ruoyi.web.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户快速操作结果VO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImUserQuickOperationResultVO {

    /**
     * 操作ID，用于跟踪批量操作
     */
    private String operationId;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 操作状态: success-成功, failed-失败, partial-部分成功
     */
    private String status;

    /**
     * 总操作数量
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 跳过数量
     */
    private Integer skippedCount;

    /**
     * 成功的用户ID列表
     */
    private List<Long> successUserIds;

    /**
     * 失败的用户ID列表
     */
    private List<Long> failedUserIds;

    /**
     * 错误详情，用户ID -> 错误信息
     */
    private Map<Long, String> errorDetails;

    /**
     * 操作结果消息
     */
    private String message;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    /**
     * 操作执行人
     */
    private String operator;

    /**
     * 操作详情链接（如导出文件下载链接）
     */
    private String detailUrl;

    /**
     * 额外的操作结果数据
     */
    private Map<String, Object> extraData;

    /**
     * 是否可以重试失败的操作
     */
    private Boolean retryable;

    /**
     * 重试建议
     */
    private String retrySuggestion;

    /**
     * 计算成功率
     */
    public double getSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 判断是否完全成功
     */
    public boolean isAllSuccess() {
        return "success".equals(status) && 
               totalCount != null && 
               successCount != null && 
               totalCount.equals(successCount);
    }

    /**
     * 判断是否部分成功
     */
    public boolean isPartialSuccess() {
        return "partial".equals(status);
    }

    /**
     * 判断是否完全失败
     */
    public boolean isAllFailed() {
        return "failed".equals(status);
    }
}