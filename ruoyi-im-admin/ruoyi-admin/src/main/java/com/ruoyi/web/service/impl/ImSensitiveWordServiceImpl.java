package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImSensitiveWord;
import com.ruoyi.web.mapper.ImSensitiveWordMapper;
import com.ruoyi.web.service.ImSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 敏感词Service实现
 *
 * @author ruoyi
 */
@Service
public class ImSensitiveWordServiceImpl implements ImSensitiveWordService {

    @Autowired
    private ImSensitiveWordMapper imSensitiveWordMapper;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private static final String RELOAD_CHANNEL = "im:sensitive:reload";

    @Override
    public ImSensitiveWord selectImSensitiveWordById(Long id) {
        return imSensitiveWordMapper.selectImSensitiveWordById(id);
    }

    @Override
    public ImSensitiveWord selectImSensitiveWordByWord(String word) {
        return imSensitiveWordMapper.selectImSensitiveWordByWord(word);
    }

    @Override
    public List<ImSensitiveWord> selectImSensitiveWordList(ImSensitiveWord imSensitiveWord) {
        return imSensitiveWordMapper.selectImSensitiveWordList(imSensitiveWord);
    }

    @Override
    public List<ImSensitiveWord> selectEnabledWords() {
        return imSensitiveWordMapper.selectEnabledWords();
    }

    @Override
    public int insertImSensitiveWord(ImSensitiveWord imSensitiveWord) {
        int rows = imSensitiveWordMapper.insertImSensitiveWord(imSensitiveWord);
        if (rows > 0) {
            publishReloadEvent();
        }
        return rows;
    }

    @Override
    public int updateImSensitiveWord(ImSensitiveWord imSensitiveWord) {
        int rows = imSensitiveWordMapper.updateImSensitiveWord(imSensitiveWord);
        if (rows > 0) {
            publishReloadEvent();
        }
        return rows;
    }

    @Override
    public int deleteImSensitiveWordByIds(Long[] ids) {
        int rows = imSensitiveWordMapper.deleteImSensitiveWordByIds(ids);
        if (rows > 0) {
            publishReloadEvent();
        }
        return rows;
    }

    @Override
    public int deleteImSensitiveWordById(Long id) {
        int rows = imSensitiveWordMapper.deleteImSensitiveWordById(id);
        if (rows > 0) {
            publishReloadEvent();
        }
        return rows;
    }

    @Override
    public int toggleEnabled(Long id) {
        ImSensitiveWord word = selectImSensitiveWordById(id);
        if (word == null) {
            return 0;
        }
        word.setIsEnabled(word.getIsEnabled() == 1 ? 0 : 1);
        int rows = imSensitiveWordMapper.updateImSensitiveWord(word);
        if (rows > 0) {
            publishReloadEvent();
        }
        return rows;
    }

    @Override
    public int batchImportWords(List<String> words) {
        int rows = imSensitiveWordMapper.batchInsertWords(words);
        if (rows > 0) {
            publishReloadEvent();
        }
        return rows;
    }

    @Override
    public Map<String, Object> getSensitiveWordStatistics() {
        return imSensitiveWordMapper.getSensitiveWordStatistics();
    }

    /**
     * 发布重载事件
     */
    private void publishReloadEvent() {
        if (redisTemplate != null) {
            try {
                redisTemplate.convertAndSend(RELOAD_CHANNEL, "reload");
            } catch (Exception e) {
                // Redis不可用时不影响主业务
                e.printStackTrace();
            }
        }
    }
}