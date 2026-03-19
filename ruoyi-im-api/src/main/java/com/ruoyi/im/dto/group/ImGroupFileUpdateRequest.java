package com.ruoyi.im.dto.group;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 群组文件更新请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImGroupFileUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    
    private String category;

    
    private String permission;
}

