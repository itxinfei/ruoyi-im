package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用实体
 *
 * 用于存储IM系统中的应用信息，包括应用的基本属性、访问地址、权限配置等
 * 支持多种应用类型（路由、嵌入、外部链接）和应用分类（办公、数据、工具、自定义）
 * 用于管理IM系统中的各类应用，提供统一的应用入口和权限控制
 *
 * @author ruoyi
 */
@TableName("im_application")
@Data
public class ImApplication extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID，主键，唯一标识应用
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 应用名称，应用的显示名称，用于用户识别和选择
     */
    private String name;

    /**
     * 应用编码，应用的唯一编码，用于系统内部标识和引用
     */
    private String code;

    /**
     * 应用图标，应用的图标URL，用于在界面上展示
     */
    private String icon;

    /**
     * 分类（OFFICE办公 DATA数据 TOOLS工具 CUSTOM自定义）
     * 用于对应用进行分类管理，便于用户查找和使用
     */
    private String category;

    /**
     * 应用描述，应用的详细说明，包括功能介绍、使用说明等
     */
    private String description;

    /**
     * 类型（ROUTE路由 IFRAME嵌入 LINK外部链接）
     * ROUTE: 路由类型，应用是系统内的路由页面
     * IFRAME: 嵌入类型，应用通过iframe嵌入到系统中
     * LINK: 外部链接类型，应用是外部系统的链接
     */
    private String appType;

    /**
     * 应用地址，应用的访问地址，根据应用类型不同而不同
     * ROUTE类型为路由路径，IFRAME类型为嵌入URL，LINK类型为外部链接
     */
    private String appUrl;

    /**
     * 是否系统应用，标识该应用是否为系统预置应用
     * true: 系统应用，由系统预置，不可删除
     * false: 自定义应用，由用户创建，可编辑和删除
     */
    private Boolean isSystem;

    /**
     * 是否可见，标识该应用是否对用户可见
     * true: 应用可见，用户可以看到并使用该应用
     * false: 应用不可见，用户无法看到该应用
     */
    private Boolean isVisible;

    /**
     * 排序，应用的显示顺序，用于控制应用在列表中的排序
     */
    private Integer sortOrder;

    /**
     * 所需权限（JSON格式），应用所需的权限配置，以JSON字符串形式存储
     * 用于控制用户对应用的访问权限
     */
    private String permissions;
}
