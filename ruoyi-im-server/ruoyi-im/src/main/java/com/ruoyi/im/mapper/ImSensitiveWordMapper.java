package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImSensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 敏感词Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Mapper
public interface ImSensitiveWordMapper extends BaseMapper<ImSensitiveWord> {

    /**
     * 查询启用的敏感词列表
     * 
     * @return 敏感词列表
     */
    List<ImSensitiveWord> selectEnabledWords();

    /**
     * 根据敏感级别查询敏感词列表
     * 
     * @param level 敏感级别
     * @return 敏感词列表
     */
    List<ImSensitiveWord> selectWordsByLevel(@Param("level") String level);

    /**
     * 根据分类查询敏感词列表
     * 
     * @param category 分类
     * @return 敏感词列表
     */
    List<ImSensitiveWord> selectWordsByCategory(@Param("category") String category);

    /**
     * 搜索敏感词（根据词语内容）
     * 
     * @param keyword 搜索关键词
     * @return 敏感词列表
     */
    List<ImSensitiveWord> searchSensitiveWords(@Param("keyword") String keyword);

    /**
     * 检查敏感词是否存在
     * 
     * @param word 敏感词
     * @return 是否存在
     */
    boolean existsSensitiveWord(@Param("word") String word);

    /**
     * 批量插入敏感词
     * 
     * @param words 敏感词列表
     * @return 插入数量
     */
    int insertBatch(@Param("words") List<ImSensitiveWord> words);

    /**
     * 批量更新敏感词状态
     * 
     * @param wordIds 敏感词ID列表
     * @param enabled 是否启用
     * @return 更新数量
     */
    int updateEnabledStatusBatch(@Param("wordIds") List<Long> wordIds, @Param("enabled") String enabled);

    /**
     * 批量删除敏感词
     * 
     * @param wordIds 敏感词ID列表
     * @return 删除数量
     */
    int deleteBatchByIds(@Param("wordIds") List<Long> wordIds);

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
     * 查询所有分类列表
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
    List<ImSensitiveWord> exportSensitiveWords(@Param("category") String category, @Param("level") String level, @Param("enabled") Boolean enabled);
}