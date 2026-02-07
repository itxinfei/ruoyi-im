package com.ruoyi.im.service.impl;

import com.ruoyi.im.mapper.ImSensitiveWordMapper;
import com.ruoyi.im.service.ISensitiveWordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 敏感词过滤服务实现
 * 使用DFA（确定有限状态自动机）算法实现高效敏感词检测
 *
 * @author ruoyi
 */
@Service
public class SensitiveWordServiceImpl implements ISensitiveWordService {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordServiceImpl.class);

    private final ImSensitiveWordMapper sensitiveWordMapper;

    public SensitiveWordServiceImpl(ImSensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }

    /**
     * 敏感词字典（DFA模型）
     */
    private Map<String, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 最短匹配长度
     */
    private int minLength = Integer.MAX_VALUE;

    /**
     * 最长匹配长度
     */
    private int maxLength = 0;

    /**
     * 默认替换符
     */
    private static final String DEFAULT_REPLACEMENT = "***";

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        reload();
    }

    @Override
    public void reload() {
        try {
            List<String> words = sensitiveWordMapper.selectAllEnabledWords();
            initSensitiveWordMap(words);
            logger.info("敏感词库加载完成，共加载{}个敏感词", words.size());
        } catch (Exception e) {
            logger.error("敏感词库加载失败", e);
        }
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            int matchLength = checkSensitiveWord(text, i);
            if (matchLength > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> detectSensitiveWords(String text) {
        Set<String> sensitiveWords = new HashSet<>();
        if (text == null || text.isEmpty()) {
            return sensitiveWords;
        }

        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                String word = text.substring(i, i + length);
                sensitiveWords.add(word);
                i += length - 1; // 跳过已检测的部分
            }
        }
        return sensitiveWords;
    }

    @Override
    public String filter(String text) {
        return filter(text, DEFAULT_REPLACEMENT);
    }

    @Override
    public String filter(String text, String replacement) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < result.length(); i++) {
            int length = checkSensitiveWord(result.toString(), i);
            if (length > 0) {
                // 替换敏感词
                for (int j = i; j < i + length; j++) {
                    if (j < result.length()) {
                        result.setCharAt(j, replacement.charAt(0));
                    }
                }
                i += length - 1; // 跳过已替换的部分
            }
        }
        return result.toString();
    }

    @Override
    public int getSensitiveWordCount() {
        return countNodes(sensitiveWordMap);
    }

    /**
     * 递归统计节点数量
     */
    @SuppressWarnings("unchecked")
    private int countNodes(Map<String, Object> map) {
        int count = 0;
        for (Object value : map.values()) {
            if (value instanceof Map) {
                count += countNodes((Map<String, Object>) value);
            } else if (Boolean.TRUE.equals(value)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 初始化敏感词字典（DFA模型）
     *
     * @param words 敏感词列表
     */
    @SuppressWarnings("unchecked")
    private void initSensitiveWordMap(List<String> words) {
        sensitiveWordMap.clear();
        minLength = Integer.MAX_VALUE;
        maxLength = 0;

        for (String word : words) {
            if (word == null || word.isEmpty()) {
                continue;
            }

            // 更新最小/最大长度
            int len = word.length();
            if (len < minLength) {
                minLength = len;
            }
            if (len > maxLength) {
                maxLength = len;
            }

            Map<String, Object> nowMap = sensitiveWordMap;
            for (int i = 0; i < len; i++) {
                char charAt = word.charAt(i);
                String key = String.valueOf(charAt);

                Object nextObj = nowMap.get(key);
                Map<String, Object> nextMap;
                if (nextObj == null) {
                    nextMap = new HashMap<>();
                    nowMap.put(key, nextMap);
                } else if (nextObj instanceof Map) {
                    nextMap = (Map<String, Object>) nextObj;
                } else {
                    // 如果是其他类型（如Boolean），需要新建Map
                    nextMap = new HashMap<>();
                    nowMap.put(key, nextMap);
                }

                nowMap = nextMap;

                // 判断是否为最后一个字符
                if (i == len - 1) {
                    nowMap.put("isEnd", true);
                }
            }
        }
    }

    /**
     * 检查文本中是否包含敏感词
     *
     * @param text 文本
     * @param beginIndex 检查起始位置
     * @return 匹配到的敏感词长度，0表示未匹配
     */
    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String text, int beginIndex) {
        if (beginIndex >= text.length()) {
            return 0;
        }

        Map<String, Object> nowMap = sensitiveWordMap;
        int matchLength = 0;
        boolean isEnd = false;

        for (int i = beginIndex; i < text.length(); i++) {
            char charAt = text.charAt(i);
            String key = String.valueOf(charAt);

            Object nextObj = nowMap.get(key);
            if (nextObj == null) {
                break;
            }

            if (nextObj instanceof Map) {
                nowMap = (Map<String, Object>) nextObj;
                matchLength++;

                // 检查是否为敏感词结尾
                Object endFlag = nowMap.get("isEnd");
                if (endFlag != null && Boolean.TRUE.equals(endFlag)) {
                    isEnd = true;
                    // 继续检查是否可以匹配更长的词
                    if (!nowMap.isEmpty()) {
                        if (nowMap.size() > 1 || !nowMap.containsKey("isEnd")) {
                            continue;
                        }
                    }
                    break;
                }
            } else {
                // 如果不是Map，说明到达了叶子节点
                break;
            }
        }

        // 如果匹配到敏感词结尾，返回匹配长度
        if (isEnd) {
            return matchLength;
        }
        return 0;
    }
}
