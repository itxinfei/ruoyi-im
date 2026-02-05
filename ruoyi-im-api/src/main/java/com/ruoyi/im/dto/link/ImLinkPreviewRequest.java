package com.ruoyi.im.dto.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 链接预览请求参数
 *
 * @author ruoyi
 */
@Data
@Schema(description = "链接预览请求")
public class ImLinkPreviewRequest {

    @NotBlank(message = "URL不能为空")
    @Pattern(regexp = "^https?://.*", message = "URL格式不正确，必须以http://或https://开头")
    @Size(max = 2048, message = "URL长度不能超过2048字符")
    @Schema(description = "链接地址", example = "https://www.example.com")
    private String url;
}
