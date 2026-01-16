package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.ImSensitiveWord;
import com.ruoyi.web.mapper.ImSensitiveWordMapper;
import com.ruoyi.web.service.ImSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return imSensitiveWordMapper.insertImSensitiveWord(imSensitiveWord);
    }

    @Override
    public int updateImSensitiveWord(ImSensitiveWord imSensitiveWord) {
        return imSensitiveWordMapper.updateImSensitiveWord(imSensitiveWord);
    }

    @Override
    public int deleteImSensitiveWordByIds(Long[] ids) {
        return imSensitiveWordMapper.deleteImSensitiveWordByIds(ids);
    }

    @Override
    public int deleteImSensitiveWordById(Long id) {
        return imSensitiveWordMapper.deleteImSensitiveWordById(id);
    }

    @Override
    public int toggleEnabled(Long id) {
        ImSensitiveWord word = selectImSensitiveWordById(id);
        if (word == null) {
            return 0;
        }
        word.setIsEnabled(word.getIsEnabled() == 1 ? 0 : 1);
        return imSensitiveWordMapper.updateImSensitiveWord(word);
    }

    @Override
    public int batchImportWords(List<String> words) {
        return imSensitiveWordMapper.batchInsertWords(words);
    }

    @Override
    public Map<String, Object> getSensitiveWordStatistics() {
        return imSensitiveWordMapper.getSensitiveWordStatistics();
    }
}
