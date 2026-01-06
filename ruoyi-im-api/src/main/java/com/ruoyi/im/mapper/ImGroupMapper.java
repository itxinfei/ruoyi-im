package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImGroup;
import java.util.List;

/**
 * 缇ょ粍Mapper鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImGroupMapper {
    /**
     * 鏌ヨ缇ょ粍
     * 
     * @param id 缇ょ粍ID
     * @return 缇ょ粍
     */
    public ImGroup selectImGroupById(Long id);

    /**
     * 鏌ヨ缇ょ粍鍒楄〃
     * 
     * @param imGroup 缇ょ粍
     * @return 缇ょ粍闆嗗悎
     */
    public List<ImGroup> selectImGroupList(ImGroup imGroup);

    /**
     * 鏂板缇ょ粍
     * 
     * @param imGroup 缇ょ粍
     * @return 缁撴灉
     */
    public int insertImGroup(ImGroup imGroup);

    /**
     * 淇敼缇ょ粍
     * 
     * @param imGroup 缇ょ粍
     * @return 缁撴灉
     */
    public int updateImGroup(ImGroup imGroup);

    /**
     * 鍒犻櫎缇ょ粍
     * 
     * @param id 缇ょ粍ID
     * @return 缁撴灉
     */
    public int deleteImGroupById(Long id);

    /**
     * 鎵归噺鍒犻櫎缇ょ粍
     * 
     * @param ids 闇€瑕佸垹闄ょ殑鏁版嵁ID
     * @return 缁撴灉
     */
    public int deleteImGroupByIds(Long[] ids);
}
