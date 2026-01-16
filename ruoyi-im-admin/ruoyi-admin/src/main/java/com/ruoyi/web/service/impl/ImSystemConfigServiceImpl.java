package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImSystemConfig;
import com.ruoyi.web.mapper.ImSystemConfigMapper;
import com.ruoyi.web.service.ImSystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 系统配置Service实现
 *
 * @author ruoyi
 */
@Service
public class ImSystemConfigServiceImpl implements ImSystemConfigService {

    @Autowired
    private ImSystemConfigMapper imSystemConfigMapper;

    @Override
    public ImSystemConfig selectImSystemConfigById(Long id) {
        return imSystemConfigMapper.selectImSystemConfigById(id);
    }

    @Override
    public ImSystemConfig selectImSystemConfigByKey(String configKey) {
        return imSystemConfigMapper.selectImSystemConfigByKey(configKey);
    }

    @Override
    public java.util.List<ImSystemConfig> selectImSystemConfigList(ImSystemConfig imSystemConfig) {
        return imSystemConfigMapper.selectImSystemConfigList(imSystemConfig);
    }

    @Override
    public int insertImSystemConfig(ImSystemConfig imSystemConfig) {
        return imSystemConfigMapper.insertImSystemConfig(imSystemConfig);
    }

    @Override
    public int updateImSystemConfig(ImSystemConfig imSystemConfig) {
        return imSystemConfigMapper.updateImSystemConfig(imSystemConfig);
    }

    @Override
    public int deleteImSystemConfigByIds(Long[] ids) {
        return imSystemConfigMapper.deleteImSystemConfigByIds(ids);
    }

    @Override
    public int deleteImSystemConfigById(Long id) {
        return imSystemConfigMapper.deleteImSystemConfigById(id);
    }

    @Override
    public int toggleStatus(Long id) {
        ImSystemConfig config = selectImSystemConfigById(id);
        if (config == null) {
            return 0;
        }
        config.setStatus("ACTIVE".equals(config.getStatus()) ? "DISABLED" : "ACTIVE");
        return imSystemConfigMapper.updateImSystemConfig(config);
    }

    @Override
    public Map<String, Object> getSystemConfigStatistics() {
        return imSystemConfigMapper.getSystemConfigStatistics();
    }
}
