package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImTask;
import com.ruoyi.im.dto.task.ImTaskQueryRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务Mapper
 *
 * @author ruoyi
 */
public interface ImTaskMapper extends BaseMapper<ImTask> {

    /**
     * 分页查询任务列表（带用户信息）
     *
     * @param page 分页参数
     * @param req 查询条件
     * @return 任务分页列表
     */
    IPage<ImTask> selectTaskPage(Page<?> page, @Param("req") ImTaskQueryRequest req);

    /**
     * 查询用户负责的任务列表
     *
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 任务列表
     */
    List<ImTask> selectByAssigneeId(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询用户创建的任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    @Select("SELECT * FROM im_task WHERE creator_id = #{userId} ORDER BY create_time DESC")
    List<ImTask> selectByCreatorId(@Param("userId") Long userId);

    /**
     * 查询子任务列表
     *
     * @param parentId 父任务ID
     * @return 子任务列表
     */
    @Select("SELECT * FROM im_task WHERE parent_id = #{parentId} ORDER BY create_time ASC")
    List<ImTask> selectSubtasks(@Param("parentId") Long parentId);

    /**
     * 统计用户负责的未完成任务数量
     *
     * @param userId 用户ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM im_task WHERE assignee_id = #{userId} AND status != 'COMPLETED' AND status != 'CANCELLED'")
    Integer countUncompletedByAssignee(@Param("userId") Long userId);

    /**
     * 统计用户负责的已逾期任务数量
     *
     * @param userId 用户ID
     * @param currentDate 当前日期
     * @return 数量
     */
    Integer countOverdueByAssignee(@Param("userId") Long userId, @Param("currentDate") String currentDate);

    /**
     * 查询逾期的任务列表
     *
     * @param currentDate 当前日期
     * @return 逾期任务列表
     */
    List<ImTask> selectOverdueTasks(@Param("currentDate") String currentDate);
}
