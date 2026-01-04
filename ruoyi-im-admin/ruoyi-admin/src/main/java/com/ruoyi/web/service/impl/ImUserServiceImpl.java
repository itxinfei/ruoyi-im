package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.service.IImUserService;
import com.ruoyi.system.domain.ImUser;
import com.ruoyi.system.mapper.ImUserMapper;

/**
 * IM用户Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ImUserServiceImpl implements IImUserService
{
    @Autowired
    private ImUserMapper imUserMapper;

    /**
     * 查询IM用户
     * 
     * @param id IM用户ID
     * @return IM用户
     */
    @Override
    public ImUser selectImUserById(Long id)
    {
        return imUserMapper.selectImUserById(id);
    }

    /**
     * 查询IM用户列表
     * 
     * @param imUser IM用户
     * @return IM用户
     */
    @Override
    public List<ImUser> selectImUserList(ImUser imUser)
    {
        return imUserMapper.selectImUserList(imUser);
    }

    /**
     * 新增IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    @Override
    public int insertImUser(ImUser imUser)
    {
        return imUserMapper.insertImUser(imUser);
    }

    /**
     * 修改IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    @Override
    public int updateImUser(ImUser imUser)
    {
        return imUserMapper.updateImUser(imUser);
    }

    /**
     * 批量删除IM用户
     * 
     * @param ids 需要删除的IM用户ID
     * @return 结果
     */
    @Override
    public int deleteImUserByIds(Long[] ids)
    {
        return imUserMapper.deleteImUserByIds(ids);
    }

    /**
     * 删除IM用户信息
     * 
     * @param id IM用户ID
     * @return 结果
     */
    @Override
    public int deleteImUserById(Long id)
    {
        return imUserMapper.deleteImUserById(id);
    }
}