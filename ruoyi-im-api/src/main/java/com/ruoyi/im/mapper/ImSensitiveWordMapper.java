package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImSensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感词Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImSensitiveWordMapper {

    /**
     * 查询所有启用的敏感词
     *
     * @return 敏感词列表
     */
    List<String> selectAllEnabledWords();

    /**
     * 查询敏感词列表
     *
     * @param sensitiveWord 敏感词
     * @return 敏感词集合
     */
    List<ImSensitiveWord> selectImSensitiveWordList(ImSensitiveWord sensitiveWord);

    /**
     * 查询敏感词
     *
     * @param id 敏感词ID
     * @return 敏感词
     */
    ImSensitiveWord selectImSensitiveWordById(Long id);

    /**
     * 新增敏感词
     *
     * @param sensitiveWord 敏感词
     * @return 结果
     */
    int insertImSensitiveWord(ImSensitiveWord sensitiveWord);

    /**
     * 修改敏感词
     *
     * @param sensitiveWord 敏感词
     * @return 结果
     */
    int updateImSensitiveWord(ImSensitiveWord sensitiveWord);

    /**
     * 删除敏感词
     *
     * @param id 敏感词ID
     * @return 结果
     */
    int deleteImSensitiveWordById(Long id);

    /**
     * 批量删除敏感词
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteImSensitiveWordByIds(Long[] ids);
}
