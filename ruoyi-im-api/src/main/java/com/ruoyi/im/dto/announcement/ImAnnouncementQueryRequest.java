package com.ruoyi.im.dto.announcement;

import com.ruoyi.im.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告查询请求
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公告查询请求")
public class ImAnnouncementQueryRequest extends PageRequest {

    @Schema(description = "关键词（标题或内容模糊搜索）")
    private String keyword;

    @Schema(description = "公告类型（SYSTEM系统 DEPARTMENT部门 PROJECT项目）")
    private String announcementType;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "状态（DRAFT草稿 PUBLISHED已发布 EXPIRED已过期）")
    private String status;

    @Schema(description = "发布人ID")
    private Long publisherId;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "是否只查询置顶")
    private Boolean pinnedOnly = false;

    @Schema(description = "是否只查询未过期")
    private Boolean activeOnly = true;

    @Schema(description = "是否只查询我发布的")
    private Boolean myPublishedOnly = false;

    @Schema(description = "排序字段（publishTime, priority, viewCount）")
    private String orderBy = "publishTime";

    @Schema(description = "排序方向（ASC DESC）")
    private String orderDirection = "DESC";
}
