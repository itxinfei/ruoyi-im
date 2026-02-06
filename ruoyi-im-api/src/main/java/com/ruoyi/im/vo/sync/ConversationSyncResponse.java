package com.ruoyi.im.vo.sync;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 会话同步响应
 *
 * @author ruoyi
 */
@Data
public class ConversationSyncResponse {

    /**
     * 会话事件列表
     */
    private List<ConversationSyncEvent> events;

    /**
     * 是否还有更多事件
     */
    private Boolean hasMore;

    /**
     * 新的同步时间戳
     */
    private Long newSyncTime;

    /**
     * 事件总数
     */
    private Integer totalCount;

    /**
     * 创建空响应
     */
    public static ConversationSyncResponse empty() {
        ConversationSyncResponse response = new ConversationSyncResponse();
        response.setEvents(Collections.emptyList());
        response.setHasMore(false);
        response.setNewSyncTime(System.currentTimeMillis());
        response.setTotalCount(0);
        return response;
    }

    /**
     * 创建响应
     */
    public static ConversationSyncResponse create(List<ConversationSyncEvent> events, Long newSyncTime) {
        ConversationSyncResponse response = new ConversationSyncResponse();
        response.setEvents(events);
        response.setHasMore(false);
        response.setNewSyncTime(newSyncTime);
        response.setTotalCount(events.size());
        return response;
    }
}
