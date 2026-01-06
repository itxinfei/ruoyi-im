package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.dto.session.ImSessionUpdateRequest;
import com.ruoyi.im.service.ImSessionService;
import com.ruoyi.im.vo.session.ImSessionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 会话控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/im/session")
public class ImSessionController {

    @Autowired
    private ImSessionService imSessionService;

    /**
     * 获取当前用户会话列表
     * 查询当前用户的所有聊天会话，按最后消息时间倒序排列
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 会话列表，包含单聊和群聊会话
     * @apiNote 每个会话包含最后一条消息、未读消息数等信息
     */
    @GetMapping("/list")
    public Result<List<ImSessionVO>> getList(
            @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        List<ImSessionVO> list = imSessionService.getSessionList(userId);
        return Result.success(list);
    }

    /**
     * 获取会话详情
     * 根据会话ID查询会话的详细信息
     *
     * @param id 会话ID
     * @return 会话详细信息，包含会话基本信息、参与用户、最后消息等
     * @apiNote 会话不存在时返回null，调用方需要判空处理
     * @throws BusinessException 当会话ID无效时抛出业务异常
     */
    @GetMapping("/{id}")
    public Result<ImSessionVO> getById(@PathVariable Long id) {
        ImSessionVO vo = imSessionService.getSessionById(id);
        return Result.success(vo);
    }

    /**
     * 更新会话信息
     * 更新会话的基本信息，如会话名称、头像等
     *
     * @param id 会话ID
     * @param request 会话更新请求参数，包含会话名称、头像URL等
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验；仅支持更新单聊会话的名称和头像
     * @throws BusinessException 当会话不存在或参数无效时抛出业务异常
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                              @Valid @RequestBody ImSessionUpdateRequest request) {
        imSessionService.updateSession(id, request);
        return Result.success("更新成功");
    }

    /**
     * 删除会话
     * 从当前用户的会话列表中删除指定会话，不会删除实际的聊天记录
     *
     * @param id 会话ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 删除结果
     * @apiNote 删除会话后，该会话将从用户的会话列表中移除；对方不受影响
     * @throws BusinessException 当会话不存在或不属于当前用户时抛出业务异常
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) {
            userId = 1L;
        }
        imSessionService.deleteSession(id, userId);
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
        imSessionService.clearUnread(id, userId);
        return Result.success("已清空未读消息");
    }

    /**
     * 置顶/取消置顶会话
     * 将会话设置为置顶状态或取消置顶，置顶的会话将显示在会话列表顶部
     *
     * @param id 会话ID
     * @param pinned 置顶状态，1表示置顶，0表示取消置顶
     * @return 操作结果
     * @apiNote 置顶的会话将固定显示在会话列表最上方，不受最后消息时间影响
     * @throws BusinessException 当会话不存在时抛出业务异常
     */
    @PutMapping("/{id}/pin")
    public Result<Void> togglePin(@PathVariable Long id,
                                 @RequestParam Integer pinned) {
        imSessionService.togglePin(id, pinned);
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
                                   @RequestParam Integer muted) {
        imSessionService.toggleMute(id, muted);
        return Result.success(muted == 1 ? "已设为免打扰" : "已取消免打扰");
    }
}
