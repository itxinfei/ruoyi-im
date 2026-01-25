package com.ruoyi.im.dto.ding;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DING消息发送请求")
public class DingSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @Schema(description = "会话ID")
    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    /**
     * DING类型
     * APP：应用内强提醒
     * SMS：短信提醒（需要第三方服务）
     * CALL：电话提醒（需要第三方服务）
     */
    @Schema(description = "DING类型：APP应用内/SMS短信/CALL电话")
    private String dingType = "APP";

    /**
     * 优先级
     * URGENT：紧急
     * NORMAL：普通
     */
    @Schema(description = "优先级：URGENT紧急/NORMAL普通")
    private String priority = "NORMAL";

    /** DING内容 */
    @Schema(description = "DING内容")
    @NotBlank(message = "DING内容不能为空")
    private String content;

    /** 目标用户ID列表（为空表示发送给会话中所有成员） */
    @Schema(description = "目标用户ID列表")
    private List<Long> targetUsers;

    /** 过期时间（小时，默认24小时） */
    @Schema(description = "过期时间（小时）")
    private Integer expireHours = 24;

    /** 是否需要回执 */
    @Schema(description = "是否需要回执")
    private Boolean receiptRequired = true;

    // ==================== 兼容旧版字段 ====================

    /** 兼容旧版：接收者ID列表 */
    @Schema(description = "接收者ID列表（兼容旧版）")
    private Long[] receiverIds;

    /** 兼容旧版：是否紧急 */
    @Schema(description = "是否紧急（兼容旧版）")
    private Integer isUrgent = 0;
}
