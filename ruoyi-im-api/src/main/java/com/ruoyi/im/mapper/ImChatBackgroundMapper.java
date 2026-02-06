package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImChatBackground;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天背景设置 Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImChatBackgroundMapper extends BaseMapper<ImChatBackground> {

    /**
     * 根据用户ID和会话ID查询背景设置
     *
     * @param userId 用户ID
     * @param conversationId 会话ID（NULL表示全局背景）
     * @return 背景设置对象
     */
    ImChatBackground selectByUserIdAndConversationId(Long userId, Long conversationId);
}
