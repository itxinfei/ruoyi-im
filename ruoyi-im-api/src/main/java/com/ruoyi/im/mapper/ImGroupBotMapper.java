package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImGroupBot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群机器人Mapper接口
 *
 * @author ruoyi
 */
public interface ImGroupBotMapper extends BaseMapper<ImGroupBot> {

    /**
     * 根据群组ID查询机器人列表
     *
     * @param groupId 群组ID
     * @return 机器人列表
     */
    List<ImGroupBot> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询启用的机器人列表
     *
     * @return 启用的机器人列表
     */
    List<ImGroupBot> selectEnabledBots();

    /**
     * 根据群组ID查询启用的机器人列表
     *
     * @param groupId 群组ID
     * @return 启用的机器人列表
     */
    List<ImGroupBot> selectEnabledByGroupId(@Param("groupId") Long groupId);

    /**
     * 统计群组的机器人数量
     *
     * @param groupId 群组ID
     * @return 机器人数量
     */
    int countByGroupId(@Param("groupId") Long groupId);

    /**
     * 批量查询多个群组的机器人
     *
     * @param groupIds 群组ID列表
     * @return 机器人列表
     */
    List<ImGroupBot> selectByGroupIds(@Param("groupIds") List<Long> groupIds);
}
