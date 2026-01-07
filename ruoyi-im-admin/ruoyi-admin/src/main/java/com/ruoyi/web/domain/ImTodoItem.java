package com.ruoyi.web.domain;

import io.swagger.v3.oas.annotations.media.Schema;

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

public class ImTodoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

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

    @Schema(description = "优先级（HIGH高/NORMAL普通/LOW低）")
    private String priority;

    @Schema(description = "截止日期")
    private LocalDateTime dueDate;

    @Schema(description = "是否完成")
    private Boolean isCompleted;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public LocalDateTime getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

}
