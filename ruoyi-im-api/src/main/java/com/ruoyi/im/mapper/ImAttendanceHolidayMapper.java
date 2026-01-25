package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAttendanceHoliday;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤假期Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImAttendanceHolidayMapper extends BaseMapper<ImAttendanceHoliday> {

    /**
     * 查询指定年份的假期
     *
     * @param year 年份
     * @return 假期列表
     */
    List<ImAttendanceHoliday> selectByYear(@Param("year") Integer year);

    /**
     * 查询指定日期范围的假期
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 假期列表
     */
    List<ImAttendanceHoliday> selectByDateRange(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 检查指定日期是否为假期
     *
     * @param date 日期
     * @return 假期信息
     */
    ImAttendanceHoliday selectByDate(@Param("date") LocalDate date);
}
