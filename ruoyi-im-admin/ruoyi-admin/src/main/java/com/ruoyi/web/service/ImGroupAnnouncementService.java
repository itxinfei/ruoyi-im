package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImGroupAnnouncement;
import java.util.List;

/**
 * 群组公告Service接口
 *
 * @author ruoyi
 */
public interface ImGroupAnnouncementService {

    /**
     * 查询群组公告
     *
     * @param id 群组公告主键
     * @return 群组公告
     */
    ImGroupAnnouncement selectImGroupAnnouncementById(Long id);

    /**
     * 查询群组公告列表
     *
     * @param imGroupAnnouncement 群组公告
     * @return 群组公告集合
     */
    List<ImGroupAnnouncement> selectImGroupAnnouncementList(ImGroupAnnouncement imGroupAnnouncement);

    /**
     * 根据群组ID查询公告列表
     *
     * @param groupId 群组ID
     * @return 公告列表
     */
    List<ImGroupAnnouncement> selectAnnouncementsByGroupId(Long groupId);

    /**
     * 查询群组的置顶公告
     *
     * @param groupId 群组ID
     * @return 置顶公告列表
     */
    List<ImGroupAnnouncement> selectPinnedAnnouncementsByGroupId(Long groupId);

    /**
     * 统计群组公告数量
     *
     * @param groupId 群组ID
     * @return 公告数量
     */
    Integer countAnnouncementsByGroupId(Long groupId);

    /**
     * 新增群组公告
     *
     * @param imGroupAnnouncement 群组公告
     * @return 结果
     */
    int insertImGroupAnnouncement(ImGroupAnnouncement imGroupAnnouncement);

    /**
     * 修改群组公告
     *
     * @param imGroupAnnouncement 群组公告
     * @return 结果
     */
    int updateImGroupAnnouncement(ImGroupAnnouncement imGroupAnnouncement);

    /**
     * 修改置顶状态
     *
     * @param id 公告ID
     * @param isPinned 是否置顶
     * @return 结果
     */
    int updatePinnedStatus(Long id, Integer isPinned);

    /**
     * 批量删除群组公告
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteImGroupAnnouncementByIds(Long[] ids);

    /**
     * 根据群组ID删除所有公告
     *
     * @param groupId 群组ID
     * @return 结果
     */
    int deleteAnnouncementsByGroupId(Long groupId);
}
