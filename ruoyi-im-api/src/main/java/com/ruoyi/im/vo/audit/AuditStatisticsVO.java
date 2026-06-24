package com.ruoyi.im.vo.audit;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计统计信息VO
 */
@Data
public class AuditStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总操作数 */
    private int totalOperations;

    /** 成功次数 */
    private long successCount;

    /** 失败次数 */
    private long failCount;

    /** 查询开始时间 */
    private LocalDateTime startTime;

    /** 查询结束时间 */
    private LocalDateTime endTime;
}
