package com.ruoyi.im.domain;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

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
@TableName("im_todo_item")
@Data
public class ImTodoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID，主键，唯一标识待办事项
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，待办事项所属的用户ID
     */
    private Long userId;

    /**
     * 待办标题，待办事项的简短标题
     */
    private String title;

    /**
     * 描述，待办事项的详细描述
     */
    private String description;

    /**
     * 类型，支持APPROVAL审批/MESSAGE消息/TASK任务
     */
    private String type;

    /**
     * 关联ID，待办事项关联的业务ID
     */
    private Long relatedId;

    /**
     * 优先级，支持HIGH高/NORMAL普通/LOW低
     */
    private String priority;

    /**
     * 截止日期，待办事项的截止时间
     */
    private LocalDateTime dueDate;

    /**
     * 是否完成，标识待办事项是否已完成
     */
    private Boolean isCompleted;

    /**
     * 完成时间，待办事项完成的时间
     */
    private LocalDateTime completedTime;

    /**
     * 创建时间，待办事项的创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间，待办事项的更新时间
     */
    private LocalDateTime updateTime;
}
