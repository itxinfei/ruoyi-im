package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM用户实体
 *
 * @author ruoyi
 */
@TableName("im_user")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImUser extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，用于登录系统，唯一标识 */
    private String username;

    /** 密码，加密存储 */
    private String password;

    /** 昵称，用户在系统中显示的名称 */
    private String nickname;

    /** 头像，存储头像文件的URL路径 */
    private String avatar;

    /** 性别: 0=未知, 1=男, 2=女 */
    private Integer gender;

    /** 手机号，用于登录或接收短信通知 */
    private String mobile;

    /** 邮箱，用于找回密码或接收通知 */
    private String email;

    /** 状态: 0=禁用, 1=启用 */
    private Integer status;

    /** 用户角色: USER=普通用户, ADMIN=管理员, SUPER_ADMIN=超级管理员 */
    private String role;

    /** 签名,用户个人简介或个性签名 */
    private String signature;

    /** 部门 */
    private String department;

    /** 职位 */
    private String position;

    /** 最后在线时间,记录用户最近一次在线的时间 */
    @TableField("last_online_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;

    /** 登录状态（非数据库字段，用于会话管理） */
    @TableField(exist = false)
    private Boolean online;
}
