package com.ruoyi.im.dto.e2e;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * E2E公钥注册请求
 *
 * @author ruoyi
 */
public class E2EKeyRegisterRequest {

    /** 公钥（PEM格式） */
    @NotBlank(message = "公钥不能为空")
    private String publicKey;

    /** 密钥算法：RSA/ECC */
    private String keyAlgorithm;

    /** 密钥长度 */
    private Integer keySize;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }
}
