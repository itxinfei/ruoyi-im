package com.ruoyi.web.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息搜索条件DTO
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Data
public class SearchCriteria {

    private String keyword;
    
    private String searchMode = "FULL_TEXT";
    
    private Long conversationId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private List<String> messageTypes;
    
    private List<Long> senderIds;
    
    private List<Integer> sensitiveLevels;
    
    private LocalDateTime minDate;
    
    private LocalDateTime maxDate;
    
    private String sortBy = "createTime";
    
    private String sortOrder = "desc";
    
    private Boolean caseSensitive = false;
    
    private Boolean highlightResults = true;
    
    private Integer pageSize = 20;
    
    private Integer pageNum = 1;
    
    /**
     * 搜索选项构建器
     */
    public static class Builder {
        private String keyword;
        private Long conversationId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<String> messageTypes;
        private List<Long> senderIds;
        private List<Integer> sensitiveLevels;
        private LocalDateTime minDate;
        private LocalDateTime maxDate;
        private String sortBy = "createTime";
        private String sortOrder = "desc";
        private Boolean caseSensitive = false;
        private Boolean highlightResults = true;
        private Boolean useFullText = false;
        
        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }
        
        public Builder conversationId(Long conversationId) {
            this.conversationId = conversationId;
            return this;
        }
        
        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }
        
        public Builder messageTypes(List<String> messageTypes) {
            this.messageTypes = messageTypes;
            return this;
        }
        
        public Builder senderIds(List<Long> senderIds) {
            this.senderIds = senderIds;
            return this;
        }
        
        public Builder sensitiveLevels(List<Integer> sensitiveLevels) {
            this.sensitiveLevels = sensitiveLevels;
            return this;
        }
        
        public Builder minDate(LocalDateTime minDate) {
            this.minDate = minDate;
            return this;
        }
        
        public Builder maxDate(LocalDateTime maxDate) {
            this.maxDate = maxDate;
            return this;
        }
        
        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }
        
        public Builder sortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }
        
        public Builder caseSensitive(Boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }
        
        public Builder highlightResults(Boolean highlightResults) {
            this.highlightResults = highlightResults;
            return this;
        }
        
        public Builder useFullText(Boolean useFullText) {
            this.useFullText = useFullText;
            return this;
        }
        
        public SearchOptions build() {
            SearchOptions options = new SearchOptions();
            options.setKeyword(keyword);
            options.setConversationId(conversationId);
            options.setStartTime(startTime);
            options.setEndTime(endTime);
            options.setMessageTypes(messageTypes);
            options.setSenderIds(senderIds);
            options.setSensitiveLevels(sensitiveLevels);
            options.setMinDate(minDate);
            options.setMaxDate(maxDate);
            options.setSortBy(sortBy);
            options.setSortOrder(sortOrder);
            options.setCaseSensitive(caseSensitive);
            options.setHighlightResults(highlightResults);
            options.setUseFullText(useFullText);
            return options;
        }
    }
}

/**
 * 搜索选项类
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Data
class SearchOptions {
    private String keyword;
    private Long conversationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> messageTypes;
    private List<Long> senderIds;
    private List<Integer> sensitiveLevels;
    private LocalDateTime minDate;
    private LocalDateTime maxDate;
    private String sortBy;
    private String sortOrder;
    private Boolean caseSensitive = false;
    private Boolean highlightResults = true;
    private Boolean useFullText = false;
}

/**
 * 搜索历史记录类
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Data
public class SearchHistory {
    
    private String keyword;
    
    private SearchCriteria criteria;
    
    private LocalDateTime searchTime;
    
    private Integer resultCount;
    
    private Long searchTimeMillis;
    
    public SearchHistory(String keyword, SearchCriteria criteria, LocalDateTime searchTime) {
        this.keyword = keyword;
        this.criteria = criteria;
        this.searchTime = searchTime;
        this.searchTimeMillis = System.currentTimeMillis();
        this.resultCount = 0;
    }
}