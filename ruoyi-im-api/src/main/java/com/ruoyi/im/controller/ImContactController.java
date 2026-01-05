package com.ruoyi.im.controller;

import com.github.pagehelper.PageInfo;
import com.ruoyi.im.annotation.ImApiVersion;
import com.ruoyi.im.annotation.RequirePermission;
import com.ruoyi.im.common.PageResult;
import com.ruoyi.im.common.Result;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.contact.ImFriendAddRequest;
import com.ruoyi.im.dto.contact.ImFriendQueryRequest;
import com.ruoyi.im.dto.contact.ImFriendUpdateRequest;
import com.ruoyi.im.service.ImFriendService;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.vo.contact.ImContactGroupVO;
import com.ruoyi.im.vo.contact.ImFriendVO;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IM联系人控制器
 * 提供好友管理、联系人查询、好友申请等接口
 * 
 * @author ruoyi
 */
@Api(tags = "IM联系人管理")
@RestController
@RequestMapping("/api/{version}/im/contacts")
@ImApiVersion(value = {"v1", "v2"}, description = "联系人管理API，支持v1和v2版本")
public class ImContactController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ImContactController.class);

    @Autowired
    private ImFriendService imFriendService;
    
    @Autowired
    private ImUserService imUserService;

    /**
     * 将ImFriend实体转换为ImFriendVO
     */
    private ImFriendVO convertToVO(ImFriend friend, ImUser user) {
        if (friend == null || user == null) {
            return null;
        }
        
        ImFriendVO vo = new ImFriendVO();
        vo.setId(friend.getId());
        vo.setFriendUserId(friend.getFriendUserId());
        vo.setFriendUsername(user.getUsername());
        vo.setFriendNickname(user.getNickname());
        vo.setFriendAvatar(user.getAvatar());
        vo.setFriendStatus(user.getStatus());
        vo.setFriendStatusDesc(getUserStatusDesc(user.getStatus()));
        vo.setFriendEmail(user.getEmail());
        vo.setFriendPhone(user.getPhone());
        vo.setAlias(friend.getAlias());
        vo.setRemark(friend.getRemark());
        vo.setFriendRelationStatus(friend.getStatus());
        vo.setFriendRelationStatusDesc(getFriendStatusDesc(friend.getStatus()));
        vo.setCreateTime(friend.getCreateTime());
        vo.setUpdateTime(friend.getUpdateTime());
        
        return vo;
    }

    /**
     * 将ImUser转换为ImFriendVO（非好友关系）
     */
    private ImFriendVO convertToVO(ImUser user) {
        if (user == null) {
            return null;
        }
        
        ImFriendVO vo = new ImFriendVO();
        vo.setFriendUserId(user.getId());
        vo.setFriendUsername(user.getUsername());
        vo.setFriendNickname(user.getNickname());
        vo.setFriendAvatar(user.getAvatar());
        vo.setFriendStatus(user.getStatus());
        vo.setFriendStatusDesc(getUserStatusDesc(user.getStatus()));
        vo.setFriendEmail(user.getEmail());
        vo.setFriendPhone(user.getPhone());
        vo.setIsFriend(false);
        
        return vo;
    }

    /**
     * 获取用户状态描述
     */
    private String getUserStatusDesc(String status) {
        if (status == null) {
            return "离线";
        }
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
            case "ONLINE":
                return "在线";
            case "BUSY":
                return "忙碌";
            case "AWAY":
                return "离开";
            case "DND":
                return "请勿打扰";
            default:
                return "离线";
        }
    }

    /**
     * 获取好友关系状态描述
     */
    private String getFriendStatusDesc(String status) {
        if (status == null) {
            return "正常";
        }
        
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "正常";
            case "BLOCKED":
                return "已屏蔽";
            case "SUSPENDED":
                return "已停用";
            default:
                return "正常";
        }
    }
  
    /**
     * 获取联系人列表
     * 根据当前用户ID分页获取所有好友信息，支持关键词搜索
     */
    @ApiOperation(value = "获取联系人列表", notes = "分页获取当前用户的好友列表，支持按用户名、昵称或别名搜索")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping
    @RequirePermission(value = "im:contact:list", desc = "查看联系人列表")
    public Result<PageResult<ImFriendVO>> getContactList(
            @Valid ImFriendQueryRequest request,
            BindingResult bindingResult) {
        
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("获取联系人列表请求: keyword={}, pageNum={}, pageSize={}", 
                request.getKeyword(), request.getPageNum(), request.getPageSize());
        
        Long currentUserId = getCurrentUserId();
        
        try {
            // 使用PageHelper进行分页查询
            startPage(request.getPageNum(), request.getPageSize());
            List<ImFriend> friendList = imFriendService.selectImFriendListByUserId(currentUserId);
            
            // 转换为VO列表
            List<ImFriendVO> friendVOList = friendList.stream()
                .map(friend -> {
                    ImUser user = imUserService.selectImUserById(friend.getFriendUserId());
                    if (user != null) {
                        // 过滤关键词搜索
                        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                            String keyword = request.getKeyword().trim().toLowerCase();
                            String username = user.getUsername().toLowerCase();
                            String nickname = user.getNickname().toLowerCase();
                            String alias = friend.getAlias() != null ? friend.getAlias().toLowerCase() : "";
                            
                            if (!username.contains(keyword) && 
                                !nickname.contains(keyword) && 
                                !alias.contains(keyword)) {
                                return null;
                            }
                        }
                        
                        return convertToVO(friend, user);
                    }
                    return null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
            
            PageInfo<ImFriend> pageInfo = new PageInfo<>(friendList);
            PageResult<ImFriendVO> pageResult = new PageResult<>(
                    friendVOList,
                    pageInfo.getTotal(),
                    pageInfo.getPageNum(),
                    pageInfo.getPageSize()
            );
            
            log.info("联系人列表查询成功: 用户ID={}, 总数={}", currentUserId, pageInfo.getTotal());
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("获取联系人列表异常: 用户ID={}, error={}", currentUserId, e.getMessage(), e);
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取联系人详情
     * 根据用户ID获取联系人详细信息，包括好友关系状态
     */
    @ApiOperation(value = "获取联系人详情", notes = "根据用户ID获取联系人详细信息，包括个人资料和好友关系状态")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "联系人不存在")
    })
    @GetMapping("/{userId}")
    @RequirePermission(value = "im:contact:detail", desc = "查看联系人详情")
    public Result<ImFriendVO> getContactDetail(
            @PathVariable @NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须为正数") Long userId) {
        
        log.info("获取联系人详情请求: userId={}", userId);
        
        Long currentUserId = getCurrentUserId();
        
        try {
            ImUser user = imUserService.selectImUserById(userId);
            if (user == null) {
                log.warn("联系人不存在: userId={}", userId);
                return Result.error(404, "联系人不存在");
            }
            
            ImFriend friend = imFriendService.selectImFriendByUserIdAndFriendUserId(currentUserId, userId);
            
            ImFriendVO contactVO;
            if (friend != null) {
                contactVO = convertToVO(friend, user);
            } else {
                contactVO = convertToVO(user);
            }
            
            log.info("联系人详情查询成功: userId={}, isFriend={}", userId, friend != null);
            return Result.success(contactVO);
        } catch (Exception e) {
            log.error("获取联系人详情异常: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 添加联系人（发送好友申请）
     * 向指定用户发送好友申请，建立好友关系
     */
    @ApiOperation(value = "添加联系人", notes = "向指定用户发送好友申请，建立好友关系")
    @ApiResponses({
        @ApiResponse(code = 200, message = "好友申请发送成功"),
        @ApiResponse(code = 400, message = "参数验证失败或已是好友"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "好友申请发送失败")
    })
    @PostMapping
    @RequirePermission(value = "im:contact:add", desc = "添加联系人")
    public Result<ImFriendVO> addContact(
            @Valid @RequestBody ImFriendAddRequest request,
            BindingResult bindingResult) {
        
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        log.info("添加联系人请求: friendUserId={}, message={}, alias={}", 
                request.getFriendUserId(), request.getMessage(), request.getAlias());
        
        Long currentUserId = getCurrentUserId();
        
        // 检查不能添加自己为好友
        if (currentUserId.equals(request.getFriendUserId())) {
            log.warn("用户尝试添加自己为好友: userId={}", currentUserId);
            return Result.error(400, "不能添加自己为好友");
        }
        
        try {
            // 检查用户是否存在
            ImUser user = imUserService.selectImUserById(request.getFriendUserId());
            if (user == null) {
                logger.warn("用户不存在: friendUserId={}", request.getFriendUserId());
                return Result.error(404, "用户不存在");
            }
            
            // 检查是否已经是好友
            ImFriend existingFriend = imFriendService.selectImFriendByUserIdAndFriendUserId(currentUserId, request.getFriendUserId());
            if (existingFriend != null) {
                logger.warn("已经是好友: currentUserId={}, friendUserId={}", currentUserId, request.getFriendUserId());
                return Result.error(400, "已经是好友，无需重复添加");
            }
            
            // 发送好友申请
            int addResult = imFriendService.addFriend(currentUserId, request.getFriendUserId(), request.getAlias(), request.getMessage());
            
            if (addResult > 0) {
                // 获取新创建的好友记录
                ImFriend friend = imFriendService.selectImFriendByUserIdAndFriendUserId(currentUserId, request.getFriendUserId());
                ImFriendVO friendVO = convertToVO(friend, user);
                
                logger.info("好友申请发送成功: currentUserId={}, friendUserId={}", currentUserId, request.getFriendUserId());
                return Result.success(friendVO, "好友申请已发送");
            } else {
                logger.error("好友申请发送失败: currentUserId={}, friendUserId={}", currentUserId, request.getFriendUserId());
                return Result.error(500, "好友申请发送失败");
            }
        } catch (Exception e) {
            logger.error("添加联系人异常: currentUserId={}, friendUserId={}, error={}", 
                    currentUserId, request.getFriendUserId(), e.getMessage(), e);
            return Result.error(500, "添加联系人失败: " + e.getMessage());
        }
    }

    /**
     * 更新联系人信息
     * 修改好友的别名和备注信息
     */
    @ApiOperation(value = "更新联系人信息", notes = "修改好友的别名和备注信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "联系人不存在"),
        @ApiResponse(code = 500, message = "更新失败")
    })
    @PutMapping
    @RequirePermission(value = "im:contact:update", desc = "更新联系人信息")
    public Result<ImFriendVO> updateContact(
            @Valid @RequestBody ImFriendUpdateRequest request,
            BindingResult bindingResult) {
        
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return Result.error(validationResult.getCode(), validationResult.getMessage());
        }
        
        logger.info("更新联系人信息请求: id={}, alias={}, remark={}, friendUserId={}", 
                request.getId(), request.getAlias(), request.getRemark(), request.getFriendUserId());
        
        Long currentUserId = getCurrentUserId();
        
        try {
            ImFriend friend = imFriendService.selectImFriendById(request.getId());
            
            if (friend == null) {
                logger.warn("联系人不存在: id={}", request.getId());
                return Result.error(404, "联系人不存在");
            }
            
            // 验证好友关系是否属于当前用户
            if (!currentUserId.equals(friend.getUserId())) {
                logger.warn("无权更新好友信息: friendId={}, currentUserId={}, friendOwnerId={}", 
                        request.getId(), currentUserId, friend.getUserId());
                return Result.error(403, "无权更新该联系人信息");
            }
            
            // 更新好友信息
            if (request.getAlias() != null) {
                friend.setAlias(request.getAlias());
            }
            if (request.getRemark() != null) {
                friend.setRemark(request.getRemark());
            }
            
            int updateResult = imFriendService.updateImFriend(friend);
            
            if (updateResult > 0) {
                // 获取更新后的用户信息
                ImUser user = imUserService.selectImUserById(friend.getFriendUserId());
                ImFriendVO friendVO = convertToVO(friend, user);
                
                logger.info("联系人信息更新成功: id={}", request.getId());
                return Result.success(friendVO, "联系人更新成功");
            } else {
                logger.error("联系人信息更新失败: id={}", request.getId());
                return Result.error(500, "联系人更新失败");
            }
        } catch (Exception e) {
            logger.error("更新联系人信息异常: id={}, error={}", request.getId(), e.getMessage(), e);
            return Result.error(500, "更新联系人失败: " + e.getMessage());
        }
    }

    /**
     * 删除联系人（删除好友关系）
     * 删除与指定用户的好友关系
     */
    @ApiOperation(value = "删除联系人", notes = "删除与指定用户的好友关系")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 404, message = "联系人不存在"),
        @ApiResponse(code = 500, message = "删除失败")
    })
    @DeleteMapping("/{userId}")
    @RequirePermission(value = "im:contact:delete", desc = "删除联系人")
    public Result<Void> deleteContact(
            @PathVariable @NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须为正数") Long userId) {
        
        logger.info("删除联系人请求: userId={}", userId);
        
        Long currentUserId = getCurrentUserId();
        
        try {
            int deleteResult = imFriendService.deleteFriend(currentUserId, userId);
            if (deleteResult > 0) {
                logger.info("联系人删除成功: currentUserId={}, userId={}", currentUserId, userId);
                return Result.success();
            } else {
                logger.warn("联系人不存在: currentUserId={}, userId={}", currentUserId, userId);
                return Result.error(404, "联系人不存在");
            }
        } catch (Exception e) {
            logger.error("删除联系人异常: currentUserId={}, userId={}, error={}", 
                    currentUserId, userId, e.getMessage(), e);
            return Result.error(500, "删除联系人失败: " + e.getMessage());
        }
    }

    /**
     * 搜索用户
     * 根据关键词搜索用户，可用于添加好友功能
     */
    @ApiOperation(value = "搜索用户", notes = "根据关键词搜索用户，用于添加好友功能")
    @ApiResponses({
        @ApiResponse(code = 200, message = "搜索成功"),
        @ApiResponse(code = 400, message = "关键词不能为空"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 403, message = "权限不足"),
        @ApiResponse(code = 500, message = "搜索失败")
    })
    @GetMapping("/search")
    @RequirePermission(value = "im:contact:search", desc = "搜索用户")
    public Result<PageResult<ImFriendVO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        logger.info("搜索用户请求: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        
        Long currentUserId = getCurrentUserId();
        
        try {
            // 使用PageHelper进行分页查询
            startPage(pageNum, pageSize);
            
            // 创建查询条件
            ImUser queryUser = new ImUser();
            queryUser.setUsername(keyword);
            queryUser.setNickname(keyword);
            
            List<ImUser> users = imUserService.selectImUserList(queryUser);
            
            // 转换为VO列表，排除自己
            List<ImFriendVO> userVOList = users.stream()
                .filter(u -> !u.getId().equals(currentUserId)) // 排除自己
                .map(this::convertToVO)
                .collect(Collectors.toList());
            
            PageInfo<ImUser> pageInfo = new PageInfo<>(users);
            PageResult<ImFriendVO> pageResult = new PageResult<>(
                    userVOList,
                    pageInfo.getTotal(),
                    pageInfo.getPageNum(),
                    pageInfo.getPageSize()
            );
            
            logger.info("用户搜索成功: keyword={}, 总数={}", keyword, pageInfo.getTotal());
            return Result.success(pageResult);
        } catch (Exception e) {
            logger.error("搜索用户异常: keyword={}, error={}", keyword, e.getMessage(), e);
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取联系人分组
     */
    @GetMapping("/groups")
    public Result<List<Map<String, Object>>> getContactGroups() {
        try {
            // 这里可以实现联系人分组功能
            // 暂时返回默认分组
            List<Map<String, Object>> groups = java.util.Arrays.asList(
                java.util.Map.of("name", "我的好友", "count", 10),
                java.util.Map.of("name", "同事", "count", 5),
                java.util.Map.of("name", "家人", "count", 3),
                java.util.Map.of("name", "同学", "count", 8)
            );
            
            return Result.success(groups, "获取联系人分组成功");
        } catch (Exception e) {
            return Result.error(500, "获取联系人分组失败: " + e.getMessage());
        }
    }

    /**
     * 获取联系人在线状态
     */
    @PostMapping("/status")
    public Result<Map<String, Object>> getContactStatus(@RequestBody List<Long> userIds) {
        try {
            Long currentUserId = getCurrentUserId(); // 获取当前用户ID

            Map<Long, String> statusMap = new HashMap<>();
            if (userIds != null && !userIds.isEmpty()) {
                for (Long userId : userIds) {
                    ImUser user = imUserService.selectImUserById(userId);
                    if (user != null) {
                        statusMap.put(userId, user.getStatus() != null ? user.getStatus() : "offline");
                    } else {
                        statusMap.put(userId, "offline");
                    }
                }
            }

            return Result.success(statusMap, "查询成功");
        } catch (Exception e) {
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 设置备注
     */
    @PutMapping("/{contactId}/remark")
    public Result<ImFriendVO> setRemark(@PathVariable Long contactId, 
                                       @RequestParam String remark) {
        try {
            Long currentUserId = getCurrentUserId();
            
            ImFriend friend = imFriendService.selectImFriendByUserIdAndFriendUserId(currentUserId, contactId);
            if (friend == null) {
                return Result.error(404, "联系人不存在");
            }
            
            friend.setRemark(remark);
            int updateResult = imFriendService.updateImFriend(friend);
            
            if (updateResult > 0) {
                // 获取更新后的用户信息并转换为VO
                ImUser user = imUserService.selectImUserById(friend.getFriendUserId());
                ImFriendVO friendVO = convertToVO(friend, user);
                return Result.success(friendVO, "备注设置成功");
            } else {
                return Result.error(500, "备注设置失败");
            }
        } catch (Exception e) {
            return Result.error(500, "设置备注失败: " + e.getMessage());
        }
    }
}