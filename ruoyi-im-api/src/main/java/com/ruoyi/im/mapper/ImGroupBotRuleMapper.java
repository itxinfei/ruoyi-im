package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImGroupBotRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群机器人规则Mapper接口
 *
 * @author ruoyi
 */
public interface ImGroupBotRuleMapper extends BaseMapper<ImGroupBotRule> {

    /**
     * 根据机器人ID查询规则列表
     *
     * @param botId 机器人ID
     * @return 规则列表
     */
    List<ImGroupBotRule> selectByBotId(@Param("botId") Long botId);

    /**
     * 根据机器人ID查询启用的规则列表（按优先级降序）
     *
     * @param botId 机器人ID
     * @return 启用的规则列表
     */
    List<ImGroupBotRule> selectEnabledByBotId(@Param("botId") Long botId);

    /**
     * 根据机器人ID删除所有规则
     *
     * @param botId 机器人ID
     * @return 影响行数
     */
    int deleteByBotId(@Param("botId") Long botId);

    /**
     * 批量查询多个机器人的规则
     *
     * @param botIds 机器人ID列表
     * @return 规则列表
     */
    List<ImGroupBotRule> selectByBotIds(@Param("botIds") List<Long> botIds);

    /**
     * 根据触发类型查询规则列表
     *
     * @param triggerType 触发类型
     * @return 规则列表
     */
    List<ImGroupBotRule> selectByTriggerType(@Param("triggerType") String triggerType);
}
