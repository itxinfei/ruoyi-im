package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImConversationMember;
import com.ruoyi.im.dto.conversation.ImConversationMemberUpdateRequest;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.conversation.ImConversationMemberVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImConversationMemberController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImConversationMemberControllerTest {

    @Mock
    private ImConversationMemberService conversationMemberService;

    @InjectMocks
    private ImConversationMemberController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_CONVERSATION_ID = 2001L;

    @BeforeEach
    void setUp() {
        controller = new ImConversationMemberController(conversationMemberService);
    }

    @Test
    void list_Success() {
        ImConversationMember member = new ImConversationMember();
        member.setUserId(TEST_USER_ID);

        ImConversationMemberVO vo1 = new ImConversationMemberVO();
        vo1.setConversationId(1L);
        vo1.setUserId(TEST_USER_ID);
        ImConversationMemberVO vo2 = new ImConversationMemberVO();
        vo2.setConversationId(2L);
        vo2.setUserId(TEST_USER_ID);

        when(conversationMemberService.getConversationMemberList(TEST_USER_ID))
                .thenReturn(Arrays.asList(vo1, vo2));

        Result<List<ImConversationMemberVO>> result = controller.list(member);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(conversationMemberService).getConversationMemberList(TEST_USER_ID);
    }

    @Test
    void list_EmptyList() {
        ImConversationMember member = new ImConversationMember();
        member.setUserId(TEST_USER_ID);

        when(conversationMemberService.getConversationMemberList(TEST_USER_ID))
                .thenReturn(Collections.emptyList());

        Result<List<ImConversationMemberVO>> result = controller.list(member);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void getMyConversationList_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationMemberVO vo1 = new ImConversationMemberVO();
            vo1.setConversationId(1L);
            vo1.setUserId(TEST_USER_ID);
            ImConversationMemberVO vo2 = new ImConversationMemberVO();
            vo2.setConversationId(2L);
            vo2.setUserId(TEST_USER_ID);

            when(conversationMemberService.getConversationMemberList(TEST_USER_ID))
                    .thenReturn(Arrays.asList(vo1, vo2));

            Result<List<ImConversationMemberVO>> result = controller.getMyConversationList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(conversationMemberService).getConversationMemberList(TEST_USER_ID);
        }
    }

    @Test
    void getMyConversationList_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(conversationMemberService.getConversationMemberList(TEST_USER_ID))
                    .thenReturn(Collections.emptyList());

            Result<List<ImConversationMemberVO>> result = controller.getMyConversationList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void getInfo_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationMemberVO vo = new ImConversationMemberVO();
            vo.setConversationId(TEST_CONVERSATION_ID);
            vo.setUserId(TEST_USER_ID);

            when(conversationMemberService.getConversationMember(TEST_CONVERSATION_ID, TEST_USER_ID))
                    .thenReturn(vo);

            Result<ImConversationMemberVO> result = controller.getInfo(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_CONVERSATION_ID, result.getData().getConversationId());
            verify(conversationMemberService).getConversationMember(TEST_CONVERSATION_ID, TEST_USER_ID);
        }
    }

    @Test
    void getInfo_NotFound() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(conversationMemberService.getConversationMember(TEST_CONVERSATION_ID, TEST_USER_ID))
                    .thenReturn(null);

            Result<ImConversationMemberVO> result = controller.getInfo(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNull(result.getData());
        }
    }

    @Test
    void edit_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationMemberUpdateRequest request = new ImConversationMemberUpdateRequest();
            request.setIsPinned(1);
            request.setIsMuted(0);

            doNothing().when(conversationMemberService)
                    .updateConversationMember(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), any());

            Result<Void> result = controller.edit(TEST_CONVERSATION_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(conversationMemberService)
                    .updateConversationMember(eq(TEST_CONVERSATION_ID), eq(TEST_USER_ID), any());
        }
    }

    @Test
    void edit_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImConversationMemberUpdateRequest request = new ImConversationMemberUpdateRequest();

            doThrow(new RuntimeException("会话不存在")).when(conversationMemberService)
                    .updateConversationMember(anyLong(), anyLong(), any());

            assertThrows(RuntimeException.class, () ->
                    controller.edit(TEST_CONVERSATION_ID, request));
        }
    }

    @Test
    void remove_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService)
                    .deleteConversationMember(TEST_CONVERSATION_ID, TEST_USER_ID);

            Result<Void> result = controller.remove(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(conversationMemberService)
                    .deleteConversationMember(TEST_CONVERSATION_ID, TEST_USER_ID);
        }
    }

    @Test
    void remove_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("会话不存在")).when(conversationMemberService)
                    .deleteConversationMember(anyLong(), anyLong());

            assertThrows(RuntimeException.class, () ->
                    controller.remove(TEST_CONVERSATION_ID));
        }
    }

    @Test
    void clearUnread_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService)
                    .clearUnread(TEST_CONVERSATION_ID, TEST_USER_ID);

            Result<Void> result = controller.clearUnread(TEST_CONVERSATION_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(conversationMemberService)
                    .clearUnread(TEST_CONVERSATION_ID, TEST_USER_ID);
        }
    }

    @Test
    void clearUnread_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("清除失败")).when(conversationMemberService)
                    .clearUnread(anyLong(), anyLong());

            assertThrows(RuntimeException.class, () ->
                    controller.clearUnread(TEST_CONVERSATION_ID));
        }
    }

    @Test
    void togglePin_Success_Pin() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService)
                    .togglePin(TEST_CONVERSATION_ID, TEST_USER_ID, 1);

            Result<Void> result = controller.togglePin(TEST_CONVERSATION_ID, 1);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(conversationMemberService)
                    .togglePin(TEST_CONVERSATION_ID, TEST_USER_ID, 1);
        }
    }

    @Test
    void togglePin_Success_Unpin() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService)
                    .togglePin(TEST_CONVERSATION_ID, TEST_USER_ID, 0);

            Result<Void> result = controller.togglePin(TEST_CONVERSATION_ID, 0);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(conversationMemberService)
                    .togglePin(TEST_CONVERSATION_ID, TEST_USER_ID, 0);
        }
    }

    @Test
    void toggleMute_Success_Mute() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService)
                    .toggleMute(TEST_CONVERSATION_ID, TEST_USER_ID, 1);

            Result<Void> result = controller.toggleMute(TEST_CONVERSATION_ID, 1);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(conversationMemberService)
                    .toggleMute(TEST_CONVERSATION_ID, TEST_USER_ID, 1);
        }
    }

    @Test
    void toggleMute_Success_Unmute() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(conversationMemberService)
                    .toggleMute(TEST_CONVERSATION_ID, TEST_USER_ID, 0);

            Result<Void> result = controller.toggleMute(TEST_CONVERSATION_ID, 0);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(conversationMemberService)
                    .toggleMute(TEST_CONVERSATION_ID, TEST_USER_ID, 0);
        }
    }
}
