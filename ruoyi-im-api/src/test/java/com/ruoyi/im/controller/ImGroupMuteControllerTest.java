package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImGroupMuteService;
import com.ruoyi.im.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImGroupMuteController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImGroupMuteControllerTest {

    @Mock
    private ImGroupMuteService groupMuteService;

    @InjectMocks
    private ImGroupMuteController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_GROUP_ID = 2001L;
    private static final Long TEST_TARGET_USER_ID = 3001L;

    @Test
    void setAllMuted_Success_EnableMute() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupMuteService).setAllMuted(TEST_GROUP_ID, true, TEST_USER_ID);

            Result<Void> result = controller.setAllMuted(TEST_GROUP_ID, true);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已开启全员禁言", result.getMsg());
            verify(groupMuteService).setAllMuted(TEST_GROUP_ID, true, TEST_USER_ID);
        }
    }

    @Test
    void setAllMuted_Success_DisableMute() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupMuteService).setAllMuted(TEST_GROUP_ID, false, TEST_USER_ID);

            Result<Void> result = controller.setAllMuted(TEST_GROUP_ID, false);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已关闭全员禁言", result.getMsg());
            verify(groupMuteService).setAllMuted(TEST_GROUP_ID, false, TEST_USER_ID);
        }
    }

    @Test
    void setAllMuted_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("Permission denied")).when(groupMuteService)
                    .setAllMuted(TEST_GROUP_ID, true, TEST_USER_ID);

            assertThrows(RuntimeException.class, () -> controller.setAllMuted(TEST_GROUP_ID, true));
        }
    }

    @Test
    void muteMember_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupMuteService)
                    .muteMember(TEST_GROUP_ID, TEST_TARGET_USER_ID, 30, TEST_USER_ID);

            Result<Void> result = controller.muteMember(TEST_GROUP_ID, TEST_TARGET_USER_ID, 30);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("成员已禁言", result.getMsg());
            verify(groupMuteService).muteMember(TEST_GROUP_ID, TEST_TARGET_USER_ID, 30, TEST_USER_ID);
        }
    }

    @Test
    void muteMember_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("User not in group")).when(groupMuteService)
                    .muteMember(TEST_GROUP_ID, TEST_TARGET_USER_ID, 30, TEST_USER_ID);

            assertThrows(RuntimeException.class,
                    () -> controller.muteMember(TEST_GROUP_ID, TEST_TARGET_USER_ID, 30));
        }
    }

    @Test
    void unmuteMember_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupMuteService)
                    .unmuteMember(TEST_GROUP_ID, TEST_TARGET_USER_ID, TEST_USER_ID);

            Result<Void> result = controller.unmuteMember(TEST_GROUP_ID, TEST_TARGET_USER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已解除禁言", result.getMsg());
            verify(groupMuteService).unmuteMember(TEST_GROUP_ID, TEST_TARGET_USER_ID, TEST_USER_ID);
        }
    }

    @Test
    void batchMuteMembers_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> userIds = Arrays.asList(3001L, 3002L, 3003L);
            doNothing().when(groupMuteService)
                    .batchMuteMembers(TEST_GROUP_ID, userIds, 60, TEST_USER_ID);

            Result<Void> result = controller.batchMuteMembers(TEST_GROUP_ID, userIds, 60);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("批量禁言成功", result.getMsg());
            verify(groupMuteService).batchMuteMembers(TEST_GROUP_ID, userIds, 60, TEST_USER_ID);
        }
    }

    @Test
    void batchMuteMembers_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> userIds = Arrays.asList();
            doNothing().when(groupMuteService)
                    .batchMuteMembers(TEST_GROUP_ID, userIds, 60, TEST_USER_ID);

            Result<Void> result = controller.batchMuteMembers(TEST_GROUP_ID, userIds, 60);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("批量禁言成功", result.getMsg());
        }
    }

    @Test
    void isUserMuted_ReturnTrue() {
        when(groupMuteService.isUserMuted(TEST_GROUP_ID, TEST_TARGET_USER_ID)).thenReturn(true);

        Result<Boolean> result = controller.isUserMuted(TEST_GROUP_ID, TEST_TARGET_USER_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(groupMuteService).isUserMuted(TEST_GROUP_ID, TEST_TARGET_USER_ID);
    }

    @Test
    void isUserMuted_ReturnFalse() {
        when(groupMuteService.isUserMuted(TEST_GROUP_ID, TEST_TARGET_USER_ID)).thenReturn(false);

        Result<Boolean> result = controller.isUserMuted(TEST_GROUP_ID, TEST_TARGET_USER_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
    }

    @Test
    void getMuteEndTime_Success() {
        LocalDateTime expectedEndTime = LocalDateTime.now().plusMinutes(30);
        when(groupMuteService.getMuteEndTime(TEST_GROUP_ID, TEST_TARGET_USER_ID))
                .thenReturn(expectedEndTime);

        Result<LocalDateTime> result = controller.getMuteEndTime(TEST_GROUP_ID, TEST_TARGET_USER_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(groupMuteService).getMuteEndTime(TEST_GROUP_ID, TEST_TARGET_USER_ID);
    }

    @Test
    void getMuteEndTime_ReturnNull_WhenNotMuted() {
        when(groupMuteService.getMuteEndTime(TEST_GROUP_ID, TEST_TARGET_USER_ID)).thenReturn(null);

        Result<LocalDateTime> result = controller.getMuteEndTime(TEST_GROUP_ID, TEST_TARGET_USER_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    void isAllMuted_ReturnTrue() {
        when(groupMuteService.isAllMuted(TEST_GROUP_ID)).thenReturn(true);

        Result<Boolean> result = controller.isAllMuted(TEST_GROUP_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(groupMuteService).isAllMuted(TEST_GROUP_ID);
    }

    @Test
    void isAllMuted_ReturnFalse() {
        when(groupMuteService.isAllMuted(TEST_GROUP_ID)).thenReturn(false);

        Result<Boolean> result = controller.isAllMuted(TEST_GROUP_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
    }
}
