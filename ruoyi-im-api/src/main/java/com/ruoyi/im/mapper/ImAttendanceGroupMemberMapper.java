package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImAttendanceGroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考勤组成员Mapper
 *
 * @author ruoyi
 */
@Mapper
public interface ImAttendanceGroupMemberMapper extends BaseMapper<ImAttendanceGroupMember> {

    /**
     * 查询考勤组成员列表
     *
     * @param groupId 考勤组ID
     * @return 成员列表
     */
    List<ImAttendanceGroupMember> selectByGroupId(@Param("groupId") Long groupId);

    /**
     * 查询用户在考勤组中的信息
     *
     * @param groupId 考勤组ID
     * @param userId  用户ID
     * @return 成员信息
     */
    ImAttendanceGroupMember selectByGroupAndUser(@Param("groupId") Long groupId,
                                                 @Param("userId") Long userId);

    /**
     * 批量插入成员
     *
     * @param members 成员列表
     * @return 插入行数
     */
    int batchInsert(@Param("members") List<ImAttendanceGroupMember> members);

    /**
     * 批量更新成员状态
     *
     * @param groupId 考勤组ID
     * @param userIds 用户ID列表
     * @param status  状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("groupId") Long groupId,
                         @Param("userIds") List<Long> userIds,
                         @Param("status") String status);
}
