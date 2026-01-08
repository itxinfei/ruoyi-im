package com.ruoyi.im.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.dto.workreport.WorkReportCreateRequest;
import com.ruoyi.im.dto.workreport.WorkReportQueryRequest;
import com.ruoyi.im.vo.workreport.WorkReportCommentVO;
import com.ruoyi.im.vo.workreport.WorkReportDetailVO;
import com.ruoyi.im.vo.workreport.WorkReportLikeUserVO;

import java.util.List;

/**
 * 工作日志服务
 */
public interface ImWorkReportService {

    /**
     * 创建工作日志
     *
     * @param request 创建请求
     * @param userId  用户ID
     * @return 日志ID
     */
    Long createReport(WorkReportCreateRequest request, Long userId);

    /**
     * 更新工作日志
     *
     * @param reportId 日志ID
     * @param request  更新请求
     * @param userId   用户ID
     */
    void updateReport(Long reportId, WorkReportCreateRequest request, Long userId);

    /**
     * 删除工作日志
     *
     * @param reportId 日志ID
     * @param userId   用户ID
     */
    void deleteReport(Long reportId, Long userId);

    /**
     * 提交工作日志
     *
     * @param reportId 日志ID
     * @param userId   用户ID
     */
    void submitReport(Long reportId, Long userId);

    /**
     * 获取工作日志详情
     *
     * @param reportId 日志ID
     * @param userId   用户ID
     * @return 日志详情
     */
    WorkReportDetailVO getReportDetail(Long reportId, Long userId);

    /**
     * 分页查询工作日志列表
     *
     * @param request 查询请求
     * @param userId  用户ID
     * @return 日志列表
     */
    IPage<WorkReportDetailVO> getReportPage(WorkReportQueryRequest request, Long userId);

    /**
     * 获取我的日志列表
     *
     * @param userId 用户ID
     * @return 日志列表
     */
    List<WorkReportDetailVO> getMyReports(Long userId);

    /**
     * 添加评论
     *
     * @param reportId 日志ID
     * @param content  评论内容
     * @param parentId 父评论ID
     * @param userId   用户ID
     * @return 评论ID
     */
    Long addComment(Long reportId, String content, Long parentId, Long userId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取评论列表
     *
     * @param reportId 日志ID
     * @return 评论列表
     */
    List<WorkReportCommentVO> getComments(Long reportId);

    /**
     * 点赞/取消点赞
     *
     * @param reportId 日志ID
     * @param userId   用户ID
     * @return 是否点赞
     */
    boolean toggleLike(Long reportId, Long userId);

    /**
     * 获取点赞用户列表
     *
     * @param reportId 日志ID
     * @return 点赞用户列表
     */
    List<WorkReportLikeUserVO> getLikeUsers(Long reportId);

    /**
     * 审批工作日志
     *
     * @param reportId 日志ID
     * @param userId   审批人ID
     * @param approved 是否通过
     * @param remark   审批备注
     */
    void approveReport(Long reportId, Long userId, Boolean approved, String remark);

    /**
     * 获取统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Object getStatistics(Long userId);
}
