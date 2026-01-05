package com.ruoyi.im.service.impl;

import com.ruoyi.im.domain.ImUserDevice;
import com.ruoyi.im.mapper.ImUserDeviceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 用户设备服务测试类
 *
 * @author ruoyi
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class ImUserDeviceServiceImplTest {

    @Mock
    private ImUserDeviceMapper mockImUserDeviceMapper;
    
    @Mock
    private RedisTemplate<String, Object> mockRedisTemplate;
    
    @Mock
    private ValueOperations<String, Object> mockValueOperations;
    
    private ImUserDeviceServiceImpl imUserDeviceServiceImpl;
    
    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_DEVICE_ID = 2001L;
    private static final String TEST_DEVICE_NAME = "测试设备";
    private static final String TEST_DEVICE_TYPE = "ANDROID";
    private static final String TEST_DEVICE_IDENTIFIER = "device123456";
    
    @BeforeEach
    public void setup() {
        // 创建服务实例
        imUserDeviceServiceImpl = new ImUserDeviceServiceImpl();
        
        // 使用反射设置mock对象
        ReflectionTestUtils.setField(imUserDeviceServiceImpl, "baseMapper", mockImUserDeviceMapper);
        ReflectionTestUtils.setField(imUserDeviceServiceImpl, "redisTemplate", mockRedisTemplate);
        
        // 设置Redis ValueOperations的mock
        when(mockRedisTemplate.opsForValue()).thenReturn(mockValueOperations);
    }
    
    /**
     * 测试通过ID查询设备
     */
    @Test
    public void testSelectById() {
        // 准备测试数据
        ImUserDevice testDevice = new ImUserDevice();
        testDevice.setId(TEST_DEVICE_ID);
        testDevice.setUserId(TEST_USER_ID);
        testDevice.setDeviceName(TEST_DEVICE_NAME);
        testDevice.setDeviceType(TEST_DEVICE_TYPE);
        testDevice.setDeviceId(TEST_DEVICE_IDENTIFIER);
        testDevice.setStatus("ACTIVE");
        testDevice.setLastActiveTime(LocalDateTime.now());
        
        // 设置mock行为
        when(mockValueOperations.get("im:entity:userDevice:" + TEST_DEVICE_ID)).thenReturn(null);
        when(mockImUserDeviceMapper.selectById(TEST_DEVICE_ID)).thenReturn(testDevice);
        
        // 执行测试
        ImUserDevice result = imUserDeviceServiceImpl.selectById(TEST_DEVICE_ID);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_DEVICE_ID, result.getId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_DEVICE_NAME, result.getDeviceName());
        assertEquals(TEST_DEVICE_TYPE, result.getDeviceType());
        assertEquals(TEST_DEVICE_IDENTIFIER, result.getDeviceId());
        assertEquals("ACTIVE", result.getStatus());
        
        // 验证缓存
        verify(mockRedisTemplate).opsForValue();
        verify(mockValueOperations).get("im:entity:userDevice:" + TEST_DEVICE_ID);
        verify(mockValueOperations).set(eq("im:entity:userDevice:" + TEST_DEVICE_ID), eq(testDevice), anyInt(), eq(TimeUnit.MINUTES));
        
        // 验证数据库查询
        verify(mockImUserDeviceMapper).selectById(TEST_DEVICE_ID);
    }
    
    /**
     * 测试注册新设备
     */
    @Test
    public void testRegisterDeviceNewDevice() {
        // 准备测试数据
        String osVersion = "Android 12";
        String appVersion = "1.0.0";
        String ipAddress = "192.168.1.100";
        String location = "北京市";
        
        // 设置mock行为
        when(mockImUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(TEST_USER_ID, TEST_DEVICE_IDENTIFIER))
                .thenReturn(null); // 设备不存在
        
        when(mockImUserDeviceMapper.insert(any(ImUserDevice.class))).thenReturn(1);
        
        // 执行测试
        int result = imUserDeviceServiceImpl.registerDevice(
                TEST_USER_ID, 
                TEST_DEVICE_TYPE, 
                TEST_DEVICE_IDENTIFIER, 
                TEST_DEVICE_NAME, 
                osVersion, 
                appVersion, 
                ipAddress, 
                location);
        
        // 验证结果
        assertEquals(1, result);
        
        // 验证数据库插入
        verify(mockImUserDeviceMapper).insert(argThat(device -> {
            return device.getUserId().equals(TEST_USER_ID) &&
                   device.getDeviceType().equals(TEST_DEVICE_TYPE) &&
                   device.getDeviceId().equals(TEST_DEVICE_IDENTIFIER) &&
                   device.getDeviceName().equals(TEST_DEVICE_NAME) &&
                   device.getOsVersion().equals(osVersion) &&
                   device.getAppVersion().equals(appVersion) &&
                   device.getIpAddress().equals(ipAddress) &&
                   device.getLocation().equals(location) &&
                   "ACTIVE".equals(device.getStatus());
        }));
    }
    
    /**
     * 测试注册已存在设备（更新）
     */
    @Test
    public void testRegisterDeviceExistingDevice() {
        // 准备测试数据
        ImUserDevice existingDevice = new ImUserDevice();
        existingDevice.setId(TEST_DEVICE_ID);
        existingDevice.setUserId(TEST_USER_ID);
        existingDevice.setDeviceName("旧设备名称");
        existingDevice.setDeviceType(TEST_DEVICE_TYPE);
        existingDevice.setDeviceId(TEST_DEVICE_IDENTIFIER);
        existingDevice.setStatus("ACTIVE");
        existingDevice.setLastActiveTime(LocalDateTime.now());
        
        String osVersion = "Android 13";
        String appVersion = "2.0.0";
        String ipAddress = "192.168.1.101";
        String location = "上海市";
        
        // 设置mock行为
        when(mockImUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(TEST_USER_ID, TEST_DEVICE_IDENTIFIER))
                .thenReturn(existingDevice);
        
        // 设置缓存查询
        when(mockValueOperations.get("im:entity:userDevice:" + TEST_DEVICE_ID)).thenReturn(null);
        when(mockImUserDeviceMapper.selectById(TEST_DEVICE_ID)).thenReturn(existingDevice);
        
        when(mockImUserDeviceMapper.updateById(any(ImUserDevice.class))).thenReturn(1);
        
        // 执行测试
        int result = imUserDeviceServiceImpl.registerDevice(
                TEST_USER_ID, 
                TEST_DEVICE_TYPE, 
                TEST_DEVICE_IDENTIFIER, 
                TEST_DEVICE_NAME, 
                osVersion, 
                appVersion, 
                ipAddress, 
                location);
        
        // 验证结果
        assertEquals(1, result);
        
        // 验证数据库更新
        verify(mockImUserDeviceMapper).updateById(argThat(device -> {
            return device.getId().equals(TEST_DEVICE_ID) &&
                   device.getUserId().equals(TEST_USER_ID) &&
                   device.getDeviceName().equals(TEST_DEVICE_NAME) &&
                   device.getDeviceType().equals(TEST_DEVICE_TYPE) &&
                   device.getDeviceId().equals(TEST_DEVICE_IDENTIFIER) &&
                   device.getOsVersion().equals(osVersion) &&
                   device.getAppVersion().equals(appVersion) &&
                   device.getIpAddress().equals(ipAddress) &&
                   device.getLocation().equals(location) &&
                   "ACTIVE".equals(device.getStatus());
        }));
    }
    
    /**
     * 测试删除设备
     */
    @Test
    public void testDeleteById() {
        // 准备测试数据
        ImUserDevice testDevice = new ImUserDevice();
        testDevice.setId(TEST_DEVICE_ID);
        testDevice.setUserId(TEST_USER_ID);
        testDevice.setDeviceName(TEST_DEVICE_NAME);
        testDevice.setDeviceType(TEST_DEVICE_TYPE);
        testDevice.setDeviceId(TEST_DEVICE_IDENTIFIER);
        testDevice.setStatus("ACTIVE");
        testDevice.setLastActiveTime(LocalDateTime.now());
        
        // 设置mock行为
        when(mockValueOperations.get("im:entity:userDevice:" + TEST_DEVICE_ID)).thenReturn(null);
        when(mockImUserDeviceMapper.selectById(TEST_DEVICE_ID)).thenReturn(testDevice);
        when(mockImUserDeviceMapper.deleteById(TEST_DEVICE_ID)).thenReturn(1);
        
        // 执行测试
        int result = imUserDeviceServiceImpl.deleteById(TEST_DEVICE_ID);
        
        // 验证结果
        assertEquals(1, result);
        
        // 验证数据库删除
        verify(mockImUserDeviceMapper).deleteById(TEST_DEVICE_ID);
        
        // 验证缓存清理
        verify(mockRedisTemplate).delete("im:entity:userDevice:" + TEST_DEVICE_ID);
        verify(mockRedisTemplate).delete("im:device:id:" + TEST_DEVICE_IDENTIFIER);
        verify(mockRedisTemplate).delete("im:user:device:id:" + TEST_USER_ID + ":" + TEST_DEVICE_IDENTIFIER);
    }
    
    /**
     * 测试根据用户ID查询设备列表
     */
    @Test
    public void testSelectImUserDeviceByUserId() {
        // 准备测试数据
        List<ImUserDevice> testDevices = new ArrayList<>();
        
        ImUserDevice device1 = new ImUserDevice();
        device1.setId(TEST_DEVICE_ID);
        device1.setUserId(TEST_USER_ID);
        device1.setDeviceName("设备1");
        device1.setDeviceType(TEST_DEVICE_TYPE);
        device1.setDeviceId("device1");
        testDevices.add(device1);
        
        ImUserDevice device2 = new ImUserDevice();
        device2.setId(2002L);
        device2.setUserId(TEST_USER_ID);
        device2.setDeviceName("设备2");
        device2.setDeviceType("IOS");
        device2.setDeviceId("device2");
        testDevices.add(device2);
        
        // 设置mock行为
        when(mockValueOperations.get("im:user:devices:" + TEST_USER_ID)).thenReturn(null);
        when(mockImUserDeviceMapper.selectImUserDeviceByUserId(TEST_USER_ID)).thenReturn(testDevices);
        
        // 执行测试
        List<ImUserDevice> result = imUserDeviceServiceImpl.selectImUserDeviceByUserId(TEST_USER_ID);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("设备1", result.get(0).getDeviceName());
        assertEquals("设备2", result.get(1).getDeviceName());
        
        // 验证缓存
        verify(mockRedisTemplate).opsForValue();
        verify(mockValueOperations).get("im:user:devices:" + TEST_USER_ID);
        verify(mockValueOperations).set(eq("im:user:devices:" + TEST_USER_ID), eq(testDevices), anyInt(), eq(TimeUnit.MINUTES));
        
        // 验证数据库查询
        verify(mockImUserDeviceMapper).selectImUserDeviceByUserId(TEST_USER_ID);
    }
    
    /**
     * 测试通过用户ID和设备ID查询设备
     */
    @Test
    public void testSelectImUserDeviceByUserIdAndDeviceId() {
        // 准备测试数据
        ImUserDevice testDevice = new ImUserDevice();
        testDevice.setId(TEST_DEVICE_ID);
        testDevice.setUserId(TEST_USER_ID);
        testDevice.setDeviceName(TEST_DEVICE_NAME);
        testDevice.setDeviceType(TEST_DEVICE_TYPE);
        testDevice.setDeviceId(TEST_DEVICE_IDENTIFIER);
        testDevice.setStatus("ACTIVE");
        testDevice.setLastActiveTime(LocalDateTime.now());
        
        // 设置mock行为
        when(mockValueOperations.get("im:user:device:id:" + TEST_USER_ID + ":" + TEST_DEVICE_IDENTIFIER)).thenReturn(null);
        when(mockImUserDeviceMapper.selectImUserDeviceByUserIdAndDeviceId(TEST_USER_ID, TEST_DEVICE_IDENTIFIER))
                .thenReturn(testDevice);
        
        // 执行测试
        ImUserDevice result = imUserDeviceServiceImpl.selectImUserDeviceByUserIdAndDeviceId(
                TEST_USER_ID, 
                TEST_DEVICE_IDENTIFIER);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_DEVICE_ID, result.getId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_DEVICE_IDENTIFIER, result.getDeviceId());
        assertEquals("ACTIVE", result.getStatus());
        
        // 验证缓存
        verify(mockRedisTemplate).opsForValue();
        verify(mockValueOperations).get("im:user:device:id:" + TEST_USER_ID + ":" + TEST_DEVICE_IDENTIFIER);
        verify(mockValueOperations).set(
                eq("im:user:device:id:" + TEST_USER_ID + ":" + TEST_DEVICE_IDENTIFIER), 
                eq(testDevice), 
                anyInt(), 
                eq(TimeUnit.MINUTES));
        
        // 验证数据库查询
        verify(mockImUserDeviceMapper).selectImUserDeviceByUserIdAndDeviceId(TEST_USER_ID, TEST_DEVICE_IDENTIFIER);
    }
    
    /**
     * 测试更新设备状态
     */
    @Test
    public void testUpdateDeviceStatus() {
        // 准备测试数据
        ImUserDevice testDevice = new ImUserDevice();
        testDevice.setId(TEST_DEVICE_ID);
        testDevice.setUserId(TEST_USER_ID);
        testDevice.setDeviceName(TEST_DEVICE_NAME);
        testDevice.setDeviceType(TEST_DEVICE_TYPE);
        testDevice.setDeviceId(TEST_DEVICE_IDENTIFIER);
        testDevice.setStatus("ACTIVE");
        testDevice.setLastActiveTime(LocalDateTime.now());
        
        // 设置mock行为
        when(mockValueOperations.get("im:entity:userDevice:" + TEST_DEVICE_ID)).thenReturn(null);
        when(mockImUserDeviceMapper.selectById(TEST_DEVICE_ID)).thenReturn(testDevice);
        when(mockImUserDeviceMapper.updateById(any(ImUserDevice.class))).thenReturn(1);
        
        // 执行测试
        int result = imUserDeviceServiceImpl.updateDeviceStatus(TEST_DEVICE_ID, "OFFLINE");
        
        // 验证结果
        assertEquals(1, result);
        
        // 验证数据库更新
        verify(mockImUserDeviceMapper).updateById(argThat(device -> {
            return device.getId().equals(TEST_DEVICE_ID) &&
                   "OFFLINE".equals(device.getStatus());
        }));
    }
}