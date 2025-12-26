package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.im.domain.ImSensitiveWord;
import com.ruoyi.im.mapper.ImSensitiveWordMapper;
import com.ruoyi.im.service.IImSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 敏感词Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImSensitiveWordServiceImpl extends ServiceImpl<ImSensitiveWordMapper, ImSensitiveWord> implements IImSensitiveWordService {

    @Autowired
    private ImSensitiveWordMapper imSensitiveWordMapper;

    /**
     * 检测文本中的敏感词
     * 
     * @param content 待检测内容
     * @return 检测结果（包含命中的敏感词和级别）
     */
    @Override
    public Map<String, Object> detectSensitiveWords(String content) {
        // 获取所有启用的敏感词
        List<ImSensitiveWord> words = selectEnabledWords();
        
        Map<String, Object> result = new HashMap<>();
        List<String> hitWords = new ArrayList<>();
        String level = "NORMAL"; // 默认级别
        
        if (content != null && !content.isEmpty() && words != null && !words.isEmpty()) {
            for (ImSensitiveWord word : words) {
                if (content.contains(word.getWord())) {
                    hitWords.add(word.getWord());
                    // 根据检测到的最高级别设置结果级别
                    if ("BLOCK".equals(word.getLevel()) || 
                        ("WARN".equals(word.getLevel()) && !"BLOCK".equals(level))) {
                        level = word.getLevel();
                    }
                }
            }
        }
        
        result.put("level", level);
        result.put("hitWords", hitWords);
        result.put("wordId", hitWords.isEmpty() ? null : getWordIdByWord(hitWords.get(0)));
        
        return result;
    }
    
    private Long getWordIdByWord(String word) {
        List<ImSensitiveWord> words = imSensitiveWordMapper.selectList(null);
        for (ImSensitiveWord w : words) {
            if (w.getWord().equals(word)) {
                return w.getId();
            }
        }
        return null;
    }

    /**
     * 过滤敏感词（替换为*）
     * 
     * @param content 待过滤内容
     * @return 过滤后的内容
     */
    @Override
    public String filterSensitiveWords(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        List<ImSensitiveWord> words = selectEnabledWords();
        
        String filteredContent = content;
        if (words != null) {
            for (ImSensitiveWord word : words) {
                if (filteredContent.contains(word.getWord())) {
                    String replacement = new String(new char[word.getWord().length()]).replace("\0", "*");
                    filteredContent = filteredContent.replace(word.getWord(), replacement);
                }
            }
        }
        
        return filteredContent;
    }

    /**
     * 添加敏感词
     * 
     * @param word 敏感词
     * @param level 敏感级别
     * @param category 分类
     * @return 是否成功
     */
    @Override
    public boolean addSensitiveWord(String word, String level, String category) {
        ImSensitiveWord sensitiveWord = new ImSensitiveWord();
        sensitiveWord.setWord(word);
        sensitiveWord.setLevel(level);
        sensitiveWord.setCategory(category);
        sensitiveWord.setEnabled(true);
        return save(sensitiveWord);
    }

    /**
     * 批量添加敏感词
     * 
     * @param words 敏感词列表
     * @return 添加数量
     */
    @Override
    public int addSensitiveWordBatch(List<ImSensitiveWord> words) {
        int count = 0;
        for (ImSensitiveWord word : words) {
            if (save(word)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 删除敏感词
     * 
     * @param wordId 敏感词ID
     * @return 是否成功
     */
    @Override
    public boolean deleteSensitiveWord(Long wordId) {
        return removeById(wordId);
    }

    /**
     * 批量删除敏感词
     * 
     * @param wordIds 敏感词ID列表
     * @return 删除数量
     */
    @Override
    public int deleteSensitiveWordBatch(List<Long> wordIds) {
        return Math.toIntExact(removeByIds(wordIds) ? wordIds.size() : 0);
    }

    /**
     * 更新敏感词
     * 
     * @param sensitiveWord 敏感词信息
     * @return 是否成功
     */
    @Override
    public boolean updateSensitiveWord(ImSensitiveWord sensitiveWord) {
        return updateById(sensitiveWord);
    }

    /**
     * 查询启用的敏感词列表
     * 
     * @return 敏感词列表
     */
    @Override
    public List<ImSensitiveWord> selectEnabledWords() {
        return imSensitiveWordMapper.selectEnabledWords();
    }

    /**
     * 根据级别查询敏感词
     * 
     * @param level 敏感级别
     * @return 敏感词列表
     */
    @Override
    public List<ImSensitiveWord> selectWordsByLevel(String level) {
        return imSensitiveWordMapper.selectWordsByLevel(level);
    }

    /**
     * 根据分类查询敏感词
     * 
     * @param category 分类
     * @return 敏感词列表
     */
    @Override
    public List<ImSensitiveWord> selectWordsByCategory(String category) {
        return imSensitiveWordMapper.selectWordsByCategory(category);
    }

    /**
     * 搜索敏感词
     * 
     * @param keyword 搜索关键词
     * @return 敏感词列表
     */
    @Override
    public List<ImSensitiveWord> searchSensitiveWords(String keyword) {
        return imSensitiveWordMapper.searchSensitiveWords(keyword);
    }

    /**
     * 检查敏感词是否存在
     * 
     * @param word 敏感词
     * @return 是否存在
     */
    @Override
    public boolean existsSensitiveWord(String word) {
        return imSensitiveWordMapper.existsSensitiveWord(word);
    }

    /**
     * 批量更新敏感词启用状态
     * 
     * @param wordIds 敏感词ID列表
     * @param enabled 是否启用
     * @return 更新数量
     */
    @Override
    public int updateEnabledStatusBatch(List<Long> wordIds, boolean enabled) {
        return imSensitiveWordMapper.updateEnabledStatusBatch(wordIds, enabled ? "1" : "0");
    }

    /**
     * 统计各分类敏感词数量
     * 
     * @return 统计信息
     */
    @Override
    public Map<String, Integer> countWordsByCategory() {
        return imSensitiveWordMapper.countWordsByCategory();
    }

    /**
     * 统计各级别敏感词数量
     * 
     * @return 统计信息
     */
    @Override
    public Map<String, Integer> countWordsByLevel() {
        return imSensitiveWordMapper.countWordsByLevel();
    }

    /**
     * 查询所有敏感词分类
     * 
     * @return 分类列表
     */
    @Override
    public List<String> selectAllCategories() {
        return imSensitiveWordMapper.selectAllCategories();
    }

    /**
     * 导出敏感词数据
     * 
     * @param category 分类（可选）
     * @param level 级别（可选）
     * @param enabled 是否启用（可选）
     * @return 敏感词列表
     */
    @Override
    public List<ImSensitiveWord> exportSensitiveWords(String category, String level, Boolean enabled) {
        return imSensitiveWordMapper.exportSensitiveWords(category, level, enabled);
    }

    /**
     * 从文件导入敏感词
     * 
     * @param filePath 文件路径
     * @param category 分类
     * @param level 级别
     * @return 导入数量
     */
    @Override
    public int importSensitiveWordsFromFile(String filePath, String category, String level) {
        // 实现文件导入逻辑
        return 0;
    }

    /**
     * 重新构建敏感词检测器
     * 
     * @return 是否成功
     */
    @Override
    public boolean rebuildSensitiveWordDetector() {
        // 重新加载敏感词到内存
        return true;
    }

    /**
     * 获取敏感词检测统计信息
     * 
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getSensitiveWordStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", count(null));
        stats.put("enabled", selectEnabledWords().size());
        stats.put("categories", selectAllCategories().size());
        return stats;
    }

    /**
     * 验证敏感词格式
     * 
     * @param word 敏感词
     * @return 是否有效
     */
    @Override
    public boolean validateSensitiveWord(String word) {
        return word != null && !word.trim().isEmpty();
    }
}