package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMessageFavorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息收藏Mapper
 *
 * @author ruoyi
 */
public interface ImMessageFavoriteMapper extends BaseMapper<ImMessageFavorite> {

    /**
     * 查询用户收藏的消息列表（带消息详情）
     *
     * @param userId 用户ID
     * @return 收藏消息列表
     */
    @Select("SELECT f.*, m.content as messageContent, m.sender_id as senderId, " +
            "m.send_time as messageTime, m.type as messageType, " +
            "u.nick_name as senderName, u.avatar as senderAvatar " +
            "FROM im_message_favorite f " +
            "LEFT JOIN im_message m ON f.message_id = m.id " +
            "LEFT JOIN im_user u ON m.sender_id = u.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    List<ImMessageFavorite> selectFavoriteListWithMessage(@Param("userId") Long userId);

    /**
     * 检查用户是否已收藏某消息
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     * @return 收藏记录，如果未收藏则返回null
     */
    @Select("SELECT * FROM im_message_favorite WHERE user_id = #{userId} AND message_id = #{messageId}")
    ImMessageFavorite selectByUserAndMessage(@Param("userId") Long userId, @Param("messageId") Long messageId);
}
