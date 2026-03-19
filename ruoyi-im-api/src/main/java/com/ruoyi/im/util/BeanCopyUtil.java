package com.ruoyi.im.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Bean复制工具类
 * 解决项目中大量重复的BeanUtils.copyProperties模式
 *
 * @author ruoyi
 */
public class BeanCopyUtil {

    /**
     * 获取null属性的名称
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 复制对象属性（跳过null值）
     */
    public static void copyNonNullProperties(Object src, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    /**
     * 复制列表对象（基础版本）
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                .map(source -> copyToObject(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * 复制列表对象（带自定义处理）
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass,
                                            Consumer<S> customizer) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                .map(source -> {
                    T target = copyToObject(source, targetClass);
                    if (customizer != null) {
                        customizer.accept(source);
                    }
                    return target;
                })
                .collect(Collectors.toList());
    }

    /**
     * 复制单个对象
     */
    public static <S, T> T copyToObject(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean复制失败: " + e.getMessage(), e);
        }
    }

    /**
     * 复制单个对象（带自定义处理）
     */
    public static <S, T> T copyToObject(S source, Class<T> targetClass, Consumer<S> customizer) {
        if (source == null) {
            return null;
        }
        T target = copyToObject(source, targetClass);
        if (customizer != null) {
            customizer.accept(source);
        }
        return target;
    }

    /**
     * 复制并设置属性（简化版，用于常见场景）
     */
    public static <S, T> T copyAndSet(S source, Class<T> targetClass,
                                        Consumer<T> propertySetter) {
        if (source == null) {
            return null;
        }
        T target = copyToObject(source, targetClass);
        if (propertySetter != null) {
            propertySetter.accept(target);
        }
        return target;
    }
}
