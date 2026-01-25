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
import java.util.List;

/**
 * 群机器人实体
 *
 * 用于存储群组机器人的配置信息
 * 支持关键词自动回复、定时消息、命令执行等功能
 *
 * @author ruoyi
 */
@TableName("im_group_bot")
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "群机器人")
public class ImGroupBot extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 机器人ID，主键 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "机器人ID")
    private Long id;

    /** 群组ID，关联到im_group表 */
    @Schema(description = "群组ID")
    @TableField("group_id")
    private Long groupId;

    /** 机器人名称 */
    @Schema(description = "机器人名称")
    private String botName;

    /**
     * 机器人类型
     * SERVICE：客服机器人（关键词匹配自动回复）
     * NOTIFY：通知机器人（定时推送消息）
     * MANAGE：管理机器人（群管理操作）
     */
    @Schema(description = "机器人类型：SERVICE客服/NOTIFY通知/MANAGE管理")
    @TableField("bot_type")
    private String botType;

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
    @TableField("is_enabled")
    private Integer isEnabled;

    // ==================== 以下字段为非数据库字段 ====================

    /** 机器人规则列表（非数据库字段） */
    @TableField(exist = false)
    private List<ImGroupBotRule> rules;

    /** 规则数量（非数据库字段） */
    @TableField(exist = false)
    private Integer ruleCount;

    /** 群组名称（非数据库字段） */
    @TableField(exist = false)
    private String groupName;
}
