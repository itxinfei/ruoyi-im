package com.ruoyi.web.service;

import com.ruoyi.system.domain.ImSession;
import java.util.List;

/**
 * 会话Service接口
 * 
 * @author ruoyi
 */
public interface IImSessionService 
{
    /**
     * 查询会话
     * 
     * @param id 会话ID
     * @return 会话
     */
    public ImSession selectImSessionById(Long id);

    /**
     * 查询会话列表
     * 
     * @param imSession 会话
     * @return 会话集合
     */
    public List<ImSession> selectImSessionList(ImSession imSession);

    /**
     * 新增会话
     * 
     * @param imSession 会话
     * @return 结果
     */
    public int insertImSession(ImSession imSession);

    /**
     * 修改会话
     * 
     * @param imSession 会话
     * @return 结果
     */
    public int updateImSession(ImSession imSession);

    /**
     * 批量删除会话
     * 
     * @param ids 需要删除的会话ID
     * @return 结果
     */
    public int deleteImSessionByIds(Long[] ids);

    /**
     * 删除会话信息
     * 
     * @param id 会话ID
     * @return 结果
     */
    public int deleteImSessionById(Long id);
}