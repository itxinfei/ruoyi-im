package com.ruoyi.web.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.domain.ImDingTemplate;
import com.ruoyi.web.mapper.ImDingTemplateMapper;
import com.ruoyi.web.service.ImDingTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DING模板Service实现
 *
 * @author ruoyi
 * @date 2025-01-18
 */
@Service
public class ImDingTemplateServiceImpl implements ImDingTemplateService {

    @Autowired
    private ImDingTemplateMapper dingTemplateMapper;

    /**
     * 查询DING模板列表
     *
     * @param imDingTemplate DING模板
     * @return DING模板
     */
    @Override
    public List<ImDingTemplate> selectImDingTemplateList(ImDingTemplate imDingTemplate) {
        return dingTemplateMapper.selectImDingTemplateList(imDingTemplate);
    }

    /**
     * 根据ID获取DING模板
     *
     * @param id DING模板主键
     * @return DING模板
     */
    @Override
    public ImDingTemplate selectImDingTemplateById(Long id) {
        return dingTemplateMapper.selectImDingTemplateById(id);
    }

    /**
     * 新增DING模板
     *
     * @param imDingTemplate DING模板
     * @return 结果
     */
    @Override
    public int insertImDingTemplate(ImDingTemplate imDingTemplate) {
        return dingTemplateMapper.insertImDingTemplate(imDingTemplate);
    }

    /**
     * 修改DING模板
     *
     * @param imDingTemplate DING模板
     * @return 结果
     */
    @Override
    public int updateImDingTemplate(ImDingTemplate imDingTemplate) {
        return dingTemplateMapper.updateImDingTemplate(imDingTemplate);
    }

    /**
     * 批量删除DING模板
     *
     * @param ids DING模板主键集合
     * @return 结果
     */
    @Override
    public int deleteImDingTemplateByIds(Long[] ids) {
        return dingTemplateMapper.deleteImDingTemplateByIds(ids);
    }

    /**
     * 校验模板名称是否唯一
     *
     * @param imDingTemplate DING模板信息
     * @return 结果
     */
    @Override
    public String checkTemplateNameUnique(ImDingTemplate imDingTemplate) {
        Long templateId = StringUtils.isNull(imDingTemplate.getId()) ? -1L : imDingTemplate.getId();
        ImDingTemplate info = dingTemplateMapper.checkTemplateNameUnique(imDingTemplate.getName());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != templateId.longValue()) {
            return "1";
        }
        return "0";
    }
}
