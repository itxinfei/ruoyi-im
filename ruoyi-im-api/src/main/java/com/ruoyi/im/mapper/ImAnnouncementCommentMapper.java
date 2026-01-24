package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAnnouncementComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告评论Mapper
 *
 * @author ruoyi
 */
public interface ImAnnouncementCommentMapper extends BaseMapper<ImAnnouncementComment> {

    /**
     * 查询公告评论列表
     *
     * @param announcementId 公告ID
     * @return 评论列表
     */
    List<ImAnnouncementComment> selectByAnnouncementId(@Param("announcementId") Long announcementId);

    /**
     * 查询一级评论列表
     *
     * @param announcementId 公告ID
     * @return 一级评论列表
     */
    List<ImAnnouncementComment> selectRootCommentsByAnnouncementId(@Param("announcementId") Long announcementId);

    /**
     * 查询评论的子评论列表
     *
     * @param parentIds 父评论ID列表
     * @return 子评论列表
     */
    List<ImAnnouncementComment> selectByParentIds(@Param("parentIds") List<Long> parentIds);

    /**
     * 统计公告评论数
     *
     * @param announcementId 公告ID
     * @return 评论数
     */
    int countByAnnouncementId(@Param("announcementId") Long announcementId);
}
