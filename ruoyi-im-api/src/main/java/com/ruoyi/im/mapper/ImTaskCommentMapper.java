package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImTaskComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务评论Mapper
 *
 * @author ruoyi
 */
public interface ImTaskCommentMapper extends BaseMapper<ImTaskComment> {

    /**
     * 查询任务评论列表（带用户信息）
     *
     * @param taskId 任务ID
     * @return 评论列表
     */
    List<ImTaskComment> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计任务评论数
     *
     * @param taskId 任务ID
     * @return 评论数
     */
    int countByTaskId(@Param("taskId") Long taskId);

    /**
     * 软删除评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int softDelete(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
