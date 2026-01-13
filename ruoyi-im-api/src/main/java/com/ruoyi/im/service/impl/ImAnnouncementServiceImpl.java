package com.ruoyi.im.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImAnnouncement;
import com.ruoyi.im.dto.announcement.ImAnnouncementCreateRequest;
import com.ruoyi.im.dto.announcement.ImAnnouncementQueryRequest;
import com.ruoyi.im.dto.announcement.ImAnnouncementUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImAnnouncementMapper;
import com.ruoyi.im.service.ImAnnouncementService;
import com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO;
import com.ruoyi.im.vo.announcement.ImAnnouncementVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 公告管理服务实现
 *
 * @author ruoyi
 */
@Service
public class ImAnnouncementServiceImpl implements ImAnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(ImAnnouncementServiceImpl.class);

    @Autowired
    private ImAnnouncementMapper announcementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAnnouncement(ImAnnouncementCreateRequest request, Long userId) {
        ImAnnouncement announcement = new ImAnnouncement();
        BeanUtils.copyProperties(request, announcement);

        announcement.setStatus("DRAFT");
        announcement.setViewCount(0);
        announcement.setLikeCount(0);
        announcement.setCommentCount(0);
        announcement.setReadCount(0);
        announcement.setCreateTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());

        // 处理附件
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            announcement.setAttachments(JSON.toJSONString(request.getAttachments()));
        }

        // 处理目标ID列表
        if (request.getTargetIds() != null && !request.getTargetIds().isEmpty()) {
            announcement.setTargetIds(JSON.toJSONString(request.getTargetIds()));
            announcement.setTotalTargetCount(request.getTargetIds().size());
        } else {
            announcement.setTotalTargetCount(0); // 全员公告需要后续计算
        }

        announcementMapper.insert(announcement);
        log.info("创建公告成功: announcementId={}, title={}, publisher={}", announcement.getId(), announcement.getTitle(), userId);
        return announcement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAnnouncement(ImAnnouncementUpdateRequest request, Long userId) {
        ImAnnouncement announcement = announcementMapper.selectById(request.getId());
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 只有草稿或已撤回的公告才能修改
        if (!"DRAFT".equals(announcement.getStatus()) && !"WITHDRAWN".equals(announcement.getStatus())) {
            throw new BusinessException("只能修改草稿或已撤回的公告");
        }

        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            announcement.setContent(request.getContent());
        }
        if (request.getPriority() != null) {
            announcement.setPriority(request.getPriority());
        }
        if (request.getExpiryTime() != null) {
            announcement.setExpiryTime(request.getExpiryTime());
        }
        if (request.getIsPinned() != null) {
            announcement.setIsPinned(request.getIsPinned());
        }
        if (request.getAllowComment() != null) {
            announcement.setAllowComment(request.getAllowComment());
        }
        if (request.getRemark() != null) {
            announcement.setRemark(request.getRemark());
        }

        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        log.info("更新公告成功: announcementId={}, operator={}", request.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Long announcementId, Long userId) {
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 只有发布人可以删除
        if (!announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("只有发布人可以删除公告");
        }

        announcementMapper.deleteById(announcementId);
        log.info("删除公告成功: announcementId={}, operator={}", announcementId, userId);
    }

    @Override
    public ImAnnouncementDetailVO getAnnouncementDetail(Long announcementId, Long userId) {
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 增加浏览次数
        announcement.setViewCount(announcement.getViewCount() + 1);
        announcementMapper.updateById(announcement);

        // 标记为已读
        // TODO: 记录用户已读状态

        return convertToDetailVO(announcement, userId);
    }

    @Override
    public IPage<ImAnnouncementVO> getAnnouncementPage(ImAnnouncementQueryRequest request, Long userId) {
        Page<ImAnnouncement> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ImAnnouncement> announcementPage = announcementMapper.selectAnnouncementPage(page, request);

        Page<ImAnnouncementVO> voPage = new Page<>(announcementPage.getCurrent(), announcementPage.getSize(), announcementPage.getTotal());
        List<ImAnnouncementVO> vos = announcementPage.getRecords().stream()
                .map(a -> convertToVO(a, userId))
                .collect(Collectors.toList());
        voPage.setRecords(vos);

        return voPage;
    }

    @Override
    public List<ImAnnouncementVO> getPinnedAnnouncements() {
        List<ImAnnouncement> announcements = announcementMapper.selectPinnedAnnouncements();
        return announcements.stream()
                .map(a -> convertToVO(a, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<ImAnnouncementVO> getLatestAnnouncements(int limit) {
        List<ImAnnouncement> announcements = announcementMapper.selectLatestAnnouncements(limit);
        return announcements.stream()
                .map(a -> convertToVO(a, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAnnouncement(Long announcementId, Long userId) {
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        if (!"DRAFT".equals(announcement.getStatus())) {
            throw new BusinessException("只能发布草稿状态的公告");
        }

        announcement.setStatus("PUBLISHED");
        announcement.setPublisherId(userId);
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);

        // TODO: 发送通知给目标用户
        log.info("发布公告成功: announcementId={}, publisher={}", announcementId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawAnnouncement(Long announcementId, Long userId) {
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        if (!announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("只有发布人可以撤回公告");
        }

        announcement.setStatus("WITHDRAWN");
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        log.info("撤回公告成功: announcementId={}, operator={}", announcementId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long announcementId, Long userId) {
        // TODO: 记录用户已读状态到数据库
        // 更新公告的已读人数
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement != null) {
            announcement.setReadCount(announcement.getReadCount() + 1);
            announcementMapper.updateById(announcement);
        }
        log.info("标记公告已读: announcementId={}, userId={}", announcementId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        // TODO: 批量标记用户所有未读公告为已读
        log.info("标记所有公告已读: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long announcementId, Long userId) {
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // TODO: 检查用户是否已点赞，然后添加或取消点赞
        // 临时实现：直接增加点赞数
        announcement.setLikeCount(announcement.getLikeCount() + 1);
        announcementMapper.updateById(announcement);
        log.info("点赞公告: announcementId={}, userId={}", announcementId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long announcementId, String content, Long userId) {
        // TODO: 实现评论添加功能
        log.info("添加公告评论: announcementId={}, content={}, userId={}", announcementId, content, userId);
        return System.currentTimeMillis();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        // TODO: 实现评论删除功能
        log.info("删除公告评论: commentId={}, userId={}", commentId, userId);
    }

    @Override
    public Map<String, Object> getAnnouncementStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 总发布数
        Long totalPublished = announcementMapper.countByPublisherId(userId);
        stats.put("totalPublished", totalPublished != null ? totalPublished : 0);

        // 草稿数
        Long draftCount = announcementMapper.selectCount(
                new LambdaQueryWrapper<ImAnnouncement>()
                        .eq(ImAnnouncement::getPublisherId, userId)
                        .eq(ImAnnouncement::getStatus, "DRAFT")
        );
        stats.put("draftCount", draftCount != null ? draftCount : 0);

        // 已发布数
        Long publishedCount = announcementMapper.selectCount(
                new LambdaQueryWrapper<ImAnnouncement>()
                        .eq(ImAnnouncement::getPublisherId, userId)
                        .eq(ImAnnouncement::getStatus, "PUBLISHED")
        );
        stats.put("publishedCount", publishedCount != null ? publishedCount : 0);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getReadUsers(Long announcementId) {
        // TODO: 实现已读用户列表查询
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getLikedUsers(Long announcementId) {
        // TODO: 实现点赞用户列表查询
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPinned(Long announcementId, Boolean pinned, Long userId) {
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        announcement.setIsPinned(pinned);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        log.info("设置公告置顶状态: announcementId={}, pinned={}, operator={}", announcementId, pinned, userId);
    }

    /**
     * 转换为VO
     */
    private ImAnnouncementVO convertToVO(ImAnnouncement announcement, Long userId) {
        ImAnnouncementVO vo = new ImAnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);

        // 设置显示名称
        vo.setAnnouncementTypeDisplay(getTypeDisplay(announcement.getAnnouncementType()));
        vo.setPriorityDisplay(getPriorityDisplay(announcement.getPriority()));
        vo.setStatusDisplay(getStatusDisplay(announcement.getStatus()));

        // 计算阅读进度百分比
        if (announcement.getTotalTargetCount() != null && announcement.getTotalTargetCount() > 0) {
            int readPercent = (int) ((announcement.getReadCount() * 100.0) / announcement.getTotalTargetCount());
            vo.setReadPercent(readPercent);
        }

        // 判断是否过期
        if (announcement.getExpiryTime() != null) {
            vo.setIsExpired(announcement.getExpiryTime().isBefore(LocalDateTime.now()));
        } else {
            vo.setIsExpired(false);
        }

        // TODO: 设置点赞和已读状态
        vo.setIsLiked(false);
        vo.setIsRead(false);

        return vo;
    }

    /**
     * 转换为详情VO
     */
    private ImAnnouncementDetailVO convertToDetailVO(ImAnnouncement announcement, Long userId) {
        ImAnnouncementDetailVO vo = new ImAnnouncementDetailVO();
        BeanUtils.copyProperties(announcement, vo);

        // 设置显示名称
        vo.setAnnouncementTypeDisplay(getTypeDisplay(announcement.getAnnouncementType()));
        vo.setPriorityDisplay(getPriorityDisplay(announcement.getPriority()));
        vo.setStatusDisplay(getStatusDisplay(announcement.getStatus()));
        vo.setTargetTypeDisplay(getTargetTypeDisplay(announcement.getTargetType()));

        // 解析附件
        if (announcement.getAttachments() != null && !announcement.getAttachments().isEmpty()) {
            try {
                List<ImAnnouncementCreateRequest.AttachmentInfo> attachments =
                        JSON.parseArray(announcement.getAttachments(), ImAnnouncementCreateRequest.AttachmentInfo.class);
                // TODO: 转换为详情VO的附件格式
            } catch (Exception e) {
                log.warn("解析附件失败: announcementId={}", announcement.getId());
            }
        }

        // 计算阅读进度百分比
        if (announcement.getTotalTargetCount() != null && announcement.getTotalTargetCount() > 0) {
            int readPercent = (int) ((announcement.getReadCount() * 100.0) / announcement.getTotalTargetCount());
            vo.setReadPercent(readPercent);
        }

        // 判断是否过期
        if (announcement.getExpiryTime() != null) {
            vo.setIsExpired(announcement.getExpiryTime().isBefore(LocalDateTime.now()));
        } else {
            vo.setIsExpired(false);
        }

        // TODO: 设置评论列表、已读用户列表、点赞用户列表
        vo.setComments(new ArrayList<>());
        vo.setReadUsers(new ArrayList<>());
        vo.setLikedUsers(new ArrayList<>());

        return vo;
    }

    private String getTypeDisplay(String type) {
        if (type == null) return "系统公告";
        switch (type) {
            case "SYSTEM": return "系统公告";
            case "DEPARTMENT": return "部门公告";
            case "PROJECT": return "项目公告";
            default: return type;
        }
    }

    private String getPriorityDisplay(Integer priority) {
        if (priority == null) return "普通";
        switch (priority) {
            case 1: return "普通";
            case 2: return "重要";
            case 3: return "紧急";
            default: return "普通";
        }
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "草稿";
        switch (status) {
            case "DRAFT": return "草稿";
            case "PUBLISHED": return "已发布";
            case "EXPIRED": return "已过期";
            case "WITHDRAWN": return "已撤回";
            default: return status;
        }
    }

    private String getTargetTypeDisplay(String targetType) {
        if (targetType == null) return "全部";
        switch (targetType) {
            case "ALL": return "全部";
            case "DEPARTMENT": return "指定部门";
            case "ROLE": return "指定角色";
            case "USER": return "指定用户";
            default: return targetType;
        }
    }
}
