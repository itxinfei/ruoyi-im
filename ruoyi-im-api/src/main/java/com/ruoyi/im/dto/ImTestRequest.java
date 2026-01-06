package com.ruoyi.im.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * IM测试请求DTO（示例）
 *
 * @author ruoyi
 */
public class ImTestRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 名称 */
    @NotBlank(message = "名称不能为空")
    @Size(max = 50, message = "名称长度不能超过50个字符")
    private String name;

    /** 描述 */
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
