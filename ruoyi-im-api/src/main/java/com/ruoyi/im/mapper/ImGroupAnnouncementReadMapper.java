package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImGroupAnnouncementRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 群公告已读状态 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImGroupAnnouncementReadMapper extends BaseMapper<ImGroupAnnouncementRead> {

    /**
     * 查询公告的已读用户ID列表
     *
     * @param announcementId 公告ID
     * @return 已读用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM im_group_announcement_read WHERE announcement_id = #{announcementId}")
    List<Long> selectReadUserIdsByAnnouncementId(@Param("announcementId") Long announcementId);

    /**
     * 统计公告已读人数
     *
     * @param announcementId 公告ID
     * @return 已读人数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM im_group_announcement_read WHERE announcement_id = #{announcementId}")
    Integer countReadUsers(@Param("announcementId") Long announcementId);

    /**
     * 检查用户是否已读公告
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     * @return 已读记录
     */
    @Select("SELECT * FROM im_group_announcement_read WHERE announcement_id = #{announcementId} AND user_id = #{userId}")
    ImGroupAnnouncementRead selectByAnnouncementAndUser(@Param("announcementId") Long announcementId,
                                                         @Param("userId") Long userId);

    /**
     * 删除公告的所有已读记录
     *
     * @param announcementId 公告ID
     * @return 删除数量
     */
    int deleteByAnnouncementId(@Param("announcementId") Long announcementId);

    /**
     * 批量查询用户对多个公告的已读状态
     *
     * @param userId 用户ID
     * @param announcementIds 公告ID列表
     * @return 已读记录列表
     */
    List<ImGroupAnnouncementRead> selectByUserAndAnnouncementIds(@Param("userId") Long userId,
                                                                  @Param("announcementIds") List<Long> announcementIds);
}
