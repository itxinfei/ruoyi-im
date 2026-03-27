package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;

/**
 * 用户布局配置实体
 * 保存用户的工作台布局自定义设置
 *
 * @author ruoyi
 */
@TableName("im_user_layout")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImUserLayout extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID，主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 布局类型（如 WORKBENCH, SIDEBAR 等） */
    @TableField("layout_type")
    private String layoutType;

    /** 布局配置JSON */
    @TableField("layout_config")
    private String layoutConfig;
}
