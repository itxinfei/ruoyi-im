package com.ruoyi.im.dto.approval;

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

public class ConditionOperand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段名
     * 支持格式：fieldName 或 {fieldName}
     * 支持嵌套：user.department.name
     */
    
    private String field;

    /**
     * 比较运算符
     * 支持：==、!=、>、<、>=、<=、contains、!contains、in、!in、regex、!regex
     */
    
    private String operator;

    /**
     * 比较值
     * 可以是字符串、数字、布尔值、数组
     */
    
    private Object value;
}

