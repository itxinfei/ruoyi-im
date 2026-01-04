package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IM群组对象 im_group
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 群组ID */
    private Long id;

    /** 群组名称 */
    @Excel(name = "群组名称")
    private String name;

    /** 群主ID */
    @Excel(name = "群主ID")
    private Long ownerId;

    /** 群主名称 */
    @Excel(name = "群主名称")
    private String ownerName;

    /** 群组描述 */
    @Excel(name = "群组描述")
    private String description;

    /** 群组头像 */
    private String avatar;

    /** 群组状态（NORMAL/MUTED/DISBANDED） */
    @Excel(name = "群组状态")
    private String status;

    /** 成员数量 */
    @Excel(name = "成员数量")
    private Integer memberCount;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /** 更新时间 */
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
}