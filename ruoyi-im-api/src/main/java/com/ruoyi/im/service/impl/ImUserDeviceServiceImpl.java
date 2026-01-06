package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.mapper.ImUserDeviceMapper;
import com.ruoyi.im.service.ImUserDeviceService;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * 閻劍鍩涚拋鎯ь槵Service娑撴艾濮熺仦鍌氼槱閻? * 
 * @author ruoyi
 */
@Service
public class ImUserDeviceServiceImpl extends EnhancedBaseServiceImpl<ImUserDevice, ImUserDeviceMapper> implements ImUserDeviceService {
    private static final Logger log = LoggerFactory.getLogger(ImUserDeviceServiceImpl.class);

    @Autowired
    private ImUserDeviceMapper imUserDeviceMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缂傛挸鐡ㄩ柨顔煎缂傗偓
    private static final String USER_DEVICE_CACHE_PREFIX = "im:user:devices:";
    private static final String DEVICE_ID_CACHE_PREFIX = "im:device:id:";
    private static final String USER_CURRENT_DEVICE_CACHE_PREFIX = "im:user:current_device:";

    /**
     * 閺屻儴顕楅悽銊﹀煕鐠佹儳顦?
     * 
     * @param id 閻劍鍩涚拋鎯ь槵ID
     * @return 閻劍鍩涚拋鎯ь槵
     */
    @Override
    @Cacheable(value = "im-user-device", key = "#id")
    public ImUserDevice selectById(Long id) {
        ImUserDevice device = super.selectById(id);
        if (device != null) {
            // 缂傛挸鐡ㄩ悽銊﹀煕閸滃矁顔曟径鍢擠閻ㄥ嫭妲х亸?            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + device.getDeviceId();
            redisTemplate.opsForValue().set(deviceCacheKey, device.getUserId(), 30, TimeUnit.MINUTES);
        }
        return device;
    }

    /**
     * 閺屻儴顕楅悽銊﹀煕鐠佹儳顦崚妤勩€?
     * 
     * @param imUserDevice 閻劍鍩涚拋鎯ь槵
     * @return 閻劍鍩涚拋鎯ь槵闂嗗棗鎮?
     */
    @Override
    public List<ImUserDevice> selectList(ImUserDevice imUserDevice) {
        return imUserDeviceMapper.selectImUserDeviceList(imUserDevice);
    }

    /**
     * 閺傛澘顤冮悽銊﹀煕鐠佹儳顦?
     * 
     * @param imUserDevice 閻劍鍩涚拋鎯ь槵
     * @return 缂佹挻鐏?
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, key = "#imUserDevice.userId")
    public int insert(ImUserDevice imUserDevice) {
        // 鐠佸墽鐤嗛崚娑樼紦閺冨爼妫?
        imUserDevice.setCreateTime(LocalDateTime.now());
        imUserDevice.setUpdateTime(LocalDateTime.now());
        
        // 鐠佸墽鐤嗛崚婵嗩潗閻樿埖鈧?
        if (imUserDevice.getStatus() == null) {
            imUserDevice.setStatus("active");
        }
        
        int result = imUserDeviceMapper.insertImUserDevice(imUserDevice);
        
        if (result > 0) {
            // 濞撳懐鎮婇悽銊﹀煕鐠佹儳顦崚妤勩€冪紓鎾崇摠
            clearUserDevicesCache(imUserDevice.getUserId());
            // 缂傛挸鐡ㄧ拋鎯ь槵娣団剝浼?
            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + imUserDevice.getDeviceId();
            redisTemplate.opsForValue().set(deviceCacheKey, imUserDevice.getUserId(), 30, TimeUnit.MINUTES);
        }
        
        return result;
    }

    /**
     * 娣囶喗鏁奸悽銊﹀煕鐠佹儳顦?
     * 
     * @param imUserDevice 閻劍鍩涚拋鎯ь槵
     * @return 缂佹挻鐏?
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, key = "#imUserDevice.id")
    public int update(ImUserDevice imUserDevice) {
        // 鐠佸墽鐤嗛弴瀛樻煀閺冨爼妫?
        imUserDevice.setUpdateTime(LocalDateTime.now());
        
        // 濡偓閺屻儲妲搁崥锔芥箒鐠佹儳顦悩鑸碘偓浣稿綁閺?        ImUserDevice existingDevice = selectById(imUserDevice.getId());
        if (existingDevice != null && !existingDevice.getStatus().equals(imUserDevice.getStatus())) {
            // 婵″倹鐏夐悩鑸碘偓浣稿綁娑撴椽娼ú鏄忕┈閿涘本绔婚悶鍡楃秼閸撳秷顔曟径鍥╃处鐎?
            if ("inactive".equals(imUserDevice.getStatus()) || "deleted".equals(imUserDevice.getStatus())) {
                String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + imUserDevice.getUserId();
                redisTemplate.delete(currentDeviceCacheKey);
            }
        }
        
        int result = imUserDeviceMapper.updateImUserDevice(imUserDevice);
        
        if (result > 0) {
            // 濞撳懐鎮婇悽銊﹀煕鐠佹儳顦崚妤勩€冪紓鎾崇摠
            clearUserDevicesCache(imUserDevice.getUserId());
        }
        
        return result;
    }

    /**
     * 閹靛綊鍣洪崚鐘绘珟閻劍鍩涚拋鎯ь槵
     * 
     * @param ids 闂団偓鐟曚礁鍨归梽銈囨畱閻劍鍩涚拋鎯ь槵ID
     * @return 缂佹挻鐏?
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, allEntries = true)
    public int deleteByIds(Long[] ids) {
        int result = imUserDeviceMapper.deleteImUserDeviceByIds(ids);
        
        if (result > 0) {
            // 閼惧嘲褰囬幍鈧張澶婂綀瑜板崬鎼烽惃鍕暏閹寸īD楠炶埖绔婚悶鍡欑处鐎?
            for (Long id : ids) {
                ImUserDevice device = selectById(id);
                if (device != null) {
                    clearUserDevicesCache(device.getUserId());
                }
            }
        }
        
        return result;
    }

    /**
     * 閸掔娀娅庨悽銊﹀煕鐠佹儳顦穱鈩冧紖
     * 
     * @param id 閻劍鍩涚拋鎯ь槵ID
     * @return 缂佹挻鐏?
     */
    @Override
    @CacheEvict(value = {"im-user-device", "im-user-devices"}, key = "#id")
    public int deleteById(Long id) {
        ImUserDevice device = selectById(id);
        if (device == null) {
            return 0;
        }
        
        int result = imUserDeviceMapper.deleteImUserDeviceById(id);
        
        if (result > 0) {
            // 濞撳懐鎮婇悽銊﹀煕鐠佹儳顦崚妤勩€冪紓鎾崇摠
            clearUserDevicesCache(device.getUserId());
            // 濞撳懐鎮婄拋鎯ь槵ID閺勭姴鐨犵紓鎾崇摠
            String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + device.getDeviceId();
            redisTemplate.delete(deviceCacheKey);
        }
        
        return result;
    }

    /**
     * 濞夈劌鍞界拋鎯ь槵
     * 
     * @param userId 閻劍鍩汭D
     * @param deviceType 鐠佹儳顦猾璇茬€?
     * @param deviceId 鐠佹儳顦琁D
     * @param deviceName 鐠佹儳顦崥宥囆?
     * @param osVersion 閹垮秳缍旂化鑽ょ埠閻楀牊婀?
     * @param appVersion 鎼存梻鏁ら悧鍫熸拱
     * @param ipAddress IP閸︽澘娼?
     * @param location 娴ｅ秶鐤?
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int registerDevice(Long userId, String deviceType, String deviceId, String deviceName, String osVersion, String appVersion, String ipAddress, String location) {
        String methodName = "registerDevice";
        long startTime = System.currentTimeMillis();

        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceType, "鐠佹儳顦猾璇茬€?, methodName);
            ValidationUtils.validateString(deviceId, "鐠佹儳顦琁D", methodName);

            // 濡偓閺屻儴顔曟径鍥ㄦЦ閸氾箑鍑＄€涙ê婀?
            ImUserDevice existingDevice = imUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
            ImUserDevice device;

            if (existingDevice != null) {
                // 閺囧瓨鏌婇悳鐗堟箒鐠佹儳顦穱鈩冧紖
                device = existingDevice;
                device.setDeviceType(deviceType);
                device.setDeviceName(deviceName);
                device.setOsVersion(osVersion);
                device.setAppVersion(appVersion);
                device.setIpAddress(ipAddress);
                device.setLocation(location);
                device.setUpdateTime(LocalDateTime.now());
                device.setStatus("active");

                int result = imUserDeviceMapper.updateImUserDevice(device);
                if (result > 0) {
                    clearUserDevicesCache(userId);
                    log.debug("閺囧瓨鏌婄拋鎯ь槵娣団剝浼呴幋鎰: userId={}, deviceId={}", userId, deviceId);
                }
                return result;
            } else {
                // 閸掓稑缂撻弬鎷岊啎婢跺洩顔囪ぐ?                device = new ImUserDevice();
                device.setUserId(userId);
                device.setDeviceType(deviceType);
                device.setDeviceId(deviceId);
                device.setDeviceName(deviceName);
                device.setOsVersion(osVersion);
                device.setAppVersion(appVersion);
                device.setIpAddress(ipAddress);
                device.setLocation(location);
                device.setStatus("active");
                device.setCreateTime(LocalDateTime.now());
                device.setUpdateTime(LocalDateTime.now());

                int result = imUserDeviceMapper.insertImUserDevice(device);
                if (result > 0) {
                    clearUserDevicesCache(userId);
                    // 缂傛挸鐡ㄧ拋鎯ь槵娣団剝浼?
                    String deviceCacheKey = DEVICE_ID_CACHE_PREFIX + deviceId;
                    redisTemplate.opsForValue().set(deviceCacheKey, userId, 30, TimeUnit.MINUTES);
                    log.debug("濞夈劌鍞界拋鎯ь槵閹存劕濮? userId={}, deviceId={}", userId, deviceId);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("濞夈劌鍞界拋鎯ь槵瀵倸鐖? userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("濞夈劌鍞界拋鎯ь槵閼版妞? {}ms, userId={}, deviceId={}", duration, userId, deviceId);
        }
    }

    /**
     * 閺囧瓨鏌婄拋鎯ь槵濞叉槒绌弮鍫曟？
     * 
     * @param userId 閻劍鍩汭D
     * @param deviceId 鐠佹儳顦琁D
     * @return 缂佹挻鐏?
     */
    @Override
    public int updateDeviceActiveTime(Long userId, String deviceId) {
        String methodName = "updateDeviceActiveTime";
        long startTime = System.currentTimeMillis();

        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceId, "鐠佹儳顦琁D", methodName);

            // 閺囧瓨鏌婄拋鎯ь槵濞叉槒绌弮鍫曟？
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("deviceId", deviceId);
            params.put("updateTime", LocalDateTime.now());

            int result = imUserDeviceMapper.updateDeviceActiveTime(params);

            if (result > 0) {
                // 瀵倹顒炲〒鍛倞缂傛挸鐡?
                CompletableFuture.runAsync(() -> {
                    clearUserDevicesCache(userId);
                });
            }

            return result;
        } catch (Exception e) {
            log.error("閺囧瓨鏌婄拋鎯ь槵濞叉槒绌弮鍫曟？瀵倸鐖? userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺囧瓨鏌婄拋鎯ь槵濞叉槒绌弮鍫曟？閼版妞? {}ms, userId={}, deviceId={}", duration, userId, deviceId);
        }
    }

    /**
     * 閺囧瓨鏌婄拋鎯ь槵閻樿埖鈧?     * 
     * @param userId 閻劍鍩汭D
     * @param deviceId 鐠佹儳顦琁D
     * @param status 閻樿埖鈧?     * @return 缂佹挻鐏?
     */
    @Override
    public int updateDeviceStatus(Long userId, String deviceId, String status) {
        String methodName = "updateDeviceStatus";
        long startTime = System.currentTimeMillis();

        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateId(userId, methodName);
            ValidationUtils.validateString(deviceId, "鐠佹儳顦琁D", methodName);
            ValidationUtils.validateString(status, "閻樿埖鈧?, methodName);

            // 閺囧瓨鏌婄拋鎯ь槵閻樿埖鈧?            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("deviceId", deviceId);
            params.put("status", status);
            params.put("updateTime", LocalDateTime.now());

            int result = imUserDeviceMapper.updateDeviceStatus(params);

            if (result > 0) {
                // 濞撳懐鎮婇悽銊﹀煕鐠佹儳顦崚妤勩€冪紓鎾崇摠
                clearUserDevicesCache(userId);
                // 婵″倹鐏夐悩鑸碘偓浣稿綁娑撴椽娼ú鏄忕┈閿涘本绔婚悶鍡楃秼閸撳秷顔曟径鍥╃处鐎?
                if ("inactive".equals(status) || "deleted".equals(status)) {
                    String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
                    redisTemplate.delete(currentDeviceCacheKey);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("閺囧瓨鏌婄拋鎯ь槵閻樿埖鈧礁绱撶敮? userId={}, deviceId={}, status={}, error={}", userId, deviceId, status, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺囧瓨鏌婄拋鎯ь槵閻樿埖鈧浇鈧妞? {}ms, userId={}, deviceId={}, status={}", duration, userId, deviceId, status);
        }
    }

    /**
     * 閺嶈宓侀悽銊﹀煕ID閸掔娀娅庨悽銊﹀煕鐠佹儳顦?
     * 
     * @param userId 閻劍鍩汭D
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteImUserDeviceByUserId(Long userId) {
        String methodName = "deleteImUserDeviceByUserId";
        long startTime = System.currentTimeMillis();

        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateId(userId, methodName);

            int result = imUserDeviceMapper.deleteImUserDeviceByUserId(userId);

            if (result > 0) {
                clearUserDevicesCache(userId);
            }

            return result;
        } catch (Exception e) {
            log.error("閸掔娀娅庨悽銊﹀煕鐠佹儳顦鍌氱埗: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閸掔娀娅庨悽銊﹀煕鐠佹儳顦懓妤佹: {}ms, userId={}", duration, userId);
        }
    }

    /**
     * 閺嶈宓侀悽銊﹀煕ID閺屻儴顕楃拋鎯ь槵閸掓銆?
     * 
     * @param userId 閻劍鍩汭D
     * @return 鐠佹儳顦崚妤勩€?
     */
    @Cacheable(value = "im-user-devices", key = "#userId")
    public List<ImUserDevice> selectImUserDeviceByUserId(Long userId) {
        return imUserDeviceMapper.selectImUserDeviceByUserId(userId);
    }

    /**
     * 閺嶈宓侀悽銊﹀煕ID閸滃矁顔曟径鍢擠閺屻儴顕楃拋鎯ь槵
     * 
     * @param userId 閻劍鍩汭D
     * @param deviceId 鐠佹儳顦琁D
     * @return 鐠佹儳顦穱鈩冧紖
     */
    public ImUserDevice selectImUserDeviceByUserIdAndDeviceId(Long userId, String deviceId) {
        return imUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(userId, deviceId);
    }

    /**
     * 閺屻儴顕楅悽銊﹀煕閻ㄥ嫬缍嬮崜宥嗘た鐠哄啳顔曟径?     * 
     * @param userId 閻劍鍩汭D
     * @return 瑜版挸澧犲ú鏄忕┈鐠佹儳顦?
     */
    public ImUserDevice getCurrentActiveDevice(Long userId) {
        String cacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
        ImUserDevice cachedDevice = (ImUserDevice) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedDevice != null) {
            return cachedDevice;
        }
        
        // 閺屻儴顕楅弫鐗堝祦鎼存捁骞忛崣鏍ㄦ付閺傜増妞跨捄鍐啎婢?        ImUserDevice device = imUserDeviceMapper.selectCurrentActiveDevice(userId);
        
        if (device != null) {
            // 缂傛挸鐡ㄧ紒鎾寸亯
            redisTemplate.opsForValue().set(cacheKey, device, 5, TimeUnit.MINUTES);
        }
        
        return device;
    }

    /**
     * 濞撳懐鎮婇悽銊﹀煕鐠佹儳顦紓鎾崇摠
     * 
     * @param userId 閻劍鍩汭D
     */
    private void clearUserDevicesCache(Long userId) {
        if (userId != null) {
            String cacheKey = "im:user:devices:" + userId;
            redisTemplate.delete(cacheKey);
            
            // 濞撳懐鎮婇惄绋垮彠缂傛挸鐡?
            String currentDeviceCacheKey = USER_CURRENT_DEVICE_CACHE_PREFIX + userId;
            redisTemplate.delete(currentDeviceCacheKey);
        }
    }

    // 鐎圭偟骞囬幎鍊熻杽閺傝纭?
    @Override
    protected String getEntityType() {
        return "user_device";
    }

    @Override
    protected Long getEntityId(ImUserDevice entity) {
        return entity.getId();
    }

    @Override
    protected void setCreateTime(ImUserDevice entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }

    @Override
    protected void setUpdateTime(ImUserDevice entity) {
        entity.setUpdateTime(LocalDateTime.now());
    }
}
