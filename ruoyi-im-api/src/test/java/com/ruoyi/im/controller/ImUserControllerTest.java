package com.ruoyi.im.controller;

import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.test.BaseTest;
import com.ruoyi.im.test.util.MockDataFactory;
import com.ruoyi.im.vo.user.ImUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器测试
 *
 * 测试用户相关的 REST API 接口
 *
 * @author ruoyi
 */
public class ImUserControllerTest extends BaseTest {

    @org.springframework.beans.factory.annotation.Autowired
    private ImUserController userController;

    @MockBean
    private ImUserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    public void testGetUserInfo_Success() {
        // Arrange
        Long userId = getTestUserId();
        ImUser expectedUser = MockDataFactory.createTestUser(userId);

        when(userService.selectImUserById(userId)).thenReturn(expectedUser);

        // Act
        var response = userController.getInfo(userId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());

        verify(userService, times(1)).selectImUserById(userId);
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    public void testGetUserInfo_NotFound() {
        // Arrange
        Long userId = 99999L;
        when(userService.selectImUserById(userId)).thenReturn(null);

        // Act
        var response = userController.getInfo(userId);

        // Assert
        assertNotNull(response);

        verify(userService, times(1)).selectImUserById(userId);
    }

    @Test
    @DisplayName("更新用户状态 - 成功")
    public void testUpdateUserStatus_Success() {
        // Arrange
        Long userId = getTestUserId();
        String newStatus = "ONLINE";

        // Act
        var response = userController.updateStatus(userId, newStatus);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());

        verify(userService, times(1)).updateUserStatus(eq(userId), eq(newStatus));
    }

    @Test
    @DisplayName("搜索用户 - 成功")
    public void testSearchUsers_Success() {
        // Arrange
        String keyword = "test";
        List<ImUserVO> expectedUsers = List.of(
            createMockUserVO(1L, "testuser1", "测试用户1"),
            createMockUserVO(2L, "testuser2", "测试用户2")
        );

        when(userService.searchUsers(keyword)).thenReturn(expectedUsers);

        // Act
        var response = userController.searchUsers(keyword);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());

        verify(userService, times(1)).searchUsers(keyword);
    }

    @Test
    @DisplayName("批量获取用户信息 - 成功")
    public void testGetUsersById_Success() {
        // Arrange
        List<Long> userIds = List.of(1L, 2L, 3L);
        List<ImUser> expectedUsers = MockDataFactory.createTestUsers(3);

        when(userService.selectImUserListByIds(userIds)).thenReturn(expectedUsers);

        // Act
        var response = userController.getUsersById(userIds);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(3, response.getData().size());

        verify(userService, times(1)).selectImUserListByIds(userIds);
    }

    /**
     * 创建模拟用户VO
     */
    private ImUserVO createMockUserVO(Long id, String username, String nickname) {
        ImUserVO vo = new ImUserVO();
        vo.setId(id);
        vo.setUsername(username);
        vo.setNickname(nickname);
        vo.setAvatar("/avatar/default.png");
        vo.setStatus("ACTIVE");
        return vo;
    }
}
