package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.conversation.ImConversationMemberUpdateRequest;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.service.ImConversationService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.conversation.ImConversationMemberVO;
import com.ruoyi.im.vo.conversation.ImConversationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImSessionController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImSessionControllerTest {

    @Mock
    private ImConversationMemberService conversationMemberService;

    @Mock
    private ImConversationService conversationService;

    @InjectMocks
    private ImSessionController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_SESSION_ID = 2001L;

    @BeforeEach
    void setUp() {
        controller = new ImSessionController(conversationMemberService, conversationService);
    }

    @Test
    void getList_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationVO vo1 = new ImConversationVO();
            vo1.setId(1L);
            vo1.setName("Session 1");
            ImConversationVO vo2 = new ImConversationVO();
            vo2.setId(2L);
            vo2.setName("Session 2");

            when(conversationService.getUserConversations(TEST_USER_ID)).thenReturn(Arrays.asList(vo1, vo2));

            Result<List<ImConversationVO>> result = controller.getList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(conversationService).getUserConversations(TEST_USER_ID);
        }
    }

    @Test
    void getList_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(conversationService.getUserConversations(TEST_USER_ID)).thenReturn(Arrays.asList());

            Result<List<ImConversationVO>> result = controller.getList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void getById_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationVO vo = new ImConversationVO();
            vo.setId(TEST_SESSION_ID);
            vo.setName("Test Session");

            when(conversationService.getConversationById(TEST_SESSION_ID, TEST_USER_ID)).thenReturn(vo);

            Result<ImConversationVO> result = controller.getById(TEST_SESSION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Test Session", result.getData().getName());
            verify(conversationService).getConversationById(TEST_SESSION_ID, TEST_USER_ID);
        }
    }

    @Test
    void update_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationMemberUpdateRequest request = new ImConversationMemberUpdateRequest();
            request.setIsPinned(1);
            request.setIsMuted(0);

            doNothing().when(conversationMemberService).updateConversationMember(
                    eq(TEST_SESSION_ID), eq(TEST_USER_ID), any(ImConversationMemberUpdateRequest.class));

            Result<Void> result = controller.update(TEST_SESSION_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("更新成功", result.getMsg());
            verify(conversationMemberService).updateConversationMember(
                    eq(TEST_SESSION_ID), eq(TEST_USER_ID), any(ImConversationMemberUpdateRequest.class));
        }
    }

    @Test
    void update_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationMemberUpdateRequest request = new ImConversationMemberUpdateRequest();

            doThrow(new RuntimeException("会话不存在")).when(conversationMemberService)
                    .updateConversationMember(eq(TEST_SESSION_ID), eq(TEST_USER_ID),
                            any(ImConversationMemberUpdateRequest.class));

            assertThrows(RuntimeException.class, () -> controller.update(TEST_SESSION_ID, request));
        }
    }

    @Test
    void delete_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService).deleteConversationMember(TEST_SESSION_ID, TEST_USER_ID);

            Result<Void> result = controller.delete(TEST_SESSION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("删除成功", result.getMsg());
            verify(conversationMemberService).deleteConversationMember(TEST_SESSION_ID, TEST_USER_ID);
        }
    }

    @Test
    void delete_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("会话不存在")).when(conversationMemberService)
                    .deleteConversationMember(TEST_SESSION_ID, TEST_USER_ID);

            assertThrows(RuntimeException.class, () -> controller.delete(TEST_SESSION_ID));
        }
    }

    @Test
    void clearUnread_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService).clearUnread(TEST_SESSION_ID, TEST_USER_ID);

            Result<Void> result = controller.clearUnread(TEST_SESSION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已清空未读消息", result.getMsg());
            verify(conversationMemberService).clearUnread(TEST_SESSION_ID, TEST_USER_ID);
        }
    }

    @Test
    void togglePin_Success_Pin() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService).togglePin(TEST_SESSION_ID, TEST_USER_ID, 1);

            Result<Void> result = controller.togglePin(TEST_SESSION_ID, 1);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已置顶", result.getMsg());
            verify(conversationMemberService).togglePin(TEST_SESSION_ID, TEST_USER_ID, 1);
        }
    }

    @Test
    void togglePin_Success_Unpin() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService).togglePin(TEST_SESSION_ID, TEST_USER_ID, 0);

            Result<Void> result = controller.togglePin(TEST_SESSION_ID, 0);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已取消置顶", result.getMsg());
            verify(conversationMemberService).togglePin(TEST_SESSION_ID, TEST_USER_ID, 0);
        }
    }

    @Test
    void toggleMute_Success_Mute() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService).toggleMute(TEST_SESSION_ID, TEST_USER_ID, 1);

            Result<Void> result = controller.toggleMute(TEST_SESSION_ID, 1);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已设为免打扰", result.getMsg());
            verify(conversationMemberService).toggleMute(TEST_SESSION_ID, TEST_USER_ID, 1);
        }
    }

    @Test
    void toggleMute_Success_Unmute() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService).toggleMute(TEST_SESSION_ID, TEST_USER_ID, 0);

            Result<Void> result = controller.toggleMute(TEST_SESSION_ID, 0);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已取消免打扰", result.getMsg());
            verify(conversationMemberService).toggleMute(TEST_SESSION_ID, TEST_USER_ID, 0);
        }
    }
}