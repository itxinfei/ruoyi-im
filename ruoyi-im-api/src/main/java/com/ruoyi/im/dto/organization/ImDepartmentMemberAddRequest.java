package com.ruoyi.im.dto.organization;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 添加部门成员请求DTO
 *
 * 用于接收添加部门成员的请求参数
 *
 * @author ruoyi
 */
@Data
public class ImDepartmentMemberAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 是否为主部门: 0=否, 1=是 */
    private Integer isPrimary;

    /** 职位名称 */
    @Size(max = 50, message = "职位名称长度不能超过50个字符")
    private String position;
}
