package com.ruoyi.im.vo.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消息同步响应VO
 *
 * @author ruoyi
 */
@Data
public class SyncMessageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 同步的消息列表
     */
    private List<ImMessageVO> messages;

    /**
     * 是否还有更多消息
     */
    private Boolean hasMore;

    /**
     * 新的同步时间戳（毫秒）
     * 客户端需要保存这个值，下次同步时使用
     */
    private Long newSyncTime;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 同步到的消息总数
     */
    private Integer totalCount;

    /**
     * 离线消息数量（未读）
     */
    private Integer offlineCount;
}
