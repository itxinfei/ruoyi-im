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

    /** 发送者用户ID，关联到im_user表 */
    @Schema(description = "发送者ID")
    @TableField("sender_id")
    private Long senderId;

    /** DING内容 */
    @Schema(description = "DING内容")
    private String content;

    /**
     * DING类型
     * APP：应用内强提醒
     * SMS：短信提醒（需要第三方服务）
     * SYSTEM：系统通知
     */
    @Schema(description = "DING类型：APP应用内/SMS短信/SYSTEM系统通知")
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

    /** 定时发送时间 */
    @Schema(description = "定时发送时间")
    @TableField("schedule_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduleTime;

    /** 实际发送时间 */
    @Schema(description = "实际发送时间")
    @TableField("send_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     * 状态
     * DRAFT：草稿
     * SENDING：发送中
     * SENT：已发送
     * CANCELLED：已取消
     * FAILED：失败
     */
    @Schema(description = "状态：DRAFT草稿/SENDING发送中/SENT已发送/CANCELLED已取消/FAILED失败")
    @TableField("status")
    private String status;

    /** 是否需要回执 */
    @Schema(description = "是否需要回执：0否/1是")
    @TableField("receipt_required")
    private Integer receiptRequired;

    /** 总接收人数 */
    @Schema(description = "总接收人数")
    @TableField("total_count")
    private Integer totalCount;

    /** 已读人数 */
    @Schema(description = "已读人数")
    @TableField("read_count")
    private Integer readCount;

    /** 已确认人数 */
    @Schema(description = "已确认人数")
    @TableField("confirmed_count")
    private Integer confirmedCount;

    /** 附件URL */
    @Schema(description = "附件URL")
    @TableField("attachment")
    private String attachment;

    // ==================== 以下字段为非数据库字段 ====================

    /** 会话ID（非数据库字段，用于业务逻辑） */
    @TableField(exist = false)
    private Long conversationId;

    /** 目标用户ID列表（非数据库字段，JSON格式） */
    @TableField(exist = false)
    private String targetUsers;

    /** 发送人数（非数据库字段，业务计算用） */
    @TableField(exist = false)
    private Integer sendCount;

    /** 过期时间（非数据库字段，业务计算用） */
    @TableField(exist = false)
    private LocalDateTime expireTime;

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
