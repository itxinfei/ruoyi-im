package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImGroupAnnouncement;
import com.ruoyi.web.mapper.ImGroupAnnouncementMapper;
import com.ruoyi.web.service.ImGroupAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组公告Service实现
 *
 * @author ruoyi
 */
@Service
public class ImGroupAnnouncementServiceImpl implements ImGroupAnnouncementService {

    @Autowired
    private ImGroupAnnouncementMapper imGroupAnnouncementMapper;

    @Override
    public ImGroupAnnouncement selectImGroupAnnouncementById(Long id) {
        return imGroupAnnouncementMapper.selectImGroupAnnouncementById(id);
    }

    @Override
    public List<ImGroupAnnouncement> selectImGroupAnnouncementList(ImGroupAnnouncement imGroupAnnouncement) {
        return imGroupAnnouncementMapper.selectImGroupAnnouncementList(imGroupAnnouncement);
    }

    @Override
    public List<ImGroupAnnouncement> selectAnnouncementsByGroupId(Long groupId) {
        return imGroupAnnouncementMapper.selectAnnouncementsByGroupId(groupId);
    }

    @Override
    public List<ImGroupAnnouncement> selectPinnedAnnouncementsByGroupId(Long groupId) {
        return imGroupAnnouncementMapper.selectPinnedAnnouncementsByGroupId(groupId);
    }

    @Override
    public Integer countAnnouncementsByGroupId(Long groupId) {
        return imGroupAnnouncementMapper.countAnnouncementsByGroupId(groupId);
    }

    @Override
    public int insertImGroupAnnouncement(ImGroupAnnouncement imGroupAnnouncement) {
        return imGroupAnnouncementMapper.insertImGroupAnnouncement(imGroupAnnouncement);
    }

    @Override
    public int updateImGroupAnnouncement(ImGroupAnnouncement imGroupAnnouncement) {
        return imGroupAnnouncementMapper.updateImGroupAnnouncement(imGroupAnnouncement);
    }

    @Override
    public int updatePinnedStatus(Long id, Integer isPinned) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("isPinned", isPinned);
        return imGroupAnnouncementMapper.updatePinnedStatus(params);
    }

    @Override
    public int deleteImGroupAnnouncementByIds(Long[] ids) {
        return imGroupAnnouncementMapper.deleteImGroupAnnouncementByIds(ids);
    }

    @Override
    public int deleteAnnouncementsByGroupId(Long groupId) {
        return imGroupAnnouncementMapper.deleteAnnouncementsByGroupId(groupId);
    }

    @Override
    public Map<String, Object> getAnnouncementStatistics() {
        return imGroupAnnouncementMapper.getAnnouncementStatistics();
    }
}
