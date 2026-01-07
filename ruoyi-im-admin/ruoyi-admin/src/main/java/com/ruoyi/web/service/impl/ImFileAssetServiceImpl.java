package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImFileAsset;
import com.ruoyi.web.mapper.ImFileAssetMapper;
import com.ruoyi.web.service.ImFileAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * IM文件资源Service实现（Admin模块专用）
 */
@Service
public class ImFileAssetServiceImpl implements ImFileAssetService {

    @Autowired
    private ImFileAssetMapper fileAssetMapper;

    @Override
    public List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset) {
        return fileAssetMapper.selectImFileAssetList(imFileAsset);
    }

    @Override
    public ImFileAsset selectImFileAssetById(Long id) {
        return fileAssetMapper.selectImFileAssetById(id);
    }

    @Override
    public int insertImFileAsset(ImFileAsset imFileAsset) {
        return fileAssetMapper.insertImFileAsset(imFileAsset);
    }

    @Override
    public int updateImFileAsset(ImFileAsset imFileAsset) {
        return fileAssetMapper.updateImFileAsset(imFileAsset);
    }

    @Override
    public int deleteImFileAssetById(Long id) {
        return fileAssetMapper.deleteImFileAssetById(id);
    }

    @Override
    public int deleteImFileAssetByIds(Long[] ids) {
        return fileAssetMapper.deleteImFileAssetByIds(ids);
    }

    @Override
    public int countTotalFiles(ImFileAsset imFileAsset) {
        return fileAssetMapper.countTotalFiles(imFileAsset);
    }

    @Override
    public int countTodayUploads(ImFileAsset imFileAsset) {
        return fileAssetMapper.countTodayUploads(imFileAsset);
    }

    @Override
    public Long countTotalStorage() {
        return fileAssetMapper.countTotalStorage();
    }

    @Override
    public int cleanInvalidFiles() {
        return fileAssetMapper.cleanInvalidFiles();
    }

    @Override
    public Map<String, Object> getFileStatistics() {
        return fileAssetMapper.getFileStatistics();
    }
}
