package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.conversation.ImConversationMemberUpdateRequest;
import com.ruoyi.im.service.ImConversationMemberService;
import com.ruoyi.im.vo.conversation.ImConversationMemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 会话成员控制器
 *
 * 用于管理用户与会话的关系，包括会话列表、未读消息数、置顶、免打扰等功能
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/session")
public class ImSessionController {

    @Autowired
    private ImConversationMemberService conversationMemberService;

    /**
     * 获取当前用户会话列表
     * 查询当前用户的所有聊天会话，按最后消息时间倒序排列
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 会话列表，包含单聊和群聊会话
     * @apiNote 每个会话包含最后一条消息、未读消息数等信息
     */
    @GetMapping("/list")
    public Result<List<ImConversationMemberVO>> getList(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImConversationMemberVO> list = conversationMemberService.getConversationMemberList(userId);
        return Result.success(list);
    }

    /**
     * 获取会话成员详情
     * 根据会话ID和用户ID查询会话成员的详细信息
     *
     * @param id 会话ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 会话成员详细信息，包含未读消息数、置顶状态、免打扰状态等
     * @apiNote 会话成员不存在时抛出业务异常
     * @throws BusinessException 当会话ID无效或用户不在会话中时抛出业务异常
     */
    @GetMapping("/{id}")
    public Result<ImConversationMemberVO> getById(@PathVariable Long id,
                                                    @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        ImConversationMemberVO vo = conversationMemberService.getConversationMember(id, userId);
        return Result.success(vo);
    }

    /**
     * 更新会话成员信息
     * 更新会话成员的基本信息，如置顶状态、免打扰状态等
     *
     * @param id 会话ID
     * @param request 会话成员更新请求参数，包含置顶状态、免打扰状态等
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验；仅支持更新置顶状态和免打扰状态
     * @throws BusinessException 当会话不存在或用户不在会话中时抛出业务异常
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                              @Valid @RequestBody ImConversationMemberUpdateRequest request,
                              @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        conversationMemberService.updateConversationMember(id, userId, request);
        return Result.success("更新成功");
    }

    /**
     * 删除会话成员
     * 从当前用户的会话列表中删除指定会话，不会删除实际的聊天记录
     *
     * @param id 会话ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 删除结果
     * @apiNote 删除会话后，该会话将从用户的会话列表中移除；其他成员不受影响
     * @throws BusinessException 当会话不存在或不属于当前用户时抛出业务异常
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        conversationMemberService.deleteConversationMember(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 清空未读消息数
     * 将指定会话的未读消息数清零，标记为已读
     *
     * @param id 会话ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 清空结果
     * @apiNote 清空未读后，会话列表中的未读消息数将显示为0
     * @throws BusinessException 当会话不存在或不属于当前用户时抛出业务异常
     */
    @PutMapping("/{id}/read")
    public Result<Void> clearUnread(@PathVariable Long id,
                                   @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        conversationMemberService.clearUnread(id, userId);
        return Result.success("已清空未读消息");
    }

    /**
     * 置顶/取消置顶会话
     * 将会话设置为置顶状态或取消置顶，置顶的会话将显示在会话列表顶部
     *
     * @param id 会话ID
     * @param pinned 置顶状态，1表示置顶，0表示取消置顶
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 置顶的会话将固定显示在会话列表最上方，不受最后消息时间影响
     * @throws BusinessException 当会话不存在时抛出业务异常
     */
    @PutMapping("/{id}/pin")
    public Result<Void> togglePin(@PathVariable Long id,
                                 @RequestParam Integer pinned,
                                 @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        conversationMemberService.togglePin(id, userId, pinned);
        return Result.success(pinned == 1 ? "已置顶" : "已取消置顶");
    }

    /**
     * 免打扰/取消免打扰会话
     * 将会话设置为免打扰状态或取消免打扰，免打扰的会话不会推送新消息通知
     *
     * @param id 会话ID
     * @param muted 免打扰状态，1表示免打扰，0表示取消免打扰
     * @return 操作结果
     * @apiNote 免打扰状态下，该会话的新消息不会触发系统通知，但仍会在会话列表中显示
     * @throws BusinessException 当会话不存在时抛出业务异常
     */
    @PutMapping("/{id}/mute")
    public Result<Void> toggleMute(@PathVariable Long id,
                                   @RequestParam Integer muted,
                                   @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        conversationMemberService.toggleMute(id, userId, muted);
        return Result.success(muted == 1 ? "已设为免打扰" : "已取消免打扰");
    }
}
