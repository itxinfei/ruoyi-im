package com.ruoyi.im.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 邮件保存草稿请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "邮件保存草稿请求")
public class EmailSaveDraftRequest {

    @NotBlank(message = "邮件主题不能为空")
    @Schema(description = "邮件主题", required = true)
    private String subject;

    @Schema(description = "邮件内容")
    private String content;
}