package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import com.ruoyi.im.mapper.ImUserMapper;
import com.ruoyi.im.domain.ImUser;
import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.service.ImUserService;
import com.ruoyi.im.utils.ValidationUtils;

/**
 * IM鐢ㄦ埛Service涓氬姟灞傚鐞?- 浼樺寲鐗堟湰
 * 浼樺寲鍐呭锛氭坊鍔犵紦瀛樻満鍒躲€佷簨鍔℃帶鍒躲€佸紓姝ュ鐞嗐€佹€ц兘鐩戞帶銆侀敊璇鐞? * 
 * @author ruoyi
 */
@Service
public class ImUserServiceImpl extends EnhancedBaseServiceImpl<ImUser, ImUserMapper> implements ImUserService {
    private static final Logger log = LoggerFactory.getLogger(ImUserServiceImpl.class);
    
    @Autowired
    private ImUserMapper imUserMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缂撳瓨閿墠缂€
    private static final String USER_CACHE_PREFIX = "im:user:";
    private static final String USERNAME_CACHE_PREFIX = "im:username:";
    private static final String EMAIL_CACHE_PREFIX = "im:email:";
    
    // 缂撳瓨瓒呮椂鏃堕棿锛堝垎閽燂級
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    @PostConstruct
    public void init() {
        log.info("ImUserService鍒濆鍖栧畬鎴愶紝寮€濮嬪惎鐢ㄧ紦瀛樺拰鎬ц兘鐩戞帶");
    }
    
    /**
     * 鏍规嵁鐢ㄦ埛鍚嶆煡璇㈢敤鎴?- 浼樺寲鐗堟湰
     * 浼樺寲鍐呭锛氭坊鍔燫edis缂撳瓨銆佹€ц兘鐩戞帶銆佸紓甯稿鐞?     * 
     * @param username 鐢ㄦ埛鍚?     * @return IM鐢ㄦ埛
     */
    @Override
    @Cacheable(value = "im-users", key = "'username:' + #username", unless = "#result == null")
    public ImUser selectImUserByUsername(String username) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 棣栧厛妫€鏌ョ紦瀛?            String cacheKey = USERNAME_CACHE_PREFIX + username;
            ImUser cachedUser = (ImUser) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                log.debug("浠庣紦瀛樿幏鍙栫敤鎴? {}", username);
                return cachedUser;
            }
            
            // 缂撳瓨鏈懡涓紝鏌ヨ鏁版嵁搴?            log.debug("缂撳瓨鏈懡涓紝鏌ヨ鏁版嵁搴撹幏鍙栫敤鎴? {}", username);
            ImUser user = imUserMapper.selectImUserByUsername(username);
            
            // 灏嗙粨鏋滄斁鍏ョ紦瀛?            if (user != null) {
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                // 鍚屾椂缂撳瓨ID鏄犲皠
                redisTemplate.opsForValue().set(USER_CACHE_PREFIX + user.getId(), user, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("鐢ㄦ埛淇℃伅宸茬紦瀛? {}", username);
            }
            
            return user;
        } catch (Exception e) {
            log.error("鏌ヨ鐢ㄦ埛寮傚父: username={}, error={}", username, e.getMessage(), e);
            throw new BusinessException("鐢ㄦ埛鏌ヨ澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏌ヨ鐢ㄦ埛鑰楁椂: {}ms, username={}", duration, username);
        }
    }
    
    /**
     * 鏍规嵁閭鏌ヨ鐢ㄦ埛
     * 
     * @param email 閭
     * @return IM鐢ㄦ埛
     */
    @Override
    public ImUser selectImUserByEmail(String email) {
        return imUserMapper.selectImUserByEmail(email);
    }
    
    /**
     * 鐢ㄦ埛娉ㄥ唽 - 浼樺寲鐗堟湰
     * 浼樺寲鍐呭锛氫簨鍔℃帶鍒躲€佸弬鏁伴獙璇併€侀噸澶嶆鏌ヤ紭鍖栥€佺紦瀛樺け鏁堛€佸紓姝ュ鐞?     * 
     * @param username 鐢ㄦ埛鍚?     * @param password 瀵嗙爜
     * @param nickname 鏄电О
     * @param email 閭
     * @param phone 鐢佃瘽
     * @return 鐢ㄦ埛ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerUser(String username, String password, String nickname, String email, String phone) {
        long startTime = System.currentTimeMillis();
        log.info("寮€濮嬬敤鎴锋敞鍐? username={}, nickname={}, email={}", username, nickname, email);
        
        try {
            // 鍙傛暟楠岃瘉
            ValidationUtils.validateUsername(username, "registerUser");
            ValidationUtils.validatePassword(password, "registerUser");
            ValidationUtils.validateString(nickname, "鏄电О", "registerUser");
            if (email != null && !email.trim().isEmpty()) {
                ValidationUtils.validateEmail(email, "registerUser");
            }
            if (phone != null && !phone.trim().isEmpty()) {
                ValidationUtils.validatePhone(phone, "registerUser");
            }
            
            // 骞惰妫€鏌ョ敤鎴峰悕鍜岄偖绠辨槸鍚﹀瓨鍦紝鎻愰珮鎬ц兘
            CompletableFuture<ImUser> usernameCheck = CompletableFuture.supplyAsync(() -> {
                return selectImUserByUsername(username);
            });
            
            CompletableFuture<ImUser> emailCheck = CompletableFuture.supplyAsync(() -> {
                if (email != null && !email.trim().isEmpty()) {
                    return selectImUserByEmail(email);
                }
                return null;
            });
            
            // 绛夊緟鎵€鏈夋鏌ュ畬鎴?            CompletableFuture<Void> allChecks = CompletableFuture.allOf(usernameCheck, emailCheck);
            allChecks.get();
            
            ImUser existingUser = usernameCheck.get();
            ImUser emailUser = emailCheck.get();
            
            // 妫€鏌ョ敤鎴峰悕鏄惁宸插瓨鍦?            if (existingUser != null) {
                log.warn("鐢ㄦ埛鍚嶅凡瀛樺湪: {}", username);
                throw new BusinessException("鐢ㄦ埛鍚嶅凡瀛樺湪");
            }
            
            // 妫€鏌ラ偖绠辨槸鍚﹀凡瀛樺湪
            if (emailUser != null) {
                log.warn("閭宸插瓨鍦? {}", email);
                throw new BusinessException("閭宸插瓨鍦?);
            }
            
            // 鍒涘缓鏂扮敤鎴?            ImUser user = new ImUser();
            user.setUsername(username.trim());
            // 浣跨敤BCrypt鍔犲瘑瀵嗙爜
            user.setPassword(passwordEncoder.encode(password));
            user.setNickname(nickname.trim());
            user.setEmail(email != null ? email.trim() : null);
            user.setPhone(phone != null ? phone.trim() : null);
            user.setStatus("ACTIVE");
            user.setAvatar("/profile/avatar.png"); // 榛樿澶村儚
            user.setCreateTime(java.time.LocalDateTime.now());
            
            int result = insert(user);
            if (result <= 0) {
                log.error("鐢ㄦ埛鍒涘缓澶辫触: {}", username);
                throw new BusinessException("鐢ㄦ埛鍒涘缓澶辫触");
            }
            
            // 娓呴櫎鐩稿叧缂撳瓨
            clearUserCache(username, email);
            
            log.info("鐢ㄦ埛娉ㄥ唽鎴愬姛: userId={}, username={}, 鑰楁椂={}ms", user.getId(), username, 
                     System.currentTimeMillis() - startTime);
            
            return user.getId();
            
        } catch (BusinessException e) {
            // 涓氬姟寮傚父涓嶅洖婊氾紝鐩存帴鎶涘嚭
            throw e;
        } catch (Exception e) {
            log.error("鐢ㄦ埛娉ㄥ唽寮傚父: username={}, error={}", username, e.getMessage(), e);
            // 鏍囪浜嬪姟鍥炴粴
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("鐢ㄦ埛娉ㄥ唽澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鐢ㄦ埛娉ㄥ唽鎬昏€楁椂: {}ms, username={}", duration, username);
        }
    }
    

    
    /**
     * 娓呴櫎鐢ㄦ埛鐩稿叧缂撳瓨
     * 
     * @param username 鐢ㄦ埛鍚?     * @param email 閭
     */
    @CacheEvict(value = "im-users", key = "'username:' + #username")
    private void clearUserCache(String username, String email) {
        try {
            // 娓呴櫎鐢ㄦ埛鍚嶇紦瀛?            redisTemplate.delete(USERNAME_CACHE_PREFIX + username);
            
            // 娓呴櫎閭缂撳瓨
            if (email != null) {
                redisTemplate.delete(EMAIL_CACHE_PREFIX + email);
            }
            
            log.debug("宸叉竻闄ょ敤鎴风紦瀛? username={}, email={}", username, email);
        } catch (Exception e) {
            log.warn("娓呴櫎鐢ㄦ埛缂撳瓨澶辫触: username={}, error={}", username, e.getMessage());
        }
    }
    
    /**
     * 鏇存柊鐢ㄦ埛鐘舵€?- 浼樺寲鐗堟湰
     * 浼樺寲鍐呭锛氱紦瀛樻洿鏂般€佷簨鍔℃帶鍒躲€佺姸鎬侀獙璇併€佹€ц兘鐩戞帶
     * 
     * @param userId 鐢ㄦ埛ID
     * @param status 鐘舵€?     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "im-users", allEntries = true)
    public int updateUserStatus(Long userId, String status) {
        long startTime = System.currentTimeMillis();
        log.info("寮€濮嬫洿鏂扮敤鎴风姸鎬? userId={}, status={}", userId, status);
        
        try {
            // 鍙傛暟楠岃瘉
            if (userId == null || userId <= 0) {
                throw new BusinessException("鐢ㄦ埛ID涓嶈兘涓虹┖");
            }
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException("鐘舵€佷笉鑳戒负绌?);
            }
            
            // 楠岃瘉鐘舵€佸€?            if (!isValidStatus(status)) {
                throw new BusinessException("鏃犳晥鐨勭姸鎬佸€? " + status);
            }
            
            ImUser user = selectById(userId);
            if (user == null) {
                log.warn("鐢ㄦ埛涓嶅瓨鍦? userId={}", userId);
                throw new BusinessException("鐢ㄦ埛涓嶅瓨鍦?);
            }
            
            // 妫€鏌ョ姸鎬佹槸鍚﹂渶瑕佹洿鏂?            if (status.equals(user.getStatus())) {
                log.info("鐢ㄦ埛鐘舵€佹棤闇€鏇存柊: userId={}, status={}", userId, status);
                return 0;
            }
            
            user.setStatus(status);
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            int result = updateById(user);
            if (result <= 0) {
                log.error("鐢ㄦ埛鐘舵€佹洿鏂板け璐? userId={}, status={}", userId, status);
                throw new BusinessException("鐢ㄦ埛鐘舵€佹洿鏂板け璐?);
            }
            
            // 娓呴櫎鐩稿叧缂撳瓨
            clearUserCacheById(userId);
            
            log.info("鐢ㄦ埛鐘舵€佹洿鏂版垚鍔? userId={}, oldStatus={}, newStatus={}, 鑰楁椂={}ms", 
                     userId, user.getStatus(), status, System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("鏇存柊鐢ㄦ埛鐘舵€佸紓甯? userId={}, status={}, error={}", userId, status, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("鐢ㄦ埛鐘舵€佹洿鏂板け璐?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏇存柊鐢ㄦ埛鐘舵€佹€昏€楁椂: {}ms, userId={}, status={}", duration, userId, status);
        }
    }
    
    /**
     * 楠岃瘉鐘舵€佸€兼槸鍚︽湁鏁?     * 
     * @param status 鐘舵€佸€?     * @return 鏄惁鏈夋晥
     */
    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || 
               "INACTIVE".equals(status) || 
               "BANNED".equals(status) || 
               "OFFLINE".equals(status);
    }
    
    /**
     * 鏍规嵁ID娓呴櫎鐢ㄦ埛缂撳瓨
     * 
     * @param userId 鐢ㄦ埛ID
     */
    private void clearUserCacheById(Long userId) {
        try {
            // 娓呴櫎ID缂撳瓨
            redisTemplate.delete(USER_CACHE_PREFIX + userId);
            
            // 娓呴櫎鐢ㄦ埛鍚嶇紦瀛橈紙闇€瑕佸厛鏌ヨ鐢ㄦ埛鍚嶏級
            ImUser user = selectById(userId);
            if (user != null) {
                redisTemplate.delete(USERNAME_CACHE_PREFIX + user.getUsername());
                if (user.getEmail() != null) {
                    redisTemplate.delete(EMAIL_CACHE_PREFIX + user.getEmail());
                }
            }
            
            log.debug("宸叉竻闄ょ敤鎴风紦瀛? userId={}", userId);
        } catch (Exception e) {
            log.warn("娓呴櫎鐢ㄦ埛缂撳瓨澶辫触: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 鏇存柊鐢ㄦ埛澶村儚 - 浼樺寲鐗堟湰
     * 浼樺寲鍐呭锛氱紦瀛樻洿鏂般€佷簨鍔℃帶鍒躲€佸弬鏁伴獙璇併€佹€ц兘鐩戞帶
     * 
     * @param userId 鐢ㄦ埛ID
     * @param avatar 澶村儚URL
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "im-users", key = "'userId:' + #userId")
    public int updateUserAvatar(Long userId, String avatar) {
        long startTime = System.currentTimeMillis();
        log.info("寮€濮嬫洿鏂扮敤鎴峰ご鍍? userId={}, avatar={}", userId, avatar);
        
        try {
            // 鍙傛暟楠岃瘉
            if (userId == null || userId <= 0) {
                throw new BusinessException("鐢ㄦ埛ID涓嶈兘涓虹┖");
            }
            if (avatar == null || avatar.trim().isEmpty()) {
                throw new BusinessException("澶村儚URL涓嶈兘涓虹┖");
            }
            if (avatar.length() > 500) {
                throw new BusinessException("澶村儚URL闀垮害涓嶈兘瓒呰繃500涓瓧绗?);
            }
            
            ImUser user = selectById(userId);
            if (user == null) {
                log.warn("鐢ㄦ埛涓嶅瓨鍦? userId={}", userId);
                throw new BusinessException("鐢ㄦ埛涓嶅瓨鍦?);
            }
            
            // 妫€鏌ュご鍍忔槸鍚﹂渶瑕佹洿鏂?            if (avatar.equals(user.getAvatar())) {
                log.info("鐢ㄦ埛澶村儚鏃犻渶鏇存柊: userId={}, avatar={}", userId, avatar);
                return 0;
            }
            
            String oldAvatar = user.getAvatar();
            user.setAvatar(avatar.trim());
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            int result = updateById(user);
            if (result <= 0) {
                log.error("鐢ㄦ埛澶村儚鏇存柊澶辫触: userId={}, avatar={}", userId, avatar);
                throw new BusinessException("鐢ㄦ埛澶村儚鏇存柊澶辫触");
            }
            
            // 娓呴櫎鐩稿叧缂撳瓨
            clearUserCacheById(userId);
            
            log.info("鐢ㄦ埛澶村儚鏇存柊鎴愬姛: userId={}, oldAvatar={}, newAvatar={}, 鑰楁椂={}ms", 
                     userId, oldAvatar, avatar, System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("鏇存柊鐢ㄦ埛澶村儚寮傚父: userId={}, avatar={}, error={}", userId, avatar, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("鐢ㄦ埛澶村儚鏇存柊澶辫触", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("鏇存柊鐢ㄦ埛澶村儚鎬昏€楁椂: {}ms, userId={}, avatar={}", duration, userId, avatar);
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @return 瀹炰綋绫诲瀷鍚嶇О
     */
    @Override
    protected String getEntityType() {
        return "user";
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 鐢ㄦ埛瀹炰綋
     * @return 鐢ㄦ埛ID
     */
    @Override
    protected Long getEntityId(ImUser entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 鐢ㄦ埛瀹炰綋
     */
    @Override
    protected void setCreateTime(ImUser entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl鐨勬娊璞℃柟娉?     * 
     * @param entity 鐢ㄦ埛瀹炰綋
     */
    @Override
    protected void setUpdateTime(ImUser entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 瀹炵幇EnhancedBaseServiceImpl涓殑clearRelatedCache鏂规硶锛屾彁渚涚敤鎴风壒瀹氱紦瀛樻竻鐞嗛€昏緫
     * 
     * @param entity 鐢ㄦ埛瀹炰綋
     */
    @Override
    protected void clearRelatedCache(ImUser entity) {
        if (entity != null) {
            // 娓呴櫎ID缂撳瓨
            clearEntityCache(entity.getId());
            
            // 娓呴櫎鐢ㄦ埛鍚嶇紦瀛?            if (entity.getUsername() != null) {
                redisTemplate.delete(USERNAME_CACHE_PREFIX + entity.getUsername());
            }
            
            // 娓呴櫎閭缂撳瓨
            if (entity.getEmail() != null) {
                redisTemplate.delete(EMAIL_CACHE_PREFIX + entity.getEmail());
            }
        }
    }
    
    /**
     * 閲嶅啓澧炲己鐗堟柟娉曪紝娣诲姞鐢ㄦ埛鐗瑰畾鐨勯€昏緫
     * 
     * @param id 鐢ㄦ埛ID
     * @return 鐢ㄦ埛瀹炰綋
     */
    @Override
    public ImUser selectById(Long id) {
        ImUser user = super.selectById(id);
        if (user != null) {
            // 棰濆缂撳瓨鐢ㄦ埛鍚嶅拰閭鏄犲皠
            if (user.getUsername() != null) {
                redisTemplate.opsForValue().set(USERNAME_CACHE_PREFIX + user.getUsername(), user, 
                    CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            }
            if (user.getEmail() != null) {
                redisTemplate.opsForValue().set(EMAIL_CACHE_PREFIX + user.getEmail(), user, 
                    CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            }
        }
        return user;
    }
    
    /**
     * 閲嶅啓澧炲己鐗堟柟娉曪紝娣诲姞鐢ㄦ埛鐗瑰畾閫昏緫
     * 
     * @param entity 鐢ㄦ埛瀹炰綋
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ImUser entity) {
        // 妫€鏌ョ敤鎴峰悕鏄惁鍙樻洿
        boolean usernameChanged = false;
        boolean emailChanged = false;
        
        if (entity.getId() != null) {
            ImUser oldUser = super.selectById(entity.getId());
            if (oldUser != null) {
                usernameChanged = !Objects.equals(oldUser.getUsername(), entity.getUsername());
                emailChanged = !Objects.equals(oldUser.getEmail(), entity.getEmail());
                
                // 濡傛灉鐢ㄦ埛鍚嶅彉鏇达紝娓呴櫎鏃х殑缂撳瓨
                if (usernameChanged && oldUser.getUsername() != null) {
                    redisTemplate.delete(USERNAME_CACHE_PREFIX + oldUser.getUsername());
                }
                
                // 濡傛灉閭鍙樻洿锛屾竻闄ゆ棫鐨勭紦瀛?                if (emailChanged && oldUser.getEmail() != null) {
                    redisTemplate.delete(EMAIL_CACHE_PREFIX + oldUser.getEmail());
                }
            }
        }
        
        // 璋冪敤鐖剁被鏇存柊鏂规硶
        int result = super.update(entity);
        
        // 濡傛灉鐢ㄦ埛鍚嶆垨閭鍙樻洿锛屾竻闄ょ浉鍏崇紦瀛?        if (result > 0 && (usernameChanged || emailChanged)) {
            clearRelatedCache(entity);
        }
        
        return result;
    }
    
    /**
     * 閲嶅啓澧炲己鐗堟柟娉曪紝娣诲姞鐢ㄦ埛鐗瑰畾閫昏緫
     * 
     * @param id 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        // 鍏堟煡璇㈢敤鎴凤紙鐢ㄤ簬娓呴櫎缂撳瓨锛?        ImUser user = selectById(id);
        
        // 璋冪敤鐖剁被鍒犻櫎鏂规硶
        int result = super.deleteById(id);
        
        // 濡傛灉鍒犻櫎鎴愬姛锛屾竻闄ょ浉鍏崇紦瀛?        if (result > 0 && user != null) {
            clearRelatedCache(user);
        }
        
        return result;
    }
    
    /**
     * 鏇存柊鐢ㄦ埛淇℃伅
     * 
     * @param userId 鐢ㄦ埛ID
     * @param nickname 鏄电О
     * @param email 閭
     * @param phone 鐢佃瘽
     * @return 缁撴灉
     */
    @Override
    public int updateUserInfo(Long userId, String nickname, String email, String phone) {
        ImUser user = selectById(userId);
        if (user != null) {
            if (nickname != null) user.setNickname(nickname);
            if (email != null) user.setEmail(email);
            if (phone != null) user.setPhone(phone);
            return update(user);
        }
        return 0;
    }
    
    /**
     * 鑾峰彇鐢ㄦ埛鏉冮檺鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 鏉冮檺闆嗗悎
     */
    @Override
    public Set<String> getUserPermissions(Long userId) {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("鑾峰彇鐢ㄦ埛鏉冮檺: userId={}", userId);
            
            // 缂撳瓨閿?            String cacheKey = "im:user:permissions:" + userId;
            
            // 鍏堟鏌ョ紦瀛?            Set<String> cachedPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedPermissions != null) {
                log.debug("浠庣紦瀛樿幏鍙栫敤鎴锋潈闄? userId={}, permissionCount={}", userId, cachedPermissions.size());
                return cachedPermissions;
            }
            
            // 缂撳瓨鏈懡涓紝浠庢暟鎹簱鑾峰彇
            Set<String> permissions = imUserMapper.selectUserPermissionsByUserId(userId);
            if (permissions == null) {
                permissions = new HashSet<>();
            }
            
            // 缂撳瓨缁撴灉
            redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            log.debug("鑾峰彇鐢ㄦ埛鏉冮檺鎴愬姛: userId={}, permissionCount={}, 鑰楁椂={}ms", 
                    userId, permissions.size(), System.currentTimeMillis() - startTime);
            
            return permissions;
            
        } catch (Exception e) {
            log.error("鑾峰彇鐢ㄦ埛鏉冮檺寮傚父: userId={}, error={}", userId, e.getMessage(), e);
            return new HashSet<>();
        }
    }
    
    /**
     * 鑾峰彇鐢ㄦ埛瑙掕壊鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 瑙掕壊闆嗗悎
     */
    @Override
    public Set<String> getUserRoles(Long userId) {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("鑾峰彇鐢ㄦ埛瑙掕壊: userId={}", userId);
            
            // 缂撳瓨閿?            String cacheKey = "im:user:roles:" + userId;
            
            // 鍏堟鏌ョ紦瀛?            Set<String> cachedRoles = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRoles != null) {
                log.debug("浠庣紦瀛樿幏鍙栫敤鎴疯鑹? userId={}, roleCount={}", userId, cachedRoles.size());
                return cachedRoles;
            }
            
            // 缂撳瓨鏈懡涓紝浠庢暟鎹簱鑾峰彇
            Set<String> roles = imUserMapper.selectUserRolesByUserId(userId);
            if (roles == null) {
                roles = new HashSet<>();
            }
            
            // 缂撳瓨缁撴灉
            redisTemplate.opsForValue().set(cacheKey, roles, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            log.debug("鑾峰彇鐢ㄦ埛瑙掕壊鎴愬姛: userId={}, roleCount={}, 鑰楁椂={}ms", 
                    userId, roles.size(), System.currentTimeMillis() - startTime);
            
            return roles;
            
        } catch (Exception e) {
            log.error("鑾峰彇鐢ㄦ埛瑙掕壊寮傚父: userId={}, error={}", userId, e.getMessage(), e);
            return new HashSet<>();
        }
    }
}
