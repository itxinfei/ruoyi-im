package com.ruoyi.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 高级搜索请求DTO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImUserAdvancedSearchDTO {

    /**
     * 基础搜索条件
     */
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    private Integer status;
    private Integer gender;

    /**
     * 创建时间范围搜索
     */
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;

    /**
     * 最后登录时间范围搜索
     */
    private LocalDateTime lastLoginTimeStart;
    private LocalDateTime lastLoginTimeEnd;

    /**
     * 高级条件组合
     */
    private List<SearchCondition> conditions;

    /**
     * 部门条件
     */
    private List<Long> departmentIds;

    /**
     * 角色条件
     */
    private List<Long> roleIds;

    /**
     * 排序条件
     */
    private List<SortCondition> sortConditions;

    /**
     * 分页参数
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    /**
     * 搜索模式: simple-简单搜索, advanced-高级搜索, saved-保存的搜索
     */
    private String searchMode = "simple";

    /**
     * 保存的搜索ID（用于调用保存的搜索条件）
     */
    private String savedSearchId;

    /**
     * 是否包含已删除用户
     */
    private Boolean includeDeleted = false;

    /**
     * 是否只返回统计信息
     */
    private Boolean statisticsOnly = false;

    /**
     * 导出条件（用于导出时的特殊条件）
     */
    private Map<String, Object> exportConditions;

    /**
     * 搜索条件逻辑关系: AND-且, OR-或
     */
    private String conditionLogic = "AND";

    /**
     * 验证搜索参数有效性
     */
    public boolean isValid() {
        if (pageNum == null || pageNum < 1) return false;
        if (pageSize == null || pageSize < 1 || pageSize > 1000) return false;
        if (!"simple".equals(searchMode) && !"advanced".equals(searchMode) && !"saved".equals(searchMode)) return false;
        return true;
    }

    /**
     * 判断是否有时间范围搜索
     */
    public boolean hasTimeRangeSearch() {
        return (createTimeStart != null && createTimeEnd != null) ||
               (lastLoginTimeStart != null && lastLoginTimeEnd != null);
    }

    /**
     * 获取搜索条件指纹（用于缓存）
     */
    public String getSearchFingerprint() {
        return String.format("%s_%s_%s", 
            searchMode, 
            conditionLogic, 
            Math.abs(hashCode()));
    }

    /**
     * 搜索条件内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchCondition {
        /**
         * 字段名
         */
        private String field;

        /**
         * 操作符: eq-等于, ne-不等于, gt-大于, gte-大于等于, 
         *           lt-小于, lte-小于等于, like-模糊匹配, in-包含, notIn-不包含
         */
        private String operator;

        /**
         * 比较值（可以是单个值或数组）
         */
        private Object value;

        /**
         * 逻辑关系: AND-且, OR-或
         */
        private String logic = "AND";

        /**
         * 验证条件有效性
         */
        public boolean isValid() {
            if (field == null || field.trim().isEmpty()) return false;
            if (operator == null) return false;
            if (value == null) return false;
            
            String[] validOperators = {"eq", "ne", "gt", "gte", "lt", "lte", "like", "in", "notIn"};
            for (String validOp : validOperators) {
                if (validOp.equals(operator)) return true;
            }
            return false;
        }
    }

    /**
     * 排序条件内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortCondition {
        /**
         * 排序字段
         */
        private String field;

        /**
         * 排序方向: ASC-升序, DESC-降序
         */
        private String direction = "ASC";

        /**
         * 排序优先级（数字越小优先级越高）
         */
        private Integer priority = 0;

        /**
         * 验证排序条件有效性
         */
        public boolean isValid() {
            if (field == null || field.trim().isEmpty()) return false;
            if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) return false;
            return true;
        }
    }
}