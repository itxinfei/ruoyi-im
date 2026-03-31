package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.mapper.ImMessageReactionMapper;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 消息表情回应服务实现 - 彻底修复编译错误，严格对齐接口
 */
@Service
@RequiredArgsConstructor
public class ImMessageReactionServiceImpl implements ImMessageReactionService {

    private final ImMessageReactionMapper reactionMapper;
    private final ImMessageMapper messageMapper;
    private final ImWebSocketBroadcastService broadcastService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImMessageReactionVO addReaction(ImMessageReactionAddRequest request, Long userId) {
        ImMessage message = messageMapper.selectImMessageById(request.getMessageId());
        if (Objects.isNull(message)) {
            return null;
        }

        LambdaQueryWrapper<ImMessageReaction> query = Wrappers.lambdaQuery();
        query.eq(ImMessageReaction::getMessageId, request.getMessageId())
             .eq(ImMessageReaction::getUserId, userId)
             .eq(ImMessageReaction::getEmoji, request.getEmoji());

        ImMessageReaction exist = reactionMapper.selectOne(query);
        String action;

        if (Objects.nonNull(exist)) {
            // 已存在则取消 (Toggle)
            reactionMapper.deleteById(exist.getId());
            action = "REMOVE";
        } else {
            // 不存在则新增
            ImMessageReaction reaction = new ImMessageReaction();
            reaction.setMessageId(request.getMessageId());
            reaction.setUserId(userId);
            reaction.setEmoji(request.getEmoji());
            reaction.setCreateTime(LocalDateTime.now()); // 修正 Date 到 LocalDateTime
            reactionMapper.insert(reaction);
            action = "ADD";
        }

        // 异步广播更新通知 (严格对齐 5 参数签名)
        broadcastService.broadcastReactionUpdate(
            message.getConversationId(), 
            request.getMessageId(), 
            userId, 
            request.getEmoji(), 
            action
        );
        
        return new ImMessageReactionVO();
    }

    @Override
    public void removeReaction(Long messageId, Long userId) {
        reactionMapper.delete(Wrappers.<ImMessageReaction>lambdaQuery()
                .eq(ImMessageReaction::getMessageId, messageId)
                .eq(ImMessageReaction::getUserId, userId));
    }

    @Override
    public List<ImMessageReactionVO> getMessageReactions(Long messageId, Long userId) {
        List<ImMessageReaction> reactions = reactionMapper.selectReactionsByMessageId(messageId);
        List<ImMessageReactionVO> voList = new ArrayList<>();
        for (ImMessageReaction reaction : reactions) {
            ImMessageReactionVO vo = new ImMessageReactionVO();
            vo.setId(reaction.getId());
            vo.setMessageId(reaction.getMessageId());
            vo.setUserId(reaction.getUserId());
            vo.setEmoji(reaction.getEmoji());
            vo.setCreateTime(reaction.getCreateTime());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<ImMessageReactionVO> getReactionStats(Long messageId, Long userId) {
        List<ImMessageReaction> stats = reactionMapper.selectReactionStats(messageId);
        List<ImMessageReactionVO> voList = new ArrayList<>();
        for (ImMessageReaction reaction : stats) {
            ImMessageReactionVO vo = new ImMessageReactionVO();
            vo.setMessageId(reaction.getMessageId());
            vo.setEmoji(reaction.getEmoji());
            vo.setCount(reaction.getId() != null ? reaction.getId().intValue() : 0); // count(*) aliased to id
            voList.add(vo);
        }
        return voList;
    }
}
