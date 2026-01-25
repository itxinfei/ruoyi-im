package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImDingRead;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DING消息已读记录Mapper接口
 *
 * @author ruoyi
 */
public interface ImDingReadMapper extends BaseMapper<ImDingRead> {

    /**
     * 根据DING消息ID查询已读记录列表
     *
     * @param dingId DING消息ID
     * @return 已读记录列表
     */
    List<ImDingRead> selectByDingId(@Param("dingId") Long dingId);

    /**
     * 根据用户ID查询已读记录列表
     *
     * @param userId 用户ID
     * @return 已读记录列表
     */
    List<ImDingRead> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户对指定DING消息的已读记录
     *
     * @param dingId DING消息ID
     * @param userId 用户ID
     * @return 已读记录
     */
    ImDingRead selectByDingIdAndUserId(@Param("dingId") Long dingId, @Param("userId") Long userId);

    /**
     * 删除DING消息的所有已读记录
     *
     * @param dingId DING消息ID
     * @return 影响行数
     */
    int deleteByDingId(@Param("dingId") Long dingId);

    /**
     * 统计DING消息的已读人数
     *
     * @param dingId DING消息ID
     * @return 已读人数
     */
    int countByDingId(@Param("dingId") Long dingId);

    /**
     * 批量查询用户对多个DING消息的已读状态
     *
     * @param dingIds DING消息ID列表
     * @param userId  用户ID
     * @return 已读的DING消息ID列表
     */
    List<Long> selectReadDingIdsByUserId(@Param("dingIds") List<Long> dingIds, @Param("userId") Long userId);
}
