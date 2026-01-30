package com.ruoyi.im.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 批量添加好友和群组测试
 * 给张三和李四账号加其他所有人为好友，加所有的群
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestConfig.class)
@EnableAutoConfiguration(exclude = {
    HibernateJpaAutoConfiguration.class
})
public class BatchAddFriendsAndGroupsTest {

    @Autowired
    private ImUserMapper imUserMapper;

    @Autowired
    private ImFriendMapper imFriendMapper;

    @Autowired
    private ImGroupMapper imGroupMapper;

    @Autowired
    private ImGroupMemberMapper imGroupMemberMapper;

    /**
     * 执行批量添加好友和群组的操作
     */
    @Test
    public void testBatchAddFriendsAndGroups() {
        // 1. 获取张三和李四的用户信息
        ImUser zhangsan = getUserIdByUsername("zhangsan");
        ImUser lisi = getUserIdByUsername("lisi");

        if (zhangsan == null || lisi == null) {
            System.out.println("未找到张三或李四账号");
            return;
        }

        System.out.println("找到用户：");
        System.out.println("张三 - ID: " + zhangsan.getId() + ", 用户名: " + zhangsan.getUsername());
        System.out.println("李四 - ID: " + lisi.getId() + ", 用户名: " + lisi.getUsername());

        // 2. 获取所有其他用户
        List<ImUser> otherUsers = getAllOtherUsers(zhangsan.getId(), lisi.getId());
        System.out.println("\n其他用户列表（共" + otherUsers.size() + "人）：");
        otherUsers.forEach(user -> System.out.println("ID: " + user.getId() + ", 用户名: " + user.getUsername()));

        // 3. 获取所有群组
        List<ImGroup> allGroups = getAllGroups();
        System.out.println("\n所有群组（共" + allGroups.size() + "个）：");
        allGroups.forEach(group -> System.out.println("ID: " + group.getId() + ", 群组名称: " + group.getName()));

        // 4. 给张三添加所有其他人为好友
        System.out.println("\n开始给张三添加好友...");
        addAllFriends(zhangsan.getId(), otherUsers);

        // 5. 给李四添加所有其他人为好友
        System.out.println("\n开始给李四添加好友...");
        addAllFriends(lisi.getId(), otherUsers);

        // 6. 给张三加入所有群
        System.out.println("\n开始给张三加入群组...");
        addToAllGroups(zhangsan.getId(), allGroups);

        // 7. 给李四加入所有群
        System.out.println("\n开始给李四加入群组...");
        addToAllGroups(lisi.getId(), allGroups);

        System.out.println("\n批量操作完成！");
    }

    /**
     * 根据用户名获取用户信息
     */
    private ImUser getUserIdByUsername(String username) {
        return imUserMapper.selectImUserByUsername(username);
    }

    /**
     * 获取除了指定用户外的所有其他用户
     */
    private List<ImUser> getAllOtherUsers(Long... excludeUserIds) {
        // 由于ImUserMapper没有提供复杂的查询方法，这里我们先获取所有用户，然后在内存中过滤
        ImUser queryUser = new ImUser();
        queryUser.setStatus(1); // 只获取启用状态的用户
        List<ImUser> allUsers = imUserMapper.selectImUserList(queryUser);
        
        // 过滤掉指定的用户
        return allUsers.stream()
                .filter(user -> {
                    for (Long excludeId : excludeUserIds) {
                        if (user.getId().equals(excludeId)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取所有群组
     */
    private List<ImGroup> getAllGroups() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ImGroup> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0); // 只获取未删除的群组
        return imGroupMapper.selectList(queryWrapper);
    }

    /**
     * 给指定用户添加所有其他用户为好友
     */
    private void addAllFriends(Long userId, List<ImUser> otherUsers) {
        for (ImUser otherUser : otherUsers) {
            // 检查是否已经是好友
            if (!isFriend(userId, otherUser.getId())) {
                // 创建好友关系
                createFriendship(userId, otherUser.getId());
                System.out.println("添加好友：用户" + userId + " -> " + otherUser.getUsername() + "(" + otherUser.getId() + ")");
            }
        }
    }

    /**
     * 检查两个用户是否已经是好友
     */
    private boolean isFriend(Long userId1, Long userId2) {
        ImFriend friend = imFriendMapper.selectImFriendByUserIdAndFriendId(userId1, userId2);
        return friend != null;
    }

    /**
     * 创建好友关系（双向）
     */
    private void createFriendship(Long userId1, Long userId2) {
        LocalDateTime now = LocalDateTime.now();
        
        // 用户1 -> 用户2
        ImFriend friend1 = new ImFriend();
        friend1.setUserId(userId1);
        friend1.setFriendId(userId2);
        friend1.setIsDeleted(0); // 未删除
        friend1.setCreateTime(now);
        friend1.setUpdateTime(now);
        imFriendMapper.insertImFriend(friend1);

        // 用户2 -> 用户1
        ImFriend friend2 = new ImFriend();
        friend2.setUserId(userId2);
        friend2.setFriendId(userId1);
        friend2.setIsDeleted(0); // 未删除
        friend2.setCreateTime(now);
        friend2.setUpdateTime(now);
        imFriendMapper.insertImFriend(friend2);
    }

    /**
     * 给指定用户加入所有群组
     */
    private void addToAllGroups(Long userId, List<ImGroup> groups) {
        for (ImGroup group : groups) {
            // 检查用户是否已经在群里
            if (!isInGroup(userId, group.getId())) {
                // 添加用户到群组
                addUserToGroup(userId, group.getId());
                System.out.println("加入群组：用户" + userId + " -> " + group.getName() + "(" + group.getId() + ")");
            }
        }
    }

    /**
     * 检查用户是否已经在群组中
     */
    private boolean isInGroup(Long userId, Long groupId) {
        ImGroupMember member = imGroupMemberMapper.selectImGroupMemberByGroupIdAndUserId(groupId, userId);
        return member != null;
    }

    /**
     * 添加用户到群组
     */
    private void addUserToGroup(Long userId, Long groupId) {
        LocalDateTime now = LocalDateTime.now();
        
        ImGroupMember member = new ImGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("MEMBER"); // 角色：普通成员
        member.setIsDeleted(0); // 未删除
        member.setIsMuted(0); // 未禁言
        member.setCreateTime(now);
        member.setUpdateTime(now);
        imGroupMemberMapper.insertImGroupMember(member);
    }
}
