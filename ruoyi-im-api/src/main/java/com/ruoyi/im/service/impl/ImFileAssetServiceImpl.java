package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.service.ImFileAssetService;

/**
 * 文件资源Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImFileAssetServiceImpl implements ImFileAssetService {
    @Autowired
    private ImFileAssetMapper imFileAssetMapper;

    /**
     * 查询文件资源
     * 
     * @param id 文件资源ID
     * @return 文件资源
     */
    @Override
    public ImFileAsset selectImFileAssetById(Long id) {
        return imFileAssetMapper.selectImFileAssetById(id);
    }

    /**
     * 查询文件资源列表
     * 
     * @param imFileAsset 文件资源
     * @return 文件资源
     */
    @Override
    public List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset) {
        return imFileAssetMapper.selectImFileAssetList(imFileAsset);
    }

    /**
     * 新增文件资源
     * 
     * @param imFileAsset 文件资源
     * @return 结果
     */
    @Override
    public int insertImFileAsset(ImFileAsset imFileAsset) {
        return imFileAssetMapper.insertImFileAsset(imFileAsset);
    }

    /**
     * 修改文件资源
     * 
     * @param imFileAsset 文件资源
     * @return 结果
     */
    @Override
    public int updateImFileAsset(ImFileAsset imFileAsset) {
        return imFileAssetMapper.updateImFileAsset(imFileAsset);
    }

    /**
     * 批量删除文件资源
     * 
     * @param ids 需要删除的文件资源ID
     * @return 结果
     */
    @Override
    public int deleteImFileAssetByIds(Long[] ids) {
        return imFileAssetMapper.deleteImFileAssetByIds(ids);
    }

    /**
     * 删除文件资源信息
     * 
     * @param id 文件资源ID
     * @return 结果
     */
    @Override
    public int deleteImFileAssetById(Long id) {
        return imFileAssetMapper.deleteImFileAssetById(id);
    }
}