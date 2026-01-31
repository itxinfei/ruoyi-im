package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.domain.ImNudge;
import com.ruoyi.im.domain.ImNudgeConfig;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.nudge.ImNudgeCreateRequest;
import com.ruoyi.im.mapper.ImNudgeConfigMapper;
import com.ruoyi.im.mapper.ImNudgeMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImNudgeService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.nudge.ImNudgeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 拍一拍服务实现
 *
 * @author ruoyi
 */
@Service
public class ImNudgeServiceImpl implements ImNudgeService {

    private static final Logger log = LoggerFactory.getLogger(ImNudgeServiceImpl.class);

    private final ImNudgeMapper nudgeMapper;
    private final ImNudgeConfigMapper configMapper;
    private final ImUserMapper userMapper;
    private final ImWebSocketBroadcastService broadcastService;

    /** 默认冷却时间（秒） */
    private static final int DEFAULT_COOLDOWN_SECONDS = 30;

    /** 默认最大连续次数 */
    private static final int DEFAULT_MAX_CONTINUOUS_COUNT = 3;

    public ImNudgeServiceImpl(
            ImNudgeMapper nudgeMapper,
            ImNudgeConfigMapper configMapper,
            ImUserMapper userMapper,
            ImWebSocketBroadcastService broadcastService) {
        this.nudgeMapper = nudgeMapper;
        this.configMapper = configMapper;
        this.userMapper = userMapper;
        this.broadcastService = broadcastService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImNudgeVO nudge(ImNudgeCreateRequest request, Long userId) {
        // 1. 校验：不能拍自己
        if (userId.equals(request.getNudgedUserId())) {
            throw new BusinessException("不能拍自己哦");
        }

        // 2. 获取被拍用户配置
        ImNudgeConfig nudgedConfig = getUserConfig(request.getNudgedUserId());
        if (nudgedConfig.getNudgeEnabled() == 0) {
            throw new BusinessException("对方已关闭拍一拍功能");
        }

        // 3. 检查冷却时间
        LocalDateTime cooldownTime = LocalDateTime.now()
                .minusSeconds(nudgedConfig.getCooldownSeconds() != null
                        ? nudgedConfig.getCooldownSeconds()
                        : DEFAULT_COOLDOWN_SECONDS);

        ImNudge recentNudge = nudgeMapper.findRecentNudge(userId, request.getNudgedUserId(), cooldownTime);
        if (recentNudge != null) {
            int remainingSeconds = nudgedConfig.getCooldownSeconds() -
                    (int) (java.time.Duration.between(recentNudge.getCreateTime(), LocalDateTime.now()).getSeconds());
            throw new BusinessException("拍得太快了，请" + remainingSeconds + "秒后再试");
        }

        // 4. 获取用户信息
        ImUser nudger = userMapper.selectImUserById(userId);
        ImUser nudgedUser = userMapper.selectImUserById(request.getNudgedUserId());

        if (nudger == null || nudgedUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 5. 创建拍一拍记录
        ImNudge nudge = new ImNudge();
        nudge.setConversationId(request.getConversationId());
        nudge.setNudgerId(userId);
        nudge.setNudgedUserId(request.getNudgedUserId());
        nudge.setNudgeCount(1);
        nudge.setCreateTime(LocalDateTime.now());

        nudgeMapper.insert(nudge);

        // 6. 构建 VO
        ImNudgeVO vo = new ImNudgeVO();
        vo.setId(nudge.getId());
        vo.setConversationId(nudge.getConversationId());
        vo.setNudgerId(nudger.getId());
        vo.setNudgerName(nudger.getNickname());
        vo.setNudgerAvatar(nudger.getAvatar());
        vo.setNudgedUserId(nudgedUser.getId());
        vo.setNudgedUserName(nudgedUser.getNickname());
        vo.setNudgedUserAvatar(nudgedUser.getAvatar());
        vo.setNudgeCount(1);
        vo.setCreateTime(nudge.getCreateTime());

        // 7. 通过 WebSocket 广播拍一拍消息
        broadcastNudgeMessage(request.getConversationId(), vo);

        log.info("拍一拍成功: nudger={}, nudged={}", userId, request.getNudgedUserId());
        return vo;
    }

    @Override
    public ImNudgeConfig getUserConfig(Long userId) {
        ImNudgeConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<ImNudgeConfig>()
                        .eq(ImNudgeConfig::getUserId, userId)
                        .last("LIMIT 1")
        );

        if (config == null) {
            // 返回默认配置
            config = new ImNudgeConfig();
            config.setUserId(userId);
            config.setNudgeEnabled(1);
            config.setNudgeSoundEnabled(1);
            config.setNudgeVibrationEnabled(1);
            config.setCooldownSeconds(DEFAULT_COOLDOWN_SECONDS);
            config.setMaxContinuousCount(DEFAULT_MAX_CONTINUOUS_COUNT);
        }

        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserConfig(Long userId, ImNudgeConfig config) {
        config.setUserId(userId);

        ImNudgeConfig existing = configMapper.selectOne(
                new LambdaQueryWrapper<ImNudgeConfig>()
                        .eq(ImNudgeConfig::getUserId, userId)
                        .last("LIMIT 1")
        );

        if (existing == null) {
            configMapper.insert(config);
        } else {
            config.setId(existing.getId());
            configMapper.updateById(config);
        }

        log.info("更新拍一拍配置: userId={}, enabled={}", userId, config.getNudgeEnabled());
    }

    /**
     * 广播拍一拍消息到会话
     */
    private void broadcastNudgeMessage(Long conversationId, ImNudgeVO vo) {
        broadcastService.broadcastNudgeMessage(conversationId, vo.getNudgerId(), vo.getNudgedUserId());
    }
}
