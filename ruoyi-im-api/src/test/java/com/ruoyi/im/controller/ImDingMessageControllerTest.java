package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImDingMessage;
import com.ruoyi.im.domain.ImDingTemplate;
import com.ruoyi.im.dto.ding.DingSendRequest;
import com.ruoyi.im.service.ImDingMessageService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.ding.DingDetailVO;
import com.ruoyi.im.vo.ding.DingReceiptVO;
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
 * ImDingMessageController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImDingMessageControllerTest {

    @Mock
    private ImDingMessageService dingMessageService;

    @InjectMocks
    private ImDingMessageController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_DING_ID = 2001L;
    private static final Long TEST_TEMPLATE_ID = 3001L;

    @BeforeEach
    void setUp() {
        controller = new ImDingMessageController(dingMessageService);
    }

    @Test
    void sendDing_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            DingSendRequest request = new DingSendRequest();
            request.setContent("Test DING message");

            when(dingMessageService.sendDing(any(DingSendRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(TEST_DING_ID);

            Result<Long> result = controller.sendDing(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_DING_ID, result.getData());
            assertEquals("DING发送成功", result.getMsg());
        }
    }

    @Test
    void sendDing_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            DingSendRequest request = new DingSendRequest();

            when(dingMessageService.sendDing(any(DingSendRequest.class), eq(TEST_USER_ID)))
                    .thenThrow(new RuntimeException("发送失败"));

            assertThrows(RuntimeException.class, () -> controller.sendDing(request));
        }
    }

    @Test
    void getReceivedDingList_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImDingMessage ding1 = new ImDingMessage();
            ding1.setId(1L);
            ImDingMessage ding2 = new ImDingMessage();
            ding2.setId(2L);

            when(dingMessageService.getReceivedDingList(TEST_USER_ID))
                    .thenReturn(Arrays.asList(ding1, ding2));

            Result<List<ImDingMessage>> result = controller.getReceivedDingList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
        }
    }

    @Test
    void getReceivedDingList_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(dingMessageService.getReceivedDingList(TEST_USER_ID))
                    .thenReturn(Collections.emptyList());

            Result<List<ImDingMessage>> result = controller.getReceivedDingList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void getSentDingList_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImDingMessage ding = new ImDingMessage();
            ding.setId(1L);

            when(dingMessageService.getSentDingList(TEST_USER_ID))
                    .thenReturn(Arrays.asList(ding));

            Result<List<ImDingMessage>> result = controller.getSentDingList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
        }
    }

    @Test
    void getDingDetail_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            DingDetailVO detail = new DingDetailVO();
            detail.setId(TEST_DING_ID);
            detail.setContent("Test DING");

            when(dingMessageService.getDingDetail(TEST_DING_ID, TEST_USER_ID))
                    .thenReturn(detail);

            Result<DingDetailVO> result = controller.getDingDetail(TEST_DING_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_DING_ID, result.getData().getId());
        }
    }

    @Test
    void getDingDetail_NotFound() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(dingMessageService.getDingDetail(TEST_DING_ID, TEST_USER_ID))
                    .thenReturn(null);

            Result<DingDetailVO> result = controller.getDingDetail(TEST_DING_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNull(result.getData());
        }
    }

    @Test
    void readDing_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(dingMessageService).readDing(TEST_DING_ID, TEST_USER_ID);

            Result<Void> result = controller.readDing(TEST_DING_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已标记为已读", result.getMsg());
            verify(dingMessageService).readDing(TEST_DING_ID, TEST_USER_ID);
        }
    }

    @Test
    void confirmDing_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(dingMessageService).confirmDing(eq(TEST_DING_ID), eq(TEST_USER_ID), any());

            Result<Void> result = controller.confirmDing(TEST_DING_ID, "收到");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("确认成功", result.getMsg());
            verify(dingMessageService).confirmDing(TEST_DING_ID, TEST_USER_ID, "收到");
        }
    }

    @Test
    void confirmDing_WithoutRemark() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(dingMessageService).confirmDing(eq(TEST_DING_ID), eq(TEST_USER_ID), isNull());

            Result<Void> result = controller.confirmDing(TEST_DING_ID, null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(dingMessageService).confirmDing(TEST_DING_ID, TEST_USER_ID, null);
        }
    }

    @Test
    void getDingReceipts_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            DingReceiptVO receipt1 = new DingReceiptVO();
            receipt1.setId(1L);
            DingReceiptVO receipt2 = new DingReceiptVO();
            receipt2.setId(2L);

            when(dingMessageService.getDingReceipts(TEST_DING_ID, TEST_USER_ID))
                    .thenReturn(Arrays.asList(receipt1, receipt2));

            Result<List<DingReceiptVO>> result = controller.getDingReceipts(TEST_DING_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
        }
    }

    @Test
    void getDingReceipts_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(dingMessageService.getDingReceipts(TEST_DING_ID, TEST_USER_ID))
                    .thenReturn(Collections.emptyList());

            Result<List<DingReceiptVO>> result = controller.getDingReceipts(TEST_DING_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void cancelScheduledDing_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(dingMessageService).cancelScheduledDing(TEST_DING_ID, TEST_USER_ID);

            Result<Void> result = controller.cancelScheduledDing(TEST_DING_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已取消", result.getMsg());
            verify(dingMessageService).cancelScheduledDing(TEST_DING_ID, TEST_USER_ID);
        }
    }

    @Test
    void cancelScheduledDing_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("取消失败")).when(dingMessageService)
                    .cancelScheduledDing(TEST_DING_ID, TEST_USER_ID);

            assertThrows(RuntimeException.class, () ->
                    controller.cancelScheduledDing(TEST_DING_ID));
        }
    }

    @Test
    void getTemplates_Success() {
        ImDingTemplate template1 = new ImDingTemplate();
        template1.setId(1L);
        template1.setName("Template 1");
        ImDingTemplate template2 = new ImDingTemplate();
        template2.setId(2L);
        template2.setName("Template 2");

        when(dingMessageService.getTemplateList(null))
                .thenReturn(Arrays.asList(template1, template2));

        Result<List<ImDingTemplate>> result = controller.getTemplates(null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    @Test
    void getTemplates_WithCategory() {
        ImDingTemplate template = new ImDingTemplate();
        template.setId(1L);
        template.setName("Work Template");
        template.setCategory("WORK");

        when(dingMessageService.getTemplateList("WORK"))
                .thenReturn(Arrays.asList(template));

        Result<List<ImDingTemplate>> result = controller.getTemplates("WORK");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("WORK", result.getData().get(0).getCategory());
    }

    @Test
    void createFromTemplate_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(dingMessageService.createFromTemplate(eq(TEST_TEMPLATE_ID), anyString(), eq(TEST_USER_ID)))
                    .thenReturn(TEST_DING_ID);

            Result<Long> result = controller.createFromTemplate(TEST_TEMPLATE_ID, "param1,param2");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_DING_ID, result.getData());
            assertEquals("创建成功", result.getMsg());
        }
    }
}
