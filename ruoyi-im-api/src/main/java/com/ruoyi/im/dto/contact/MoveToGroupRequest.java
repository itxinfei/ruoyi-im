package com.ruoyi.im.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 移动到分组请求
 *
 * @author ruoyi
 */
@Schema(description = "移动到分组请求")
@Validated
public class MoveToGroupRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 好友ID列表
     */
    @Schema(description = "好友ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "好友ID列表不能为空")
    private List<Long> friendIds;

    /**
     * 目标分组名称
     */
    @Schema(description = "目标分组名称", example = "工作同事")
    private String groupName;

    /**
     * 获取好友ID列表
     */
    public List<Long> getFriendIds() {
        return friendIds;
    }

    /**
     * 设置好友ID列表
     */
    public void setFriendIds(List<Long> friendIds) {
        this.friendIds = friendIds;
    }

    /**
     * 获取目标分组名称
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * 设置目标分组名称
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
