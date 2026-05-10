package com.lanf.seckill.service.index;

import com.lanf.seckill.model.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RSearch;
import org.redisson.api.RedissonClient;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.IndexOptions;
import org.redisson.api.search.index.IndexType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RediSearchService {

    @Autowired
    private RedissonClient redissonClient;

    private static final String SECKILL_INDEX_NAME = "seckillIdx";
    private static final String SECKILL_HASH_PREFIX = "seckill:item:";

    /**
     * 1. 创建秒杀商品搜索索引 (适配 Redisson 3.17+ 语法)
     */
    public void createSecKillIndex() {
        try {
            RSearch search = redissonClient.getSearch();

            // 1. 定义索引字段
            FieldIndex nameField = FieldIndex.text("itemTitle").weight(5.0); // 文本字段，权重5
            //numeric类型默认支持排序
            FieldIndex priceField = FieldIndex.numeric("seckillPrice");             // 价格字段
            FieldIndex activityIdField = FieldIndex.tag("secKillItemId");       // 标签字段（精确匹配）

            // 2. 定义索引选项：指定数据类型为 HASH，并设置 Key 的前缀
            IndexOptions options = IndexOptions.defaults()
                    .on(IndexType.HASH)
                    .prefix(SECKILL_HASH_PREFIX);

            // 3. 创建索引
            search.createIndex(SECKILL_INDEX_NAME, options, nameField, priceField, activityIdField);
            
            log.info("RediSearch 秒杀索引 [{}] 创建成功", SECKILL_INDEX_NAME);
        } catch (Exception e) {
            // 索引已存在时会报错，忽略即可
            if (e.getMessage() != null && !e.getMessage().contains("already exists")) {
                log.warn("RediSearch 索引创建警告: {}", e.getMessage());
            } else {
                log.debug("RediSearch 索引 [{}] 已存在", SECKILL_INDEX_NAME);
            }
        }
    }

    /**
     * 2. 将商品数据写入 Redis Hash
     */
    public void saveItemToHash(Long itemId, Map<String, Object> fields) {
        try {
            String hashKey = SECKILL_HASH_PREFIX + itemId;
            RMap<String, Object> map = redissonClient.getMap(hashKey);
            map.putAll(fields);

            map.expire(24, TimeUnit.HOURS); 
            log.debug("秒杀商品 Hash 写入成功: {}", hashKey);
        } catch (Exception e) {
            log.error("秒杀商品 Hash 写入失败, itemId: {}", itemId, e);
        }
    }

    /**
     * 3. 执行搜索
     */
    public SearchResult searchItems(String keyword, Long activityId, int pageNum, int pageSize) {
        try {
            RSearch search = redissonClient.getSearch();
            
            StringBuilder queryStr = new StringBuilder();
            if (keyword != null && !keyword.isEmpty()) {
                queryStr.append("@goodsName:").append(keyword);
            } else {
                queryStr.append("*");
            }

            if (activityId != null) {
                queryStr.append(" @activityId:{").append(activityId).append("}");
            }

//            Query query = new Query(queryStr.toString())
//                    .setSortBy("price", true)
//                    .limit((pageNum - 1) * pageSize, pageSize);

            return null;
        } catch (Exception e) {
            log.error("RediSearch 搜索异常", e);
            return null;
        }
    }

    /**
     * 删除商品索引
     */
    public void deleteItem(Long itemId) {
        try {
            String hashKey = SECKILL_HASH_PREFIX + itemId;
            redissonClient.getMap(hashKey).delete();
            log.info("秒杀商品已从 RediSearch 移除: {}", itemId);
        } catch (Exception e) {
            log.error("移除商品失败", e);
        }
    }
}
