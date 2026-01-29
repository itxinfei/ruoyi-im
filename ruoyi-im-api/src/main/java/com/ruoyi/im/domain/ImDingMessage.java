package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DING强提醒消息实体
 *
 * 用于存储DING强提醒消息，支持应用内强提醒、短信提醒、电话提醒
 * 确保重要消息能够及时触达用户
 *
 * @author ruoyi
 */
@TableName("im_ding_message")
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "DING强提醒消息")
public class ImDingMessage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** DING消息ID，主键 */
    @TableId(type = IdType.AUTO)
    @Schema(description = "DING ID")
    private Long id;

    /** 会话ID，关联到im_conversation表 */
    @Schema(description = "会话ID")
    @TableField("conversation_id")
    private Long conversationId;

    /** 发送者用户ID，关联到im_user表 */
    @Schema(description = "发送者ID")
    @TableField("sender_id")
    private Long senderId;

    /**
     * DING类型
     * APP：应用内强提醒
     * SMS：短信提醒（需要第三方服务）
     * CALL：电话提醒（需要第三方服务）
     */
    @Schema(description = "DING类型：APP应用内/SMS短信/CALL电话")
    @TableField("ding_type")
    private String dingType;

    /**
     * 是否紧急
     * 0：普通
     * 1：紧急
     */
    @Schema(description = "是否紧急：0否/1是")
    @TableField("is_urgent")
    private Integer isUrgent;

    /** DING内容 */
    @Schema(description = "DING内容")
    private String content;

    /**
     * 目标用户ID列表（JSON格式）
     * 为空表示发送给会话中的所有成员
     */
    @Schema(description = "目标用户ID列表（JSON格式）")
    @TableField("target_users")
    private String targetUsers;

    /** 已读人数 */
    @Schema(description = "已读人数")
    @TableField("read_count")
    private Integer readCount;

    /** 发送人数 */
    @Schema(description = "发送人数")
    @TableField("send_count")
    private Integer sendCount;

    /** 过期时间 */
    @Schema(description = "过期时间")
    @TableField("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 状态
     * ACTIVE：激活
     * EXPIRED：过期
     * CANCELLED：已取消
     */
    @Schema(description = "状态：ACTIVE激活/EXPIRED过期/CANCELLED已取消")
    @TableField("status")
    private String status;

    // ==================== 以下字段为非数据库字段 ====================

    /** 发送者信息（非数据库字段） */
    @TableField(exist = false)
    private ImUser sender;

    /** 发送者名称（非数据库字段） */
    @TableField(exist = false)
    private String senderName;

    /** 发送者头像（非数据库字段） */
    @TableField(exist = false)
    private String senderAvatar;

    /**
     * 目标用户ID列表（非数据库字段，解析后的列表）
     */
    @TableField(exist = false)
    private List<Long> targetUserList;

    /**
     * 是否已读（非数据库字段，当前用户是否已读）
     */
    @TableField(exist = false)
    private Boolean isRead;

    /**
     * 已读用户列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<ImDingRead> readRecords;

    /** 接收者ID数组（用于创建DING时） */
    @TableField(exist = false)
    private Long[] receiverIds;
}
