package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.mapper.base.BaseMapper;
import java.util.List;

/**
 * 鏂囦欢璧勬簮Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImFileAssetMapper extends BaseMapper<ImFileAsset> {
    /**
     * 鏌ヨ鏂囦欢璧勬簮
     * 
     * @param id 鏂囦欢璧勬簮ID
     * @return 鏂囦欢璧勬簮
     */
    public ImFileAsset selectImFileAssetById(Long id);

    /**
     * 鏌ヨ鏂囦欢璧勬簮鍒楄〃
     * 
     * @param imFileAsset 鏂囦欢璧勬簮
     * @return 鏂囦欢璧勬簮闆嗗悎
     */
    public List<ImFileAsset> selectImFileAssetList(ImFileAsset imFileAsset);

    /**
     * 鏂板鏂囦欢璧勬簮
     * 
     * @param imFileAsset 鏂囦欢璧勬簮
     * @return 缁撴灉
     */
    public int insertImFileAsset(ImFileAsset imFileAsset);

    /**
     * 淇敼鏂囦欢璧勬簮
     * 
     * @param imFileAsset 鏂囦欢璧勬簮
     * @return 缁撴灉
     */
    public int updateImFileAsset(ImFileAsset imFileAsset);

    /**
     * 鍒犻櫎鏂囦欢璧勬簮
     * 
     * @param id 鏂囦欢璧勬簮ID
     * @return 缁撴灉
     */
    public int deleteImFileAssetById(Long id);

    /**
     * 鎵归噺鍒犻櫎鏂囦欢璧勬簮
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImFileAssetByIds(Long[] ids);
}
