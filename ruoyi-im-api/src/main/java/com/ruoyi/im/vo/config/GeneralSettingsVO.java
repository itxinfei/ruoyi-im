package com.ruoyi.im.vo.config;

import lombok.Data;

import java.util.Map;

/**
 * 通用设置VO
 */
@Data
public class GeneralSettingsVO {
    /** 语言 */
    private String language;
    /** 主题 */
    private String theme;
    /** 字体大小 */
    private Integer fontSize;
    /** 消息预览 */
    private Boolean messagePreview;
    /** 发送方式(Enter/Ctrl+Enter) */
    private String sendMethod;
    /** 扩展字段 */
    private Map<String, Object> extra;

    public static GeneralSettingsVO fromMap(Map<String, Object> map) {
        GeneralSettingsVO vo = new GeneralSettingsVO();
        if (map == null) return vo;
        vo.language = (String) map.get("language");
        vo.theme = (String) map.get("theme");
        vo.fontSize = (Integer) map.get("fontSize");
        vo.messagePreview = (Boolean) map.get("messagePreview");
        vo.sendMethod = (String) map.get("sendMethod");
        vo.extra = map;
        return vo;
    }
}
