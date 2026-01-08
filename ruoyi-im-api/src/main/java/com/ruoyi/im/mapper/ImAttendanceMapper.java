package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImAttendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 考勤打卡Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImAttendanceMapper {

    /**
     * 查询打卡记录
     *
     * @param id 打卡ID
     * @return 打卡记录
     */
    ImAttendance selectImAttendanceById(Long id);

    /**
     * 根据用户ID和日期查询打卡记录
     *
     * @param userId 用户ID
     * @param attendanceDate 打卡日期
     * @return 打卡记录
     */
    ImAttendance selectByUserIdAndDate(@Param("userId") Long userId, @Param("attendanceDate") LocalDate attendanceDate);

    /**
     * 查询用户打卡记录列表
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡记录列表
     */
    List<ImAttendance> selectByUserIdAndDateRange(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 查询打卡记录列表
     *
     * @param imAttendance 打卡记录
     * @return 打卡记录集合
     */
    List<ImAttendance> selectImAttendanceList(ImAttendance imAttendance);

    /**
     * 新增打卡记录
     *
     * @param imAttendance 打卡记录
     * @return 结果
     */
    int insertImAttendance(ImAttendance imAttendance);

    /**
     * 修改打卡记录
     *
     * @param imAttendance 打卡记录
     * @return 结果
     */
    int updateImAttendance(ImAttendance imAttendance);

    /**
     * 删除打卡记录
     *
     * @param id 打卡ID
     * @return 结果
     */
    int deleteImAttendanceById(Long id);

    /**
     * 统计用户月度考勤数据
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> statisticsByUserIdAndDateRange(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 查询今日打卡状态
     *
     * @param userId 用户ID
     * @param today 今日日期
     * @return 打卡记录
     */
    ImAttendance selectTodayAttendance(@Param("userId") Long userId, @Param("today") LocalDate today);
}
