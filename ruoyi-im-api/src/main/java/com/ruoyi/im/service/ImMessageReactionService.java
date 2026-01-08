package com.ruoyi.im.service;

import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;

import java.util.List;

/**
 * 消息表情反应服务接口
 *
 * @author ruoyi
 */
public interface ImMessageReactionService {

    /**
     * 添加消息反应
     * 如果用户已对该消息使用相同表情，则删除原反应（切换效果）
     * 如果用户使用不同表情，则替换为新表情
     *
     * @param request 反应请求
     * @param userId 当前用户ID
     * @return 反应VO
     */
    ImMessageReactionVO addReaction(ImMessageReactionAddRequest request, Long userId);

    /**
     * 删除消息反应
     *
     * @param messageId 消息ID
     * @param userId 当前用户ID
     */
    void removeReaction(Long messageId, Long userId);

    /**
     * 获取消息的所有反应
     *
     * @param messageId 消息ID
     * @param userId 当前用户ID（用于标记是否当前用户的反应）
     * @return 反应列表
     */
    List<ImMessageReactionVO> getMessageReactions(Long messageId, Long userId);

    /**
     * 获取消息的反应统计
     * 按emoji分组，返回每个emoji的反应数量和用户列表
     *
     * @param messageId 消息ID
     * @param userId 当前用户ID
     * @return 反应统计列表
     */
    List<ImMessageReactionVO> getReactionStats(Long messageId, Long userId);
}
