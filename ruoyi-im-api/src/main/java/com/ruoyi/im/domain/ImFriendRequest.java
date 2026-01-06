package com.ruoyi.im.domain;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友申请实体
 *
 * 用于存储IM系统中的好友申请信息，包括申请发起、状态跟踪、处理时间等
 * 支持好友申请的完整流程管理，从发起申请到同意或拒绝的整个生命周期
 *
 * @author ruoyi
 */
@Data
public class ImFriendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，唯一标识好友申请记录
     */
    private Long id;

    /**
     * 申请人用户ID，发起好友申请的用户ID
     */
    private Long fromUserId;

    /**
     * 被申请人用户ID，接收好友申请的用户ID
     */
    private Long toUserId;

    /**
     * 申请消息，申请人添加好友时的附言或说明
     */
    private String message;

    /**
     * 状态（PENDING待处理 APPROVED已同意 REJECTED已拒绝）
     * PENDING: 申请已发送，等待对方处理
     * APPROVED: 申请已被对方同意，双方成为好友
     * REJECTED: 申请已被对方拒绝，无法成为好友
     */
    private String status;

    /**
     * 创建时间，好友申请发起的时间
     */
    private LocalDateTime createTime;

    /**
     * 处理时间，被申请人处理申请的时间（同意或拒绝的时间）
     */
    private LocalDateTime handledTime;

    /**
     * 申请人名称，非数据库字段，用于显示申请人的昵称
     */
    private String fromUserName;

    /**
     * 申请人头像，非数据库字段，用于显示申请人的头像URL
     */
    private String fromUserAvatar;
}
