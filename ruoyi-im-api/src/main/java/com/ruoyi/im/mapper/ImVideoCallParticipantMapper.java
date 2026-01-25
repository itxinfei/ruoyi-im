package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImVideoCallParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频通话参与者Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImVideoCallParticipantMapper extends BaseMapper<ImVideoCallParticipant> {

    /**
     * 根据通话ID查询参与者列表
     *
     * @param callId 通话ID
     * @return 参与者列表
     */
    List<ImVideoCallParticipant> selectByCallId(@Param("callId") Long callId);

    /**
     * 根据通话ID和用户ID查询参与者
     *
     * @param callId 通话ID
     * @param userId 用户ID
     * @return 参与者信息
     */
    ImVideoCallParticipant selectByCallIdAndUserId(@Param("callId") Long callId, @Param("userId") Long userId);

    /**
     * 根据通话ID统计已加入的参与者数量
     *
     * @param callId 通话ID
     * @return 参与者数量
     */
    Integer countJoinedByCallId(@Param("callId") Long callId);

    /**
     * 更新参与者状态
     *
     * @param id 参与者ID
     * @param status 状态
     * @return 更新行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
