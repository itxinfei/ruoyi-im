package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImGroupAnnouncement;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImGroupAnnouncementMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.service.ImGroupAnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组公告服务实现
 *
 * @author ruoyi
 */
@Service
public class ImGroupAnnouncementServiceImpl implements ImGroupAnnouncementService {

    private final ImGroupAnnouncementMapper announcementMapper;
    private final ImGroupMemberMapper groupMemberMapper;

    /**
     * 构造器注入依赖
     *
     * @param announcementMapper 群组公告Mapper
     * @param groupMemberMapper   群组成员Mapper
     */
    public ImGroupAnnouncementServiceImpl(ImGroupAnnouncementMapper announcementMapper,
                                           ImGroupMemberMapper groupMemberMapper) {
        this.announcementMapper = announcementMapper;
        this.groupMemberMapper = groupMemberMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAnnouncement(Long groupId, String content, Integer type, String attachmentUrl,
                                   Integer isPinned, LocalDateTime expireTime, Long senderId) {
        // 验证用户是否为群主或管理员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, senderId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String role = member.getRole();
        if (!"OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException("只有群主和管理员可以发布公告");
        }

        // 创建公告
        ImGroupAnnouncement announcement = new ImGroupAnnouncement();
        announcement.setGroupId(groupId);
        announcement.setSenderId(senderId);
        announcement.setContent(content);
        announcement.setType(type != null ? type : 1);
        announcement.setAttachmentUrl(attachmentUrl);
        announcement.setIsPinned(isPinned != null ? isPinned : 0);
        announcement.setStatus(1);
        announcement.setExpireTime(expireTime);
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());

        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    @Override
    public List<ImGroupAnnouncement> getAnnouncements(Long groupId, Long userId) {
        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        return announcementMapper.selectByGroupId(groupId);
    }

    @Override
    public List<ImGroupAnnouncement> getValidAnnouncements(Long groupId, Long userId) {
        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        return announcementMapper.selectValidByGroupId(groupId);
    }

    @Override
    public List<ImGroupAnnouncement> getPinnedAnnouncements(Long groupId, Long userId) {
        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        return announcementMapper.selectPinnedByGroupId(groupId);
    }

    @Override
    public ImGroupAnnouncement getLatestAnnouncement(Long groupId, Long userId) {
        // 验证用户是否为群组成员
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        return announcementMapper.selectLatestByGroupId(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnouncement(Long announcementId, String content, Long userId) {
        ImGroupAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 只有公告发送者或群主可以编辑
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(announcement.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String role = member.getRole();
        boolean isOwner = "OWNER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);
        boolean isSender = announcement.getSenderId().equals(userId);

        if (!isOwner && !isAdmin && !isSender) {
            throw new BusinessException("只有群主、管理员或发送者可以编辑公告");
        }

        // 更新公告
        announcement.setContent(content);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Long announcementId, Long userId) {
        ImGroupAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 只有公告发送者或群主可以删除
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(announcement.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String role = member.getRole();
        boolean isOwner = "OWNER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);
        boolean isSender = announcement.getSenderId().equals(userId);

        if (!isOwner && !isAdmin && !isSender) {
            throw new BusinessException("只有群主、管理员或发送者可以删除公告");
        }

        announcementMapper.deleteById(announcementId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallAnnouncement(Long announcementId, Long userId) {
        ImGroupAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 只有公告发送者或群主可以撤回
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(announcement.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String role = member.getRole();
        boolean isOwner = "OWNER".equals(role);
        boolean isSender = announcement.getSenderId().equals(userId);

        if (!isOwner && !isSender) {
            throw new BusinessException("只有群主或发送者可以撤回公告");
        }

        // 撤回公告
        announcement.setStatus(0);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPinned(Long announcementId, Integer isPinned, Long userId) {
        ImGroupAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 只有群主和管理员可以置顶/取消置顶
        ImGroupMember member = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(announcement.getGroupId(), userId);
        if (member == null) {
            throw new BusinessException("您不是该群组成员");
        }

        String role = member.getRole();
        if (!"OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException("只有群主和管理员可以置顶公告");
        }

        announcement.setIsPinned(isPinned);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredAnnouncements() {
        List<ImGroupAnnouncement> expiredAnnouncements = announcementMapper.selectExpiredAnnouncements(LocalDateTime.now());
        int count = 0;
        for (ImGroupAnnouncement announcement : expiredAnnouncements) {
            announcement.setStatus(0); // 标记为已撤回
            announcement.setUpdateTime(LocalDateTime.now());
            announcementMapper.updateById(announcement);
            count++;
        }
        return count;
    }
}
