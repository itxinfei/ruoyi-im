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
 * 拍一拍互动实体
 *
 * 用于记录用户之间的拍一拍互动行为
 *
 * @author ruoyi
 */
@TableName("im_nudge")
@Data
public class ImNudge implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 拍一拍记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    @TableField("conversation_id")
    private Long conversationId;

    /** 发起者用户ID */
    @TableField("nudger_id")
    private Long nudgerId;

    /** 被拍用户ID */
    @TableField("nudged_user_id")
    private Long nudgedUserId;

    /** 连续拍拍次数 */
    @TableField("nudge_count")
    private Integer nudgeCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
