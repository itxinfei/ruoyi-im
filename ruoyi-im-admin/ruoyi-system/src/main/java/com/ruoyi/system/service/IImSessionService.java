package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ImSession;

/**
 * IM会话Service接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface IImSessionService 
{
    /**
     * 查询IM会话
     * 
     * @param id IM会话ID
     * @return IM会话
     */
    public ImSession selectImSessionById(Long id);

    /**
     * 查询IM会话列表
     * 
     * @param imSession IM会话
     * @return IM会话集合
     */
    public List<ImSession> selectImSessionList(ImSession imSession);

    /**
     * 新增IM会话
     * 
     * @param imSession IM会话
     * @return 结果
     */
    public int insertImSession(ImSession imSession);

    /**
     * 修改IM会话
     * 
     * @param imSession IM会话
     * @return 结果
     */
    public int updateImSession(ImSession imSession);

    /**
     * 批量删除IM会话
     * 
     * @param ids 需要删除的IM会话ID
     * @return 结果
     */
    public int deleteImSessionByIds(Long[] ids);

    /**
     * 删除IM会话信息
     * 
     * @param id IM会话ID
     * @return 结果
     */
    public int deleteImSessionById(Long id);
}