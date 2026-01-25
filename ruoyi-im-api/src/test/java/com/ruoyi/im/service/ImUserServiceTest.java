package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.dto.user.ImUserRegisterRequest;
import com.ruoyi.im.dto.user.ImUserUpdateRequest;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.service.impl.ImUserServiceImpl;
import com.ruoyi.im.test.BaseTest;
import com.ruoyi.im.test.util.MockDataFactory;
import com.ruoyi.im.vo.user.ImUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 *
 * 测试用户相关的业务逻辑
 *
 * @author ruoyi
 */
public class ImUserServiceTest extends BaseTest {

    @Autowired
    private ImUserService userService;

    @MockBean
    private ImUserMapper userMapper;

    @MockBean
    private ImFriendService friendService;

    @Test
    @DisplayName("获取用户信息 - 成功")
    public void testGetUserById_Success() {
        // Arrange
        Long userId = getTestUserId();
        ImUser expectedUser = MockDataFactory.createTestUser(userId);

        when(userMapper.selectImUserById(userId)).thenReturn(expectedUser);

        // Act
        ImUser result = userService.selectImUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser" + userId, result.getUsername());

        verify(userMapper, times(1)).selectImUserById(userId);
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    public void testGetUserById_NotFound() {
        // Arrange
        Long userId = 99999L;
        when(userMapper.selectImUserById(userId)).thenReturn(null);

        // Act
        ImUser result = userService.selectImUserById(userId);

        // Assert
        assertNull(result);

        verify(userMapper, times(1)).selectImUserById(userId);
    }

    @Test
    @DisplayName("批量获取用户信息 - 成功")
    public void testGetUsersByIds_Success() {
        // Arrange
        List<Long> userIds = List.of(1L, 2L, 3L);
        List<ImUser> expectedUsers = MockDataFactory.createTestUsers(3);

        when(userMapper.selectImUserListByIds(userIds)).thenReturn(expectedUsers);

        // Act
        List<ImUser> result = userService.selectImUserListByIds(userIds);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(userMapper, times(1)).selectImUserListByIds(userIds);
    }

    @Test
    @DisplayName("更新用户状态 - 成功")
    public void testUpdateUserStatus_Success() {
        // Arrange
        Long userId = getTestUserId();
        String newStatus = "ONLINE";

        ImUser existingUser = MockDataFactory.createTestUser(userId);
        when(userMapper.selectImUserById(userId)).thenReturn(existingUser);
        when(userMapper.updateById(any(ImUser.class))).thenReturn(1);

        // Act
        userService.updateUserStatus(userId, newStatus);

        // Assert
        verify(userMapper, times(1)).updateById(any(ImUser.class));
    }

    @Test
    @DisplayName("搜索用户 - 关键词匹配")
    public void testSearchUsers_Success() {
        // Arrange
        String keyword = "test";
        List<ImUser> expectedUsers = MockDataFactory.createTestUsers(3);

        when(userMapper.selectImUserList(any())).thenReturn(expectedUsers);

        // Act
        List<ImUserVO> result = userService.searchUsers(keyword);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());

        verify(userMapper, times(1)).selectImUserList(any());
    }

    @Test
    @DisplayName("检查用户名是否存在 - 已存在")
    public void testCheckUsernameExists_Exists() {
        // Arrange
        String username = "testuser1";
        ImUser existingUser = MockDataFactory.createTestUser(1L);
        existingUser.setUsername(username);

        when(userMapper.selectImUserByUsername(username)).thenReturn(existingUser);

        // Act
        boolean result = userService.checkUsernameExists(username);

        // Assert
        assertTrue(result);

        verify(userMapper, times(1)).selectImUserByUsername(username);
    }

    @Test
    @DisplayName("检查用户名是否存在 - 不存在")
    public void testCheckUsernameExists_NotExists() {
        // Arrange
        String username = "nonexistent_user";
        when(userMapper.selectImUserByUsername(username)).thenReturn(null);

        // Act
        boolean result = userService.checkUsernameExists(username);

        // Assert
        assertFalse(result);

        verify(userMapper, times(1)).selectImUserByUsername(username);
    }

    @Test
    @DisplayName("获取在线用户列表 - 成功")
    public void testGetOnlineUsers_Success() {
        // Arrange
        List<ImUser> onlineUsers = MockDataFactory.createTestUsers(5);

        when(userMapper.selectImUserList(any())).thenReturn(onlineUsers);

        // Act
        List<ImUser> result = userService.getOnlineUsers();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());

        verify(userMapper, times(1)).selectImUserList(any());
    }
}
