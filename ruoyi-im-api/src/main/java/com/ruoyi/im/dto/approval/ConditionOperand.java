package com.ruoyi.im.dto.approval;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 条件操作数DTO
 *
 * 用于定义条件表达式中的单个操作数
 *
 * @author ruoyi
 */
@Data
@Schema(description = "条件操作数")
public class ConditionOperand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段名
     * 支持格式：fieldName 或 {fieldName}
     * 支持嵌套：user.department.name
     */
    @Schema(description = "字段名")
    private String field;

    /**
     * 比较运算符
     * 支持：==、!=、>、<、>=、<=、contains、!contains、in、!in、regex、!regex
     */
    @Schema(description = "比较运算符")
    private String operator;

    /**
     * 比较值
     * 可以是字符串、数字、布尔值、数组
     */
    @Schema(description = "比较值")
    private Object value;
}
