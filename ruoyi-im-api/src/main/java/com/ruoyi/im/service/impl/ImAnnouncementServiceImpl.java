package com.ruoyi.im.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constant.StatusConstants;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImAnnouncement;
import com.ruoyi.im.domain.ImAnnouncementComment;
import com.ruoyi.im.domain.ImAnnouncementLike;
import com.ruoyi.im.domain.ImAnnouncementRead;
import com.ruoyi.im.dto.announcement.ImAnnouncementCreateRequest;
import com.ruoyi.im.dto.announcement.ImAnnouncementQueryRequest;
import com.ruoyi.im.dto.announcement.ImAnnouncementUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImAnnouncementCommentMapper;
import com.ruoyi.im.mapper.ImAnnouncementLikeMapper;
import com.ruoyi.im.mapper.ImAnnouncementMapper;
import com.ruoyi.im.mapper.ImAnnouncementReadMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImAnnouncementService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO;
import com.ruoyi.im.vo.announcement.ImAnnouncementVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.im.util.BeanConvertUtil;
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

    private final ImAnnouncementMapper announcementMapper;
    private final ImAnnouncementReadMapper announcementReadMapper;
    private final ImAnnouncementLikeMapper announcementLikeMapper;
    private final ImAnnouncementCommentMapper announcementCommentMapper;
    private final ImWebSocketBroadcastService broadcastService;
    private final ImUserMapper userMapper;

    /**
     * 构造器注入依赖
     */
    public ImAnnouncementServiceImpl(ImAnnouncementMapper announcementMapper,
                                     ImAnnouncementReadMapper announcementReadMapper,
                                     ImAnnouncementLikeMapper announcementLikeMapper,
                                     ImAnnouncementCommentMapper announcementCommentMapper,
                                     ImWebSocketBroadcastService broadcastService,
                                     ImUserMapper userMapper) {
        this.announcementMapper = announcementMapper;
        this.announcementReadMapper = announcementReadMapper;
        this.announcementLikeMapper = announcementLikeMapper;
        this.announcementCommentMapper = announcementCommentMapper;
        this.broadcastService = broadcastService;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAnnouncement(ImAnnouncementCreateRequest request, Long userId) {
        ImAnnouncement announcement = new ImAnnouncement();
        BeanConvertUtil.copyProperties(request, announcement);

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
        if (!StatusConstants.AnnouncementStatus.DRAFT.equals(announcement.getStatus())
                && !StatusConstants.AnnouncementStatus.WITHDRAWN.equals(announcement.getStatus())) {
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

        // 记录用户已读状态
        ImAnnouncementRead existRead = announcementReadMapper.selectByAnnouncementIdAndUserId(announcementId, userId);
        if (existRead == null) {
            ImAnnouncementRead announcementRead = new ImAnnouncementRead();
            announcementRead.setAnnouncementId(announcementId);
            announcementRead.setUserId(userId);
            announcementRead.setReadTime(LocalDateTime.now());
            announcementReadMapper.insert(announcementRead);

            // 更新公告的已读人数
            announcement.setReadCount(announcement.getReadCount() + 1);
            announcementMapper.updateById(announcement);
        }

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

        announcement.setStatus(StatusConstants.AnnouncementStatus.PUBLISHED);
        announcement.setPublisherId(userId);
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);

        // 发送通知给目标用户
        Set<Long> targetUserIds = resolveTargetUserIds(announcement);
        if (!targetUserIds.isEmpty()) {
            broadcastService.broadcastAnnouncement(announcementId, targetUserIds);
        }

        log.info("发布公告成功: announcementId={}, publisher={}, targetCount={}",
                announcementId, userId, targetUserIds.size());
    }

    /**
     * 解析公告目标用户ID列表
     *
     * @param announcement 公告实体
     * @return 目标用户ID集合
     */
    private Set<Long> resolveTargetUserIds(ImAnnouncement announcement) {
        Set<Long> targetUserIds = new HashSet<>();

        String targetType = announcement.getTargetType();
        if ("ALL".equals(targetType)) {
            // 全员公告 - 获取所有有效用户
            List<ImUser> allUsers = userMapper.selectImUserList(new ImUser());
            for (ImUser user : allUsers) {
                targetUserIds.add(user.getId());
            }
        } else if (announcement.getTargetIds() != null && !announcement.getTargetIds().isEmpty()) {
            try {
                // 解析目标ID列表
                List<Long> ids = JSON.parseArray(announcement.getTargetIds(), Long.class);
                targetUserIds.addAll(ids);
            } catch (Exception e) {
                log.warn("解析公告目标ID失败: announcementId={}", announcement.getId(), e);
            }
        }

        return targetUserIds;
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

        announcement.setStatus(StatusConstants.AnnouncementStatus.WITHDRAWN);
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        log.info("撤回公告成功: announcementId={}, operator={}", announcementId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long announcementId, Long userId) {
        // 检查是否已读
        ImAnnouncementRead existRead = announcementReadMapper.selectByAnnouncementIdAndUserId(announcementId, userId);
        if (existRead != null) {
            return; // 已读，无需重复处理
        }

        // 记录已读状态
        ImAnnouncementRead announcementRead = new ImAnnouncementRead();
        announcementRead.setAnnouncementId(announcementId);
        announcementRead.setUserId(userId);
        announcementRead.setReadTime(LocalDateTime.now());
        announcementReadMapper.insert(announcementRead);

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
        // 查询所有已发布的公告
        LambdaQueryWrapper<ImAnnouncement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImAnnouncement::getStatus, StatusConstants.AnnouncementStatus.PUBLISHED);
        List<ImAnnouncement> announcements = announcementMapper.selectList(queryWrapper);

        // 批量插入已读记录
        List<Long> readAnnouncementIds = announcementReadMapper.selectReadAnnouncementIdsByUserId(userId);
        for (ImAnnouncement announcement : announcements) {
            if (!readAnnouncementIds.contains(announcement.getId())) {
                ImAnnouncementRead announcementRead = new ImAnnouncementRead();
                announcementRead.setAnnouncementId(announcement.getId());
                announcementRead.setUserId(userId);
                announcementRead.setReadTime(LocalDateTime.now());
                announcementReadMapper.insert(announcementRead);

                // 更新公告的已读人数
                announcement.setReadCount(announcement.getReadCount() + 1);
                announcementMapper.updateById(announcement);
            }
        }
        log.info("标记所有公告已读: userId={}, count={}", userId, announcements.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long announcementId, Long userId) {
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 检查是否已点赞
        ImAnnouncementLike existLike = announcementLikeMapper.selectByAnnouncementIdAndUserId(announcementId, userId);
        if (existLike != null) {
            // 已点赞，取消点赞
            announcementLikeMapper.deleteById(existLike.getId());
            announcement.setLikeCount(Math.max(0, announcement.getLikeCount() - 1));
            announcementMapper.updateById(announcement);
            log.info("取消点赞公告: announcementId={}, userId={}", announcementId, userId);
        } else {
            // 未点赞，添加点赞
            ImAnnouncementLike announcementLike = new ImAnnouncementLike();
            announcementLike.setAnnouncementId(announcementId);
            announcementLike.setUserId(userId);
            announcementLike.setLikeTime(LocalDateTime.now());
            announcementLikeMapper.insert(announcementLike);

            announcement.setLikeCount(announcement.getLikeCount() + 1);
            announcementMapper.updateById(announcement);
            log.info("点赞公告: announcementId={}, userId={}", announcementId, userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long announcementId, String content, Long userId) {
        // 检查公告是否存在
        ImAnnouncement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 检查是否允许评论
        if (announcement.getAllowComment() != null && !announcement.getAllowComment()) {
            throw new BusinessException("该公告不允许评论");
        }

        // 创建评论
        ImAnnouncementComment comment = new ImAnnouncementComment();
        comment.setAnnouncementId(announcementId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(0L); // 一级评论
        comment.setCreateTime(LocalDateTime.now());
        comment.setIsDeleted(0);
        announcementCommentMapper.insert(comment);

        // 更新公告的评论数
        announcement.setCommentCount(announcement.getCommentCount() + 1);
        announcementMapper.updateById(announcement);

        log.info("添加公告评论: announcementId={}, content={}, userId={}", announcementId, content, userId);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        // 查询评论
        ImAnnouncementComment comment = announcementCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 只有评论作者或公告发布人可以删除评论
        ImAnnouncement announcement = announcementMapper.selectById(comment.getAnnouncementId());
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        if (!comment.getUserId().equals(userId) && !announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("只有评论作者或公告发布人可以删除评论");
        }

        // 软删除评论
        comment.setIsDeleted(1);
        announcementCommentMapper.updateById(comment);

        // 更新公告的评论数
        announcement.setCommentCount(Math.max(0, announcement.getCommentCount() - 1));
        announcementMapper.updateById(announcement);

        log.info("删除公告评论: commentId={}, userId={}", commentId, userId);
    }

    @Override
    public Map<String, Object> getAnnouncementStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 总发布数
        int totalPublished = announcementMapper.countByPublisherId(userId);
        stats.put("totalPublished", totalPublished);

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
                        .eq(ImAnnouncement::getStatus, StatusConstants.AnnouncementStatus.PUBLISHED)
        );
        stats.put("publishedCount", publishedCount != null ? publishedCount : 0);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getReadUsers(Long announcementId) {
        List<ImAnnouncementRead> readRecords = announcementReadMapper.selectByAnnouncementId(announcementId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ImAnnouncementRead record : readRecords) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", record.getUserId());
            userMap.put("userNickname", record.getUserNickname());
            userMap.put("userAvatar", record.getUserAvatar());
            userMap.put("readTime", record.getReadTime());
            result.add(userMap);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getLikedUsers(Long announcementId) {
        List<ImAnnouncementLike> likeRecords = announcementLikeMapper.selectByAnnouncementId(announcementId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ImAnnouncementLike record : likeRecords) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", record.getUserId());
            userMap.put("userNickname", record.getUserNickname());
            userMap.put("userAvatar", record.getUserAvatar());
            userMap.put("likeTime", record.getLikeTime());
            result.add(userMap);
        }
        return result;
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
        BeanConvertUtil.copyProperties(announcement, vo);

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

        // 设置点赞和已读状态
        if (userId != null) {
            // 检查是否已点赞
            ImAnnouncementLike likeRecord = announcementLikeMapper.selectByAnnouncementIdAndUserId(announcement.getId(), userId);
            vo.setIsLiked(likeRecord != null);

            // 检查是否已读
            ImAnnouncementRead readRecord = announcementReadMapper.selectByAnnouncementIdAndUserId(announcement.getId(), userId);
            vo.setIsRead(readRecord != null);
        } else {
            vo.setIsLiked(false);
            vo.setIsRead(false);
        }

        return vo;
    }

    /**
     * 转换为详情VO
     */
    private ImAnnouncementDetailVO convertToDetailVO(ImAnnouncement announcement, Long userId) {
        ImAnnouncementDetailVO vo = new ImAnnouncementDetailVO();
        BeanConvertUtil.copyProperties(announcement, vo);

        // 设置显示名称
        vo.setAnnouncementTypeDisplay(getTypeDisplay(announcement.getAnnouncementType()));
        vo.setPriorityDisplay(getPriorityDisplay(announcement.getPriority()));
        vo.setStatusDisplay(getStatusDisplay(announcement.getStatus()));
        vo.setTargetTypeDisplay(getTargetTypeDisplay(announcement.getTargetType()));

        // 解析附件
        if (announcement.getAttachments() != null && !announcement.getAttachments().isEmpty()) {
            try {
                List<ImAnnouncementCreateRequest.AttachmentInfo> attachmentInfos =
                        JSON.parseArray(announcement.getAttachments(), ImAnnouncementCreateRequest.AttachmentInfo.class);
                List<ImAnnouncementDetailVO.AnnouncementAttachment> attachmentVOs = new ArrayList<>();
                for (ImAnnouncementCreateRequest.AttachmentInfo info : attachmentInfos) {
                    ImAnnouncementDetailVO.AnnouncementAttachment attachmentVO =
                            new ImAnnouncementDetailVO.AnnouncementAttachment();
                    attachmentVO.setId(info.getId() != null ? info.getId() : 0L);
                    attachmentVO.setName(info.getName());
                    attachmentVO.setUrl(info.getUrl());
                    attachmentVO.setSize(info.getSize() != null ? info.getSize() : 0L);
                    attachmentVO.setSizeDisplay(formatFileSize(info.getSize()));
                    attachmentVO.setFileType(info.getFileType());
                    attachmentVO.setIsImage(isImageFile(info.getFileType()));
                    attachmentVOs.add(attachmentVO);
                }
                vo.setAttachments(attachmentVOs);
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

        // 设置评论列表、已读用户列表、点赞用户列表
        // 获取评论列表
        List<ImAnnouncementComment> comments = announcementCommentMapper.selectByAnnouncementId(announcement.getId());
        List<com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.AnnouncementComment> commentList = new ArrayList<>();
        for (ImAnnouncementComment comment : comments) {
            com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.AnnouncementComment commentVO =
                new com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.AnnouncementComment();
            commentVO.setId(comment.getId());
            commentVO.setContent(comment.getContent());
            commentVO.setCommentatorId(comment.getUserId());
            commentVO.setCommentatorName(comment.getUserNickname());
            commentVO.setCommentatorAvatar(comment.getUserAvatar());
            commentVO.setCommentTime(comment.getCreateTime());
            commentList.add(commentVO);
        }
        vo.setComments(commentList);

        // 获取已读用户列表（限制前100个）
        List<ImAnnouncementRead> readRecords = announcementReadMapper.selectByAnnouncementId(announcement.getId());
        List<com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.ReadUser> readUserList = new ArrayList<>();
        for (ImAnnouncementRead record : readRecords) {
            com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.ReadUser readUser =
                new com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.ReadUser();
            readUser.setUserId(record.getUserId());
            readUser.setUserName(record.getUserNickname());
            readUser.setUserAvatar(record.getUserAvatar());
            readUser.setReadTime(record.getReadTime());
            readUserList.add(readUser);
        }
        vo.setReadUsers(readUserList);

        // 获取点赞用户列表（限制前100个）
        List<ImAnnouncementLike> likeRecords = announcementLikeMapper.selectByAnnouncementId(announcement.getId());
        List<com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.LikedUser> likedUserList = new ArrayList<>();
        for (ImAnnouncementLike record : likeRecords) {
            com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.LikedUser likedUser =
                new com.ruoyi.im.vo.announcement.ImAnnouncementDetailVO.LikedUser();
            likedUser.setUserId(record.getUserId());
            likedUser.setUserName(record.getUserNickname());
            likedUser.setUserAvatar(record.getUserAvatar());
            likedUser.setLikeTime(record.getLikeTime());
            likedUserList.add(likedUser);
        }
        vo.setLikedUsers(likedUserList);

        return vo;
    }

    private String getTypeDisplay(String type) {
        if (type == null) return "系统公告";
        switch (type) {
            case StatusConstants.AnnouncementType.SYSTEM: return "系统公告";
            case StatusConstants.AnnouncementType.DEPARTMENT: return "部门公告";
            case StatusConstants.AnnouncementType.PROJECT: return "项目公告";
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
            case StatusConstants.AnnouncementStatus.PUBLISHED: return "已发布";
            case StatusConstants.AnnouncementStatus.EXPIRED: return "已过期";
            case StatusConstants.AnnouncementStatus.WITHDRAWN: return "已撤回";
            default: return status;
        }
    }

    private String getTargetTypeDisplay(String targetType) {
        if (targetType == null) return "全部";
        switch (targetType) {
            case "ALL": return "全部";
            case StatusConstants.AnnouncementType.DEPARTMENT: return "指定部门";
            case "ROLE": return "指定角色";
            case "USER": return "指定用户";
            default: return targetType;
        }
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log10(size) / Math.log10(1024));
        double unitSize = size / Math.pow(1024, unitIndex);
        return String.format("%.1f %s", unitSize, units[unitIndex]);
    }

    /**
     * 判断是否为图片文件
     *
     * @param fileType 文件类型
     * @return 是否为图片
     */
    private boolean isImageFile(String fileType) {
        if (fileType == null) {
            return false;
        }
        String upperType = fileType.toUpperCase();
        return upperType.startsWith("IMAGE/") ||
                "JPG".equals(upperType) ||
                "JPEG".equals(upperType) ||
                "PNG".equals(upperType) ||
                "GIF".equals(upperType) ||
                "BMP".equals(upperType) ||
                "WEBP".equals(upperType);
    }
}
