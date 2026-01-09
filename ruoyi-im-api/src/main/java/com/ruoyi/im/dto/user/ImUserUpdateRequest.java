package com.ruoyi.im.dto.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户信息更新请求
 *
 * @author ruoyi
 */
@Data
public class ImUserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 昵称 */
    @Size(min = 2, max = 50, message = "昵称长度必须在2-50个字符之间")
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /** 性别: 0=未知, 1=男, 2=女 */
    @Min(value = 0, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;

    /** 签名 */
    @Size(max = 200, message = "签名长度不能超过200个字符")
    private String signature;
}
