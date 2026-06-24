package com.ruoyi.im.vo.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * JVM信息VO
 */
@Data
public class JvmInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Java版本 */
    private String javaVersion;

    /** Java厂商 */
    private String javaVendor;

    /** Java安装路径 */
    private String javaHome;

    /** 操作系统名称 */
    private String osName;

    /** 操作系统版本 */
    private String osVersion;

    /** 系统架构 */
    private String osArch;

    /** 可用处理器数量 */
    private int processors;

    /** 运行时间 */
    private String uptime;

    /** JVM启动参数 */
    private List<String> inputArguments;
}
