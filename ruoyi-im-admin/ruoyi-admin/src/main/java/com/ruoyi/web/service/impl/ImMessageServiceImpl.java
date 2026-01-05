package com.ruoyi.web.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.service.IImMessageService;
import com.ruoyi.system.domain.ImMessage;

/**
 * 消息Service业务层处理
 * 
 * @author ruoyi
 */
@Service("adminImMessageServiceImpl")
public class ImMessageServiceImpl implements IImMessageService
{
    @Autowired
    private com.ruoyi.system.service.IImMessageService systemMessageService;

    /**
     * 查询消息
     * 
     * @param id 消息ID
     * @return 消息
     */
    @Override
    public ImMessage selectImMessageById(Long id)
    {
        return systemMessageService.selectImMessageById(id);
    }

    /**
     * 查询消息列表
     * 
     * @param imMessage 消息
     * @return 消息
     */
    @Override
    public List<ImMessage> selectImMessageList(ImMessage imMessage)
    {
        return systemMessageService.selectImMessageList(imMessage);
    }

    /**
     * 新增消息
     * 
     * @param imMessage 消息
     * @return 结果
     */
    @Override
    public int insertImMessage(ImMessage imMessage)
    {
        return systemMessageService.insertImMessage(imMessage);
    }

    /**
     * 修改消息
     * 
     * @param imMessage 消息
     * @return 结果
     */
    @Override
    public int updateImMessage(ImMessage imMessage)
    {
        return systemMessageService.updateImMessage(imMessage);
    }

    /**
     * 批量删除消息
     * 
     * @param ids 需要删除的消息ID
     * @return 结果
     */
    @Override
    public int deleteImMessageByIds(Long[] ids)
    {
        return systemMessageService.deleteImMessageByIds(ids);
    }

    /**
     * 删除消息信息
     * 
     * @param id 消息ID
     * @return 结果
     */
    @Override
    public int deleteImMessageById(Long id)
    {
        return systemMessageService.deleteImMessageById(id);
    }
}