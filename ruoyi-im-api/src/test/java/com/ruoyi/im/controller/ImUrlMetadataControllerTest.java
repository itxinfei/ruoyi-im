package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImUrlMetadata;
import com.ruoyi.im.service.ImUrlMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImUrlMetadataController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImUrlMetadataControllerTest {

    @Mock
    private ImUrlMetadataService urlMetadataService;

    @InjectMocks
    private ImUrlMetadataController controller;

    @BeforeEach
    void setUp() {
        controller = new ImUrlMetadataController(urlMetadataService);
    }

    @Test
    void parseUrl_Success() {
        String url = "https://example.com/article";

        ImUrlMetadata metadata = new ImUrlMetadata();
        metadata.setUrl(url);
        metadata.setTitle("Example Article");
        metadata.setDescription("This is an example article");
        metadata.setImageUrl("https://example.com/image.jpg");
        metadata.setSiteName("Example");
        metadata.setFaviconUrl("https://example.com/favicon.ico");
        metadata.setContentType("text/html");
        metadata.setFetchStatus("SUCCESS");

        when(urlMetadataService.parseUrl(url)).thenReturn(metadata);

        Result<Map<String, Object>> result = controller.parseUrl(url);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(url, result.getData().get("url"));
        assertEquals("Example Article", result.getData().get("title"));
        assertEquals("This is an example article", result.getData().get("description"));
        assertEquals("SUCCESS", result.getData().get("fetchStatus"));
        verify(urlMetadataService).parseUrl(url);
    }

    @Test
    void parseUrl_InvalidUrl_Null() {
        Result<Map<String, Object>> result = controller.parseUrl(null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("无效的 URL 格式", result.getMsg());
    }

    @Test
    void parseUrl_InvalidUrl_Empty() {
        Result<Map<String, Object>> result = controller.parseUrl("");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("无效的 URL 格式", result.getMsg());
    }

    @Test
    void parseUrl_InvalidUrl_NoProtocol() {
        Result<Map<String, Object>> result = controller.parseUrl("not-a-url");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("无效的 URL 格式", result.getMsg());
    }

    @Test
    void parseUrl_InvalidUrl_FtpProtocol() {
        Result<Map<String, Object>> result = controller.parseUrl("ftp://example.com");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("无效的 URL 格式", result.getMsg());
    }

    @Test
    void parseUrl_FetchFailed() {
        String url = "https://example.com/failed";

        ImUrlMetadata metadata = new ImUrlMetadata();
        metadata.setUrl(url);
        metadata.setFetchStatus("FAILED");
        metadata.setErrorMessage("Connection timeout");

        when(urlMetadataService.parseUrl(url)).thenReturn(metadata);

        Result<Map<String, Object>> result = controller.parseUrl(url);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("FAILED", result.getData().get("fetchStatus"));
        assertEquals("Connection timeout", result.getData().get("errorMessage"));
    }

    @Test
    void parseUrl_ServiceThrowsException() {
        String url = "https://example.com/error";

        when(urlMetadataService.parseUrl(url))
                .thenThrow(new RuntimeException("Network error"));

        Result<Map<String, Object>> result = controller.parseUrl(url);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("解析失败"));
    }

    @Test
    void refreshUrl_Success() {
        String url = "https://example.com/article";

        ImUrlMetadata metadata = new ImUrlMetadata();
        metadata.setUrl(url);
        metadata.setTitle("Refreshed Article");
        metadata.setFetchStatus("SUCCESS");

        when(urlMetadataService.refreshUrl(url)).thenReturn(metadata);

        Result<Map<String, Object>> result = controller.refreshUrl(url);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(url, result.getData().get("url"));
        assertEquals("Refreshed Article", result.getData().get("title"));
        verify(urlMetadataService).refreshUrl(url);
    }

    @Test
    void refreshUrl_InvalidUrl() {
        Result<Map<String, Object>> result = controller.refreshUrl("invalid-url");

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("无效的 URL 格式", result.getMsg());
    }

    @Test
    void refreshUrl_ServiceThrowsException() {
        String url = "https://example.com/refresh-error";

        when(urlMetadataService.refreshUrl(url))
                .thenThrow(new RuntimeException("Refresh failed"));

        Result<Map<String, Object>> result = controller.refreshUrl(url);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("刷新失败"));
    }

    @Test
    void isValidUrl_ValidHttps() {
        // Test via the controller by checking success case
        String url = "https://example.com";
        ImUrlMetadata metadata = new ImUrlMetadata();
        metadata.setUrl(url);
        metadata.setTitle("Test");
        metadata.setFetchStatus("SUCCESS");

        when(urlMetadataService.parseUrl(url)).thenReturn(metadata);

        Result<Map<String, Object>> result = controller.parseUrl(url);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void isValidUrl_ValidHttp() {
        String url = "http://example.com";
        ImUrlMetadata metadata = new ImUrlMetadata();
        metadata.setUrl(url);
        metadata.setTitle("Test");
        metadata.setFetchStatus("SUCCESS");

        when(urlMetadataService.parseUrl(url)).thenReturn(metadata);

        Result<Map<String, Object>> result = controller.parseUrl(url);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
}
