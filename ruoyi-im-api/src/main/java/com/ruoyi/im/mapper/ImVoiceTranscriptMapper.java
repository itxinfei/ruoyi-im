package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImVoiceTranscript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 语音转文字Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImVoiceTranscriptMapper extends BaseMapper<ImVoiceTranscript> {

    /**
     * 根据消息ID查询转写记录
     *
     * @param messageId 消息ID
     * @return 转写记录
     */
    ImVoiceTranscript selectByMessageId(@Param("messageId") Long messageId);

    /**
     * 查询待处理的转写记录
     *
     * @param limit 限制数量
     * @return 转写记录列表
     */
    List<ImVoiceTranscript> selectPending(@Param("limit") Integer limit);

    /**
     * 查询用户转写记录列表
     *
     * @param userId 用户ID
     * @return 转写记录列表
     */
    List<ImVoiceTranscript> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计用户转写次数
     *
     * @param userId 用户ID
     * @return 次数
     */
    Integer countByUserId(@Param("userId") Long userId);
}
