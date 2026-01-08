package com.ruoyi.im.service;

import java.util.Set;

/**
 * 敏感词过滤服务接口
 *
 * @author ruoyi
 */
public interface ISensitiveWordService {

    /**
     * 检查文本中是否包含敏感词
     *
     * @param text 待检查文本
     * @return 是否包含敏感词
     */
    boolean containsSensitiveWord(String text);

    /**
     * 检测文本中的敏感词
     *
     * @param text 待检测文本
     * @return 敏感词集合
     */
    Set<String> detectSensitiveWords(String text);

    /**
     * 过滤敏感词
     * 将敏感词替换为指定字符（默认***）
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    String filter(String text);

    /**
     * 过滤敏感词（指定替换字符）
     *
     * @param text 待过滤文本
     * @param replacement 替换字符
     * @return 过滤后的文本
     */
    String filter(String text, String replacement);

    /**
     * 重新加载敏感词
     * 从数据库重新加载敏感词配置
     */
    void reload();

    /**
     * 获取敏感词数量
     *
     * @return 敏感词数量
     */
    int getSensitiveWordCount();
}
