package com.ruoyi.im.dto.group;

import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 群组更新请求
 *
 * @author ruoyi
 */
@Data
public class ImGroupUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组名称
     */
    @Size(max = 50, message = "群组名称长度不能超过50个字符")
    private String name;

    /**
     * 群头像
     */
    @Size(max = 255, message = "群头像URL长度不能超过255个字符")
    private String avatar;

    /**
     * 群公告
     */
    @Size(max = 500, message = "群公告长度不能超过500个字符")
    private String notice;

    /**
     * 群组描述
     */
    @Size(max = 500, message = "群组描述长度不能超过500个字符")
    private String description;

    /**
     * 成员数量限制
     */
    private Integer memberLimit;
}
