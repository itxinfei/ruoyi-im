package com.ruoyi.im.vo.config;

import lombok.Data;

import java.util.Map;

/**
 * 快捷键设置VO
 */
@Data
public class ShortcutSettingsVO {
    /** 发送消息快捷键 */
    private String sendMessage;
    /** 全局搜索快捷键 */
    private String globalSearch;
    /** 新建会话快捷键 */
    private String newConversation;
    /** 切换会话快捷键 */
    private String switchConversation;
    /** 扩展字段 */
    private Map<String, Object> extra;

    public static ShortcutSettingsVO fromMap(Map<String, Object> map) {
        ShortcutSettingsVO vo = new ShortcutSettingsVO();
        if (map == null) {
            return vo;
        }
        vo.sendMessage = (String) map.get("sendMessage");
        vo.globalSearch = (String) map.get("globalSearch");
        vo.newConversation = (String) map.get("newConversation");
        vo.switchConversation = (String) map.get("switchConversation");
        vo.extra = map;
        return vo;
    }
}
