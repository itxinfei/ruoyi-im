package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImGroupBot;
import com.ruoyi.im.dto.groupbot.BotCreateRequest;
import com.ruoyi.im.dto.groupbot.BotRuleRequest;
import com.ruoyi.im.dto.groupbot.BotUpdateRequest;
import com.ruoyi.im.service.ImGroupBotService;
import com.ruoyi.im.util.SecurityUtils;
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
 * ImGroupBotController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImGroupBotControllerTest {

    @Mock
    private ImGroupBotService groupBotService;

    @InjectMocks
    private ImGroupBotController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_GROUP_ID = 2001L;
    private static final Long TEST_BOT_ID = 3001L;
    private static final Long TEST_RULE_ID = 4001L;

    @Test
    void create_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            BotCreateRequest request = new BotCreateRequest();
            request.setGroupId(TEST_GROUP_ID);
            request.setBotName("TestBot");

            when(groupBotService.createBot(request, TEST_USER_ID)).thenReturn(TEST_BOT_ID);

            Result<Long> result = controller.create(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_BOT_ID, result.getData());
            assertEquals("机器人创建成功", result.getMsg());
            verify(groupBotService).createBot(request, TEST_USER_ID);
        }
    }

    @Test
    void create_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            BotCreateRequest request = new BotCreateRequest();
            request.setGroupId(TEST_GROUP_ID);
            request.setBotName("TestBot");

            when(groupBotService.createBot(request, TEST_USER_ID))
                    .thenThrow(new RuntimeException("Bot creation failed"));

            assertThrows(RuntimeException.class, () -> controller.create(request));
        }
    }

    @Test
    void update_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            BotUpdateRequest request = new BotUpdateRequest();
            request.setBotId(TEST_BOT_ID);
            request.setBotName("UpdatedBot");

            doNothing().when(groupBotService).updateBot(request, TEST_USER_ID);

            Result<Void> result = controller.update(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("机器人更新成功", result.getMsg());
            verify(groupBotService).updateBot(request, TEST_USER_ID);
        }
    }

    @Test
    void delete_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupBotService).deleteBot(TEST_BOT_ID, TEST_USER_ID);

            Result<Void> result = controller.delete(TEST_BOT_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("机器人删除成功", result.getMsg());
            verify(groupBotService).deleteBot(TEST_BOT_ID, TEST_USER_ID);
        }
    }

    @Test
    void delete_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("Bot not found")).when(groupBotService).deleteBot(TEST_BOT_ID, TEST_USER_ID);

            assertThrows(RuntimeException.class, () -> controller.delete(TEST_BOT_ID));
        }
    }

    @Test
    void list_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupBot bot1 = new ImGroupBot();
            bot1.setId(1L);
            bot1.setBotName("Bot1");
            ImGroupBot bot2 = new ImGroupBot();
            bot2.setId(2L);
            bot2.setBotName("Bot2");
            List<ImGroupBot> bots = Arrays.asList(bot1, bot2);

            when(groupBotService.getGroupBots(TEST_GROUP_ID, TEST_USER_ID)).thenReturn(bots);

            Result<List<ImGroupBot>> result = controller.list(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(groupBotService).getGroupBots(TEST_GROUP_ID, TEST_USER_ID);
        }
    }

    @Test
    void list_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(groupBotService.getGroupBots(TEST_GROUP_ID, TEST_USER_ID)).thenReturn(Arrays.asList());

            Result<List<ImGroupBot>> result = controller.list(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void getDetail_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupBot bot = new ImGroupBot();
            bot.setId(TEST_BOT_ID);
            bot.setBotName("TestBot");

            when(groupBotService.getBotDetail(TEST_BOT_ID, TEST_USER_ID)).thenReturn(bot);

            Result<ImGroupBot> result = controller.getDetail(TEST_BOT_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("TestBot", result.getData().getBotName());
            verify(groupBotService).getBotDetail(TEST_BOT_ID, TEST_USER_ID);
        }
    }

    @Test
    void getDetail_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(groupBotService.getBotDetail(TEST_BOT_ID, TEST_USER_ID))
                    .thenThrow(new RuntimeException("Bot not found"));

            assertThrows(RuntimeException.class, () -> controller.getDetail(TEST_BOT_ID));
        }
    }

    @Test
    void addRule_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            BotRuleRequest request = new BotRuleRequest();
            request.setRuleName("TestRule");
            request.setTriggerType("KEYWORD");
            request.setTriggerContent("test.*");
            request.setReplyType("TEXT");
            request.setReplyContent("Hello");

            when(groupBotService.addRule(TEST_BOT_ID, request, TEST_USER_ID)).thenReturn(TEST_RULE_ID);

            Result<Long> result = controller.addRule(TEST_BOT_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_RULE_ID, result.getData());
            assertEquals("规则添加成功", result.getMsg());
            verify(groupBotService).addRule(TEST_BOT_ID, request, TEST_USER_ID);
        }
    }

    @Test
    void updateRule_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            BotRuleRequest request = new BotRuleRequest();
            request.setRuleName("UpdatedRule");

            doNothing().when(groupBotService).updateRule(TEST_RULE_ID, request, TEST_USER_ID);

            Result<Void> result = controller.updateRule(TEST_RULE_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("规则更新成功", result.getMsg());
            verify(groupBotService).updateRule(TEST_RULE_ID, request, TEST_USER_ID);
        }
    }

    @Test
    void deleteRule_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupBotService).deleteRule(TEST_RULE_ID, TEST_USER_ID);

            Result<Void> result = controller.deleteRule(TEST_RULE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("规则删除成功", result.getMsg());
            verify(groupBotService).deleteRule(TEST_RULE_ID, TEST_USER_ID);
        }
    }

    @Test
    void setEnabled_Success_Enable() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupBotService).setBotEnabled(TEST_BOT_ID, true, TEST_USER_ID);

            Result<Void> result = controller.setEnabled(TEST_BOT_ID, true);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("机器人状态已更新", result.getMsg());
            verify(groupBotService).setBotEnabled(TEST_BOT_ID, true, TEST_USER_ID);
        }
    }

    @Test
    void setEnabled_Success_Disable() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupBotService).setBotEnabled(TEST_BOT_ID, false, TEST_USER_ID);

            Result<Void> result = controller.setEnabled(TEST_BOT_ID, false);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("机器人状态已更新", result.getMsg());
        }
    }
}
