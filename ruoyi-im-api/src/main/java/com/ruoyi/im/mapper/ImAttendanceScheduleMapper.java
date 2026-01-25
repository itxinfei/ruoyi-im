package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAttendanceSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤排班Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImAttendanceScheduleMapper extends BaseMapper<ImAttendanceSchedule> {

    /**
     * 查询用户排班
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 排班列表
     */
    List<ImAttendanceSchedule> selectUserSchedule(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期的排班
     *
     * @param groupId     考勤组ID
     * @param scheduleDate 排班日期
     * @return 排班列表
     */
    List<ImAttendanceSchedule> selectByDate(@Param("groupId") Long groupId,
                                            @Param("scheduleDate") LocalDate scheduleDate);

    /**
     * 批量插入排班
     *
     * @param schedules 排班列表
     * @return 插入行数
     */
    int batchInsert(@Param("schedules") List<ImAttendanceSchedule> schedules);
}
