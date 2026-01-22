package com.ruoyi.common.utils.security;

import com.ruoyi.common.utils.StringUtils;
import java.util.regex.Pattern;

/**
 * 密码策略工具类
 * 提供强密码验证功能，符合企业级安全要求
 * 
 * @author ruoyi
 */
public class PasswordPolicyValidator {
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    // 常见弱密码列表
    private static final String[] WEAK_PASSWORDS = {
        "123456", "password", "password", "123456", "123456", "123456", "123456", "admin",
        "123123", "111111", "000000", "qwerty", "abc123", "123qwe", "qwe123",
        "654321", "123321", "123456789", "1234567890", "password123",
        "admin123", "root123", "test123", "user123", "guest123"
    };
    
    // 常见密码模式
    private static final Pattern[] COMMON_PATTERNS = {
        // 纯数字
        Pattern.compile("^\\d+$"),
        // 纯字母
        Pattern.compile("^[a-zA-Z]+$"),
        // 连续数字
        Pattern.compile("(?:012|123|234|345|456|567|678|789|890|987|876|765|654|543|432|321|210)"),
        // 连续字母
        Pattern.compile("(?:abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)"),
        // 重复字符
        Pattern.compile("(.)\\1{2,}")
    };
    
    /**
     * 验证密码是否符合安全策略
     * 
     * @param password 待验证的密码
     * @return 验证结果，包含是否通过和错误信息
     */
    public static PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        if (StringUtils.isEmpty(password)) {
            result.setValid(false);
            result.setErrorMessage("密码不能为空");
            return result;
        }
        
        // 长度检查
        if (password.length() < MIN_PASSWORD_LENGTH) {
            result.setValid(false);
            result.setErrorMessage(String.format("密码长度不能少于%d位", MIN_PASSWORD_LENGTH));
            return result;
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            result.setValid(false);
            result.setErrorMessage(String.format("密码长度不能超过%d位", MAX_PASSWORD_LENGTH));
            return result;
        }
        
        // 弱密码检查
        if (isWeakPassword(password)) {
            result.setValid(false);
            result.setErrorMessage("密码过于简单，请使用更复杂的密码");
            return result;
        }
        
        // 复杂度检查 - 必须包含以下3种字符类型中的至少3种
        int complexityScore = calculateComplexity(password);
        if (complexityScore < 3) {
            result.setValid(false);
            result.setErrorMessage("密码必须包含大小写字母、数字、特殊字符中的至少3种");
            return result;
        }
        
        result.setValid(true);
        return result;
    }
    
    /**
     * 检查是否为弱密码
     */
    private static boolean isWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();
        
        // 检查常见弱密码
        for (String weakPassword : WEAK_PASSWORDS) {
            if (lowerPassword.contains(weakPassword)) {
                return true;
            }
        }
        
        // 检查常见模式
        for (Pattern pattern : COMMON_PATTERNS) {
            if (pattern.matcher(password).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 计算密码复杂度分数
     * 
     * @param password 密码
     * @return 复杂度分数 (0-4)
     */
    private static int calculateComplexity(String password) {
        int score = 0;
        
        // 包含小写字母
        if (password.matches(".*[a-z].*")) {
            score++;
        }
        
        // 包含大写字母
        if (password.matches(".*[A-Z].*")) {
            score++;
        }
        
        // 包含数字
        if (password.matches(".*\\d.*")) {
            score++;
        }
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>/?`~].*")) {
            score++;
        }
        
        return score;
    }
    
    /**
     * 生成密码强度等级描述
     */
    public static String getPasswordStrengthDescription(String password) {
        if (StringUtils.isEmpty(password)) {
            return "无";
        }
        
        int complexityScore = calculateComplexity(password);
        
        if (complexityScore >= 4) {
            return "很强";
        } else if (complexityScore >= 3) {
            return "强";
        } else if (complexityScore >= 2) {
            return "中等";
        } else if (complexityScore >= 1) {
            return "弱";
        } else {
            return "很弱";
        }
    }
    
    /**
     * 获取密码策略要求描述
     */
    public static String getPasswordPolicyDescription() {
        return String.format(
            "密码要求：\n" +
            "1. 长度：%d-%d位\n" +
            "2. 复杂度：必须包含大小写字母、数字、特殊字符中的至少3种\n" +
            "3. 安全性：不能使用常见弱密码或连续重复字符",
            MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH
        );
    }
    
    /**
     * 密码验证结果类
     */
    public static class PasswordValidationResult {
        private boolean valid;
        private String errorMessage;
        private int complexityScore;
        private String strengthDescription;
        
        public PasswordValidationResult() {
            this.valid = false;
            this.errorMessage = "";
            this.complexityScore = 0;
            this.strengthDescription = "";
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public int getComplexityScore() {
            return complexityScore;
        }
        
        public void setComplexityScore(int complexityScore) {
            this.complexityScore = complexityScore;
        }
        
        public String getStrengthDescription() {
            return strengthDescription;
        }
        
        public void setStrengthDescription(String strengthDescription) {
            this.strengthDescription = strengthDescription;
        }
    }
}