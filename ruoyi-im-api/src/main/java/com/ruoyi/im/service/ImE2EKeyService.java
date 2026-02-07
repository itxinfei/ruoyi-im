package com.ruoyi.im.service;

import com.ruoyi.im.domain.ImE2EKey;
import com.ruoyi.im.dto.e2e.E2EKeyRegisterRequest;
import com.ruoyi.im.vo.e2e.E2EKeyVO;

import java.util.List;

/**
 * 端到端加密密钥服务接口
 *
 * @author ruoyi
 */
public interface ImE2EKeyService {

    /**
     * 注册或更新用户公钥
     *
     * @param request 注册请求
     * @param userId 用户ID
     * @return 密钥VO
     */
    E2EKeyVO registerPublicKey(E2EKeyRegisterRequest request, Long userId);

    /**
     * 获取用户的公钥
     *
     * @param userId 用户ID
     * @return 公钥VO
     */
    E2EKeyVO getUserPublicKey(Long userId);

    /**
     * 批量获取用户的公钥
     *
     * @param userIds 用户ID列表
     * @return 公钥列表
     */
    List<E2EKeyVO> getPublicKeys(List<Long> userIds);

    /**
     * 删除用户的旧密钥并生成新密钥
     *
     * @param userId 用户ID
     * @return 新密钥VO
     */
    E2EKeyVO rotateKey(Long userId);

    /**
     * 禁用用户的密钥
     *
     * @param userId 用户ID
     */
    void disableUserKeys(Long userId);

    /**
     * 检查用户是否启用E2E加密
     *
     * @param userId 用户ID
     * @return 是否启用
     */
    boolean isUserE2EEnabled(Long userId);
}
