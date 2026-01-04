package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ImSession;
import com.ruoyi.system.mapper.ImSessionMapper;
import com.ruoyi.system.service.IImSessionService;

/**
 * IM会话Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImSessionServiceImpl implements IImSessionService
{
    @Autowired
    private ImSessionMapper imSessionMapper;

    /**
     * 查询IM会话
     * 
     * @param id IM会话ID
     * @return IM会话
     */
    @Override
    public ImSession selectImSessionById(Long id)
    {
        return imSessionMapper.selectImSessionById(id);
    }

    /**
     * 查询IM会话列表
     * 
     * @param imSession IM会话
     * @return IM会话
     */
    @Override
    public List<ImSession> selectImSessionList(ImSession imSession)
    {
        return imSessionMapper.selectImSessionList(imSession);
    }

    /**
     * 新增IM会话
     * 
     * @param imSession IM会话
     * @return 结果
     */
    @Override
    public int insertImSession(ImSession imSession)
    {
        return imSessionMapper.insertImSession(imSession);
    }

    /**
     * 修改IM会话
     * 
     * @param imSession IM会话
     * @return 结果
     */
    @Override
    public int updateImSession(ImSession imSession)
    {
        return imSessionMapper.updateImSession(imSession);
    }

    /**
     * 批量删除IM会话
     * 
     * @param ids 需要删除的IM会话ID
     * @return 结果
     */
    @Override
    public int deleteImSessionByIds(Long[] ids)
    {
        return imSessionMapper.deleteImSessionByIds(ids);
    }

    /**
     * 删除IM会话信息
     * 
     * @param id IM会话ID
     * @return 结果
     */
    @Override
    public int deleteImSessionById(Long id)
    {
        return imSessionMapper.deleteImSessionById(id);
    }
}