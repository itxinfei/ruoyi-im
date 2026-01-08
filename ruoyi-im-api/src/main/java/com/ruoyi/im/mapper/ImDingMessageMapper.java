package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDingMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * DING消息Mapper
 */
public interface ImDingMessageMapper extends BaseMapper<ImDingMessage> {

    /**
     * 查询用户接收的DING消息列表
     */
    @Select("SELECT DISTINCT d.*, u.nick_name as senderName, u.avatar as senderAvatar " +
            "FROM im_ding_message d " +
            "INNER JOIN im_ding_receipt r ON d.id = r.ding_id " +
            "LEFT JOIN im_user u ON d.sender_id = u.id " +
            "WHERE r.receiver_id = #{userId} " +
            "ORDER BY d.send_time DESC")
    List<ImDingMessage> selectUserDingList(@Param("userId") Long userId);

    /**
     * 查询用户发送的DING消息列表
     */
    @Select("SELECT d.*, u.nick_name as senderName, u.avatar as senderAvatar " +
            "FROM im_ding_message d " +
            "LEFT JOIN im_user u ON d.sender_id = u.id " +
            "WHERE d.sender_id = #{userId} " +
            "ORDER BY d.send_time DESC")
    List<ImDingMessage> selectSentDingList(@Param("userId") Long userId);
}
