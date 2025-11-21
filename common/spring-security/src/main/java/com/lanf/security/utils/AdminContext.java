package com.lanf.security.utils;


/**
 * 用户上下文工具类
 * 用于在同一个线程中共享用户信息
 */
public class AdminContext {
    
    private static final ThreadLocal<Long> USER_ID_CONTEXT = new ThreadLocal<>();
    

}