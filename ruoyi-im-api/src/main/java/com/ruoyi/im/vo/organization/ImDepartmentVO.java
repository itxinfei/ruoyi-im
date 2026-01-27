package com.ruoyi.im.vo.organization;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门视图对象
 * 用于返回给前端的部门数据，与数据库实体 ImDepartment 分离
 *
 * @author ruoyi
 */
@Data
public class ImDepartmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private Long id;

    /** 部门名称 */
    private String name;

    /** 父部门ID，根部门为0 */
    private Long parentId;

    /** 父部门名称 */
    private String parentName;

    /** 祖级列表，逗号分隔 */
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

    /** 部门状态：0=正常, 1=停用 */
    private Integer status;

    /** 部门成员数量（非数据库字段） */
    private Integer memberCount;

    /** 部门描述 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
