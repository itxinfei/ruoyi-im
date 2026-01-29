package com.ruoyi.im.service;

import com.ruoyi.im.dto.emoji.ImEmojiUploadRequest;
import com.ruoyi.im.vo.emoji.ImEmojiVO;

import java.util.List;

/**
 * 自定义表情包服务接口
 *
 * @author ruoyi
 */
public interface ImEmojiService {

    /**
     * 上传自定义表情
     *
     * @param request 上传请求
     * @param userId 当前用户ID
     * @return 表情VO
     */
    ImEmojiVO uploadEmoji(ImEmojiUploadRequest request, Long userId);

    /**
     * 获取用户的表情列表（包括系统表情和自定义表情）
     *
     * @param userId 用户ID
     * @return 表情列表
     */
    List<ImEmojiVO> getUserEmojis(Long userId);

    /**
     * 获取指定分类的表情列表
     *
     * @param userId 用户ID
     * @param category 分类（system/custom）
     * @return 表情列表
     */
    List<ImEmojiVO> getEmojisByCategory(Long userId, String category);

    /**
     * 获取公开的表情列表
     *
     * @return 表情列表
     */
    List<ImEmojiVO> getPublicEmojis();

    /**
     * 删除自定义表情
     *
     * @param emojiId 表情ID
     * @param userId 当前用户ID
     */
    void deleteEmoji(Long emojiId, Long userId);

    /**
     * 使用表情（增加使用次数）
     *
     * @param emojiId 表情ID
     */
    void useEmoji(Long emojiId);

    /**
     * 分享表情
     *
     * @param emojiId 表情ID
     * @param userId 当前用户ID
     * @return 表情VO
     */
    ImEmojiVO shareEmoji(Long emojiId, Long userId);
}
