package com.tiancai.utils;

/**
 * 基于 ThreadLocal 封装的工具类，用于在同一次请求中安全地保存和获取当前登录用户的 ID
 */
public class BaseContext {
    private static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     * @param id
     */
    public static void setCurrentId(Integer id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前用户ID
     * @return
     */
    public static Integer getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前用户ID，在请求结束后调用以防内存泄漏
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }
}