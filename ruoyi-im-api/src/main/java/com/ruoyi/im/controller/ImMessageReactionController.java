package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImMessageReaction;
import com.ruoyi.im.service.ImMessageReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息互动控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/reaction")
public class ImMessageReactionController {

    @Autowired
    private ImMessageReactionService imMessageReactionService;

    /**
     * 查询消息互动列表
     */
    @GetMapping("/list")
    public Result<List<ImMessageReaction>> list(@RequestParam(required = false) Long messageId,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String reactionType) {
        ImMessageReaction query = new ImMessageReaction();
        query.setMessageId(messageId);
        query.setUserId(userId);
        query.setReactionType(reactionType);
        
        List<ImMessageReaction> list = imMessageReactionService.selectImMessageReactionList(query);
        
        return Result.success("查询成功", list);
    }

    /**
     * 查询消息互动详细
     */
    @GetMapping("/{id}")
    public Result<ImMessageReaction> getInfo(@PathVariable Long id) {
        ImMessageReaction reaction = imMessageReactionService.selectImMessageReactionById(id);
        
        if (reaction != null) {
            return Result.success("查询成功", reaction);
        } else {
            return Result.error(404, "消息互动不存在");
        }
    }

    /**
     * 查询消息的所有互动
     */
    @GetMapping("/message/{messageId}")
    public Result<List<ImMessageReaction>> listByMessageId(@PathVariable Long messageId) {
        List<ImMessageReaction> list = imMessageReactionService.selectImMessageReactionByMessageId(messageId);
        
        return Result.success("查询成功", list);
    }

    /**
     * 查询用户的所有互动
     */
    @GetMapping("/user/{userId}")
    public Result<List<ImMessageReaction>> listByUserId(@PathVariable Long userId) {
        List<ImMessageReaction> list = imMessageReactionService.selectImMessageReactionByUserId(userId);
        
        return Result.success("查询成功", list);
    }

    /**
     * 新增消息互动
     */
    @PostMapping
    public Result<ImMessageReaction> add(@RequestBody ImMessageReaction reaction) {
        int rows = imMessageReactionService.insertImMessageReaction(reaction);
        
        if (rows > 0) {
            return Result.success("消息互动添加成功", reaction);
        } else {
            return Result.error(500, "消息互动添加失败");
        }
    }

    /**
     * 添加消息互动（简化接口）
     */
    @PostMapping("/add")
    public Result<Void> addReaction(@RequestBody Map<String, Object> params) {
        Long messageId = Long.valueOf(params.get("messageId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String reactionType = params.get("reactionType").toString();
        String emoji = params.get("emoji") != null ? params.get("emoji").toString() : null;
        
        int rows = imMessageReactionService.addReaction(messageId, userId, reactionType, emoji);
        
        if (rows > 0) {
            return Result.success(200, "消息互动添加成功", null);
        } else {
            return Result.error(500, "消息互动添加失败");
        }
    }

    /**
     * 取消消息互动
     */
    @PostMapping("/remove")
    public Result<Void> removeReaction(@RequestBody Map<String, Object> params) {
        Long messageId = Long.valueOf(params.get("messageId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String reactionType = params.get("reactionType").toString();
        
        int rows = imMessageReactionService.removeReaction(messageId, userId, reactionType);
        
        if (rows > 0) {
            return Result.success(200, "消息互动取消成功", null);
        } else {
            return Result.error(500, "消息互动取消失败");
        }
    }

    /**
     * 修改消息互动
     */
    @PutMapping
    public Result<ImMessageReaction> edit(@RequestBody ImMessageReaction reaction) {
        int rows = imMessageReactionService.updateImMessageReaction(reaction);
        
        if (rows > 0) {
            return Result.success("消息互动修改成功", reaction);
        } else {
            return Result.error(500, "消息互动修改失败");
        }
    }

    /**
     * 删除消息互动
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        int rows = imMessageReactionService.deleteImMessageReactionById(id);
        
        if (rows > 0) {
            return Result.success(200, "消息互动删除成功", null);
        } else {
            return Result.error(404, "消息互动不存在");
        }
    }

    /**
     * 批量删除消息互动
     */
    @DeleteMapping
    public Result<Void> remove(@RequestBody Long[] ids) {
        int rows = imMessageReactionService.deleteImMessageReactionByIds(ids);
        
        if (rows > 0) {
            return Result.success(200, "消息互动删除成功", null);
        } else {
            return Result.error(500, "消息互动删除失败");
        }
    }
}
