package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMessageReaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM消息表情反应Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImMessageReactionMapper extends BaseMapper<ImMessageReaction> {

    /**
     * 查询消息的所有反应
     *
     * @param messageId 消息ID
     * @return 反应列表
     */
    List<ImMessageReaction> selectReactionsByMessageId(@Param("messageId") Long messageId);

    /**
     * 查询用户对消息的反应
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 反应对象
     */
    ImMessageReaction selectUserReaction(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 查询消息的反应统计（按emoji分组）
     *
     * @param messageId 消息ID
     * @return 反应统计列表
     */
    List<ImMessageReaction> selectReactionStats(@Param("messageId") Long messageId);

    /**
     * 删除消息反应
     *
     * @param id 反应ID
     * @return 删除行数
     */
    int deleteReaction(@Param("id") Long id);

    /**
     * 删除用户对消息的反应
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteUserReaction(@Param("messageId") Long messageId, @Param("userId") Long userId);
}
