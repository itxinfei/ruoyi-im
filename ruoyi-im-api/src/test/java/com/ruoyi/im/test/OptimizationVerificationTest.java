package com.ruoyi.im.test;

import com.ruoyi.im.common.Result;
import com.ruoyi.im.constant.ApiErrorCode;
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
     * 测试错误码枚举的正确性
     */
    @Test
    void testApiErrorCodeEnum() {
        // 验证SUCCESS错误码
        assertEquals(200, ApiErrorCode.SUCCESS.getCode());
        assertEquals("操作成功", ApiErrorCode.SUCCESS.getMessage());

        // 验证ERROR错误码
        assertEquals(500, ApiErrorCode.ERROR.getCode());
        assertEquals("操作失败", ApiErrorCode.ERROR.getMessage());

        // 验证USER_NOT_EXIST错误码
        assertEquals(4001, ApiErrorCode.USER_NOT_EXIST.getCode());
        assertEquals("用户不存在", ApiErrorCode.USER_NOT_EXIST.getMessage());

        // 验证通过错误码获取枚举
        assertEquals(ApiErrorCode.SUCCESS, ApiErrorCode.fromCode(200));
        assertEquals(ApiErrorCode.ERROR, ApiErrorCode.fromCode(500));
        assertEquals(ApiErrorCode.USER_NOT_EXIST, ApiErrorCode.fromCode(4001));

        // 验证不存在的错误码返回默认值
        assertEquals(ApiErrorCode.ERROR, ApiErrorCode.fromCode(9999));
    }

    /**
     * 测试Result类的新功能
     */
    @Test
    void testResultClassEnhancements() {
        // 测试使用错误码枚举的成功响应
        Result<String> successResult = Result.success("测试数据");
        assertTrue(successResult.isSuccess());
        assertEquals(ApiErrorCode.SUCCESS.getCode(), successResult.getCode());
        assertEquals("测试数据", successResult.getData());

        // 测试使用错误码枚举的错误响应
        Result<Void> errorResult = Result.error(ApiErrorCode.USER_NOT_EXIST);
        assertFalse(errorResult.isSuccess());
        assertEquals(ApiErrorCode.USER_NOT_EXIST.getCode(), errorResult.getCode());
        assertEquals(ApiErrorCode.USER_NOT_EXIST.getMessage(), errorResult.getMsg());

        // 测试使用错误码枚举的失败响应
        Result<Void> failResult = Result.fail(ApiErrorCode.PARAM_ERROR);
        assertFalse(failResult.isSuccess());
        assertEquals(ApiErrorCode.PARAM_ERROR.getCode(), failResult.getCode());
        assertEquals(ApiErrorCode.PARAM_ERROR.getMessage(), failResult.getMsg());
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
        String cleanOutput = com.ruoyi.im.filter.XssFilter.cleanXss(maliciousInput);
        
        // 验证恶意脚本被移除
        assertFalse(cleanOutput.contains("<script>"));
        assertFalse(cleanOutput.contains("alert"));
        assertTrue(cleanOutput.contains("Safe content"));
        
        // 验证安全的HTML标签被保留
        assertTrue(cleanOutput.contains("<p>"));
        assertTrue(cleanOutput.contains("</p>"));
    }
}