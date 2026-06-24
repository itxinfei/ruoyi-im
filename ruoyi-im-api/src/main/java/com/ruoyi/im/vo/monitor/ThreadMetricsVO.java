package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 线程指标VO
 */
@Data
public class ThreadMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前线程数 */
    private int count;

    /** 峰值线程数 */
    private int peak;

    /** 守护线程数 */
    private int daemonCount;

    /** 已启动线程总数 */
    private long totalStarted;
}
