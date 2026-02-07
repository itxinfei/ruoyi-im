package com.ruoyi.im.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.constants.MeetingRoomConstants;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.im.domain.ImMeetingBooking;
import com.ruoyi.im.domain.ImMeetingRoom;
import com.ruoyi.im.dto.meeting.ImMeetingBookingRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomCreateRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomQueryRequest;
import com.ruoyi.im.dto.meeting.ImMeetingRoomUpdateRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.mapper.ImMeetingBookingMapper;
import com.ruoyi.im.mapper.ImMeetingRoomMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.ImMeetingRoomService;
import com.ruoyi.im.service.ImWebSocketBroadcastService;
import com.ruoyi.im.vo.meeting.ImMeetingBookingVO;
import com.ruoyi.im.vo.meeting.ImMeetingRoomScheduleVO;
import com.ruoyi.im.vo.meeting.ImMeetingRoomVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.im.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会议室管理服务实现
 *
 * @author ruoyi
 */
@Service
public class ImMeetingRoomServiceImpl implements ImMeetingRoomService {

    private static final Logger log = LoggerFactory.getLogger(ImMeetingRoomServiceImpl.class);

    @Autowired
    private ImMeetingRoomMapper roomMapper;

    @Autowired
    private ImMeetingBookingMapper bookingMapper;

    @Autowired
    private ImWebSocketBroadcastService webSocketBroadcastService;

    @Autowired
    private ImUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoom(ImMeetingRoomCreateRequest request, Long userId) {
        // 检查编号是否重复
        if (request.getRoomNumber() != null) {
            ImMeetingRoom existing = roomMapper.selectOne(
                    new LambdaQueryWrapper<ImMeetingRoom>()
                            .eq(ImMeetingRoom::getRoomNumber, request.getRoomNumber())
            );
            if (existing != null) {
                throw new BusinessException("会议室编号已存在");
            }
        }

        ImMeetingRoom room = new ImMeetingRoom();
        BeanConvertUtil.copyProperties(request, room);

        room.setStatus(MeetingRoomConstants.Status.AVAILABLE);
        room.setIsBookable(true);
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());

        // 处理设施列表
        if (request.getFacilities() != null && !request.getFacilities().isEmpty()) {
            room.setFacilities(JSON.toJSONString(request.getFacilities()));
        }

        // 处理图片列表
        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
            room.setPhotos(JSON.toJSONString(request.getPhotos()));
        }

        roomMapper.insert(room);
        log.info("创建会议室成功: roomId={}, name={}, operator={}", room.getId(), room.getRoomName(), userId);
        return room.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(ImMeetingRoomUpdateRequest request, Long userId) {
        ImMeetingRoom room = roomMapper.selectById(request.getId());
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }

        // 检查编号是否重复
        if (request.getRoomNumber() != null && !request.getRoomNumber().equals(room.getRoomNumber())) {
            ImMeetingRoom existing = roomMapper.selectOne(
                    new LambdaQueryWrapper<ImMeetingRoom>()
                            .eq(ImMeetingRoom::getRoomNumber, request.getRoomNumber())
                            .ne(ImMeetingRoom::getId, request.getId())
            );
            if (existing != null) {
                throw new BusinessException("会议室编号已存在");
            }
        }

        if (request.getRoomName() != null) {
            room.setRoomName(request.getRoomName());
        }
        if (request.getRoomNumber() != null) {
            room.setRoomNumber(request.getRoomNumber());
        }
        if (request.getDepartmentId() != null) {
            room.setDepartmentId(request.getDepartmentId());
        }
        if (request.getLocation() != null) {
            room.setLocation(request.getLocation());
        }
        if (request.getFloor() != null) {
            room.setFloor(request.getFloor());
        }
        if (request.getCapacity() != null) {
            room.setCapacity(request.getCapacity());
        }
        if (request.getHasProjector() != null) {
            room.setHasProjector(request.getHasProjector());
        }
        if (request.getHasWhiteboard() != null) {
            room.setHasWhiteboard(request.getHasWhiteboard());
        }
        if (request.getHasVideoConf() != null) {
            room.setHasVideoConf(request.getHasVideoConf());
        }
        if (request.getHasPhone() != null) {
            room.setHasPhone(request.getHasPhone());
        }
        if (request.getFacilities() != null) {
            room.setFacilities(JSON.toJSONString(request.getFacilities()));
        }
        if (request.getPhotos() != null) {
            room.setPhotos(JSON.toJSONString(request.getPhotos()));
        }
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }
        if (request.getIsBookable() != null) {
            room.setIsBookable(request.getIsBookable());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getRemark() != null) {
            room.setRemark(request.getRemark());
        }

        room.setUpdateTime(LocalDateTime.now());
        roomMapper.updateById(room);
        log.info("更新会议室成功: roomId={}, operator={}", request.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long roomId, Long userId) {
        ImMeetingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }

        // 检查是否有未完成的预订
        Long bookingCount = bookingMapper.selectCount(
                new LambdaQueryWrapper<ImMeetingBooking>()
                        .eq(ImMeetingBooking::getRoomId, roomId)
                        .in(ImMeetingBooking::getStatus, Arrays.asList(
                                MeetingRoomConstants.BookingStatus.PENDING,
                                MeetingRoomConstants.BookingStatus.CONFIRMED))
                        .gt(ImMeetingBooking::getEndTime, LocalDateTime.now())
        );
        if (bookingCount != null && bookingCount > 0) {
            throw new BusinessException("会议室存在未完成的预订，无法删除");
        }

        roomMapper.deleteById(roomId);
        log.info("删除会议室成功: roomId={}, operator={}", roomId, userId);
    }

    @Override
    public ImMeetingRoomVO getRoomDetail(Long roomId) {
        ImMeetingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }
        return convertToVO(room);
    }

    @Override
    public IPage<ImMeetingRoomVO> getRoomPage(ImMeetingRoomQueryRequest request) {
        Page<ImMeetingRoom> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ImMeetingRoom> roomPage = roomMapper.selectRoomPage(page, request);

        Page<ImMeetingRoomVO> voPage = new Page<>(roomPage.getCurrent(), roomPage.getSize(), roomPage.getTotal());
        List<ImMeetingRoomVO> vos = roomPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(vos);

        return voPage;
    }

    @Override
    public List<ImMeetingRoomVO> getAvailableRooms() {
        List<ImMeetingRoom> rooms = roomMapper.selectAvailableRooms();
        return rooms.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bookRoom(ImMeetingBookingRequest request, Long userId) {
        // 检查会议室是否存在
        ImMeetingRoom room = roomMapper.selectById(request.getRoomId());
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }

        if (!room.getIsBookable()) {
            throw new BusinessException("该会议室暂不可预订");
        }

        if (!MeetingRoomConstants.Status.AVAILABLE.equals(room.getStatus())) {
            throw new BusinessException("该会议室当前不可用");
        }

        // 检查时间范围
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("不能预订过去的时间");
        }

        // 检查容纳人数
        if (request.getAttendeeCount() > room.getCapacity()) {
            throw new BusinessException("参会人数超过会议室容纳人数");
        }

        // 检查时间冲突
        if (!checkAvailability(request.getRoomId(), request.getStartTime(), request.getEndTime())) {
            throw new BusinessException("该时间段已被预订");
        }

        // 创建预订
        ImMeetingBooking booking = new ImMeetingBooking();
        BeanConvertUtil.copyProperties(request, booking);
        booking.setBookingUserId(userId);
        booking.setStatus(MeetingRoomConstants.BookingStatus.CONFIRMED);
        booking.setReminderSent(false);
        booking.setCreateTime(LocalDateTime.now());
        booking.setUpdateTime(LocalDateTime.now());

        // 处理参会人员
        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            booking.setAttendees(JSON.toJSONString(request.getAttendees()));
        }

        // 处理资源列表
        if (request.getResources() != null && !request.getResources().isEmpty()) {
            booking.setResources(JSON.toJSONString(request.getResources()));
        }

        bookingMapper.insert(booking);
        log.info("预订会议室成功: bookingId={}, roomId={}, userId={}", booking.getId(), request.getRoomId(), userId);

        // 发送预订通知给参会人员
        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            Set<Long> attendeeIds = new HashSet<>(request.getAttendees());
            // 格式化时间字符串
            String startTimeStr = request.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String endTimeStr = request.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            webSocketBroadcastService.broadcastMeetingRoomBooking(
                    booking.getId(), room.getRoomName(), startTimeStr, endTimeStr, attendeeIds, userId);
        }

        return booking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId, Long userId) {
        ImMeetingBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        if (!booking.getBookingUserId().equals(userId)) {
            throw new BusinessException("只有预订人可以取消预订");
        }

        if (MeetingRoomConstants.BookingStatus.CANCELLED.equals(booking.getStatus())
                || MeetingRoomConstants.BookingStatus.COMPLETED.equals(booking.getStatus())) {
            throw new BusinessException("该预订无法取消");
        }

        booking.setStatus(MeetingRoomConstants.BookingStatus.CANCELLED);
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        log.info("取消预订成功: bookingId={}, userId={}", bookingId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBooking(Long bookingId, Long userId) {
        ImMeetingBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        if (!MeetingRoomConstants.BookingStatus.PENDING.equals(booking.getStatus())) {
            throw new BusinessException("该预订不需要确认");
        }

        booking.setStatus(MeetingRoomConstants.BookingStatus.CONFIRMED);
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        log.info("确认预订成功: bookingId={}, userId={}", bookingId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long bookingId, Long userId) {
        ImMeetingBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        if (!booking.getBookingUserId().equals(userId)) {
            throw new BusinessException("只有预订人可以签到");
        }

        if (!MeetingRoomConstants.BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new BusinessException("预订状态不正确");
        }

        if (booking.getCheckInTime() != null) {
            throw new BusinessException("已经签过到了");
        }

        booking.setCheckInTime(LocalDateTime.now());
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        log.info("签到成功: bookingId={}, userId={}", bookingId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long bookingId, Long userId) {
        ImMeetingBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        if (!booking.getBookingUserId().equals(userId)) {
            throw new BusinessException("只有预订人可以签退");
        }

        if (booking.getCheckInTime() == null) {
            throw new BusinessException("请先签到");
        }

        if (booking.getCheckOutTime() != null) {
            throw new BusinessException("已经签退过了");
        }

        booking.setCheckOutTime(LocalDateTime.now());
        booking.setStatus(MeetingRoomConstants.BookingStatus.COMPLETED);
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        log.info("签退成功: bookingId={}, userId={}", bookingId, userId);
    }

    @Override
    public ImMeetingBookingVO getBookingDetail(Long bookingId, Long userId) {
        ImMeetingBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        return convertToBookingVO(booking);
    }

    @Override
    public List<ImMeetingBookingVO> getUserBookings(Long userId) {
        List<ImMeetingBooking> bookings = bookingMapper.selectUserBookings(userId);
        return bookings.stream()
                .map(this::convertToBookingVO)
                .collect(Collectors.toList());
    }

    @Override
    public ImMeetingRoomScheduleVO getRoomSchedule(Long roomId, String date) {
        ImMeetingRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }

        // 解析日期
        LocalDate queryDate;
        try {
            queryDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            queryDate = LocalDate.now();
        }

        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.plusDays(1).atStartOfDay();

        List<ImMeetingBooking> bookings = bookingMapper.selectRoomBookings(roomId, startOfDay, endOfDay);

        ImMeetingRoomScheduleVO vo = new ImMeetingRoomScheduleVO();
        vo.setRoomId(room.getId());
        vo.setRoomName(room.getRoomName());
        vo.setLocation(room.getLocation());
        vo.setCapacity(room.getCapacity());
        vo.setDate(date);

        List<ImMeetingRoomScheduleVO.ScheduleItem> items = bookings.stream()
                .map(booking -> {
                    ImMeetingRoomScheduleVO.ScheduleItem item = new ImMeetingRoomScheduleVO.ScheduleItem();
                    item.setBookingId(booking.getId());
                    item.setMeetingTitle(booking.getMeetingTitle());
                    item.setMeetingType(booking.getMeetingType());
                    item.setBookingUserName(booking.getBookingUserName());
                    item.setStartTime(booking.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    item.setEndTime(booking.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    item.setStatus(booking.getStatus());
                    item.setAttendeeCount(booking.getAttendeeCount());
                    return item;
                })
                .collect(Collectors.toList());

        vo.setSchedules(items);
        return vo;
    }

    @Override
    public boolean checkAvailability(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        int conflicts = roomMapper.checkRoomAvailability(roomId, startTime, endTime);
        return conflicts == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFeedback(Long bookingId, String feedback, Integer rating, Long userId) {
        ImMeetingBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        if (!booking.getBookingUserId().equals(userId)) {
            throw new BusinessException("只有预订人可以提交反馈");
        }

        if (!MeetingRoomConstants.BookingStatus.COMPLETED.equals(booking.getStatus())) {
            throw new BusinessException("会议结束后才能提交反馈");
        }

        booking.setFeedback(feedback);
        booking.setRating(rating);
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        log.info("提交会议反馈成功: bookingId={}, userId={}", bookingId, userId);
    }

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 总预订次数
        int totalBookings = bookingMapper.countByUserId(userId);
        stats.put("totalBookings", totalBookings);

        // 即将开始的预订
        List<ImMeetingBooking> upcomingBookings = bookingMapper.selectList(
                new LambdaQueryWrapper<ImMeetingBooking>()
                        .eq(ImMeetingBooking::getBookingUserId, userId)
                        .eq(ImMeetingBooking::getStatus, MeetingRoomConstants.BookingStatus.CONFIRMED)
                        .gt(ImMeetingBooking::getStartTime, LocalDateTime.now())
                        .orderByAsc(ImMeetingBooking::getStartTime)
                        .last("LIMIT 5")
        );
        stats.put("upcomingBookings", upcomingBookings.stream()
                .map(this::convertToBookingVO)
                .collect(Collectors.toList()));

        return stats;
    }

    /**
     * 转换为VO
     */
    private ImMeetingRoomVO convertToVO(ImMeetingRoom room) {
        ImMeetingRoomVO vo = new ImMeetingRoomVO();
        BeanConvertUtil.copyProperties(room, vo);

        vo.setStatusDisplay(getStatusDisplay(room.getStatus()));

        // 解析设施列表
        if (room.getFacilities() != null && !room.getFacilities().isEmpty()) {
            try {
                vo.setFacilities(JSON.parseArray(room.getFacilities(), String.class));
            } catch (Exception e) {
                log.warn("解析设施列表失败: roomId={}", room.getId());
            }
        }

        // 解析图片列表
        if (room.getPhotos() != null && !room.getPhotos().isEmpty()) {
            try {
                vo.setPhotos(JSON.parseArray(room.getPhotos(), String.class));
            } catch (Exception e) {
                log.warn("解析图片列表失败: roomId={}", room.getId());
            }
        }

        // 获取预订数量
        int bookingCount = roomMapper.countBookingsByRoomId(room.getId());
        vo.setBookingCount(bookingCount);

        return vo;
    }

    /**
     * 转换为预订VO
     */
    private ImMeetingBookingVO convertToBookingVO(ImMeetingBooking booking) {
        ImMeetingBookingVO vo = new ImMeetingBookingVO();
        BeanConvertUtil.copyProperties(booking, vo);

        vo.setStatusDisplay(getStatusDisplay(booking.getStatus()));
        vo.setMeetingTypeDisplay(getMeetingTypeDisplay(booking.getMeetingType()));

        // 判断是否已过期
        vo.setIsExpired(booking.getEndTime().isBefore(LocalDateTime.now()));

        // 判断签到状态
        vo.setIsCheckedIn(booking.getCheckInTime() != null);
        vo.setIsCheckedOut(booking.getCheckOutTime() != null);

        // 解析参会人员
        if (booking.getAttendees() != null && !booking.getAttendees().isEmpty()) {
            try {
                List<Long> attendeeIds = JSON.parseArray(booking.getAttendees(), Long.class);
                if (attendeeIds != null && !attendeeIds.isEmpty()) {
                    List<com.ruoyi.im.vo.meeting.ImMeetingBookingVO.Attendee> attendeeList = new ArrayList<>();
                    for (Long attendeeId : attendeeIds) {
                        com.ruoyi.im.domain.ImUser user = userMapper.selectImUserById(attendeeId);
                        if (user != null) {
                            com.ruoyi.im.vo.meeting.ImMeetingBookingVO.Attendee attendee =
                                    new com.ruoyi.im.vo.meeting.ImMeetingBookingVO.Attendee();
                            attendee.setUserId(attendeeId);
                            attendee.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                            attendee.setUserAvatar(user.getAvatar());
                            attendee.setIsCheckedIn(false); // TODO: 从签到记录表查询实际签到状态
                            attendeeList.add(attendee);
                        }
                    }
                    vo.setAttendees(attendeeList);
                }
            } catch (Exception e) {
                log.warn("解析参会人员失败: bookingId={}", booking.getId());
            }
        }

        // 解析资源列表
        if (booking.getResources() != null && !booking.getResources().isEmpty()) {
            try {
                vo.setResources(JSON.parseArray(booking.getResources(), String.class));
            } catch (Exception e) {
                log.warn("解析资源列表失败: bookingId={}", booking.getId());
            }
        }

        return vo;
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "未知";
        switch (status) {
            case MeetingRoomConstants.Status.AVAILABLE: return "可用";
            case MeetingRoomConstants.Status.MAINTENANCE: return "维护中";
            case MeetingRoomConstants.Status.DISABLED: return "已停用";
            case MeetingRoomConstants.BookingStatus.PENDING: return "待确认";
            case MeetingRoomConstants.BookingStatus.CONFIRMED: return "已确认";
            case MeetingRoomConstants.BookingStatus.CANCELLED: return "已取消";
            case MeetingRoomConstants.BookingStatus.COMPLETED: return "已完成";
            default: return status;
        }
    }

    private String getMeetingTypeDisplay(String type) {
        if (type == null) return "常规会议";
        switch (type) {
            case MeetingRoomConstants.MeetingType.REGULAR: return "常规会议";
            case MeetingRoomConstants.MeetingType.TRAINING: return "培训";
            case MeetingRoomConstants.MeetingType.INTERVIEW: return "面试";
            case MeetingRoomConstants.MeetingType.CLIENT: return "客户会议";
            default: return type;
        }
    }
}
