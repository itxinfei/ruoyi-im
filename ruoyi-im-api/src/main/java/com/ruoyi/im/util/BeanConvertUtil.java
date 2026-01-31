package com.ruoyi.im.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean转换工具类
 * <p>
 * 封装常用的对象转换操作，减少重复代码
 * </p>
 *
 * @author ruoyi
 */
public class BeanConvertUtil {

    /**
     * 转换对象
     *
     * @param source 源对象
     * @param targetClass 目标类
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return BeanUtil.toBean(source, targetClass);
    }

    /**
     * 转换对象（忽略空值）
     *
     * @param source 源对象
     * @param targetClass 目标类
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T convertIgnoreNull(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        T target = BeanUtil.toBean(source, targetClass);
        BeanUtil.copyProperties(source, target, true);
        return target;
    }

    /**
     * 转换列表
     *
     * @param sourceList 源列表
     * @param targetClass 目标类
     * @param <T> 目标类型
     * @param <S> 源类型
     * @return 目标列表
     */
    public static <T, S> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
        if (CollUtil.isEmpty(sourceList)) {
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(sourceList, targetClass);
    }

    /**
     * 转换对象到已有目标对象
     *
     * @param source 源对象
     * @param target 目标对象
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T copyProperties(Object source, T target) {
        if (source == null || target == null) {
            return target;
        }
        BeanUtil.copyProperties(source, target);
        return target;
    }

    /**
     * 转换对象并执行额外操作
     *
     * @param source 源对象
     * @param targetClass 目标类
     * @param consumer 额外操作
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T convertAndThen(Object source, Class<T> targetClass, java.util.function.Consumer<T> consumer) {
        T target = convert(source, targetClass);
        if (target != null && consumer != null) {
            consumer.accept(target);
        }
        return target;
    }

    /**
     * 转换列表并执行额外操作
     *
     * @param sourceList 源列表
     * @param targetClass 目标类
     * @param consumer 额外操作
     * @param <T> 目标类型
     * @param <S> 源类型
     * @return 目标列表
     */
    public static <T, S> List<T> convertListAndThen(List<S> sourceList, Class<T> targetClass, java.util.function.Consumer<T> consumer) {
        List<T> targetList = convertList(sourceList, targetClass);
        if (consumer != null) {
            targetList.forEach(consumer);
        }
        return targetList;
    }

    private BeanConvertUtil() {
        // 工具类禁止实例化
    }
}
