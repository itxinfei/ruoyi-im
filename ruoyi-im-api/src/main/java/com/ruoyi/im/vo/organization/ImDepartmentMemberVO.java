package com.ruoyi.im.vo.organization;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门成员VO
 *
 * 用于返回部门成员信息
 *
 * @author ruoyi
 */
@Data
public class ImDepartmentMemberVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关系ID */
    private Long id;

    /** 部门ID */
    private Long departmentId;

    /** 部门名称 */
    private String departmentName;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String mobile;

    /** 是否为主部门: 0=否, 1=是 */
    private Integer isPrimary;

    /** 职位名称 */
    private String position;

    /** 入职时间 */
    private LocalDateTime joinTime;

    /** 状态: 0=在职, 1=离职 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
