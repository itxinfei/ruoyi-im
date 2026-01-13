package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImAnnouncement;
import com.ruoyi.im.dto.announcement.ImAnnouncementQueryRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告Mapper
 *
 * @author ruoyi
 */
public interface ImAnnouncementMapper extends BaseMapper<ImAnnouncement> {

    /**
     * 分页查询公告列表（带用户信息）
     *
     * @param page 分页参数
     * @param req 查询条件
     * @return 公告分页列表
     */
    IPage<ImAnnouncement> selectAnnouncementPage(Page<?> page, @Param("req") ImAnnouncementQueryRequest req);

    /**
     * 查询置顶公告列表
     *
     * @return 置顶公告列表
     */
    List<ImAnnouncement> selectPinnedAnnouncements();

    /**
     * 查询最新公告列表
     *
     * @param limit 限制数量
     * @return 公告列表
     */
    List<ImAnnouncement> selectLatestAnnouncements(@Param("limit") int limit);

    /**
     * 统计用户发布的公告数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    int countByPublisherId(@Param("userId") Long userId);

    /**
     * 查询过期公告列表
     *
     * @return 过期公告列表
     */
    List<ImAnnouncement> selectExpiredAnnouncements();
}
