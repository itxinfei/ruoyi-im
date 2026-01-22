package com.ruoyi.im.domain.dto;

import lombok.Data;
import javax.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

/**
 * IM用户更新请求DTO
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Data
public class ImUserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Length(min = 2, max = 50, message = "昵称长度必须在2-50个字符之间")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Length(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Length(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    @Min(value = 0, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;

    @Length(max = 200, message = "个性签名长度不能超过200个字符")
    private String signature;

    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    private Integer status;
}