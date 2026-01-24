package com.ruoyi.web.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 高级搜索结果VO
 * 
 * @author ruoyi
 * @date 2025-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImUserAdvancedSearchResultVO {

    /**
     * 搜索结果列表
     */
    private List<ImUserSearchResultItemVO> results;

    /**
     * 搜索统计信息
     */
    private SearchStatistics statistics;

    /**
     * 分页信息
     */
    private PaginationInfo pagination;

    /**
     * 搜索耗时（毫秒）
     */
    private Long searchTime;

    /**
     * 搜索建议（用于拼写纠正或相关搜索）
     */
    private List<SearchSuggestion> suggestions;

    /**
     * 搜索查询摘要（SQL查询摘要，用于调试）
     */
    private String querySummary;

    /**
     * 搜索结果标记（是否精确匹配、模糊匹配等）
     */
    private String searchAccuracy;

    /**
     * 搜索统计内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchStatistics {
        /**
         * 总记录数
         */
        private Long totalCount;

        /**
         * 当前页记录数
         */
        private Integer currentPageCount;

        /**
         * 匹配类型分布
         */
        private Map<String, Integer> matchTypeDistribution;

        /**
         * 字段匹配分布
         */
        private Map<String, Integer> fieldDistribution;

        /**
         * 时间分布（按创建时间）
         */
        private Map<String, Integer> timeDistribution;

        /**
         * 状态分布
         */
        private Map<String, Integer> statusDistribution;

        /**
         * 获取搜索准确率
         */
        public double getAccuracyRate() {
            if (totalCount == null || totalCount == 0) return 0.0;
            if (matchTypeDistribution == null) return 0.0;
            
            Integer exactMatch = matchTypeDistribution.getOrDefault("exact", 0);
            return (double) exactMatch / totalCount * 100;
        }
    }

    /**
     * 分页信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        /**
         * 当前页码
         */
        private Integer pageNum;

        /**
         * 每页大小
         */
        private Integer pageSize;

        /**
         * 总页数
         */
        private Integer totalPages;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 是否有下一页
         */
        private Boolean hasNext;

        /**
         * 是否有上一页
         */
        private Boolean hasPrevious;

        /**
         * 起始记录索引
         */
        private Long startIndex;

        /**
         * 结束记录索引
         */
        private Long endIndex;
    }

    /**
     * 搜索建议内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchSuggestion {
        /**
         * 建议文本
         */
        private String text;

        /**
         * 建议类型: spelling-拼写纠正, related-相关搜索, completion-自动补全
         */
        private String type;

        /**
         * 建议权重（越高越相关）
         */
        private Double weight;

        /**
         * 建议字段（如果针对特定字段）
         */
        private String field;
    }

    /**
     * 搜索结果项内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImUserSearchResultItemVO {
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 头像
         */
        private String avatar;

        /**
         * 状态
         */
        private Integer status;

        /**
         * 性别
         */
        private Integer gender;

        /**
         * 部门
         */
        private String department;

        /**
         * 角色
         */
        private String roles;

        /**
         * 最后登录时间
         */
        private String lastLoginTime;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 匹配高亮信息
         */
        private Map<String, String> highlightInfo;

        /**
         * 匹配得分（相关度评分）
         */
        private Double matchScore;

        /**
         * 匹配类型: exact-精确匹配, partial-部分匹配, fuzzy-模糊匹配
         */
        private String matchType;

        /**
         * 获取显示名称（优先显示昵称，其次用户名）
         */
        public String getDisplayName() {
            if (nickname != null && !nickname.trim().isEmpty()) {
                return nickname;
            }
            return username != null ? username : "";
        }

        /**
         * 获取状态文本
         */
        public String getStatusText() {
            if (status == null) return "未知";
            switch (status) {
                case 0: return "禁用";
                case 1: return "正常";
                default: return "未知";
            }
        }

        /**
         * 获取性别文本
         */
        public String getGenderText() {
            if (gender == null) return "未知";
            switch (gender) {
                case 0: return "未知";
                case 1: return "男";
                case 2: return "女";
                default: return "未知";
            }
        }
    }
}