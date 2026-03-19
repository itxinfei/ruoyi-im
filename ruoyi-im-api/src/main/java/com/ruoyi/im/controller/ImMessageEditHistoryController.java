package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.service.ImMessageEditHistoryService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.message.MessageEditHistoryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息编辑历史控制器
 *
 * @author ruoyi
 */

@RestController
@RequestMapping("/api/im/message/edit-history")
public class ImMessageEditHistoryController {

    private static final Logger log = LoggerFactory.getLogger(ImMessageEditHistoryController.class);

    private final ImMessageEditHistoryService editHistoryService;

    public ImMessageEditHistoryController(ImMessageEditHistoryService editHistoryService) {
        this.editHistoryService = editHistoryService;
    }

    /**
     * 获取消息编辑历史列表
     *
     * @param messageId 消息 ID
     * @return 编辑历史列表
     */
    
    @GetMapping("/{messageId}")
    public Result<List<MessageEditHistoryVO>> getEditHistory(@PathVariable Long messageId) {
        try {
            Long userId = SecurityUtils.getLoginUserId();
            log.info("查询消息编辑历史：messageId={}, userId={}", messageId, userId);

            List<MessageEditHistoryVO> historyList = editHistoryService.getEditHistory(messageId);
            if (historyList == null) {
                return Result.error("查询编辑历史失败");
            }
            return Result.success(historyList);
        } catch (Exception e) {
            log.error("获取消息编辑历史失败：messageId={}", messageId, e);
            return Result.error("查询编辑历史失败：" + e.getMessage());
        }
    }

    /**
     * 获取消息编辑统计
     *
     * @param messageId 消息 ID
     * @return 编辑统计信息
     */
    
    @GetMapping("/stats/{messageId}")
    public Result<Map<String, Object>> getEditStats(@PathVariable Long messageId) {
        try {
            Long userId = SecurityUtils.getLoginUserId();
            Integer editTimes = editHistoryService.countEditTimes(messageId);

            Map<String, Object> result = new HashMap<>();
            result.put("messageId", messageId);
            result.put("editTimes", editTimes != null ? editTimes : 0);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取消息编辑统计失败：messageId={}", messageId, e);
            return Result.error("查询编辑统计失败：" + e.getMessage());
        }
    }
}

