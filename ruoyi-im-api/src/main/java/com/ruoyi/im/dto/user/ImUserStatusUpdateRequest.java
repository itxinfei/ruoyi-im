package com.ruoyi.im.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 用户状态更新请求DTO
 *
 * @author ruoyi
 */
@Data

public class ImUserStatusUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    
    private Long id;

    /**
     * 用户状态
     */
    
    @NotBlank(message = "用户状态不能为空")
    @Pattern(regexp = "^(ENABLED|DISABLED)$", message = "用户状态只能是ENABLED或DISABLED")
    private String status;
}

