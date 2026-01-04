package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ImMessage;
import com.ruoyi.system.mapper.ImMessageMapper;
import com.ruoyi.system.service.IImMessageService;

/**
 * IM消息Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Service
public class ImMessageServiceImpl implements IImMessageService
{
    @Autowired
    private ImMessageMapper imMessageMapper;

    /**
     * 查询IM消息
     * 
     * @param id IM消息ID
     * @return IM消息
     */
    @Override
    public ImMessage selectImMessageById(Long id)
    {
        return imMessageMapper.selectImMessageById(id);
    }

    /**
     * 查询IM消息列表
     * 
     * @param imMessage IM消息
     * @return IM消息
     */
    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage)
    {
        return imMessageMapper.selectImMessageList(imMessage);
    }

    /**
     * 新增IM消息
     * 
     * @param imMessage IM消息
     * @return 结果
     */
    @Override
    public int insertImMessage(ImMessage imMessage)
    {
        return imMessageMapper.insertImMessage(imMessage);
    }

    /**
     * 修改IM消息
     * 
     * @param imMessage IM消息
     * @return 结果
     */
    @Override
    public int updateImMessage(ImMessage imMessage)
    {
        return imMessageMapper.updateImMessage(imMessage);
    }

    /**
     * 批量删除IM消息
     * 
     * @param ids 需要删除的IM消息ID
     * @return 结果
     */
    @Override
    public int deleteImMessageByIds(Long[] ids)
    {
        return imMessageMapper.deleteImMessageByIds(ids);
    }

    /**
     * 删除IM消息信息
     * 
     * @param id IM消息ID
     * @return 结果
     */
    @Override
    public int deleteImMessageById(Long id)
    {
        return imMessageMapper.deleteImMessageById(id);
    }
}