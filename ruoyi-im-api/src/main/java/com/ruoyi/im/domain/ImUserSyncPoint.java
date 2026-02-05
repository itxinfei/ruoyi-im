package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户消息同步点实体
 *
 * 用于记录用户各设备的最后同步时间戳，实现消息同步功能。
 * 参考野火IM的同步机制设计。
 *
 * @author ruoyi
 */
@TableName("im_user_sync_point")
@Data
@EqualsAndHashCode(callSuper = false)
public class ImUserSyncPoint extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 设备ID（客户端生成，如：web_abc123）
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 最后同步时间戳（毫秒）
     * 表示该设备已经同步到哪个时间点的消息
     */
    @TableField("last_sync_time")
    private Long lastSyncTime;

    /**
     * 最后同步消息ID
     * 用于精确定位同步位置
     */
    @TableField("last_message_id")
    private Long lastMessageId;
}
