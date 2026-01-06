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
 * IM閻劍鍩汼ervice娑撴艾濮熺仦鍌氼槱閻?- 娴兼ê瀵查悧鍫熸拱
 * 娴兼ê瀵查崘鍛啇閿涙碍鍧婇崝鐘电处鐎涙ɑ婧€閸掕翰鈧椒绨ㄩ崝鈩冨付閸掕翰鈧礁绱撳銉ヮ槱閻炲棎鈧焦鈧嗗厴閻╂垶甯堕妴渚€鏁婄拠顖氼槱閻? * 
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
    
    // 缂傛挸鐡ㄩ柨顔煎缂傗偓
    private static final String USER_CACHE_PREFIX = "im:user:";
    private static final String USERNAME_CACHE_PREFIX = "im:username:";
    private static final String EMAIL_CACHE_PREFIX = "im:email:";
    
    // 缂傛挸鐡ㄧ搾鍛閺冨爼妫块敍鍫濆瀻闁界噦绱?
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    @PostConstruct
    public void init() {
        log.info("ImUserService閸掓繂顫愰崠鏍х暚閹存劧绱濆鈧慨瀣儙閻劎绱︾€涙ê鎷伴幀褑鍏橀惄鎴炲付");
    }
    
    /**
     * 閺嶈宓侀悽銊﹀煕閸氬秵鐓＄拠銏㈡暏閹?- 娴兼ê瀵查悧鍫熸拱
     * 娴兼ê瀵查崘鍛啇閿涙碍鍧婇崝鐕玡dis缂傛挸鐡ㄩ妴浣光偓褑鍏橀惄鎴炲付閵嗕礁绱撶敮绋款槱閻?     * 
     * @param username 閻劍鍩涢崥?     * @return IM閻劍鍩?
     */
    @Override
    @Cacheable(value = "im-users", key = "'username:' + #username", unless = "#result == null")
    public ImUser selectImUserByUsername(String username) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 妫ｆ牕鍘涘Λ鈧?弻銉х处鐎?
            String cacheKey = USERNAME_CACHE_PREFIX + username;
            ImUser cachedUser = (ImUser) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍暏閹? {}", username);
                return cachedUser;
            }
            
            // 缂傛挸鐡ㄩ張顏勬嚒娑擃叏绱濋弻銉嚄閺佺増宓佹惔?            log.debug("缂傛挸鐡ㄩ張顏勬嚒娑擃叏绱濋弻銉嚄閺佺増宓佹惔鎾瑰箯閸欐牜鏁ら幋? {}", username);
            ImUser user = imUserMapper.selectImUserByUsername(username);
            
            // 鐏忓棛绮ㄩ弸婊勬杹閸忋儳绱︾€?            if (user != null) {
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                // 閸氬本妞傜紓鎾崇摠ID閺勭姴鐨?
                redisTemplate.opsForValue().set(USER_CACHE_PREFIX + user.getId(), user, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("閻劍鍩涙穱鈩冧紖瀹歌尙绱︾€? {}", username);
            }
            
            return user;
        } catch (Exception e) {
            log.error("閺屻儴顕楅悽銊﹀煕瀵倸鐖? username={}, error={}", username, e.getMessage(), e);
            throw new BusinessException("閻劍鍩涢弻銉嚄婢惰精瑙?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺屻儴顕楅悽銊﹀煕閼版妞? {}ms, username={}", duration, username);
        }
    }
    
    /**
     * 閺嶈宓侀柇顔绢唸閺屻儴顕楅悽銊﹀煕
     * 
     * @param email 闁喚顔?
     * @return IM閻劍鍩?
     */
    @Override
    public ImUser selectImUserByEmail(String email) {
        return imUserMapper.selectImUserByEmail(email);
    }
    
    /**
     * 閻劍鍩涘▔銊ュ斀 - 娴兼ê瀵查悧鍫熸拱
     * 娴兼ê瀵查崘鍛啇閿涙矮绨ㄩ崝鈩冨付閸掕翰鈧礁寮弫浼寸崣鐠囦降鈧線鍣告径宥嗩梾閺屻儰绱崠鏍モ偓浣虹处鐎涙ê銇戦弫鍫涒偓浣哥磽濮濄儱顦╅悶?     * 
     * @param username 閻劍鍩涢崥?     * @param password 鐎靛棛鐖?
     * @param nickname 閺勭數袨
     * @param email 闁喚顔?
     * @param phone 閻絻鐦?
     * @return 閻劍鍩汭D
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerUser(String username, String password, String nickname, String email, String phone) {
        long startTime = System.currentTimeMillis();
        log.info("瀵偓婵鏁ら幋閿嬫暈閸? username={}, nickname={}, email={}", username, nickname, email);
        
        try {
            // 閸欏倹鏆熸宀冪槈
            ValidationUtils.validateUsername(username, "registerUser");
            ValidationUtils.validatePassword(password, "registerUser");
            ValidationUtils.validateString(nickname, "閺勭數袨", "registerUser");
            if (email != null && !email.trim().isEmpty()) {
                ValidationUtils.validateEmail(email, "registerUser");
            }
            if (phone != null && !phone.trim().isEmpty()) {
                ValidationUtils.validatePhone(phone, "registerUser");
            }
            
            // 楠炴儼顢戝Λ鈧弻銉ф暏閹村嘲鎮曢崪宀勫仏缁犺鲸妲搁崥锕€鐡ㄩ崷顭掔礉閹绘劙鐝幀褑鍏?
            CompletableFuture<ImUser> usernameCheck = CompletableFuture.supplyAsync(() -> {
                return selectImUserByUsername(username);
            });
            
            CompletableFuture<ImUser> emailCheck = CompletableFuture.supplyAsync(() -> {
                if (email != null && !email.trim().isEmpty()) {
                    return selectImUserByEmail(email);
                }
                return null;
            });
            
            // 缁涘绶熼幍鈧張澶嬵梾閺屻儱鐣幋?            CompletableFuture<Void> allChecks = CompletableFuture.allOf(usernameCheck, emailCheck);
            allChecks.get();
            
            ImUser existingUser = usernameCheck.get();
            ImUser emailUser = emailCheck.get();
            
            // 濡偓閺屻儳鏁ら幋宄版倳閺勵垰鎯佸鎻掔摠閸?            if (existingUser != null) {
                log.warn("閻劍鍩涢崥宥呭嚒鐎涙ê婀? {}", username);
                throw new BusinessException("閻劍鍩涢崥宥呭嚒鐎涙ê婀?);
            }
            
            // 濡偓閺屻儵鍋栫粻杈ㄦЦ閸氾箑鍑＄€涙ê婀?
            if (emailUser != null) {
                log.warn("闁喚顔堝鎻掔摠閸? {}", email);
                throw new BusinessException("闁喚顔堝鎻掔摠閸?);
            }
            
            // 閸掓稑缂撻弬鎵暏閹?            ImUser user = new ImUser();
            user.setUsername(username.trim());
            // 娴ｈ法鏁Crypt閸旂姴鐦戠€靛棛鐖?
            user.setPassword(passwordEncoder.encode(password));
            user.setNickname(nickname.trim());
            user.setEmail(email != null ? email.trim() : null);
            user.setPhone(phone != null ? phone.trim() : null);
            user.setStatus("ACTIVE");
            user.setAvatar("/profile/avatar.png"); // 姒涙顓绘径鏉戝剼
            user.setCreateTime(java.time.LocalDateTime.now());
            
            int result = insert(user);
            if (result <= 0) {
                log.error("閻劍鍩涢崚娑樼紦婢惰精瑙? {}", username);
                throw new BusinessException("閻劍鍩涢崚娑樼紦婢惰精瑙?);
            }
            
            // 濞撳懘娅庨惄绋垮彠缂傛挸鐡?
            clearUserCache(username, email);
            
            log.info("閻劍鍩涘▔銊ュ斀閹存劕濮? userId={}, username={}, 閼版妞?{}ms", user.getId(), username, 
                     System.currentTimeMillis() - startTime);
            
            return user.getId();
            
        } catch (BusinessException e) {
            // 娑撴艾濮熷鍌氱埗娑撳秴娲栧姘剧礉閻╁瓨甯撮幎娑樺毉
            throw e;
        } catch (Exception e) {
            log.error("閻劍鍩涘▔銊ュ斀瀵倸鐖? username={}, error={}", username, e.getMessage(), e);
            // 閺嶅洩顔囨禍瀣閸ョ偞绮?
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("閻劍鍩涘▔銊ュ斀婢惰精瑙?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閻劍鍩涘▔銊ュ斀閹槒鈧妞? {}ms, username={}", duration, username);
        }
    }
    

    
    /**
     * 濞撳懘娅庨悽銊﹀煕閻╃鍙х紓鎾崇摠
     * 
     * @param username 閻劍鍩涢崥?     * @param email 闁喚顔?
     */
    @CacheEvict(value = "im-users", key = "'username:' + #username")
    private void clearUserCache(String username, String email) {
        try {
            // 濞撳懘娅庨悽銊﹀煕閸氬秶绱︾€?            redisTemplate.delete(USERNAME_CACHE_PREFIX + username);
            
            // 濞撳懘娅庨柇顔绢唸缂傛挸鐡?
            if (email != null) {
                redisTemplate.delete(EMAIL_CACHE_PREFIX + email);
            }
            
            log.debug("瀹稿弶绔婚梽銈囨暏閹撮绱︾€? username={}, email={}", username, email);
        } catch (Exception e) {
            log.warn("濞撳懘娅庨悽銊﹀煕缂傛挸鐡ㄦ径杈Е: username={}, error={}", username, e.getMessage());
        }
    }
    
    /**
     * 閺囧瓨鏌婇悽銊﹀煕閻樿埖鈧?- 娴兼ê瀵查悧鍫熸拱
     * 娴兼ê瀵查崘鍛啇閿涙氨绱︾€涙ɑ娲块弬鑸偓浣风皑閸斺剝甯堕崚韬测偓浣哄Ц閹線鐛欑拠浣碘偓浣光偓褑鍏橀惄鎴炲付
     * 
     * @param userId 閻劍鍩汭D
     * @param status 閻樿埖鈧?     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "im-users", allEntries = true)
    public int updateUserStatus(Long userId, String status) {
        long startTime = System.currentTimeMillis();
        log.info("瀵偓婵娲块弬鎵暏閹撮濮搁幀? userId={}, status={}", userId, status);
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (userId == null || userId <= 0) {
                throw new BusinessException("閻劍鍩汭D娑撳秷鍏樻稉铏光敄");
            }
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException("閻樿埖鈧椒绗夐懗鎴掕礋缁?);
            }
            
            // 妤犲矁鐦夐悩鑸碘偓浣糕偓?            if (!isValidStatus(status)) {
                throw new BusinessException("閺冪姵鏅ラ惃鍕Ц閹礁鈧? " + status);
            }
            
            ImUser user = selectById(userId);
            if (user == null) {
                log.warn("閻劍鍩涙稉宥呯摠閸? userId={}", userId);
                throw new BusinessException("閻劍鍩涙稉宥呯摠閸?);
            }
            
            // 濡偓閺屻儳濮搁幀浣规Ц閸氾箓娓剁憰浣规纯閺?            if (status.equals(user.getStatus())) {
                log.info("閻劍鍩涢悩鑸碘偓浣规￥闂団偓閺囧瓨鏌? userId={}, status={}", userId, status);
                return 0;
            }
            
            user.setStatus(status);
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            int result = updateById(user);
            if (result <= 0) {
                log.error("閻劍鍩涢悩鑸碘偓浣规纯閺傛澘銇戠拹? userId={}, status={}", userId, status);
                throw new BusinessException("閻劍鍩涢悩鑸碘偓浣规纯閺傛澘銇戠拹?);
            }
            
            // 濞撳懘娅庨惄绋垮彠缂傛挸鐡?
            clearUserCacheById(userId);
            
            log.info("閻劍鍩涢悩鑸碘偓浣规纯閺傜増鍨氶崝? userId={}, oldStatus={}, newStatus={}, 閼版妞?{}ms", 
                     userId, user.getStatus(), status, System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("閺囧瓨鏌婇悽銊﹀煕閻樿埖鈧礁绱撶敮? userId={}, status={}, error={}", userId, status, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("閻劍鍩涢悩鑸碘偓浣规纯閺傛澘銇戠拹?, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺囧瓨鏌婇悽銊﹀煕閻樿埖鈧焦鈧槒鈧妞? {}ms, userId={}, status={}", duration, userId, status);
        }
    }
    
    /**
     * 妤犲矁鐦夐悩鑸碘偓浣糕偓鍏兼Ц閸氾附婀侀弫?     * 
     * @param status 閻樿埖鈧礁鈧?     * @return 閺勵垰鎯侀張澶嬫櫏
     */
    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || 
               "INACTIVE".equals(status) || 
               "BANNED".equals(status) || 
               "OFFLINE".equals(status);
    }
    
    /**
     * 閺嶈宓両D濞撳懘娅庨悽銊﹀煕缂傛挸鐡?
     * 
     * @param userId 閻劍鍩汭D
     */
    private void clearUserCacheById(Long userId) {
        try {
            // 濞撳懘娅嶪D缂傛挸鐡?
            redisTemplate.delete(USER_CACHE_PREFIX + userId);
            
            // 濞撳懘娅庨悽銊﹀煕閸氬秶绱︾€涙﹫绱欓棁鈧憰浣稿帥閺屻儴顕楅悽銊﹀煕閸氬稄绱?
            ImUser user = selectById(userId);
            if (user != null) {
                redisTemplate.delete(USERNAME_CACHE_PREFIX + user.getUsername());
                if (user.getEmail() != null) {
                    redisTemplate.delete(EMAIL_CACHE_PREFIX + user.getEmail());
                }
            }
            
            log.debug("瀹稿弶绔婚梽銈囨暏閹撮绱︾€? userId={}", userId);
        } catch (Exception e) {
            log.warn("濞撳懘娅庨悽銊﹀煕缂傛挸鐡ㄦ径杈Е: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 閺囧瓨鏌婇悽銊﹀煕婢舵潙鍎?- 娴兼ê瀵查悧鍫熸拱
     * 娴兼ê瀵查崘鍛啇閿涙氨绱︾€涙ɑ娲块弬鑸偓浣风皑閸斺剝甯堕崚韬测偓浣稿棘閺佷即鐛欑拠浣碘偓浣光偓褑鍏橀惄鎴炲付
     * 
     * @param userId 閻劍鍩汭D
     * @param avatar 婢舵潙鍎歎RL
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "im-users", key = "'userId:' + #userId")
    public int updateUserAvatar(Long userId, String avatar) {
        long startTime = System.currentTimeMillis();
        log.info("瀵偓婵娲块弬鎵暏閹村嘲銇旈崓? userId={}, avatar={}", userId, avatar);
        
        try {
            // 閸欏倹鏆熸宀冪槈
            if (userId == null || userId <= 0) {
                throw new BusinessException("閻劍鍩汭D娑撳秷鍏樻稉铏光敄");
            }
            if (avatar == null || avatar.trim().isEmpty()) {
                throw new BusinessException("婢舵潙鍎歎RL娑撳秷鍏樻稉铏光敄");
            }
            if (avatar.length() > 500) {
                throw new BusinessException("婢舵潙鍎歎RL闂€鍨娑撳秷鍏樼搾鍛扮箖500娑擃亜鐡х粭?);
            }
            
            ImUser user = selectById(userId);
            if (user == null) {
                log.warn("閻劍鍩涙稉宥呯摠閸? userId={}", userId);
                throw new BusinessException("閻劍鍩涙稉宥呯摠閸?);
            }
            
            // 濡偓閺屻儱銇旈崓蹇旀Ц閸氾箓娓剁憰浣规纯閺?            if (avatar.equals(user.getAvatar())) {
                log.info("閻劍鍩涙径鏉戝剼閺冪娀娓堕弴瀛樻煀: userId={}, avatar={}", userId, avatar);
                return 0;
            }
            
            String oldAvatar = user.getAvatar();
            user.setAvatar(avatar.trim());
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            int result = updateById(user);
            if (result <= 0) {
                log.error("閻劍鍩涙径鏉戝剼閺囧瓨鏌婃径杈Е: userId={}, avatar={}", userId, avatar);
                throw new BusinessException("閻劍鍩涙径鏉戝剼閺囧瓨鏌婃径杈Е");
            }
            
            // 濞撳懘娅庨惄绋垮彠缂傛挸鐡?
            clearUserCacheById(userId);
            
            log.info("閻劍鍩涙径鏉戝剼閺囧瓨鏌婇幋鎰: userId={}, oldAvatar={}, newAvatar={}, 閼版妞?{}ms", 
                     userId, oldAvatar, avatar, System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("閺囧瓨鏌婇悽銊﹀煕婢舵潙鍎氬鍌氱埗: userId={}, avatar={}, error={}", userId, avatar, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("閻劍鍩涙径鏉戝剼閺囧瓨鏌婃径杈Е", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("閺囧瓨鏌婇悽銊﹀煕婢舵潙鍎氶幀鏄忊偓妤佹: {}ms, userId={}, avatar={}", duration, userId, avatar);
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @return 鐎圭偘缍嬬猾璇茬€烽崥宥囆?
     */
    @Override
    protected String getEntityType() {
        return "user";
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 閻劍鍩涚€圭偘缍?
     * @return 閻劍鍩汭D
     */
    @Override
    protected Long getEntityId(ImUser entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 閻劍鍩涚€圭偘缍?
     */
    @Override
    protected void setCreateTime(ImUser entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl閻ㄥ嫭濞婄挒鈩冩煙濞?     * 
     * @param entity 閻劍鍩涚€圭偘缍?
     */
    @Override
    protected void setUpdateTime(ImUser entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 鐎圭偟骞嘐nhancedBaseServiceImpl娑擃厾娈慶learRelatedCache閺傝纭堕敍灞惧絹娓氭稓鏁ら幋椋庡鐎规氨绱︾€涙ɑ绔婚悶鍡涒偓鏄忕帆
     * 
     * @param entity 閻劍鍩涚€圭偘缍?
     */
    @Override
    protected void clearRelatedCache(ImUser entity) {
        if (entity != null) {
            // 濞撳懘娅嶪D缂傛挸鐡?
            clearEntityCache(entity.getId());
            
            // 濞撳懘娅庨悽銊﹀煕閸氬秶绱︾€?            if (entity.getUsername() != null) {
                redisTemplate.delete(USERNAME_CACHE_PREFIX + entity.getUsername());
            }
            
            // 濞撳懘娅庨柇顔绢唸缂傛挸鐡?
            if (entity.getEmail() != null) {
                redisTemplate.delete(EMAIL_CACHE_PREFIX + entity.getEmail());
            }
        }
    }
    
    /**
     * 闁插秴鍟撴晶鐐插繁閻楀牊鏌熷▔鏇礉濞ｈ濮為悽銊﹀煕閻楃懓鐣鹃惃鍕偓鏄忕帆
     * 
     * @param id 閻劍鍩汭D
     * @return 閻劍鍩涚€圭偘缍?
     */
    @Override
    public ImUser selectById(Long id) {
        ImUser user = super.selectById(id);
        if (user != null) {
            // 妫版繂顦荤紓鎾崇摠閻劍鍩涢崥宥呮嫲闁喚顔堥弰鐘茬殸
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
     * 闁插秴鍟撴晶鐐插繁閻楀牊鏌熷▔鏇礉濞ｈ濮為悽銊﹀煕閻楃懓鐣鹃柅鏄忕帆
     * 
     * @param entity 閻劍鍩涚€圭偘缍?
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ImUser entity) {
        // 濡偓閺屻儳鏁ら幋宄版倳閺勵垰鎯侀崣妯绘纯
        boolean usernameChanged = false;
        boolean emailChanged = false;
        
        if (entity.getId() != null) {
            ImUser oldUser = super.selectById(entity.getId());
            if (oldUser != null) {
                usernameChanged = !Objects.equals(oldUser.getUsername(), entity.getUsername());
                emailChanged = !Objects.equals(oldUser.getEmail(), entity.getEmail());
                
                // 婵″倹鐏夐悽銊﹀煕閸氬秴褰夐弴杈剧礉濞撳懘娅庨弮褏娈戠紓鎾崇摠
                if (usernameChanged && oldUser.getUsername() != null) {
                    redisTemplate.delete(USERNAME_CACHE_PREFIX + oldUser.getUsername());
                }
                
                // 婵″倹鐏夐柇顔绢唸閸欐ɑ娲块敍灞剧闂勩倖妫惃鍕处鐎?                if (emailChanged && oldUser.getEmail() != null) {
                    redisTemplate.delete(EMAIL_CACHE_PREFIX + oldUser.getEmail());
                }
            }
        }
        
        // 鐠嬪啰鏁ら悥鍓佽閺囧瓨鏌婇弬瑙勭《
        int result = super.update(entity);
        
        // 婵″倹鐏夐悽銊﹀煕閸氬秵鍨ㄩ柇顔绢唸閸欐ɑ娲块敍灞剧闂勩倗娴夐崗宕囩处鐎?        if (result > 0 && (usernameChanged || emailChanged)) {
            clearRelatedCache(entity);
        }
        
        return result;
    }
    
    /**
     * 闁插秴鍟撴晶鐐插繁閻楀牊鏌熷▔鏇礉濞ｈ濮為悽銊﹀煕閻楃懓鐣鹃柅鏄忕帆
     * 
     * @param id 閻劍鍩汭D
     * @return 缂佹挻鐏?
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        // 閸忓牊鐓＄拠銏㈡暏閹村嚖绱欓悽銊ょ艾濞撳懘娅庣紓鎾崇摠閿?        ImUser user = selectById(id);
        
        // 鐠嬪啰鏁ら悥鍓佽閸掔娀娅庨弬瑙勭《
        int result = super.deleteById(id);
        
        // 婵″倹鐏夐崚鐘绘珟閹存劕濮涢敍灞剧闂勩倗娴夐崗宕囩处鐎?        if (result > 0 && user != null) {
            clearRelatedCache(user);
        }
        
        return result;
    }
    
    /**
     * 閺囧瓨鏌婇悽銊﹀煕娣団剝浼?
     * 
     * @param userId 閻劍鍩汭D
     * @param nickname 閺勭數袨
     * @param email 闁喚顔?
     * @param phone 閻絻鐦?
     * @return 缂佹挻鐏?
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
     * 閼惧嘲褰囬悽銊﹀煕閺夊啴妾洪崚妤勩€?
     * 
     * @param userId 閻劍鍩汭D
     * @return 閺夊啴妾洪梿鍡楁値
     */
    @Override
    public Set<String> getUserPermissions(Long userId) {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("閼惧嘲褰囬悽銊﹀煕閺夊啴妾? userId={}", userId);
            
            // 缂傛挸鐡ㄩ柨?            String cacheKey = "im:user:permissions:" + userId;
            
            // 閸忓牊顥呴弻銉х处鐎?            Set<String> cachedPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedPermissions != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍暏閹撮攱娼堥梽? userId={}, permissionCount={}", userId, cachedPermissions.size());
                return cachedPermissions;
            }
            
            // 缂傛挸鐡ㄩ張顏勬嚒娑擃叏绱濇禒搴㈡殶閹诡喖绨遍懢宄板絿
            Set<String> permissions = imUserMapper.selectUserPermissionsByUserId(userId);
            if (permissions == null) {
                permissions = new HashSet<>();
            }
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            log.debug("閼惧嘲褰囬悽銊﹀煕閺夊啴妾洪幋鎰: userId={}, permissionCount={}, 閼版妞?{}ms", 
                    userId, permissions.size(), System.currentTimeMillis() - startTime);
            
            return permissions;
            
        } catch (Exception e) {
            log.error("閼惧嘲褰囬悽銊﹀煕閺夊啴妾哄鍌氱埗: userId={}, error={}", userId, e.getMessage(), e);
            return new HashSet<>();
        }
    }
    
    /**
     * 閼惧嘲褰囬悽銊﹀煕鐟欐帟澹婇崚妤勩€?
     * 
     * @param userId 閻劍鍩汭D
     * @return 鐟欐帟澹婇梿鍡楁値
     */
    @Override
    public Set<String> getUserRoles(Long userId) {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("閼惧嘲褰囬悽銊﹀煕鐟欐帟澹? userId={}", userId);
            
            // 缂傛挸鐡ㄩ柨?            String cacheKey = "im:user:roles:" + userId;
            
            // 閸忓牊顥呴弻銉х处鐎?            Set<String> cachedRoles = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRoles != null) {
                log.debug("娴犲海绱︾€涙骞忛崣鏍暏閹寸柉顫楅懝? userId={}, roleCount={}", userId, cachedRoles.size());
                return cachedRoles;
            }
            
            // 缂傛挸鐡ㄩ張顏勬嚒娑擃叏绱濇禒搴㈡殶閹诡喖绨遍懢宄板絿
            Set<String> roles = imUserMapper.selectUserRolesByUserId(userId);
            if (roles == null) {
                roles = new HashSet<>();
            }
            
            // 缂傛挸鐡ㄧ紒鎾寸亯
            redisTemplate.opsForValue().set(cacheKey, roles, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            log.debug("閼惧嘲褰囬悽銊﹀煕鐟欐帟澹婇幋鎰: userId={}, roleCount={}, 閼版妞?{}ms", 
                    userId, roles.size(), System.currentTimeMillis() - startTime);
            
            return roles;
            
        } catch (Exception e) {
            log.error("閼惧嘲褰囬悽銊﹀煕鐟欐帟澹婂鍌氱埗: userId={}, error={}", userId, e.getMessage(), e);
            return new HashSet<>();
        }
    }
}
