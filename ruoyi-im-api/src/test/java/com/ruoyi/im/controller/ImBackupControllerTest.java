package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImBackupService;
import com.ruoyi.im.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ImBackupController 单元测试
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class ImBackupControllerTest {

    @Mock
    private ImBackupService imBackupService;

    @InjectMocks
    private ImBackupController imBackupController;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long ADMIN_USER_ID = 1L;

    /**
     * 测试获取备份列表 - 成功
     */
    @Test
    void testList_Success() {
        List<Map<String, Object>> backupList = Arrays.asList(
                createBackupMap(1L, "backup_001", 1024L),
                createBackupMap(2L, "backup_002", 2048L)
        );
        when(imBackupService.getBackupList()).thenReturn(backupList);

        Result<List<Map<String, Object>>> result = imBackupController.list();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(imBackupService).getBackupList();
    }

    /**
     * 测试获取备份列表 - 空列表
     */
    @Test
    void testList_Empty() {
        when(imBackupService.getBackupList()).thenReturn(Collections.emptyList());

        Result<List<Map<String, Object>>> result = imBackupController.list();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    /**
     * 测试创建备份 - 成功
     */
    @Test
    void testCreateBackup_Success() {
        Map<String, Object> backupResult = createBackupMap(1L, "new_backup", 1024L);
        when(imBackupService.createBackup("测试备份")).thenReturn(backupResult);

        Result<Map<String, Object>> result = imBackupController.createBackup("测试备份");

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("备份创建成功", result.getMsg());
        assertEquals(1L, result.getData().get("id"));
        verify(imBackupService).createBackup("测试备份");
    }

    /**
     * 测试创建备份 - 无描述
     */
    @Test
    void testCreateBackup_NoDescription() {
        Map<String, Object> backupResult = createBackupMap(1L, "new_backup", 1024L);
        when(imBackupService.createBackup(null)).thenReturn(backupResult);

        Result<Map<String, Object>> result = imBackupController.createBackup(null);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(imBackupService).createBackup(null);
    }

    /**
     * 测试恢复备份 - 成功
     */
    @Test
    void testRestoreBackup_Success() {
        doNothing().when(imBackupService).restoreBackup(1L);

        Result<Void> result = imBackupController.restoreBackup(1L);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("恢复成功", result.getMsg());
        verify(imBackupService).restoreBackup(1L);
    }

    /**
     * 测试恢复备份 - 备份不存在
     */
    @Test
    void testRestoreBackup_NotFound() {
        doThrow(new BusinessException("备份不存在")).when(imBackupService).restoreBackup(999L);

        assertThrows(BusinessException.class, () -> {
            imBackupController.restoreBackup(999L);
        });
    }

    /**
     * 测试删除备份 - 成功
     */
    @Test
    void testDeleteBackup_Success() {
        doNothing().when(imBackupService).deleteBackup(1L);

        Result<Void> result = imBackupController.deleteBackup(1L);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("删除成功", result.getMsg());
        verify(imBackupService).deleteBackup(1L);
    }

    /**
     * 测试删除备份 - 备份不存在
     */
    @Test
    void testDeleteBackup_NotFound() {
        doThrow(new BusinessException("备份不存在")).when(imBackupService).deleteBackup(999L);

        assertThrows(BusinessException.class, () -> {
            imBackupController.deleteBackup(999L);
        });
    }

    /**
     * 测试获取备份详情 - 成功
     */
    @Test
    void testGetBackupDetail_Success() {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", 1L);
        detail.put("fileName", "backup_001.sql");
        detail.put("fileSize", 1024L);
        detail.put("recordCount", 1000);
        detail.put("checksum", "abc123");
        detail.put("createTime", "2026-03-27 10:00:00");

        when(imBackupService.getBackupDetail(1L)).thenReturn(detail);

        Result<Map<String, Object>> result = imBackupController.getBackupDetail(1L);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1L, result.getData().get("id"));
        assertEquals("backup_001.sql", result.getData().get("fileName"));
        verify(imBackupService).getBackupDetail(1L);
    }

    /**
     * 测试获取备份详情 - 不存在
     */
    @Test
    void testGetBackupDetail_NotFound() {
        when(imBackupService.getBackupDetail(999L))
                .thenThrow(new BusinessException("备份不存在"));

        assertThrows(BusinessException.class, () -> {
            imBackupController.getBackupDetail(999L);
        });
    }

    /**
     * 测试导出用户数据 - 本人导出成功
     */
    @Test
    void testExportUserData_OwnUser() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("USER");

            Map<String, Object> exportResult = new HashMap<>();
            exportResult.put("fileName", "user_data_1001.zip");
            exportResult.put("fileSize", 512L);
            exportResult.put("recordCount", 500);

            when(imBackupService.exportUserData(TEST_USER_ID)).thenReturn(exportResult);

            Result<Map<String, Object>> result = imBackupController.exportUserData(TEST_USER_ID);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("导出成功", result.getMsg());
            assertEquals("user_data_1001.zip", result.getData().get("fileName"));
            verify(imBackupService).exportUserData(TEST_USER_ID);
        }
    }

    /**
     * 测试导出用户数据 - 管理员导出他人数据
     */
    @Test
    void testExportUserData_AdminOtherUser() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(ADMIN_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("ADMIN");

            Map<String, Object> exportResult = new HashMap<>();
            exportResult.put("fileName", "user_data_1001.zip");
            exportResult.put("fileSize", 512L);

            when(imBackupService.exportUserData(1002L)).thenReturn(exportResult);

            Result<Map<String, Object>> result = imBackupController.exportUserData(1002L);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(imBackupService).exportUserData(1002L);
        }
    }

    /**
     * 测试导出用户数据 - 普通用户导出他人数据无权限
     */
    @Test
    void testExportUserData_NoPermission() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(TEST_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("USER");

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                imBackupController.exportUserData(1002L); // 尝试导出其他用户
            });

            assertEquals("无权限导出其他用户数据", exception.getMessage());
        }
    }

    /**
     * 测试导出用户数据 - 超级管理员导出他人数据
     */
    @Test
    void testExportUserData_SuperAdmin() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(ADMIN_USER_ID);
            mockedSecurityUtils.when(SecurityUtils::getLoginUserRole).thenReturn("SUPER_ADMIN");

            Map<String, Object> exportResult = new HashMap<>();
            exportResult.put("fileName", "user_data_1002.zip");

            when(imBackupService.exportUserData(1002L)).thenReturn(exportResult);

            Result<Map<String, Object>> result = imBackupController.exportUserData(1002L);

            assertNotNull(result);
            assertTrue(result.isSuccess());
            verify(imBackupService).exportUserData(1002L);
        }
    }

    /**
     * 测试获取备份统计信息 - 成功
     */
    @Test
    void testGetStatistics_Success() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBackups", 10);
        stats.put("totalSize", 10240L);
        stats.put("lastBackupTime", "2026-03-27 10:00:00");
        stats.put("todayBackups", 1);

        when(imBackupService.getBackupStatistics()).thenReturn(stats);

        Result<Map<String, Object>> result = imBackupController.getStatistics();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(10, result.getData().get("totalBackups"));
        assertEquals(10240L, result.getData().get("totalSize"));
        verify(imBackupService).getBackupStatistics();
    }

    /**
     * 测试获取备份统计信息 - 无数据
     */
    @Test
    void testGetStatistics_NoData() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBackups", 0);
        stats.put("totalSize", 0L);
        stats.put("lastBackupTime", null);
        stats.put("todayBackups", 0);

        when(imBackupService.getBackupStatistics()).thenReturn(stats);

        Result<Map<String, Object>> result = imBackupController.getStatistics();

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getData().get("totalBackups"));
    }

    /**
     * 创建测试用备份Map
     */
    private Map<String, Object> createBackupMap(Long id, String fileName, Long fileSize) {
        Map<String, Object> backup = new HashMap<>();
        backup.put("id", id);
        backup.put("fileName", fileName);
        backup.put("fileSize", fileSize);
        backup.put("createTime", "2026-03-27 10:00:00");
        return backup;
    }
}
