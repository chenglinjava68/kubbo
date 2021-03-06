package com.sogou.map.kubbo.common.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sogou.map.kubbo.common.logger.impl.JdkLoggerAdapter;
import com.sogou.map.kubbo.common.logger.impl.Log4j2LoggerAdapter;
import com.sogou.map.kubbo.common.logger.impl.Log4jLoggerAdapter;
import com.sogou.map.kubbo.common.logger.impl.Slf4jLoggerAdapter;

/**
 * 日志输出器工厂
 * 
 * @author liufuliang
 */
public class LoggerFactory {

    private static volatile LoggerAdapter LOGGER_ADAPTER;
    
    private static final ConcurrentMap<String, WrappedLogger> LOGGERS = new ConcurrentHashMap<String, WrappedLogger>();

    // 查找常用的日志框架
    static {
        String logger = System.getProperty("kubbo.logger");
        if ("log4j2".equals(logger)) {
            setLoggerAdapter(new Log4j2LoggerAdapter());
        } else if("log4j".equals(logger)){
            setLoggerAdapter(new Log4jLoggerAdapter());
        } else if ("slf4j".equals(logger)) {
            setLoggerAdapter(new Slf4jLoggerAdapter());
        } else if ("jdk".equals(logger)) {
            setLoggerAdapter(new JdkLoggerAdapter());
        } else {
            try {
                setLoggerAdapter(new Log4j2LoggerAdapter());
            } catch (Throwable e1) {
                try {
                    setLoggerAdapter(new Log4jLoggerAdapter());
                } catch (Throwable e2) {
                    try {
                        setLoggerAdapter(new Slf4jLoggerAdapter());
                    } catch (Throwable e3) {
                        setLoggerAdapter(new JdkLoggerAdapter());
                    }
                }
            }
        }
    }

    /**
     * 设置日志输出器供给器
     * 
     * @param loggerAdapter
     *            日志输出器供给器
     */
    public static void setLoggerAdapter(LoggerAdapter loggerAdapter) {
        if (loggerAdapter != null) {
            Logger logger = loggerAdapter.getLogger(LoggerFactory.class.getName());
            logger.info(WrappedLogger.LOG_HEADER + "Using logger: " + loggerAdapter.getClass().getName());
            LoggerFactory.LOGGER_ADAPTER = loggerAdapter;
            for (Map.Entry<String, WrappedLogger> entry : LOGGERS.entrySet()) {
                entry.getValue().setLogger(LOGGER_ADAPTER.getLogger(entry.getKey()));
            }
        }
    }

    /**
     * 获取日志输出器
     * 
     * @param key
     *            分类键
     * @return 日志输出器, 后验条件: 不返回null.
     */
    public static Logger getLogger(Class<?> key) {
        WrappedLogger logger = LOGGERS.get(key.getName());
        if (logger == null) {
            LOGGERS.putIfAbsent(key.getName(), new WrappedLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = LOGGERS.get(key.getName());
        }
        return logger;
    }

    /**
     * 获取日志输出器
     * 
     * @param key
     *            分类键
     * @return 日志输出器, 后验条件: 不返回null.
     */
    public static Logger getLogger(String key) {
        WrappedLogger logger = LOGGERS.get(key);
        if (logger == null) {
            LOGGERS.putIfAbsent(key, new WrappedLogger(LOGGER_ADAPTER.getLogger(key)));
            logger = LOGGERS.get(key);
        }
        return logger;
    }
    
    private LoggerFactory() {
    }

}