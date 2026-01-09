package com.ruoyi.im.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户登录请求
 *
 * @author ruoyi
 */
@Data
public class ImLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 客户端类型: web, mobile, pc */
    private String clientType;
}
