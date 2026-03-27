package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.search.GlobalSearchRequest;
import com.ruoyi.im.service.ImSearchService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.search.GlobalSearchResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImSearchController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImSearchControllerTest {

    @Mock
    private ImSearchService searchService;

    @InjectMocks
    private ImSearchController controller;

    private static final Long TEST_USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        controller = new ImSearchController(searchService);
    }

    @Test
    void globalSearch_WithKeyword_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            GlobalSearchRequest request = new GlobalSearchRequest();
            request.setKeyword("test");

            GlobalSearchResultVO searchResult = new GlobalSearchResultVO();
            searchResult.setKeyword("test");

            when(searchService.search(any(GlobalSearchRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(searchResult);

            Result<GlobalSearchResultVO> result = controller.globalSearch(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals("test", result.getData().getKeyword());
            verify(searchService).search(any(GlobalSearchRequest.class), eq(TEST_USER_ID));
        }
    }

    @Test
    void globalSearch_WithEmptyKeyword_ReturnsEmptyResult() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            GlobalSearchRequest request = new GlobalSearchRequest();
            request.setKeyword("");

            Result<GlobalSearchResultVO> result = controller.globalSearch(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            verify(searchService, never()).search(any(), anyLong());
        }
    }

    @Test
    void globalSearch_WithNullKeyword_ReturnsEmptyResult() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            GlobalSearchRequest request = new GlobalSearchRequest();
            request.setKeyword(null);

            Result<GlobalSearchResultVO> result = controller.globalSearch(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            verify(searchService, never()).search(any(), anyLong());
        }
    }

    @Test
    void globalSearch_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            GlobalSearchRequest request = new GlobalSearchRequest();
            request.setKeyword("test");

            when(searchService.search(any(GlobalSearchRequest.class), eq(TEST_USER_ID)))
                    .thenThrow(new RuntimeException("Search failed"));

            assertThrows(RuntimeException.class, () -> controller.globalSearch(request));
        }
    }
}
