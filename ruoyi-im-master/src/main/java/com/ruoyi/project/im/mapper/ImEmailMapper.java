package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.ImEmail;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

/**
 * 邮件Mapper接口
 *
 * @author ruoyi
 * @date 2025-01-15
 */
@Mapper
public interface ImEmailMapper {

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
     * 批量更新已读状态
     */
    int batchUpdateIsRead(List<Long> ids, Integer isRead);

    /**
     * 批量更新文件夹
     */
    int batchUpdateFolder(List<Long> ids, String folder);

    /**
     * 删除单个邮件
     */
    int deleteImEmailById(Long id);

    /**
     * 根据文件夹删除邮件
     */
    int deleteEmailsByFolder(String folder);

    /**
     * 统计邮件数量
     */
    int countEmails(ImEmail imEmail);
}
