package com.ruoyi.im.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.im.mapper.ImFileAssetMapper;
import com.ruoyi.im.domain.ImFileAsset;
import com.ruoyi.im.service.ImFileAssetService;
import com.ruoyi.im.exception.BusinessException;

/**
 * 文件资源Service业务层处理 - 优化版本
 * 优化内容：添加缓存机制、事务控制、性能监控、错误处理
 * 
 * @author ruoyi
 */
@Service("imFileAssetServiceImpl")
public class ImFileAssetServiceImpl extends EnhancedBaseServiceImpl<ImFileAsset, ImFileAssetMapper> implements ImFileAssetService {
    private static final Logger log = LoggerFactory.getLogger(ImFileAssetServiceImpl.class);
    
    @Autowired
    private ImFileAssetMapper imFileAssetMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存键前缀
    private static final String FILE_ASSET_CACHE_PREFIX = "im:file:asset:";
    private static final String FILE_MD5_CACHE_PREFIX = "im:file:md5:";
    private static final String USER_FILES_CACHE_PREFIX = "im:user:files:";
    
    // 缓存超时时间（分钟）
    private static final int CACHE_TIMEOUT_MINUTES = 60; // 文件资源缓存时间相对较长
    
    /**
     * 查询文件资源列表（支持分页）
     * 
     * @param imFileAsset 文件资源条件
     * @return 文件资源集合
     */
    @Override
    public List<ImFileAsset> selectList(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectList";
        
        try {
            log.debug("查询文件资源列表: params={}, method={}", imFileAsset, methodName);
            
            // 基础列表查询逻辑由父类实现
            List<ImFileAsset> result = super.selectList(imFileAsset);
            
            log.debug("查询文件资源列表完成: count={}, method={}", result != null ? result.size() : 0, methodName);
            return result;
            
        } catch (Exception e) {
            log.error("查询文件资源列表异常: params={}, error={}, method={}", imFileAsset, e.getMessage(), methodName, e);
            throw new BusinessException("查询文件资源列表失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("查询文件资源列表耗时: {}ms, method={}", duration, methodName);
        }
    }
    
    /**
     * 新增文件资源
     * 
     * @param imFileAsset 文件资源信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "insert";
        
        try {
            log.debug("新增文件资源: fileName={}, method={}", imFileAsset != null ? imFileAsset.getFileName() : null, methodName);
            
            // 设置创建时间
            setCreateTime(imFileAsset);
            
            // 基础插入逻辑由父类实现
            int result = super.insert(imFileAsset);
            
            if (result > 0) {
                log.info("新增文件资源成功: fileId={}, fileName={}, fileSize={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), imFileAsset.getFileSize(), result, methodName);
                
                // 清除用户文件列表缓存
                if (imFileAsset.getUploaderId() != null) {
                    clearUserFilesCache(imFileAsset.getUploaderId());
                }
                
                // 缓存MD5信息
                if (imFileAsset.getMd5() != null) {
                    cacheFileByMd5(imFileAsset);
                }
            } else {
                log.warn("新增文件资源失败: fileName={}, result={}, method={}", imFileAsset.getFileName(), result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("新增文件资源异常: fileName={}, error={}, method={}", imFileAsset.getFileName(), e.getMessage(), methodName, e);
            throw new BusinessException("新增文件资源失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("新增文件资源耗时: {}ms, fileName={}, method={}", duration, imFileAsset.getFileName(), methodName);
        }
    }
    
    /**
     * 修改文件资源
     * 
     * @param imFileAsset 文件资源信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ImFileAsset imFileAsset) {
        long startTime = System.currentTimeMillis();
        String methodName = "update";
        
        try {
            log.debug("修改文件资源: fileId={}, fileName={}, method={}", 
                      imFileAsset.getId(), imFileAsset.getFileName(), methodName);
            
            // 设置更新时间
            setUpdateTime(imFileAsset);
            
            // 基础修改逻辑由父类实现
            int result = super.update(imFileAsset);
            
            if (result > 0) {
                log.info("修改文件资源成功: fileId={}, fileName={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), result, methodName);
                
                // 清除相关缓存
                clearRelatedCache(imFileAsset);
            } else {
                log.warn("修改文件资源失败: fileId={}, fileName={}, result={}, method={}", 
                         imFileAsset.getId(), imFileAsset.getFileName(), result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("修改文件资源异常: fileId={}, fileName={}, error={}, method={}", 
                      imFileAsset.getId(), imFileAsset.getFileName(), e.getMessage(), methodName, e);
            throw new BusinessException("修改文件资源失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("修改文件资源耗时: {}ms, fileId={}, method={}", duration, imFileAsset.getId(), methodName);
        }
    }
    
    /**
     * 批量删除文件资源
     * 
     * @param ids 需要删除的文件资源ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteByIds";
        
        try {
            log.debug("批量删除文件资源: ids={}, count={}, method={}", ids, ids != null ? ids.length : 0, methodName);
            
            if (ids == null || ids.length == 0) {
                log.warn("批量删除文件资源参数无效: ids={}, method={}", ids, methodName);
                throw new BusinessException("批量删除文件资源参数无效");
            }
            
            // 批量删除前先查询相关文件信息，用于缓存清理
            List<ImFileAsset> fileAssets = null;
            for (Long id : ids) {
                ImFileAsset fileAsset = selectById(id);
                if (fileAsset != null) {
                    if (fileAssets == null) {
                        fileAssets = new java.util.ArrayList<>();
                    }
                    fileAssets.add(fileAsset);
                }
            }
            
            // 基础批量删除逻辑由父类实现
            int result = super.deleteByIds(ids);
            
            if (result > 0) {
                log.info("批量删除文件资源成功: count={}, result={}, method={}", ids.length, result, methodName);
                
                // 清除相关缓存
                if (fileAssets != null) {
                    for (ImFileAsset fileAsset : fileAssets) {
                        clearRelatedCache(fileAsset);
                    }
                }
            } else {
                log.warn("批量删除文件资源失败: ids={}, count={}, result={}, method={}", ids, ids.length, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("批量删除文件资源异常: ids={}, error={}, method={}", ids, e.getMessage(), methodName, e);
            throw new BusinessException("批量删除文件资源失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("批量删除文件资源耗时: {}ms, ids={}, count={}, method={}", duration, ids, ids != null ? ids.length : 0, methodName);
        }
    }
    
    /**
     * 删除文件资源信息
     * 
     * @param id 文件资源ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        long startTime = System.currentTimeMillis();
        String methodName = "deleteById";
        
        try {
            log.debug("删除文件资源: fileId={}, method={}", id, methodName);
            
            // 先查询文件信息，用于缓存清理
            ImFileAsset fileAsset = selectById(id);
            
            // 基础删除逻辑由父类实现
            int result = super.deleteById(id);
            
            if (result > 0) {
                log.info("删除文件资源成功: fileId={}, result={}, method={}", id, result, methodName);
                
                // 清除相关缓存
                if (fileAsset != null) {
                    clearRelatedCache(fileAsset);
                }
            } else {
                log.warn("删除文件资源失败: fileId={}, result={}, method={}", id, result, methodName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("删除文件资源异常: fileId={}, error={}, method={}", id, e.getMessage(), methodName, e);
            throw new BusinessException("删除文件资源失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("删除文件资源耗时: {}ms, fileId={}, method={}", duration, id, methodName);
        }
    }
    
    /**
     * 根据MD5值查询文件资源
     * 
     * @param md5 文件MD5值
     * @return 文件资源
     */
    public ImFileAsset selectImFileAssetByMd5(String md5) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImFileAssetByMd5";
        
        try {
            // 参数验证
            if (md5 == null || md5.trim().isEmpty()) {
                throw new BusinessException(methodName + "参数无效: MD5不能为空");
            }
            
            log.debug("根据MD5查询文件资源: md5={}, method={}", md5, methodName);
            
            // 生成缓存键
            String cacheKey = FILE_MD5_CACHE_PREFIX + md5;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            ImFileAsset cachedFile = (ImFileAsset) redisTemplate.opsForValue().get(cacheKey);
            if (cachedFile != null) {
                log.debug("从缓存获取文件资源: md5={}, fileId={}, method={}", md5, cachedFile.getId(), methodName);
                return cachedFile;
            }
            
            // 构建查询条件
            ImFileAsset query = new ImFileAsset();
            query.setMd5(md5);
            
            // 查询数据库
            List<ImFileAsset> fileAssets = selectList(query);
            ImFileAsset fileAsset = fileAssets != null && !fileAssets.isEmpty() ? fileAssets.get(0) : null;
            
            // 缓存结果
            if (fileAsset != null) {
                redisTemplate.opsForValue().set(cacheKey, fileAsset, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("文件资源已缓存: md5={}, fileId={}, method={}", md5, fileAsset.getId(), methodName);
            }
            
            return fileAsset;
            
        } catch (Exception e) {
            log.error("根据MD5查询文件资源异常: md5={}, error={}, method={}", md5, e.getMessage(), methodName, e);
            throw new BusinessException("根据MD5查询文件资源失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据MD5查询文件资源耗时: {}ms, md5={}, method={}", duration, md5, methodName);
        }
    }
    
    /**
     * 根据用户ID查询用户上传的文件资源列表
     * 
     * @param userId 用户ID
     * @return 文件资源集合
     */
    public List<ImFileAsset> selectImFileAssetListByUploaderId(Long userId) {
        long startTime = System.currentTimeMillis();
        String methodName = "selectImFileAssetListByUploaderId";
        
        try {
            // 参数验证
            validateId(userId, methodName);
            
            log.debug("根据用户ID查询文件资源列表: userId={}, method={}", userId, methodName);
            
            // 生成缓存键
            String cacheKey = USER_FILES_CACHE_PREFIX + userId;
            
            // 检查缓存
            @SuppressWarnings("unchecked")
            List<ImFileAsset> cachedFiles = (List<ImFileAsset>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedFiles != null && !cachedFiles.isEmpty()) {
                log.debug("从缓存获取用户文件资源列表: userId={}, count={}, method={}", userId, cachedFiles.size(), methodName);
                return cachedFiles;
            }
            
            // 构建查询条件
            ImFileAsset query = new ImFileAsset();
            query.setUploaderId(userId);
            
            // 查询数据库
            List<ImFileAsset> fileAssets = selectList(query);
            
            // 缓存结果
            if (fileAssets != null && !fileAssets.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, fileAssets, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("用户文件资源列表已缓存: userId={}, count={}, method={}", userId, fileAssets.size(), methodName);
            }
            
            return fileAssets;
            
        } catch (Exception e) {
            log.error("根据用户ID查询文件资源列表异常: userId={}, error={}, method={}", userId, e.getMessage(), methodName, e);
            throw new BusinessException("根据用户ID查询文件资源列表失败", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("根据用户ID查询文件资源列表耗时: {}ms, userId={}, method={}", duration, userId, methodName);
        }
    }
    
    /**
     * 根据MD5缓存文件资源
     * 
     * @param fileAsset 文件资源
     */
    private void cacheFileByMd5(ImFileAsset fileAsset) {
        if (fileAsset != null && fileAsset.getMd5() != null) {
            String cacheKey = FILE_MD5_CACHE_PREFIX + fileAsset.getMd5();
            redisTemplate.opsForValue().set(cacheKey, fileAsset, CACHE_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
            log.debug("文件资源MD5缓存已更新: fileId={}, md5={}", fileAsset.getId(), fileAsset.getMd5());
        }
    }
    
    /**
     * 清除用户文件列表缓存
     * 
     * @param userId 用户ID
     */
    private void clearUserFilesCache(Long userId) {
        if (userId != null) {
            String cacheKey = USER_FILES_CACHE_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("用户文件列表缓存已清除: userId={}", userId);
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @return 实体类型名称
     */
    @Override
    protected String getEntityType() {
        return "fileAsset";
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 文件资源实体
     * @return 文件资源ID
     */
    @Override
    protected Long getEntityId(ImFileAsset entity) {
        return entity != null ? entity.getId() : null;
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 文件资源实体
     */
    @Override
    protected void setCreateTime(ImFileAsset entity) {
        if (entity != null && entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl的抽象方法
     * 
     * @param entity 文件资源实体
     */
    @Override
    protected void setUpdateTime(ImFileAsset entity) {
        if (entity != null) {
            // 文件资源通常不需要更新时间，但这里提供统一的接口
            // 如果需要更新，可以取消下面的注释
            // entity.setUpdateTime(LocalDateTime.now());
        }
    }
    
    /**
     * 实现EnhancedBaseServiceImpl中的clearRelatedCache方法，提供文件资源特定缓存清理逻辑
     * 
     * @param entity 文件资源实体
     */
    @Override
    protected void clearRelatedCache(ImFileAsset entity) {
        if (entity != null) {
            // 清除实体缓存
            clearEntityCache(entity.getId());
            
            // 清除MD5缓存
            if (entity.getMd5() != null) {
                redisTemplate.delete(FILE_MD5_CACHE_PREFIX + entity.getMd5());
            }
            
            // 清除用户文件列表缓存
            if (entity.getUploaderId() != null) {
                clearUserFilesCache(entity.getUploaderId());
            }
        }
    }
}