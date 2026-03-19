package com.ruoyi.im.dto.ding;

import com.ruoyi.im.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DING消息查询请求DTO
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)

public class DingQueryRequest extends PageRequest {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    
    private Long conversationId;

    /** 发送者ID */
    
    private Long senderId;

    /** DING类型 */
    
    private String dingType;

    /** 优先级 */
    
    private String priority;

    /** 状态 */
    
    private String status;

    /** 是否只查询未读 */
    
    private Boolean unreadOnly = false;
}

