package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 运行时信息VO
 */
@Data
public class RuntimeMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 启动时间 */
    private String startTime;

    /** 运行时间 */
    private String uptime;

    /** 启动类路径 */
    private String bootClassPath;
}
