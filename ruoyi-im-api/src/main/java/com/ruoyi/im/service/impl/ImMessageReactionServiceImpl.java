package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImMessageReactionMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息表情反应服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMessageReactionServiceImpl implements ImMessageReactionService {

    private final ImMessageReactionMapper reactionMapper;
    private final ImMessageMapper messageMapper;
    private final ImUserMapper userMapper;

    public ImMessageReactionServiceImpl(ImMessageReactionMapper reactionMapper,
                                       ImMessageMapper messageMapper,
                                       ImUserMapper userMapper) {
        this.reactionMapper = reactionMapper;
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public ImMessageReactionVO addReaction(ImMessageReactionAddRequest request, Long userId) {
        // 验证消息是否存在
        ImMessage message = messageMapper.selectImMessageById(request.getMessageId());
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 检查用户是否已对该消息使用过表情
        ImMessageReaction existingReaction = reactionMapper.selectUserReaction(request.getMessageId(), userId);

        if (existingReaction != null) {
            // 如果使用的是相同表情，则删除（切换效果）
            if (request.getEmoji().equals(existingReaction.getEmoji())) {
                reactionMapper.deleteUserReaction(request.getMessageId(), userId);
                return null; // 返回null表示已删除
            } else {
                // 使用不同表情，则删除旧的，新增新的
                reactionMapper.deleteUserReaction(request.getMessageId(), userId);

                ImMessageReaction newReaction = new ImMessageReaction();
                newReaction.setMessageId(request.getMessageId());
                newReaction.setUserId(userId);
                newReaction.setEmoji(request.getEmoji());
                newReaction.setReactionType(request.getReactionType() != null ? request.getReactionType() : "EMOJI");
                newReaction.setCreateTime(LocalDateTime.now());

                reactionMapper.insert(newReaction);

                return convertToVO(newReaction, userId);
            }
        } else {
            // 新增反应
            ImMessageReaction reaction = new ImMessageReaction();
            reaction.setMessageId(request.getMessageId());
            reaction.setUserId(userId);
            reaction.setEmoji(request.getEmoji());
            reaction.setReactionType(request.getReactionType() != null ? request.getReactionType() : "EMOJI");
            reaction.setCreateTime(LocalDateTime.now());

            reactionMapper.insert(reaction);

            return convertToVO(reaction, userId);
        }
    }

    @Override
    @Transactional
    public void removeReaction(Long messageId, Long userId) {
        int rows = reactionMapper.deleteUserReaction(messageId, userId);
        if (rows == 0) {
            throw new BusinessException("反应不存在或已删除");
        }
    }

    @Override
    public List<ImMessageReactionVO> getMessageReactions(Long messageId, Long userId) {
        List<ImMessageReaction> reactions = reactionMapper.selectReactionsByMessageId(messageId);

        // 按emoji分组统计
        Map<String, List<ImMessageReaction>> groupedByEmoji = reactions.stream()
                .collect(Collectors.groupingBy(ImMessageReaction::getEmoji));

        List<ImMessageReactionVO> result = new ArrayList<>();

        for (Map.Entry<String, List<ImMessageReaction>> entry : groupedByEmoji.entrySet()) {
            List<ImMessageReaction> emojiReactions = entry.getValue();

            ImMessageReactionVO vo = new ImMessageReactionVO();
            vo.setEmoji(entry.getKey());
            vo.setCount(emojiReactions.size());

            // 检查当前用户是否使用了该表情
            boolean isMine = emojiReactions.stream()
                    .anyMatch(r -> r.getUserId().equals(userId));
            vo.setIsMine(isMine);

            // 设置反应类型
            if (!emojiReactions.isEmpty()) {
                vo.setReactionType(emojiReactions.get(0).getReactionType());
            }

            // 获取反应用户列表（用于显示头像）
            List<Long> userIds = emojiReactions.stream()
                    .map(ImMessageReaction::getUserId)
                    .limit(5) // 只取前5个用户
                    .collect(Collectors.toList());

            // 这里可以扩展为设置用户头像列表
            result.add(vo);
        }

        // 按数量降序排序
        result.sort((a, b) -> b.getCount().compareTo(a.getCount()));

        return result;
    }

    @Override
    public List<ImMessageReactionVO> getReactionStats(Long messageId, Long userId) {
        List<ImMessageReaction> reactions = reactionMapper.selectReactionsByMessageId(messageId);

        Map<String, ImMessageReactionVO> statsMap = new HashMap<>();

        for (ImMessageReaction reaction : reactions) {
            String emoji = reaction.getEmoji();
            ImMessageReactionVO vo = statsMap.get(emoji);

            if (vo == null) {
                vo = new ImMessageReactionVO();
                vo.setEmoji(emoji);
                vo.setReactionType(reaction.getReactionType());
                vo.setCount(1);
                vo.setIsMine(reaction.getUserId().equals(userId));

                // 获取用户信息
                ImUser user = userMapper.selectImUserById(reaction.getUserId());
                if (user != null) {
                    vo.setUserNickname(user.getNickname());
                    vo.setUserAvatar(user.getAvatar());
                }

                statsMap.put(emoji, vo);
            } else {
                vo.setCount(vo.getCount() + 1);
                if (reaction.getUserId().equals(userId)) {
                    vo.setIsMine(true);
                }
            }
        }

        return new ArrayList<>(statsMap.values());
    }

    /**
     * 转换为VO对象
     */
    private ImMessageReactionVO convertToVO(ImMessageReaction reaction, Long currentUserId) {
        ImMessageReactionVO vo = new ImMessageReactionVO();
        BeanConvertUtil.copyProperties(reaction, vo);
        vo.setIsMine(reaction.getUserId().equals(currentUserId));
        vo.setCount(1);

        // 获取用户信息
        ImUser user = userMapper.selectImUserById(reaction.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        return vo;
    }
}
