package com.ruoyi.im.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImConversation;
import com.ruoyi.im.domain.ImMessage;
import com.ruoyi.im.domain.ImMessageFavorite;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImConversationMapper;
import com.ruoyi.im.mapper.ImMessageFavoriteMapper;
import com.ruoyi.im.mapper.ImMessageMapper;
import com.ruoyi.im.service.ImMessageFavoriteService;
import com.ruoyi.im.utils.MessageEncryptionUtil;
import com.ruoyi.im.vo.favorite.FavoriteMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息收藏服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMessageFavoriteServiceImpl implements ImMessageFavoriteService {

    @Autowired
    private ImMessageFavoriteMapper favoriteMapper;

    @Autowired
    private ImMessageMapper messageMapper;

    @Autowired
    private ImConversationMapper conversationMapper;

    @Autowired
    private MessageEncryptionUtil encryptionUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addFavorite(Long messageId, Long conversationId, Long userId, String remark, String tags) {
        // 检查消息是否存在
        ImMessage message = messageMapper.selectImMessageById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 检查是否已收藏
        ImMessageFavorite existing = favoriteMapper.selectByUserAndMessage(userId, messageId);
        if (existing != null) {
            throw new BusinessException("该消息已收藏");
        }

        ImMessageFavorite favorite = new ImMessageFavorite();
        favorite.setUserId(userId);
        favorite.setMessageId(messageId);
        favorite.setConversationId(conversationId != null ? conversationId : message.getConversationId());
        favorite.setRemark(remark);
        favorite.setTags(tags);
        favorite.setCreateTime(LocalDateTime.now());

        favoriteMapper.insert(favorite);
        return favorite.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long messageId, Long userId) {
        int result = favoriteMapper.delete(new LambdaQueryWrapper<ImMessageFavorite>()
                .eq(ImMessageFavorite::getUserId, userId)
                .eq(ImMessageFavorite::getMessageId, messageId));
        if (result == 0) {
            throw new BusinessException("收藏记录不存在");
        }
    }

    @Override
    public boolean isFavorited(Long messageId, Long userId) {
        return favoriteMapper.selectByUserAndMessage(userId, messageId) != null;
    }

    @Override
    public List<FavoriteMessageVO> getUserFavorites(Long userId) {
        List<ImMessageFavorite> favorites = favoriteMapper.selectFavoriteListWithMessage(userId);
        return convertToVOList(favorites);
    }

    @Override
    public List<FavoriteMessageVO> getConversationFavorites(Long conversationId, Long userId) {
        List<ImMessageFavorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<ImMessageFavorite>()
                        .eq(ImMessageFavorite::getUserId, userId)
                        .eq(ImMessageFavorite::getConversationId, conversationId)
                        .orderByDesc(ImMessageFavorite::getCreateTime)
        );
        return convertToVOList(favorites);
    }

    @Override
    public List<FavoriteMessageVO> getFavoritesByTag(Long userId, String tag) {
        List<ImMessageFavorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<ImMessageFavorite>()
                        .eq(ImMessageFavorite::getUserId, userId)
                        .like(ImMessageFavorite::getTags, tag)
                        .orderByDesc(ImMessageFavorite::getCreateTime)
        );
        return convertToVOList(favorites);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFavorite(Long messageId, Long userId, String remark, String tags) {
        ImMessageFavorite favorite = favoriteMapper.selectByUserAndMessage(userId, messageId);
        if (favorite == null) {
            throw new BusinessException("收藏记录不存在");
        }

        if (remark != null) {
            favorite.setRemark(remark);
        }
        if (tags != null) {
            favorite.setTags(tags);
        }

        favoriteMapper.updateById(favorite);
    }

    /**
     * 转换为VO列表
     */
    private List<FavoriteMessageVO> convertToVOList(List<ImMessageFavorite> favorites) {
        List<FavoriteMessageVO> voList = new ArrayList<>();
        for (ImMessageFavorite favorite : favorites) {
            FavoriteMessageVO vo = new FavoriteMessageVO();
            vo.setId(favorite.getId());
            vo.setMessageId(favorite.getMessageId());
            vo.setConversationId(favorite.getConversationId());
            vo.setRemark(favorite.getRemark());
            vo.setTags(favorite.getTags());
            vo.setCreateTime(favorite.getCreateTime());

            // 设置消息内容（解密）
            if (favorite.getMessageContent() != null) {
                vo.setMessageContent(encryptionUtil.decryptMessage(favorite.getMessageContent()));
            }

            // 设置发送者信息
            vo.setSenderId(favorite.getSenderId());
            vo.setSenderName(favorite.getSenderName());
            vo.setSenderAvatar(favorite.getSenderAvatar());

            // 设置消息信息
            vo.setMessageType(favorite.getMessageType());
            vo.setMessageTime(favorite.getMessageTime());

            // 获取会话名称
            if (favorite.getConversationId() != null) {
                ImConversation conversation = conversationMapper.selectById(favorite.getConversationId());
                if (conversation != null && StrUtil.isNotBlank(conversation.getName())) {
                    vo.setConversationName(conversation.getName());
                } else {
                    vo.setConversationName("私聊");
                }
            }

            voList.add(vo);
        }
        return voList;
    }
}
