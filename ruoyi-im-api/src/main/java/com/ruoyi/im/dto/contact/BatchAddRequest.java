package com.ruoyi.im.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 批量添加好友请求
 *
 * @author ruoyi
 */
@Schema(description = "批量添加好友请求")
@Validated
public class BatchAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID列表
     */
    @Schema(description = "用户ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    /**
     * 验证消息/备注
     */
    @Schema(description = "验证消息/备注", example = "我是张三")
    private String remark;

    /**
     * 获取用户ID列表
     */
    public List<Long> getUserIds() {
        return userIds;
    }

    /**
     * 设置用户ID列表
     */
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    /**
     * 获取验证消息/备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置验证消息/备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
