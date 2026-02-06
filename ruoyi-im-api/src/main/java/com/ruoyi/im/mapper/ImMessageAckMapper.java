package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImMessageAck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息确认 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImMessageAckMapper extends BaseMapper<ImMessageAck> {

    /**
     * 查询消息的所有接收确认
     *
     * @param messageId 消息ID
     * @return 接收确认列表
     */
    @Select("SELECT * FROM im_message_ack WHERE message_id = #{messageId} AND ack_type = 'receive' ORDER BY ack_timestamp ASC")
    List<ImMessageAck> selectReceiveAcksByMessageId(@Param("messageId") Long messageId);

    /**
     * 检查消息是否已被指定用户接收
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param ackType 确认类型
     * @return 是否存在确认记录
     */
    @Select("SELECT COUNT(*) FROM im_message_ack WHERE message_id = #{messageId} AND user_id = #{userId} AND ack_type = #{ackType}")
    int countAck(@Param("messageId") Long messageId, @Param("userId") Long userId, @Param("ackType") String ackType);

    /**
     * 查询用户的消息确认记录
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     * @return 确认记录列表
     */
    @Select("SELECT * FROM im_message_ack WHERE user_id = #{userId} AND message_id = #{messageId} ORDER BY ack_timestamp ASC")
    List<ImMessageAck> selectUserMessageAcks(@Param("userId") Long userId, @Param("messageId") Long messageId);

    /**
     * 删除消息的所有确认记录
     *
     * @param messageId 消息ID
     * @return 删除数量
     */
    int deleteByMessageId(Long messageId);
}
