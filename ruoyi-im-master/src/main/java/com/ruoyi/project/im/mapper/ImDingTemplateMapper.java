package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImDingTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * DING模板Mapper接口
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Mapper
public interface ImDingTemplateMapper {

    /**
     * 查询DING模板列表
     *
     * @param imDingTemplate DING模板
     * @return DING模板集合
     */
    List<ImDingTemplate> selectImDingTemplateList(ImDingTemplate imDingTemplate);

    /**
     * 根据ID获取DING模板
     *
     * @param id DING模板主键
     * @return DING模板
     */
    ImDingTemplate selectImDingTemplateById(Long id);

    /**
     * 新增DING模板
     *
     * @param imDingTemplate DING模板
     * @return 结果
     */
    int insertImDingTemplate(ImDingTemplate imDingTemplate);

    /**
     * 修改DING模板
     *
     * @param imDingTemplate DING模板
     * @return 结果
     */
    int updateImDingTemplate(ImDingTemplate imDingTemplate);

    /**
     * 删除DING模板
     *
     * @param ids DING模板主键集合
     * @return 结果
     */
    int deleteImDingTemplateByIds(Long[] ids);

    /**
     * 校验模板名称是否唯一
     *
     * @param name 模板名称
     * @return 结果
     */
    ImDingTemplate checkTemplateNameUnique(String name);
}
