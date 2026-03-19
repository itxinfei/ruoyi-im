package com.ruoyi.im.dto.ding;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * DING消息发送请求DTO
 *
 * @author ruoyi
 */
@Data

public class DingSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * DING类型
     * APP：应用内强提醒
     * SMS：短信提醒（需要第三方服务）
     * CALL：电话提醒（需要第三方服务）
     */
    
    private String dingType = "APP";

    /**
     * 优先级
     * URGENT：紧急
     * NORMAL：普通
     */
    
    private String priority = "NORMAL";

    /** DING内容 */
    
    @NotBlank(message = "DING内容不能为空")
    private String content;

    /** 目标用户ID列表（为空表示发送给会话中所有成员） */
    
    private List<Long> targetUsers;

    /** 过期时间（小时，默认24小时） */
    
    private Integer expireHours = 24;

    /** 是否需要回执 */
    
    private Boolean receiptRequired = true;

    // ==================== 兼容旧版字段 ====================

    /** 兼容旧版：接收者ID列表 */
    
    private Long[] receiverIds;

    /** 兼容旧版：是否紧急 */
    
    private Integer isUrgent = 0;
}

