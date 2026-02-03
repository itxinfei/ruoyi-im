package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;

/**
 * 用户应用安装实体
 *
 * 记录用户安装的应用列表和配置信息
 *
 * @author ruoyi
 */
@TableName("im_user_application")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImUserApplication extends BaseEntity implements Serializable {

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
     * 应用ID
     */
    @TableField("app_id")
    private Long appId;

    /**
     * 是否固定到工作台：0=否, 1=是
     */
    @TableField("is_pinned")
    private Integer isPinned;

    /**
     * 排序位置
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 应用配置（JSON格式）
     * 用户级别的应用配置，如主题、语言、通知设置等
     */
    @TableField("app_config")
    private String appConfig;

    /**
     * 最后使用时间
     */
    @TableField("last_used_time")
    private java.time.LocalDateTime lastUsedTime;

    /**
     * 使用次数
     */
    @TableField("use_count")
    private Integer useCount;

    /**
     * 是否启用：0=禁用, 1=启用
     */
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 创建者
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 更新者
     */
    @TableField("update_by")
    private String updateBy;
}
