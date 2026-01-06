package com.ruoyi.im.dto.group;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 群组创建请求
 *
 * @author ruoyi
 */
public class ImGroupCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组名称
     */
    @NotBlank(message = "群组名称不能为空")
    @Size(max = 50, message = "群组名称长度不能超过50个字符")
    private String name;

    /**
     * 群头像
     */
    @Size(max = 255, message = "群头像URL长度不能超过255个字符")
    private String avatar;

    /**
     * 群组描述
     */
    @Size(max = 500, message = "群组描述长度不能超过500个字符")
    private String description;

    /**
     * 群组类型（PUBLIC公开群 PRIVATE私密群）
     */
    private String type;

    /**
     * 成员数量限制
     */
    private Integer memberLimit;

    /**
     * 初始成员ID列表
     */
    private List<Long> memberIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(Integer memberLimit) {
        this.memberLimit = memberLimit;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}
