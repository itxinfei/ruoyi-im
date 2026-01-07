package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM用户实体
 *
 * 用于存储IM系统中的用户基本信息，包括登录凭证、个人资料、状态信息等
 *
 * @author ruoyi
 */
@TableName("im_user")
@Data
public class ImUser extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，用于登录系统，唯一标识 */
    private String username;

    /** 昵称，用户在系统中显示的名称 */
    private String nickname;

    /** 密码，加密存储 */
    private String password;

    /** 头像，存储头像文件的URL路径 */
    private String avatar;

    /** 邮箱，用于找回密码或接收通知 */
    private String email;

    /** 手机号，用于登录或接收短信通知 */
    private String mobile;

    /** 性别: 0=未知, 1=男, 2=女 */
    private Integer gender;

    /** 状态: ACTIVE=在线, OFFLINE=离线 */
    private String status;

    /** 签名，用户个人简介或个性签名 */
    private String signature;

    /** 最后登录时间，记录用户最近一次登录的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /** 最后登录IP，记录用户最近一次登录的IP地址 */
    private String lastLoginIp;
}