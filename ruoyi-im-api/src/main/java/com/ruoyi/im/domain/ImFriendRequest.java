package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友申请实体
 *
 * @author ruoyi
 */
@TableName("im_friend_request")
@Data
public class ImFriendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请人用户ID */
    @TableField("from_user_id")
    private Long fromUserId;

    /** 被申请人用户ID */
    @TableField("to_user_id")
    private Long toUserId;

    /** 申请消息 */
    private String message;

    /** 状态（PENDING待处理 APPROVED已同意 REJECTED已拒绝） */
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handledTime;

    // ==================== 非数据库字段 ====================

    /** 申请人名称 */
    @TableField(exist = false)
    private String fromUserName;

    /** 申请人头像 */
    @TableField(exist = false)
    private String fromUserAvatar;
}
