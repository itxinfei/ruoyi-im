package com.ruoyi.im.dto.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

/**
 * 好友标签更新请求
 *
 * @author ruoyi
 */
@Schema(description = "好友标签更新请求")
@Validated
public class FriendTagsUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表", example = "[\"重要\", \"同事\"]")
    private List<String> tags;

    /**
     * 获取标签列表
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * 设置标签列表
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
