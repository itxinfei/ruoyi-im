package com.ruoyi.im.vo.reaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息反应VO
 *
 * @author ruoyi
 */
@Data
public class ImMessageReactionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 反应ID */
    private Long id;

    /** 消息ID */
    private Long messageId;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String userNickname;

    /** 用户头像 */
    private String userAvatar;

    /** 反应类型 */
    private String reactionType;

    /** emoji表情 */
    private String emoji;

    /** 反应数量（统计用） */
    private Integer count;

    /** 是否当前用户反应 */
    private Boolean isMine;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
