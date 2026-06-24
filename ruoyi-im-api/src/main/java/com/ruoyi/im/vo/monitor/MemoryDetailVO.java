package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 内存详情VO（堆/非堆共用）
 */
@Data
public class MemoryDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 初始内存 */
    private String init;

    /** 已使用内存 */
    private String used;

    /** 已提交内存 */
    private String committed;

    /** 最大内存 */
    private String max;

    /** 使用率百分比（仅堆内存有值） */
    private Double usagePercent;
}
