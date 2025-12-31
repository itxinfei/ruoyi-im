package com.ruoyi.im.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.service.ImGroupService;

/**
 * 群组Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {
    @Autowired
    private ImGroupMapper imGroupMapper;

    /**
     * 查询群组
     * 
     * @param id 群组ID
     * @return 群组
     */
    @Override
    public ImGroup selectImGroupById(Long id) {
        return imGroupMapper.selectImGroupById(id);
    }

    /**
     * 查询群组列表
     * 
     * @param imGroup 群组
     * @return 群组
     */
    @Override
    public List<ImGroup> selectImGroupList(ImGroup imGroup) {
        return imGroupMapper.selectImGroupList(imGroup);
    }

    /**
     * 新增群组
     * 
     * @param imGroup 群组
     * @return 结果
     */
    @Override
    public int insertImGroup(ImGroup imGroup) {
        return imGroupMapper.insertImGroup(imGroup);
    }

    /**
     * 修改群组
     * 
     * @param imGroup 群组
     * @return 结果
     */
    @Override
    public int updateImGroup(ImGroup imGroup) {
        return imGroupMapper.updateImGroup(imGroup);
    }

    /**
     * 批量删除群组
     * 
     * @param ids 需要删除的群组ID
     * @return 结果
     */
    @Override
    public int deleteImGroupByIds(Long[] ids) {
        return imGroupMapper.deleteImGroupByIds(ids);
    }

    /**
     * 删除群组信息
     * 
     * @param id 群组ID
     * @return 结果
     */
    @Override
    public int deleteImGroupById(Long id) {
        return imGroupMapper.deleteImGroupById(id);
    }
    
    /**
     * 根据用户ID查询群组列表
     * 
     * @param userId 用户ID
     * @return 群组集合
     */
    @Override
    public List<ImGroup> selectImGroupListByUserId(Long userId) {
        // TODO: 实现根据用户ID查询群组列表的逻辑
        // 这里需要关联查询群组成员表来获取用户所在的群组
        return imGroupMapper.selectImGroupList(new ImGroup());
    }
}