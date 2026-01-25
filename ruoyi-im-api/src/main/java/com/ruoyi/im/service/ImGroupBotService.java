package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImGroupBot;
import com.ruoyi.im.dto.groupbot.BotCreateRequest;
import com.ruoyi.im.dto.groupbot.BotRuleRequest;
import com.ruoyi.im.dto.groupbot.BotUpdateRequest;

import java.util.List;

/**
 * 群机器人服务接口
 *
 * @author ruoyi
 */
public interface ImGroupBotService {

    /**
     * 创建群机器人
     *
     * @param request 创建请求
     * @param userId  当前用户ID
     * @return 机器人ID
     */
    Long createBot(BotCreateRequest request, Long userId);

    /**
     * 更新群机器人
     *
     * @param request 更新请求
     * @param userId  当前用户ID
     */
    void updateBot(BotUpdateRequest request, Long userId);

    /**
     * 删除群机器人
     *
     * @param botId  机器人ID
     * @param userId 当前用户ID
     */
    void deleteBot(Long botId, Long userId);

    /**
     * 获取群组机器人列表
     *
     * @param groupId 群组ID
     * @param userId  当前用户ID
     * @return 机器人列表
     */
    List<ImGroupBot> getGroupBots(Long groupId, Long userId);

    /**
     * 获取机器人详情
     *
     * @param botId  机器人ID
     * @param userId 当前用户ID
     * @return 机器人实体
     */
    ImGroupBot getBotDetail(Long botId, Long userId);

    /**
     * 添加机器人规则
     *
     * @param botId  机器人ID
     * @param request 规则请求
     * @param userId 当前用户ID
     * @return 规则ID
     */
    Long addRule(Long botId, BotRuleRequest request, Long userId);

    /**
     * 更新机器人规则
     *
     * @param ruleId  规则ID
     * @param request 规则请求
     * @param userId  当前用户ID
     */
    void updateRule(Long ruleId, BotRuleRequest request, Long userId);

    /**
     * 删除机器人规则
     *
     * @param ruleId 规则ID
     * @param userId 当前用户ID
     */
    void deleteRule(Long ruleId, Long userId);

    /**
     * 启用/禁用机器人
     *
     * @param botId   机器人ID
     * @param enabled 是否启用
     * @param userId  当前用户ID
     */
    void setBotEnabled(Long botId, Boolean enabled, Long userId);

    /**
     * 处理群消息，触发机器人自动回复
     *
     * @param groupId       群组ID
     * @param senderId      发送者ID
     * @param messageContent 消息内容
     */
    void processGroupMessage(Long groupId, Long senderId, String messageContent);

    /**
     * 匹配机器人规则
     *
     * @param botId   机器人ID
     * @param content 消息内容
     * @return 匹配的规则回复内容，如果没有匹配则返回null
     */
    String matchBotRule(Long botId, String content);
}
