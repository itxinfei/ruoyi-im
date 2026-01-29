package com.ruoyi.im.vo.contact;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友视图对象
 *
 * @author ruoyi
 */
@Data
public class ImFriendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 好友关系ID
     */
    private Long id;

    /**
     * 好友用户ID
     */
    private Long friendId;

    /**
     * 好友名称
     */
    private String friendName;

    /**
     * 好友头像
     */
    private String friendAvatar;

    /**
     * 备注
     */
    private String remark;

    /**
     * 好友分组
     */
    private String groupName;

    /**
     * 好友状态（NORMAL正常 BLOCKED已拉黑 DELETED已删除）
     */
    private String status;

    /**
     * 在线状态
     */
    private Boolean online;

    /**
     * 添加时间
     */
    private LocalDateTime createTime;

    /**
     * 好友用户名
     */
    private String username;

    /**
     * 好友邮箱
     */
    private String email;

    /**
     * 好友手机号
     */
    private String phone;

    /**
     * 好友个性签名
     */
    private String signature;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

    /**
     * 好友标签，多个标签用逗号分隔
     */
    private String tags;
}
