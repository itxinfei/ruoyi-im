package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 机器人消息日志实体
 *
 * 用于记录机器人触发和回复的日志
 *
 * @author ruoyi
 */
@TableName("im_bot_message_log")
@Data
@EqualsAndHashCode(callSuper = true)

public class ImBotMessageLog extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID，主键 */
    @TableId(type = IdType.AUTO)
    
    private Long id;

    /** 机器人ID，关联到im_group_bot表 */
    
    @TableField("bot_id")
    private Long botId;

    /** 触发的规则ID，关联到im_group_bot_rule表 */
    
    @TableField("rule_id")
    private Long ruleId;

    /** 群组ID，关联到im_group表 */
    
    @TableField("group_id")
    private Long groupId;

    /** 触发用户ID，关联到im_user表 */
    
    @TableField("trigger_user_id")
    private Long triggerUserId;

    /** 触发的消息内容 */
    
    @TableField("trigger_message")
    private String triggerMessage;

    /** 机器人回复内容 */
    
    @TableField("reply_message")
    private String replyMessage;

    // createTime 和 updateTime 继承自 BaseEntity
}

