package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ImMessage;

/**
 * IM消息Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface ImMessageMapper 
{
    /**
     * 查询IM消息
     * 
     * @param id IM消息ID
     * @return IM消息
     */
    public ImMessage selectImMessageById(Long id);

    /**
     * 查询IM消息列表
     * 
     * @param imMessage IM消息
     * @return IM消息集合
     */
    public List<ImMessage> selectImMessageList(ImMessage imMessage);

    /**
     * 新增IM消息
     * 
     * @param imMessage IM消息
     * @return 结果
     */
    public int insertImMessage(ImMessage imMessage);

    /**
     * 修改IM消息
     * 
     * @param imMessage IM消息
     * @return 结果
     */
    public int updateImMessage(ImMessage imMessage);

    /**
     * 删除IM消息
     * 
     * @param id IM消息ID
     * @return 结果
     */
    public int deleteImMessageById(Long id);

    /**
     * 批量删除IM消息
     * 
     * @param ids 需要删除的IM消息ID
     * @return 结果
     */
    public int deleteImMessageByIds(Long[] ids);
}