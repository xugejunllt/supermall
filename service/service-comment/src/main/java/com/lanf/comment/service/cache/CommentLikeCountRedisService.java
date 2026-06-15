package com.lanf.comment.service.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论点赞数 Redis 缓存服务
 * <p>
 * 使用 Redis Hash 结构存储每个商品下的评论点赞数：
 * <ul>
 *     <li>Key: comment:like:count:{goodsId}</li>
 *     <li>Field: {commentId}</li>
 *     <li>Value: 点赞数（String 格式存储，避免 Kryo 序列化问题）</li>
 * </ul>
 * 每次写入刷新过期时间为 7 天。
 * <p>
 * <b>设计说明：</b>使用 StringCodec 替代默认的 FstCodec/Kryo，彻底避免
 * {@code Encountered unregistered class ID} 序列化异常。
 *
 * @author lanf
 */
@Slf4j
@Service
public class CommentLikeCountRedisService {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * Redis Hash key 前缀
     */
    private static final String LIKE_COUNT_HASH_KEY_PREFIX = "comment:like:count:";

    /**
     * 过期时间（7天，单位：秒）
     */
    private static final long EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    /**
     * Lua 脚本：原子性 HINCRBY + EXPIRE（避免多次网络往返）
     * <p>
     * KEYS[1] = key
     * ARGV[1] = field
     * ARGV[2] = delta
     * ARGV[3] = expireSeconds
     */
    private static final String INCR_LIKE_COUNT_LUA =
            "local result = redis.call('HINCRBY', KEYS[1], ARGV[1], ARGV[2]); " +
            "redis.call('EXPIRE', KEYS[1], ARGV[3]); " +
            "return result;";

    /**
     * 增加点赞数（原子操作）
     *
     * @param goodsId   商品ID
     * @param commentId 评论ID
     */
    public void incrementLikeCount(Long goodsId, Long commentId) {
        executeHincrby(goodsId, commentId, 1);
    }

    /**
     * 减少点赞数（原子操作）
     *
     * @param goodsId   商品ID
     * @param commentId 评论ID
     */
    public void decrementLikeCount(Long goodsId, Long commentId) {
        executeHincrby(goodsId, commentId, -1);
    }

    /**
     * 获取指定评论的点赞数
     *
     * @param goodsId   商品ID
     * @param commentId 评论ID
     * @return 点赞数，不存在返回 null
     */
    public Long getLikeCount(Long goodsId, Long commentId) {
        try {
            String key = buildKey(goodsId);
            // 使用 StringCodec 避免 Kryo 序列化问题
            RMap<String, String> map = redissonClient.getMap(key, StringCodec.INSTANCE);
            String value = map.get(String.valueOf(commentId));
            return value != null ? Long.valueOf(value) : null;
        } catch (Exception e) {
            log.error("获取点赞数异常, goodsId={}, commentId={}", goodsId, commentId, e);
            return null;
        }
    }

    /**
     * 批量获取点赞数
     *
     * @param goodsId    商品ID
     * @param commentIds 评论ID列表
     * @return key=commentId, value=likeCount
     */
    public Map<Long, Long> batchGetLikeCount(Long goodsId, List<Long> commentIds) {
        Map<Long, Long> result = new HashMap<>();
        if (commentIds == null || commentIds.isEmpty()) {
            return result;
        }
        try {
            String key = buildKey(goodsId);
            // 使用 StringCodec 避免 Kryo 序列化问题
            RMap<String, String> map = redissonClient.getMap(key, StringCodec.INSTANCE);
            for (Long commentId : commentIds) {
                String value = map.get(String.valueOf(commentId));
                if (value != null) {
                    result.put(commentId, Long.valueOf(value));
                }
            }
        } catch (Exception e) {
            log.error("批量获取点赞数异常, goodsId={}", goodsId, e);
        }
        return result;
    }

    /**
     * 从 Redis 获取点赞数，未命中从 MongoDB 补充（初始化场景）
     *
     * @param goodsId      商品ID
     * @param commentId    评论ID
     * @param dbLikeCount  数据库中的点赞数
     * @return 点赞数
     */
    public Long getLikeCountWithFallback(Long goodsId, Long commentId, Long dbLikeCount) {
        Long redisCount = getLikeCount(goodsId, commentId);
        if (redisCount != null) {
            return redisCount;
        }
        return dbLikeCount != null ? dbLikeCount : 0L;
    }

    /**
     * 执行原子性 HINCRBY（Lua 脚本）
     *
     * @param goodsId   商品ID
     * @param commentId 评论ID
     * @param delta     增量（+1 或 -1）
     */
    private void executeHincrby(Long goodsId, Long commentId, long delta) {
        try {
            String key = buildKey(goodsId);
            String field = String.valueOf(commentId);

            // 使用 Lua 脚本原子执行 HINCRBY + EXPIRE，避免多次网络往返
            RScript script = redissonClient.getScript(StringCodec.INSTANCE);
            Long result = script.eval(
                    RScript.Mode.READ_WRITE,
                    INCR_LIKE_COUNT_LUA,
                    RScript.ReturnType.INTEGER,
                    Collections.singletonList(key),
                    field,
                    String.valueOf(delta),
                    String.valueOf(EXPIRE_SECONDS)
            );

            log.debug("点赞数更新成功, goodsId={}, commentId={}, delta={}, result={}",
                    goodsId, commentId, delta, result);
        } catch (Exception e) {
            log.error("更新点赞数异常, goodsId={}, commentId={}, delta={}", goodsId, commentId, delta, e);
        }
    }

    /**
     * 构建 Redis Hash key
     *
     * @param goodsId 商品ID
     * @return Redis key
     */
    private String buildKey(Long goodsId) {
        return LIKE_COUNT_HASH_KEY_PREFIX + goodsId;
    }
}