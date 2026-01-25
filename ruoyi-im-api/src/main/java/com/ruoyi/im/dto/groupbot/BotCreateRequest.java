package com.ruoyi.im.dto.groupbot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 创建群机器人请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "创建群机器人请求")
public class BotCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 群组ID */
    @Schema(description = "群组ID")
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    /** 机器人名称 */
    @Schema(description = "机器人名称")
    @NotBlank(message = "机器人名称不能为空")
    private String botName;

    /**
     * 机器人类型
     * SERVICE：客服机器人
     * NOTIFY：通知机器人
     * MANAGE：管理机器人
     */
    @Schema(description = "机器人类型：SERVICE客服/NOTIFY通知/MANAGE管理")
    private String botType = "SERVICE";

    /** 机器人头像 */
    @Schema(description = "机器人头像")
    private String avatar;

    /** 机器人描述 */
    @Schema(description = "机器人描述")
    private String description;

    /** 机器人规则列表 */
    @Schema(description = "机器人规则列表")
    private List<BotRuleRequest> rules;
}
