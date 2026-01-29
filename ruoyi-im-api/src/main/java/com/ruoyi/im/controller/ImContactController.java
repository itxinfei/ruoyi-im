package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 联系人控制器
 * 提供用户搜索、好友申请、好友管理、分组管理等功能
 *
 * @author ruoyi
 */
@Tag(name = "联系人管理", description = "用户搜索、好友申请、好友管理、分组管理等接口")
@RestController
@RequestMapping("/api/im/contact")
public class ImContactController {

    private final ImFriendService imFriendService;

    /**
     * 构造器注入依赖
     *
     * @param imFriendService 好友服务
     */
    public ImContactController(ImFriendService imFriendService) {
        this.imFriendService = imFriendService;
    }

    /**
     * 搜索用户
     * 根据关键词搜索用户，支持按用户名、昵称、手机号搜索
     *
     * @param keyword 搜索关键词
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 用户列表，不包含当前用户和已是好友的用户
     * @apiNote 搜索结果会排除当前用户和已经是好友的用户
     * @throws BusinessException 当搜索参数无效时抛出业务异常
     */
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户，支持用户名、昵称、手机号搜索")
    @GetMapping("/search")
    public Result<List<ImUserVO>> search(@RequestParam String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImUserVO> list = imFriendService.searchUsers(keyword, userId);
        return Result.success(list);
    }

    /**
     * 发送好友申请
     * 向指定用户发送好友申请，对方同意后成为好友
     *
     * @param request 好友申请请求参数，包含目标用户ID、申请理由等
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 申请结果，包含申请记录ID
     * @apiNote 使用 @Valid 注解进行参数校验；对方会收到好友申请通知
     * @throws BusinessException 当用户不存在、已是好友或申请已存在时抛出业务异常
     */
    @Operation(summary = "发送好友申请", description = "向指定用户发送好友申请")
    @PostMapping("/request/send")
    public Result<Long> sendRequest(@Valid @RequestBody ImFriendAddRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        Long requestId = imFriendService.sendFriendRequest(request, userId);
        return Result.success("发送成功", requestId);
    }

    /**
     * 获取收到的好友申请列表
     * 查询当前用户收到的好友申请，按申请时间倒序排列
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 好友申请列表
     * @apiNote 只返回待处理状态的申请，已处理的申请不在此列表中
     */
    @Operation(summary = "获取收到的好友申请", description = "查询当前用户收到的好友申请列表")
    @GetMapping("/request/received")
    public Result<List<ImFriendRequest>> getReceivedRequests() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImFriendRequest> list = imFriendService.getReceivedRequests(userId);
        return Result.success(list);
    }

    /**
     * 获取发送的好友申请列表
     * 查询当前用户发送的好友申请，按申请时间倒序排列
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 好友申请列表
     * @apiNote 包含所有状态的申请（待处理、已同意、已拒绝）
     */
    @Operation(summary = "获取发送的好友申请", description = "查询当前用户发送的好友申请列表")
    @GetMapping("/request/sent")
    public Result<List<ImFriendRequest>> getSentRequests() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImFriendRequest> list = imFriendService.getSentRequests(userId);
        return Result.success(list);
    }

    /**
     * 处理好友申请
     * 同意或拒绝好友申请
     *
     * @param id 好友申请ID
     * @param approved 是否同意，true表示同意，false表示拒绝
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 处理结果
     * @apiNote 同意后会建立好友关系，并创建私聊会话；拒绝后申请状态更新为已拒绝
     * @throws BusinessException 当申请不存在或无权限处理时抛出业务异常
     */
    @Operation(summary = "处理好友申请", description = "同意或拒绝好友申请")
    @PostMapping("/request/{id}/handle")
    public Result<Void> handleRequest(@PathVariable Long id,
                                      @RequestParam Boolean approved) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.handleFriendRequest(id, approved, userId);
        return Result.success(approved ? "已同意" : "已拒绝");
    }

    /**
     * 获取好友列表
     * 查询当前用户的所有好友，按昵称排序
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 好友列表
     * @apiNote 返回的好友信息包含在线状态、最后活跃时间等
     */
    @Operation(summary = "获取好友列表", description = "查询当前用户的所有好友")
    @GetMapping("/list")
    public Result<List<ImFriendVO>> getFriendList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImFriendVO> list = imFriendService.getFriendList(userId);
        return Result.success(list);
    }

    /**
     * 获取分组好友列表
     * 查询当前用户的好友，按分组进行组织
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 分组好友列表，每个分组包含该分组下的好友
     * @apiNote 未分组的好友会放在"默认分组"中
     */
    @Operation(summary = "获取分组好友列表", description = "查询当前用户的好友，按分组进行组织")
    @GetMapping("/grouped")
    public Result<List<ImContactGroupVO>> getGroupedFriendList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImContactGroupVO> list = imFriendService.getGroupedFriendList(userId);
        return Result.success(list);
    }

    /**
     * 获取好友详情
     * 查询指定好友的详细信息
     *
     * @param id 好友关系ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 好友详细信息
     * @apiNote 只能查询自己的好友信息
     * @throws BusinessException 当好友关系不存在时抛出业务异常
     */
    @Operation(summary = "获取好友详情", description = "查询指定好友的详细信息")
    @GetMapping("/{id}")
    public Result<ImFriendVO> getFriendById(@PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        ImFriendVO vo = imFriendService.getFriendById(id, userId);
        return Result.success(vo);
    }

    /**
     * 更新好友信息
     * 更新好友的备注名、分组等信息
     *
     * @param id 好友关系ID
     * @param request 更新请求参数，包含备注名、分组ID等
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验；只能更新自己的好友信息
     * @throws BusinessException 当好友关系不存在时抛出业务异常
     */
    @Operation(summary = "更新好友信息", description = "更新好友的备注名、分组等信息")
    @PutMapping("/{id}")
    public Result<Void> updateFriend(@PathVariable Long id,
                                     @Valid @RequestBody ImFriendUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.updateFriend(id, request, userId);
        return Result.success("更新成功");
    }

    /**
     * 删除好友
     * 删除指定好友关系，删除后双方不再是好友
     *
     * @param id 好友关系ID
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 删除结果
     * @apiNote 删除好友后，对应的私聊会话也会被删除
     * @throws BusinessException 当好友关系不存在时抛出业务异常
     */
    @Operation(summary = "删除好友", description = "删除指定好友关系")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFriend(@PathVariable Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.deleteFriend(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 拉黑/解除拉黑好友
     * 拉黑好友后，将无法接收该好友的消息
     *
     * @param id 好友关系ID
     * @param blocked 是否拉黑，true表示拉黑，false表示解除拉黑
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 拉黑是单向的，拉黑好友后，对方仍可以发送消息，但自己无法接收
     * @throws BusinessException 当好友关系不存在时抛出业务异常
     */
    @Operation(summary = "拉黑/解除拉黑好友", description = "拉黑好友后无法接收其消息")
    @PutMapping("/{id}/block")
    public Result<Void> blockFriend(@PathVariable Long id,
                                    @RequestParam Boolean blocked) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.blockFriend(id, blocked, userId);
        return Result.success(blocked ? "已拉黑" : "已解除拉黑");
    }

    /**
     * 获取所有好友分组名称列表
     * 查询当前用户使用的所有好友分组
     *
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 分组名称列表
     * @apiNote 分组是从好友关系中动态提取的，返回所有使用过的分组名称
     */
    @Operation(summary = "获取好友分组列表", description = "查询当前用户使用的所有好友分组")
    @GetMapping("/group/list")
    public Result<java.util.List<String>> getGroupList() {
        Long userId = SecurityUtils.getLoginUserId();
        java.util.List<String> list = imFriendService.getGroupNames(userId);
        return Result.success(list);
    }

    /**
     * 重命名好友分组
     * 将指定的分组名称重命名为新名称
     *
     * @param oldName 旧分组名称（URL编码）
     * @param request 重命名请求，包含新分组名称
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 此操作会更新所有使用该分组的好友关系
     * @throws BusinessException 当分组不存在时抛出业务异常
     */
    @Operation(summary = "重命名好友分组", description = "将指定的分组名称重命名为新名称")
    @PutMapping("/group/{oldName}")
    public Result<Void> renameGroup(@PathVariable String oldName,
                                     @RequestBody GroupRenameRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            String decodedName = java.net.URLDecoder.decode(oldName, "UTF-8");
            imFriendService.renameGroup(userId, decodedName, request.getNewName());
        } catch (java.io.UnsupportedEncodingException e) {
            // UTF-8 is always supported, this should never happen
            imFriendService.renameGroup(userId, oldName, request.getNewName());
        }
        return Result.success("重命名成功");
    }

    /**
     * 删除好友分组
     * 删除指定分组，将该分组下的所有好友移至"默认分组"（清空分组名）
     *
     * @param groupName 分组名称（URL编码）
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 删除分组不会删除好友，只是清空好友的分组信息
     * @throws BusinessException 当分组不存在时抛出业务异常
     */
    @Operation(summary = "删除好友分组", description = "删除指定分组，好友移至默认分组")
    @DeleteMapping("/group/{groupName}")
    public Result<Void> deleteGroup(@PathVariable String groupName) {
        Long userId = SecurityUtils.getLoginUserId();
        try {
            String decodedName = java.net.URLDecoder.decode(groupName, "UTF-8");
            imFriendService.deleteGroup(userId, decodedName);
        } catch (java.io.UnsupportedEncodingException e) {
            // UTF-8 is always supported, this should never happen
            imFriendService.deleteGroup(userId, groupName);
        }
        return Result.success("删除成功");
    }

    /**
     * 移动好友到分组
     * 批量移动好友到指定分组
     *
     * @param request 移动请求，包含好友ID列表和目标分组ID（分组名称）
     * @param userId 当前登录用户ID，从请求头中获取
     * @return 操作结果
     * @apiNote 分组ID实际是分组名称；如果分组名称为空，则移至默认分组
     * @throws BusinessException 当好友不存在时抛出业务异常
     */
    @Operation(summary = "移动好友到分组", description = "批量移动好友到指定分组")
    @PutMapping("/group/move")
    public Result<Void> moveFriendToGroup(@RequestBody MoveToGroupRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.moveFriendsToGroup(userId, request.getFriendIds(), request.getGroupName());
        return Result.success("移动成功");
    }

    /**
     * 获取用户的所有标签
     * 获取当前用户使用过的所有好友标签
     *
     * @return 标签列表
     * @apiNote 标签是从好友关系中动态提取的
     */
    @Operation(summary = "获取用户标签", description = "获取当前用户的所有好友标签")
    @GetMapping("/tags")
    public Result<List<String>> getUserTags() {
        Long userId = SecurityUtils.getLoginUserId();
        List<String> tags = imFriendService.getUserTags(userId);
        return Result.success(tags);
    }

    /**
     * 更新好友标签
     * 为指定好友设置标签
     *
     * @param friendId 好友用户ID
     * @param tags 标签列表
     * @return 更新结果
     * @apiNote 如果标签不存在，会自动创建
     */
    @Operation(summary = "更新好友标签", description = "为指定好友设置标签")
    @PutMapping("/{friendId}/tags")
    public Result<Void> updateFriendTags(@PathVariable Long friendId,
                                           @RequestBody java.util.Map<String, Object> request) {
        Long userId = SecurityUtils.getLoginUserId();
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.get("tags");
        imFriendService.updateFriendTags(friendId, userId, tags);
        return Result.success("标签更新成功");
    }

    /**
     * 按标签获取好友
     * 获取具有指定标签的所有好友
     *
     * @param tag 标签名称
     * @return 好友列表
     * @apiNote 用于标签筛选功能
     */
    @Operation(summary = "按标签获取好友", description = "获取具有指定标签的所有好友")
    @GetMapping("/tag/{tag}")
    public Result<List<ImFriendVO>> getFriendsByTag(@PathVariable String tag) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImFriendVO> list = imFriendService.getFriendsByTag(userId, tag);
        return Result.success(list);
    }

    /**
     * 重命名分组请求体
     */
    public static class GroupRenameRequest {
        private String newName;

        public String getNewName() {
            return newName;
        }

        public void setNewName(String newName) {
            this.newName = newName;
        }
    }

    /**
     * 移动到分组请求体
     */
    public static class MoveToGroupRequest {
        private java.util.List<Long> friendIds;
        private String groupName;

        public java.util.List<Long> getFriendIds() {
            return friendIds;
        }

        public void setFriendIds(java.util.List<Long> friendIds) {
            this.friendIds = friendIds;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }
    }
}
