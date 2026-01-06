package com.ruoyi.im.dto.organization;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 更新部门请求DTO
 *
 * 用于接收更新部门的请求参数
 *
 * @author ruoyi
 */
@Data
public class ImDepartmentUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    @NotNull(message = "部门ID不能为空")
    private Long id;

    /** 部门名称 */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50个字符")
    private String name;

    /** 父部门ID，根部门为0 */
    @NotNull(message = "父部门ID不能为空")
    private Long parentId;

    /** 显示顺序 */
    private Integer orderNum;

    /** 负责人用户ID */
    private Long leaderId;

    /** 联系电话 */
    @Pattern(regexp = "^1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$", message = "联系电话格式不正确")
    private String phone;

    /** 邮箱 */
    @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式不正确")
    private String email;

    /** 部门状态: 0=正常, 1=停用 */
    private Integer status;

    /** 部门描述 */
    @Size(max = 500, message = "部门描述长度不能超过500个字符")
    private String remark;
}
