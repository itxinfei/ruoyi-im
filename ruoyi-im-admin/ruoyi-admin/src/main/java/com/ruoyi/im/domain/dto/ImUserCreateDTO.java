package com.ruoyi.im.domain.dto;

import com.ruoyi.common.annotation.Excel;
import lombok.Data;
import javax.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

/**
 * IM用户创建请求DTO
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Data
public class ImUserCreateDTO {

    @Excel(name = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Length(min = 4, max = 50, message = "用户名长度必须在4-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Excel(name = "密码")
    @NotBlank(message = "密码不能为空")
    @Length(min = 8, max = 128, message = "密码长度必须在8-128个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]", 
             message = "密码必须包含大小写字母、数字和特殊字符")
    private String password;

    @Excel(name = "昵称")
    @NotBlank(message = "昵称不能为空")
    @Length(min = 2, max = 50, message = "昵称长度必须在2-50个字符之间")
    private String nickname;

    @Excel(name = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Length(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Excel(name = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Excel(name = "头像")
    @Length(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知")
    @Min(value = 0, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;

    @Excel(name = "个性签名")
    @Length(max = 200, message = "个性签名长度不能超过200个字符")
    private String signature;

    @Excel(name = "状态", readConverterExp = "0=正常,1=禁用")
    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    private Integer status = 0;

    @Excel(name = "部门ID")
    @NotNull(message = "部门不能为空")
    private Long deptId;

    @Excel(name = "角色ID数组")
    @NotEmpty(message = "角色不能为空")
    private Long[] roleIds;
}