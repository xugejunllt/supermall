package com.lanf.comment.service.cache;

import com.lanf.comment.model.document.CommentLikeDocument;
import com.lanf.comment.model.document.CommentStatsDocument;
import com.lanf.comment.repository.CommentLikeRepository;
import com.lanf.comment.repository.CommentStatsRepository;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
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

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    /**
     * Redis Hash key 前缀（存储点赞数）
     */
    private static final String LIKE_COUNT_HASH_KEY_PREFIX = "comment:like:count:";

    /**
     * Redis Set key 前缀（存储用户已点赞的评论ID，用于去重）
     */
    private static final String USER_LIKE_SET_KEY_PREFIX = "comment:user:like:%s:%s";

    /**
     * 分布式锁 key 前缀（用于初始化点赞数）
     */
    private static final String INIT_LOCK_KEY_PREFIX = "lock:comment:like:init:";

    /**
     * 分布式锁 key 前缀（用于初始化用户点赞 Set）
     */
    private static final String USER_LIKE_INIT_LOCK_PREFIX = "lock:comment:user:like:init:";

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
     * Lua 脚本：检查并添加点赞记录到 Set（原子操作）
     * <p>
     * KEYS[1] = set key (comment:user:like:{userId})
     * ARGV[1] = commentId
     * <p>
     * 返回: -1=Set不存在, 1=已存在(重复点赞), 0=新添加(成功)
     */
    private static final String CHECK_AND_ADD_LIKE_LUA =
            "local exists = redis.call('EXISTS', KEYS[1]); " +
                    "if exists == 0 then " +
                    "    return -1; " +
                    "end; " +
                    "local isMember = redis.call('SISMEMBER', KEYS[1], ARGV[1]); " +
                    "if isMember == 1 then " +
                    "    return 1; " +
                    "end; " +
                    "redis.call('SADD', KEYS[1], ARGV[1]); " +
                    "redis.call('EXPIRE', KEYS[1], " + EXPIRE_SECONDS + "); " +
                    "return 0;";

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

    // ==================== 用户点赞 Set 去重 ====================

    /**
     * 检查用户是否已点赞某条评论（带 Redis Set 去重）
     * <p>
     * 流程：
     * 1. 调用 Lua 脚本检查 Set 是否存在，以及 commentId 是否已存在
     * 2. 如果 Set 不存在（返回 -1），从 DB 加载用户所有点赞记录到 Set，然后重新判断
     * 3. 如果已存在（返回 1），说明重复点赞，返回 false
     * 4. 如果新添加（返回 0），返回 true
     *
     * @param userId    用户ID
     * @param commentId 评论ID
     * @return true=可以点赞（未重复）, false=已点赞（重复）
     */
    public boolean checkAndAddLike(Long goodsId, Long userId, Long commentId) {


        try {
            String key = buildUserLikeKey(goodsId, userId);

            // 1. Lua 脚本原子检查并添加
            int result = evalCheckAndAddLike(key, String.valueOf(commentId));

            if (result == -1) {
                // Set 不存在，从 DB 加载用户点赞记录
                loadUserLikesFromDb(goodsId, userId, key);
                // 重新判断
                result = evalCheckAndAddLike(key, String.valueOf(commentId));
            }

            if (result == 1) {
                // 重复点赞
                log.info("重复点赞, userId={}, commentId={}", userId, commentId);
                return false;
            }

            if (result == 0) {
                // 新点赞
                return true;
            }

            log.error("未知状态, userId={}, commentId={}, result={}", userId, commentId, result);
            return false;
        } catch (Exception e) {
            log.error("点赞去重检查异常, userId={}, commentId={}", userId, commentId, e);
            // 异常时放行（降级到 DB 层面幂等）
            return true;
        }
    }

    /**
     * 从用户点赞 Set 中移除评论ID（取消点赞）
     *
     * @param userId    用户ID
     * @param commentId 评论ID
     */
    public void removeLikeFromSet(Long goodsId, Long userId, Long commentId) {
        try {
            String key = buildUserLikeKey(goodsId, userId);
            redissonClient.getSet(key, StringCodec.INSTANCE).remove(String.valueOf(commentId));
            log.debug("从用户点赞 Set 移除, userId={}, commentId={}", userId, commentId);
        } catch (Exception e) {
            log.error("移除点赞记录异常, userId={}, commentId={}", userId, commentId, e);
        }
    }

    /**
     * 从 DB 加载用户所有点赞记录到 Redis Set
     *
     * @param userId 用户ID
     * @param key    Redis Set key
     */
    private void loadUserLikesFromDb(Long goodsId, Long userId, String key) {
        String lockKey = USER_LIKE_INIT_LOCK_PREFIX + userId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 3, TimeUnit.SECONDS);
            if (!locked) {
                log.debug("获取用户点赞初始化锁失败, userId={}", userId);

                return ;
            }
            // 双重检查
            if (redissonClient.getSet(key, StringCodec.INSTANCE).isExists()) {
                return ;
            }
            // 从 DB 加载
            List<CommentLikeDocument> likes = commentLikeRepository.findByGoodsIdAndUserId(goodsId, userId);

            RSet<String> set = redissonClient.getSet(key, StringCodec.INSTANCE);


            if ( IStringUtils.isEmpty(likes)){
                log.info("DB中点赞记录为空");
                //初始化为 -1
                set.add("-1");
                redissonClient.getBucket(key, StringCodec.INSTANCE).expire(EXPIRE_SECONDS, TimeUnit.SECONDS);
                return ;
            }

            for (CommentLikeDocument like : likes) {
                set.add(String.valueOf(like.getCommentId()));
            }
            // 设置过期时间
            redissonClient.getBucket(key, StringCodec.INSTANCE).expire(EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.info("用户点赞 Set 初始化完成, userId={}, size={}", userId,
                    likes.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("加载用户点赞记录被中断, userId={}", userId, e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 执行 Lua 脚本：检查并添加点赞记录
     *
     * @param key       Redis Set key
     * @param commentId 评论ID（字符串）
     * @return -1=Set不存在, 1=已存在, 0=新添加
     */
    private int evalCheckAndAddLike(String key, String commentId) {
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result = script.eval(
                RScript.Mode.READ_WRITE,
                CHECK_AND_ADD_LIKE_LUA,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(key),
                commentId
        );
        return result != null ? result.intValue() : -1;
    }

    /**
     * 批量判断用户是否点赞过这些评论
     * <p>
     * 优先从 Redis Set 批量判断，Set 不存在则从 DB 加载初始化后判断。
     *
     * @param goodsId    商品ID
     * @param userId     用户ID
     * @param commentIds 评论ID列表
     * @return Set<已点赞的评论ID>
     */
    public Set<Long> batchCheckUserLiked(Long goodsId, Long userId, List<Long> commentIds) {
        Set<Long> likedCommentIds = new HashSet<>();
        if (commentIds == null || commentIds.isEmpty() || userId == null) {
            return likedCommentIds;
        }
        try {
            String key = buildUserLikeKey(goodsId, userId);
            RSet<String> set = redissonClient.getSet(key, StringCodec.INSTANCE);

            // 如果 Set 不存在，从 DB 加载初始化
            if (!set.isExists()) {
                loadUserLikesFromDb(goodsId, userId, key);
            }

            // 批量判断每个 commentId 是否在 Set 中
            for (Long commentId : commentIds) {
                if (set.contains(String.valueOf(commentId))) {
                    likedCommentIds.add(commentId);
                }
            }
        } catch (Exception e) {
            log.error("批量判断用户点赞异常, goodsId={}, userId={}", goodsId, userId, e);
        }
        return likedCommentIds;
    }

    /**
     * 构建用户点赞 Set 的 Redis key
     *
     * @param userId 用户ID
     * @return Redis key
     */
    private String buildUserLikeKey(Long goodsId, Long userId) {
        return String.format(USER_LIKE_SET_KEY_PREFIX, goodsId, userId);
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