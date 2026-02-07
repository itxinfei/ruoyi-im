package com.ruoyi.im.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 基础批量操作请求
 * 所有批量操作请求的基类
 *
 * @author ruoyi
 */
@Schema(description = "基础批量操作请求")
@Validated
public class BaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID列表
     */
    @Schema(description = "ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;

    /**
     * 获取ID列表
     */
    public List<Long> getIds() {
        return ids;
    }

    /**
     * 设置ID列表
     */
    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
