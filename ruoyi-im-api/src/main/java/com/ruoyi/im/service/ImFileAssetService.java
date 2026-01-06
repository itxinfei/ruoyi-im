package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImFileAsset;
import java.util.List;

/**
 * 鏂囦欢璧勬簮Service鎺ュ彛
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
     * 淇敼鏂囦欢璧勬簮锛堜笌update鏂规硶鐩稿悓锛岀敤浜庡吋瀹规€э級
     * 
     * @param imFileAsset 鏂囦欢璧勬簮
     * @return 缁撴灉
     */
    default int updateImFileAsset(ImFileAsset imFileAsset) {
        return update(imFileAsset);
    }
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
}
