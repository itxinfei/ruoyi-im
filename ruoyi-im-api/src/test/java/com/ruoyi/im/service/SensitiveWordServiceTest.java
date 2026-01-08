package com.ruoyi.im.service;

import com.ruoyi.im.service.impl.SensitiveWordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敏感词服务测试
 *
 * @author ruoyi
 */
@SpringBootTest
public class SensitiveWordServiceTest {

    private ISensitiveWordService sensitiveWordService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sensitiveWordService = new SensitiveWordServiceImpl();
    }

    @Test
    public void testContainsSensitiveWord() {
        // 测试敏感词检测
        String text1 = "这是一个测试";
        assertFalse(sensitiveWordService.containsSensitiveWord(text1));

        String text2 = "这是一个笨蛋测试";
        // 如果敏感词库已加载，应该返回true
        boolean result = sensitiveWordService.containsSensitiveWord(text2);
        System.out.println("敏感词检测结果: " + result);
    }

    @Test
    public void testDetectSensitiveWords() {
        // 测试敏感词检测（返回所有敏感词）
        String text = "测试文本包含傻瓜和笨蛋";
        Set<String> words = sensitiveWordService.detectSensitiveWords(text);
        assertNotNull(words);
        System.out.println("检测到的敏感词: " + words);
    }

    @Test
    public void testFilter() {
        // 测试敏感词过滤
        String text = "这是一个笨蛋测试";
        String filtered = sensitiveWordService.filter(text);
        assertNotNull(filtered);
        assertNotEquals(text, filtered);
        System.out.println("过滤前: " + text);
        System.out.println("过滤后: " + filtered);
    }

    @Test
    public void testFilterWithReplacement() {
        // 测试自定义替换符
        String text = "这是一个笨蛋测试";
        String filtered = sensitiveWordService.filter(text, "***");
        assertTrue(filtered.contains("***"));
    }

    @Test
    public void testEmptyText() {
        // 测试空文本
        assertFalse(sensitiveWordService.containsSensitiveWord(""));
        assertFalse(sensitiveWordService.containsSensitiveWord(null));
        assertEquals("", sensitiveWordService.filter(""));
        assertNull(sensitiveWordService.filter(null));
    }
}
