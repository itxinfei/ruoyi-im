package com.ruoyi.im.vo.ding;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DING消息VO
 *
 * @author ruoyi
 */
@Data

public class DingMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** DING消息ID */
    
    private Long id;

    /** 会话ID */
    
    private Long conversationId;

    /** 发送者ID */
    
    private Long senderId;

    /** 发送者名称 */
    
    private String senderName;

    /** 发送者头像 */
    
    private String senderAvatar;

    /** DING类型 */
    
    private String dingType;

    /** 优先级 */
    
    private String priority;

    /** DING内容 */
    
    private String content;

    /** 已读人数 */
    
    private Integer readCount;

    /** 发送人数 */
    
    private Integer sendCount;

    /** 过期时间 */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /** 状态 */
    
    private String status;

    /** 创建时间 */
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 是否已读（当前用户） */
    
    private Boolean isRead;

    /** 已读用户列表 */
    
    private List<DingReadUserVO> readUsers;
}

