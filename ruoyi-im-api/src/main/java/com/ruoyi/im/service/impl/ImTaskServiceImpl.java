package com.ruoyi.im.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImTask;
import com.ruoyi.im.domain.ImTaskComment;
import com.ruoyi.im.domain.ImTaskAttachment;
import com.ruoyi.im.dto.task.ImTaskCreateRequest;
import com.ruoyi.im.dto.task.ImTaskQueryRequest;
import com.ruoyi.im.dto.task.ImTaskUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImTaskMapper;
import com.ruoyi.im.mapper.ImTaskCommentMapper;
import com.ruoyi.im.mapper.ImTaskAttachmentMapper;
import com.ruoyi.im.service.ImTaskService;
import com.ruoyi.im.vo.task.ImTaskDetailVO;
import com.ruoyi.im.vo.task.ImTaskVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务管理服务实现
 *
 * @author ruoyi
 */
@Service
public class ImTaskServiceImpl implements ImTaskService {

    private static final Logger log = LoggerFactory.getLogger(ImTaskServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ImTaskMapper taskMapper;

    @Autowired
    private ImTaskCommentMapper taskCommentMapper;

    @Autowired
    private ImTaskAttachmentMapper taskAttachmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(ImTaskCreateRequest request, Long userId) {
        ImTask task = new ImTask();
        BeanUtils.copyProperties(request, task);

        // 生成任务编号
        task.setTaskNumber(generateTaskNumber());
        task.setCreatorId(userId);
        task.setStatus("PENDING");
        task.setCompletionPercent(0);
        task.setHasSubtask(false);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        // 处理标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            task.setTags(String.join(",", request.getTags()));
        }

        // 处理关注人
        if (request.getFollowerIds() != null && !request.getFollowerIds().isEmpty()) {
            task.setFollowers(JSON.toJSONString(request.getFollowerIds()));
        }

        // 如果是子任务，更新父任务的hasSubtask标记
        if (request.getParentId() != null) {
            ImTask parentTask = taskMapper.selectById(request.getParentId());
            if (parentTask != null) {
                parentTask.setHasSubtask(true);
                parentTask.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(parentTask);
            }
        }

        taskMapper.insert(task);
        log.info("创建任务成功: taskId={}, title={}, creator={}", task.getId(), task.getTitle(), userId);
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(ImTaskUpdateRequest request, Long userId) {
        ImTask task = taskMapper.selectById(request.getId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 更新字段
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if ("COMPLETED".equals(request.getStatus()) && task.getCompletedTime() == null) {
                task.setCompletedTime(LocalDateTime.now());
                task.setCompletionPercent(100);
            }
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }
        if (request.getCompletionPercent() != null) {
            task.setCompletionPercent(request.getCompletionPercent());
            if (request.getCompletionPercent() == 100 && "COMPLETED".equals(task.getStatus())) {
                task.setCompletedTime(LocalDateTime.now());
            }
        }
        if (request.getRemindTime() != null) {
            task.setRemindTime(request.getRemindTime());
        }
        if (request.getRemindType() != null) {
            task.setRemindType(request.getRemindType());
        }
        if (request.getRepeatType() != null) {
            task.setRepeatType(request.getRepeatType());
        }
        if (request.getRemark() != null) {
            task.setRemark(request.getRemark());
        }

        // 处理标签
        if (request.getTags() != null) {
            task.setTags(String.join(",", request.getTags()));
        }

        // 处理关注人
        if (request.getFollowerIds() != null) {
            task.setFollowers(JSON.toJSONString(request.getFollowerIds()));
        }

        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        log.info("更新任务成功: taskId={}, operator={}", request.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId, Long userId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 检查权限
        if (!task.getCreatorId().equals(userId) && !task.getAssigneeId().equals(userId)) {
            throw new BusinessException("无权限删除此任务");
        }

        // 如果有子任务，先删除子任务
        if (task.getHasSubtask()) {
            List<ImTask> subtasks = taskMapper.selectSubtasks(taskId);
            for (ImTask subtask : subtasks) {
                taskMapper.deleteById(subtask.getId());
            }
        }

        taskMapper.deleteById(taskId);
        log.info("删除任务成功: taskId={}, operator={}", taskId, userId);
    }

    @Override
    public ImTaskDetailVO getTaskDetail(Long taskId, Long userId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        ImTaskDetailVO detailVO = convertToDetailVO(task);

        // 获取子任务
        if (task.getHasSubtask()) {
            List<ImTask> subtasks = taskMapper.selectSubtasks(taskId);
            List<ImTaskVO> subtaskVOs = subtasks.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            detailVO.setSubtasks(subtaskVOs);
        }

        // 获取评论
        List<Map<String, Object>> comments = getComments(taskId);
        detailVO.setComments(convertToCommentVO(comments));

        // 获取附件
        List<ImTaskAttachment> attachments = taskAttachmentMapper.selectByTaskId(taskId);
        detailVO.setAttachments(convertToAttachmentVO(attachments));

        return detailVO;
    }

    @Override
    public IPage<ImTaskVO> getTaskPage(ImTaskQueryRequest request, Long userId) {
        Page<ImTask> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ImTask> taskPage = taskMapper.selectTaskPage(page, request);

        Page<ImTaskVO> voPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        List<ImTaskVO> vos = taskPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(vos);

        return voPage;
    }

    @Override
    public List<ImTaskVO> getMyTasks(Long userId, String status) {
        List<ImTask> tasks = taskMapper.selectByAssigneeId(userId, status);
        return tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImTaskVO> getMyCreatedTasks(Long userId) {
        List<ImTask> tasks = taskMapper.selectByCreatorId(userId);
        return tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTask(Long taskId, Long assigneeId, Long userId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        task.setAssigneeId(assigneeId);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
        log.info("分配任务成功: taskId={}, assigneeId={}, operator={}", taskId, assigneeId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStatus(Long taskId, String status, Long userId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        task.setStatus(status);
        task.setUpdateTime(LocalDateTime.now());
        if ("COMPLETED".equals(status)) {
            task.setCompletedTime(LocalDateTime.now());
            task.setCompletionPercent(100);
        }
        taskMapper.updateById(task);
        log.info("更新任务状态成功: taskId={}, status={}, operator={}", taskId, status, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long taskId, Integer percent, Long userId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        task.setCompletionPercent(percent);
        task.setUpdateTime(LocalDateTime.now());
        if (percent >= 100) {
            task.setStatus("COMPLETED");
            task.setCompletedTime(LocalDateTime.now());
        }
        taskMapper.updateById(task);
        log.info("更新任务进度成功: taskId={}, percent={}, operator={}", taskId, percent, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFollow(Long taskId, Long userId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        Set<Long> followers = new HashSet<>();
        if (task.getFollowers() != null && !task.getFollowers().isEmpty()) {
            try {
                List<Long> list = JSON.parseArray(task.getFollowers(), Long.class);
                followers = new HashSet<>(list);
            } catch (Exception e) {
                log.warn("解析关注人失败: taskId={}", taskId);
            }
        }

        if (followers.contains(userId)) {
            followers.remove(userId);
        } else {
            followers.add(userId);
        }

        task.setFollowers(JSON.toJSONString(followers));
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long taskId, String content, Long replyToId, Long userId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        ImTaskComment comment = new ImTaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setReplyToUserId(replyToId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setIsDeleted(0);

        taskCommentMapper.insert(comment);
        log.info("添加任务评论成功: commentId={}, taskId={}, userId={}", comment.getId(), taskId, userId);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        ImTaskComment comment = taskCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 只有评论作者可以删除
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此评论");
        }

        taskCommentMapper.softDelete(commentId, userId);
        log.info("删除任务评论成功: commentId={}, userId={}", commentId, userId);
    }

    @Override
    public List<Map<String, Object>> getComments(Long taskId) {
        List<ImTaskComment> comments = taskCommentMapper.selectByTaskId(taskId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ImTaskComment comment : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("commentatorId", comment.getUserId());
            map.put("commentatorName", comment.getUserNickname());
            map.put("commentatorAvatar", comment.getUserAvatar());
            map.put("replyToUserId", comment.getReplyToUserId());
            map.put("replyToUserNickname", comment.getReplyToUserNickname());
            map.put("commentTime", comment.getCreateTime());
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAttachment(Long taskId, String fileName, String fileUrl, Long fileSize, Long userId) {
        ImTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        ImTaskAttachment attachment = new ImTaskAttachment();
        attachment.setTaskId(taskId);
        attachment.setFileName(fileName);
        attachment.setFileUrl(fileUrl);
        attachment.setFileSize(fileSize);
        attachment.setUploadUserId(userId);
        attachment.setUploadTime(LocalDateTime.now());

        taskAttachmentMapper.insert(attachment);
        log.info("添加任务附件成功: attachmentId={}, taskId={}, fileName={}", attachment.getId(), taskId, fileName);
        return attachment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long attachmentId, Long userId) {
        ImTaskAttachment attachment = taskAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }

        // 只有上传者可以删除
        if (!attachment.getUploadUserId().equals(userId)) {
            throw new BusinessException("无权限删除此附件");
        }

        taskAttachmentMapper.deleteById(attachmentId);
        log.info("删除任务附件成功: attachmentId={}, userId={}", attachmentId, userId);
    }

    @Override
    public Map<String, Object> getTaskStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 待办任务数
        int pendingCount = taskMapper.selectByAssigneeId(userId, "PENDING").size();
        stats.put("pendingCount", pendingCount);

        // 进行中任务数
        int inProgressCount = taskMapper.selectByAssigneeId(userId, "IN_PROGRESS").size();
        stats.put("inProgressCount", inProgressCount);

        // 已完成任务数
        int completedCount = taskMapper.selectByAssigneeId(userId, "COMPLETED").size();
        stats.put("completedCount", completedCount);

        // 未完成任务总数
        int uncompletedCount = taskMapper.countUncompletedByAssignee(userId);
        stats.put("uncompletedCount", uncompletedCount);

        // 逾期任务数
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        int overdueCount = taskMapper.countOverdueByAssignee(userId, currentDate);
        stats.put("overdueCount", overdueCount);

        // 我创建的任务数
        int myCreatedCount = taskMapper.selectByCreatorId(userId).size();
        stats.put("myCreatedCount", myCreatedCount);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> taskIds, Long userId) {
        for (Long taskId : taskIds) {
            try {
                deleteTask(taskId, userId);
            } catch (Exception e) {
                log.warn("批量删除任务失败: taskId={}", taskId, e);
            }
        }
        log.info("批量删除任务成功: count={}, operator={}", taskIds.size(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> taskIds, String status, Long userId) {
        for (Long taskId : taskIds) {
            try {
                updateTaskStatus(taskId, status, userId);
            } catch (Exception e) {
                log.warn("批量更新任务状态失败: taskId={}", taskId, e);
            }
        }
        log.info("批量更新任务状态成功: count={}, status={}, operator={}", taskIds.size(), status, userId);
    }

    @Override
    public List<ImTaskVO> getSubtasks(Long parentId, Long userId) {
        List<ImTask> subtasks = taskMapper.selectSubtasks(parentId);
        return subtasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyTask(Long taskId, Long userId) {
        ImTask sourceTask = taskMapper.selectById(taskId);
        if (sourceTask == null) {
            throw new BusinessException("任务不存在");
        }

        ImTask newTask = new ImTask();
        BeanUtils.copyProperties(sourceTask, newTask);
        newTask.setId(null);
        newTask.setTaskNumber(generateTaskNumber());
        newTask.setTitle(sourceTask.getTitle() + " (副本)");
        newTask.setCreatorId(userId);
        newTask.setStatus("PENDING");
        newTask.setCompletionPercent(0);
        newTask.setCompletedTime(null);
        newTask.setCreateTime(LocalDateTime.now());
        newTask.setUpdateTime(LocalDateTime.now());

        taskMapper.insert(newTask);
        log.info("复制任务成功: sourceTaskId={}, newTaskId={}, operator={}", taskId, newTask.getId(), userId);
        return newTask.getId();
    }

    /**
     * 生成任务编号
     */
    private String generateTaskNumber() {
        return "TASK-" + System.currentTimeMillis();
    }

    /**
     * 转换为VO
     */
    private ImTaskVO convertToVO(ImTask task) {
        ImTaskVO vo = new ImTaskVO();
        BeanUtils.copyProperties(task, vo);

        // 设置显示名称
        vo.setPriorityDisplay(getPriorityDisplay(task.getPriority()));
        vo.setStatusDisplay(getStatusDisplay(task.getStatus()));
        vo.setTaskTypeDisplay(getTaskTypeDisplay(task.getTaskType()));

        // 解析标签
        if (task.getTags() != null && !task.getTags().isEmpty()) {
            vo.setTags(Arrays.asList(task.getTags().split(",")));
        }

        // 计算是否逾期
        if (task.getDueDate() != null && !"COMPLETED".equals(task.getStatus())) {
            boolean isOverdue = task.getDueDate().isBefore(LocalDate.now());
            vo.setIsOverdue(isOverdue);
            vo.setRemainingDays((int) (task.getDueDate().toEpochDay() - LocalDate.now().toEpochDay()));
        }

        // 设置子任务数量
        if (task.getHasSubtask()) {
            List<ImTask> subtasks = taskMapper.selectSubtasks(task.getId());
            vo.setSubtaskCount(subtasks.size());
            long completedCount = subtasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
            vo.setCompletedSubtaskCount((int) completedCount);
        }

        vo.setCommentCount(taskCommentMapper.countByTaskId(task.getId()));
        vo.setAttachmentCount(taskAttachmentMapper.countByTaskId(task.getId()));

        return vo;
    }

    /**
     * 转换为详情VO
     */
    private ImTaskDetailVO convertToDetailVO(ImTask task) {
        ImTaskDetailVO vo = new ImTaskDetailVO();
        BeanUtils.copyProperties(task, vo);

        // 设置显示名称
        vo.setPriorityDisplay(getPriorityDisplay(task.getPriority()));
        vo.setStatusDisplay(getStatusDisplay(task.getStatus()));
        vo.setTaskTypeDisplay(getTaskTypeDisplay(task.getTaskType()));
        vo.setRemindTypeDisplay(getRemindTypeDisplay(task.getRemindType()));

        // 解析标签
        if (task.getTags() != null && !task.getTags().isEmpty()) {
            vo.setTags(Arrays.asList(task.getTags().split(",")));
        }

        // 解析关注人
        if (task.getFollowers() != null && !task.getFollowers().isEmpty()) {
            try {
                List<Long> followerIds = JSON.parseArray(task.getFollowers(), Long.class);
                // TODO: 设置关注人详情
            } catch (Exception e) {
                log.warn("解析关注人失败: taskId={}", task.getId());
            }
        }

        // 计算是否逾期
        if (task.getDueDate() != null && !"COMPLETED".equals(task.getStatus())) {
            boolean isOverdue = task.getDueDate().isBefore(LocalDate.now());
            vo.setIsOverdue(isOverdue);
            vo.setRemainingDays((int) (task.getDueDate().toEpochDay() - LocalDate.now().toEpochDay()));
        }

        // 设置子任务数量
        if (task.getHasSubtask()) {
            List<ImTask> subtasks = taskMapper.selectSubtasks(task.getId());
            long completedCount = subtasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
            vo.setSubtaskCount(subtasks.size());
            vo.setCompletedSubtaskCount((int) completedCount);
        }

        vo.setCommentCount(taskCommentMapper.countByTaskId(task.getId()));
        vo.setAttachmentCount(taskAttachmentMapper.countByTaskId(task.getId()));

        return vo;
    }

    /**
     * 转换为评论VO
     */
    private List<ImTaskDetailVO.TaskComment> convertToCommentVO(List<Map<String, Object>> comments) {
        return comments.stream().map(comment -> {
            ImTaskDetailVO.TaskComment commentVO = new ImTaskDetailVO.TaskComment();
            commentVO.setId(((Number) comment.get("id")).longValue());
            commentVO.setContent((String) comment.get("content"));
            commentVO.setCommentatorId(((Number) comment.get("commentatorId")).longValue());
            commentVO.setCommentatorName((String) comment.get("commentatorName"));
            commentVO.setCommentTime((LocalDateTime) comment.get("commentTime"));
            return commentVO;
        }).collect(Collectors.toList());
    }

    /**
     * 转换为附件VO
     */
    private List<ImTaskDetailVO.TaskAttachment> convertToAttachmentVO(List<ImTaskAttachment> attachments) {
        return attachments.stream().map(attachment -> {
            ImTaskDetailVO.TaskAttachment attachmentVO = new ImTaskDetailVO.TaskAttachment();
            attachmentVO.setId(attachment.getId());
            attachmentVO.setName(attachment.getFileName());
            attachmentVO.setUrl(attachment.getFileUrl());
            attachmentVO.setSize(attachment.getFileSize());
            attachmentVO.setFileType(attachment.getFileType());
            attachmentVO.setUploaderId(attachment.getUploadUserId());
            attachmentVO.setUploadTime(attachment.getUploadTime());
            attachmentVO.setUploaderName(attachment.getUploaderName());
            return attachmentVO;
        }).collect(Collectors.toList());
    }

    private String getPriorityDisplay(Integer priority) {
        if (priority == null) return "中";
        switch (priority) {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            case 4: return "紧急";
            default: return "中";
        }
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "待办";
        switch (status) {
            case "PENDING": return "待办";
            case "IN_PROGRESS": return "进行中";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            case "BLOCKED": return "阻塞";
            default: return status;
        }
    }

    private String getTaskTypeDisplay(String taskType) {
        if (taskType == null) return "个人";
        switch (taskType) {
            case "PERSONAL": return "个人";
            case "TEAM": return "团队";
            case "PROJECT": return "项目";
            default: return taskType;
        }
    }

    private String getRemindTypeDisplay(String remindType) {
        if (remindType == null) return "无";
        switch (remindType) {
            case "NONE": return "无";
            case "EMAIL": return "邮件";
            case "SMS": return "短信";
            case "DING": return "钉钉";
            default: return remindType;
        }
    }
}
