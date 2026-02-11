package com.ruoyi.im.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 文档协作权限更新请求
 *
 * @author ruoyi
 */
@Data
@Schema(description = "文档协作权限更新请求")
public class DocumentPermissionUpdateRequest {

    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档ID", required = true)
    private Long documentId;

    @NotEmpty(message = "协作者列表不能为空")
    @Schema(description = "协作者列表", required = true)
    private List<CollaboratorPermission> collaborators;

    @Data
    @Schema(description = "协作者权限")
    public static class CollaboratorPermission {
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID", required = true)
        private Long userId;

        @Schema(description = "权限类型: read=只读, write=可编辑, owner=所有者")
        private String permission;
    }
}