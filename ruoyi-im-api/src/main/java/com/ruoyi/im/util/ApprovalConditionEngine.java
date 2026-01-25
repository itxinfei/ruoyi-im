package com.ruoyi.im.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ruoyi.im.dto.approval.ConditionBranch;
import com.ruoyi.im.dto.approval.ConditionOperand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 审批条件表达式引擎
 *
 * 用于解析和执行审批流程中的条件表达式
 * 支持的比较运算：==、!=、>、<、>=、<=、contains、in、regex
 * 支持的逻辑运算：AND、OR、NOT
 * 支持的字段引用：{fieldName}
 *
 * @author ruoyi
 */
@Component
public class ApprovalConditionEngine {

    private static final Logger log = LoggerFactory.getLogger(ApprovalConditionEngine.class);

    // 比较运算符枚举
    private enum CompareOperator {
        EQUAL("=="),
        NOT_EQUAL("!="),
        GREATER(">"),
        LESS("<"),
        GREATER_EQUAL(">="),
        LESS_EQUAL("<="),
        CONTAINS("contains"),
        NOT_CONTAINS("!contains"),
        IN("in"),
        NOT_IN("!in"),
        REGEX("regex"),
        NOT_REGEX("!regex");

        private final String symbol;

        CompareOperator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static CompareOperator fromSymbol(String symbol) {
            for (CompareOperator op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("未知的比较运算符: " + symbol);
        }
    }

    // 逻辑运算符枚举
    private enum LogicalOperator {
        AND("&&", "and"),
        OR("||", "or"),
        NOT("!", "not");

        private final String symbol1;
        private final String symbol2;

        LogicalOperator(String symbol1, String symbol2) {
            this.symbol1 = symbol1;
            this.symbol2 = symbol2;
        }

        public static LogicalOperator fromSymbol(String symbol) {
            for (LogicalOperator op : values()) {
                if (op.symbol1.equals(symbol) || op.symbol2.equals(symbol)) {
                    return op;
                }
            }
            return null;
        }
    }

    /**
     * 评估条件表达式
     *
     * @param expression 条件表达式
     * @param formData  表单数据
     * @return 评估结果
     */
    public boolean evaluate(String expression, Map<String, Object> formData) {
        if (StrUtil.isBlank(expression)) {
            return true;
        }

        try {
            // 解析表达式为条件分支
            ConditionBranch branch = parseExpression(expression);
            return evaluateCondition(branch, formData);
        } catch (Exception e) {
            log.error("条件表达式评估失败: expression={}", expression, e);
            return false;
        }
    }

    /**
     * 评估条件分支
     *
     * @param branch    条件分支
     * @param formData 表单数据
     * @return 评估结果
     */
    public boolean evaluateCondition(ConditionBranch branch, Map<String, Object> formData) {
        if (branch == null || StrUtil.isBlank(branch.getExpression())) {
            return true;
        }

        List<List<ConditionOperand>> operands = branch.getOperands();
        String logicOperator = branch.getLogicOperator();

        if (operands == null || operands.isEmpty()) {
            return true;
        }

        boolean result = true;

        for (int i = 0; i < operands.size(); i++) {
            List<ConditionOperand> group = operands.get(i);
            boolean groupResult = evaluateOperandGroup(group, formData);

            if ("OR".equalsIgnoreCase(logicOperator) || "||".equals(logicOperator)) {
                result = result || groupResult;
                if (result) {
                    break; // OR运算，只要有一个为true就可以提前退出
                }
            } else {
                // AND运算（默认）
                if (i > 0) {
                    result = result && groupResult;
                } else {
                    result = groupResult;
                }
                if (!result) {
                    break; // AND运算，只要有一个为false就可以提前退出
                }
            }
        }

        return result;
    }

    /**
     * 评估操作数组（组内AND关系）
     *
     * @param operands 操作数列表
     * @param formData  表单数据
     * @return 评估结果
     */
    private boolean evaluateOperandGroup(List<ConditionOperand> operands, Map<String, Object> formData) {
        if (operands == null || operands.isEmpty()) {
            return true;
        }

        boolean result = true;
        for (ConditionOperand operand : operands) {
            boolean operandResult = evaluateOperand(operand, formData);
            result = result && operandResult;
            if (!result) {
                break;
            }
        }
        return result;
    }

    /**
     * 评估单个操作数
     *
     * @param operand  操作数
     * @param formData 表单数据
     * @return 评估结果
     */
    private boolean evaluateOperand(ConditionOperand operand, Map<String, Object> formData) {
        String fieldName = operand.getField();
        Object fieldValue = getFieldValue(formData, fieldName);

        if (fieldValue == null && !"null".equals(String.valueOf(operand.getValue()))) {
            // 字段不存在且值不是null比较时，返回false
            return false;
        }

        CompareOperator operator = CompareOperator.fromSymbol(operand.getOperator());
        Object expectedValue = operand.getValue();

        switch (operator) {
            case EQUAL:
                return Objects.equals(String.valueOf(fieldValue), String.valueOf(expectedValue));
            case NOT_EQUAL:
                return !Objects.equals(String.valueOf(fieldValue), String.valueOf(expectedValue));
            case GREATER:
                return compareNumbers(fieldValue, expectedValue) > 0;
            case LESS:
                return compareNumbers(fieldValue, expectedValue) < 0;
            case GREATER_EQUAL:
                return compareNumbers(fieldValue, expectedValue) >= 0;
            case LESS_EQUAL:
                return compareNumbers(fieldValue, expectedValue) <= 0;
            case CONTAINS:
                return String.valueOf(fieldValue).contains(String.valueOf(expectedValue));
            case NOT_CONTAINS:
                return !String.valueOf(fieldValue).contains(String.valueOf(expectedValue));
            case IN:
                return evaluateIn(fieldValue, expectedValue);
            case NOT_IN:
                return !evaluateIn(fieldValue, expectedValue);
            case REGEX:
                return evaluateRegex(fieldValue, expectedValue);
            case NOT_REGEX:
                return !evaluateRegex(fieldValue, expectedValue);
            default:
                return false;
        }
    }

    /**
     * 比较两个数值
     *
     * @param value1    值1
     * @param value2    值2
     * @return 比较结果
     */
    private int compareNumbers(Object value1, Object value2) {
        try {
            BigDecimal num1 = new BigDecimal(String.valueOf(value1));
            BigDecimal num2 = new BigDecimal(String.valueOf(value2));
            return num1.compareTo(num2);
        } catch (NumberFormatException e) {
            log.warn("数值比较失败，将按字符串比较: value1={}, value2={}", value1, value2);
            return String.valueOf(value1).compareTo(String.valueOf(value2));
        }
    }

    /**
     * 评估IN操作
     *
     * @param value     值
     * @param expected 期望值（可以是数组或逗号分隔的字符串）
     * @return 评估结果
     */
    private boolean evaluateIn(Object value, Object expected) {
        String valueStr = String.valueOf(value);

        // 尝试解析为数组
        if (expected instanceof List) {
            List<?> list = (List<?>) expected;
            for (Object item : list) {
                if (Objects.equals(valueStr, String.valueOf(item))) {
                    return true;
                }
            }
            return false;
        }

        // 按逗号分隔
        String expectedStr = String.valueOf(expected);
        String[] parts = expectedStr.split(",");
        for (String part : parts) {
            if (valueStr.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 评估正则表达式
     *
     * @param value     值
     * @param pattern   正则表达式
     * @return 匹配结果
     */
    private boolean evaluateRegex(Object value, Object pattern) {
        try {
            Pattern regex = Pattern.compile(String.valueOf(pattern));
            Matcher matcher = regex.matcher(String.valueOf(value));
            return matcher.find();
        } catch (Exception e) {
            log.warn("正则表达式匹配失败: pattern={}", pattern, e);
            return false;
        }
    }

    /**
     * 获取字段值
     * 支持嵌套字段，如：user.department.name
     *
     * @param formData  表单数据
     * @param fieldName 字段名
     * @return 字段值
     */
    private Object getFieldValue(Map<String, Object> formData, String fieldName) {
        if (formData == null || StrUtil.isBlank(fieldName)) {
            return null;
        }

        // 处理{fieldName}格式
        String cleanFieldName = fieldName.trim();
        if (cleanFieldName.startsWith("{") && cleanFieldName.endsWith("}")) {
            cleanFieldName = cleanFieldName.substring(1, cleanFieldName.length() - 1);
        }

        // 尝试直接获取
        if (formData.containsKey(cleanFieldName)) {
            return formData.get(cleanFieldName);
        }

        // 支持嵌套字段（如 user.department.name）
        String[] parts = cleanFieldName.split("\\.");
        Object current = formData;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * 解析条件表达式
     * 支持格式：
     * 1. 简单表达式：{amount} > 5000
     * 2. AND表达式：{amount} > 5000 && {department} == '财务部'
     * 3. OR表达式：{type} == 'urgent' || {priority} == 'high'
     * 4. 混合表达式：({amount} > 5000 && {department} == '财务部') || {type} == 'urgent'
     *
     * @param expression 条件表达式
     * @return 条件分支对象
     */
    public ConditionBranch parseExpression(String expression) {
        ConditionBranch branch = new ConditionBranch();
        branch.setExpression(expression.trim());

        // 确定主要逻辑运算符（最外层的AND或OR）
        String mainLogicOperator = determineMainLogicOperator(expression);
        branch.setLogicOperator(mainLogicOperator);

        // 解析操作数
        List<List<ConditionOperand>> operands = parseOperands(expression, mainLogicOperator);
        branch.setOperands(operands);

        return branch;
    }

    /**
     * 确定主要逻辑运算符
     *
     * @param expression 表达式
     * @return 逻辑运算符（AND或OR）
     */
    private String determineMainLogicOperator(String expression) {
        // 简化处理：查找最外层的&&或||
        int parenDepth = 0;
        boolean hasAnd = false;
        boolean hasOr = false;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth--;
            } else if (parenDepth == 0) {
                if (expression.startsWith("&&", i) || expression.startsWith("and", i)) {
                    hasAnd = true;
                } else if (expression.startsWith("||", i) || expression.startsWith("or", i)) {
                    hasOr = true;
                }
            }
        }

        // OR优先级高于AND
        if (hasOr) {
            return "OR";
        }
        if (hasAnd) {
            return "AND";
        }
        return "AND"; // 默认AND
    }

    /**
     * 解析操作数
     *
     * @param expression      表达式
     * @param logicOperator   逻辑运算符
     * @return 操作数列表
     */
    private List<List<ConditionOperand>> parseOperands(String expression, String logicOperator) {
        List<List<ConditionOperand>> result = new ArrayList<>();

        // 根据逻辑运算符分割表达式
        List<String> groups = splitByLogicOperator(expression, logicOperator);

        for (String group : groups) {
            List<ConditionOperand> operands = parseGroup(group.trim());
            if (!operands.isEmpty()) {
                result.add(operands);
            }
        }

        return result;
    }

    /**
     * 根据逻辑运算符分割表达式
     *
     * @param expression     表达式
     * @param logicOperator  逻辑运算符
     * @return 分割后的表达式组
     */
    private List<String> splitByLogicOperator(String expression, String logicOperator) {
        List<String> result = new ArrayList<>();

        if ("OR".equals(logicOperator)) {
            result = splitByOperator(expression, "\\|\\||\\sor\\s");
        } else {
            // 默认AND
            result = splitByOperator(expression, "&&|\\sand\\s");
        }

        return result;
    }

    /**
     * 按运算符分割表达式
     *
     * @param expression 表达式
     * @param regex     运算符正则
     * @return 分割后的表达式列表
     */
    private List<String> splitByOperator(String expression, String regex) {
        List<String> result = new ArrayList<>();
        int parenDepth = 0;
        int start = 0;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                parenDepth++;
            } else if (c == ')') {
                parenDepth--;
            } else if (parenDepth == 0) {
                // 检查是否匹配运算符
                String remaining = expression.substring(i);
                if (remaining.matches("^(" + regex + ").*")) {
                    String part = expression.substring(start, i).trim();
                    if (!part.isEmpty()) {
                        result.add(part);
                    }
                    // 跳过运算符
                    String operatorMatch = remaining.replaceAll("^(" + regex + ")\\s*", "");
                    i += operatorMatch.length();
                    start = i + 1;
                }
            }
        }

        // 添加最后一部分
        String lastPart = expression.substring(start).trim();
        if (!lastPart.isEmpty()) {
            result.add(lastPart);
        }

        return result;
    }

    /**
     * 解析单个表达式组
     *
     * @param group 表达式组
     * @return 操作数列表
     */
    private List<ConditionOperand> parseGroup(String group) {
        List<ConditionOperand> result = new ArrayList<>();

        // 去除外层括号
        String trimmed = group.trim();
        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        // 简化处理：假设组内都是AND关系
        // 查找比较运算符
        String[] operators = {"==", "!=", ">=", "<=", ">", "<", "contains", "!contains", "in", "!in", "regex", "!regex"};

        // 创建正则模式来匹配比较运算符
        StringBuilder operatorPattern = new StringBuilder("(");
        for (String op : operators) {
            if (operatorPattern.length() > 1) {
                operatorPattern.append("|");
            }
            operatorPattern.append(Pattern.quote(op));
        }
        operatorPattern.append(")");

        Pattern pattern = Pattern.compile(operatorPattern.toString());
        Matcher matcher = pattern.matcher(trimmed);

        if (matcher.find()) {
            String leftPart = trimmed.substring(0, matcher.start()).trim();
            String operator = matcher.group();
            String rightPart = trimmed.substring(matcher.end()).trim();

            ConditionOperand operand = new ConditionOperand();
            operand.setField(extractFieldName(leftPart));
            operand.setOperator(operator);
            operand.setValue(parseValue(rightPart));

            result.add(operand);
        }

        return result;
    }

    /**
     * 从表达式中提取字段名
     *
     * @param expression 表达式片段
     * @return 字段名
     */
    private String extractFieldName(String expression) {
        String trimmed = expression.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    /**
     * 解析值
     *
     * @param valueStr 值字符串
     * @return 解析后的值
     */
    private Object parseValue(String valueStr) {
        String trimmed = valueStr.trim();

        // 去除引号
        if ((trimmed.startsWith("'") && trimmed.endsWith("'")) ||
            (trimmed.startsWith("\"") && trimmed.endsWith("\""))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        // 数字
        if (trimmed.matches("-?\\d+\\.?\\d*")) {
            try {
                if (trimmed.contains(".")) {
                    return Double.parseDouble(trimmed);
                } else {
                    return Long.parseLong(trimmed);
                }
            } catch (NumberFormatException e) {
                // 忽略，返回字符串
            }
        }

        // 布尔值
        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }

        // null
        if ("null".equalsIgnoreCase(trimmed)) {
            return null;
        }

        return trimmed;
    }

    /**
     * 从JSON解析条件分支
     *
     * @param json JSON字符串
     * @return 条件分支对象
     */
    public ConditionBranch parseFromJson(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, ConditionBranch.class);
        } catch (Exception e) {
            log.error("解析条件分支JSON失败: json={}", json, e);
            return null;
        }
    }

    /**
     * 将条件分支转换为JSON
     *
     * @param branch 条件分支对象
     * @return JSON字符串
     */
    public String toJson(ConditionBranch branch) {
        if (branch == null) {
            return null;
        }
        return JSONUtil.toJsonStr(branch);
    }
}
