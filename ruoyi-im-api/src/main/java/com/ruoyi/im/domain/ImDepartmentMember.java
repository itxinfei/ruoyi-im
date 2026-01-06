package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM部门成员关系实体
 *
 * 用于存储用户与部门的关联关系，支持用户在多个部门
 *
 * @author ruoyi
 */
@TableName("im_department_member")
@Data
public class ImDepartmentMember extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关系ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 部门ID */
    private Long departmentId;

    /** 用户ID */
    private Long userId;

    /** 是否为主部门: 0=否, 1=是，每个用户只能有一个主部门 */
    private Integer isPrimary;

    /** 职位名称 */
    private String position;

    /** 入职时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    /** 离职时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveTime;

    /** 状态: 0=在职, 1=离职 */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
