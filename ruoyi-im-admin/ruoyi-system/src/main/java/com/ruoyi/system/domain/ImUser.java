package com.ruoyi.system.domain;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IM用户对象 im_user
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Excel(name = "用户名")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空", groups = {Insert.class})
    private String password;

    /** 昵称 */
    @Excel(name = "昵称")
    private String nickname;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    @Excel(name = "邮箱")
    private String email;

    /** 电话 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "电话号码格式不正确")
    @Excel(name = "电话")
    private String phone;

    /** 头像 */
    private String avatar;

    /** 状态（ACTIVE/INACTIVE/BANNED） */
    @Excel(name = "状态")
    private String status;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /** 更新时间 */
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;

    /** 新增分组 */
    public interface Insert {}

    /** 修改分组 */
    public interface Update {}
}