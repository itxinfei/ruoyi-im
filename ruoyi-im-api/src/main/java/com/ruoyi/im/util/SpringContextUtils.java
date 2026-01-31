package com.ruoyi.im.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring ApplicationContext 工具类
 * <p>
 * 用于在非Spring管理的类中获取Bean
 * </p>
 *
 * @author ruoyi
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取Bean
     *
     * @param name Bean名称
     * @param <T>  Bean类型
     * @return Bean实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    /**
     * 通过class获取Bean
     *
     * @param clazz Bean类
     * @param <T>   Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 通过name和class获取Bean
     *
     * @param name  Bean名称
     * @param clazz Bean类
     * @param <T>   Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 判断是否包含指定Bean
     *
     * @param name Bean名称
     * @return true-包含，false-不包含
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * 判断Bean是否是单例
     *
     * @param name Bean名称
     * @return true-单例，false-非单例
     */
    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    /**
     * 获取Bean的类型
     *
     * @param name Bean名称
     * @return Bean的Class类型
     */
    public static Class<?> getType(String name) {
        return applicationContext.getType(name);
    }

    /**
     * 获取Bean的别名
     *
     * @param name Bean名称
     * @return Bean的别名数组
     */
    public static String[] getAliases(String name) {
        return applicationContext.getAliases(name);
    }
}
