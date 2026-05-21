package com.lanf.constant.utils;

/**
 * 消息链路层级工具类
 * <p>使用 ThreadLocal 记录当前线程的消息发送/消费层级</p>
 *
 * @author system
 * @since 2024-01-15
 */
public class MessageLevelUtils {

    private static final ThreadLocal<Integer> MESSAGE_LEVEL_HOLDER = new ThreadLocal<>();
    /**
     * 默认初始层级
     */
    private static final int DEFAULT_LEVEL = 1;

    /**
     * 获取当前消息层级
     *
     * @return 消息层级，如果未设置则返回 1
     */
    public static int getLevel() {
        Integer level = MESSAGE_LEVEL_HOLDER.get();
        return level != null ? level : DEFAULT_LEVEL;
    }

    /**
     * 设置消息层级
     *
     * @param level 消息层级
     */
    public static void setLevel(int level) {
        MESSAGE_LEVEL_HOLDER.set(level);
    }

    /**
     * 递增消息层级（用于消费者再次发送消息）
     *
     * @return 递增后的层级
     */
    public static int incrementLevel() {
        int currentLevel = getLevel();
        int newLevel = currentLevel + 1;
        MESSAGE_LEVEL_HOLDER.set(newLevel);
        return newLevel;
    }

    /**
     * 重置消息层级
     */
    public static void reset() {
        MESSAGE_LEVEL_HOLDER.remove();
    }

    /**
     * 清除消息层级（防止内存泄漏）
     */
    public static void clear() {
        MESSAGE_LEVEL_HOLDER.remove();
    }
}
