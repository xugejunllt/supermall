package com.lanf.comment.service.cache;

import com.lanf.comment.model.document.CommentStatsDocument;
import com.lanf.comment.repository.CommentStatsRepository;
import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private CommentStatsRepository commentStatsRepository;

    /**
     * Redis Hash key 前缀
     */
    private static final String LIKE_COUNT_HASH_KEY_PREFIX = "comment:like:count:";

    /**
     * 分布式锁 key 前缀（用于初始化点赞数）
     */
    private static final String INIT_LOCK_KEY_PREFIX = "lock:comment:like:init:";

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

            // 批量获取（减少网络往返）
            Set<String> fields = commentIds.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.toSet());
            Map<String, String> allValues = map.getAll(fields);

            for (Map.Entry<String, String> entry : allValues.entrySet()) {
                result.put(Long.valueOf(entry.getKey()), Long.valueOf(entry.getValue()));
            }
        } catch (Exception e) {
            log.error("批量获取点赞数异常, goodsId={}", goodsId, e);
        }
        return result;
    }

    /**
     * 从 Redis 获取点赞数，未命中从 MongoDB 补充（初始化场景）
     *
     * @param goodsId     商品ID
     * @param commentId   评论ID
     * @param dbLikeCount 数据库中的点赞数
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
     * 执行原子性 HINCRBY（带初始化判断）
     * <p>
     * 流程：
     * 1. 判断 field 是否存在于 hash 中
     * 2. 不存在则获取分布式锁，一个线程从 DB 获取点赞数并写入 Redis
     * 3. 获取锁失败的线程阻塞 50ms，等待初始化完成后执行 HINCRBY
     *
     * @param goodsId   商品ID
     * @param commentId 评论ID
     * @param delta     增量（+1 或 -1）
     */
    private void executeHincrby(Long goodsId, Long commentId, long delta) {
        try {
            String key = buildKey(goodsId);
            String field = String.valueOf(commentId);

            // 1. 判断 field 是否在 hash 中存在
            RMap<String, String> map = redissonClient.getMap(key, StringCodec.INSTANCE);
            if (!map.containsKey(field)) {
                // 2. field 不存在，尝试获取分布式锁初始化
                String lockKey = INIT_LOCK_KEY_PREFIX + commentId;
                RLock lock = redissonClient.getLock(lockKey);

                boolean locked = false;
                try {
                    // 尝试获取锁：不等待（0秒），锁自动释放时间 5 秒
                    locked = lock.tryLock(0, 5, TimeUnit.SECONDS);

                    if (locked) {
                        try {
                            // 双重检查：再次确认 field 不存在（可能被其他线程初始化了）
                            if (!map.containsKey(field)) {
                                // 从 DB 获取点赞数并写入 Redis
                                Long dbLikeCount = getLikeCountFromDb(commentId);
                                map.put(field, String.valueOf(dbLikeCount));
                                log.info("初始化评论点赞数缓存, commentId={}, dbLikeCount={}", commentId, dbLikeCount);
                            }
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        // 3. 获取锁失败，阻塞 50ms 等待初始化线程完成
                        log.debug("获取初始化锁失败, commentId={}, 等待 50ms", commentId);
                        Thread.sleep(50);
                        // 兜底：如果 50ms 后仍不存在 抛出异常
                        if (!map.containsKey(field)) {
                            /**
                             * 打印error日志
                             */
                            log.error("初始化评论点赞数失败, commentId={}, 兜底初始化", commentId);
                            throw new BizException("服务繁忙,请稍后再试!");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("获取分布式锁被中断, commentId={}", commentId, e);
                    throw new BizException("获取分布式锁被中断");
                }
            }

            // 4. 执行 HINCRBY（此时 field 一定存在）
            doHincrby(key, field, delta);

        } catch (Exception e) {
            log.error("更新点赞数异常, goodsId={}, commentId={}, delta={}", goodsId, commentId, delta, e);
            throw e;
        }
    }

    /**
     * 实际执行 HINCRBY 的 Lua 脚本
     */
    private void doHincrby(String key, String field, long delta) {
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
        log.debug("点赞数更新成功, key={}, field={}, delta={}, result={}", key, field, delta, result);
    }

    /**
     * 从 DB 获取评论点赞数
     *
     * @param commentId 评论ID
     * @return 点赞数
     */
    private Long getLikeCountFromDb(Long commentId) {

        CommentStatsDocument stats = commentStatsRepository.findByCommentId(commentId);
        if (stats != null && stats.getLikeCount() != null) {
            return stats.getLikeCount();
        }

        return 0L;
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