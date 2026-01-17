package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门对象 im_department
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImDepartment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private Long id;

    /** 部门名称 */
    @Excel(name = "部门名称")
    private String name;

    /** 父部门ID，根部门为0 */
    @Excel(name = "父部门ID")
    private Long parentId;

    /** 父部门名称（关联查询） */
    private String parentName;

    /** 祖级列表，逗号分隔 */
    private String ancestors;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer orderNum;

    /** 负责人用户ID */
    @Excel(name = "负责人ID")
    private Long leaderId;

    /** 负责人姓名（关联查询） */
    @Excel(name = "负责人")
    private String leaderName;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 部门状态: 0=正常, 1=停用 */
    @Excel(name = "部门状态")
    private Integer status;

    /** 删除标志: 0=存在, 1=删除 */
    private Integer delFlag;

    /** 子部门列表 */
    private List<ImDepartment> children = new ArrayList<>();

    public Integer getStatus() {
        return status;
    }

    public String getStatusDisplay() {
        if (status == null) return "正常";
        return status == 0 ? "正常" : "停用";
    }
}
