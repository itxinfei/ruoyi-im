package com.ruoyi.im.util;

import cn.hutool.core.util.StrUtil;

import java.util.function.Function;

/**
 * VO构建器
 * <p>
 * 简化Entity到VO的转换过程，支持链式调用和自定义转换逻辑
 * </p>
 *
 * @param <E> Entity类型
 * @param <V> VO类型
 * @author ruoyi
 */
public class VoBuilder<E, V> {

    private final E entity;
    private final Class<V> voClass;
    private V vo;

    private VoBuilder(E entity, Class<V> voClass) {
        this.entity = entity;
        this.voClass = voClass;
    }

    /**
     * 开始构建
     *
     * @param entity 源对象
     * @param voClass 目标VO类
     * @param <E> Entity类型
     * @param <V> VO类型
     * @return 构建器
     */
    public static <E, V> VoBuilder<E, V> of(E entity, Class<V> voClass) {
        return new VoBuilder<>(entity, voClass);
    }

    /**
     * 执行转换
     *
     * @return 构建器
     */
    public VoBuilder<E, V> convert() {
        this.vo = BeanConvertUtil.convert(entity, voClass);
        return this;
    }

    /**
     * 自定义转换逻辑
     *
     * @param mapper 转换函数
     * @return 构建器
     */
    public VoBuilder<E, V> with(Function<E, V> mapper) {
        this.vo = mapper.apply(entity);
        return this;
    }

    /**
     * 应用额外操作
     *
     * @param consumer 操作函数
     * @return 构建器
     */
    public VoBuilder<E, V> andThen(java.util.function.Consumer<V> consumer) {
        if (this.vo != null && consumer != null) {
            consumer.accept(this.vo);
        }
        return this;
    }

    /**
     * 条件应用
     *
     * @param condition 条件
     * @param consumer 操作函数
     * @return 构建器
     */
    public VoBuilder<E, V> andThenIf(boolean condition, java.util.function.Consumer<V> consumer) {
        if (condition && this.vo != null && consumer != null) {
            consumer.accept(this.vo);
        }
        return this;
    }

    /**
     * 非空条件应用
     *
     * @param value 判断值
     * @param consumer 操作函数
     * @return 构建器
     */
    public VoBuilder<E, V> andThenIfNotNull(Object value, java.util.function.Consumer<V> consumer) {
        if (value != null && this.vo != null && consumer != null) {
            consumer.accept(this.vo);
        }
        return this;
    }

    /**
     * 非空字符串条件应用
     *
     * @param value 判断值
     * @param consumer 操作函数
     * @return 构建器
     */
    public VoBuilder<E, V> andThenIfNotBlank(String value, java.util.function.Consumer<V> consumer) {
        if (StrUtil.isNotBlank(value) && this.vo != null && consumer != null) {
            consumer.accept(this.vo);
        }
        return this;
    }

    /**
     * 构建并返回VO
     *
     * @return VO对象
     */
    public V build() {
        if (this.vo == null) {
            this.vo = BeanConvertUtil.convert(entity, voClass);
        }
        return this.vo;
    }
}
