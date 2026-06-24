package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统监控概览VO
 */
@Data
public class MonitorOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 系统状态 */
    private String status;

    /** 时间戳 */
    private String timestamp;

    /** JVM信息 */
    private JvmInfoVO jvm;

    /** 线程信息 */
    private ThreadMetricsVO thread;

    /** 内存信息 */
    private OverviewMemoryVO memory;
}
