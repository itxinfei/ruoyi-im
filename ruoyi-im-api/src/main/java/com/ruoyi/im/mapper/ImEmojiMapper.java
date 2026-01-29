package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImEmoji;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 自定义表情包Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImEmojiMapper extends BaseMapper<ImEmoji> {

    /**
     * 查询系统表情列表
     *
     * @return 表情列表
     */
    List<ImEmoji> selectSystemEmojis();

    /**
     * 查询用户的自定义表情列表
     *
     * @param userId 用户ID
     * @return 表情列表
     */
    List<ImEmoji> selectUserEmojis(@Param("userId") Long userId);

    /**
     * 查询公开的表情列表
     *
     * @return 表情列表
     */
    List<ImEmoji> selectPublicEmojis();

    /**
     * 查询表情使用统计
     *
     * @param userId 用户ID
     * @return 使用次数最多的表情列表
     */
    List<ImEmoji> selectMostUsedEmojis(@Param("userId") Long userId);

    /**
     * 增加表情使用次数
     *
     * @param emojiId 表情ID
     * @return 更新行数
     */
    int incrementUseCount(@Param("emojiId") Long emojiId);

    /**
     * 增加表情分享次数
     *
     * @param emojiId 表情ID
     * @return 更新行数
     */
    int incrementShareCount(@Param("emojiId") Long emojiId);
}
