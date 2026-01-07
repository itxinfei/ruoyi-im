package com.ruoyi.web.service;

import com.ruoyi.im.domain.ImFileAsset;
import java.util.List;
import java.util.Map;

/**
 * IM文件资源Service接口（Admin模块专用）
 */
public interface ImFileAssetService {
    
    List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset);
    
    ImFileAsset selectImFileAssetById(Long id);
    
    int insertImFileAsset(ImFileAsset imFileAsset);
    
    int updateImFileAsset(ImFileAsset imFileAsset);
    
    int deleteImFileAssetById(Long id);
    
    int deleteImFileAssetByIds(Long[] ids);
    
    int countTotalFiles(ImFileAsset imFileAsset);
    
    int countTodayUploads(ImFileAsset imFileAsset);
    
    Long countTotalStorage();
    
    int cleanInvalidFiles();
    
    Map<String, Object> getFileStatistics();
}
