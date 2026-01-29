package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM部门实体
 *
 * 用于存储IM系统中的部门组织架构信息，支持树形结构
 *
 * @author ruoyi
 */
@TableName("im_department")
@Data
@EqualsAndHashCode(callSuper = false)
public class ImDepartment extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 部门名称 */
    private String name;

    /** 父部门ID，根部门为0 */
    private Long parentId;

    /** 祖级列表，逗号分隔，如：0,1,2 */
    private String ancestors;

    /** 显示顺序，数字越小越靠前 */
    private Integer orderNum;

    /** 负责人用户ID */
    private Long leaderId;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 部门状态: 0=正常, 1=停用 */
    private Integer status;

    /** 删除标志: 0=存在, 1=删除 */
    private Integer delFlag;

    /** 部门描述 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
