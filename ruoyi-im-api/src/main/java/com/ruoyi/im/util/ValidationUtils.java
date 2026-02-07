package com.ruoyi.im.util;

import com.ruoyi.im.exception.BusinessException;
import com.ruoyi.im.util.ExceptionHandlerUtil;

/**
 * 参数校验工具类
 * <p>
 * 统一参数校验逻辑，减少重复代码
 * </p>
 *
 * @author ruoyi
 */
public final class ValidationUtils {

    private ValidationUtils() {
        // 防止实例化
    }

    // ========== 非空校验 ==========

    /**
     * 校验对象非空
     *
     * @param obj       待校验对象
     * @param paramName 参数名称
     * @throws BusinessException 参数为空时抛出
     */
    public static void notNull(Object obj, String paramName) {
        if (obj == null) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "不能为空");
        }
    }

    /**
     * 校验对象非空（带默认参数名）
     *
     * @param obj 待校验对象
     * @throws BusinessException 参数为空时抛出
     */
    public static void notNull(Object obj) {
        notNull(obj, "参数");
    }

    /**
     * 校验字符串非空
     *
     * @param str       待校验字符串
     * @param paramName 参数名称
     * @throws BusinessException 字符串为空时抛出
     */
    public static void notEmpty(String str, String paramName) {
        if (str == null || str.isEmpty()) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "不能为空");
        }
    }

    /**
     * 校验字符串非空（带默认参数名）
     *
     * @param str 待校验字符串
     * @throws BusinessException 字符串为空时抛出
     */
    public static void notEmpty(String str) {
        notEmpty(str, "参数");
    }

    /**
     * 校验字符串非空白（非空且不含仅空白字符）
     *
     * @param str       待校验字符串
     * @param paramName 参数名称
     * @throws BusinessException 字符串为空白时抛出
     */
    public static void notBlank(String str, String paramName) {
        if (str == null || str.trim().isEmpty()) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "不能为空");
        }
    }

    // ========== ID校验 ==========

    /**
     * 校验ID有效（大于0）
     *
     * @param id        待校验ID
     * @param paramName 参数名称
     * @throws BusinessException ID无效时抛出
     */
    public static void validId(Long id, String paramName) {
        if (id == null || id <= 0) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "无效");
        }
    }

    /**
     * 校验ID有效（带默认参数名）
     *
     * @param id 待校验ID
     * @throws BusinessException ID无效时抛出
     */
    public static void validId(Long id) {
        validId(id, "ID");
    }

    /**
     * 校验ID有效（Integer版本）
     *
     * @param id        待校验ID
     * @param paramName 参数名称
     * @throws BusinessException ID无效时抛出
     */
    public static void validId(Integer id, String paramName) {
        if (id == null || id <= 0) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "无效");
        }
    }

    /**
     * 校验ID有效（Integer版本，带默认参数名）
     *
     * @param id 待校验ID
     * @throws BusinessException ID无效时抛出
     */
    public static void validId(Integer id) {
        validId(id, "ID");
    }

    // ========== 存在性校验 ==========

    /**
     * 校验对象存在（带默认错误消息）
     *
     * @param obj     待校验对象
     * @param paramName 参数名称
     * @param <T>     对象类型
     * @return 对象本身（用于链式调用）
     * @throws BusinessException 对象为空时抛出
     */
    public static <T> T checkExists(T obj, String paramName) {
        if (obj == null) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "不存在");
        }
        return obj;
    }

    // ========== 范围校验 ==========

    /**
     * 校验数值在指定范围内
     *
     * @param value    待校验值
     * @param min      最小值（包含）
     * @param max      最大值（包含）
     * @param paramName 参数名称
     * @throws BusinessException 数值超出范围时抛出
     */
    public static void inRange(Integer value, int min, int max, String paramName) {
        if (value == null || value < min || value > max) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "必须在" + min + "到" + max + "之间");
        }
    }

    /**
     * 校验数值在指定范围内（Long版本）
     *
     * @param value    待校验值
     * @param min      最小值（包含）
     * @param max      最大值（包含）
     * @param paramName 参数名称
     * @throws BusinessException 数值超出范围时抛出
     */
    public static void inRange(Long value, long min, long max, String paramName) {
        if (value == null || value < min || value > max) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "必须在" + min + "到" + max + "之间");
        }
    }

    /**
     * 校验数值为正数
     *
     * @param value    待校验值
     * @param paramName 参数名称
     * @throws BusinessException 数值不为正时抛出
     */
    public static void positive(Long value, String paramName) {
        if (value == null || value <= 0) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "必须大于0");
        }
    }

    /**
     * 校验数值为正数（Integer版本）
     *
     * @param value    待校验值
     * @param paramName 参数名称
     * @throws BusinessException 数值不为正时抛出
     */
    public static void positive(Integer value, String paramName) {
        if (value == null || value <= 0) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "必须大于0");
        }
    }

    // ========== 集合校验 ==========

    /**
     * 校验集合非空
     *
     * @param collection 待校验集合
     * @param paramName  参数名称
     * @throws BusinessException 集合为空时抛出
     */
    public static void notEmpty(java.util.Collection<?> collection, String paramName) {
        if (collection == null || collection.isEmpty()) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "不能为空");
        }
    }

    /**
     * 校验数组非空
     *
     * @param array     待校验数组
     * @param paramName 参数名称
     * @throws BusinessException 数组为空时抛出
     */
    public static void notEmpty(Object[] array, String paramName) {
        if (array == null || array.length == 0) {
            ExceptionHandlerUtil.throwBusinessException(paramName + "不能为空");
        }
    }
}
