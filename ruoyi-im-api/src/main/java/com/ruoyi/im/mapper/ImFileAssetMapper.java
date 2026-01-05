package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.mapper.base.BaseMapper;
import java.util.List;

/**
 * 文件资源Mapper接口
 * 
 * @author ruoyi
 */
public interface ImFileAssetMapper extends BaseMapper<ImFileAsset> {
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
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteImFileAssetByIds(Long[] ids);
}