package com.ruoyi.im.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 分组重命名请求
 *
 * @author ruoyi
 */
@Schema(description = "分组重命名请求")
@Validated
public class GroupRenameRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 新分组名称
     */
    @Schema(description = "新分组名称", required = true, example = "工作同事")
    @NotBlank(message = "新分组名称不能为空")
    private String newName;

    /**
     * 获取新分组名称
     */
    public String getNewName() {
        return newName;
    }

    /**
     * 设置新分组名称
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }
}
