package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 编译信息VO
 */
@Data
public class CompilationMetricsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 编译器名称 */
    private String name;

    /** 总编译时间（毫秒） */
    private long totalCompilationTime;
}
