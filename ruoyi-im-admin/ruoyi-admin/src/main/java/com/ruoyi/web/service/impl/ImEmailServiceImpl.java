package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImEmail;
import com.ruoyi.web.mapper.ImEmailMapper;
import com.ruoyi.web.service.ImEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 邮件Service实现
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Service
public class ImEmailServiceImpl implements ImEmailService {

    @Autowired
    private ImEmailMapper emailMapper;

    @Override
    public List<ImEmail> selectImEmailList(ImEmail imEmail) {
        return emailMapper.selectImEmailList(imEmail);
    }

    @Override
    public ImEmail selectImEmailById(Long id) {
        return emailMapper.selectImEmailById(id);
    }

    @Override
    public int insertImEmail(ImEmail imEmail) {
        return emailMapper.insertImEmail(imEmail);
    }

    @Override
    public int updateImEmail(ImEmail imEmail) {
        return emailMapper.updateImEmail(imEmail);
    }

    @Override
    public int deleteImEmailByIds(Long[] ids) {
        return emailMapper.deleteImEmailByIds(ids);
    }

    @Override
    public Map<String, Object> getEmailStatistics() {
        return emailMapper.getEmailStatistics();
    }
}
