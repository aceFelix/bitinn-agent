package com.itniuma.bitinn.utils;

/**
 * ThreadLocal 工具类
 *
 * @author aceFelix
 */
@SuppressWarnings("all")
public class ThreadLocalUtil {
    //提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    /**
     * 从 ThreadLocal 获取当前线程的用户数据。
     */
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    /**
     * 将用户数据存入当前线程的 ThreadLocal。
     */
    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    /**
     * 清除当前线程的 ThreadLocal 数据，防止内存泄漏。
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
