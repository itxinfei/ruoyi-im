package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 概览内存信息VO（用于overview接口的嵌套memory字段）
 */
@Data
public class OverviewMemoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总内存 */
    private String total;

    /** 已使用内存 */
    private String used;

    /** 空闲内存 */
    private String free;

    /** 最大内存 */
    private String max;

    /** 使用率百分比 */
    private Double usagePercent;
}
