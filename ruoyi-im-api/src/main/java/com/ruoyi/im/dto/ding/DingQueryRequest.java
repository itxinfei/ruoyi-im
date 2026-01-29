package com.ruoyi.im.dto.ding;

import com.ruoyi.im.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DING消息查询请求DTO
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DING消息查询请求")
public class DingQueryRequest extends PageRequest {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @Schema(description = "会话ID")
    private Long conversationId;

    /** 发送者ID */
    @Schema(description = "发送者ID")
    private Long senderId;

    /** DING类型 */
    @Schema(description = "DING类型")
    private String dingType;

    /** 是否紧急 */
    @Schema(description = "是否紧急：0否/1是")
    private Integer isUrgent;

    /** 状态 */
    @Schema(description = "状态")
    private String status;

    /** 是否只查询未读 */
    @Schema(description = "是否只查询未读")
    private Boolean unreadOnly = false;
}
