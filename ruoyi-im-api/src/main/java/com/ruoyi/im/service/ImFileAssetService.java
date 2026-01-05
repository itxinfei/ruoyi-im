package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImFileAsset;
import java.util.List;

/**
 * 文件资源Service接口
 * 
 * @author ruoyi
 */
public interface ImFileAssetService extends BaseService<ImFileAsset> {
    
    @Override
    ImFileAsset selectById(Long id);
    
    @Override
    List<ImFileAsset> selectList(ImFileAsset imFileAsset);
    
    @Override
    int insert(ImFileAsset imFileAsset);
    
    @Override
    int update(ImFileAsset imFileAsset);
    
    /**
     * 修改文件资源（与update方法相同，用于兼容性）
     * 
     * @param imFileAsset 文件资源
     * @return 结果
     */
    default int updateImFileAsset(ImFileAsset imFileAsset) {
        return update(imFileAsset);
    }
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
}