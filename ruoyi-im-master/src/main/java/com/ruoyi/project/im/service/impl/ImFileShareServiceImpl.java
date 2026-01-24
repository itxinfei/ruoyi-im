package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImFileShare;
import com.ruoyi.web.mapper.ImFileShareMapper;
import com.ruoyi.web.service.ImFileShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 文件分享Service实现
 *
 * @author ruoyi
 */
@Service
public class ImFileShareServiceImpl implements ImFileShareService {

    @Autowired
    private ImFileShareMapper imFileShareMapper;

    @Override
    public ImFileShare selectImFileShareById(Long id) {
        return imFileShareMapper.selectImFileShareById(id);
    }

    @Override
    public List<ImFileShare> selectImFileShareList(ImFileShare imFileShare) {
        return imFileShareMapper.selectImFileShareList(imFileShare);
    }

    @Override
    public List<ImFileShare> selectSharesBySharerId(Long sharerId) {
        return imFileShareMapper.selectSharesBySharerId(sharerId);
    }

    @Override
    public int insertImFileShare(ImFileShare imFileShare) {
        return imFileShareMapper.insertImFileShare(imFileShare);
    }

    @Override
    public int updateImFileShare(ImFileShare imFileShare) {
        return imFileShareMapper.updateImFileShare(imFileShare);
    }

    @Override
    public int deleteImFileShareByIds(Long[] ids) {
        return imFileShareMapper.deleteImFileShareByIds(ids);
    }

    @Override
    public int deleteImFileShareById(Long id) {
        return imFileShareMapper.deleteImFileShareById(id);
    }

    @Override
    public int incrementAccessCount(Long id) {
        return imFileShareMapper.incrementAccessCount(id);
    }

    @Override
    public int incrementDownloadCount(Long id) {
        return imFileShareMapper.incrementDownloadCount(id);
    }

    @Override
    public Map<String, Object> getFileShareStatistics() {
        return imFileShareMapper.getFileShareStatistics();
    }

    @Override
    public List<ImFileShare> selectExpiredShares() {
        return imFileShareMapper.selectExpiredShares();
    }

    @Override
    public int cleanExpiredShares() {
        return imFileShareMapper.cleanExpiredShares();
    }
}
