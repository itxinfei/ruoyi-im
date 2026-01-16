package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户设备对象 im_user_device
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImUserDevice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 设备ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名（关联查询） */
    @Excel(name = "用户名")
    private String username;

    /** 昵称（关联查询） */
    @Excel(name = "昵称")
    private String nickname;

    /** 设备类型: PC/MOBILE/WEB */
    @Excel(name = "设备类型")
    private String deviceType;

    /** 设备唯一标识 */
    @Excel(name = "设备标识")
    private String deviceId;

    /** 设备名称 */
    @Excel(name = "设备名称")
    private String deviceName;

    /** 操作系统版本 */
    @Excel(name = "系统版本")
    private String osVersion;

    /** 应用版本 */
    @Excel(name = "应用版本")
    private String appVersion;

    /** IP地址 */
    @Excel(name = "IP地址")
    private String ipAddress;

    /** 位置 */
    @Excel(name = "位置")
    private String location;

    /** 状态: ONLINE在线 OFFLINE离开 */
    @Excel(name = "状态")
    private String status;

    /** 最后活跃时间 */
    @Excel(name = "最后活跃时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastActiveTime;

}
