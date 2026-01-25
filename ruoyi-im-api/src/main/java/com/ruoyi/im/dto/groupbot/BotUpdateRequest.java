package com.ruoyi.im.dto.groupbot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新群机器人请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "更新群机器人请求")
public class BotUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 机器人ID */
    @Schema(description = "机器人ID")
    @NotNull(message = "机器人ID不能为空")
    private Long botId;

    /** 机器人名称 */
    @Schema(description = "机器人名称")
    private String botName;

    /** 机器人头像 */
    @Schema(description = "机器人头像")
    private String avatar;

    /** 机器人描述 */
    @Schema(description = "机器人描述")
    private String description;

    /**
     * 是否启用
     * 0：禁用
     * 1：启用
     */
    @Schema(description = "是否启用：0禁用/1启用")
    private Integer isEnabled;
}
