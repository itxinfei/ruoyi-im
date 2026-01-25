package com.ruoyi.im.dto.approval;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 条件分支DTO
 *
 * 用于定义审批流程中的条件分支
 * 根据表单数据动态确定审批路径
 *
 * @author ruoyi
 */
@Data
@Schema(description = "条件分支")
public class ConditionBranch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 条件表达式
     * 示例：{amount} > 5000 && {department} == '财务部'
     */
    @Schema(description = "条件表达式")
    private String expression;

    /**
     * 逻辑运算符：AND、OR
     */
    @Schema(description = "逻辑运算符")
    private String logicOperator = "AND";

    /**
     * 操作数列表
     * 外层列表表示OR关系的条件组
     * 内层列表表示AND关系的条件
     */
    @Schema(description = "操作数列表")
    private List<List<ConditionOperand>> operands;

    /**
     * 满足条件后的目标节点ID
     */
    @Schema(description = "目标节点ID")
    private String targetNodeId;

    /**
     * 满足条件后的流程描述
     */
    @Schema(description = "流程描述")
    private String description;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean enabled = true;
}
