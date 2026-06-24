package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 内存指标VO
 */
@Data
public class MemoryMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 堆内存信息 */
    private MemoryDetailVO heap;

    /** 非堆内存信息 */
    private MemoryDetailVO nonHeap;
}
