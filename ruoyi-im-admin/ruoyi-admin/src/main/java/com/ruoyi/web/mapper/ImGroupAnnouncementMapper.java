package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImGroupAnnouncement;
import java.util.List;
import java.util.Map;

/**
 * 群组公告Mapper接口
 *
 * @author ruoyi
 */
public interface ImGroupAnnouncementMapper {

    /**
     * 查询群组公告
     *
     * @param id 群组公告主键
     * @return 群组公告
     */
    public ImGroupAnnouncement selectImGroupAnnouncementById(Long id);

    /**
     * 查询群组公告列表
     *
     * @param imGroupAnnouncement 群组公告
     * @return 群组公告集合
     */
    public List<ImGroupAnnouncement> selectImGroupAnnouncementList(ImGroupAnnouncement imGroupAnnouncement);

    /**
     * 根据群组ID查询公告列表
     *
     * @param groupId 群组ID
     * @return 公告列表
     */
    public List<ImGroupAnnouncement> selectAnnouncementsByGroupId(Long groupId);

    /**
     * 查询群组的置顶公告
     *
     * @param groupId 群组ID
     * @return 置顶公告列表
     */
    public List<ImGroupAnnouncement> selectPinnedAnnouncementsByGroupId(Long groupId);

    /**
     * 统计群组公告数量
     *
     * @param groupId 群组ID
     * @return 公告数量
     */
    public Integer countAnnouncementsByGroupId(Long groupId);

    /**
     * 新增群组公告
     *
     * @param imGroupAnnouncement 群组公告
     * @return 结果
     */
    public int insertImGroupAnnouncement(ImGroupAnnouncement imGroupAnnouncement);

    /**
     * 修改群组公告
     *
     * @param imGroupAnnouncement 群组公告
     * @return 结果
     */
    public int updateImGroupAnnouncement(ImGroupAnnouncement imGroupAnnouncement);

    /**
     * 修改置顶状态
     *
     * @param params 包含id和isPinned的参数
     * @return 结果
     */
    public int updatePinnedStatus(java.util.Map<String, Object> params);

    /**
     * 删除群组公告
     *
     * @param id 群组公告主键
     * @return 结果
     */
    public int deleteImGroupAnnouncementById(Long id);

    /**
     * 批量删除群组公告
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteImGroupAnnouncementByIds(Long[] ids);

    /**
     * 根据群组ID删除所有公告
     *
     * @param groupId 群组ID
     * @return 结果
     */
    public int deleteAnnouncementsByGroupId(Long groupId);

    /**
     * 获取群公告统计数据
     *
     * @return 统计数据
     */
    public Map<String, Object> getAnnouncementStatistics();
}
