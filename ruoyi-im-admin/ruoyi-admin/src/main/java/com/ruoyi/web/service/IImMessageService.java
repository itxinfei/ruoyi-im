package com.ruoyi.web.service;

import com.ruoyi.system.domain.ImMessage;
import java.util.List;

/**
 * 消息Service接口
 * 
 * @author ruoyi
 */
public interface IImMessageService 
{
    /**
     * 查询消息
     * 
     * @param id 消息ID
     * @return 消息
     */
    public ImMessage selectImMessageById(Long id);

    /**
     * 查询消息列表
     * 
     * @param imMessage 消息
     * @return 消息集合
     */
    public List<ImMessage> selectImMessageList(ImMessage imMessage);

    /**
     * 新增消息
     * 
     * @param imMessage 消息
     * @return 结果
     */
    public int insertImMessage(ImMessage imMessage);

    /**
     * 修改消息
     * 
     * @param imMessage 消息
     * @return 结果
     */
    public int updateImMessage(ImMessage imMessage);

    /**
     * 批量删除消息
     * 
     * @param ids 需要删除的消息ID
     * @return 结果
     */
    public int deleteImMessageByIds(Long[] ids);

    /**
     * 删除消息信息
     * 
     * @param id 消息ID
     * @return 结果
     */
    public int deleteImMessageById(Long id);
}