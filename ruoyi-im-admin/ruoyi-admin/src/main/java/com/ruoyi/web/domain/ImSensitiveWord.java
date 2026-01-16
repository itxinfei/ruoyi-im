package com.ruoyi.web.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感词实体
 *
 * 对应数据库表 im_sensitive_word
 *
 * @author ruoyi
 */
public class ImSensitiveWord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 敏感词ID
     */
    private Long id;

    /**
     * 敏感词内容
     */
    private String word;

    /**
     * 敏感词类型
     */
    private String wordType;

    /**
     * 敏感级别（1低 2中 3高）
     */
    private Integer level;

    /**
     * 处理方式（REPLACE替换、REJECT拒绝、WARN警告）
     */
    private String action;

    /**
     * 替换内容
     */
    private String replacement;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
