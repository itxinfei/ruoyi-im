package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImBackup;

import java.util.List;
import java.util.Map;

/**
 * IM数据备份Service接口（Admin模块专用）
 *
 * <p>提供数据备份的核心业务功能，包括手动备份、自动备份、备份文件管理等操作</p>
 * <p>支持数据库全量备份、核心表备份、备份文件下载、删除、恢复等功能</p>
 *
 * @author ruoyi
 */
public interface ImBackupService {

    /**
     * 查询备份记录列表
     *
     * @param backup 查询条件对象
     * @return 备份记录列表
     */
    List<ImBackup> selectBackupList(ImBackup backup);

    /**
     * 根据ID查询备份记录
     *
     * @param id 备份记录ID
     * @return 备份记录对象
     */
    ImBackup selectBackupById(Long id);

    /**
     * 手动备份核心数据
     *
     * <p>执行数据库全量备份，使用 mysqldump 命令</p>
     * <p>备份文件保存到配置的备份目录</p>
     *
     * @param description 备份描述
     * @param operator 操作人
     * @return 备份记录对象
     */
    ImBackup manualBackup(String description, String operator);

    /**
     * 定时自动备份
     *
     * <p>由定时任务调用，执行自动备份</p>
     * <p>根据备份类型（每日/每周）执行相应的备份操作</p>
     *
     * @param backupType 备份类型：AUTO_DAILY 或 AUTO_WEEKLY
     * @return 备份记录对象
     */
    ImBackup autoBackup(String backupType);

    /**
     * 删除备份记录
     *
     * @param id 备份记录ID
     * @return 影响的记录数
     */
    int deleteBackupById(Long id);

    /**
     * 批量删除备份记录
     *
     * @param ids 备份记录ID数组
     * @return 影响的记录数
     */
    int deleteBackupByIds(Long[] ids);

    /**
     * 获取备份文件路径
     *
     * @param backupId 备份记录ID
     * @return 备份文件的完整路径
     */
    String getBackupFilePath(Long backupId);

    /**
     * 恢复数据
     *
     * <p>从备份文件恢复数据到数据库</p>
     * <p>注意：此操作会覆盖当前数据库数据，请谨慎使用</p>
     *
     * @param backupId 备份记录ID
     * @return 恢复是否成功
     */
    boolean restoreBackup(Long backupId);

    /**
     * 获取备份统计信息
     *
     * @return 统计信息，包含总备份数、总占用空间、今日备份数等
     */
    Map<String, Object> getBackupStatistics();
}
