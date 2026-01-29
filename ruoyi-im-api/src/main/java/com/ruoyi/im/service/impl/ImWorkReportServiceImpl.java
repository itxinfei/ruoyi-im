package com.ruoyi.im.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ruoyi.im.constants.StatusConstants;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.domain.ImWorkReport;
import com.ruoyi.im.domain.ImWorkReportComment;
import com.ruoyi.im.domain.ImWorkReportLike;
import com.ruoyi.im.dto.workreport.WorkReportCreateRequest;
import com.ruoyi.im.dto.workreport.WorkReportQueryRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.mapper.ImWorkReportCommentMapper;
import com.ruoyi.im.mapper.ImWorkReportLikeMapper;
import com.ruoyi.im.mapper.ImWorkReportMapper;
import com.ruoyi.im.service.ImWorkReportService;
import com.ruoyi.im.vo.workreport.WorkReportCommentVO;
import com.ruoyi.im.vo.workreport.WorkReportDetailVO;
import com.ruoyi.im.vo.workreport.WorkReportLikeUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作日志服务实现
 */
@Service
public class ImWorkReportServiceImpl implements ImWorkReportService {

    @Autowired
    private ImWorkReportMapper workReportMapper;

    @Autowired
    private ImWorkReportCommentMapper commentMapper;

    @Autowired
    private ImWorkReportLikeMapper likeMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReport(WorkReportCreateRequest request, Long userId) {
        ImWorkReport report = new ImWorkReport();
        BeanUtils.copyProperties(request, report);
        report.setUserId(userId);
        report.setStatus(Boolean.TRUE.equals(request.getIsDraft()) ? "DRAFT" : "SUBMITTED");
        report.setSubmitTime(LocalDateTime.now());
        workReportMapper.insert(report);
        return report.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReport(Long reportId, WorkReportCreateRequest request, Long userId) {
        ImWorkReport report = workReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("工作日志不存在");
        }
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        if ("SUBMITTED".equals(report.getStatus())) {
            throw new BusinessException("已提交的日志不能修改");
        }

        BeanUtils.copyProperties(request, report);
        workReportMapper.updateById(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long reportId, Long userId) {
        ImWorkReport report = workReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("工作日志不存在");
        }
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        if ("SUBMITTED".equals(report.getStatus())) {
            throw new BusinessException("已提交的日志不能删除");
        }
        workReportMapper.deleteById(reportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReport(Long reportId, Long userId) {
        ImWorkReport report = workReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("工作日志不存在");
        }
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        report.setStatus("SUBMITTED");
        report.setSubmitTime(LocalDateTime.now());
        workReportMapper.updateById(report);
    }

    @Override
    public WorkReportDetailVO getReportDetail(Long reportId, Long userId) {
        ImWorkReport report = workReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("工作日志不存在");
        }

        WorkReportDetailVO vo = new WorkReportDetailVO();
        BeanUtils.copyProperties(report, vo);

        // 获取用户信息
        ImUser user = userMapper.selectImUserById(report.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        // 获取审批人信息
        if (report.getApproverId() != null) {
            ImUser approver = userMapper.selectImUserById(report.getApproverId());
            if (approver != null) {
                vo.setApproverName(approver.getNickname());
            }
        }

        // 获取评论列表
        List<ImWorkReportComment> comments = commentMapper.selectByReportId(reportId);
        vo.setCommentCount(comments.size());
        vo.setComments(convertToCommentVOList(comments));

        // 获取点赞列表
        List<ImWorkReportLike> likes = likeMapper.selectByReportId(reportId);
        vo.setLikeCount(likes.size());

        // 检查是否已点赞
        ImWorkReportLike myLike = likeMapper.selectByReportAndUser(reportId, userId);
        vo.setIsLiked(myLike != null);

        // 转换点赞用户列表
        List<WorkReportLikeUserVO> likeUsers = likes.stream()
                .map(like -> {
                    WorkReportLikeUserVO likeUserVO = new WorkReportLikeUserVO();
                    likeUserVO.setUserId(like.getUserId());
                    ImUser likeUser = userMapper.selectImUserById(like.getUserId());
                    if (likeUser != null) {
                        likeUserVO.setUserName(likeUser.getNickname());
                        likeUserVO.setUserAvatar(likeUser.getAvatar());
                    }
                    return likeUserVO;
                })
                .collect(Collectors.toList());
        vo.setLikeUsers(likeUsers);

        // 设置附件列表
        if (StrUtil.isNotBlank(report.getAttachments())) {
            vo.setAttachmentList(Arrays.asList(report.getAttachments().split(",")));
        }

        // 设置类型名称
        vo.setReportTypeName(getReportTypeName(report.getReportType()));
        vo.setCompletionStatusName(getCompletionStatusName(report.getCompletionStatus()));
        vo.setVisibilityName(getVisibilityName(report.getVisibility()));
        vo.setStatusName(getStatusName(report.getStatus()));

        return vo;
    }

    @Override
    public IPage<WorkReportDetailVO> getReportPage(WorkReportQueryRequest request, Long userId) {
        Page<ImWorkReport> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ImWorkReport> reportPage = workReportMapper.selectReportPage(page, request);

        Page<WorkReportDetailVO> resultPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        List<WorkReportDetailVO> voList = reportPage.getRecords().stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public List<WorkReportDetailVO> getMyReports(Long userId) {
        List<ImWorkReport> reports = workReportMapper.selectByUserId(userId);
        return reports.stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long reportId, String content, Long parentId, Long userId) {
        ImWorkReport report = workReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("工作日志不存在");
        }

        ImWorkReportComment comment = new ImWorkReportComment();
        comment.setReportId(reportId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        ImWorkReportComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        commentMapper.deleteById(commentId);
    }

    @Override
    public List<WorkReportCommentVO> getComments(Long reportId) {
        List<ImWorkReportComment> comments = commentMapper.selectByReportId(reportId);
        return convertToCommentVOList(comments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long reportId, Long userId) {
        ImWorkReport report = workReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("工作日志不存在");
        }

        ImWorkReportLike existingLike = likeMapper.selectByReportAndUser(reportId, userId);
        if (existingLike != null) {
            // 取消点赞
            likeMapper.deleteById(existingLike.getId());
            return false;
        } else {
            // 添加点赞
            ImWorkReportLike like = new ImWorkReportLike();
            like.setReportId(reportId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            likeMapper.insert(like);
            return true;
        }
    }

    @Override
    public List<WorkReportLikeUserVO> getLikeUsers(Long reportId) {
        List<ImWorkReportLike> likes = likeMapper.selectByReportId(reportId);
        return likes.stream()
                .map(like -> {
                    WorkReportLikeUserVO vo = new WorkReportLikeUserVO();
                    vo.setUserId(like.getUserId());
                    ImUser user = userMapper.selectImUserById(like.getUserId());
                    if (user != null) {
                        vo.setUserName(user.getNickname());
                        vo.setUserAvatar(user.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveReport(Long reportId, Long userId, Boolean approved, String remark) {
        ImWorkReport report = workReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("工作日志不存在");
        }
        if (!"SUBMITTED".equals(report.getStatus())) {
            throw new BusinessException("只能审批已提交的日志");
        }

        report.setApproverId(userId);
        report.setApproveTime(LocalDateTime.now());
        report.setApproveRemark(remark);
        report.setStatus(approved ? StatusConstants.Approval.APPROVED : StatusConstants.Approval.REJECTED);
        workReportMapper.updateById(report);
    }

    @Override
    public Object getStatistics(Long userId) {
        Integer totalCount = workReportMapper.countByUserId(userId);
        List<ImWorkReport> reports = workReportMapper.selectByUserId(userId);

        int dailyCount = (int) reports.stream().filter(r -> "DAILY".equals(r.getReportType())).count();
        int weeklyCount = (int) reports.stream().filter(r -> "WEEKLY".equals(r.getReportType())).count();
        int monthlyCount = (int) reports.stream().filter(r -> "MONTHLY".equals(r.getReportType())).count();
        int submittedCount = (int) reports.stream().filter(r -> "SUBMITTED".equals(r.getStatus())).count();
        int draftCount = (int) reports.stream().filter(r -> "DRAFT".equals(r.getStatus())).count();
        int approvedCount = (int) reports.stream().filter(r -> "APPROVED".equals(r.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("dailyCount", dailyCount);
        stats.put("weeklyCount", weeklyCount);
        stats.put("monthlyCount", monthlyCount);
        stats.put("submittedCount", submittedCount);
        stats.put("draftCount", draftCount);
        stats.put("approvedCount", approvedCount);

        return stats;
    }

    /**
     * 转换为详情VO
     */
    private WorkReportDetailVO convertToDetailVO(ImWorkReport report) {
        WorkReportDetailVO vo = new WorkReportDetailVO();
        BeanUtils.copyProperties(report, vo);

        ImUser user = userMapper.selectImUserById(report.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        vo.setCommentCount(commentMapper.countByReportId(report.getId()));
        vo.setLikeCount(likeMapper.countByReportId(report.getId()));

        if (StrUtil.isNotBlank(report.getAttachments())) {
            vo.setAttachmentList(Arrays.asList(report.getAttachments().split(",")));
        }

        vo.setReportTypeName(getReportTypeName(report.getReportType()));
        vo.setCompletionStatusName(getCompletionStatusName(report.getCompletionStatus()));
        vo.setVisibilityName(getVisibilityName(report.getVisibility()));
        vo.setStatusName(getStatusName(report.getStatus()));

        return vo;
    }

    /**
     * 转换评论列表
     */
    private List<WorkReportCommentVO> convertToCommentVOList(List<ImWorkReportComment> comments) {
        if (CollUtil.isEmpty(comments)) {
            return new ArrayList<>();
        }

        return comments.stream()
                .map(comment -> {
                    WorkReportCommentVO vo = new WorkReportCommentVO();
                    BeanUtils.copyProperties(comment, vo);
                    vo.setUserName(comment.getUserName());
                    vo.setUserAvatar(comment.getUserAvatar());

                    // 如果有父评论，查找父评论内容
                    if (comment.getParentId() != null) {
                        ImWorkReportComment parent = commentMapper.selectById(comment.getParentId());
                        if (parent != null) {
                            vo.setParentContent(parent.getContent());
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    private String getReportTypeName(String type) {
        if ("DAILY".equals(type)) {
            return "日报";
        } else if ("WEEKLY".equals(type)) {
            return "周报";
        } else if ("MONTHLY".equals(type)) {
            return "月报";
        }
        return type;
    }

    private String getCompletionStatusName(String status) {
        if (StatusConstants.Task.COMPLETED.equals(status)) {
            return "已完成";
        } else if (StatusConstants.Task.IN_PROGRESS.equals(status)) {
            return "进行中";
        } else if (StatusConstants.Approval.PENDING.equals(status)) {
            return "待处理";
        }
        return status;
    }

    private String getVisibilityName(String visibility) {
        if ("PRIVATE".equals(visibility)) {
            return "私有";
        } else if ("DEPARTMENT".equals(visibility)) {
            return "部门";
        } else if ("PUBLIC".equals(visibility)) {
            return "公开";
        }
        return visibility;
    }

    private String getStatusName(String status) {
        if ("DRAFT".equals(status)) {
            return "草稿";
        } else if ("SUBMITTED".equals(status)) {
            return "已提交";
        } else if (StatusConstants.Approval.APPROVED.equals(status)) {
            return "已审批";
        } else if (StatusConstants.Approval.REJECTED.equals(status)) {
            return "已退回";
        }
        return status;
    }
}
