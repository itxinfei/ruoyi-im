package com.ruoyi.im.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 消息验证器
 * 提供消息内容验证、敏感词检测等功能
 *
 * @author ruoyi
 */
@Component
public class MessageValidator {

    private static final Logger log = LoggerFactory.getLogger(MessageValidator.class);

    private static final int MAX_TEXT_LENGTH = 5000;
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * 敏感词字典（DFA算法模型）
     */
    private Map<String, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 默认敏感词列表
     */
    private static final String[] DEFAULT_SENSITIVE_WORDS = {
            // 政治敏感词
            "法轮", "法轮功", "falun", "falungong",
            "六四", "天安门事件",
            "江泽民", "胡锦涛",
            // 暴力恐怖词
            "恐怖", "炸弹", "炸药", "制造炸弹",
            "杀人", "如何杀人", "杀人在",
            // 色情词汇
            "色情", "黄色", "淫秽",
            // 赌博词汇
            "赌博", "赌场", "博彩", "时时彩",
            // 毒品词汇
            "毒品", "大麻", "海洛因", "冰毒", "摇头丸",
            "吸毒", "贩毒",
            // 诈骗词汇
            "刷单", "兼职刷单", "网络兼职", "时时彩", "PK10",
            // 其他违禁词
            "代开", "代办", "假证", "办证"
    };

    /**
     * 初始化敏感词字典
     */
    @PostConstruct
    public void init() {
        long startTime = System.currentTimeMillis();
        // 先尝试从配置文件加载
        loadSensitiveWordsFromFile();
        // 添加默认敏感词
        for (String word : DEFAULT_SENSITIVE_WORDS) {
            addSensitiveWord(word);
        }
        log.info("敏感词字典初始化完成，耗时: {}ms", System.currentTimeMillis() - startTime);
    }

    /**
     * 从配置文件加载敏感词
     */
    private void loadSensitiveWordsFromFile() {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        addSensitiveWord(line);
                    }
                }
                reader.close();
                log.info("从配置文件加载敏感词成功");
            } else {
                log.info("未找到敏感词配置文件，使用默认敏感词库");
            }
        } catch (IOException e) {
            log.warn("加载敏感词配置文件失败: {}", e.getMessage());
        }
    }

    /**
     * 添加敏感词到DFA字典
     *
     * @param word 敏感词
     */
    private void addSensitiveWord(String word) {
        if (!StringUtils.hasText(word)) {
            return;
        }
        Map<String, Object> nowMap = sensitiveWordMap;
        for (int i = 0; i < word.length(); i++) {
            char key = word.charAt(i);
            Object value = nowMap.get(String.valueOf(key));
            if (value == null) {
                Map<String, Object> newMap = new HashMap<>();
                nowMap.put(String.valueOf(key), newMap);
                nowMap = newMap;
            } else {
                nowMap = (Map<String, Object>) value;
            }

            // 标记结束
            if (i == word.length() - 1) {
                nowMap.put("isEnd", true);
            }
        }
    }

    /**
     * 验证文本消息
     */
    public void validateTextMessage(String content) {
        if (!StringUtils.hasText(content)) {
            throw new RuntimeException("消息内容不能为空");
        }

        if (content.length() > MAX_TEXT_LENGTH) {
            throw new RuntimeException("消息内容过长，最大支持" + MAX_TEXT_LENGTH + "个字符");
        }

        // 检查是否包含敏感词
        if (containsSensitiveWords(content)) {
            throw new RuntimeException("消息包含敏感词汇");
        }
    }

    /**
     * 验证文件消息
     */
    public void validateFileMessage(String fileName, long fileSize, String fileType) {
        if (!StringUtils.hasText(fileName)) {
            throw new RuntimeException("文件名不能为空");
        }

        if (fileSize <= 0) {
            throw new RuntimeException("文件大小无效");
        }

        if (fileSize > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小超过限制，最大支持50MB");
        }

        if (!isAllowedFileType(fileType)) {
            throw new RuntimeException("不支持的文件类型");
        }
    }

    /**
     * 验证图片消息
     */
    public void validateImageMessage(String fileName, long fileSize) {
        validateFileMessage(fileName, fileSize, getFileExtension(fileName));

        String extension = getFileExtension(fileName).toLowerCase();
        if (!isImageType(extension)) {
            throw new RuntimeException("不支持的图片格式");
        }
    }

    /**
     * 检查是否包含敏感词
     * 使用DFA算法进行高效匹配
     *
     * @param content 待检测内容
     * @return true表示包含敏感词，false表示不包含
     */
    private boolean containsSensitiveWords(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }

        for (int i = 0; i < content.length(); i++) {
            int matchLength = checkSensitiveWord(content, i);
            if (matchLength > 0) {
                String sensitiveWord = content.substring(i, i + matchLength);
                log.warn("检测到敏感词: {}", sensitiveWord);
                return true;
            }
        }
        return false;
    }

    /**
     * 从指定位置开始检查敏感词
     *
     * @param content 待检测内容
     * @param beginIndex 开始位置
     * @return 匹配到的敏感词长度，0表示未匹配
     */
    private int checkSensitiveWord(String content, int beginIndex) {
        Map<String, Object> nowMap = sensitiveWordMap;
        int matchLength = 0;
        boolean isEnd = false;

        for (int i = beginIndex; i < content.length(); i++) {
            char key = content.charAt(i);
            Object value = nowMap.get(String.valueOf(key));

            if (value == null) {
                break;
            }

            matchLength++;
            nowMap = (Map<String, Object>) value;
            isEnd = Boolean.TRUE.equals(nowMap.get("isEnd"));

            if (isEnd) {
                // 找到完整敏感词
                return matchLength;
            }
        }

        return isEnd ? matchLength : 0;
    }

    /**
     * 获取内容中的所有敏感词
     *
     * @param content 待检测内容
     * @return 敏感词列表
     */
    public Set<String> getSensitiveWords(String content) {
        Set<String> sensitiveWords = new HashSet<>();
        if (!StringUtils.hasText(content)) {
            return sensitiveWords;
        }

        for (int i = 0; i < content.length(); i++) {
            int matchLength = checkSensitiveWord(content, i);
            if (matchLength > 0) {
                String sensitiveWord = content.substring(i, i + matchLength);
                sensitiveWords.add(sensitiveWord);
                i += matchLength - 1; // 跳过已匹配的部分
            }
        }
        return sensitiveWords;
    }

    /**
     * 替换内容中的敏感词为指定字符
     *
     * @param content 待处理内容
     * @param replaceChar 替换字符，默认为*
     * @return 替换后的内容
     */
    public String replaceSensitiveWords(String content, char replaceChar) {
        if (!StringUtils.hasText(content)) {
            return content;
        }

        StringBuilder result = new StringBuilder(content);
        Set<int[]> ranges = new HashSet<>(); // 记录敏感词位置范围

        for (int i = 0; i < content.length(); i++) {
            int matchLength = checkSensitiveWord(content, i);
            if (matchLength > 0) {
                ranges.add(new int[]{i, i + matchLength});
                i += matchLength - 1;
            }
        }

        // 从后往前替换，避免位置偏移
        List<int[]> sortedRanges = new ArrayList<>(ranges);
        sortedRanges.sort((a, b) -> b[0] - a[0]);
        for (int[] range : sortedRanges) {
            for (int i = range[0]; i < range[1]; i++) {
                result.setCharAt(i, replaceChar);
            }
        }

        return result.toString();
    }

    /**
     * 检查是否为允许的文件类型
     */
    private boolean isAllowedFileType(String fileType) {
        if (!StringUtils.hasText(fileType)) {
            return false;
        }

        String[] allowedTypes = {
                "jpg", "jpeg", "png", "gif", "bmp", "webp", // 图片
                "mp4", "avi", "mov", "wmv", "flv", "mkv",   // 视频
                "mp3", "wav", "aac", "flac", "ogg",         // 音频
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // 文档
                "txt", "zip", "rar", "7z"                   // 其他
        };

        String lowerType = fileType.toLowerCase();
        for (String type : allowedTypes) {
            if (type.equals(lowerType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否为图片类型
     */
    private boolean isImageType(String extension) {
        String[] imageTypes = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        for (String type : imageTypes) {
            if (type.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1);
    }
}
