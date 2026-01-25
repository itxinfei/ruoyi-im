package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 群机器人规则实体
 *
 * 用于存储群机器人的触发规则和回复内容
 *
 * @author ruoyi
 */
@TableName("im_group_bot_rule")
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "群机器人规则")
public class ImGroupBotRule extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 规则ID，主键 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "规则ID")
    private Long id;

    /** 机器人ID，关联到im_group_bot表 */
    @Schema(description = "机器人ID")
    @TableField("bot_id")
    private Long botId;

    /** 规则名称 */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 触发类型
     * KEYWORD：关键词触发
     * TIME：定时触发
     * COMMAND：命令触发
     */
    @Schema(description = "触发类型：KEYWORD关键词/TIME定时/COMMAND命令")
    @TableField("trigger_type")
    private String triggerType;

    /** 触发内容（关键词、Cron表达式、命令等） */
    @Schema(description = "触发内容")
    @TableField("trigger_content")
    private String triggerContent;

    /**
     * 回复类型
     * TEXT：文本
     * IMAGE：图片
     * CARD：卡片
     */
    @Schema(description = "回复类型：TEXT文本/IMAGE图片/CARD卡片")
    @TableField("reply_type")
    private String replyType;

    /** 回复内容 */
    @Schema(description = "回复内容")
    @TableField("reply_content")
    private String replyContent;

    /**
     * 优先级，数字越大优先级越高
     * 用于多个规则同时匹配时的排序
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 是否启用
     * 0：禁用
     * 1：启用
     */
    @Schema(description = "是否启用：0禁用/1启用")
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 匹配模式
     * EXACT：精确匹配
     * CONTAINS：包含匹配
     * REGEX：正则表达式匹配
     */
    @Schema(description = "匹配模式：EXACT精确/CONTAINS包含/REGEX正则")
    @TableField("match_mode")
    private String matchMode;
}
