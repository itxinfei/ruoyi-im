package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.im.domain.ImE2EKey;
import com.ruoyi.im.dto.e2e.E2EKeyRegisterRequest;
import com.ruoyi.im.mapper.ImE2EKeyMapper;
import com.ruoyi.im.service.ImE2EKeyService;
import com.ruoyi.im.vo.e2e.E2EKeyVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 端到端加密密钥服务实现
 *
 * @author ruoyi
 */
@Service
public class ImE2EKeyServiceImpl implements ImE2EKeyService {

    private static final Logger log = LoggerFactory.getLogger(ImE2EKeyServiceImpl.class);

    private final ImE2EKeyMapper e2eKeyMapper;
    private final com.ruoyi.im.util.E2EEncryptionUtil encryptionUtil;

    public ImE2EKeyServiceImpl(ImE2EKeyMapper e2eKeyMapper) {
        this.e2eKeyMapper = e2eKeyMapper;
        this.encryptionUtil = new com.ruoyi.im.util.E2EEncryptionUtil();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E2EKeyVO registerPublicKey(E2EKeyRegisterRequest request, Long userId) {
        // 检查是否已存在激活的密钥
        ImE2EKey existingKey = e2eKeyMapper.selectActiveKeyByUserId(userId);
        if (existingKey != null) {
            // 如果公钥相同，直接返回
            String requestPublicKey = request.getPublicKey();
            if (requestPublicKey != null && requestPublicKey.equals(existingKey.getPublicKey())) {
                log.info("公钥已存在，无需更新: userId={}", userId);
                return toVO(existingKey);
            }
            // 禁用旧密钥
            existingKey.setIsActive(false);
            e2eKeyMapper.updateById(existingKey);
            log.info("禁用旧密钥: userId={}, version={}", userId, existingKey.getKeyVersion());
        }

        // 获取当前最大版本号
        Integer maxVersion = 0;
        List<ImE2EKey> userKeys = e2eKeyMapper.selectKeysByUserId(userId);
        if (!userKeys.isEmpty()) {
            maxVersion = userKeys.get(0).getKeyVersion();
        }

        // 创建新密钥记录
        ImE2EKey newKey = new ImE2EKey();
        newKey.setUserId(userId);
        newKey.setPublicKey(request.getPublicKey());
        newKey.setKeyVersion(maxVersion + 1);
        newKey.setKeyAlgorithm(request.getKeyAlgorithm() != null ? request.getKeyAlgorithm() : "RSA");
        newKey.setKeySize(request.getKeySize() != null ? request.getKeySize() : 2048);
        newKey.setIsActive(true);
        newKey.setCreateTime(LocalDateTime.now());
        newKey.setExpireTime(LocalDateTime.now().plusYears(1)); // 默认1年有效期

        e2eKeyMapper.insert(newKey);
        log.info("注册新公钥: userId={}, version={}", userId, newKey.getKeyVersion());

        return toVO(newKey);
    }

    @Override
    public E2EKeyVO getUserPublicKey(Long userId) {
        ImE2EKey key = e2eKeyMapper.selectActiveKeyByUserId(userId);
        if (key == null) {
            return null;
        }
        return toVO(key);
    }

    @Override
    public List<E2EKeyVO> getPublicKeys(List<Long> userIds) {
        List<ImE2EKey> keys = e2eKeyMapper.selectActiveKeysByUserIds(userIds);
        return keys.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E2EKeyVO rotateKey(Long userId) {
        // 禁用所有旧密钥
        LambdaUpdateWrapper<ImE2EKey> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ImE2EKey::getUserId, userId)
                .set(ImE2EKey::getIsActive, false);
        e2eKeyMapper.update(null, updateWrapper);

        // 生成新的RSA密钥对
        KeyPair keyPair = com.ruoyi.im.util.E2EEncryptionUtil.generateRSAKeyPair();
        String publicKeyPEM = com.ruoyi.im.util.E2EEncryptionUtil.publicKeyToPEM(keyPair.getPublic());

        // 获取当前最大版本号
        Integer maxVersion = 0;
        List<ImE2EKey> userKeys = e2eKeyMapper.selectKeysByUserId(userId);
        if (!userKeys.isEmpty()) {
            maxVersion = userKeys.get(0).getKeyVersion();
        }

        // 创建新密钥记录
        ImE2EKey newKey = new ImE2EKey();
        newKey.setUserId(userId);
        newKey.setPublicKey(publicKeyPEM);
        newKey.setKeyVersion(maxVersion + 1);
        newKey.setKeyAlgorithm("RSA");
        newKey.setKeySize(2048);
        newKey.setIsActive(true);
        newKey.setCreateTime(LocalDateTime.now());
        newKey.setExpireTime(LocalDateTime.now().plusYears(1));

        e2eKeyMapper.insert(newKey);
        log.info("密钥轮换成功: userId={}, newVersion={}", userId, newKey.getKeyVersion());

        return toVO(newKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUserKeys(Long userId) {
        LambdaUpdateWrapper<ImE2EKey> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ImE2EKey::getUserId, userId)
                .set(ImE2EKey::getIsActive, false);
        e2eKeyMapper.update(null, updateWrapper);
        log.info("禁用用户所有密钥: userId={}", userId);
    }

    @Override
    public boolean isUserE2EEnabled(Long userId) {
        ImE2EKey key = e2eKeyMapper.selectActiveKeyByUserId(userId);
        return key != null && key.getIsActive();
    }

    private E2EKeyVO toVO(ImE2EKey entity) {
        E2EKeyVO vo = new E2EKeyVO();
        vo.setUserId(entity.getUserId());
        vo.setPublicKey(entity.getPublicKey());
        vo.setKeyVersion(entity.getKeyVersion());
        vo.setKeyAlgorithm(entity.getKeyAlgorithm());
        vo.setKeySize(entity.getKeySize());
        vo.setIsActive(entity.getIsActive());
        vo.setExpireTime(entity.getExpireTime());
        return vo;
    }
}
