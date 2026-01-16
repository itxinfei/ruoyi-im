package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImSensitiveWord;
import java.util.List;
import java.util.Map;

/**
 * 敏感词Mapper接口
 *
 * @author ruoyi
 */
public interface ImSensitiveWordMapper {

    /**
     * 查询敏感词
     *
     * @param id 敏感词主键
     * @return 敏感词
     */
    ImSensitiveWord selectImSensitiveWordById(Long id);

    /**
     * 根据词语查询敏感词
     *
     * @param word 词语
     * @return 敏感词
     */
    ImSensitiveWord selectImSensitiveWordByWord(String word);

    /**
     * 查询敏感词列表
     *
     * @param imSensitiveWord 敏感词
     * @return 敏感词集合
     */
    List<ImSensitiveWord> selectImSensitiveWordList(ImSensitiveWord imSensitiveWord);

    /**
     * 查询所有启用的敏感词
     *
     * @return 敏感词集合
     */
    List<ImSensitiveWord> selectEnabledWords();

    /**
     * 新增敏感词
     *
     * @param imSensitiveWord 敏感词
     * @return 结果
     */
    int insertImSensitiveWord(ImSensitiveWord imSensitiveWord);

    /**
     * 修改敏感词
     *
     * @param imSensitiveWord 敏感词
     * @return 结果
     */
    int updateImSensitiveWord(ImSensitiveWord imSensitiveWord);

    /**
     * 删除敏感词
     *
     * @param id 敏感词主键
     * @return 结果
     */
    int deleteImSensitiveWordById(Long id);

    /**
     * 批量删除敏感词
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImSensitiveWordByIds(Long[] ids);

    /**
     * 批量导入敏感词
     *
     * @param words 词语列表
     * @return 结果
     */
    int batchInsertWords(List<String> words);

    /**
     * 获取敏感词统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getSensitiveWordStatistics();
}
