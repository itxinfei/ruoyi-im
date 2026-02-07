package com.ruoyi.im.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * 基础创建请求
 * 所有创建请求的基类
 *
 * @author ruoyi
 */
@Schema(description = "基础创建请求")
@Validated
public class BaseCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "这是一条备注")
    private String remark;

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
