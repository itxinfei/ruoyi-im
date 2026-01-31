package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constants.StatusConstants;
import com.ruoyi.im.domain.ImAttendanceGroup;
import com.ruoyi.im.domain.ImAttendanceGroupMember;
import com.ruoyi.im.domain.ImAttendanceSchedule;
import com.ruoyi.im.domain.ImAttendanceShift;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.attendance.ImAttendanceGroupCreateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImAttendanceGroupMapper;
import com.ruoyi.im.mapper.ImAttendanceGroupMemberMapper;
import com.ruoyi.im.mapper.ImAttendanceScheduleMapper;
import com.ruoyi.im.mapper.ImAttendanceShiftMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImAttendanceGroupService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.util.VoBuilder;
import com.ruoyi.im.vo.attendance.ImAttendanceGroupVO;
import com.ruoyi.im.vo.attendance.ImAttendanceShiftVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考勤组服务实现
 *
 * @author ruoyi
 */
@Service
public class ImAttendanceGroupServiceImpl implements ImAttendanceGroupService {

    private static final Logger logger = LoggerFactory.getLogger(ImAttendanceGroupServiceImpl.class);

    @Autowired
    private ImAttendanceGroupMapper attendanceGroupMapper;

    @Autowired
    private ImAttendanceGroupMemberMapper attendanceGroupMemberMapper;

    @Autowired
    private ImAttendanceShiftMapper attendanceShiftMapper;

    @Autowired
    private ImAttendanceScheduleMapper attendanceScheduleMapper;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(ImAttendanceGroupCreateRequest request, Long userId) {
        // 检查同名考勤组
        Long count = attendanceGroupMapper.selectCount(
                new LambdaQueryWrapper<ImAttendanceGroup>()
                        .eq(ImAttendanceGroup::getGroupName, request.getGroupName())
        );
        if (count > 0) {
            throw new BusinessException("考勤组名称已存在");
        }

        ImAttendanceGroup group = new ImAttendanceGroup();
        group.setGroupName(request.getGroupName());
        group.setManagerId(userId);
        group.setDescription(request.getDescription());
        group.setAttendanceType(request.getAttendanceType());
        group.setCheckMethod(request.getCheckMethod() != null ? request.getCheckMethod() : StatusConstants.CheckMethod.LOCATION);
        group.setWorkStartTime(request.getWorkStartTime());
        group.setWorkEndTime(request.getWorkEndTime());
        group.setCheckInBefore(request.getCheckInBefore() != null ? request.getCheckInBefore() : 120);
        group.setLateTolerance(request.getLateTolerance() != null ? request.getLateTolerance() : 0);
        group.setEarlyTolerance(request.getEarlyTolerance() != null ? request.getEarlyTolerance() : 0);
        group.setWorkDays(request.getWorkDays() != null ? request.getWorkDays() : "1,2,3,4,5");
        group.setNeedCheckIn(true);
        group.setAllowRemote(request.getAllowRemote() != null ? request.getAllowRemote() : false);
        group.setCheckRange(request.getCheckRange() != null ? request.getCheckRange() : 100);
        group.setCheckLocation(request.getCheckLocation());
        group.setWifiSsid(request.getWifiSsid());
        group.setAutoAttendance(false);
        group.setStatus(StatusConstants.Active.ACTIVE);
        group.setCreatorId(userId);
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());

        attendanceGroupMapper.insert(group);

        // 添加创建者为管理员
        if (request.getMemberIds() == null || !request.getMemberIds().contains(userId)) {
            ImAttendanceGroupMember adminMember = new ImAttendanceGroupMember();
            adminMember.setGroupId(group.getId());
            adminMember.setUserId(userId);
            ImUser creator = userMapper.selectImUserById(userId);
            adminMember.setUserName(creator != null ? creator.getNickname() : "");
            adminMember.setRole(StatusConstants.AttendanceMemberRole.ADMIN);
            adminMember.setStatus(StatusConstants.Active.ACTIVE);
            adminMember.setJoinTime(LocalDateTime.now());
            adminMember.setCreateTime(LocalDateTime.now());
            attendanceGroupMemberMapper.insert(adminMember);
        }

        // 添加成员
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            addMembers(group.getId(), request.getMemberIds(), userId);
        }

        logger.info("创建考勤组成功: groupId={}, groupName={}", group.getId(), group.getGroupName());
        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(Long groupId, ImAttendanceGroupCreateRequest request, Long userId) {
        ImAttendanceGroup group = attendanceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("考勤组不存在");
        }

        if (!group.getManagerId().equals(userId)) {
            throw new BusinessException("无权限操作此考勤组");
        }

        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setAttendanceType(request.getAttendanceType());
        group.setCheckMethod(request.getCheckMethod());
        group.setWorkStartTime(request.getWorkStartTime());
        group.setWorkEndTime(request.getWorkEndTime());
        group.setCheckInBefore(request.getCheckInBefore());
        group.setLateTolerance(request.getLateTolerance());
        group.setEarlyTolerance(request.getEarlyTolerance());
        group.setWorkDays(request.getWorkDays());
        group.setAllowRemote(request.getAllowRemote());
        group.setCheckRange(request.getCheckRange());
        group.setCheckLocation(request.getCheckLocation());
        group.setWifiSsid(request.getWifiSsid());
        group.setUpdateTime(LocalDateTime.now());

        attendanceGroupMapper.updateById(group);

        logger.info("更新考勤组成功: groupId={}", groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId, Long userId) {
        ImAttendanceGroup group = attendanceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("考勤组不存在");
        }

        if (!group.getManagerId().equals(userId)) {
            throw new BusinessException("无权限操作此考勤组");
        }

        attendanceGroupMapper.deleteById(groupId);

        // 同时删除成员关系
        attendanceGroupMemberMapper.delete(
                new LambdaQueryWrapper<ImAttendanceGroupMember>()
                        .eq(ImAttendanceGroupMember::getGroupId, groupId)
        );

        logger.info("删除考勤组成功: groupId={}", groupId);
    }

    @Override
    public ImAttendanceGroupVO getGroupDetail(Long groupId) {
        ImAttendanceGroup group = attendanceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("考勤组不存在");
        }

        // 使用 VoBuilder 进行转换并设置额外字段
        return VoBuilder.of(group, ImAttendanceGroupVO.class)
                .convert()
                .andThen(vo -> {
                    // 获取负责人信息
                    ImUser manager = userMapper.selectImUserById(group.getManagerId());
                    if (manager != null) {
                        vo.setManagerName(manager.getNickname());
                    }
                })
                .andThenIfNotBlank(group.getWorkDays(), vo -> {
                    // 解析工作日
                    List<Integer> workDays = Arrays.stream(group.getWorkDays().split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                    vo.setWorkDays(workDays);
                })
                .andThen(vo -> {
                    // 统计成员数量
                    Integer memberCount = attendanceGroupMapper.countMembers(groupId);
                    vo.setMemberCount(memberCount != null ? memberCount : 0);
                })
                .build();
    }

    @Override
    public List<ImAttendanceGroupVO> getGroupList(Long userId) {
        List<ImAttendanceGroup> groups = attendanceGroupMapper.selectGroupsByUser(userId);

        return groups.stream().map(group -> {
            ImAttendanceGroupVO vo = BeanConvertUtil.convert(group, ImAttendanceGroupVO.class);

            if (group.getWorkDays() != null) {
                vo.setWorkDays(Arrays.stream(group.getWorkDays().split(","))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList()));
            }

            Integer memberCount = attendanceGroupMapper.countMembers(group.getId());
            vo.setMemberCount(memberCount != null ? memberCount : 0);

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ImAttendanceGroupVO getUserGroup(Long userId) {
        ImAttendanceGroup group = attendanceGroupMapper.selectByUserId(userId);
        if (group == null) {
            return null;
        }

        return VoBuilder.of(group, ImAttendanceGroupVO.class)
                .convert()
                .andThenIfNotBlank(group.getWorkDays(), vo -> {
                    // 解析工作日
                    vo.setWorkDays(Arrays.stream(group.getWorkDays().split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList()));
                })
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(Long groupId, List<Long> memberIds, Long userId) {
        ImAttendanceGroup group = attendanceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("考勤组不存在");
        }

        if (!group.getManagerId().equals(userId)) {
            throw new BusinessException("无权限操作此考勤组");
        }

        List<ImAttendanceGroupMember> members = new ArrayList<>();
        for (Long memberId : memberIds) {
            // 检查是否已是成员
            ImAttendanceGroupMember existMember = attendanceGroupMemberMapper.selectByGroupAndUser(groupId, memberId);
            if (existMember != null && StatusConstants.Active.ACTIVE.equals(existMember.getStatus())) {
                continue;
            }

            // 如果是重新加入，更新状态
            if (existMember != null) {
                existMember.setStatus("ACTIVE");
                existMember.setLeaveTime(null);
                attendanceGroupMemberMapper.updateById(existMember);
                continue;
            }

            // 创建新成员
            ImAttendanceGroupMember member = new ImAttendanceGroupMember();
            member.setGroupId(groupId);
            member.setUserId(memberId);
            ImUser user = userMapper.selectImUserById(memberId);
            member.setUserName(user != null ? user.getNickname() : "");
            member.setRole(StatusConstants.AttendanceMemberRole.MEMBER);
            member.setStatus(StatusConstants.Active.ACTIVE);
            member.setJoinTime(LocalDateTime.now());
            member.setCreateTime(LocalDateTime.now());
            members.add(member);
        }

        if (!members.isEmpty()) {
            attendanceGroupMemberMapper.batchInsert(members);
        }

        logger.info("添加考勤组成员成功: groupId={}, count={}", groupId, members.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMembers(Long groupId, List<Long> memberIds, Long userId) {
        ImAttendanceGroup group = attendanceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("考勤组不存在");
        }

        if (!group.getManagerId().equals(userId)) {
            throw new BusinessException("无权限操作此考勤组");
        }

        attendanceGroupMemberMapper.batchUpdateStatus(groupId, memberIds,
                StatusConstants.AttendanceMemberStatus.LEFT);

        logger.info("移除考勤组成员成功: groupId={}, count={}", groupId, memberIds.size());
    }

    @Override
    public List<Long> getGroupMembers(Long groupId) {
        List<ImAttendanceGroupMember> members = attendanceGroupMemberMapper.selectByGroupId(groupId);
        return members.stream()
                .filter(m -> StatusConstants.Active.ACTIVE.equals(m.getStatus()))
                .map(ImAttendanceGroupMember::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createShift(Long groupId, String shiftName, String workStartTime,
                           String workEndTime, Long userId) {
        ImAttendanceGroup group = attendanceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("考勤组不存在");
        }

        if (!group.getManagerId().equals(userId)) {
            throw new BusinessException("无权限操作此考勤组");
        }

        ImAttendanceShift shift = new ImAttendanceShift();
        shift.setGroupId(groupId);
        shift.setShiftName(shiftName);
        shift.setShiftType(StatusConstants.ShiftType.NORMAL);
        shift.setWorkStartTime(workStartTime);
        shift.setWorkEndTime(workEndTime);
        shift.setCheckInBefore(120);
        shift.setCheckOutAfter(60);
        shift.setLateTolerance(0);
        shift.setEarlyTolerance(0);
        shift.setStatus(StatusConstants.Active.ACTIVE);
        shift.setCreateTime(LocalDateTime.now());
        shift.setUpdateTime(LocalDateTime.now());

        attendanceShiftMapper.insert(shift);

        logger.info("创建班次成功: shiftId={}, shiftName={}", shift.getId(), shiftName);
        return shift.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShift(Long shiftId, Long userId) {
        ImAttendanceShift shift = attendanceShiftMapper.selectById(shiftId);
        if (shift == null) {
            throw new BusinessException("班次不存在");
        }

        ImAttendanceGroup group = attendanceGroupMapper.selectById(shift.getGroupId());
        if (group == null || !group.getManagerId().equals(userId)) {
            throw new BusinessException("无权限操作此班次");
        }

        attendanceShiftMapper.deleteById(shiftId);

        logger.info("删除班次成功: shiftId={}", shiftId);
    }

    @Override
    public List<ImAttendanceShiftVO> getShiftList(Long groupId) {
        List<ImAttendanceShift> shifts = attendanceShiftMapper.selectByGroupId(groupId);
        return BeanConvertUtil.convertList(shifts, ImAttendanceShiftVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedule(Long groupId, Long userId, Long shiftId,
                        LocalDate startDate, LocalDate endDate, List<Integer> workDays) {
        ImAttendanceGroup group = attendanceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("考勤组不存在");
        }

        if (!group.getManagerId().equals(userId)) {
            throw new BusinessException("无权限操作此考勤组");
        }

        ImAttendanceShift shift = attendanceShiftMapper.selectById(shiftId);
        if (shift == null) {
            throw new BusinessException("班次不存在");
        }

        // 获取考勤组成员
        List<Long> memberIds = getGroupMembers(groupId);

        // 批量创建排班记录
        List<ImAttendanceSchedule> schedules = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            if (workDays.contains(dayOfWeek)) {
                for (Long memberId : memberIds) {
                    ImAttendanceSchedule schedule = new ImAttendanceSchedule();
                    schedule.setGroupId(groupId);
                    schedule.setUserId(memberId);
                    schedule.setShiftId(shiftId);
                    schedule.setScheduleDate(currentDate);
                    schedule.setScheduleType("WORK");
                    schedule.setNeedCheckIn(true);
                    schedule.setCreateTime(LocalDateTime.now());
                    schedule.setUpdateTime(LocalDateTime.now());
                    schedules.add(schedule);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        if (!schedules.isEmpty()) {
            attendanceScheduleMapper.batchInsert(schedules);
        }

        logger.info("批量排班成功: groupId={}, count={}", groupId, schedules.size());
    }

    @Override
    public List<ImAttendanceShiftVO> getUserSchedule(Long userId, LocalDate startDate, LocalDate endDate) {
        List<ImAttendanceSchedule> schedules = attendanceScheduleMapper.selectUserSchedule(userId, startDate, endDate);

        return schedules.stream()
                .map(schedule -> attendanceShiftMapper.selectById(schedule.getShiftId()))
                .filter(shift -> shift != null)
                .map(shift -> BeanConvertUtil.convert(shift, ImAttendanceShiftVO.class))
                .collect(Collectors.toList());
    }
}
