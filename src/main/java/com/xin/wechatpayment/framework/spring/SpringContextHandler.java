package com.xin.wechatpayment.framework.spring;


import com.xin.wechatpayment.framework.log.Log;
import com.xin.wechatpayment.framework.log.constant.LogScene;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHandler implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHandler.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        try {
            return (T) applicationContext.getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            Log.e(LogScene.SYSTEM_ERROR,"SpringContextHandler.getBean(name) -> "+name);
            return null;
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            Log.e(LogScene.SYSTEM_ERROR,"SpringContextHandler.getBean(name) -> "+clazz.getName());
            return null;
        }
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        try {
            return applicationContext.getBean(name, clazz);
        } catch (NoSuchBeanDefinitionException e) {
            Log.e(LogScene.SYSTEM_ERROR,"SpringContextHandler.getBean(name) -> "+name+"&clazz:"+clazz.getName());
            return null;
        }
    }
}
