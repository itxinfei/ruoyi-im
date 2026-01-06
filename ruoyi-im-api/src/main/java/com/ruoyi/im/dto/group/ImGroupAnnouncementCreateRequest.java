package com.ruoyi.im.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 群组公告创建请求DTO
 *
 * @author ruoyi
 */
@Data
@Schema(description = "群组公告创建请求")
public class ImGroupAnnouncementCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "群组ID", required = true)
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    @Schema(description = "公告内容", required = true)
    @NotBlank(message = "公告内容不能为空")
    private String content;

    @Schema(description = "公告类型: 1=普通公告, 2=系统公告")
    private Integer type = 1;

    @Schema(description = "是否置顶")
    private Integer isPinned = 0;

    @Schema(description = "附件URL")
    private String attachmentUrl;

    @Schema(description = "过期时间（分钟），0表示永久")
    private Integer expireMinutes = 0;
}
