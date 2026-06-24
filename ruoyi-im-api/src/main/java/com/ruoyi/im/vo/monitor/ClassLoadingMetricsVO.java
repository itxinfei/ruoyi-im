package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 类加载指标VO
 */
@Data
public class ClassLoadingMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 已加载类数量 */
    private int loadedClassCount;

    /** 累计加载类数量 */
    private long totalLoadedClassCount;

    /** 已卸载类数量 */
    private long unloadedClassCount;
}
