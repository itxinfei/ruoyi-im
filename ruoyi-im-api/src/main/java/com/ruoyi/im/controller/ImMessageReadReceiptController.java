package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImMessageReadReceipt;
import com.ruoyi.im.service.ImMessageReadReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息已读回执控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/readReceipt")
public class ImMessageReadReceiptController {

    @Autowired
    private ImMessageReadReceiptService imMessageReadReceiptService;

    /**
     * 查询消息已读回执列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) Long messageId,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) Long conversationId) {
        Map<String, Object> result = new HashMap<>();
        
        ImMessageReadReceipt query = new ImMessageReadReceipt();
        query.setMessageId(messageId);
        query.setUserId(userId);
        query.setConversationId(conversationId);
        
        List<ImMessageReadReceipt> list = imMessageReadReceiptService.selectImMessageReadReceiptList(query);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 查询消息已读回执详细
     */
    @GetMapping("/{id}")
    public Map<String, Object> getInfo(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        ImMessageReadReceipt receipt = imMessageReadReceiptService.selectImMessageReadReceiptById(id);
        
        if (receipt != null) {
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", receipt);
        } else {
            result.put("code", 404);
            result.put("msg", "消息已读回执不存在");
        }
        
        return result;
    }

    /**
     * 查询消息的所有已读回执
     */
    @GetMapping("/message/{messageId}")
    public Map<String, Object> listByMessageId(@PathVariable Long messageId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImMessageReadReceipt> list = imMessageReadReceiptService.selectImMessageReadReceiptByMessageId(messageId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 查询用户的所有已读回执
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> listByUserId(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImMessageReadReceipt> list = imMessageReadReceiptService.selectImMessageReadReceiptByUserId(userId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 查询会话的所有已读回执
     */
    @GetMapping("/conversation/{conversationId}")
    public Map<String, Object> listByConversationId(@PathVariable Long conversationId) {
        Map<String, Object> result = new HashMap<>();
        
        List<ImMessageReadReceipt> list = imMessageReadReceiptService.selectImMessageReadReceiptByConversationId(conversationId);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);
        
        return result;
    }

    /**
     * 新增消息已读回执
     */
    @PostMapping
    public Map<String, Object> add(@RequestBody ImMessageReadReceipt receipt) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imMessageReadReceiptService.insertImMessageReadReceipt(receipt);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息已读回执添加成功");
            result.put("data", receipt);
        } else {
            result.put("code", 500);
            result.put("msg", "消息已读回执添加失败");
        }
        
        return result;
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/mark")
    public Map<String, Object> markAsRead(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        Long messageId = Long.valueOf(params.get("messageId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        Long conversationId = Long.valueOf(params.get("conversationId").toString());
        String deviceType = params.get("deviceType") != null ? params.get("deviceType").toString() : "UNKNOWN";
        
        int rows = imMessageReadReceiptService.markMessageAsRead(messageId, userId, conversationId, deviceType);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息已读标记成功");
        } else {
            result.put("code", 500);
            result.put("msg", "消息已读标记失败");
        }
        
        return result;
    }

    /**
     * 批量标记消息已读
     */
    @PostMapping("/batchMark")
    public Map<String, Object> batchMarkAsRead(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<Long> messageIds = (List<Long>) params.get("messageIds");
        Long userId = Long.valueOf(params.get("userId").toString());
        Long conversationId = Long.valueOf(params.get("conversationId").toString());
        String deviceType = params.get("deviceType") != null ? params.get("deviceType").toString() : "UNKNOWN";
        
        int rows = imMessageReadReceiptService.batchMarkMessagesAsRead(messageIds, userId, conversationId, deviceType);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "批量标记消息已读成功");
            result.put("data", rows);
        } else {
            result.put("code", 500);
            result.put("msg", "批量标记消息已读失败");
        }
        
        return result;
    }

    /**
     * 修改消息已读回执
     */
    @PutMapping
    public Map<String, Object> edit(@RequestBody ImMessageReadReceipt receipt) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imMessageReadReceiptService.updateImMessageReadReceipt(receipt);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息已读回执修改成功");
            result.put("data", receipt);
        } else {
            result.put("code", 500);
            result.put("msg", "消息已读回执修改失败");
        }
        
        return result;
    }

    /**
     * 删除消息已读回执
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> remove(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imMessageReadReceiptService.deleteImMessageReadReceiptById(id);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息已读回执删除成功");
        } else {
            result.put("code", 404);
            result.put("msg", "消息已读回执不存在");
        }
        
        return result;
    }

    /**
     * 批量删除消息已读回执
     */
    @DeleteMapping
    public Map<String, Object> remove(@RequestBody Long[] ids) {
        Map<String, Object> result = new HashMap<>();
        
        int rows = imMessageReadReceiptService.deleteImMessageReadReceiptByIds(ids);
        
        if (rows > 0) {
            result.put("code", 200);
            result.put("msg", "消息已读回执删除成功");
        } else {
            result.put("code", 500);
            result.put("msg", "消息已读回执删除失败");
        }
        
        return result;
    }
}
