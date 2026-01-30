package com.ruoyi.im.test;

import com.ruoyi.im.domain.ImFriend;
import com.ruoyi.im.domain.ImGroup;
import com.ruoyi.im.domain.ImGroupMember;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.mapper.ImFriendMapper;
import com.ruoyi.im.mapper.ImGroupMapper;
import com.ruoyi.im.mapper.ImGroupMemberMapper;
import com.ruoyi.im.mapper.ImUserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简单批量添加好友和群组测试
 * 直接使用MyBatis的SqlSession进行数据库操作
 */
public class SimpleBatchAddTest {

    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            // 加载MyBatis配置文件
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        
        try {
            // 获取Mapper接口
            ImUserMapper userMapper = sqlSession.getMapper(ImUserMapper.class);
            ImFriendMapper friendMapper = sqlSession.getMapper(ImFriendMapper.class);
            ImGroupMapper groupMapper = sqlSession.getMapper(ImGroupMapper.class);
            ImGroupMemberMapper groupMemberMapper = sqlSession.getMapper(ImGroupMemberMapper.class);

            // 1. 获取张三和李四的用户信息
            ImUser zhangsan = userMapper.selectImUserByUsername("zhangsan");
            ImUser lisi = userMapper.selectImUserByUsername("lisi");

            if (zhangsan == null || lisi == null) {
                System.out.println("未找到张三或李四账号");
                return;
            }

            System.out.println("找到用户：");
            System.out.println("张三 - ID: " + zhangsan.getId() + ", 用户名: " + zhangsan.getUsername());
            System.out.println("李四 - ID: " + lisi.getId() + ", 用户名: " + lisi.getUsername());

            // 2. 获取所有其他用户
            ImUser queryUser = new ImUser();
            queryUser.setStatus(1); // 只获取启用状态的用户
            List<ImUser> allUsers = userMapper.selectImUserList(queryUser);
            
            // 过滤掉张三和李四
            List<ImUser> otherUsers = allUsers.stream()
                    .filter(user -> !user.getId().equals(zhangsan.getId()) && !user.getId().equals(lisi.getId()))
                    .collect(Collectors.toList());
            
            System.out.println("\n其他用户列表（共" + otherUsers.size() + "人）：");
            otherUsers.forEach(user -> System.out.println("ID: " + user.getId() + ", 用户名: " + user.getUsername()));

            // 3. 获取所有群组
            List<ImGroup> allGroups = groupMapper.selectImGroupList(new ImGroup());
            System.out.println("\n所有群组（共" + allGroups.size() + "个）：");
            allGroups.forEach(group -> System.out.println("ID: " + group.getId() + ", 群组名称: " + group.getName()));

            // 4. 给张三添加所有其他人为好友
            System.out.println("\n开始给张三添加好友...");
            for (ImUser otherUser : otherUsers) {
                // 检查是否已经是好友
                ImFriend existingFriend = friendMapper.selectImFriendByUserIdAndFriendId(zhangsan.getId(), otherUser.getId());
                if (existingFriend == null) {
                    // 创建好友关系
                    LocalDateTime now = LocalDateTime.now();
                    
                    // 张三 -> 其他用户
                    ImFriend friend1 = new ImFriend();
                    friend1.setUserId(zhangsan.getId());
                    friend1.setFriendId(otherUser.getId());
                    friend1.setIsDeleted(0);
                    friend1.setCreateTime(now);
                    friend1.setUpdateTime(now);
                    friendMapper.insertImFriend(friend1);

                    // 其他用户 -> 张三
                    ImFriend friend2 = new ImFriend();
                    friend2.setUserId(otherUser.getId());
                    friend2.setFriendId(zhangsan.getId());
                    friend2.setIsDeleted(0);
                    friend2.setCreateTime(now);
                    friend2.setUpdateTime(now);
                    friendMapper.insertImFriend(friend2);

                    System.out.println("添加好友：张三 -> " + otherUser.getUsername() + "(" + otherUser.getId() + ")");
                }
            }

            // 5. 给李四添加所有其他人为好友
            System.out.println("\n开始给李四添加好友...");
            for (ImUser otherUser : otherUsers) {
                // 检查是否已经是好友
                ImFriend existingFriend = friendMapper.selectImFriendByUserIdAndFriendId(lisi.getId(), otherUser.getId());
                if (existingFriend == null) {
                    // 创建好友关系
                    LocalDateTime now = LocalDateTime.now();
                    
                    // 李四 -> 其他用户
                    ImFriend friend1 = new ImFriend();
                    friend1.setUserId(lisi.getId());
                    friend1.setFriendId(otherUser.getId());
                    friend1.setIsDeleted(0);
                    friend1.setCreateTime(now);
                    friend1.setUpdateTime(now);
                    friendMapper.insertImFriend(friend1);

                    // 其他用户 -> 李四
                    ImFriend friend2 = new ImFriend();
                    friend2.setUserId(otherUser.getId());
                    friend2.setFriendId(lisi.getId());
                    friend2.setIsDeleted(0);
                    friend2.setCreateTime(now);
                    friend2.setUpdateTime(now);
                    friendMapper.insertImFriend(friend2);

                    System.out.println("添加好友：李四 -> " + otherUser.getUsername() + "(" + otherUser.getId() + ")");
                }
            }

            // 6. 给张三加入所有群
            System.out.println("\n开始给张三加入群组...");
            for (ImGroup group : allGroups) {
                // 检查是否已经在群里
                ImGroupMember existingMember = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(group.getId(), zhangsan.getId());
                if (existingMember == null) {
                    // 添加到群组
                    LocalDateTime now = LocalDateTime.now();
                    ImGroupMember member = new ImGroupMember();
                    member.setGroupId(group.getId());
                    member.setUserId(zhangsan.getId());
                    member.setRole("MEMBER");
                    member.setIsDeleted(0);
                    member.setIsMuted(0);
                    member.setCreateTime(now);
                    member.setUpdateTime(now);
                    groupMemberMapper.insertImGroupMember(member);

                    System.out.println("加入群组：张三 -> " + group.getName() + "(" + group.getId() + ")");
                }
            }

            // 7. 给李四加入所有群
            System.out.println("\n开始给李四加入群组...");
            for (ImGroup group : allGroups) {
                // 检查是否已经在群里
                ImGroupMember existingMember = groupMemberMapper.selectImGroupMemberByGroupIdAndUserId(group.getId(), lisi.getId());
                if (existingMember == null) {
                    // 添加到群组
                    LocalDateTime now = LocalDateTime.now();
                    ImGroupMember member = new ImGroupMember();
                    member.setGroupId(group.getId());
                    member.setUserId(lisi.getId());
                    member.setRole("MEMBER");
                    member.setIsDeleted(0);
                    member.setIsMuted(0);
                    member.setCreateTime(now);
                    member.setUpdateTime(now);
                    groupMemberMapper.insertImGroupMember(member);

                    System.out.println("加入群组：李四 -> " + group.getName() + "(" + group.getId() + ")");
                }
            }

            // 提交事务
            sqlSession.commit();
            System.out.println("\n批量操作完成！");

        } catch (Exception e) {
            e.printStackTrace();
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
    }
}
