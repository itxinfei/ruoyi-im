package com.ruoyi.im.exception;

/**
 * 限流异常
 * 当接口请求超过限流阈值时抛出
 *
 * @author ruoyi
 */
public class RateLimitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 限流key
     */
    private String limitKey;

    /**
     * 限流次数
     */
    private int limitCount;

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String limitKey, int limitCount) {
        super(String.format("接口访问频率过高，Key: %s, 限制: %d次/分钟", limitKey, limitCount));
        this.limitKey = limitKey;
        this.limitCount = limitCount;
    }

    public RateLimitException(String message, String limitKey, int limitCount) {
        super(message);
        this.limitKey = limitKey;
        this.limitCount = limitCount;
    }

    public String getLimitKey() {
        return limitKey;
    }

    public int getLimitCount() {
        return limitCount;
    }
}
