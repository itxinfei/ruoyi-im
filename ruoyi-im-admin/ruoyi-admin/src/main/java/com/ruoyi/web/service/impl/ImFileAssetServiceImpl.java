package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImFileAsset;
import com.ruoyi.web.mapper.ImFileAssetMapper;
import com.ruoyi.web.service.ImFileAssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM文件资源Service实现（Admin模块专用）
 */
@Service
public class ImFileAssetServiceImpl implements ImFileAssetService {

    private static final Logger logger = LoggerFactory.getLogger(ImFileAssetServiceImpl.class);

    @Autowired
    private ImFileAssetMapper fileAssetMapper;

    @Override
    public List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset) {
        try {
            List<ImFileAsset> list = fileAssetMapper.selectImFileAssetList(imFileAsset);
            if (list == null) {
                return new ArrayList<>();
            }
            return list;
        } catch (Exception e) {
            logger.error("查询文件列表异常", e);
            return new ArrayList<>();
        }
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
        try {
            Map<String, Object> result = fileAssetMapper.getFileStatistics();
            if (result == null) {
                result = new HashMap<>();
                result.put("totalCount", 0);
                result.put("imageCount", 0);
                result.put("videoCount", 0);
                result.put("docCount", 0);
                result.put("audioCount", 0);
                result.put("otherCount", 0);
                result.put("totalStorage", 0L);
                result.put("todayUploads", 0);
            }
            return result;
        } catch (Exception e) {
            logger.error("获取文件统计异常", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("totalCount", 0);
            errorResult.put("imageCount", 0);
            errorResult.put("videoCount", 0);
            errorResult.put("docCount", 0);
            errorResult.put("audioCount", 0);
            errorResult.put("otherCount", 0);
            errorResult.put("totalStorage", 0L);
            errorResult.put("todayUploads", 0);
            return errorResult;
        }
    }
}
