package com.ruoyi.im.dto.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 任务查询请求
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "任务查询请求")
public class ImTaskQueryRequest extends PageRequest {

    @Schema(description = "任务标题（模糊搜索）")
    private String keyword;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "负责人ID")
    private Long assigneeId;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "父任务ID（查询子任务）")
    private Long parentId;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "开始日期-开始")
    private LocalDate startDateBegin;

    @Schema(description = "开始日期-结束")
    private LocalDate startDateEnd;

    @Schema(description = "截止日期-开始")
    private LocalDate dueDateBegin;

    @Schema(description = "截止日期-结束")
    private LocalDate dueDateEnd;

    @Schema(description = "是否只查询逾期任务")
    private Boolean overdueOnly = false;

    @Schema(description = "是否只查询我创建的")
    private Boolean myCreatedOnly = false;

    @Schema(description = "是否只查询我负责的")
    private Boolean myAssignedOnly = false;

    @Schema(description = "是否只查询我关注的")
    private Boolean myFollowedOnly = false;

    @Schema(description = "排序字段（createTime, dueDate, priority, completionPercent）")
    private String orderBy = "createTime";

    @Schema(description = "排序方向（ASC DESC）")
    private String orderDirection = "DESC";
}
