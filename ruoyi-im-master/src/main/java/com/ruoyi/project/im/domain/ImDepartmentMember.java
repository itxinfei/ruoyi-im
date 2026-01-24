package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 部门成员关系对象 im_department_member
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImDepartmentMember extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关系ID */
    private Long id;

    /** 部门ID */
    @Excel(name = "部门ID")
    private Long departmentId;

    /** 部门名称（关联查询） */
    @Excel(name = "部门名称")
    private String departmentName;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名（关联查询） */
    @Excel(name = "用户名")
    private String userName;

    /** 用户昵称（关联查询） */
    @Excel(name = "用户昵称")
    private String userNickname;

    /** 是否为主部门: 0=否, 1=是 */
    @Excel(name = "是否主部门")
    private Integer isPrimary;

    /** 职位名称 */
    @Excel(name = "职位")
    private String position;

    /** 入职时间 */
    @Excel(name = "入职时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date joinTime;

    /** 离职时间 */
    @Excel(name = "离职时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date leaveTime;

    /** 状态: 0=在职, 1=离职 */
    @Excel(name = "状态")
    private Integer status;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 获取状态显示文本
     */
    public String getStatusDisplay() {
        if (status == null) {
            return "在职";
        }
        return status == 0 ? "在职" : "离职";
    }

    /**
     * 获取是否主部门显示文本
     */
    public String getIsPrimaryDisplay() {
        if (isPrimary == null) {
            return "否";
        }
        return isPrimary == 1 ? "是" : "否";
    }
}
