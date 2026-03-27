package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImUserLayout;
import com.ruoyi.im.mapper.ImUserLayoutMapper;
import com.ruoyi.im.service.ImUserLayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户布局配置服务实现
 *
 * @author ruoyi
 */
@Service
public class ImUserLayoutServiceImpl implements ImUserLayoutService {

    @Autowired
    private ImUserLayoutMapper userLayoutMapper;

    @Override
    public String getLayoutConfig(Long userId, String layoutType) {
        ImUserLayout layout = userLayoutMapper.selectByUserIdAndType(userId, layoutType);
        return layout != null ? layout.getLayoutConfig() : null;
    }

    @Override
    public void saveLayoutConfig(Long userId, String layoutType, String layoutConfig) {
        ImUserLayout layout = new ImUserLayout();
        layout.setUserId(userId);
        layout.setLayoutType(layoutType);
        layout.setLayoutConfig(layoutConfig);
        userLayoutMapper.insertOrUpdate(layout);
    }

    @Override
    public void deleteLayoutConfig(Long userId, String layoutType) {
        userLayoutMapper.deleteByUserIdAndType(userId, layoutType);
    }
}
