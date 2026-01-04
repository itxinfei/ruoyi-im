package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.ImUser;
import com.ruoyi.system.exception.ImException;
import com.ruoyi.system.mapper.ImUserMapper;
import com.ruoyi.system.service.IImUserService;

/**
 * IM用户Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
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
        if (id == null || id <= 0)
        {
            throw new ImException("用户ID不能为空或小于0");
        }
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
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertImUser(ImUser imUser)
    {
        // 验证用户名是否已存在
        ImUser existUser = imUserMapper.selectImUserByUsername(imUser.getUsername());
        if (existUser != null)
        {
            throw new ImException("用户名已存在");
        }

        // 验证邮箱是否已存在
        if (imUser.getEmail() != null && !imUser.getEmail().isEmpty())
        {
            ImUser emailUser = imUserMapper.selectImUserByEmail(imUser.getEmail());
            if (emailUser != null)
            {
                throw new ImException("邮箱已被使用");
            }
        }

        return imUserMapper.insertImUser(imUser);
    }

    /**
     * 修改IM用户
     * 
     * @param imUser IM用户
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateImUser(ImUser imUser)
    {
        if (imUser.getId() == null || imUser.getId() <= 0)
        {
            throw new ImException("用户ID不能为空");
        }

        // 验证用户是否存在
        ImUser existUser = imUserMapper.selectImUserById(imUser.getId());
        if (existUser == null)
        {
            throw new ImException("用户不存在");
        }

        return imUserMapper.updateImUser(imUser);
    }

    /**
     * 批量删除IM用户
     * 
     * @param ids 需要删除的IM用户ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteImUserByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            throw new ImException("删除的用户ID不能为空");
        }
        return imUserMapper.deleteImUserByIds(ids);
    }

    /**
     * 删除IM用户信息
     * 
     * @param id IM用户ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteImUserById(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new ImException("用户ID不能为空");
        }
        return imUserMapper.deleteImUserById(id);
    }
}