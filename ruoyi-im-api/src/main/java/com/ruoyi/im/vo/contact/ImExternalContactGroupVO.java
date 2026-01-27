package com.ruoyi.im.vo.contact;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 外部联系人分组视图对象
 * 用于返回给前端的分组数据，与数据库实体 ImExternalContactGroup 分离
 *
 * @author ruoyi
 */
@Data
public class ImExternalContactGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分组ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 分组名称 */
    private String name;

    /** 排序 */
    private Integer sortOrder;

    /** 联系人数量（关联查询） */
    private Integer contactCount;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
