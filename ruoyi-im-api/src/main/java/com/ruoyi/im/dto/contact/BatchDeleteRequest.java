package com.ruoyi.im.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求
 *
 * @author ruoyi
 */
@Schema(description = "批量删除请求")
@Validated
public class BatchDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 联系人ID列表
     */
    @Schema(description = "联系人ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "联系人ID列表不能为空")
    private List<Long> contactIds;

    /**
     * 获取联系人ID列表
     */
    public List<Long> getContactIds() {
        return contactIds;
    }

    /**
     * 设置联系人ID列表
     */
    public void setContactIds(List<Long> contactIds) {
        this.contactIds = contactIds;
    }
}
