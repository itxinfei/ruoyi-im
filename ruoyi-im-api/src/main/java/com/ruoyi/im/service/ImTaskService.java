package com.ruoyi.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.dto.task.ImTaskCreateRequest;
import com.ruoyi.im.dto.task.ImTaskQueryRequest;
import com.ruoyi.im.dto.task.ImTaskUpdateRequest;
import com.ruoyi.im.vo.task.ImTaskDetailVO;
import com.ruoyi.im.vo.task.ImTaskVO;

import java.util.List;
import java.util.Map;

/**
 * 任务管理服务接口
 *
 * @author ruoyi
 */
public interface ImTaskService {

    /**
     * 创建任务
     *
     * @param request 创建请求
     * @param userId 当前用户ID
     * @return 任务ID
     */
    Long createTask(ImTaskCreateRequest request, Long userId);

    /**
     * 更新任务
     *
     * @param request 更新请求
     * @param userId 当前用户ID
     */
    void updateTask(ImTaskUpdateRequest request, Long userId);

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     */
    void deleteTask(Long taskId, Long userId);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @return 任务详情
     */
    ImTaskDetailVO getTaskDetail(Long taskId, Long userId);

    /**
     * 分页查询任务列表
     *
     * @param request 查询条件
     * @param userId 当前用户ID
     * @return 分页结果
     */
    IPage<ImTaskVO> getTaskPage(ImTaskQueryRequest request, Long userId);

    /**
     * 获取我的任务列表
     *
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 任务列表
     */
    List<ImTaskVO> getMyTasks(Long userId, String status);

    /**
     * 获取我创建的任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<ImTaskVO> getMyCreatedTasks(Long userId);

    /**
     * 分配任务
     *
     * @param taskId 任务ID
     * @param assigneeId 负责人ID
     * @param userId 当前用户ID
     */
    void assignTask(Long taskId, Long assigneeId, Long userId);

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param userId 当前用户ID
     */
    void updateTaskStatus(Long taskId, String status, Long userId);

    /**
     * 更新任务进度
     *
     * @param taskId 任务ID
     * @param percent 完成百分比
     * @param userId 当前用户ID
     */
    void updateProgress(Long taskId, Integer percent, Long userId);

    /**
     * 关注/取消关注任务
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     */
    void toggleFollow(Long taskId, Long userId);

    /**
     * 添加任务评论
     *
     * @param taskId 任务ID
     * @param content 评论内容
     * @param replyToId 回复的评论ID（可选）
     * @param userId 当前用户ID
     * @return 评论ID
     */
    Long addComment(Long taskId, String content, Long replyToId, Long userId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取任务评论列表
     *
     * @param taskId 任务ID
     * @return 评论列表
     */
    List<Map<String, Object>> getComments(Long taskId);

    /**
     * 添加任务附件
     *
     * @param taskId 任务ID
     * @param fileName 文件名
     * @param fileUrl 文件URL
     * @param fileSize 文件大小
     * @param userId 当前用户ID
     * @return 附件ID
     */
    Long addAttachment(Long taskId, String fileName, String fileUrl, Long fileSize, Long userId);

    /**
     * 删除附件
     *
     * @param attachmentId 附件ID
     * @param userId 当前用户ID
     */
    void deleteAttachment(Long attachmentId, Long userId);

    /**
     * 获取任务统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getTaskStatistics(Long userId);

    /**
     * 批量删除任务
     *
     * @param taskIds 任务ID列表
     * @param userId 当前用户ID
     */
    void batchDelete(List<Long> taskIds, Long userId);

    /**
     * 批量更新任务状态
     *
     * @param taskIds 任务ID列表
     * @param status 状态
     * @param userId 当前用户ID
     */
    void batchUpdateStatus(List<Long> taskIds, String status, Long userId);

    /**
     * 获取子任务列表
     *
     * @param parentId 父任务ID
     * @param userId 当前用户ID
     * @return 子任务列表
     */
    List<ImTaskVO> getSubtasks(Long parentId, Long userId);

    /**
     * 复制任务
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @return 新任务ID
     */
    Long copyTask(Long taskId, Long userId);
}
