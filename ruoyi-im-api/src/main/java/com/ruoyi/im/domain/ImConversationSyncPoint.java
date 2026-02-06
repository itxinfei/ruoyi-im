package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话同步点实体
 *
 * 用于记录用户各设备的会话设置同步点
 *
 * @author ruoyi
 */
@TableName("im_conversation_sync_point")
@Data
@EqualsAndHashCode(callSuper = false)
public class ImConversationSyncPoint extends BaseEntity {

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
     * 设备ID
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 最后同步时间戳（毫秒）
     */
    @TableField("last_sync_time")
    private Long lastSyncTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private java.time.LocalDateTime updateTime;
}
