package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImSession;
import com.ruoyi.im.dto.session.ImSessionQueryRequest;
import java.util.List;

/**
 * 会话Service接口
 * 
 * @author ruoyi
 */
public interface ImSessionService {
    
    /**
     * 根据ID查询会话
     * 
     * @param id 会话ID
     * @return 会话
     */
    ImSession selectById(Long id);
    
    /**
     * 查询会话列表
     * 
     * @param request 查询条件
     * @return 会话集合
     */
    List<ImSession> selectImSessionList(ImSessionQueryRequest request);
    
    /**
     * 新增会话
     * 
     * @param imSession 会话
     * @return 结果
     */
    int insert(ImSession imSession);
    
    /**
     * 修改会话
     * 
     * @param imSession 会话
     * @return 结果
     */
    int update(ImSession imSession);
    
    /**
     * 批量删除会话
     * 
     * @param ids 需要删除的会话ID
     * @return 结果
     */
    int deleteByIds(Long[] ids);
    
    /**
     * 删除会话信息
     * 
     * @param id 会话ID
     * @return 结果
     */
    int deleteById(Long id);
}