package com.ruoyi.im.dto.session;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 会话更新请求
 *
 * @author ruoyi
 */
public class ImSessionUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话名称 */
    @Size(max = 100, message = "会话名称长度不能超过100")
    private String name;

    /** 是否置顶: 0=否, 1=是 */
    private Integer isPinned;

    /** 是否免打扰: 0=否, 1=是 */
    private Integer isMuted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    public Integer getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Integer isMuted) {
        this.isMuted = isMuted;
    }
}
