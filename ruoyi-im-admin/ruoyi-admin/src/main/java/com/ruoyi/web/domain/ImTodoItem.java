package com.ruoyi.web.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待办事项实体
 *
 * 用于存储IM系统中的待办事项信息，记录用户的待办任务和提醒
 * 包括待办标题、描述、类型、优先级、截止日期、完成状态等，用于管理用户的待办事项
 * 支持多种待办类型（审批、消息、任务）和优先级（高、普通、低）
 *
 * @author ruoyi
 */
@Schema(description = "待办事项")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImTodoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    /** 用户名（关联查询，管理后台专用） */
    private String username;

    /** 昵称（关联查询，管理后台专用） */
    private String nickname;

    @Schema(description = "待办标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "类型（APPROVAL审批/MESSAGE消息/TASK任务）")
    private String type;

    @Schema(description = "关联ID")
    private Long relatedId;

    @Schema(description = "关联类型")
    private String relatedType;

    @Schema(description = "优先级（1低 2中 3高）")
    private Integer priority;

    @Schema(description = "状态（PENDING待办/IN_PROGRESS进行中/COMPLETED已完成/CANCELLED已取消）")
    private String status;

    @Schema(description = "截止日期")
    private LocalDateTime dueDate;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
