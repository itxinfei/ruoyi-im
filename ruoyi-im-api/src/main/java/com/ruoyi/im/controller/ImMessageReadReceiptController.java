package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessageReadReceipt;
import com.ruoyi.im.service.ImMessageReadReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<List<ImMessageReadReceipt>> list(@RequestParam(required = false) Long messageId,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) Long conversationId) {
        ImMessageReadReceipt query = new ImMessageReadReceipt();
        query.setMessageId(messageId);
        query.setUserId(userId);
        query.setConversationId(conversationId);
        
        List<ImMessageReadReceipt> list = imMessageReadReceiptService.selectImMessageReadReceiptList(query);
        
        return Result.success("查询成功", list);
    }

    /**
     * 查询消息已读回执详细
     */
    @GetMapping("/{id}")
    public Result<ImMessageReadReceipt> getInfo(@PathVariable Long id) {
        ImMessageReadReceipt receipt = imMessageReadReceiptService.selectImMessageReadReceiptById(id);
        
        if (receipt != null) {
            return Result.success("查询成功", receipt);
        } else {
            return Result.error(404, "消息已读回执不存在");
        }
    }

    /**
     * 查询消息的所有已读回执
     */
    @GetMapping("/message/{messageId}")
    public Result<List<ImMessageReadReceipt>> listByMessageId(@PathVariable Long messageId) {
        List<ImMessageReadReceipt> list = imMessageReadReceiptService.selectImMessageReadReceiptByMessageId(messageId);
        
        return Result.success("查询成功", list);
    }

    /**
     * 查询用户的所有已读回执
     */
    @GetMapping("/user/{userId}")
    public Result<List<ImMessageReadReceipt>> listByUserId(@PathVariable Long userId) {
        List<ImMessageReadReceipt> list = imMessageReadReceiptService.selectImMessageReadReceiptByUserId(userId);
        
        return Result.success("查询成功", list);
    }

    /**
     * 查询会话的所有已读回执
     */
    @GetMapping("/conversation/{conversationId}")
    public Result<List<ImMessageReadReceipt>> listByConversationId(@PathVariable Long conversationId) {
        List<ImMessageReadReceipt> list = imMessageReadReceiptService.selectImMessageReadReceiptByConversationId(conversationId);
        
        return Result.success("查询成功", list);
    }

    /**
     * 新增消息已读回执
     */
    @PostMapping
    public Result<ImMessageReadReceipt> add(@RequestBody ImMessageReadReceipt receipt) {
        int rows = imMessageReadReceiptService.insertImMessageReadReceipt(receipt);
        
        if (rows > 0) {
            return Result.success("消息已读回执添加成功", receipt);
        } else {
            return Result.error(500, "消息已读回执添加失败");
        }
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/mark")
    public Result<Void> markAsRead(@RequestBody Map<String, Object> params) {
        Long messageId = Long.valueOf(params.get("messageId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        Long conversationId = Long.valueOf(params.get("conversationId").toString());
        String deviceType = params.get("deviceType") != null ? params.get("deviceType").toString() : "UNKNOWN";
        
        int rows = imMessageReadReceiptService.markMessageAsRead(messageId, userId, conversationId, deviceType);
        
        if (rows > 0) {
            return Result.success(200, "消息已读标记成功", null);
        } else {
            return Result.error(500, "消息已读标记失败");
        }
    }

    /**
     * 批量标记消息已读
     */
    @PostMapping("/batchMark")
    public Result<Integer> batchMarkAsRead(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> messageIds = (List<Long>) params.get("messageIds");
        Long userId = Long.valueOf(params.get("userId").toString());
        Long conversationId = Long.valueOf(params.get("conversationId").toString());
        String deviceType = params.get("deviceType") != null ? params.get("deviceType").toString() : "UNKNOWN";
        
        int rows = imMessageReadReceiptService.batchMarkMessagesAsRead(messageIds, userId, conversationId, deviceType);
        
        if (rows > 0) {
            return Result.success("批量标记消息已读成功", rows);
        } else {
            return Result.error(500, "批量标记消息已读失败");
        }
    }

    /**
     * 修改消息已读回执
     */
    @PutMapping
    public Result<ImMessageReadReceipt> edit(@RequestBody ImMessageReadReceipt receipt) {
        int rows = imMessageReadReceiptService.updateImMessageReadReceipt(receipt);
        
        if (rows > 0) {
            return Result.success("消息已读回执修改成功", receipt);
        } else {
            return Result.error(500, "消息已读回执修改失败");
        }
    }

    /**
     * 删除消息已读回执
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        int rows = imMessageReadReceiptService.deleteImMessageReadReceiptById(id);
        
        if (rows > 0) {
            return Result.success(200, "消息已读回执删除成功", null);
        } else {
            return Result.error(404, "消息已读回执不存在");
        }
    }

    /**
     * 批量删除消息已读回执
     */
    @DeleteMapping
    public Result<Void> remove(@RequestBody Long[] ids) {
        int rows = imMessageReadReceiptService.deleteImMessageReadReceiptByIds(ids);
        
        if (rows > 0) {
            return Result.success(200, "消息已读回执删除成功", null);
        } else {
            return Result.error(500, "消息已读回执删除失败");
        }
    }
}
