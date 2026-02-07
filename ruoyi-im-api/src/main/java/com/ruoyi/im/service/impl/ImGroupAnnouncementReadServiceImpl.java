package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImGroupAnnouncementRead;
import com.ruoyi.im.mapper.ImGroupAnnouncementReadMapper;
import com.ruoyi.im.service.ImGroupAnnouncementReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群公告已读状态服务实现类
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class ImGroupAnnouncementReadServiceImpl implements ImGroupAnnouncementReadService {

    private final ImGroupAnnouncementReadMapper announcementReadMapper;

    public ImGroupAnnouncementReadServiceImpl(ImGroupAnnouncementReadMapper announcementReadMapper) {
        this.announcementReadMapper = announcementReadMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long groupId, Long announcementId, Long userId) {
        if (groupId == null || announcementId == null || userId == null) {
            return;
        }

        // 检查是否已读
        ImGroupAnnouncementRead existing = announcementReadMapper.selectByAnnouncementAndUser(announcementId, userId);
        if (existing != null) {
            log.debug("用户已读公告，跳过: announcementId={}, userId={}", announcementId, userId);
            return;
        }

        // 创建已读记录
        ImGroupAnnouncementRead read = new ImGroupAnnouncementRead();
        read.setGroupId(groupId);
        read.setAnnouncementId(announcementId);
        read.setUserId(userId);
        read.setReadTime(LocalDateTime.now());
        read.setCreateTime(LocalDateTime.now());

        announcementReadMapper.insert(read);
        log.debug("标记公告已读: announcementId={}, userId={}", announcementId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markBatchAsRead(Long groupId, List<Long> announcementIds, Long userId) {
        if (groupId == null || announcementIds == null || announcementIds.isEmpty() || userId == null) {
            return;
        }

        for (Long announcementId : announcementIds) {
            markAsRead(groupId, announcementId, userId);
        }
    }

    @Override
    public boolean isRead(Long announcementId, Long userId) {
        if (announcementId == null || userId == null) {
            return false;
        }
        ImGroupAnnouncementRead read = announcementReadMapper.selectByAnnouncementAndUser(announcementId, userId);
        return read != null;
    }

    @Override
    public List<Long> getReadUserIds(Long announcementId) {
        if (announcementId == null) {
            return new java.util.ArrayList<>();
        }
        return announcementReadMapper.selectReadUserIdsByAnnouncementId(announcementId);
    }

    @Override
    public Map<String, Object> getReadStatistics(Long announcementId, Integer groupMemberCount) {
        if (announcementId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("readCount", 0);
            result.put("unreadCount", 0);
            result.put("readRate", 0.0);
            return result;
        }

        Integer readCount = announcementReadMapper.countReadUsers(announcementId);
        if (readCount == null) {
            readCount = 0;
        }

        int unreadCount = 0;
        double readRate = 0.0;

        if (groupMemberCount != null && groupMemberCount > 0) {
            unreadCount = groupMemberCount - readCount;
            readRate = (readCount * 100.0) / groupMemberCount;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("readCount", readCount);
        result.put("unreadCount", unreadCount);
        result.put("readRate", Math.round(readRate * 100.0) / 100.0); // 保留两位小数

        return result;
    }

    @Override
    public Map<Long, Boolean> getReadStatusForAnnouncements(List<Long> announcementIds, Long userId) {
        Map<Long, Boolean> result = new HashMap<>();

        if (announcementIds == null || announcementIds.isEmpty() || userId == null) {
            return result;
        }

        List<ImGroupAnnouncementRead> readRecords = announcementReadMapper.selectByUserAndAnnouncementIds(userId, announcementIds);

        // 初始化为未读
        for (Long announcementId : announcementIds) {
            result.put(announcementId, false);
        }

        // 更新已读状态
        for (ImGroupAnnouncementRead read : readRecords) {
            result.put(read.getAnnouncementId(), true);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearReadRecords(Long announcementId) {
        if (announcementId == null) {
            return;
        }
        announcementReadMapper.deleteByAnnouncementId(announcementId);
        log.info("清理公告已读记录: announcementId={}", announcementId);
    }
}
