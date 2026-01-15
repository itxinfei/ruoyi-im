package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImDingMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * DING消息Mapper接口
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Mapper
public interface ImDingMessageMapper {

    /**
     * 查询DING消息列表
     */
    List<ImDingMessage> selectImDingMessageList(ImDingMessage imDingMessage);

    /**
     * 根据ID获取DING消息
     */
    ImDingMessage selectImDingMessageById(Long id);

    /**
     * 新增DING消息
     */
    int insertImDingMessage(ImDingMessage imDingMessage);

    /**
     * 修改DING消息
     */
    int updateImDingMessage(ImDingMessage imDingMessage);

    /**
     * 删除DING消息
     */
    int deleteImDingMessageByIds(Long[] ids);

    /**
     * 获取DING消息统计
     */
    Map<String, Object> getDingStatistics();

    /**
     * 获取DING消息回执详情
     */
    List<Map<String, Object>> getDingReceipts(Long dingId);
}
