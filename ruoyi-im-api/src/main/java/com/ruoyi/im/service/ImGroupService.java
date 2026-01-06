package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImGroup;
import java.util.List;

/**
 * 缇ょ粍Service鎺ュ彛
 * 
 * @author ruoyi
 */
public interface ImGroupService extends BaseService<ImGroup> {
    
    @Override
    ImGroup selectById(Long id);
    
    @Override
    List<ImGroup> selectList(ImGroup imGroup);
    
    @Override
    int insert(ImGroup imGroup);
    
    @Override
    int update(ImGroup imGroup);
    
    @Override
    int deleteByIds(Long[] ids);
    
    @Override
    int deleteById(Long id);
    
    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ缇ょ粍鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 缇ょ粍闆嗗悎
     */
    public List<ImGroup> selectImGroupListByUserId(Long userId);
    
    /**
     * 鏍规嵁缇ょ粍ID鏌ヨ缇ょ粍淇℃伅
     * 
     * @param id 缇ょ粍ID
     * @return 缇ょ粍淇℃伅
     */
    public ImGroup selectImGroupById(Long id);
    
    /**
     * 鏇存柊缇ょ粍淇℃伅
     * 
     * @param imGroup 缇ょ粍淇℃伅
     * @return 缁撴灉
     */
    public int updateImGroup(ImGroup imGroup);
    
    /**
     * 鏍规嵁ID鍒犻櫎缇ょ粍淇℃伅
     * 
     * @param id 缇ょ粍ID
     * @return 缁撴灉
     */
    public int deleteImGroupById(Long id);
    
    /**
     * 鏌ヨ缇ょ粍鍒楄〃
     * 
     * @param request 鏌ヨ璇锋眰
     * @return 缇ょ粍闆嗗悎
     */
    default List<ImGroup> selectImGroupList(com.ruoyi.im.dto.group.ImGroupQueryRequest request) {
        return selectList(new com.ruoyi.im.domain.ImGroup());
    }
}
