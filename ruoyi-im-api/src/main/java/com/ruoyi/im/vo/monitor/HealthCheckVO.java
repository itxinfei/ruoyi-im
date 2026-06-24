package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 健康检查VO
 */
@Data
public class HealthCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 健康状态: HEALTHY / WARNING */
    private String status;

    /** 内存使用率 */
    private String memoryUsage;
}
