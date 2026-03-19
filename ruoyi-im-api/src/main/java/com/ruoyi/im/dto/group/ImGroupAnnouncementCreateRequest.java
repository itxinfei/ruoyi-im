package com.ruoyi.im.dto.group;

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

public class ImGroupAnnouncementCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotNull(message = "群组ID不能为空")
    private Long groupId;

    
    @NotBlank(message = "公告内容不能为空")
    private String content;

    
    private Integer type = 1;

    
    private Integer isPinned = 0;

    
    private String attachmentUrl;

    
    private Integer expireMinutes = 0;
}

