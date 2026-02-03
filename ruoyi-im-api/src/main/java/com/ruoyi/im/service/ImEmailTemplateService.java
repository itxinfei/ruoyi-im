package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImEmailTemplate;

import java.util.List;
import java.util.Map;

/**
 * 邮件模板服务接口
 *
 * @author ruoyi
 */
public interface ImEmailTemplateService {

    /**
     * 获取所有启用的模板
     *
     * @return 模板列表
     */
    List<ImEmailTemplate> getAllTemplates();

    /**
     * 根据模板编码获取模板
     *
     * @param templateCode 模板编码
     * @return 模板信息
     */
    ImEmailTemplate getTemplateByCode(String templateCode);

    /**
     * 按分类获取模板
     *
     * @param category 分类
     * @return 模板列表
     */
    List<ImEmailTemplate> getTemplatesByCategory(String category);

    /**
     * 创建模板
     *
     * @param template 模板信息
     * @return 模板ID
     */
    Long createTemplate(ImEmailTemplate template);

    /**
     * 更新模板
     *
     * @param template 模板信息
     */
    void updateTemplate(ImEmailTemplate template);

    /**
     * 删除模板
     *
     * @param templateId 模板ID
     */
    void deleteTemplate(Long templateId);

    /**
     * 启用/禁用模板
     *
     * @param templateId 模板ID
     * @param enabled 是否启用
     */
    void setEnabled(Long templateId, Boolean enabled);

    /**
     * 渲染模板内容
     * 使用提供的变量替换模板中的占位符
     *
     * @param templateCode 模板编码
     * @param variables 变量值映射
     * @return 渲染后的内容
     */
    String renderTemplate(String templateCode, Map<String, Object> variables);

    /**
     * 预览模板
     * 预览模板渲染效果，使用示例变量
     *
     * @param templateCode 模板编码
     * @return 预览结果，包含主题和渲染内容
     */
    Map<String, String> previewTemplate(String templateCode);

    /**
     * 获取模板变量说明
     *
     * @param templateCode 模板编码
     * @return 变量说明列表
     */
    List<Map<String, Object>> getTemplateVariables(String templateCode);

    /**
     * 复制模板
     *
     * @param templateId 原模板ID
     * @return 新模板ID
     */
    Long copyTemplate(Long templateId);
}
