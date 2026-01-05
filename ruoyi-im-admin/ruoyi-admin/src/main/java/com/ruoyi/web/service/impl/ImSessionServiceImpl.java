package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.service.IImSessionService;
import com.ruoyi.system.domain.ImSession;

/**
 * 会话Service业务层处理
 * 
 * @author ruoyi
 */
@Service("adminImSessionServiceImpl")
public class ImSessionServiceImpl implements IImSessionService
{
    @Autowired
    private com.ruoyi.system.service.IImSessionService systemSessionService;

    /**
     * 查询会话
     * 
     * @param id 会话ID
     * @return 会话
     */
    @Override
    public ImSession selectImSessionById(Long id)
    {
        return systemSessionService.selectImSessionById(id);
    }

    /**
     * 查询会话列表
     * 
     * @param imSession 会话
     * @return 会话
     */
    @Override
    public List<ImSession> selectImSessionList(ImSession imSession)
    {
        return systemSessionService.selectImSessionList(imSession);
    }

    /**
     * 新增会话
     * 
     * @param imSession 会话
     * @return 结果
     */
    @Override
    public int insertImSession(ImSession imSession)
    {
        return systemSessionService.insertImSession(imSession);
    }

    /**
     * 修改会话
     * 
     * @param imSession 会话
     * @return 结果
     */
    @Override
    public int updateImSession(ImSession imSession)
    {
        return systemSessionService.updateImSession(imSession);
    }

    /**
     * 批量删除会话
     * 
     * @param ids 需要删除的会话ID
     * @return 结果
     */
    @Override
    public int deleteImSessionByIds(Long[] ids)
    {
        return systemSessionService.deleteImSessionByIds(ids);
    }

    /**
     * 删除会话信息
     * 
     * @param id 会话ID
     * @return 结果
     */
    @Override
    public int deleteImSessionById(Long id)
    {
        return systemSessionService.deleteImSessionById(id);
    }
}