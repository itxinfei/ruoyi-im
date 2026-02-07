package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImEmoji;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImEmojiMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImEmojiService;
import com.ruoyi.im.vo.emoji.ImEmojiVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.im.util.BeanConvertUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义表情包服务实现
 *
 * @author ruoyi
 */
@Service
public class ImEmojiServiceImpl implements ImEmojiService {

    private static final Logger logger = LoggerFactory.getLogger(ImEmojiServiceImpl.class);

    private final ImEmojiMapper emojiMapper;
    private final ImUserMapper userMapper;

    public ImEmojiServiceImpl(ImEmojiMapper emojiMapper, ImUserMapper userMapper) {
        this.emojiMapper = emojiMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImEmojiVO uploadEmoji(com.ruoyi.im.dto.emoji.ImEmojiUploadRequest request, Long userId) {
        // 检查表情编码是否已存在
        ImEmoji existEmoji = emojiMapper.selectOne(
                new LambdaQueryWrapper<ImEmoji>()
                        .eq(ImEmoji::getEmojiCode, request.getEmojiCode())
        );
        if (existEmoji != null) {
            throw new BusinessException("表情编码已存在");
        }

        // 创建表情
        ImEmoji emoji = new ImEmoji();
        emoji.setEmojiCode(request.getEmojiCode());
        emoji.setEmojiUrl(request.getEmojiUrl());
        emoji.setEmojiName(request.getEmojiName());
        emoji.setCategory("custom");
        emoji.setUploaderId(userId);
        emoji.setWidth(request.getWidth() != null ? request.getWidth() : 100);
        emoji.setHeight(request.getHeight() != null ? request.getHeight() : 100);
        emoji.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        emoji.setShareCount(0);
        emoji.setUseCount(0);
        emoji.setCreateTime(LocalDateTime.now());
        emoji.setUpdateTime(LocalDateTime.now());

        emojiMapper.insert(emoji);

        logger.info("上传表情成功: emojiCode={}, userId={}", request.getEmojiCode(), userId);
        return convertToVO(emoji, userId);
    }

    @Override
    public List<ImEmojiVO> getUserEmojis(Long userId) {
        // 获取系统表情
        List<ImEmoji> systemEmojis = emojiMapper.selectList(
                new LambdaQueryWrapper<ImEmoji>()
                        .eq(ImEmoji::getCategory, "system")
                        .orderByAsc(ImEmoji::getId)
        );

        // 获取用户自定义表情
        List<ImEmoji> userEmojis = emojiMapper.selectList(
                new LambdaQueryWrapper<ImEmoji>()
                        .eq(ImEmoji::getCategory, "custom")
                        .and(wrapper -> wrapper.eq(ImEmoji::getUploaderId, userId)
                                .or().eq(ImEmoji::getIsPublic, true))
                        .orderByDesc(ImEmoji::getUseCount)
                        .orderByDesc(ImEmoji::getCreateTime)
        );

        // 合并结果，系统表情在前
        systemEmojis.addAll(userEmojis);

        return systemEmojis.stream()
                .map(emoji -> convertToVO(emoji, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ImEmojiVO> getEmojisByCategory(Long userId, String category) {
        List<ImEmoji> emojis;

        if ("system".equals(category)) {
            emojis = emojiMapper.selectList(
                    new LambdaQueryWrapper<ImEmoji>()
                            .eq(ImEmoji::getCategory, "system")
                            .orderByAsc(ImEmoji::getId)
            );
        } else {
            // 自定义表情：用户自己的 + 公开的
            emojis = emojiMapper.selectList(
                    new LambdaQueryWrapper<ImEmoji>()
                            .eq(ImEmoji::getCategory, "custom")
                            .and(wrapper -> wrapper.eq(ImEmoji::getUploaderId, userId)
                                    .or().eq(ImEmoji::getIsPublic, true))
                            .orderByDesc(ImEmoji::getUseCount)
            );
        }

        return emojis.stream()
                .map(emoji -> convertToVO(emoji, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ImEmojiVO> getPublicEmojis() {
        List<ImEmoji> emojis = emojiMapper.selectList(
                new LambdaQueryWrapper<ImEmoji>()
                        .eq(ImEmoji::getCategory, "custom")
                        .eq(ImEmoji::getIsPublic, true)
                        .orderByDesc(ImEmoji::getShareCount)
                        .orderByDesc(ImEmoji::getUseCount)
        );

        return emojis.stream()
                .map(emoji -> convertToVO(emoji, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmoji(Long emojiId, Long userId) {
        ImEmoji emoji = emojiMapper.selectById(emojiId);
        if (emoji == null) {
            throw new BusinessException("表情不存在");
        }

        if (!emoji.getCategory().equals("custom")) {
            throw new BusinessException("系统表情不能删除");
        }

        if (!emoji.getUploaderId().equals(userId)) {
            throw new BusinessException("只能删除自己上传的表情");
        }

        emojiMapper.deleteById(emojiId);

        logger.info("删除表情成功: emojiId={}, userId={}", emojiId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useEmoji(Long emojiId) {
        emojiMapper.incrementUseCount(emojiId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImEmojiVO shareEmoji(Long emojiId, Long userId) {
        ImEmoji emoji = emojiMapper.selectById(emojiId);
        if (emoji == null) {
            throw new BusinessException("表情不存在");
        }

        // 创建副本（用户收藏的表情）
        ImEmoji copyEmoji = new ImEmoji();
        copyEmoji.setEmojiCode(emoji.getEmojiCode() + "_copy_" + System.currentTimeMillis());
        copyEmoji.setEmojiUrl(emoji.getEmojiUrl());
        copyEmoji.setEmojiName(emoji.getEmojiName());
        copyEmoji.setCategory("custom");
        copyEmoji.setUploaderId(userId);
        copyEmoji.setWidth(emoji.getWidth());
        copyEmoji.setHeight(emoji.getHeight());
        copyEmoji.setIsPublic(false);
        copyEmoji.setShareCount(0);
        copyEmoji.setUseCount(0);
        copyEmoji.setCreateTime(LocalDateTime.now());
        copyEmoji.setUpdateTime(LocalDateTime.now());

        emojiMapper.insert(copyEmoji);

        // 增加原表情的分享次数
        emojiMapper.incrementShareCount(emojiId);

        logger.info("分享表情成功: emojiId={}, userId={}", emojiId, userId);
        return convertToVO(copyEmoji, userId);
    }

    /**
     * 转换为VO
     *
     * @param emoji 表情实体
     * @param userId 当前用户ID
     * @return 表情VO
     */
    private ImEmojiVO convertToVO(ImEmoji emoji, Long userId) {
        ImEmojiVO vo = new ImEmojiVO();
        BeanConvertUtil.copyProperties(emoji, vo);

        // 设置是否当前用户上传
        if (userId != null) {
            vo.setIsOwn(emoji.getUploaderId() != null && emoji.getUploaderId().equals(userId));
        } else {
            vo.setIsOwn(false);
        }

        // 设置上传者名称
        if (emoji.getUploaderId() != null) {
            ImUser uploader = userMapper.selectImUserById(emoji.getUploaderId());
            if (uploader != null) {
                vo.setUploaderName(uploader.getNickname());
            }
        }

        return vo;
    }
}
