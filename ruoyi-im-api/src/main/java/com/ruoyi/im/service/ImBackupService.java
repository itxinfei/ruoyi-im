package com.ruoyi.im.service;

import java.util.List;
import java.util.Map;

/**
 * 数据备份服务接口
 *
 * @author ruoyi
 */
public interface ImBackupService {

    /**
     * 获取备份列表
     *
     * @return 备份列表
     */
    List<Map<String, Object>> getBackupList();

    /**
     * 创建备份
     *
     * @param description 备份描述
     * @return 备份信息
     */
    Map<String, Object> createBackup(String description);

    /**
     * 恢复备份
     *
     * @param backupId 备份ID
     */
    void restoreBackup(Long backupId);

    /**
     * 删除备份
     *
     * @param backupId 备份ID
     */
    void deleteBackup(Long backupId);

    /**
     * 获取备份详情
     *
     * @param backupId 备份ID
     * @return 备份详情
     */
    Map<String, Object> getBackupDetail(Long backupId);

    /**
     * 导出用户数据
     *
     * @param userId 用户ID
     * @return 导出结果
     */
    Map<String, Object> exportUserData(Long userId);

    /**
     * 获取备份统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getBackupStatistics();
}
