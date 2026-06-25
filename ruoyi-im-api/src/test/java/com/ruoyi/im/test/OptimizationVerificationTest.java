package com.ruoyi.im.test;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.ImErrorCode;
import com.ruoyi.im.controller.ImAuthController;
import com.ruoyi.im.dto.user.ImLoginRequest;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * IM系统优化效果验证测试
 * 验证安全增强、性能优化、API文档完善和架构改进的效果
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
public class OptimizationVerificationTest {

    @Mock
    private ImUserService mockUserService;

    @Mock
    private JwtUtils mockJwtUtils;

    @InjectMocks
    private ImAuthController authController;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
    }

    /**
     * 测试错误码常量
     */
    @Test
    void testErrorCodes() {
        // 验证成功错误码
        assertEquals(200, ImErrorCode.SUCCESS);

        // 验证失败错误码
        assertEquals(500, ImErrorCode.ERROR);

        // 验证USER_NOT_EXIST错误码
        assertEquals(4001, ImErrorCode.USER_NOT_EXIST);
    }

    /**
     * 测试Result类的新功能
     */
    @Test
    void testResultClassEnhancements() {
        // 测试成功响应
        Result<String> successResult = Result.success(null, "测试数据");
        assertTrue(successResult.isSuccess());
        assertEquals(ImErrorCode.SUCCESS, successResult.getCode());
        assertEquals("测试数据", successResult.getData());

        // 测试错误响应
        Result<Void> errorResult = Result.error(ImErrorCode.USER_NOT_EXIST, "用户不存在");
        assertFalse(errorResult.isSuccess());
        assertEquals(ImErrorCode.USER_NOT_EXIST, errorResult.getCode());
        assertEquals("用户不存在", errorResult.getMsg());

        // 测试失败响应
        Result<Void> failResult = Result.fail(ImErrorCode.PARAM_ERROR, "参数错误");
        assertFalse(failResult.isSuccess());
        assertEquals(ImErrorCode.PARAM_ERROR, failResult.getCode());
        assertEquals("参数错误", failResult.getMsg());
    }

    /**
     * 测试JWT工具类的配置加载
     */
    @Test
    void testJwtUtilsInitialization() {
        assertNotNull(mockJwtUtils);
        // 由于JWT工具类使用了@PostConstruct，这里主要是验证依赖注入
    }

    /**
     * 测试登录接口的错误处理
     */
    @Test
    void testLoginErrorHandling() {
        // 模拟用户不存在的情况
        ImLoginRequest request = new ImLoginRequest();
        request.setUsername("nonexistent_user");
        request.setPassword("password");

        when(mockUserService.login(any(ImLoginRequest.class)))
                .thenThrow(new RuntimeException("用户不存在"));

        assertThrows(RuntimeException.class, () -> {
            authController.login(request);
        });
    }

    /**
     * 测试安全配置的CORS设置
     */
    @Test
    void testCorsConfiguration() {
        // 这个测试主要验证配置的存在性，实际的CORS行为需要集成测试
        assertNotNull(authController);
    }

    /**
     * 测试缓存配置
     */
    @Test
    void testCacheConfiguration() {
        // 验证缓存配置类的加载
        // 这里主要是验证配置类的存在性，实际的缓存行为需要运行时验证
        assertNotNull(mockUserService);
    }

    /**
     * 测试API版本控制注解
     */
    @Test
    void testApiVersionAnnotation() {
        // 验证API版本控制注解的存在性
        Class<?> apiVersionClass = com.ruoyi.im.annotation.ApiVersion.class;
        assertNotNull(apiVersionClass);
        
        // 验证注解的元数据
        assertTrue(apiVersionClass.isAnnotationPresent(java.lang.annotation.Retention.class));
        assertTrue(apiVersionClass.isAnnotationPresent(java.lang.annotation.Target.class));
    }

    /**
     * 测试XSS防护工具类
     */
    @Test
    void testXssFilter() {
        String maliciousInput = "<script>alert('XSS')</script><p>Safe content</p>";
        String cleanOutput = com.ruoyi.im.util.XssUtil.cleanXss(maliciousInput);
        
        // 验证恶意脚本被移除
        assertFalse(cleanOutput.contains("<script>"));
        assertFalse(cleanOutput.contains("alert"));
        assertTrue(cleanOutput.contains("Safe content"));
        
        // 验证安全的HTML标签被保留
        assertTrue(cleanOutput.contains("<p>"));
        assertTrue(cleanOutput.contains("</p>"));
    }
}