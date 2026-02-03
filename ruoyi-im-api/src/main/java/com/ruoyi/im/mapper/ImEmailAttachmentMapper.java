package com.ruoyi.im.mapper;

import com.ruoyi.im.domain.ImEmailAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 邮件附件Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface ImEmailAttachmentMapper {

    /**
     * 查询邮件的所有附件
     *
     * @param emailId 邮件ID
     * @return 附件列表
     */
    List<ImEmailAttachment> selectAttachmentsByEmailId(Long emailId);

    /**
     * 查询附件详情
     *
     * @param id 附件ID
     * @return 附件信息
     */
    ImEmailAttachment selectAttachmentById(Long id);

    /**
     * 插入附件
     *
     * @param attachment 附件信息
     * @return 影响行数
     */
    int insertAttachment(ImEmailAttachment attachment);

    /**
     * 批量插入附件
     *
     * @param attachments 附件列表
     * @return 影响行数
     */
    int batchInsertAttachments(@Param("attachments") List<ImEmailAttachment> attachments);

    /**
     * 删除附件
     *
     * @param id 附件ID
     * @return 影响行数
     */
    int deleteAttachmentById(Long id);

    /**
     * 删除邮件的所有附件
     *
     * @param emailId 邮件ID
     * @return 影响行数
     */
    int deleteAttachmentsByEmailId(Long emailId);
}
