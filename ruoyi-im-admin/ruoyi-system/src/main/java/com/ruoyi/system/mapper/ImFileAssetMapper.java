package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ImFileAsset;

/**
 * 文件资源Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface ImFileAssetMapper 
{
    /**
     * 查询文件资源
     * 
     * @param id 文件资源ID
     * @return 文件资源
     */
    public ImFileAsset selectImFileAssetById(Long id);

    /**
     * 查询文件资源列表
     * 
     * @param imFileAsset 文件资源
     * @return 文件资源集合
     */
    public List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset);

    /**
     * 新增文件资源
     * 
     * @param imFileAsset 文件资源
     * @return 结果
     */
    public int insertImFileAsset(ImFileAsset imFileAsset);

    /**
     * 修改文件资源
     * 
     * @param imFileAsset 文件资源
     * @return 结果
     */
    public int updateImFileAsset(ImFileAsset imFileAsset);

    /**
     * 删除文件资源
     * 
     * @param id 文件资源ID
     * @return 结果
     */
    public int deleteImFileAssetById(Long id);

    /**
     * 批量删除文件资源
     * 
     * @param ids 需要删除的文件资源ID
     * @return 结果
     */
    public int deleteImFileAssetByIds(Long[] ids);
}
