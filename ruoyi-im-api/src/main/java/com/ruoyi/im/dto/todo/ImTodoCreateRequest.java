package com.ruoyi.im.dto.todo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 待办创建请求DTO
 *
 * @author ruoyi
 */
@Data
public class ImTodoCreateRequest {

    /**
     * 待办标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 待办描述
     */
    private String description;

    /**
     * 待办类型：TASK-任务, APPROVAL-审批, DING-DING消息
     */
    private String type;

    /**
     * 关联ID
     */
    private Long relatedId;

    /**
     * 优先级：1=低, 2=中, 3=高
     */
    private Integer priority;
}