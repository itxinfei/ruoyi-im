package com.ruoyi.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友申请实体
 *
 * 对应数据库表 im_friend_request
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImFriendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 申请人ID
     */
    private Long fromUserId;

    /**
     * 接收人ID
     */
    private Long toUserId;

    /**
     * 申请消息
     */
    private String message;

    /**
     * 状态: PENDING待处理 APPROVED已同意 REJECTED已拒绝
     */
    private String status;

    /**
     * 申请时间
     */
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    private LocalDateTime handledTime;

    // ========== 关联查询字段（非数据库字段） ==========

    /**
     * 申请人用户名
     */
    private String fromUsername;

    /**
     * 申请人昵称
     */
    private String fromNickname;

    /**
     * 申请人头像
     */
    private String fromAvatar;

    /**
     * 接收人用户名
     */
    private String toUsername;

    /**
     * 接收人昵称
     */
    private String toNickname;

}
