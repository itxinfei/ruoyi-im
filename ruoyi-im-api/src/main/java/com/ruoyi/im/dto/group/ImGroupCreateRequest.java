package com.ruoyi.im.dto.group;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 群组创建请求
 *
 * @author ruoyi
 */
@Data
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
}
