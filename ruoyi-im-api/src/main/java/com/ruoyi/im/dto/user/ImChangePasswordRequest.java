package com.ruoyi.im.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改密码请求
 *
 * @author ruoyi
 */
@Data
public class ImChangePasswordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 旧密码 */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
    private String newPassword;
}
