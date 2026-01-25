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
 * 视频通话参与者实体
 * 用于支持多人视频通话（最多9人）
 *
 * @author ruoyi
 */
@TableName("im_video_call_participant")
@Data
public class ImVideoCallParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通话ID */
    @TableField("call_id")
    private Long callId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 用户名称（冗余字段，方便查询） */
    @TableField("user_name")
    private String userName;

    /** 用户头像（冗余字段，方便查询） */
    @TableField("user_avatar")
    private String userAvatar;

    /** 参与状态：INVITED已邀请, JOINED已加入, LEFT已离开, REJECTED已拒绝 */
    private String status;

    /** 加入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("join_time")
    private LocalDateTime joinTime;

    /** 离开时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("leave_time")
    private LocalDateTime leaveTime;

    /** 是否静音 */
    @TableField("is_muted")
    private Boolean isMuted;

    /** 是否关闭摄像头 */
    @TableField("is_camera_off")
    private Boolean isCameraOff;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
