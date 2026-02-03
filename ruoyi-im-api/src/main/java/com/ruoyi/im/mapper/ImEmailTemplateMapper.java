package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImEmailTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 邮件模板Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImEmailTemplateMapper extends BaseMapper<ImEmailTemplate> {

    /**
     * 查询所有启用的模板
     *
     * @return 模板列表
     */
    @Select("SELECT * FROM im_email_template WHERE is_enabled = 1 ORDER BY category ASC, usage_count DESC")
    List<ImEmailTemplate> selectEnabledTemplates();

    /**
     * 根据模板编码查询
     *
     * @param templateCode 模板编码
     * @return 模板
     */
    @Select("SELECT * FROM im_email_template WHERE template_code = #{templateCode} AND is_enabled = 1 LIMIT 1")
    ImEmailTemplate selectByCode(@Param("templateCode") String templateCode);

    /**
     * 按分类查询模板
     *
     * @param category 分类
     * @return 模板列表
     */
    @Select("SELECT * FROM im_email_template WHERE category = #{category} AND is_enabled = 1 ORDER BY usage_count DESC")
    List<ImEmailTemplate> selectByCategory(@Param("category") String category);

    /**
     * 增加使用次数
     *
     * @param templateId 模板ID
     */
    @Select("UPDATE im_email_template SET usage_count = usage_count + 1 WHERE id = #{templateId}")
    void incrementUsageCount(@Param("templateId") Long templateId);
}
