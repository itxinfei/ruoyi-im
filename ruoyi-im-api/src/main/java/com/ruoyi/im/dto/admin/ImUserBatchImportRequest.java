package com.ruoyi.im.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 批量导入用户请求
 */
@Data
public class ImUserBatchImportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "用户列表不能为空")
    private List<UserItem> users;

    @Data
    public static class UserItem implements Serializable {
        private static final long serialVersionUID = 1L;

        @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        private String username;

        @Size(max = 50, message = "昵称长度不能超过50")
        private String nickname;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String mobile;

        @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
        private String password;

        @Pattern(regexp = "^(USER|ADMIN|SUPER_ADMIN)$", message = "角色只能为 USER、ADMIN 或 SUPER_ADMIN")
        private String role = "USER";
    }
}
