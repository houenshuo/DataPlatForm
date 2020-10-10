package iot.ccnu.dataCollect.mqttServer.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
public class SpringUtil  {

    // 上下文对象
    public static ApplicationContext applicationContext;


    /**
     * 获取上下文对象
     * @return applicationContext
     */
    public static ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    /**
     * 判断上下文对象是否为空
     *
     * @return
     */
    public static boolean checkapplicationContext()
    {
        boolean flag = getApplicationContext() != null;
        if (!flag)
        {
            System.out.println("applicaitonContext未注入,实现ApplicationContextAware的类必须被spring管理");
        }
        return flag;
    }

    /**
     * 根据name获取bean
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name)
    {
        if (checkapplicationContext())
        {
            return (T)getApplicationContext().getBean(name);
        }
        else
        {
            return null;
        }
    }

    /**
     * 根据class 获取bean
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz)
    {
        if (checkapplicationContext())
        {
            return getApplicationContext().getBean(clazz);
        }
        else
        {
            return null;
        }
    }

    /**
     * 根据name,以及Clazz返回指定的Bean
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz)
    {
        if (checkapplicationContext())
        {
            return getApplicationContext().getBean(name, clazz);
        }
        else
        {
            return null;
        }
    }
}
