package com.ruoyi.im.dto.document;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 添加协作者请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImDocumentCollaboratorRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    
    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    /**
     * 用户ID列表
     */
    
    private List<Long> userIds;

    /**
     * 协作权限：EDIT编辑, COMMENT评论, VIEW查看
     */
    
    @NotBlank(message = "协作权限不能为空")
    private String permission;
}

