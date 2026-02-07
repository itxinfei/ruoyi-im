package com.ruoyi.im.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 基础更新请求
 * 所有更新请求的基类
 *
 * @author ruoyi
 */
@Schema(description = "基础更新请求")
@Validated
public class BaseUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实体ID
     */
    @Schema(description = "实体ID", required = true, example = "1")
    @NotNull(message = "ID不能为空")
    private Long id;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "这是一条备注")
    private String remark;

    /**
     * 获取ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取备注信息
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注信息
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
