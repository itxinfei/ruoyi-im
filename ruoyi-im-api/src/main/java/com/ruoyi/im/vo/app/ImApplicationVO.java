package com.ruoyi.im.vo.app;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用视图对象
 * 用于返回给前端的应用数据，与数据库实体 ImApplication 分离
 *
 * @author ruoyi
 */
@Data
public class ImApplicationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 应用ID */
    private Long id;

    /** 应用名称 */
    private String name;

    /** 应用编码 */
    private String code;

    /** 应用图标 */
    private String icon;

    /** 分类（OFFICE办公 DATA数据 TOOLS工具 CUSTOM自定义） */
    private String category;

    /** 应用描述 */
    private String description;

    /** 类型（ROUTE路由 IFRAME嵌入 LINK外部链接） */
    private String appType;

    /** 应用地址 */
    private String appUrl;

    /** 是否系统应用：0=否, 1=是 */
    private Integer isSystem;

    /** 是否可见：0=否, 1=是 */
    private Integer isVisible;

    /** 排序 */
    private Integer sortOrder;

    /** 所需权限（JSON格式） */
    private String permissions;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
