package com.ruoyi.web.service;

import com.ruoyi.web.domain.ImEmail;

import java.util.List;
import java.util.Map;

/**
 * 邮件Service接口
 *
 * @author ruoyi
 * @date 2025-01-15
 */
public interface ImEmailService {

    /**
     * 查询邮件列表
     */
    List<ImEmail> selectImEmailList(ImEmail imEmail);

    /**
     * 根据ID获取邮件
     */
    ImEmail selectImEmailById(Long id);

    /**
     * 新增邮件
     */
    int insertImEmail(ImEmail imEmail);

    /**
     * 修改邮件
     */
    int updateImEmail(ImEmail imEmail);

    /**
     * 删除邮件
     */
    int deleteImEmailByIds(Long[] ids);

    /**
     * 获取邮件统计
     */
    Map<String, Object> getEmailStatistics();

    /**
     * 标记邮件为已读
     */
    int markAsRead(Long id);

    /**
     * 批量标记邮件为已读
     */
    int batchMarkAsRead(List<Long> ids);

    /**
     * 标记邮件为未读
     */
    int markAsUnread(Long id);

    /**
     * 标记邮件为重要
     */
    int markAsStarred(Long id);

    /**
     * 取消标记邮件为重要
     */
    int unmarkAsStarred(Long id);

    /**
     * 移动邮件到垃圾箱
     */
    int moveToTrash(Long id);

    /**
     * 批量移动到垃圾箱
     */
    int batchMoveToTrash(List<Long> ids);

    /**
     * 从垃圾箱恢复邮件
     */
    int restoreFromTrash(Long id);

    /**
     * 永久删除邮件
     */
    int permanentDelete(Long id);

    /**
     * 清空垃圾箱
     */
    int clearTrash();

    /**
     * 根据文件夹获取邮件
     */
    List<ImEmail> getEmailsByFolder(String folder);

    /**
     * 获取未读邮件数量
     */
    int getUnreadCount();
}
