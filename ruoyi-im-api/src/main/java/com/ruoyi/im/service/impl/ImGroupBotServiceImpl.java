package com.ruoyi.im.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupBot;
import com.ruoyi.im.domain.ImGroupBotRule;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.dto.groupbot.BotCreateRequest;
import com.ruoyi.im.dto.groupbot.BotRuleRequest;
import com.ruoyi.im.dto.groupbot.BotUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImGroupBotMapper;
import com.ruoyi.im.mapper.ImGroupBotRuleMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImBotMessageLogMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.ImGroupBotService;
import com.ruoyi.im.service.ImMessageService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 群机器人服务实现
 *
 * @author ruoyi
 */
@Service
public class ImGroupBotServiceImpl implements ImGroupBotService {

    private static final Logger log = LoggerFactory.getLogger(ImGroupBotServiceImpl.class);

    // 机器人类型常量
    private static final String BOT_TYPE_SERVICE = "SERVICE";
    private static final String BOT_TYPE_NOTIFY = "NOTIFY";
    private static final String BOT_TYPE_MANAGE = "MANAGE";

    // 触发类型常量
    private static final String TRIGGER_TYPE_KEYWORD = "KEYWORD";
    private static final String TRIGGER_TYPE_TIME = "TIME";
    private static final String TRIGGER_TYPE_COMMAND = "COMMAND";

    // 匹配模式常量
    private static final String MATCH_MODE_EXACT = "EXACT";
    private static final String MATCH_MODE_CONTAINS = "CONTAINS";
    private static final String MATCH_MODE_REGEX = "REGEX";

    // 回复类型常量
    private static final String REPLY_TYPE_TEXT = "TEXT";
    private static final String REPLY_TYPE_IMAGE = "IMAGE";
    private static final String REPLY_TYPE_CARD = "CARD";

    private final ImGroupBotMapper groupBotMapper;
    private final ImGroupBotRuleMapper groupBotRuleMapper;
    private final ImGroupMapper groupMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final ImBotMessageLogMapper botMessageLogMapper;
    private final ImMessageMapper messageMapper;
    private final ImMessageService messageService;
    private final ImWebSocketBroadcastService broadcastService;

    /**
     * 构造器注入依赖
     */
    public ImGroupBotServiceImpl(ImGroupBotMapper groupBotMapper,
                                 ImGroupBotRuleMapper groupBotRuleMapper,
                                 ImGroupMapper groupMapper,
                                 ImGroupMemberMapper groupMemberMapper,
                                 ImBotMessageLogMapper botMessageLogMapper,
                                 ImMessageMapper messageMapper,
                                 ImMessageService messageService,
                                 ImWebSocketBroadcastService broadcastService) {
        this.groupBotMapper = groupBotMapper;
        this.groupBotRuleMapper = groupBotRuleMapper;
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.botMessageLogMapper = botMessageLogMapper;
        this.messageMapper = messageMapper;
        this.messageService = messageService;
        this.broadcastService = broadcastService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBot(BotCreateRequest request, Long userId) {
        log.info("创建群机器人: userId={}, groupId={}, botName={}",
                userId, request.getGroupId(), request.getBotName());

        // 1. 验证群组是否存在
        ImGroup group = groupMapper.selectById(request.getGroupId());
        if (group == null) {
            throw new BusinessException(ImErrorCode.GROUP_NOT_FOUND);
        }

        // 2. 验证用户是否是群主或管理员
        ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(request.getGroupId(), userId);
        if (member == null || !"OWNER".equals(member.getGroupRole()) && !"ADMIN".equals(member.getGroupRole())) {
            throw new BusinessException(ImErrorCode.NO_PERMISSION);
        }

        // 3. 检查群组机器人数量限制
        int botCount = groupBotMapper.countByGroupId(request.getGroupId());
        if (botCount >= 10) {
            throw new BusinessException(ImErrorCode.GROUP_BOT_LIMIT_EXCEEDED);
        }

        // 4. 创建机器人
        ImGroupBot bot = new ImGroupBot();
        bot.setGroupId(request.getGroupId());
        bot.setBotName(request.getBotName());
        bot.setBotType(request.getBotType() != null ? request.getBotType() : BOT_TYPE_SERVICE);
        bot.setAvatar(request.getAvatar());
        bot.setDescription(request.getDescription());
        bot.setIsEnabled(1);

        groupBotMapper.insert(bot);
        log.info("群机器人已创建: botId={}", bot.getId());

        // 5. 创建机器人规则
        if (request.getRules() != null && !request.getRules().isEmpty()) {
            for (BotRuleRequest ruleRequest : request.getRules()) {
                createRule(bot.getId(), ruleRequest);
            }
        }

        return bot.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBot(BotUpdateRequest request, Long userId) {
        log.info("更新群机器人: userId={}, botId={}", userId, request.getBotId());

        ImGroupBot bot = groupBotMapper.selectById(request.getBotId());
        if (bot == null) {
            throw new BusinessException(ImErrorCode.BOT_NOT_FOUND);
        }

        // 验证权限
        ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(bot.getGroupId(), userId);
        if (member == null || !"OWNER".equals(member.getGroupRole()) && !"ADMIN".equals(member.getGroupRole())) {
            throw new BusinessException(ImErrorCode.NO_PERMISSION);
        }

        // 更新字段
        if (StrUtil.isNotBlank(request.getBotName())) {
            bot.setBotName(request.getBotName());
        }
        if (StrUtil.isNotBlank(request.getAvatar())) {
            bot.setAvatar(request.getAvatar());
        }
        if (StrUtil.isNotBlank(request.getDescription())) {
            bot.setDescription(request.getDescription());
        }
        if (request.getIsEnabled() != null) {
            bot.setIsEnabled(request.getIsEnabled());
        }

        groupBotMapper.updateById(bot);
        log.info("群机器人已更新: botId={}", bot.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBot(Long botId, Long userId) {
        log.info("删除群机器人: userId={}, botId={}", userId, botId);

        ImGroupBot bot = groupBotMapper.selectById(botId);
        if (bot == null) {
            throw new BusinessException(ImErrorCode.BOT_NOT_FOUND);
        }

        // 验证权限
        ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(bot.getGroupId(), userId);
        if (member == null || !"OWNER".equals(member.getGroupRole()) && !"ADMIN".equals(member.getGroupRole())) {
            throw new BusinessException(ImErrorCode.NO_PERMISSION);
        }

        // 删除机器人规则
        groupBotRuleMapper.deleteByBotId(botId);

        // 删除机器人
        groupBotMapper.deleteById(botId);

        log.info("群机器人已删除: botId={}", botId);
    }

    @Override
    public List<ImGroupBot> getGroupBots(Long groupId, Long userId) {
        // 验证用户是否是群成员
        ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.NO_PERMISSION);
        }

        return groupBotMapper.selectByGroupId(groupId);
    }

    @Override
    public ImGroupBot getBotDetail(Long botId, Long userId) {
        ImGroupBot bot = groupBotMapper.selectById(botId);
        if (bot == null) {
            throw new BusinessException(ImErrorCode.BOT_NOT_FOUND);
        }

        // 验证权限
        ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(bot.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException(ImErrorCode.NO_PERMISSION);
        }

        // 加载机器人规则
        List<ImGroupBotRule> rules = groupBotRuleMapper.selectByBotId(botId);
        bot.setRules(rules);
        bot.setRuleCount(rules.size());

        return bot;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRule(Long botId, BotRuleRequest request, Long userId) {
        log.info("添加机器人规则: userId={}, botId={}, ruleName={}",
                userId, botId, request.getRuleName());

        ImGroupBot bot = groupBotMapper.selectById(botId);
        if (bot == null) {
            throw new BusinessException(ImErrorCode.BOT_NOT_FOUND);
        }

        // 验证权限
        ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(bot.getGroupId(), userId);
        if (member == null || !"OWNER".equals(member.getGroupRole()) && !"ADMIN".equals(member.getGroupRole())) {
            throw new BusinessException(ImErrorCode.NO_PERMISSION);
        }

        return createRule(botId, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(Long ruleId, BotRuleRequest request, Long userId) {
        log.info("更新机器人规则: userId={}, ruleId={}", userId, ruleId);

        ImGroupBotRule rule = groupBotRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException(ImErrorCode.BOT_RULE_NOT_FOUND);
        }

        // 验证权限
        ImGroupBot bot = groupBotMapper.selectById(rule.getBotId());
        if (bot != null) {
            ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(bot.getGroupId(), userId);
            if (member == null || !"OWNER".equals(member.getGroupRole()) && !"ADMIN".equals(member.getGroupRole())) {
                throw new BusinessException(ImErrorCode.NO_PERMISSION);
            }
        }

        // 更新字段
        if (StrUtil.isNotBlank(request.getRuleName())) {
            rule.setRuleName(request.getRuleName());
        }
        if (StrUtil.isNotBlank(request.getTriggerContent())) {
            rule.setTriggerContent(request.getTriggerContent());
        }
        if (StrUtil.isNotBlank(request.getReplyContent())) {
            rule.setReplyContent(request.getReplyContent());
        }
        if (request.getPriority() != null) {
            rule.setPriority(request.getPriority());
        }
        if (request.getIsEnabled() != null) {
            rule.setIsEnabled(request.getIsEnabled());
        }

        groupBotRuleMapper.updateById(rule);
        log.info("机器人规则已更新: ruleId={}", ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long ruleId, Long userId) {
        log.info("删除机器人规则: userId={}, ruleId={}", userId, ruleId);

        ImGroupBotRule rule = groupBotRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException(ImErrorCode.BOT_RULE_NOT_FOUND);
        }

        // 验证权限
        ImGroupBot bot = groupBotMapper.selectById(rule.getBotId());
        if (bot != null) {
            ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(bot.getGroupId(), userId);
            if (member == null || !"OWNER".equals(member.getGroupRole()) && !"ADMIN".equals(member.getGroupRole())) {
                throw new BusinessException(ImErrorCode.NO_PERMISSION);
            }
        }

        groupBotRuleMapper.deleteById(ruleId);
        log.info("机器人规则已删除: ruleId={}", ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setBotEnabled(Long botId, Boolean enabled, Long userId) {
        log.info("设置机器人状态: userId={}, botId={}, enabled={}", userId, botId, enabled);

        ImGroupBot bot = groupBotMapper.selectById(botId);
        if (bot == null) {
            throw new BusinessException(ImErrorCode.BOT_NOT_FOUND);
        }

        // 验证权限
        ImGroupMember member = groupMemberMapper.selectByGroupIdAndUserId(bot.getGroupId(), userId);
        if (member == null || !"OWNER".equals(member.getGroupRole()) && !"ADMIN".equals(member.getGroupRole())) {
            throw new BusinessException(ImErrorCode.NO_PERMISSION);
        }

        bot.setIsEnabled(enabled ? 1 : 0);
        groupBotMapper.updateById(bot);
        log.info("机器人状态已更新: botId={}, enabled={}", botId, enabled);
    }

    @Override
    public void processGroupMessage(Long groupId, Long senderId, String messageContent) {
        log.debug("处理群消息触发机器人: groupId={}, senderId={}, content={}",
                groupId, senderId, messageContent);

        // 获取群组启用的机器人列表
        List<ImGroupBot> bots = groupBotMapper.selectEnabledByGroupId(groupId);
        if (bots == null || bots.isEmpty()) {
            log.debug("群组无启用机器人: groupId={}", groupId);
            return;
        }

        for (ImGroupBot bot : bots) {
            // 只处理SERVICE类型的机器人
            if (!BOT_TYPE_SERVICE.equals(bot.getBotType())) {
                continue;
            }

            // 匹配规则并获取回复
            String replyContent = matchBotRule(bot.getId(), messageContent);
            if (StrUtil.isNotBlank(replyContent)) {
                // 记录日志
                logBotMessage(bot.getId(), groupId, senderId, messageContent, replyContent);

                // 发送机器人回复消息
                sendBotReply(groupId, bot, replyContent);
            }
        }
    }

    @Override
    public String matchBotRule(Long botId, String content) {
        // 获取机器人启用的规则列表（按优先级降序）
        List<ImGroupBotRule> rules = groupBotRuleMapper.selectEnabledByBotId(botId);
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        // 按优先级排序
        rules.sort((r1, r2) -> {
            int p1 = r1.getPriority() != null ? r1.getPriority() : 0;
            int p2 = r2.getPriority() != null ? r2.getPriority() : 0;
            return Integer.compare(p2, p1);
        });

        // 遍历规则匹配
        for (ImGroupBotRule rule : rules) {
            // 只处理关键词触发类型
            if (!TRIGGER_TYPE_KEYWORD.equals(rule.getTriggerType())) {
                continue;
            }

            if (isMatch(content, rule.getTriggerContent(), rule.getMatchMode())) {
                log.info("机器人规则匹配成功: botId={}, ruleName={}, content={}",
                        botId, rule.getRuleName(), content);
                return rule.getReplyContent();
            }
        }

        return null;
    }

    /**
     * 判断内容是否匹配规则
     *
     * @param content    消息内容
     * @param pattern    匹配模式
     * @param matchMode 匹配方式
     * @return 是否匹配
     */
    private boolean isMatch(String content, String pattern, String matchMode) {
        if (StrUtil.isBlank(content) || StrUtil.isBlank(pattern)) {
            return false;
        }

        switch (matchMode) {
            case MATCH_MODE_EXACT:
                return content.trim().equals(pattern.trim());
            case MATCH_MODE_CONTAINS:
                return content.contains(pattern);
            case MATCH_MODE_REGEX:
                try {
                    Pattern regex = Pattern.compile(pattern);
                    Matcher matcher = regex.matcher(content);
                    return matcher.find();
                } catch (Exception e) {
                    log.warn("正则表达式匹配失败: pattern={}", pattern, e);
                    return false;
                }
            default:
                return content.contains(pattern);
        }
    }

    /**
     * 创建规则
     *
     * @param botId  机器人ID
     * @param request 规则请求
     * @return 规则ID
     */
    private Long createRule(Long botId, BotRuleRequest request) {
        ImGroupBotRule rule = new ImGroupBotRule();
        rule.setBotId(botId);
        rule.setRuleName(request.getRuleName());
        rule.setTriggerType(request.getTriggerType());
        rule.setTriggerContent(request.getTriggerContent());
        rule.setReplyType(request.getReplyType());
        rule.setReplyContent(request.getReplyContent());
        rule.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        rule.setIsEnabled(1);
        rule.setMatchMode(request.getMatchMode() != null ? request.getMatchMode() : MATCH_MODE_CONTAINS);

        groupBotRuleMapper.insert(rule);
        log.info("机器人规则已创建: ruleId={}", rule.getId());

        return rule.getId();
    }

    /**
     * 记录机器人消息日志
     *
     * @param botId          机器人ID
     * @param groupId        群组ID
     * @param triggerUserId  触发用户ID
     * @param triggerMessage 触发消息
     * @param replyMessage   回复消息
     */
    private void logBotMessage(Long botId, Long groupId, Long triggerUserId,
                               String triggerMessage, String replyMessage) {
        try {
            com.ruoyi.im.domain.ImBotMessageLog logEntry = new com.ruoyi.im.domain.ImBotMessageLog();
            logEntry.setBotId(botId);
            logEntry.setGroupId(groupId);
            logEntry.setTriggerUserId(triggerUserId);
            logEntry.setTriggerMessage(triggerMessage);
            logEntry.setReplyMessage(replyMessage);
            botMessageLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.error("记录机器人消息日志失败", e);
        }
    }

    /**
     * 发送机器人回复消息
     *
     * @param groupId     群组ID
     * @param bot         机器人
     * @param replyContent 回复内容
     */
    private void sendBotReply(Long groupId, ImGroupBot bot, String replyContent) {
        try {
            // 构建消息发送请求
            com.ruoyi.im.dto.message.ImMessageSendRequest request =
                    new com.ruoyi.im.dto.message.ImMessageSendRequest();
            request.setConversationId(groupId); // 群组ID即为会话ID
            request.setType("TEXT");
            request.setContent(replyContent);

            // 使用机器人ID作为发送者（需要特殊处理）
            // 这里暂时使用系统用户ID发送，后续可以优化为机器人专用ID
            Long systemUserId = 0L; // 系统用户ID
            com.ruoyi.im.vo.message.ImMessageVO vo = messageService.sendMessage(request, systemUserId);

            // 广播消息
            if (vo != null && broadcastService != null) {
                broadcastService.broadcastMessageToConversation(groupId, vo.getId(), systemUserId);
            }

            log.info("机器人回复已发送: botId={}, groupId={}", bot.getId(), groupId);
        } catch (Exception e) {
            log.error("发送机器人回复失败: botId={}, groupId={}", bot.getId(), groupId, e);
        }
    }
}
