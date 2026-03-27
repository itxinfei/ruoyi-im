package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.reaction.ImMessageReactionAddRequest;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImMessageReactionService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.reaction.ImMessageReactionVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImMessageReactionController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImMessageReactionControllerTest {

    @Mock
    private ImMessageReactionService reactionService;

    @InjectMocks
    private ImMessageReactionController reactionController;

    private Validator validator;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_MESSAGE_ID = 3001L;

    /**
     * 测试添加表情回应 - 成功场景
     */
    @Test
    void testAddReaction_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageReactionAddRequest request = new ImMessageReactionAddRequest();
            request.setMessageId(TEST_MESSAGE_ID);
            request.setEmoji("👍");
            request.setReactionType("EMOJI");

            ImMessageReactionVO reactionVO = new ImMessageReactionVO();
            reactionVO.setId(1L);
            when(reactionService.addReaction(any(ImMessageReactionAddRequest.class), eq(TEST_USER_ID))).thenReturn(reactionVO);

            Result<Void> result = reactionController.addReaction(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(reactionService).addReaction(any(ImMessageReactionAddRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试添加表情回应 - 未登录
     */
    @Test
    void testAddReaction_NotLoggedIn() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId)
                    .thenThrow(new BusinessException(401, "未登录或Token已过期"));

            ImMessageReactionAddRequest request = new ImMessageReactionAddRequest();
            request.setMessageId(TEST_MESSAGE_ID);
            request.setEmoji("👍");

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                reactionController.addReaction(request);
            });

            assertEquals(401, exception.getCode());
            assertEquals("未登录或Token已过期", exception.getMessage());
        }
    }

    /**
     * 测试添加表情回应 - 缺少消息ID
     */
    @Test
    void testAddReaction_MissingMessageId() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        ImMessageReactionAddRequest request = new ImMessageReactionAddRequest();
        // messageId is null
        request.setEmoji("👍");

        Set<ConstraintViolation<ImMessageReactionAddRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("消息ID不能为空")));
    }

    /**
     * 测试添加表情回应 - 缺少表情
     */
    @Test
    void testAddReaction_MissingEmoji() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        ImMessageReactionAddRequest request = new ImMessageReactionAddRequest();
        request.setMessageId(TEST_MESSAGE_ID);
        // emoji is null/blank

        Set<ConstraintViolation<ImMessageReactionAddRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("表情不能为空")));
    }

    /**
     * 测试切换表情回应 - 成功场景
     */
    @Test
    void testToggleReaction_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageReactionAddRequest request = new ImMessageReactionAddRequest();
            request.setMessageId(TEST_MESSAGE_ID);
            request.setEmoji("❤️");
            request.setReactionType("HEART");

            ImMessageReactionVO reactionVO = new ImMessageReactionVO();
            reactionVO.setId(1L);
            when(reactionService.addReaction(any(ImMessageReactionAddRequest.class), eq(TEST_USER_ID))).thenReturn(reactionVO);

            Result<Void> result = reactionController.toggleReaction(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(reactionService).addReaction(any(ImMessageReactionAddRequest.class), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试切换表情回应 - 服务异常
     */
    @Test
    void testToggleReaction_ServiceException() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImMessageReactionAddRequest request = new ImMessageReactionAddRequest();
            request.setMessageId(TEST_MESSAGE_ID);
            request.setEmoji("👍");

            doThrow(new BusinessException("添加反应失败"))
                    .when(reactionService).addReaction(any(ImMessageReactionAddRequest.class), eq(TEST_USER_ID));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                reactionController.toggleReaction(request);
            });

            assertEquals("添加反应失败", exception.getMessage());
        }
    }

    /**
     * 测试获取消息的所有表情回应 - 成功场景
     */
    @Test
    void testGetReactions_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<ImMessageReactionVO> expectedReactions = Arrays.asList(
                    createTestReactionVO(1L, TEST_MESSAGE_ID, "👍"),
                    createTestReactionVO(2L, TEST_MESSAGE_ID, "❤️")
            );
            when(reactionService.getMessageReactions(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenReturn(expectedReactions);

            Result<List<ImMessageReactionVO>> result = reactionController.getReactions(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            verify(reactionService).getMessageReactions(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试获取消息的所有表情回应 - 空列表
     */
    @Test
    void testGetReactions_EmptyList() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(reactionService.getMessageReactions(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID)))
                    .thenReturn(Arrays.asList());

            Result<List<ImMessageReactionVO>> result = reactionController.getReactions(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    /**
     * 测试删除自己的表情回应 - 成功场景
     */
    @Test
    void testRemoveReaction_Success() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(reactionService).removeReaction(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));

            Result<Void> result = reactionController.removeReaction(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(reactionService).removeReaction(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));
        }
    }

    /**
     * 测试删除自己的表情回应 - 无权限
     */
    @Test
    void testRemoveReaction_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new BusinessException("无权删除该反应"))
                    .when(reactionService).removeReaction(eq(TEST_MESSAGE_ID), eq(TEST_USER_ID));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                reactionController.removeReaction(TEST_MESSAGE_ID);
            });

            assertEquals("无权删除该反应", exception.getMessage());
        }
    }

    /**
     * 创建测试用 ImMessageReactionVO
     */
    private ImMessageReactionVO createTestReactionVO(Long id, Long messageId, String emoji) {
        ImMessageReactionVO vo = new ImMessageReactionVO();
        vo.setId(id);
        vo.setMessageId(messageId);
        vo.setUserId(TEST_USER_ID);
        vo.setUserNickname("TestUser");
        vo.setEmoji(emoji);
        vo.setReactionType("EMOJI");
        vo.setIsMine(true);
        return vo;
    }
}
