package com.ruoyi.im.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.ruoyi.im.common.BaseEntity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待办事项实体
 *
 * @author ruoyi
 */
@TableName("im_todo_item")
@Data
@EqualsAndHashCode(callSuper = true)
public class ImTodoItem extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 待办标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 优先级：1=低, 2=中, 3=高 */
    private Integer priority;

    /** 状态（PENDING待处理 IN_PROGRESS进行中 COMPLETED已完成 CANCELLED已取消） */
    private String status;

    /** 截止日期 */
    @TableField("due_date")
    private LocalDate dueDate;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("completed_time")
    private LocalDateTime completedTime;
}
