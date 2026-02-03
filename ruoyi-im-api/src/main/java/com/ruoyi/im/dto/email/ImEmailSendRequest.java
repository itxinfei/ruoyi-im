package com.ruoyi.im.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 邮件发送请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "邮件发送请求")
public class ImEmailSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收件人ID列表
     */
    @Schema(description = "收件人ID列表")
    @NotEmpty(message = "收件人不能为空")
    private List<Long> toIds;

    /**
     * 抄送ID列表
     */
    @Schema(description = "抄送ID列表")
    private List<Long> ccIds;

    /**
     * 密送ID列表
     */
    @Schema(description = "密送ID列表")
    private List<Long> bccIds;

    /**
     * 邮件主题
     */
    @Schema(description = "邮件主题")
    @NotBlank(message = "主题不能为空")
    private String subject;

    /**
     * 邮件内容
     */
    @Schema(description = "邮件内容")
    private String content;

    /**
     * 附件ID列表
     */
    @Schema(description = "附件ID列表")
    private List<Long> attachmentIds;
}
