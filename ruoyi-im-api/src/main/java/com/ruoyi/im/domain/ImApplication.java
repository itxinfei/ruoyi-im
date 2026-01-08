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
 * 应用实体
 *
 * @author ruoyi
 */
@TableName("im_application")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImApplication extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 应用ID，主键，唯一标识应用 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应用名称，应用的显示名称，用于用户识别和选择 */
    private String name;

    /** 应用编码，应用的唯一编码，用于系统内部标识和引用 */
    private String code;

    /** 应用图标，应用的图标URL，用于在界面上展示 */
    private String icon;

    /** 分类（OFFICE办公 DATA数据 TOOLS工具 CUSTOM自定义） */
    private String category;

    /** 应用描述，应用的详细说明，包括功能介绍、使用说明等 */
    private String description;

    /** 类型（ROUTE路由 IFRAME嵌入 LINK外部链接） */
    @TableField("app_type")
    private String appType;

    /** 应用地址，应用的访问地址 */
    @TableField("app_url")
    private String appUrl;

    /** 是否系统应用：0=否, 1=是 */
    @TableField("is_system")
    private Integer isSystem;

    /** 是否可见：0=否, 1=是 */
    @TableField("is_visible")
    private Integer isVisible;

    /** 排序，应用的显示顺序，用于控制应用在列表中的排序 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 所需权限（JSON格式），应用所需的权限配置，以JSON字符串形式存储 */
    private String permissions;

    /** 创建者 */
    @TableField("create_by")
    private String createBy;

    /** 更新者 */
    @TableField("update_by")
    private String updateBy;
}
