package com.ruoyi.im.dto.groupbot;

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

public class BotCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 群组ID */
    
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    /** 机器人名称 */
    
    @NotBlank(message = "机器人名称不能为空")
    private String botName;

    /**
     * 机器人类型
     * SERVICE：客服机器人
     * NOTIFY：通知机器人
     * MANAGE：管理机器人
     */
    
    private String botType = "SERVICE";

    /** 机器人头像 */
    
    private String avatar;

    /** 机器人描述 */
    
    private String description;

    /** 机器人规则列表 */
    
    private List<BotRuleRequest> rules;
}

