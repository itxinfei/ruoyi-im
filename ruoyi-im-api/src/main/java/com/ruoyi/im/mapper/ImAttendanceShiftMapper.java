package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAttendanceShift;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考勤班次Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImAttendanceShiftMapper extends BaseMapper<ImAttendanceShift> {

    /**
     * 查询考勤组的班次列表
     *
     * @param groupId 考勤组ID
     * @return 班次列表
     */
    List<ImAttendanceShift> selectByGroupId(@Param("groupId") Long groupId);
}
