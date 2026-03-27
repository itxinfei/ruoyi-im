package com.ruoyi.im.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.group.ImGroupFileQueryRequest;
import com.ruoyi.im.dto.group.ImGroupFileUpdateRequest;
import com.ruoyi.im.dto.group.ImGroupFileUploadRequest;
import com.ruoyi.im.service.ImGroupFileService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.group.ImGroupFileStatisticsVO;
import com.ruoyi.im.vo.group.ImGroupFileVO;
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
 * ImGroupFileController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImGroupFileControllerTest {

    @Mock
    private ImGroupFileService groupFileService;

    private ImGroupFileController controller;

    @BeforeEach
    void setUp() {
        controller = new ImGroupFileController(groupFileService);
    }

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_GROUP_ID = 2001L;
    private static final Long TEST_FILE_ID = 3001L;

    @Test
    void uploadFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupFileUploadRequest request = new ImGroupFileUploadRequest();
            request.setGroupId(TEST_GROUP_ID);
            request.setFileName("test.pdf");
            request.setFileId(1L);

            when(groupFileService.uploadFile(request, TEST_USER_ID)).thenReturn(TEST_FILE_ID);

            Result<Long> result = controller.uploadFile(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_FILE_ID, result.getData());
            assertEquals("文件上传成功", result.getMsg());
            verify(groupFileService).uploadFile(request, TEST_USER_ID);
        }
    }

    @Test
    void uploadFile_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupFileUploadRequest request = new ImGroupFileUploadRequest();
            request.setGroupId(TEST_GROUP_ID);
            request.setFileName("test.pdf");

            when(groupFileService.uploadFile(request, TEST_USER_ID))
                    .thenThrow(new RuntimeException("Storage insufficient"));

            assertThrows(RuntimeException.class, () -> controller.uploadFile(request));
        }
    }

    @Test
    void getFileList_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupFileQueryRequest request = new ImGroupFileQueryRequest();
            request.setGroupId(TEST_GROUP_ID);

            ImGroupFileVO file1 = new ImGroupFileVO();
            file1.setId(1L);
            file1.setFileName("file1.pdf");
            ImGroupFileVO file2 = new ImGroupFileVO();
            file2.setId(2L);
            file2.setFileName("file2.pdf");

            IPage<ImGroupFileVO> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 2);
            page.setRecords(Arrays.asList(file1, file2));

            when(groupFileService.getFileList(request, TEST_USER_ID)).thenReturn(page);

            Result<IPage<ImGroupFileVO>> result = controller.getFileList(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().getRecords().size());
            verify(groupFileService).getFileList(request, TEST_USER_ID);
        }
    }

    @Test
    void getFileList_EmptyPage() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupFileQueryRequest request = new ImGroupFileQueryRequest();
            request.setGroupId(TEST_GROUP_ID);

            IPage<ImGroupFileVO> emptyPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 0);

            when(groupFileService.getFileList(request, TEST_USER_ID)).thenReturn(emptyPage);

            Result<IPage<ImGroupFileVO>> result = controller.getFileList(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().getRecords().isEmpty());
        }
    }

    @Test
    void getStatistics_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupFileStatisticsVO statistics = new ImGroupFileStatisticsVO();
            statistics.setTotalCount(100);
            statistics.setTotalSize(1024 * 1024 * 500L);

            when(groupFileService.getStatistics(TEST_GROUP_ID, TEST_USER_ID)).thenReturn(statistics);

            Result<ImGroupFileStatisticsVO> result = controller.getStatistics(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(Integer.valueOf(100), result.getData().getTotalCount());
            verify(groupFileService).getStatistics(TEST_GROUP_ID, TEST_USER_ID);
        }
    }

    @Test
    void getCategories_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<String> categories = Arrays.asList("Documents", "Images", "Videos");

            when(groupFileService.getCategories(TEST_GROUP_ID, TEST_USER_ID)).thenReturn(categories);

            Result<List<String>> result = controller.getCategories(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(3, result.getData().size());
            assertEquals("Documents", result.getData().get(0));
            verify(groupFileService).getCategories(TEST_GROUP_ID, TEST_USER_ID);
        }
    }

    @Test
    void getCategories_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(groupFileService.getCategories(TEST_GROUP_ID, TEST_USER_ID)).thenReturn(Arrays.asList());

            Result<List<String>> result = controller.getCategories(TEST_GROUP_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void updateFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupFileUpdateRequest request = new ImGroupFileUpdateRequest();
            request.setFileName("updated.pdf");

            doNothing().when(groupFileService).updateFile(TEST_FILE_ID, request, TEST_USER_ID);

            Result<Void> result = controller.updateFile(TEST_FILE_ID, request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("文件信息更新成功", result.getMsg());
            verify(groupFileService).updateFile(TEST_FILE_ID, request, TEST_USER_ID);
        }
    }

    @Test
    void updateFile_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImGroupFileUpdateRequest request = new ImGroupFileUpdateRequest();

            doThrow(new RuntimeException("File not found")).when(groupFileService)
                    .updateFile(TEST_FILE_ID, request, TEST_USER_ID);

            assertThrows(RuntimeException.class, () -> controller.updateFile(TEST_FILE_ID, request));
        }
    }

    @Test
    void deleteFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupFileService).deleteFile(TEST_FILE_ID, TEST_USER_ID);

            Result<Void> result = controller.deleteFile(TEST_FILE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("文件删除成功", result.getMsg());
            verify(groupFileService).deleteFile(TEST_FILE_ID, TEST_USER_ID);
        }
    }

    @Test
    void deleteFile_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("No permission")).when(groupFileService)
                    .deleteFile(TEST_FILE_ID, TEST_USER_ID);

            assertThrows(RuntimeException.class, () -> controller.deleteFile(TEST_FILE_ID));
        }
    }

    @Test
    void batchDeleteFiles_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> fileIds = Arrays.asList(1L, 2L, 3L);
            doNothing().when(groupFileService).batchDeleteFiles(fileIds, TEST_USER_ID);

            Result<Void> result = controller.batchDeleteFiles(fileIds);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("批量删除成功", result.getMsg());
            verify(groupFileService).batchDeleteFiles(fileIds, TEST_USER_ID);
        }
    }

    @Test
    void batchDeleteFiles_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            List<Long> fileIds = Arrays.asList();
            doNothing().when(groupFileService).batchDeleteFiles(fileIds, TEST_USER_ID);

            Result<Void> result = controller.batchDeleteFiles(fileIds);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("批量删除成功", result.getMsg());
        }
    }

    // @Test
    void downloadFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            String fileUrl = "http://example.com/files/test.pdf";
            when(groupFileService.downloadFile(anyLong(), anyLong())).thenReturn(fileUrl);

            Result<String> result = controller.downloadFile(TEST_FILE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(fileUrl, result.getData());
            verify(groupFileService).downloadFile(anyLong(), anyLong());
        }
    }

    @Test
    void downloadFile_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(groupFileService.downloadFile(TEST_FILE_ID, TEST_USER_ID))
                    .thenThrow(new RuntimeException("File not found"));

            assertThrows(RuntimeException.class, () -> controller.downloadFile(TEST_FILE_ID));
        }
    }

    @Test
    void moveFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(groupFileService).moveFile(TEST_FILE_ID, "Documents", TEST_USER_ID);

            Result<Void> result = controller.moveFile(TEST_FILE_ID, "Documents");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("文件移动成功", result.getMsg());
            verify(groupFileService).moveFile(TEST_FILE_ID, "Documents", TEST_USER_ID);
        }
    }

    @Test
    void moveFile_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("Category not found")).when(groupFileService)
                    .moveFile(TEST_FILE_ID, "NonExistent", TEST_USER_ID);

            assertThrows(RuntimeException.class, () -> controller.moveFile(TEST_FILE_ID, "NonExistent"));
        }
    }
}
