package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImMessageEditHistoryService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.MessageEditHistoryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImMessageEditHistoryController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImMessageEditHistoryControllerTest {

    @Mock
    private ImMessageEditHistoryService editHistoryService;

    @InjectMocks
    private ImMessageEditHistoryController editHistoryController;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_MESSAGE_ID = 3001L;

    /**
     * 测试获取消息编辑历史列表 - 成功场景
     */
    @Test
    void testGetEditHistory_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<MessageEditHistoryVO> expectedHistory = Arrays.asList(
                    createTestEditHistoryVO(1L, TEST_MESSAGE_ID, "Original content", "Updated content 1"),
                    createTestEditHistoryVO(2L, TEST_MESSAGE_ID, "Updated content 1", "Updated content 2")
            );
            when(editHistoryService.getEditHistory(eq(TEST_MESSAGE_ID)))
                    .thenReturn(expectedHistory);

            Result<List<MessageEditHistoryVO>> result = editHistoryController.getEditHistory(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(editHistoryService).getEditHistory(eq(TEST_MESSAGE_ID));
        }
    }

    /**
     * 测试获取消息编辑历史列表 - 空列表
     */
    @Test
    void testGetEditHistory_EmptyList() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(editHistoryService.getEditHistory(eq(TEST_MESSAGE_ID)))
                    .thenReturn(Arrays.asList());

            Result<List<MessageEditHistoryVO>> result = editHistoryController.getEditHistory(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试获取消息编辑历史列表 - 消息不存在
     */
    @Test
    void testGetEditHistory_MessageNotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(editHistoryService.getEditHistory(eq(TEST_MESSAGE_ID)))
                    .thenReturn(null);

            Result<List<MessageEditHistoryVO>> result = editHistoryController.getEditHistory(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals("查询编辑历史失败", result.getMsg());
        }
    }

    /**
     * 测试获取消息编辑历史列表 - 服务异常
     */
    @Test
    void testGetEditHistory_ServiceException() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(editHistoryService.getEditHistory(eq(TEST_MESSAGE_ID)))
                    .thenThrow(new RuntimeException("数据库查询失败"));

            Result<List<MessageEditHistoryVO>> result = editHistoryController.getEditHistory(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("查询编辑历史失败"));
        }
    }

    /**
     * 测试获取消息编辑统计 - 成功场景
     */
    @Test
    void testGetEditStats_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(editHistoryService.countEditTimes(eq(TEST_MESSAGE_ID)))
                    .thenReturn(3);

            Result<Map<String, Object>> result = editHistoryController.getEditStats(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals(TEST_MESSAGE_ID, result.getData().get("messageId"));
            assertEquals(3, result.getData().get("editTimes"));
        }
    }

    /**
     * 测试获取消息编辑统计 - 编辑次数为null
     */
    @Test
    void testGetEditStats_NullCount() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(editHistoryService.countEditTimes(eq(TEST_MESSAGE_ID)))
                    .thenReturn(null);

            Result<Map<String, Object>> result = editHistoryController.getEditStats(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getData().get("editTimes"));
        }
    }

    /**
     * 测试获取消息编辑统计 - 首次编辑（编辑次数为0）
     */
    @Test
    void testGetEditStats_ZeroEdits() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(editHistoryService.countEditTimes(eq(TEST_MESSAGE_ID)))
                    .thenReturn(0);

            Result<Map<String, Object>> result = editHistoryController.getEditStats(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(0, result.getData().get("editTimes"));
        }
    }

    /**
     * 测试获取消息编辑统计 - 服务异常
     */
    @Test
    void testGetEditStats_ServiceException() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(editHistoryService.countEditTimes(eq(TEST_MESSAGE_ID)))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            Result<Map<String, Object>> result = editHistoryController.getEditStats(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("查询编辑统计失败"));
        }
    }

    /**
     * 测试未登录场景
     */
    @Test
    void testGetEditHistory_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            // Controller catches exception and returns error Result
            Result<List<MessageEditHistoryVO>> result = editHistoryController.getEditHistory(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("未登录"));
        }
    }

    /**
     * 测试未登录场景 - 获取统计
     */
    @Test
    void testGetEditStats_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            // Controller catches exception and returns error Result
            Result<Map<String, Object>> result = editHistoryController.getEditStats(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("未登录"));
        }
    }

    /**
     * 创建测试用 MessageEditHistoryVO
     */
    private MessageEditHistoryVO createTestEditHistoryVO(Long id, Long messageId, String oldContent, String newContent) {
        MessageEditHistoryVO vo = new MessageEditHistoryVO();
        vo.setId(id);
        vo.setMessageId(messageId);
        vo.setOldContent(oldContent);
        vo.setNewContent(newContent);
        vo.setEditorId(TEST_USER_ID);
        vo.setEditorName("TestUser");
        vo.setEditorAvatar("avatar_url");
        vo.setEditTime(LocalDateTime.now());
        return vo;
    }
}
