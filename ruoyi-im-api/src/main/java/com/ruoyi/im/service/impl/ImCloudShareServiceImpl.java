package com.ruoyi.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.im.domain.ImCloudFileShare;
import com.ruoyi.im.dto.cloud.ImCloudFileShareRequest;
import com.ruoyi.im.mapper.ImCloudFileShareMapper;
import com.ruoyi.im.service.ImCloudShareService;
import com.ruoyi.im.util.BeanConvertUtil;
import com.ruoyi.im.vo.cloud.ImCloudFileShareVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 云盘分享服务实现
 */
@Service
public class ImCloudShareServiceImpl implements ImCloudShareService {

    private final ImCloudFileShareMapper shareMapper;

    public ImCloudShareServiceImpl(ImCloudFileShareMapper shareMapper) {
        this.shareMapper = shareMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImCloudFileShareVO createShare(ImCloudFileShareRequest req, Long uid) {
        ImCloudFileShare s = new ImCloudFileShare();
        s.setResourceId(req.getResourceId()); s.setSharerId(uid);
        s.setShareCode(UUID.randomUUID().toString().substring(0, 8)); 
        s.setAccessPassword(req.getAccessPassword());
        if (req.getExpireDays() != null && req.getExpireDays() > 0) {
            s.setExpireTime(LocalDateTime.now().plusDays(req.getExpireDays()));
        } else {
            s.setExpireTime(LocalDateTime.now().plusDays(7));
        }
        s.setCreateTime(LocalDateTime.now());
        shareMapper.insert(s);
        return BeanConvertUtil.convert(s, ImCloudFileShareVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelShare(Long id, Long uid) { shareMapper.deleteById(id); }

    @Override
    public List<ImCloudFileShareVO> getShareList(Long uid) {
        LambdaQueryWrapper<ImCloudFileShare> w = new LambdaQueryWrapper<>();
        w.eq(ImCloudFileShare::getSharerId, uid);
        return BeanConvertUtil.convertList(shareMapper.selectList(w), ImCloudFileShareVO.class);
    }

    @Override
    public ImCloudFileShareVO accessShare(String code, String pwd) {
        ImCloudFileShare s = shareMapper.selectByShareCode(code);
        if (s == null) return null;
        return BeanConvertUtil.convert(s, ImCloudFileShareVO.class);
    }
}
