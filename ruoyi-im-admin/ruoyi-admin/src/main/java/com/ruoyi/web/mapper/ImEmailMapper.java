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
}
