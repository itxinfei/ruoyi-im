package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息确认实体
 *
 * 记录消息的送达、接收、已读确认状态
 *
 * @author ruoyi
 */
@TableName("im_message_ack")
@Data
@EqualsAndHashCode(callSuper = false)
public class ImMessageAck extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 确认类型：deliver=送达 server=服务器接收 receive=客户端接收 read=已读
     */
    public static final String ACK_TYPE_DELIVER = "deliver";
    public static final String ACK_TYPE_RECEIVE = "receive";
    public static final String ACK_TYPE_READ = "read";

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息ID
     */
    @TableField("message_id")
    private Long messageId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 会话ID
     */
    @TableField("conversation_id")
    private Long conversationId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 确认类型：deliver/receive/read
     */
    @TableField("ack_type")
    private String ackType;

    /**
     * 客户端消息ID（用于去重）
     */
    @TableField("client_msg_id")
    private String clientMsgId;

    /**
     * 确认时间戳（毫秒）
     */
    @TableField("ack_timestamp")
    private Long ackTimestamp;
}
