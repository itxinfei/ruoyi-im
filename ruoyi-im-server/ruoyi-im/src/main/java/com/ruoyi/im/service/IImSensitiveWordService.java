package com.ruoyi.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.im.domain.ImSensitiveWord;

import java.util.List;
import java.util.Map;

/**
 * 敏感词Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImSensitiveWordService extends IService<ImSensitiveWord> {

    /**
     * 检测文本中的敏感词
     * 
     * @param content 待检测内容
     * @return 检测结果（包含命中的敏感词和级别）
     */
    Map<String, Object> detectSensitiveWords(String content);

    /**
     * 过滤敏感词（替换为*）
     * 
     * @param content 待过滤内容
     * @return 过滤后的内容
     */
    String filterSensitiveWords(String content);

    /**
     * 添加敏感词
     * 
     * @param word 敏感词
     * @param level 敏感级别
     * @param category 分类
     * @return 是否成功
     */
    boolean addSensitiveWord(String word, String level, String category);

    /**
     * 批量添加敏感词
     * 
     * @param words 敏感词列表
     * @return 添加数量
     */
    int addSensitiveWordBatch(List<ImSensitiveWord> words);

    /**
     * 删除敏感词
     * 
     * @param wordId 敏感词ID
     * @return 是否成功
     */
    boolean deleteSensitiveWord(Long wordId);

    /**
     * 批量删除敏感词
     * 
     * @param wordIds 敏感词ID列表
     * @return 删除数量
     */
    int deleteSensitiveWordBatch(List<Long> wordIds);

    /**
     * 更新敏感词
     * 
     * @param sensitiveWord 敏感词信息
     * @return 是否成功
     */
    boolean updateSensitiveWord(ImSensitiveWord sensitiveWord);

    /**
     * 查询启用的敏感词列表
     * 
     * @return 敏感词列表
     */
    List<ImSensitiveWord> selectEnabledWords();

    /**
     * 根据级别查询敏感词
     * 
     * @param level 敏感级别
     * @return 敏感词列表
     */
    List<ImSensitiveWord> selectWordsByLevel(String level);

    /**
     * 根据分类查询敏感词
     * 
     * @param category 分类
     * @return 敏感词列表
     */
    List<ImSensitiveWord> selectWordsByCategory(String category);

    /**
     * 搜索敏感词
     * 
     * @param keyword 搜索关键词
     * @return 敏感词列表
     */
    List<ImSensitiveWord> searchSensitiveWords(String keyword);

    /**
     * 检查敏感词是否存在
     * 
     * @param word 敏感词
     * @return 是否存在
     */
    boolean existsSensitiveWord(String word);

    /**
     * 批量更新敏感词启用状态
     * 
     * @param wordIds 敏感词ID列表
     * @param enabled 是否启用
     * @return 更新数量
     */
    int updateEnabledStatusBatch(List<Long> wordIds, boolean enabled);

    /**
     * 统计各分类敏感词数量
     * 
     * @return 统计信息
     */
    Map<String, Integer> countWordsByCategory();

    /**
     * 统计各级别敏感词数量
     * 
     * @return 统计信息
     */
    Map<String, Integer> countWordsByLevel();

    /**
     * 查询所有敏感词分类
     * 
     * @return 分类列表
     */
    List<String> selectAllCategories();

    /**
     * 导出敏感词数据
     * 
     * @param category 分类（可选）
     * @param level 级别（可选）
     * @param enabled 是否启用（可选）
     * @return 敏感词列表
     */
    List<ImSensitiveWord> exportSensitiveWords(String category, String level, Boolean enabled);

    /**
     * 从文件导入敏感词
     * 
     * @param filePath 文件路径
     * @param category 分类
     * @param level 级别
     * @return 导入数量
     */
    int importSensitiveWordsFromFile(String filePath, String category, String level);

    /**
     * 重新构建敏感词检测器
     * 
     * @return 是否成功
     */
    boolean rebuildSensitiveWordDetector();

    /**
     * 获取敏感词检测统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getSensitiveWordStatistics();

    /**
     * 验证敏感词格式
     * 
     * @param word 敏感词
     * @return 是否有效
     */
    boolean validateSensitiveWord(String word);
}