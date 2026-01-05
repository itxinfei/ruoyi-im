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
 * IM用户Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、事务控制、异步处理、性能监控、错误处理
 * 
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
    
    // 缓存键前缀
    private static final String USER_CACHE_PREFIX = "im:user:";
    private static final String USERNAME_CACHE_PREFIX = "im:username:";
    private static final String EMAIL_CACHE_PREFIX = "im:email:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 30;
    
    @PostConstruct
    public void init() {
        log.info("ImUserService初始化完成，开始启用缓存和性能监控");
    }
    
    /**
     * 根据用户名查询用户 - 优化版本
     * 优化内容：添加Redis缓存、性能监控、异常处理
     * 
     * @param username 用户名
     * @return IM用户
     */
    @Override
    @Cacheable(value = "im-users", key = "'username:' + #username", unless = "#result == null")
    public ImUser selectImUserByUsername(String username) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 首先检查缓存
            String cacheKey = USERNAME_CACHE_PREFIX + username;
            ImUser cachedUser = (ImUser) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                log.debug("从缓存获取用户: {}", username);
                return cachedUser;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库获取用户: {}", username);
            ImUser user = imUserMapper.selectImUserByUsername(username);
            
            // 将结果放入缓存
            if (user != null) {
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                // 同时缓存ID映射
                redisTemplate.opsForValue().set(USER_CACHE_PREFIX + user.getId(), user, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                log.debug("用户信息已缓存: {}", username);
            }
            
            return user;
        } catch (Exception e) {
            log.error("查询用户异常: username={}, error={}", username, e.getMessage(), e);
            throw new BusinessException("用户查询失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("查询用户耗时: {}ms, username={}", duration, username);
        }
    }
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return IM用户
     */
    @Override
    public ImUser selectImUserByEmail(String email) {
        return imUserMapper.selectImUserByEmail(email);
    }
    
    /**
     * 用户注册 - 优化版本
     * 优化内容：事务控制、参数验证、重复检查优化、缓存失效、异步处理
     * 
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @param email 邮箱
     * @param phone 电话
     * @return 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerUser(String username, String password, String nickname, String email, String phone) {
        long startTime = System.currentTimeMillis();
        log.info("开始用户注册: username={}, nickname={}, email={}", username, nickname, email);
        
        try {
            // 参数验证
            ValidationUtils.validateUsername(username, "registerUser");
            ValidationUtils.validatePassword(password, "registerUser");
            ValidationUtils.validateString(nickname, "昵称", "registerUser");
            if (email != null && !email.trim().isEmpty()) {
                ValidationUtils.validateEmail(email, "registerUser");
            }
            if (phone != null && !phone.trim().isEmpty()) {
                ValidationUtils.validatePhone(phone, "registerUser");
            }
            
            // 并行检查用户名和邮箱是否存在，提高性能
            CompletableFuture<ImUser> usernameCheck = CompletableFuture.supplyAsync(() -> {
                return selectImUserByUsername(username);
            });
            
            CompletableFuture<ImUser> emailCheck = CompletableFuture.supplyAsync(() -> {
                if (email != null && !email.trim().isEmpty()) {
                    return selectImUserByEmail(email);
                }
                return null;
            });
            
            // 等待所有检查完成
            CompletableFuture<Void> allChecks = CompletableFuture.allOf(usernameCheck, emailCheck);
            allChecks.get();
            
            ImUser existingUser = usernameCheck.get();
            ImUser emailUser = emailCheck.get();
            
            // 检查用户名是否已存在
            if (existingUser != null) {
                log.warn("用户名已存在: {}", username);
                throw new BusinessException("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (emailUser != null) {
                log.warn("邮箱已存在: {}", email);
                throw new BusinessException("邮箱已存在");
            }
            
            // 创建新用户
            ImUser user = new ImUser();
            user.setUsername(username.trim());
            // 使用BCrypt加密密码
            user.setPassword(passwordEncoder.encode(password));
            user.setNickname(nickname.trim());
            user.setEmail(email != null ? email.trim() : null);
            user.setPhone(phone != null ? phone.trim() : null);
            user.setStatus("ACTIVE");
            user.setAvatar("/profile/avatar.png"); // 默认头像
            user.setCreateTime(java.time.LocalDateTime.now());
            
            int result = insert(user);
            if (result <= 0) {
                log.error("用户创建失败: {}", username);
                throw new BusinessException("用户创建失败");
            }
            
            // 清除相关缓存
            clearUserCache(username, email);
            
            log.info("用户注册成功: userId={}, username={}, 耗时={}ms", user.getId(), username, 
                     System.currentTimeMillis() - startTime);
            
            return user.getId();
            
        } catch (BusinessException e) {
            // 业务异常不回滚，直接抛出
            throw e;
        } catch (Exception e) {
            log.error("用户注册异常: username={}, error={}", username, e.getMessage(), e);
            // 标记事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("用户注册失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("用户注册总耗时: {}ms, username={}", duration, username);
        }
    }
    

    
    /**
     * 清除用户相关缓存
     * 
     * @param username 用户名
     * @param email 邮箱
     */
    @CacheEvict(value = "im-users", key = "'username:' + #username")
    private void clearUserCache(String username, String email) {
        try {
            // 清除用户名缓存
            redisTemplate.delete(USERNAME_CACHE_PREFIX + username);
            
            // 清除邮箱缓存
            if (email != null) {
                redisTemplate.delete(EMAIL_CACHE_PREFIX + email);
            }
            
            log.debug("已清除用户缓存: username={}, email={}", username, email);
        } catch (Exception e) {
            log.warn("清除用户缓存失败: username={}, error={}", username, e.getMessage());
        }
    }
    
    /**
     * 更新用户状态 - 优化版本
     * 优化内容：缓存更新、事务控制、状态验证、性能监控
     * 
     * @param userId 用户ID
     * @param status 状态
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "im-users", allEntries = true)
    public int updateUserStatus(Long userId, String status) {
        long startTime = System.currentTimeMillis();
        log.info("开始更新用户状态: userId={}, status={}", userId, status);
        
        try {
            // 参数验证
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }
            if (status == null || status.trim().isEmpty()) {
                throw new BusinessException("状态不能为空");
            }
            
            // 验证状态值
            if (!isValidStatus(status)) {
                throw new BusinessException("无效的状态值: " + status);
            }
            
            ImUser user = selectById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                throw new BusinessException("用户不存在");
            }
            
            // 检查状态是否需要更新
            if (status.equals(user.getStatus())) {
                log.info("用户状态无需更新: userId={}, status={}", userId, status);
                return 0;
            }
            
            user.setStatus(status);
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            int result = updateById(user);
            if (result <= 0) {
                log.error("用户状态更新失败: userId={}, status={}", userId, status);
                throw new BusinessException("用户状态更新失败");
            }
            
            // 清除相关缓存
            clearUserCacheById(userId);
            
            log.info("用户状态更新成功: userId={}, oldStatus={}, newStatus={}, 耗时={}ms", 
                     userId, user.getStatus(), status, System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户状态异常: userId={}, status={}, error={}", userId, status, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("用户状态更新失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新用户状态总耗时: {}ms, userId={}, status={}", duration, userId, status);
        }
    }
    
    /**
     * 验证状态值是否有效
     * 
     * @param status 状态值
     * @return 是否有效
     */
    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || 
               "INACTIVE".equals(status) || 
               "BANNED".equals(status) || 
               "OFFLINE".equals(status);
    }
    
    /**
     * 根据ID清除用户缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserCacheById(Long userId) {
        try {
            // 清除ID缓存
            redisTemplate.delete(USER_CACHE_PREFIX + userId);
            
            // 清除用户名缓存（需要先查询用户名）
            ImUser user = selectById(userId);
            if (user != null) {
                redisTemplate.delete(USERNAME_CACHE_PREFIX + user.getUsername());
                if (user.getEmail() != null) {
                    redisTemplate.delete(EMAIL_CACHE_PREFIX + user.getEmail());
                }
            }
            
            log.debug("已清除用户缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 更新用户头像 - 优化版本
     * 优化内容：缓存更新、事务控制、参数验证、性能监控
     * 
     * @param userId 用户ID
     * @param avatar 头像URL
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "im-users", key = "'userId:' + #userId")
    public int updateUserAvatar(Long userId, String avatar) {
        long startTime = System.currentTimeMillis();
        log.info("开始更新用户头像: userId={}, avatar={}", userId, avatar);
        
        try {
            // 参数验证
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }
            if (avatar == null || avatar.trim().isEmpty()) {
                throw new BusinessException("头像URL不能为空");
            }
            if (avatar.length() > 500) {
                throw new BusinessException("头像URL长度不能超过500个字符");
            }
            
            ImUser user = selectById(userId);
            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                throw new BusinessException("用户不存在");
            }
            
            // 检查头像是否需要更新
            if (avatar.equals(user.getAvatar())) {
                log.info("用户头像无需更新: userId={}, avatar={}", userId, avatar);
                return 0;
            }
            
            String oldAvatar = user.getAvatar();
            user.setAvatar(avatar.trim());
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            int result = updateById(user);
            if (result <= 0) {
                log.error("用户头像更新失败: userId={}, avatar={}", userId, avatar);
                throw new BusinessException("用户头像更新失败");
            }
            
            // 清除相关缓存
            clearUserCacheById(userId);
            
            log.info("用户头像更新成功: userId={}, oldAvatar={}, newAvatar={}, 耗时={}ms", 
                     userId, oldAvatar, avatar, System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户头像异常: userId={}, avatar={}, error={}", userId, avatar, e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new BusinessException("用户头像更新失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("更新用户头像总耗时: {}ms, userId={}, avatar={}", duration, userId, avatar);
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @return 实体类型名称
     */
    @Override
    protected String getEntityType() {
        return "user";
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 用户实体
     * @return 用户ID
     */
    @Override
    protected Long getEntityId(ImUser entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 用户实体
     */
    @Override
    protected void setCreateTime(ImUser entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 用户实体
     */
    @Override
    protected void setUpdateTime(ImUser entity) {
        if (entity != null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供用户特定缓存清理逻辑
     * 
     * @param entity 用户实体
     */
    @Override
    protected void clearRelatedCache(ImUser entity) {
        if (entity != null) {
            // 清除ID缓存
            clearEntityCache(entity.getId());
            
            // 清除用户名缓存
            if (entity.getUsername() != null) {
                redisTemplate.delete(USERNAME_CACHE_PREFIX + entity.getUsername());
            }
            
            // 清除邮箱缓存
            if (entity.getEmail() != null) {
                redisTemplate.delete(EMAIL_CACHE_PREFIX + entity.getEmail());
            }
        }
    }
    
    /**
     * 重写增强版方法，添加用户特定的逻辑
     * 
     * @param id 用户ID
     * @return 用户实体
     */
    @Override
    public ImUser selectById(Long id) {
        ImUser user = super.selectById(id);
        if (user != null) {
            // 额外缓存用户名和邮箱映射
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
     * 重写增强版方法，添加用户特定逻辑
     * 
     * @param entity 用户实体
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ImUser entity) {
        // 检查用户名是否变更
        boolean usernameChanged = false;
        boolean emailChanged = false;
        
        if (entity.getId() != null) {
            ImUser oldUser = super.selectById(entity.getId());
            if (oldUser != null) {
                usernameChanged = !Objects.equals(oldUser.getUsername(), entity.getUsername());
                emailChanged = !Objects.equals(oldUser.getEmail(), entity.getEmail());
                
                // 如果用户名变更，清除旧的缓存
                if (usernameChanged && oldUser.getUsername() != null) {
                    redisTemplate.delete(USERNAME_CACHE_PREFIX + oldUser.getUsername());
                }
                
                // 如果邮箱变更，清除旧的缓存
                if (emailChanged && oldUser.getEmail() != null) {
                    redisTemplate.delete(EMAIL_CACHE_PREFIX + oldUser.getEmail());
                }
            }
        }
        
        // 调用父类更新方法
        int result = super.update(entity);
        
        // 如果用户名或邮箱变更，清除相关缓存
        if (result > 0 && (usernameChanged || emailChanged)) {
            clearRelatedCache(entity);
        }
        
        return result;
    }
    
    /**
     * 重写增强版方法，添加用户特定逻辑
     * 
     * @param id 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        // 先查询用户（用于清除缓存）
        ImUser user = selectById(id);
        
        // 调用父类删除方法
        int result = super.deleteById(id);
        
        // 如果删除成功，清除相关缓存
        if (result > 0 && user != null) {
            clearRelatedCache(user);
        }
        
        return result;
    }
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param nickname 昵称
     * @param email 邮箱
     * @param phone 电话
     * @return 结果
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
     * 获取用户权限列表
     * 
     * @param userId 用户ID
     * @return 权限集合
     */
    @Override
    public Set<String> getUserPermissions(Long userId) {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("获取用户权限: userId={}", userId);
            
            // 缓存键
            String cacheKey = "im:user:permissions:" + userId;
            
            // 先检查缓存
            Set<String> cachedPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedPermissions != null) {
                log.debug("从缓存获取用户权限: userId={}, permissionCount={}", userId, cachedPermissions.size());
                return cachedPermissions;
            }
            
            // 缓存未命中，从数据库获取
            Set<String> permissions = imUserMapper.selectUserPermissionsByUserId(userId);
            if (permissions == null) {
                permissions = new HashSet<>();
            }
            
            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            log.debug("获取用户权限成功: userId={}, permissionCount={}, 耗时={}ms", 
                    userId, permissions.size(), System.currentTimeMillis() - startTime);
            
            return permissions;
            
        } catch (Exception e) {
            log.error("获取用户权限异常: userId={}, error={}", userId, e.getMessage(), e);
            return new HashSet<>();
        }
    }
    
    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色集合
     */
    @Override
    public Set<String> getUserRoles(Long userId) {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("获取用户角色: userId={}", userId);
            
            // 缓存键
            String cacheKey = "im:user:roles:" + userId;
            
            // 先检查缓存
            Set<String> cachedRoles = (Set<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRoles != null) {
                log.debug("从缓存获取用户角色: userId={}, roleCount={}", userId, cachedRoles.size());
                return cachedRoles;
            }
            
            // 缓存未命中，从数据库获取
            Set<String> roles = imUserMapper.selectUserRolesByUserId(userId);
            if (roles == null) {
                roles = new HashSet<>();
            }
            
            // 缓存结果
            redisTemplate.opsForValue().set(cacheKey, roles, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            log.debug("获取用户角色成功: userId={}, roleCount={}, 耗时={}ms", 
                    userId, roles.size(), System.currentTimeMillis() - startTime);
            
            return roles;
            
        } catch (Exception e) {
            log.error("获取用户角色异常: userId={}, error={}", userId, e.getMessage(), e);
            return new HashSet<>();
        }
    }
}