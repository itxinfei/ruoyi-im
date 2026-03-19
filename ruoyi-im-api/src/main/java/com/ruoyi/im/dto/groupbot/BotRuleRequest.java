package com.ruoyi.im.dto.groupbot;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 机器人规则请求DTO
 *
 * @author ruoyi
 */
@Data

public class BotRuleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 规则名称 */
    
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 触发类型
     * KEYWORD：关键词触发
     * TIME：定时触发
     * COMMAND：命令触发
     */
    
    @NotBlank(message = "触发类型不能为空")
    private String triggerType;

    /** 触发内容（关键词、Cron表达式、命令等） */
    
    @NotBlank(message = "触发内容不能为空")
    private String triggerContent;

    /**
     * 回复类型
     * TEXT：文本
     * IMAGE：图片
     * CARD：卡片
     */
    
    @NotBlank(message = "回复类型不能为空")
    private String replyType;

    /** 回复内容 */
    
    @NotBlank(message = "回复内容不能为空")
    private String replyContent;

    /** 优先级（数字越大优先级越高） */
    
    private Integer priority = 0;

    /**
     * 匹配模式
     * EXACT：精确匹配
     * CONTAINS：包含匹配
     * REGEX：正则表达式匹配
     */
    
    private String matchMode = "CONTAINS";

    /** 是否启用（0禁用 1启用） */
    
    private Integer isEnabled;
}

