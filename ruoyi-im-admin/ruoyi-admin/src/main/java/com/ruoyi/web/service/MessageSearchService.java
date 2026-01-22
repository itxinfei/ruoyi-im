package com.ruoyi.web.service;

import com.ruoyi.web.domain.vo.MessageDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 消息搜索服务
 * 提供全文检索、高级搜索和搜索建议功能
 * 
 * @author ruoyi
 * @version 1.0
 * @since 2025-01-22
 */
@Service
public class MessageSearchService {

    private static final Logger logger = LoggerFactory.getLogger(MessageSearchService.class);
    
    private static final String SEARCH_RESULT_KEY_PREFIX = "search:result:";
    private static final String SEARCH_SUGGESTION_KEY_PREFIX = "search:suggestion:";
    private static final int SEARCH_CACHE_EXPIRE_TIME = 300; // 5分钟缓存
    private static final int MAX_SEARCH_RESULTS = 100;
    private static final int MAX_SUGGESTIONS = 10;
    
    // 搜索模式枚举
    public enum SearchMode {
        FULL_TEXT,      // 全文检索
        KEYWORD,        // 关键词搜索
        REGEX,          // 正则表达式搜索
        FUZZY           // 模糊搜索
        ADVANCED        // 高级搜索
    }
    
    @Autowired
    private IImMessageService imMessageService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 全文搜索消息
     */
    public List<MessageDetailVO> fullTextSearch(String keyword, SearchMode mode, Long conversationId, 
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        return performSearch(keyword, mode, conversationId, startTime, endTime, 
                SearchOptions.builder()
                    .useFullText(true)
                    .caseSensitive(false)
                    .highlightResults(true)
                    .build());
    }
    
    /**
     * 高级搜索消息
     */
    public List<MessageDetailVO> advancedSearch(SearchCriteria criteria) {
        return performSearch(criteria.getKeyword(), SearchMode.ADVANCED, 
                criteria.getConversationId(), criteria.getStartTime(), criteria.getEndTime(),
                SearchOptions.builder()
                    .messageTypes(criteria.getMessageTypes())
                    .senderIds(criteria.getSenderIds())
                    .sensitiveLevels(criteria.getSensitiveLevels())
                    .minDate(criteria.getMinDate())
                    .maxDate(criteria.getMaxDate())
                    .sortBy(criteria.getSortBy())
                    .sortOrder(criteria.getSortOrder())
                    .build());
    }
    
    /**
     * 获取搜索建议
     */
    public List<String> getSearchSuggestions(String partialKeyword) {
        String cacheKey = SEARCH_SUGGESTION_KEY_PREFIX + partialKeyword.toLowerCase();
        
        // 尝试从缓存获取
        List<String> suggestions = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (suggestions != null) {
            return suggestions;
        }
        
        // 生成搜索建议
        suggestions = generateSuggestions(partialKeyword);
        
        // 缓存建议
        redisTemplate.opsForValue().set(cacheKey, suggestions, SEARCH_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        
        return suggestions;
    }
    
    /**
     * 保存搜索历史
     */
    public void saveSearchHistory(Long userId, String keyword, SearchCriteria criteria) {
        try {
            String historyKey = "search:history:" + userId;
            SearchHistory historyItem = new SearchHistory(keyword, criteria, LocalDateTime.now());
            
            redisTemplate.opsForList().rightPush(historyKey, historyItem);
            
            // 保持最近50条搜索历史
            redisTemplate.opsForList().trim(historyKey, 0, 50);
            
            // 7天后过期
            redisTemplate.expire(historyKey, 7, TimeUnit.DAYS);
            
        } catch (Exception e) {
            logger.warn("保存搜索历史失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取搜索历史
     */
    public List<SearchHistory> getSearchHistory(Long userId) {
        try {
            String historyKey = "search:history:" + userId;
            List<Object> historyObjects = redisTemplate.opsForList().range(historyKey, 0, -1);
            
            List<SearchHistory> history = new ArrayList<>();
            for (Object obj : historyObjects) {
                if (obj instanceof SearchHistory) {
                    history.add((SearchHistory) obj);
                }
            }
            
            return history;
        } catch (Exception e) {
            logger.warn("获取搜索历史失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 清除搜索历史
     */
    public void clearSearchHistory(Long userId) {
        try {
            String historyKey = "search:history:" + userId;
            redisTemplate.delete(historyKey);
        } catch (Exception e) {
            logger.warn("清除搜索历史失败: {}", e.getMessage());
        }
    }
    
    /**
     * 执行搜索的核心逻辑
     */
    private List<MessageDetailVO> performSearch(String keyword, SearchMode mode, Long conversationId,
                                          LocalDateTime startTime, LocalDateTime endTime, SearchOptions options) {
        try {
            String cacheKey = generateSearchCacheKey(keyword, mode, conversationId, startTime, endTime, options);
            
            // 尝试从缓存获取结果
            List<MessageDetailVO> cachedResults = (List<MessageDetailVO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResults != null) {
                logger.debug("从缓存获取搜索结果，关键词: {}", keyword);
                return cachedResults;
            }
            
            List<MessageDetailVO> results = new ArrayList<>();
            
            // 根据搜索模式执行不同的搜索逻辑
            switch (mode) {
                case FULL_TEXT:
                    results = performFullTextSearch(keyword, conversationId, startTime, endTime, options);
                    break;
                case KEYWORD:
                    results = performKeywordSearch(keyword, conversationId, startTime, endTime, options);
                    break;
                case REGEX:
                    results = performRegexSearch(keyword, conversationId, startTime, endTime, options);
                    break;
                case FUZZY:
                    results = performFuzzySearch(keyword, conversationId, startTime, endTime, options);
                    break;
                case ADVANCED:
                    results = performAdvancedSearch(conversationId, startTime, endTime, options);
                    break;
                default:
                    results = performDefaultSearch(keyword, conversationId, startTime, endTime, options);
                    break;
            }
            
            // 限制结果数量
            if (results.size() > MAX_SEARCH_RESULTS) {
                results = results.subList(0, MAX_SEARCH_RESULTS);
            }
            
            // 缓存搜索结果
            redisTemplate.opsForValue().set(cacheKey, results, SEARCH_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            
            logger.info("搜索完成，关键词: {}, 模式: {}, 结果数量: {}", keyword, mode, results.size());
            return results;
            
        } catch (Exception e) {
            logger.error("搜索执行失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 全文检索
     */
    private List<MessageDetailVO> performFullTextSearch(String keyword, Long conversationId,
                                                     LocalDateTime startTime, LocalDateTime endTime, SearchOptions options) {
        // 这里可以集成Elasticsearch或Lucene进行全文检索
        // 暂时使用数据库LIKE查询
        String searchPattern = "%" + keyword + "%";
        List<MessageDetailVO> results = imMessageService.searchMessagesByContent(searchPattern, conversationId, startTime, endTime);
        
        return filterAndSortResults(results, keyword, options);
    }
    
    /**
     * 关键词搜索
     */
    private List<MessageDetailVO> performKeywordSearch(String keyword, Long conversationId,
                                                   LocalDateTime startTime, LocalDateTime endTime, SearchOptions options) {
        String[] keywords = keyword.split("\\s+");
        List<MessageDetailVO> allResults = new ArrayList<>();
        
        for (String kw : keywords) {
            if (kw.trim().isEmpty()) continue;
            
            String searchPattern = "%" + kw.trim() + "%";
            List<MessageDetailVO> keywordResults = imMessageService.searchMessagesByContent(searchPattern, conversationId, startTime, endTime);
            allResults.addAll(keywordResults);
        }
        
        return filterAndSortResults(allResults, keyword, options);
    }
    
    /**
     * 正则表达式搜索
     */
    private List<MessageDetailVO> performRegexSearch(String regex, Long conversationId,
                                                  LocalDateTime startTime, LocalDateTime endTime, SearchOptions options) {
        try {
            Pattern pattern = Pattern.compile(regex, options.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
            
            List<MessageDetailVO> allMessages = imMessageService.getMessagesByConversationId(conversationId, 1, 10000);
            List<MessageDetailVO> matchedResults = new ArrayList<>();
            
            for (MessageDetailVO message : allMessages) {
                if (message.getContent() != null && pattern.matcher(message.getContent()).find()) {
                    matchedResults.add(message);
                }
            }
            
            return filterAndSortResults(matchedResults, regex, options);
            
        } catch (Exception e) {
            logger.warn("正则表达式搜索失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 模糊搜索
     */
    private List<MessageDetailVO> performFuzzySearch(String keyword, Long conversationId,
                                                 LocalDateTime startTime, LocalDateTime endTime, SearchOptions options) {
        // 使用编辑距离算法进行模糊搜索
        // 这里简化实现，实际可以集成Levenshtein算法
        List<MessageDetailVO> allMessages = imMessageService.getMessagesByConversationId(conversationId, 1, 1000);
        List<MessageDetailVO> fuzzyResults = new ArrayList<>();
        
        for (MessageDetailVO message : allMessages) {
            if (message.getContent() != null && isFuzzyMatch(keyword, message.getContent(), 0.6)) {
                fuzzyResults.add(message);
            }
        }
        
        return filterAndSortResults(fuzzyResults, keyword, options);
    }
    
    /**
     * 高级搜索
     */
    private List<MessageDetailVO> performAdvancedSearch(Long conversationId, LocalDateTime startTime, LocalDateTime endTime, SearchOptions options) {
        // 根据高级选项执行搜索
        List<MessageDetailVO> results = new ArrayList<>();
        
        if (options.getMessageTypes() != null && !options.getMessageTypes().isEmpty()) {
            // 按消息类型搜索
            for (String messageType : options.getMessageTypes()) {
                List<MessageDetailVO> typeResults = imMessageService.searchMessagesByType(messageType, conversationId, startTime, endTime);
                results.addAll(typeResults);
            }
        }
        
        if (options.getSenderIds() != null && !options.getSenderIds().isEmpty()) {
            // 按发送者搜索
            for (Long senderId : options.getSenderIds()) {
                List<MessageDetailVO> senderResults = imMessageService.searchMessagesBySenderId(senderId, conversationId, startTime, endTime);
                results.addAll(senderResults);
            }
        }
        
        if (options.getSensitiveLevels() != null && !options.getSensitiveLevels().isEmpty()) {
            // 按敏感级别搜索
            for (Integer level : options.getSensitiveLevels()) {
                List<MessageDetailVO> levelResults = imMessageService.searchMessagesBySensitiveLevel(level, conversationId, startTime, endTime);
                results.addAll(levelResults);
            }
        }
        
        return filterAndSortResults(results, null, options);
    }
    
    /**
     * 默认搜索
     */
    private List<MessageDetailVO> performDefaultSearch(String keyword, Long conversationId,
                                                   LocalDateTime startTime, LocalDateTime endTime, SearchOptions options) {
        return performKeywordSearch(keyword, conversationId, startTime, endTime, options);
    }
    
    /**
     * 简单的模糊匹配判断
     */
    private boolean isFuzzyMatch(String keyword, String text, double threshold) {
        if (keyword == null || text == null) return false;
        
        keyword = keyword.toLowerCase();
        text = text.toLowerCase();
        
        int maxLength = Math.max(keyword.length(), text.length());
        int distance = calculateLevenshteinDistance(keyword, text);
        
        double similarity = 1.0 - (double) distance / maxLength;
        return similarity >= threshold;
    }
    
    /**
     * 计算编辑距离
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * 过滤和排序结果
     */
    private List<MessageDetailVO> filterAndSortResults(List<MessageDetailVO> results, String keyword, SearchOptions options) {
        // 应用高亮
        if (options.isHighlightResults() && keyword != null) {
            for (MessageDetailVO message : results) {
                if (message.getContent() != null) {
                    message.setContent(highlightKeyword(message.getContent(), keyword, options.isCaseSensitive()));
                }
            }
        }
        
        // 排序
        return sortResults(results, options);
    }
    
    /**
     * 高亮关键词
     */
    private String highlightKeyword(String text, String keyword, boolean caseSensitive) {
        if (text == null || keyword == null || keyword.isEmpty()) {
            return text;
        }
        
        String searchText = caseSensitive ? text : text.toLowerCase();
        String searchKeyword = caseSensitive ? keyword : keyword.toLowerCase();
        
        return searchText.replaceAll(searchKeyword, "<mark>" + keyword + "</mark>");
    }
    
    /**
     * 排序结果
     */
    private List<MessageDetailVO> sortResults(List<MessageDetailVO> results, SearchOptions options) {
        if (options.getSortBy() == null) {
            return results;
        }
        
        results.sort((m1, m2) -> {
            switch (options.getSortBy()) {
                case "createTime":
                    return m1.getCreateTime().compareTo(m2.getCreateTime()) * 
                           (options.getSortOrder().equals("desc") ? -1 : 1);
                case "senderId":
                    return Long.compare(m1.getSenderId(), m2.getSenderId()) * 
                           (options.getSortOrder().equals("desc") ? -1 : 1);
                default:
                    return m1.getCreateTime().compareTo(m2.getCreateTime()) * 
                           (options.getSortOrder().equals("desc") ? -1 : 1);
            }
        });
        
        return results;
    }
    
    /**
     * 生成搜索缓存键
     */
    private String generateSearchCacheKey(String keyword, SearchMode mode, Long conversationId,
                                    LocalDateTime startTime, LocalDateTime endTime, SearchOptions options) {
        StringBuilder keyBuilder = new StringBuilder("search:");
        keyBuilder.append(mode.name()).append(":");
        
        if (keyword != null) {
            keyBuilder.append(keyword.hashCode());
        }
        
        if (conversationId != null) {
            keyBuilder.append(":conv").append(conversationId);
        }
        
        if (startTime != null) {
            keyBuilder.append(":start").append(startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        if (endTime != null) {
            keyBuilder.append(":end").append(endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * 生成搜索建议
     */
    private List<String> generateSuggestions(String partialKeyword) {
        List<String> suggestions = new ArrayList<>();
        
        // 这里可以实现更智能的建议算法
        // 暂时返回简单的基于历史的建议
        suggestions.add(partialKeyword);
        suggestions.add(partialKeyword + "的消息");
        suggestions.add("关于" + partialKeyword);
        
        return suggestions.subList(0, Math.min(suggestions.size(), MAX_SUGGESTIONS));
    }
}