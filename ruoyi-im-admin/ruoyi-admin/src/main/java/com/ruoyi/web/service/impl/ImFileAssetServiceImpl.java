package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.service.IImFileAssetService;
import com.ruoyi.system.domain.ImFileAsset;

/**
 * 文件资源Service业务层处理
 * 
 * @author ruoyi
 */
@Service("adminImFileAssetServiceImpl")
public class ImFileAssetServiceImpl implements IImFileAssetService
{
    @Autowired
    private com.ruoyi.system.service.IImFileAssetService systemFileAssetService;

    /**
     * 查询文件资源
     * 
     * @param id 文件资源ID
     * @return 文件资源
     */
    @Override
    public ImFileAsset selectImFileAssetById(Long id)
    {
        return systemFileAssetService.selectImFileAssetById(id);
    }

    /**
     * 查询文件资源列表
     * 
     * @param imFileAsset 文件资源
     * @return 文件资源
     */
    @Override
    public List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset)
    {
        return systemFileAssetService.selectImFileAssetList(imFileAsset);
    }

    /**
     * 新增文件资源
     * 
     * @param imFileAsset 文件资源
     * @return 结果
     */
    @Override
    public int insertImFileAsset(ImFileAsset imFileAsset)
    {
        return systemFileAssetService.insertImFileAsset(imFileAsset);
    }

    /**
     * 修改文件资源
     * 
     * @param imFileAsset 文件资源
     * @return 结果
     */
    @Override
    public int updateImFileAsset(ImFileAsset imFileAsset)
    {
        return systemFileAssetService.updateImFileAsset(imFileAsset);
    }

    /**
     * 批量删除文件资源
     * 
     * @param ids 需要删除的文件资源ID
     * @return 结果
     */
    @Override
    public int deleteImFileAssetByIds(Long[] ids)
    {
        return systemFileAssetService.deleteImFileAssetByIds(ids);
    }

    /**
     * 删除文件资源信息
     * 
     * @param id 文件资源ID
     * @return 结果
     */
    @Override
    public int deleteImFileAssetById(Long id)
    {
        return systemFileAssetService.deleteImFileAssetById(id);
    }
}