package com.ruoyi.im.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constant.ErrorCode;
import com.ruoyi.im.domain.ImEmailTemplate;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImEmailTemplateMapper;
import com.ruoyi.im.service.ImEmailTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;

/**
 * 邮件模板服务实现类
 *
 * @author ruoyi
 */
@Service
public class ImEmailTemplateServiceImpl implements ImEmailTemplateService {

    private static final Logger log = LoggerFactory.getLogger(ImEmailTemplateServiceImpl.class);

    /** 变量占位符正则表达式：${variableName} */
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    @Resource
    private ImEmailTemplateMapper templateMapper;

    @Override
    public List<ImEmailTemplate> getAllTemplates() {
        return templateMapper.selectEnabledTemplates();
    }

    @Override
    public ImEmailTemplate getTemplateByCode(String templateCode) {
        if (!StringUtils.hasText(templateCode)) {
            return null;
        }
        return templateMapper.selectByCode(templateCode);
    }

    @Override
    public List<ImEmailTemplate> getTemplatesByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return templateMapper.selectEnabledTemplates();
        }
        return templateMapper.selectByCategory(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(ImEmailTemplate template) {
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        template.setIsEnabled(1);
        template.setUsageCount(0);
        templateMapper.insert(template);

        log.info("创建邮件模板: id={}, code={}, name={}", template.getId(), template.getTemplateCode(), template.getTemplateName());
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(ImEmailTemplate template) {
        ImEmailTemplate existing = templateMapper.selectById(template.getId());
        if (existing == null) {
            throw BusinessException.dataNotFound();
        }

        template.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);

        log.info("更新邮件模板: id={}, code={}", template.getId(), template.getTemplateCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long templateId) {
        templateMapper.deleteById(templateId);
        log.info("删除邮件模板: id={}", templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setEnabled(Long templateId, Boolean enabled) {
        ImEmailTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw BusinessException.dataNotFound();
        }

        template.setIsEnabled(enabled ? 1 : 0);
        templateMapper.updateById(template);

        log.info("设置邮件模板状态: id={}, enabled={}", templateId, enabled);
    }

    @Override
    public String renderTemplate(String templateCode, Map<String, Object> variables) {
        ImEmailTemplate template = getTemplateByCode(templateCode);
        if (template == null) {
            throw BusinessException.dataNotFound();
        }

        // 增加使用次数
        templateMapper.incrementUsageCount(template.getId());

        // 渲染模板
        return renderVariables(template.getContent(), variables);
    }

    @Override
 public Map<String, String> previewTemplate(String templateCode) {
        ImEmailTemplate template = getTemplateByCode(templateCode);
        if (template == null) {
            throw BusinessException.dataNotFound();
        }

        // 获取变量说明
        List<Map<String, Object>> varList = getTemplateVariables(templateCode);
        Map<String, Object> sampleVars = new HashMap<>();
        for (Map<String, Object> varInfo : varList) {
            String varName = (String) varInfo.get("name");
            Object defaultValue = varInfo.get("defaultValue");
            sampleVars.put(varName, defaultValue != null ? defaultValue : "");
        }

        Map<String, String> result = new HashMap<>();
        result.put("subject", template.getSubject());
        result.put("content", renderVariables(template.getContent(), sampleVars));
        result.put("templateCode", templateCode);
        result.put("templateName", template.getTemplateName());

        return result;
    }

    @Override
    public List<Map<String, Object>> getTemplateVariables(String templateCode) {
        ImEmailTemplate template = getTemplateByCode(templateCode);
        if (template == null) {
            return Collections.emptyList();
        }

        String variablesJson = template.getVariables();
        if (!StringUtils.hasText(variablesJson)) {
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> result = new ArrayList<>();
            JSONArray array = JSON.parseArray(variablesJson);
            for (Object item : array) {
                if (item instanceof JSONObject) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) ((JSONObject) item).toJavaObject(Map.class);
                    result.add(map);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("解析模板变量失败: templateCode={}, error={}", templateCode, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyTemplate(Long templateId) {
        ImEmailTemplate original = templateMapper.selectById(templateId);
        if (original == null) {
            throw BusinessException.dataNotFound();
        }

        ImEmailTemplate copy = new ImEmailTemplate();
        copy.setTemplateName(original.getTemplateName() + " (副本)");
        copy.setTemplateCode(original.getTemplateCode() + "_copy_" + System.currentTimeMillis());
        copy.setSubject(original.getSubject());
        copy.setContent(original.getContent());
        copy.setTemplateType("USER");
        copy.setScope(original.getScope());
        copy.setVariables(original.getVariables());
        copy.setCategory(original.getCategory());
        copy.setIsEnabled(1);
        copy.setUsageCount(0);
        copy.setCreateTime(LocalDateTime.now());
        copy.setUpdateTime(LocalDateTime.now());

        templateMapper.insert(copy);

        log.info("复制邮件模板: originalId={}, newId={}", templateId, copy.getId());
        return copy.getId();
    }

    /**
     * 渲染变量替换
     *
     * @param content 模板内容
     * @param variables 变量值映射
     * @return 渲染后的内容
     */
    private String renderVariables(String content, Map<String, Object> variables) {
        if (content == null) {
            return "";
        }

        if (variables == null || variables.isEmpty()) {
            return content;
        }

        Matcher matcher = VAR_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = variables.get(varName);

            // 如果变量不存在，尝试从系统变量中获取
            if (value == null) {
                value = getSystemVariable(varName);
            }

            String replacement = value != null ? String.valueOf(value) : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        return result.toString();
    }

    /**
     * 获取系统变量
     *
     * @param varName 变量名
     * @return 变量值
     */
    private Object getSystemVariable(String varName) {
        // 支持的系统变量
        Map<String, Object> systemVars = new HashMap<>();
        systemVars.put("systemName", "RuoYi-IM");
        systemVars.put("currentDate", java.time.LocalDate.now().toString());
        systemVars.put("currentDateTime", java.time.LocalDateTime.now().toString());
        systemVars.put("systemTime", java.time.LocalTime.now().toString());
        systemVars.put("systemUrl", "http://localhost:8080");

        return systemVars.get(varName);
    }
}
