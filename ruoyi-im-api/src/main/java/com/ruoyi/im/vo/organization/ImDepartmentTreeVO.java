package com.ruoyi.im.vo.organization;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门树形VO
 *
 * 用于返回部门树形结构数据
 *
 * @author ruoyi
 */
@Data
public class ImDepartmentTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private Long id;

    /** 部门名称 */
    private String name;

    /** 父部门ID */
    private Long parentId;

    /** 祖级列表 */
    private String ancestors;

    /** 显示顺序 */
    private Integer orderNum;

    /** 负责人用户ID */
    private Long leaderId;

    /** 负责人姓名 */
    private String leaderName;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 部门状态: 0=正常, 1=停用 */
    private Integer status;

    /** 部门描述 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 子部门列表 */
    private List<ImDepartmentTreeVO> children;

    /** 成员数量 */
    private Integer memberCount;
}
