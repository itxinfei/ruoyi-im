package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAnnouncementLike;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告点赞记录Mapper
 *
 * @author ruoyi
 */
public interface ImAnnouncementLikeMapper extends BaseMapper<ImAnnouncementLike> {

    /**
     * 查询公告点赞用户列表
     *
     * @param announcementId 公告ID
     * @return 点赞用户列表
     */
    List<ImAnnouncementLike> selectByAnnouncementId(@Param("announcementId") Long announcementId);

    /**
     * 查询用户点赞的公告ID列表
     *
     * @param userId 用户ID
     * @return 点赞公告ID列表
     */
    List<Long> selectLikedAnnouncementIdsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已点赞公告
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     * @return 点赞记录
     */
    ImAnnouncementLike selectByAnnouncementIdAndUserId(@Param("announcementId") Long announcementId,
                                                        @Param("userId") Long userId);

    /**
     * 统计公告点赞数
     *
     * @param announcementId 公告ID
     * @return 点赞数
     */
    int countByAnnouncementId(@Param("announcementId") Long announcementId);
}
