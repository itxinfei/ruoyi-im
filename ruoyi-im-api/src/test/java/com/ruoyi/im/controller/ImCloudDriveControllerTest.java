package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.cloud.ImCloudFileMoveRequest;
import com.ruoyi.im.dto.cloud.ImCloudFileShareRequest;
import com.ruoyi.im.dto.cloud.ImCloudFolderCreateRequest;
import com.ruoyi.im.service.ImCloudDriveService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.cloud.ImCloudFileShareVO;
import com.ruoyi.im.vo.cloud.ImCloudFileVO;
import com.ruoyi.im.vo.cloud.ImCloudFolderVO;
import com.ruoyi.im.vo.cloud.ImCloudStorageQuotaVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ImCloudDriveController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImCloudDriveControllerTest {

    @Mock
    private ImCloudDriveService cloudDriveService;

    @InjectMocks
    private ImCloudDriveController controller;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_FOLDER_ID = 2001L;
    private static final Long TEST_FILE_ID = 3001L;
    private static final Long TEST_SHARE_ID = 4001L;

    @BeforeEach
    void setUp() {
        controller = new ImCloudDriveController(cloudDriveService);
    }

    // ==================== 文件夹管理测试 ====================

    @Test
    void createFolder_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFolderCreateRequest request = new ImCloudFolderCreateRequest();
            request.setFolderName("Test Folder");
            request.setParentId(0L);

            when(cloudDriveService.createFolder(any(ImCloudFolderCreateRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(TEST_FOLDER_ID);

            Result<Long> result = controller.createFolder(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_FOLDER_ID, result.getData());
            assertEquals("创建成功", result.getMsg());
        }
    }

    @Test
    void createFolder_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFolderCreateRequest request = new ImCloudFolderCreateRequest();
            request.setFolderName("Test Folder");

            when(cloudDriveService.createFolder(any(ImCloudFolderCreateRequest.class), eq(TEST_USER_ID)))
                    .thenThrow(new RuntimeException("文件夹名称重复"));

            Result<Long> result = controller.createFolder(request);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("文件夹名称重复"));
        }
    }

    @Test
    void renameFolder_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).renameFolder(TEST_FOLDER_ID, "New Name", TEST_USER_ID);

            Result<Void> result = controller.renameFolder(TEST_FOLDER_ID, "New Name");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("重命名成功", result.getMsg());
            verify(cloudDriveService).renameFolder(TEST_FOLDER_ID, "New Name", TEST_USER_ID);
        }
    }

    @Test
    void renameFolder_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("文件夹不存在")).when(cloudDriveService)
                    .renameFolder(eq(TEST_FOLDER_ID), anyString(), anyLong());

            Result<Void> result = controller.renameFolder(TEST_FOLDER_ID, "New Name");

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("文件夹不存在"));
        }
    }

    @Test
    void deleteFolder_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).deleteFolder(TEST_FOLDER_ID, TEST_USER_ID);

            Result<Void> result = controller.deleteFolder(TEST_FOLDER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已移入回收站", result.getMsg());
            verify(cloudDriveService).deleteFolder(TEST_FOLDER_ID, TEST_USER_ID);
        }
    }

    @Test
    void permanentlyDeleteFolder_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).permanentlyDeleteFolder(TEST_FOLDER_ID, TEST_USER_ID);

            Result<Void> result = controller.permanentlyDeleteFolder(TEST_FOLDER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已永久删除", result.getMsg());
        }
    }

    @Test
    void restoreFolder_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).restoreFolder(TEST_FOLDER_ID, TEST_USER_ID);

            Result<Void> result = controller.restoreFolder(TEST_FOLDER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("恢复成功", result.getMsg());
        }
    }

    @Test
    void restoreFolder_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("恢复失败")).when(cloudDriveService)
                    .restoreFolder(anyLong(), anyLong());

            Result<Void> result = controller.restoreFolder(TEST_FOLDER_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("恢复失败"));
        }
    }

    @Test
    void getFolderList_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFolderVO folder1 = new ImCloudFolderVO();
            folder1.setId(1L);
            folder1.setFolderName("Folder 1");
            ImCloudFolderVO folder2 = new ImCloudFolderVO();
            folder2.setId(2L);
            folder2.setFolderName("Folder 2");

            when(cloudDriveService.getFolderList(0L, "USER", TEST_USER_ID))
                    .thenReturn(Arrays.asList(folder1, folder2));

            Result<List<ImCloudFolderVO>> result = controller.getFolderList(0L, "USER");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
        }
    }

    @Test
    void getFolderList_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(cloudDriveService.getFolderList(anyLong(), anyString(), anyLong()))
                    .thenReturn(Collections.emptyList());

            Result<List<ImCloudFolderVO>> result = controller.getFolderList(0L, "USER");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void getFolderPath_Success() {
        ImCloudFolderVO folder1 = new ImCloudFolderVO();
        folder1.setId(1L);
        folder1.setFolderName("Parent");
        ImCloudFolderVO folder2 = new ImCloudFolderVO();
        folder2.setId(2L);
        folder2.setFolderName("Current");

        when(cloudDriveService.getFolderPath(TEST_FOLDER_ID))
                .thenReturn(Arrays.asList(folder1, folder2));

        Result<List<ImCloudFolderVO>> result = controller.getFolderPath(TEST_FOLDER_ID);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    // ==================== 文件管理测试 ====================

    @Test
    void uploadFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", "text/plain", "test content".getBytes());

            ImCloudFileVO fileVO = new ImCloudFileVO();
            fileVO.setId(TEST_FILE_ID);
            fileVO.setFileName("test.txt");

            when(cloudDriveService.uploadFile(anyLong(), any(), eq(TEST_USER_ID)))
                    .thenReturn(fileVO);

            Result<ImCloudFileVO> result = controller.uploadFile(0L, file);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_FILE_ID, result.getData().getId());
            assertEquals("上传成功", result.getMsg());
        }
    }

    @Test
    void uploadFile_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", "text/plain", "test content".getBytes());

            when(cloudDriveService.uploadFile(anyLong(), any(), eq(TEST_USER_ID)))
                    .thenThrow(new RuntimeException("存储空间不足"));

            Result<ImCloudFileVO> result = controller.uploadFile(0L, file);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("存储空间不足"));
        }
    }

    @Test
    void uploadFiles_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            MockMultipartFile file1 = new MockMultipartFile(
                    "files", "test1.txt", "text/plain", "content1".getBytes());
            MockMultipartFile file2 = new MockMultipartFile(
                    "files", "test2.txt", "text/plain", "content2".getBytes());

            ImCloudFileVO fileVO1 = new ImCloudFileVO();
            fileVO1.setId(1L);
            fileVO1.setFileName("test1.txt");
            ImCloudFileVO fileVO2 = new ImCloudFileVO();
            fileVO2.setId(2L);
            fileVO2.setFileName("test2.txt");

            when(cloudDriveService.uploadFile(anyLong(), any(), eq(TEST_USER_ID)))
                    .thenReturn(fileVO1)
                    .thenReturn(fileVO2);

            Result<List<ImCloudFileVO>> result = controller.uploadFiles(0L,
                    new MockMultipartFile[]{file1, file2});

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
            assertEquals("上传成功", result.getMsg());
        }
    }

    @Test
    void renameFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).renameFile(TEST_FILE_ID, "New Name.txt", TEST_USER_ID);

            Result<Void> result = controller.renameFile(TEST_FILE_ID, "New Name.txt");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("重命名成功", result.getMsg());
        }
    }

    @Test
    void deleteFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).deleteFile(TEST_FILE_ID, TEST_USER_ID);

            Result<Void> result = controller.deleteFile(TEST_FILE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已移入回收站", result.getMsg());
        }
    }

    @Test
    void permanentlyDeleteFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).permanentlyDeleteFile(TEST_FILE_ID, TEST_USER_ID);

            Result<Void> result = controller.permanentlyDeleteFile(TEST_FILE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已永久删除", result.getMsg());
        }
    }

    @Test
    void restoreFile_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).restoreFile(TEST_FILE_ID, TEST_USER_ID);

            Result<Void> result = controller.restoreFile(TEST_FILE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("恢复成功", result.getMsg());
        }
    }

    @Test
    void restoreFile_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("文件不在回收站中")).when(cloudDriveService)
                    .restoreFile(anyLong(), anyLong());

            Result<Void> result = controller.restoreFile(TEST_FILE_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("文件不在回收站中"));
        }
    }

    @Test
    void moveFiles_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileMoveRequest request = new ImCloudFileMoveRequest();
            request.setFileIds(Arrays.asList(1L, 2L, 3L));
            request.setTargetFolderId(TEST_FOLDER_ID);

            doNothing().when(cloudDriveService).moveFiles(any(ImCloudFileMoveRequest.class), eq(TEST_USER_ID));

            Result<Void> result = controller.moveFiles(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("移动成功", result.getMsg());
        }
    }

    @Test
    void getFileList_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileVO file1 = new ImCloudFileVO();
            file1.setId(1L);
            file1.setFileName("file1.txt");
            ImCloudFileVO file2 = new ImCloudFileVO();
            file2.setId(2L);
            file2.setFileName("file2.jpg");

            when(cloudDriveService.getFileList(TEST_FOLDER_ID, TEST_USER_ID))
                    .thenReturn(Arrays.asList(file1, file2));

            Result<List<ImCloudFileVO>> result = controller.getFileList(TEST_FOLDER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
        }
    }

    @Test
    void searchFiles_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileVO file = new ImCloudFileVO();
            file.setId(1L);
            file.setFileName("document.pdf");

            when(cloudDriveService.searchFiles("doc", null, TEST_USER_ID))
                    .thenReturn(Arrays.asList(file));

            Result<List<ImCloudFileVO>> result = controller.searchFiles("doc", null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
        }
    }

    @Test
    void searchFiles_WithFileType() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileVO file = new ImCloudFileVO();
            file.setId(1L);
            file.setFileName("image.jpg");
            file.setFileType("image");

            when(cloudDriveService.searchFiles("image", "image", TEST_USER_ID))
                    .thenReturn(Arrays.asList(file));

            Result<List<ImCloudFileVO>> result = controller.searchFiles("image", "image");

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
        }
    }

    @Test
    void searchFiles_EmptyResult() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(cloudDriveService.searchFiles(anyString(), any(), anyLong()))
                    .thenReturn(Collections.emptyList());

            Result<List<ImCloudFileVO>> result = controller.searchFiles("nonexistent", null);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void getRecentFiles_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileVO file = new ImCloudFileVO();
            file.setId(1L);
            file.setFileName("recent.pdf");

            when(cloudDriveService.getRecentFiles(TEST_USER_ID, 10))
                    .thenReturn(Arrays.asList(file));

            Result<List<ImCloudFileVO>> result = controller.getRecentFiles(10);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.getData().size());
        }
    }

    @Test
    void getRecentFiles_DefaultLimit() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(cloudDriveService.getRecentFiles(TEST_USER_ID, 10))
                    .thenReturn(Collections.emptyList());

            Result<List<ImCloudFileVO>> result = controller.getRecentFiles(10);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(cloudDriveService).getRecentFiles(TEST_USER_ID, 10);
        }
    }

    @Test
    void getRecycleBin_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileVO file1 = new ImCloudFileVO();
            file1.setId(1L);
            file1.setFileName("deleted1.txt");
            ImCloudFileVO file2 = new ImCloudFileVO();
            file2.setId(2L);
            file2.setFileName("deleted2.jpg");

            when(cloudDriveService.getRecycleBin(TEST_USER_ID))
                    .thenReturn(Arrays.asList(file1, file2));

            Result<List<ImCloudFileVO>> result = controller.getRecycleBin();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
        }
    }

    @Test
    void saveFromMessage_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            Long messageId = 5001L;
            ImCloudFileVO fileVO = new ImCloudFileVO();
            fileVO.setId(TEST_FILE_ID);
            fileVO.setFileName("message_attachment.pdf");

            when(cloudDriveService.saveFromMessage(messageId, TEST_FOLDER_ID, TEST_USER_ID))
                    .thenReturn(fileVO);

            Result<ImCloudFileVO> result = controller.saveFromMessage(messageId, TEST_FOLDER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_FILE_ID, result.getData().getId());
            assertEquals("保存成功", result.getMsg());
        }
    }

    // ==================== 文件分享测试 ====================

    @Test
    void createShare_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileShareRequest request = new ImCloudFileShareRequest();
            request.setResourceId(TEST_FILE_ID);
            request.setExpireDays(7);

            ImCloudFileShareVO shareVO = new ImCloudFileShareVO();
            shareVO.setId(TEST_SHARE_ID);
            shareVO.setShareLink("http://example.com/share/ABC123");

            when(cloudDriveService.createShare(any(ImCloudFileShareRequest.class), eq(TEST_USER_ID)))
                    .thenReturn(shareVO);

            Result<ImCloudFileShareVO> result = controller.createShare(request);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(TEST_SHARE_ID, result.getData().getId());
            assertEquals("创建成功", result.getMsg());
        }
    }

    @Test
    void createShare_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileShareRequest request = new ImCloudFileShareRequest();
            request.setResourceId(999L);

            when(cloudDriveService.createShare(any(ImCloudFileShareRequest.class), eq(TEST_USER_ID)))
                    .thenThrow(new RuntimeException("文件不存在"));

            Result<ImCloudFileShareVO> result = controller.createShare(request);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("文件不存在"));
        }
    }

    @Test
    void cancelShare_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doNothing().when(cloudDriveService).cancelShare(TEST_SHARE_ID, TEST_USER_ID);

            Result<Void> result = controller.cancelShare(TEST_SHARE_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("已取消分享", result.getMsg());
        }
    }

    @Test
    void cancelShare_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            doThrow(new RuntimeException("分享不存在")).when(cloudDriveService)
                    .cancelShare(anyLong(), anyLong());

            Result<Void> result = controller.cancelShare(TEST_SHARE_ID);

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("分享不存在"));
        }
    }

    @Test
    void getShareList_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudFileShareVO share1 = new ImCloudFileShareVO();
            share1.setId(1L);
            share1.setShareLink("http://example.com/share/CODE1");
            ImCloudFileShareVO share2 = new ImCloudFileShareVO();
            share2.setId(2L);
            share2.setShareLink("http://example.com/share/CODE2");

            when(cloudDriveService.getShareList(TEST_USER_ID))
                    .thenReturn(Arrays.asList(share1, share2));

            Result<List<ImCloudFileShareVO>> result = controller.getShareList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.getData().size());
        }
    }

    @Test
    void getShareList_EmptyList() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(cloudDriveService.getShareList(TEST_USER_ID))
                    .thenReturn(Collections.emptyList());

            Result<List<ImCloudFileShareVO>> result = controller.getShareList();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.getData().isEmpty());
        }
    }

    @Test
    void accessShare_Success() {
        String shareCode = "TEST123";
        ImCloudFileShareVO shareVO = new ImCloudFileShareVO();
        shareVO.setId(TEST_SHARE_ID);
        shareVO.setShareLink("http://example.com/share/" + shareCode);

        when(cloudDriveService.accessShare(shareCode, null))
                .thenReturn(shareVO);

        Result<ImCloudFileShareVO> result = controller.accessShare(shareCode, null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(TEST_SHARE_ID, result.getData().getId());
    }

    @Test
    void accessShare_WithPassword() {
        String shareCode = "TEST123";
        String password = "secret";
        ImCloudFileShareVO shareVO = new ImCloudFileShareVO();
        shareVO.setId(TEST_SHARE_ID);
        shareVO.setShareLink("http://example.com/share/" + shareCode);

        when(cloudDriveService.accessShare(shareCode, password))
                .thenReturn(shareVO);

        Result<ImCloudFileShareVO> result = controller.accessShare(shareCode, password);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(TEST_SHARE_ID, result.getData().getId());
    }

    @Test
    void accessShare_ServiceThrowsException() {
        String shareCode = "INVALID";
        when(cloudDriveService.accessShare(eq(shareCode), any()))
                .thenThrow(new RuntimeException("分享已过期"));

        Result<ImCloudFileShareVO> result = controller.accessShare(shareCode, null);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMsg().contains("分享已过期"));
    }

    // ==================== 存储配额测试 ====================

    @Test
    void getStorageQuota_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            ImCloudStorageQuotaVO quota = new ImCloudStorageQuotaVO();
            quota.setTotalCapacity(10737418240L); // 10GB
            quota.setUsedCapacity(5368709120L);   // 5GB
            quota.setAvailableCapacity(5368709120L); // 5GB

            when(cloudDriveService.getStorageQuota(TEST_USER_ID))
                    .thenReturn(quota);

            Result<ImCloudStorageQuotaVO> result = controller.getStorageQuota();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(10737418240L, result.getData().getTotalCapacity());
            assertEquals(5368709120L, result.getData().getUsedCapacity());
        }
    }

    @Test
    void getStorageQuota_ServiceThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMockedStatic = mockStatic(SecurityUtils.class)) {
            securityUtilsMockedStatic.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);

            when(cloudDriveService.getStorageQuota(TEST_USER_ID))
                    .thenThrow(new RuntimeException("获取配额失败"));

            Result<ImCloudStorageQuotaVO> result = controller.getStorageQuota();

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMsg().contains("获取失败"));
        }
    }
}
