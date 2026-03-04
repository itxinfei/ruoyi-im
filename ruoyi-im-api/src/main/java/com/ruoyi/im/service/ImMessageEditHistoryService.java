package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImMessageEditHistory;
import com.ruoyi.im.vo.message.MessageEditHistoryVO;

import java.util.List;

/**
 * 消息编辑历史服务接口
 *
 * @author ruoyi
 */
public interface ImMessageEditHistoryService {

    /**
     * 保存编辑历史
     *
     * @param history 编辑历史
     */
    void saveEditHistory(ImMessageEditHistory history);

    /**
     * 查询消息的编辑历史列表
     *
     * @param messageId 消息 ID
     * @return 编辑历史列表
     */
    List<MessageEditHistoryVO> getEditHistory(Long messageId);

    /**
     * 统计消息的编辑次数
     *
     * @param messageId 消息 ID
     * @return 编辑次数
     */
    Integer countEditTimes(Long messageId);
}
