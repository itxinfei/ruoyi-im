package com.ruoyi.im.listener;

import com.ruoyi.im.service.ImGroupBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 机器人消息监听器
 *
 * 监听消息发送事件，在群组消息发送成功后触发机器人自动回复
 *
 * @author ruoyi
 */
@Component
public class BotMessageListener {

    private static final Logger log = LoggerFactory.getLogger(BotMessageListener.class);

    private final ImGroupBotService groupBotService;

    public BotMessageListener(ImGroupBotService groupBotService) {
        this.groupBotService = groupBotService;
    }

    /**
     * 处理群组消息事件
     * 在事务提交后异步执行机器人回复逻辑
     *
     * @param event 群组消息事件
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupMessageEvent(GroupMessageEvent event) {
        try {
            log.debug("收到群组消息事件: groupId={}, senderId={}",
                    event.getGroupId(), event.getSenderId());

            // 处理群消息，触发机器人自动回复
            groupBotService.processGroupMessage(
                    event.getGroupId(),
                    event.getSenderId(),
                    event.getMessageContent()
            );
        } catch (Exception e) {
            log.error("处理群组消息事件失败: groupId={}", event.getGroupId(), e);
        }
    }

    /**
     * 群组消息事件
     *
     * @author ruoyi
     */
    public static class GroupMessageEvent {
        private final Long groupId;
        private final Long senderId;
        private final String messageContent;
        private final Long conversationId;

        public GroupMessageEvent(Long conversationId, Long groupId, Long senderId, String messageContent) {
            this.conversationId = conversationId;
            this.groupId = groupId;
            this.senderId = senderId;
            this.messageContent = messageContent;
        }

        public Long getGroupId() {
            return groupId;
        }

        public Long getSenderId() {
            return senderId;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public Long getConversationId() {
            return conversationId;
        }
    }
}
