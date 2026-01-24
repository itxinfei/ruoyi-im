package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAnnouncementRead;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告已读记录Mapper
 *
 * @author ruoyi
 */
public interface ImAnnouncementReadMapper extends BaseMapper<ImAnnouncementRead> {

    /**
     * 查询公告已读用户列表
     *
     * @param announcementId 公告ID
     * @return 已读用户列表
     */
    List<ImAnnouncementRead> selectByAnnouncementId(@Param("announcementId") Long announcementId);

    /**
     * 查询用户已读的公告ID列表
     *
     * @param userId 用户ID
     * @return 已读公告ID列表
     */
    List<Long> selectReadAnnouncementIdsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已读公告
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     * @return 已读记录
     */
    ImAnnouncementRead selectByAnnouncementIdAndUserId(@Param("announcementId") Long announcementId,
                                                        @Param("userId") Long userId);
}
