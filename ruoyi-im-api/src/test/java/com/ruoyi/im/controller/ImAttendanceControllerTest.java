package com.ruoyi.im.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImAttendance;
import com.ruoyi.im.service.ImAttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 考勤控制器测试
 *
 * @author ruoyi
 */
@WebMvcTest(ImAttendanceController.class)
public class ImAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImAttendanceService attendanceService;

    private ImAttendance mockAttendance;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockAttendance = new ImAttendance();
        mockAttendance.setId(1L);
        mockAttendance.setUserId(1L);
        mockAttendance.setAttendanceDate(LocalDate.now());
        mockAttendance.setCheckInTime(java.time.LocalDateTime.now());
        mockAttendance.setCheckInStatus("NORMAL");
    }

    @Test
    public void testCheckIn() throws Exception {
        // 测试上班打卡
        when(attendanceService.checkIn(anyLong(), anyString(), anyString()))
                .thenReturn(mockAttendance);

        mockMvc.perform(post("/api/im/attendance/checkIn")
                        .param("location", "{\"latitude\":39.9,\"longitude\":116.4}")
                        .param("deviceInfo", "iPhone 14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("上班打卡成功"));
    }

    @Test
    public void testCheckOut() throws Exception {
        // 测试下班打卡
        when(attendanceService.checkOut(anyLong(), anyString(), anyString()))
                .thenReturn(mockAttendance);

        mockMvc.perform(post("/api/im/attendance/checkOut")
                        .param("location", "{\"latitude\":39.9,\"longitude\":116.4}")
                        .param("deviceInfo", "iPhone 14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("下班打卡成功"));
    }

    @Test
    public void testGetTodayStatus() throws Exception {
        // 测试获取今日打卡状态
        when(attendanceService.getTodayAttendance(1L)).thenReturn(mockAttendance);

        mockMvc.perform(get("/api/im/attendance/today")
                        .header("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testGetStatistics() throws Exception {
        // 测试获取月度统计
        when(attendanceService.getMonthlyStatistics(eq(1L), eq(2024), eq(1)))
                .thenReturn(new java.util.HashMap<>());

        mockMvc.perform(get("/api/im/attendance/statistics")
                        .param("year", "2024")
                        .param("month", "1")
                        .header("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    public void testApplySupplement() throws Exception {
        // 测试补卡申请
        when(attendanceService.applySupplement(anyLong(), anyString(), anyLong()))
                .thenReturn(true);

        mockMvc.perform(post("/api/im/attendance/1/supplement")
                        .param("reason", "忘记打卡")
                        .header("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("补卡申请已提交"));
    }

    @Test
    public void testDeleteAttendance() throws Exception {
        // 测试删除打卡记录
        when(attendanceService.deleteAttendance(anyLong(), anyLong())).thenReturn(1);

        mockMvc.perform(delete("/api/im/attendance/1")
                        .header("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("删除成功"));
    }
}
