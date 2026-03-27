package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroupAnnouncement;
import com.ruoyi.im.dto.group.ImGroupAnnouncementCreateRequest;
import com.ruoyi.im.service.ImGroupAnnouncementService;
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
 * ImGroupAnnouncementController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImGroupAnnouncementControllerTest {

    @Mock
    private ImGroupAnnouncementService groupAnnouncementService;

    @InjectMocks
    private ImGroupAnnouncementController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_GROUP_ID = 2001L;
    private static final Long TEST_ANNOUNCEMENT_ID = 3001L;

    @Test
    void createAnnouncement_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupAnnouncementCreateRequest request = new ImGroupAnnouncementCreateRequest();
            request.setGroupId(TEST_GROUP_ID);
            request.setContent("Test Announcement");
            request.setType(1);
            request.setExpireMinutes(60);

            when(groupAnnouncementService.createAnnouncement(
                    eq(TEST_GROUP_ID),
                    eq("Test Announcement"),
                    eq(1),
                    isNull(),
                    eq(0),
                    any(LocalDateTime.class),
                    eq(TEST_USER_ID)
            )).thenReturn(TEST_ANNOUNCEMENT_ID);

            Result<Long> result = controller.createAnnouncement(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_ANNOUNCEMENT_ID, result.getData());
            assertEquals("公告发布成功", result.getMsg());
            verify(groupAnnouncementService).createAnnouncement(
                    eq(TEST_GROUP_ID),
                    eq("Test Announcement"),
                    eq(1),
                    isNull(),
                    eq(0),
                    any(LocalDateTime.class),
                    eq(TEST_USER_ID)
            );
        }
    }

    @Test
    void createAnnouncement_WithoutExpireTime_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupAnnouncementCreateRequest request = new ImGroupAnnouncementCreateRequest();
            request.setGroupId(TEST_GROUP_ID);
            request.setContent("Test Announcement");
            request.setType(1);
            request.setExpireMinutes(null);

            when(groupAnnouncementService.createAnnouncement(
                    eq(TEST_GROUP_ID),
                    eq("Test Announcement"),
                    eq(1),
                    isNull(),
                    eq(0),
                    isNull(),
                    eq(TEST_USER_ID)
            )).thenReturn(TEST_ANNOUNCEMENT_ID);

            Result<Long> result = controller.createAnnouncement(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_ANNOUNCEMENT_ID, result.getData());
        }
    }

    @Test
    void getAnnouncements_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupAnnouncement announcement1 = new ImGroupAnnouncement();
            announcement1.setId(1L);
            announcement1.setContent("Announcement 1");
            ImGroupAnnouncement announcement2 = new ImGroupAnnouncement();
            announcement2.setId(2L);
            announcement2.setContent("Announcement 2");
            List<ImGroupAnnouncement> announcements = Arrays.asList(announcement1, announcement2);

            when(groupAnnouncementService.getAnnouncements(TEST_GROUP_ID, TEST_USER_ID))
                    .thenReturn(announcements);

            Result<List<ImGroupAnnouncement>> result = controller.getAnnouncements(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(groupAnnouncementService).getAnnouncements(TEST_GROUP_ID, TEST_USER_ID);
        }
    }

    @Test
    void getValidAnnouncements_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupAnnouncement announcement = new ImGroupAnnouncement();
            announcement.setId(1L);
            announcement.setContent("Valid Announcement");
            List<ImGroupAnnouncement> announcements = Arrays.asList(announcement);

            when(groupAnnouncementService.getValidAnnouncements(TEST_GROUP_ID, TEST_USER_ID))
                    .thenReturn(announcements);

            Result<List<ImGroupAnnouncement>> result = controller.getValidAnnouncements(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
        }
    }

    @Test
    void getPinnedAnnouncements_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupAnnouncement announcement = new ImGroupAnnouncement();
            announcement.setId(1L);
            announcement.setContent("Pinned Announcement");
            announcement.setIsPinned(1);
            List<ImGroupAnnouncement> announcements = Arrays.asList(announcement);

            when(groupAnnouncementService.getPinnedAnnouncements(TEST_GROUP_ID, TEST_USER_ID))
                    .thenReturn(announcements);

            Result<List<ImGroupAnnouncement>> result = controller.getPinnedAnnouncements(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
        }
    }

    @Test
    void getLatestAnnouncement_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupAnnouncement announcement = new ImGroupAnnouncement();
            announcement.setId(1L);
            announcement.setContent("Latest Announcement");

            when(groupAnnouncementService.getLatestAnnouncement(TEST_GROUP_ID, TEST_USER_ID))
                    .thenReturn(announcement);

            Result<ImGroupAnnouncement> result = controller.getLatestAnnouncement(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("Latest Announcement", result.getData().getContent());
        }
    }

    @Test
    void getLatestAnnouncement_ReturnNull_WhenNoAnnouncement() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(groupAnnouncementService.getLatestAnnouncement(TEST_GROUP_ID, TEST_USER_ID))
                    .thenReturn(null);

            Result<ImGroupAnnouncement> result = controller.getLatestAnnouncement(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNull(result.getData());
        }
    }

    @Test
    void updateAnnouncement_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupAnnouncementService)
                    .updateAnnouncement(TEST_ANNOUNCEMENT_ID, "Updated Content", TEST_USER_ID);

            Result<Void> result = controller.updateAnnouncement(TEST_ANNOUNCEMENT_ID, "Updated Content");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("公告编辑成功", result.getMsg());
            verify(groupAnnouncementService).updateAnnouncement(TEST_ANNOUNCEMENT_ID, "Updated Content", TEST_USER_ID);
        }
    }

    @Test
    void deleteAnnouncement_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupAnnouncementService)
                    .deleteAnnouncement(TEST_ANNOUNCEMENT_ID, TEST_USER_ID);

            Result<Void> result = controller.deleteAnnouncement(TEST_ANNOUNCEMENT_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("公告删除成功", result.getMsg());
            verify(groupAnnouncementService).deleteAnnouncement(TEST_ANNOUNCEMENT_ID, TEST_USER_ID);
        }
    }

    @Test
    void recallAnnouncement_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupAnnouncementService)
                    .recallAnnouncement(TEST_ANNOUNCEMENT_ID, TEST_USER_ID);

            Result<Void> result = controller.recallAnnouncement(TEST_ANNOUNCEMENT_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("公告已撤回", result.getMsg());
            verify(groupAnnouncementService).recallAnnouncement(TEST_ANNOUNCEMENT_ID, TEST_USER_ID);
        }
    }

    @Test
    void setPinned_Success_Pin() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupAnnouncementService)
                    .setPinned(TEST_ANNOUNCEMENT_ID, 1, TEST_USER_ID);

            Result<Void> result = controller.setPinned(TEST_ANNOUNCEMENT_ID, 1);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("公告已置顶", result.getMsg());
            verify(groupAnnouncementService).setPinned(TEST_ANNOUNCEMENT_ID, 1, TEST_USER_ID);
        }
    }

    @Test
    void setPinned_Success_Unpin() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupAnnouncementService)
                    .setPinned(TEST_ANNOUNCEMENT_ID, 0, TEST_USER_ID);

            Result<Void> result = controller.setPinned(TEST_ANNOUNCEMENT_ID, 0);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已取消置顶", result.getMsg());
        }
    }

    @Test
    void cleanupExpiredAnnouncements_Success() {
        when(groupAnnouncementService.cleanupExpiredAnnouncements()).thenReturn(5);

        Result<Integer> result = controller.cleanupExpiredAnnouncements();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(5, result.getData());
        assertTrue(result.getMsg().contains("清理完成"));
    }
}
