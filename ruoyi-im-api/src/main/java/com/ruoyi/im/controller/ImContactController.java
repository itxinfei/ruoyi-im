package com.ruoyi.im.controller;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImFriendRequest;
import com.ruoyi.im.dto.contact.*;
import com.ruoyi.im.service.ImBatchOperationService;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.ImRedisUtil;
import com.ruoyi.im.util.SecurityUtils;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import com.ruoyi.im.vo.user.ImUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;

/**
 * 联系人控制器
 * 提供用户搜索、好友申请、好友管理、分组管理等功能
 *
 * @author ruoyi
 */
@Tag(name = "联系人管理", description = "用户搜索、好友申请、好友管理、分组管理等接口")
@RestController
@RequestMapping("/api/im/contacts")
@Validated
public class ImContactController {

    private final ImFriendService imFriendService;
    private final ImRedisUtil imRedisUtil;
    private final ImBatchOperationService imBatchOperationService;
    private final ImUserService imUserService;

    /**
     * 构造器注入依赖
     *
     * @param imFriendService 好友服务
     */
    public ImContactController(ImFriendService imFriendService,
            ImRedisUtil imRedisUtil,
            ImBatchOperationService imBatchOperationService,
            ImUserService imUserService) {
        this.imFriendService = imFriendService;
        this.imRedisUtil = imRedisUtil;
        this.imBatchOperationService = imBatchOperationService;
        this.imUserService = imUserService;
    }

    /**
     * 搜索用户
     * 根据关键词搜索用户，支持按用户名、昵称、手机号搜索
     *
     * @param keyword 搜索关键词
     * @return 用户列表，不包含当前用户和已是好友的用户
     * @apiNote 搜索结果会排除当前用户和已经是好友的用户
     */
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户，支持用户名、昵称、手机号搜索")
    @GetMapping("/search")
    public Result<List<ImUserVO>> search(@RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImUserVO> list = imFriendService.searchUsers(keyword, userId);
        return Result.success(list);
    }

    /**
     * 发送好友申请
     * 向指定用户发送好友申请，对方同意后成为好友
     *
     * @param request 好友申请请求参数，包含目标用户ID、申请理由等
     * @return 申请结果，包含申请记录ID
     * @apiNote 使用 @Valid 注解进行参数校验；对方会收到好友申请通知
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
     * @param id       好友申请ID
     * @param approved 是否同意，true表示同意，false表示拒绝
     * @return 处理结果
     * @apiNote 同意后会建立好友关系，并创建私聊会话；拒绝后申请状态更新为已拒绝
     */
    @Operation(summary = "处理好友申请", description = "同意或拒绝好友申请")
    @PostMapping("/request/{id}/handle")
    public Result<Void> handleRequest(@PathVariable @Positive(message = "申请ID必须为正数") Long id,
            @RequestParam @NotNull(message = "处理结果不能为空") Boolean approved) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.processFriendRequest(id, approved, userId);
        return Result.success(approved ? "已同意" : "已拒绝");
    }

    /**
     * 获取好友列表
     * 查询当前用户的所有好友，按昵称排序
     *
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
     * @return 好友详细信息
     * @apiNote 只能查询自己的好友信息
     */
    @Operation(summary = "获取好友详情", description = "查询指定好友的详细信息")
    @GetMapping("/{id}")
    public Result<ImFriendVO> getFriendById(@PathVariable @Positive(message = "好友ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        ImFriendVO vo = imFriendService.getFriendById(id, userId);
        return Result.success(vo);
    }

    /**
     * 更新好友信息
     * 更新好友的备注名、分组等信息
     *
     * @param id      好友关系ID
     * @param request 更新请求参数，包含备注名、分组ID等
     * @return 更新结果
     * @apiNote 使用 @Valid 注解进行参数校验；只能更新自己的好友信息
     */
    @Operation(summary = "更新好友信息", description = "更新好友的备注名、分组等信息")
    @PutMapping("/{id}")
    public Result<Void> updateFriend(@PathVariable @Positive(message = "好友ID必须为正数") Long id,
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
     * @return 删除结果
     * @apiNote 删除好友后，对应的私聊会话也会被删除
     */
    @Operation(summary = "删除好友", description = "删除指定好友关系")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFriend(@PathVariable @Positive(message = "好友ID必须为正数") Long id) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.deleteFriend(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 拉黑/解除拉黑好友
     * 拉黑好友后，将无法接收该好友的消息
     *
     * @param id      好友关系ID
     * @param blocked 是否拉黑，true表示拉黑，false表示解除拉黑
     * @return 操作结果
     * @apiNote 拉黑是单向的，拉黑好友后，对方仍可以发送消息，但自己无法接收
     */
    @Operation(summary = "拉黑/解除拉黑好友", description = "拉黑好友后无法接收其消息")
    @PutMapping("/{id}/block")
    public Result<Void> blockFriend(@PathVariable @Positive(message = "好友ID必须为正数") Long id,
            @RequestParam @NotNull(message = "拉黑状态不能为空") Boolean blocked) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.blockFriend(id, blocked, userId);
        return Result.success(blocked ? "已拉黑" : "已解除拉黑");
    }

    /**
     * 获取所有好友分组名称列表
     * 查询当前用户使用的所有好友分组
     *
     * @return 分组名称列表
     * @apiNote 分组是从好友关系中动态提取的，返回所有使用过的分组名称
     */
    @Operation(summary = "获取好友分组列表", description = "查询当前用户使用的所有好友分组")
    @GetMapping("/group/list")
    public Result<List<String>> getGroupList() {
        Long userId = SecurityUtils.getLoginUserId();
        List<String> list = imFriendService.getGroupNames(userId);
        return Result.success(list);
    }

    /**
     * 重命名好友分组
     * 将指定的分组名称重命名为新名称
     *
     * @param oldName  旧分组名称（URL编码）
     * @param request 重命名请求，包含新分组名称
     * @return 操作结果
     * @apiNote 此操作会更新所有使用该分组的好友关系
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
     * @return 操作结果
     * @apiNote 删除分组不会删除好友，只是清空好友的分组信息
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
     * @param request 移动请求，包含好友ID列表和目标分组名称
     * @return 操作结果
     * @apiNote 如果分组名称为空，则移至默认分组
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
     * @param request  标签更新请求
     * @return 更新结果
     * @apiNote 如果标签不存在，会自动创建
     */
    @Operation(summary = "更新好友标签", description = "为指定好友设置标签")
    @PutMapping("/{friendId}/tags")
    public Result<Void> updateFriendTags(@PathVariable Long friendId,
            @Valid @RequestBody FriendTagsUpdateRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.updateFriendTags(friendId, userId, request.getTags());
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
     * 批量添加好友
     * 向多个用户发送好友申请
     *
     * @param request 批量添加请求，包含用户ID列表和验证消息
     * @return 操作结果
     * @apiNote 批量发送好友申请，每个用户都会收到相同的好友申请
     */
    @Operation(summary = "批量添加好友", description = "向多个用户批量发送好友申请")
    @PostMapping("/batch-add")
    public Result<HashMap<String, Object>> batchAddFriends(@RequestBody BatchAddRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        java.util.Map<Long, String> results = imFriendService.batchSendFriendRequest(
                request.getUserIds(), request.getRemark(), userId);
        // 转换为Map<String, Object>
        HashMap<String, Object> resultData = new HashMap<>();
        for (java.util.Map.Entry<Long, String> entry : results.entrySet()) {
            resultData.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return Result.success("批量发送完成", resultData);
    }

    /**
     * 批量删除好友
     * 删除多个好友关系
     *
     * @param request 批量删除请求，包含好友关系ID列表
     * @return 操作结果
     * @apiNote 批量删除好友，对应的私聊会话也会被删除
     */
    @Operation(summary = "批量删除好友", description = "批量删除多个好友关系")
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDeleteFriends(@RequestBody BatchDeleteRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.batchDeleteFriends(request.getContactIds(), userId);
        return Result.success("批量删除成功");
    }

    /**
     * 批量移动到分组
     * 将多个好友移动到指定分组
     *
     * @param request 批量移动请求，包含联系人ID列表和目标分组名称
     * @return 操作结果
     * @apiNote 批量移动好友到指定分组
     */
    @Operation(summary = "批量移动到分组", description = "将多个好友移动到指定分组")
    @PutMapping("/batch-move")
    public Result<Void> batchMoveToGroup(@RequestBody BatchMoveRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.moveFriendsToGroup(userId, request.getContactIds(), request.getGroupName());
        return Result.success("批量移动成功");
    }

    /**
     * 获取推荐好友
     * 基于部门、手机号等推荐可能认识的人
     *
     * @param type  推荐类型：department(同事)、phone(手机号)、all(全部)
     * @param limit 返回数量限制
     * @return 推荐好友列表
     * @apiNote 推荐算法基于共同部门、通讯录匹配等
     */
    @Operation(summary = "获取推荐好友", description = "基于部门、手机号等推荐可能认识的人")
    @GetMapping("/recommendations")
    public Result<List<ImUserVO>> getRecommendedContacts(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImUserVO> list = imFriendService.getRecommendedContacts(userId, type, limit);
        return Result.success(list);
    }

    /**
     * 上传通讯录用于好友匹配
     * 上传用户通讯录，匹配已注册的用户
     *
     * @param request 通讯录数据
     * @return 匹配结果
     * @apiNote 通讯录数据加密存储，仅用于好友匹配
     */
    @Operation(summary = "上传通讯录", description = "上传通讯录用于好友匹配")
    @PostMapping("/address-book/upload")
    public Result<List<ImUserVO>> uploadAddressBook(@RequestBody AddressBookUploadRequest request) {
        Long userId = SecurityUtils.getLoginUserId();
        // 转换ContactItem为Map<String, String>
        java.util.List<java.util.Map<String, String>> contactsMap = new java.util.ArrayList<>();
        if (request.getContacts() != null) {
            for (AddressBookUploadRequest.ContactItem item : request.getContacts()) {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("name", item.getName());
                map.put("phone", item.getPhone());
                contactsMap.add(map);
            }
        }
        List<ImUserVO> matchedUsers = imFriendService.matchAddressBookContacts(userId, contactsMap);
        return Result.success("上传成功", matchedUsers);
    }

    /**
     * 获取通讯录匹配的好友
     * 获取之前上传通讯录匹配到的用户
     *
     * @return 匹配的用户列表
     * @apiNote 返回已通过通讯录匹配到的用户
     */
    @Operation(summary = "获取通讯录匹配结果", description = "获取之前上传通讯录匹配到的用户")
    @GetMapping("/address-book/matches")
    public Result<List<ImUserVO>> getAddressBookMatches() {
        Long userId = SecurityUtils.getLoginUserId();
        List<ImUserVO> list = imFriendService.getAddressBookMatches(userId);
        return Result.success(list);
    }

    /**
     * 清除好友列表缓存
     * 清除当前用户的好友列表Redis缓存，用于刷新数据
     *
     * @return 操作结果
     * @apiNote 当数据更新后好友列表不更新时，可以调用此接口清除缓存
     */
    @Operation(summary = "清除好友列表缓存", description = "清除当前用户的好友列表缓存")
    @PostMapping("/cache/clear")
    public Result<Void> clearFriendListCache() {
        Long userId = SecurityUtils.getLoginUserId();
        imFriendService.clearFriendListCache(userId);
        return Result.success("缓存已清除");
    }

    /**
     * 批量清除多个用户的好友列表缓存
     * 管理员接口，用于清除指定用户的缓存
     *
     * @param request 批量清除请求，包含用户ID列表
     * @return 操作结果
     * @apiNote 需要管理员权限
     */
    @Operation(summary = "批量清除好友列表缓存", description = "批量清除多个用户的好友列表缓存")
    @PostMapping("/cache/clear-batch")
    public Result<HashMap<String, Object>> batchClearFriendListCache(
            @RequestBody BatchClearCacheRequest request) {
        imFriendService.batchClearFriendListCache(request.getUserIds());
        HashMap<String, Object> result = new HashMap<>();
        result.put("cleared", request.getUserIds().size());
        result.put("message", "已清除 " + request.getUserIds().size() + " 个用户的缓存");
        return Result.success(result);
    }
}
