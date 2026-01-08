package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImGroupAnnouncement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组公告Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImGroupAnnouncementMapper extends BaseMapper<ImGroupAnnouncement> {

    /**
     * 根据群组ID查询公告列表
     *
     * @param groupId 群组ID
     * @return 公告列表
     */
    List<ImGroupAnnouncement> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据群组ID查询有效公告（未过期、未撤回）
     *
     * @param groupId 群组ID
     * @return 有效公告列表
     */
    List<ImGroupAnnouncement> selectValidByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据群组ID查询置顶公告
     *
     * @param groupId 群组ID
     * @return 置顶公告列表
     */
    List<ImGroupAnnouncement> selectPinnedByGroupId(@Param("groupId") Long groupId);

    /**
     * 获取群组最新公告
     *
     * @param groupId 群组ID
     * @return 最新公告
     */
    ImGroupAnnouncement selectLatestByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询过期公告
     *
     * @param expireTime 过期时间
     * @return 过期公告列表
     */
    List<ImGroupAnnouncement> selectExpiredAnnouncements(@Param("expireTime") LocalDateTime expireTime);
}
