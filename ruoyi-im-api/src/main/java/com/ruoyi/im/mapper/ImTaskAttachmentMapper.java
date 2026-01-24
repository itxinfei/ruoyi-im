package com.ruoyi.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.im.domain.ImTaskAttachment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务附件Mapper
 *
 * @author ruoyi
 */
public interface ImTaskAttachmentMapper extends BaseMapper<ImTaskAttachment> {

    /**
     * 查询任务附件列表（带上传者信息）
     *
     * @param taskId 任务ID
     * @return 附件列表
     */
    List<ImTaskAttachment> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计任务附件数
     *
     * @param taskId 任务ID
     * @return 附件数
     */
    int countByTaskId(@Param("taskId") Long taskId);
}
