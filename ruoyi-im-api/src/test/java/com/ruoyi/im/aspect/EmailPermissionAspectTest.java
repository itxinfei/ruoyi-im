package com.ruoyi.im.aspect;

import com.ruoyi.im.annotation.RequireEmailPermission;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.util.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 邮件权限验证切面测试类
 *
 * @author ruoyi
 */
class EmailPermissionAspectTest {

    @InjectMocks
    private EmailPermissionAspect emailPermissionAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private RequireEmailPermission permission;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckEmailPermission_Success() throws NoSuchMethodException {
        // 准备测试数据
        Long userId = 1L;
        Long emailId = 100L;
        Long targetUserId = 1L; // 与当前用户相同

        // 模拟方法签名
        Method testMethod = TestController.class.getMethod("testMethod", Long.class, Long.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"emailId", "userId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{emailId, targetUserId});

        // 模拟权限注解
        when(permission.value()).thenReturn(RequireEmailPermission.PermissionType.READ);
        when(permission.emailIdParam()).thenReturn("emailId");
        when(permission.userIdParam()).thenReturn("userId");
        when(permission.userIdFromHeader()).thenReturn(false);

        // 模拟SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            // 执行测试
            assertDoesNotThrow(() -> emailPermissionAspect.checkEmailPermission(joinPoint, permission));
        }
    }

    @Test
    void testCheckEmailPermission_UserNotLoggedIn() {
        // 模拟权限注解
        when(permission.userIdFromHeader()).thenReturn(false);

        // 模拟SecurityUtils返回null
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class);
             MockedStatic<RequestContextHolder> requestContextHolder = mockStatic(RequestContextHolder.class)) {
            
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(null);
            requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            // 执行测试并验证异常
            BusinessException exception = assertThrows(BusinessException.class, 
                () -> emailPermissionAspect.checkEmailPermission(joinPoint, permission));
            
            assertEquals("用户未登录", exception.getMessage());
        }
    }

    @Test
    void testCheckEmailPermission_EmailIdNull() throws NoSuchMethodException {
        // 准备测试数据
        Long userId = 1L;

        // 模拟方法签名
        Method testMethod = TestController.class.getMethod("testMethod", Long.class, Long.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"emailId", "userId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, userId});

        // 模拟权限注解
        when(permission.emailIdParam()).thenReturn("emailId");
        when(permission.userIdFromHeader()).thenReturn(false);

        // 模拟SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            // 执行测试并验证异常
            BusinessException exception = assertThrows(BusinessException.class, 
                () -> emailPermissionAspect.checkEmailPermission(joinPoint, permission));
            
            assertEquals("邮件ID不能为空", exception.getMessage());
        }
    }

    @Test
    void testCheckEmailPermission_NoPermission() throws NoSuchMethodException {
        // 准备测试数据
        Long currentUserId = 1L;
        Long emailId = 100L;
        Long targetUserId = 2L; // 与当前用户不同

        // 模拟方法签名
        Method testMethod = TestController.class.getMethod("testMethod", Long.class, Long.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"emailId", "userId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{emailId, targetUserId});

        // 模拟权限注解
        when(permission.value()).thenReturn(RequireEmailPermission.PermissionType.READ);
        when(permission.emailIdParam()).thenReturn("emailId");
        when(permission.userIdParam()).thenReturn("userId");
        when(permission.userIdFromHeader()).thenReturn(false);

        // 模拟SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            // 执行测试并验证异常
            BusinessException exception = assertThrows(BusinessException.class, 
                () -> emailPermissionAspect.checkEmailPermission(joinPoint, permission));
            
            assertEquals("您没有权限访问此邮件", exception.getMessage());
        }
    }

    @Test
    void testCheckEmailPermission_AdminPermissionRequired() throws NoSuchMethodException {
        // 准备测试数据
        Long currentUserId = 1L;
        Long emailId = 100L;
        Long targetUserId = 1L;

        // 模拟方法签名
        Method testMethod = TestController.class.getMethod("testMethod", Long.class, Long.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"emailId", "userId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{emailId, targetUserId});

        // 模拟权限注解 - 需要管理员权限
        when(permission.value()).thenReturn(RequireEmailPermission.PermissionType.ADMIN);
        when(permission.emailIdParam()).thenReturn("emailId");
        when(permission.userIdParam()).thenReturn("userId");
        when(permission.userIdFromHeader()).thenReturn(false);

        // 模拟SecurityUtils
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            // 执行测试并验证异常（当前用户不是管理员）
            BusinessException exception = assertThrows(BusinessException.class, 
                () -> emailPermissionAspect.checkEmailPermission(joinPoint, permission));
            
            assertEquals("您没有管理员权限", exception.getMessage());
        }
    }

    @Test
    void testCheckEmailPermission_WithHeaderUserId() throws NoSuchMethodException {
        // 准备测试数据
        Long userId = 1L;
        Long emailId = 100L;
        Long targetUserId = 1L;

        // 模拟方法签名
        Method testMethod = TestController.class.getMethod("testMethod", Long.class, Long.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"emailId", "userId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{emailId, targetUserId});

        // 模拟权限注解 - 从请求头获取用户ID
        when(permission.value()).thenReturn(RequireEmailPermission.PermissionType.READ);
        when(permission.emailIdParam()).thenReturn("emailId");
        when(permission.userIdParam()).thenReturn("userId");
        when(permission.userIdFromHeader()).thenReturn(true);

        // 模拟请求上下文
        try (MockedStatic<RequestContextHolder> requestContextHolder = mockStatic(RequestContextHolder.class)) {
            requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getHeader("X-User-Id")).thenReturn(userId.toString());

            // 执行测试
            assertDoesNotThrow(() -> emailPermissionAspect.checkEmailPermission(joinPoint, permission));
        }
    }

    // 测试用的控制器类
    static class TestController {
        public void testMethod(Long emailId, Long userId) {
            // 测试方法
        }
    }
}
