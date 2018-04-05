package com.bdcor.services.mail.bean;

import com.bdcor.services.mail.exception.ServiceException;
import org.springframework.core.env.Environment;

/**
 * Description:
 * <pre>
 *     静态获取配置文件信息
 * </pre>
 * Author: huangrupeng
 * Create: 17/5/9 下午1:56
 */
public class PropertiesStaticGetter {

    static Environment env = null;

    public PropertiesStaticGetter(Environment e) {
        env = e;
    }

    public static String getProperty(String key) {
        if(env == null) {
            throw new ServiceException("bean未初始化");
        }
        return env.getProperty(key);
    }
}
