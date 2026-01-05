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
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
}