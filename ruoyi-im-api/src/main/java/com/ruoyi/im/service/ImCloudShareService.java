package com.ruoyi.im.service;

import com.ruoyi.im.dto.cloud.ImCloudFileShareRequest;
import com.ruoyi.im.vo.cloud.ImCloudFileShareVO;

import java.util.List;

/**
 * 云盘分享服务接口
 */
public interface ImCloudShareService {

    ImCloudFileShareVO createShare(ImCloudFileShareRequest request, Long userId);

    void cancelShare(Long shareId, Long userId);

    List<ImCloudFileShareVO> getShareList(Long userId);

    ImCloudFileShareVO accessShare(String shareCode, String password);
}
