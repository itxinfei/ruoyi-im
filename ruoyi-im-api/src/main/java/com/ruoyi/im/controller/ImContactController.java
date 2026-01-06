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
 * 联系人控制器
 * 
 * 提供联系人管理相关功能，包括好友列表查询、添加好友、删除好友、好友信息更新等。
 * 支持分页查询、条件筛选等高级功能，满足用户对联系人管理的需求。
 * 
 * @author ruoyi
 */
@Api(tags = "联系人管理")
@RestController
@RequestMapping("/api/{version}/im/contact")
@ImApiVersion(value = {"v1", "v2"}, description = "联系人管理API，支持v1和v2版本")
public class ImContactController extends BaseController {
    
    private static final Logger log = LoggerFactory.getLogger(ImContactController.class);

    @Autowired
    private ImFriendService imFriendService;
    
    @Autowired
    private ImUserService imUserService;

    /**
     * 获取联系人列表
     * 
     * 查询当前用户的好友列表，支持分页和关键词搜索功能。
     * 返回包含好友基本信息的分页结果，便于前端展示。
     * 
     * @param request 联系人查询请求参数
     * @return 联系人分页结果
     */
    @ApiOperation(value = "获取联系人列表", notes = "查询当前用户的好友列表，支持分页和关键词搜索")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "version", value = "API版本", required = true, dataType = "String", paramType = "path"),
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/list")
    @RequirePermission(value = "im:contact:list", desc = "查看联系人列表")
    public Result<PageResult<ImFriendVO>> list(ImFriendQueryRequest request) {
        
        log.info("获取联系人列表请求， keyword={}, pageNum={}, pageSize={}", 
                request.getKeyword(), request.getPageNum(), request.getPageSize());
        
        Long currentUserId = getCurrentUserId();
        
        try {
            // 使用PageHelper进行分页查询
            startPage(request.getPageNum(), request.getPageSize());
            List<ImFriend> friendList = imFriendService.selectImFriendListByUserId(currentUserId);

            // 转换为VO对象
            List<ImFriendVO> friendVOList = friendList.stream().map(friend -> {
                ImUser friendUser = imUserService.selectById(friend.getFriendId());
                if (friendUser != null) {
                    ImFriendVO vo = new ImFriendVO();
                    vo.setUserId(friend.getUserId());
                    vo.setFriendId(friend.getFriendId());
                    vo.setNickname(friendUser.getNickname());
                    vo.setAvatar(friendUser.getAvatar());
                    vo.setRemark(friend.getRemark());
                    vo.setStatus(friend.getStatus());
                    vo.setCreateTime(friend.getCreateTime());
                    return vo;
                }
                return null;
            }).filter(vo -> vo != null).collect(Collectors.toList());

            PageResult<ImFriendVO> pageResult = new PageResult<>();
            pageResult.setRows(friendVOList);
            pageResult.setTotal(friendList.size());
            pageResult.setPageNum(request.getPageNum());
            pageResult.setPageSize(request.getPageSize());
            pageResult.setPages((int) Math.ceil((double) friendList.size() / request.getPageSize()));

            log.info("获取联系人列表成功， count={}", friendVOList.size());
            return Result.success("获取联系人列表成功", pageResult);
        } catch (Exception e) {
            log.error("获取联系人列表异常: {}", e.getMessage(), e);
            return Result.error(500, "获取联系人列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取联系人
     * 
     * 根据指定的用户ID查询联系人信息，通常用于查看特定联系人的详细信息。
     * 
     * @param userId 用户ID
     * @return 联系人信息
     */
    @ApiOperation(value = "根据用户ID获取联系人", notes = "根据指定的用户ID查询联系人信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "version", value = "API版本", required = true, dataType = "String", paramType = "path"),
        @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataType = "Long", paramType = "path")
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 404, message = "联系人不存在"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/{userId}")
    @RequirePermission(value = "im:contact:query", desc = "查看联系人信息")
    public Result<ImFriendVO> getInfo(@PathVariable @NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须为正数") Long userId) {
        log.info("获取联系人信息请求， userId={}", userId);
        
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "用户未登录");
            }
            
            // 查询好友关系
            ImFriend friend = imFriendService.selectImFriendByUserIdAndFriendId(currentUserId, userId);
            if (friend == null) {
                log.warn("联系人不存在: userId={}, friendId={}", currentUserId, userId);
                return Result.error(404, "联系人不存在");
            }
            
            // 查询用户信息
            ImUser friendUser = imUserService.selectById(userId);
            if (friendUser == null) {
                log.warn("好友用户不存在， userId={}", userId);
                return Result.error(404, "好友用户不存在");
            }
            
            // 构建VO对象
            ImFriendVO friendVO = new ImFriendVO();
            friendVO.setUserId(friend.getUserId());
            friendVO.setFriendId(friend.getFriendId());
            friendVO.setNickname(friendUser.getNickname());
            friendVO.setAvatar(friendUser.getAvatar());
            friendVO.setRemark(friend.getRemark());
            friendVO.setStatus(friend.getStatus());
            friendVO.setCreateTime(friend.getCreateTime());
            
            log.info("获取联系人信息成功， userId={}", userId);
            return Result.success("获取联系人信息成功", friendVO);
        } catch (Exception e) {
            log.error("获取联系人信息异常， userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "获取联系人信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加联系人（好友）
     * 
     * 向指定用户发送好友请求，建立好友关系。
     * 
     * @param request 添加好友请求参数
     * @param bindingResult 参数验证结果
     * @return 添加结果
     */
    @ApiOperation(value = "添加联系人", notes = "向指定用户发送好友请求，建立好友关系")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "version", value = "API版本", required = true, dataType = "String", paramType = "path"),
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "添加成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 409, message = "已是好友关系"),
        @ApiResponse(code = 500, message = "添加失败")
    })
    @PostMapping
    @RequirePermission(value = "im:contact:add", desc = "添加联系人")
    public Result<Void> add(@Valid @RequestBody ImFriendAddRequest request, BindingResult bindingResult) {
        log.info("添加联系人请求， request={}", request);
        
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "用户未登录");
            }
            
            // 检查是否已经好友
            ImFriend existingFriend = imFriendService.selectImFriendByUserIdAndFriendId(currentUserId, request.getFriendId());
            if (existingFriend != null) {
                log.warn("用户已是好友: userId={}, friendId={}", currentUserId, request.getFriendId());
                return Result.error(409, "您与该用户已是好友");
            }
            
            // 检查好友用户是否存在
            ImUser friendUser = imUserService.selectById(request.getFriendId());
            if (friendUser == null) {
                log.warn("好友用户不存在， friendId={}", request.getFriendId());
                return Result.error(404, "好友用户不存在");
            }
            
            // 创建好友关系
            ImFriend friend = new ImFriend();
            friend.setUserId(currentUserId);
            friend.setFriendId(request.getFriendId());
            friend.setRemark(request.getRemark());
            friend.setStatus("active"); // 默认为活跃状态
            
            int result = imFriendService.insertImFriend(friend);
            
            if (result > 0) {
                log.info("添加联系人成功， userId={}, friendId={}", currentUserId, request.getFriendId());
                return Result.success();
            } else {
                log.error("添加联系人失败， userId={}, friendId={}", currentUserId, request.getFriendId());
                return Result.error(500, "添加好友失败");
            }
        } catch (Exception e) {
            log.error("添加联系人异常， error={}", e.getMessage(), e);
            return Result.error(500, "添加好友失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除联系人
     * 
     * 解除与指定用户的好友关系。
     * 
     * @param userId 要删除的用户ID
     * @return 删除结果
     */
    @ApiOperation(value = "删除联系人", notes = "解除与指定用户的好友关系")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "version", value = "API版本", required = true, dataType = "String", paramType = "path"),
        @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataType = "Long", paramType = "path")
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 404, message = "联系人不存在"),
        @ApiResponse(code = 500, message = "删除失败")
    })
    @DeleteMapping("/{userId}")
    @RequirePermission(value = "im:contact:remove", desc = "删除联系人")
    public Result<Void> remove(@PathVariable @NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须为正数") Long userId) {
        log.info("删除联系人请求， userId={}", userId);
        
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "用户未登录");
            }
            
            // 检查好友关系是否存在
            ImFriend friend = imFriendService.selectImFriendByUserIdAndFriendId(currentUserId, userId);
            if (friend == null) {
                log.warn("好友关系不存在， userId={}, friendId={}", currentUserId, userId);
                return Result.error(404, "好友关系不存在");
            }
            
            // 删除好友关系
            int result = imFriendService.deleteImFriendByUserIdAndFriendId(currentUserId, userId);
            
            if (result > 0) {
                log.info("删除联系人成功， userId={}, friendId={}", currentUserId, userId);
                return Result.success();
            } else {
                log.error("删除联系人失败， userId={}, friendId={}", currentUserId, userId);
                return Result.error(500, "删除好友失败");
            }
        } catch (Exception e) {
            log.error("删除联系人异常， userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "删除好友失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新好友信息
     * 
     * 更新好友的备注信息或其他相关信息。
     * 
     * @param userId 好友用户ID
     * @param request 更新好友信息请求参数
     * @param bindingResult 参数验证结果
     * @return 更新结果
     */
    @ApiOperation(value = "更新好友信息", notes = "更新好友的备注信息或其他相关信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "version", value = "API版本", required = true, dataType = "String", paramType = "path"),
        @ApiImplicitParam(name = "userId", value = "用户ID", required = true, dataType = "Long", paramType = "path")
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功"),
        @ApiResponse(code = 400, message = "参数验证失败"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 404, message = "联系人不存在"),
        @ApiResponse(code = 500, message = "更新失败")
    })
    @PutMapping("/{userId}")
    @RequirePermission(value = "im:contact:edit", desc = "编辑联系人信息")
    public Result<Void> edit(@PathVariable @NotNull(message = "用户ID不能为空") @Positive(message = "用户ID必须为正数") Long userId,
                             @Valid @RequestBody ImFriendUpdateRequest request, BindingResult bindingResult) {
        log.info("更新好友信息请求: userId={}, request={}", userId, request);
        
        Result<Void> validationResult = handleValidationError(bindingResult);
        if (validationResult != null) {
            return validationResult;
        }
        
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "用户未登录");
            }
            
            // 检查好友关系是否存在
            ImFriend friend = imFriendService.selectImFriendByUserIdAndFriendId(currentUserId, userId);
            if (friend == null) {
                log.warn("好友关系不存在， userId={}, friendId={}", currentUserId, userId);
                return Result.error(404, "好友关系不存在");
            }
            
            // 更新好友信息
            friend.setRemark(request.getRemark());
            
            int result = imFriendService.updateImFriend(friend);
            
            if (result > 0) {
                log.info("更新好友信息成功: userId={}, friendId={}", currentUserId, userId);
                return Result.success();
            } else {
                log.error("更新好友信息失败: userId={}, friendId={}", currentUserId, userId);
                return Result.error(500, "更新好友信息失败");
            }
        } catch (Exception e) {
            log.error("更新好友信息异常: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error(500, "更新好友信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取联系人群组
     * 
     * 获取联系人群组信息，通常用于展示分组的联系人列表。
     * 
     * @return 联系人群组信息
     */
    @ApiOperation(value = "获取联系人群组", notes = "获取联系人群组信息，用于分组展示联系人")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "version", value = "API版本", required = true, dataType = "String", paramType = "path"),
    })
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未授权访问"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/groups")
    @RequirePermission(value = "im:contact:group", desc = "查看联系人群组")
    public Result<List<ImContactGroupVO>> getGroups() {
        log.info("获取联系人群组请求");
        
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return Result.error(401, "用户未登录");
            }
            
            // 查询好友列表
            List<ImFriend> friendList = imFriendService.selectImFriendListByUserId(currentUserId);
            
            // 按首字母分组（简化实现，实际项目中可能需要更复杂的分组逻辑）
            Map<String, List<ImFriendVO>> groupedFriends = new HashMap<>();
            for (ImFriend friend : friendList) {
                ImUser friendUser = imUserService.selectById(friend.getFriendId());
                if (friendUser != null) {
                    String firstLetter = getFirstLetter(friendUser.getNickname());
                    groupedFriends.computeIfAbsent(firstLetter, k -> new java.util.ArrayList<>());
                    
                    ImFriendVO vo = new ImFriendVO();
                    vo.setUserId(friend.getUserId());
                    vo.setFriendId(friend.getFriendId());
                    vo.setNickname(friendUser.getNickname());
                    vo.setAvatar(friendUser.getAvatar());
                    vo.setRemark(friend.getRemark());
                    vo.setStatus(friend.getStatus());
                    vo.setCreateTime(friend.getCreateTime());
                    
                    groupedFriends.get(firstLetter).add(vo);
                }
            }
            
            // 转换为群组VO
            List<ImContactGroupVO> groupList = groupedFriends.entrySet().stream()
                .map(entry -> {
                    ImContactGroupVO group = new ImContactGroupVO();
                    group.setGroupName(entry.getKey());
                    group.setFriends(entry.getValue());
                    return group;
                })
                .sorted((a, b) -> a.getGroupName().compareTo(b.getGroupName()))
                .collect(Collectors.toList());
            
            log.info("获取联系人群组成功， count={}", groupList.size());
            return Result.success("获取联系人群组成功", groupList);
        } catch (Exception e) {
            log.error("获取联系人群组异常: {}", e.getMessage(), e);
            return Result.error(500, "获取联系人群组失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取首字母（简化实现）
     * 
     * @param text 文本
     * @return 首字母
     */
    private String getFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return "#";
        }
        // 简化实现，实际项目中可能需要更复杂的拼音转换逻辑
        char firstChar = text.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            return String.valueOf(firstChar);
        } else if (firstChar >= 'a' && firstChar <= 'z') {
            return String.valueOf((char)(firstChar - 32)); // 转换为大写
        } else {
            return "#"; // 非字母字符归为#
        }
    }
}