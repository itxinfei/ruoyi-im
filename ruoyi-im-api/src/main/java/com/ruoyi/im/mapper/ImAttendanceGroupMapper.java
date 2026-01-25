package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAttendanceGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考勤组Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImAttendanceGroupMapper extends BaseMapper<ImAttendanceGroup> {

    /**
     * 查询用户所在的考勤组
     *
     * @param userId 用户ID
     * @return 考勤组
     */
    ImAttendanceGroup selectByUserId(@Param("userId") Long userId);

    /**
     * 查询考勤组列表
     *
     * @param userId 用户ID
     * @return 考勤组列表
     */
    List<ImAttendanceGroup> selectGroupsByUser(@Param("userId") Long userId);

    /**
     * 统计考勤组成员数量
     *
     * @param groupId 考勤组ID
     * @return 成员数量
     */
    Integer countMembers(@Param("groupId") Long groupId);
}
