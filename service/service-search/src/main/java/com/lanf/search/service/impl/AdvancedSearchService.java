package com.lanf.search.service.impl;

import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.web.PageResult;
import com.lanf.search.model.bo.ScoredProduct;
import com.lanf.search.model.document.GoodsDocument;
import com.lanf.search.model.query.GoodsSearchQuery;
import com.lanf.search.model.vo.HomePageVO;
import com.lanf.security.utils.UserIdContext;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdvancedSearchService {

    @Autowired
    private ElasticsearchRestTemplate esTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private GoodsQualityService goodsQualityService; // 获取 fx2
    @Autowired
    private UserBehaviorService userBehaviorService; // 获取 fx3

    // 权重配置 (可从 Nacos 动态读取)
    @Value("${search.weight.text:0.5}")
    private double w1;
    @Value("${search.weight.quality:0.3}")
    private double w2;
    @Value("${search.weight.combo:0.2}")
    private double w3;

    /**
     * 执行完整的搜索链路
     */
    public PageResult<HomePageVO> advancedSearch(GoodsSearchQuery query) {
        String cacheKey = "search:result:" + buildCacheKey(query);
        
        // 4. 最终结果缓存：先查 Redis
        PageResult<HomePageVO> cachedResult = getCachedPageResult(cacheKey, query.getPage(), query.getPageSize());
        if (cachedResult != null) {
            return cachedResult;
        }

        // --- 2.1 召回阶段 (Recall) ---
        List<Long> recalledIds = recallPhase(query);
        if (recalledIds.isEmpty()) return PageResult.emptyResult();

        // --- 2.2 粗排阶段 (Rough Sort) ---
        // 从 ES 批量获取基础信息并计算初步得分，取 Top 200
        List<ScoredProduct> roughSortedList = roughSort(recalledIds, query.getKeyword());
        
        // --- 2.3 精排阶段 (Fine Sort) ---
        // 使用 LR 模型或复杂逻辑对 Top 200 进行精细打分
        List<ScoredProduct> fineSortedList = fineSort(roughSortedList, UserIdContext.getUserId());

        // --- 2.4 重排阶段 (Re-rank) ---
        // 插队：广告、新品、促销提权
        List<ScoredProduct> finalRankedList = reRank(fineSortedList, query);

        // --- 缓存商品 ID 列表到 Redis (设置过期时间，如 10 分钟) ---
        cacheFinalIds(cacheKey, finalRankedList.stream().map(ScoredProduct::getGoodsId).collect(Collectors.toList()));

        // --- 分页获取完整数据 ---
        return getPageDataFromEs(finalRankedList, query.getPage(), query.getPageSize());
    }

    /**
     * 2.1 召回：多路 Query 合并
     */
    private List<Long> recallPhase(GoodsSearchQuery query) {
        Set<Long> allIds = new LinkedHashSet<>();
        
        // 路数 1: 关键词倒排索引匹配 (ES Match Query)
        allIds.addAll(searchByKeyword(query.getKeyword()));
        
        // 路数 2: 类目预测 (根据搜索词找到分类，再查该分类下的热门商品)
        Long predictedCategoryId = predictCategory(query.getKeyword());
        if (predictedCategoryId != null) {
            allIds.addAll(searchByCategory(predictedCategoryId));
        }

        // 限制召回总数，防止后续压力过大
        return new ArrayList<>(allIds).stream().limit(1000).collect(Collectors.toList());
    }

    /**
     * 2.2 粗排：自定义打分模型 fx = w1*fx1 + w2*fx2 + w3*fx3
     */
    private List<ScoredProduct> roughSort(List<Long> ids, String keyword) {
        // 1. 从 ES 批量查询这些 ID 的基础信息和文本得分
        Map<Long, Double> textScores = getTextScoresFromEs(ids, keyword);
        
        List<ScoredProduct> products = new ArrayList<>();
        for (Long id : ids) {
            ScoredProduct p = new ScoredProduct();
            p.setGoodsId(id);
            
            // fx1: 文本得分
            p.setTextScore(textScores.getOrDefault(id, 0.0));
            
            // fx2: 质量得分 (发布时间、商家等级等)
            p.setQualityScore(goodsQualityService.getQualityScore(id));
            
            // fx3: 组合得分 (点击率、转化率)
            p.setComboScore(userBehaviorService.getComboScore(id));
            
            // 计算总分
            p.setTotalScore(p.getTextScore() * w1 + p.getQualityScore() * w2 + p.getComboScore() * w3);
            products.add(p);
        }

        // 快速排序取 Top 200
        return products.stream()
                .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
                .limit(200)
                .collect(Collectors.toList());
    }
    // 模拟 fx2: 商品质量得分 (发布时间、商家等级等)
    private double getQualityScore(Long goodsId) {
        // 实际应查询 MySQL 的 goods_ext 表或 Redis 缓存
        // 这里简单模拟：ID 越大代表发布越晚，分数越高
        return Math.random() * 10;
    }

    // 模拟 fx3: 组合/行为得分 (点击率、转化率)
    private double getComboScore(Long goodsId) {
        // 实际应查询 Redis 中的实时统计 ZSet
        return Math.random() * 10;
    }

    /**
     * 2.3 精排：LR 模型模拟
     */
    private List<ScoredProduct> fineSort(List<ScoredProduct> candidates, Long userId) {
        // 这里通常调用 Python/TensorFlow 服务的接口
        // 简单模拟：根据用户历史偏好微调分数
        candidates.forEach(p -> {
            double userPreferenceFactor = userBehaviorService.getUserPreference(userId, p.getGoodsId());
            p.setTotalScore(p.getTotalScore() * (1 + userPreferenceFactor));
        });
        
        // 重新排序
        return candidates.stream()
                .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
                .collect(Collectors.toList());
    }

    /**
     * 2.4 重排：业务规则插队
     */
    private List<ScoredProduct> reRank(List<ScoredProduct> list, GoodsSearchQuery query) {
        // 1. 广告插队：固定前 3 位
        // 2. 新品扶持：分数 * 1.2
        // 3. 促销提权：分数 * 1.5
        list.forEach(p -> {
            if (goodsQualityService.isNewProduct(p.getGoodsId())) {
                p.setTotalScore(p.getTotalScore() * 1.2);
            }
        });
        
        // 再次排序确保插队后顺序正确
        return list.stream()
                .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
                .collect(Collectors.toList());
    }

    /**
     * 从 ES 获取完整数据并实现分页
     */
    private PageResult<HomePageVO> getPageDataFromEs(List<ScoredProduct> rankedList, int page, int pageSize) {
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, rankedList.size());
        
        if (start >= rankedList.size()) return PageResult.emptyResult();

        // 截取当前页的 ID
        List<Long> pageIds = rankedList.subList(start, end).stream()
                .map(ScoredProduct::getGoodsId)
                .collect(Collectors.toList());

        // 从 ES 批量查询完整字段
        List<HomePageVO> vos = fetchFullGoodsFromEs(pageIds);
        
        return new PageResult<>(vos, vos.size(), rankedList.size());
    }
    // 缓存整个排序后的 ID 列表
    private void cacheFinalIds(String key, List<Long> ids) {
        redisTemplate.opsForValue().set(key, ids, 10, TimeUnit.MINUTES);
    }

    // 获取分页数据
    private PageResult<HomePageVO> getCachedPageResult(String key, int page, int pageSize) {
        List<Long> allIds = (List<Long>) redisTemplate.opsForValue().get(key);
        if (allIds == null || allIds.isEmpty()) return null;

        int start = (page - 1) * pageSize;
        if (start >= allIds.size()) return PageResult.emptyResult();

        int end = Math.min(start + pageSize, allIds.size());
        List<Long> pageIds = allIds.subList(start, end);

        // 注意：这里依然需要查一次 ES 获取最新的价格、库存等实时字段
        // 如果连价格都缓存，会导致库存/价格不一致，建议只缓存 ID 排序
        List<HomePageVO> vos = fetchFullGoodsFromEs(pageIds);
        return new PageResult<>(vos, vos.size(), allIds.size());
    }
    /**
     * 路数 1: 通过关键词查询倒排表，召回商品 ID
     */
    private List<Long> searchByKeyword(String keyword) {
        if ( IStringUtils.isEmpty(keyword)) {
            return Collections.emptyList();
        }

        // 1. 构建 MultiMatch Query：同时匹配 商品名称 和 提示词标签
        // 通过在字段名后添加 ^权重 来设置 boosting
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(keyword,
                        GoodsDocument.GOODS_NAME + "^1.5",      // 商品名称权重 1.5
                        GoodsDocument.PROMPT_WORD_LABEL + "^1.0") // 提示词标签权重 1.0
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS) // 取匹配度最高的那个字段的分数
                .minimumShouldMatch("80%");

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(multiMatchQuery)
                .withFields("_id") // 【关键优化】：只返回 ID，不返回源文档
                .withPageable(PageRequest.of(0, 500)) // 限制单次召回数量
                .build();

        // 2. 执行搜索
        SearchHits<GoodsDocument> searchHits = esTemplate.search(query, GoodsDocument.class);

        // 3. 提取 ID
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getGoodsId())
                .collect(Collectors.toList());
    }
    /**
     * 路数 2: 类目预测召回 (先猜分类，再查该分类下的热销品)
     */
    private List<Long> searchByCategory(Long categoryId) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery(GoodsDocument.THREE_LEVEL_CATEGORY_ID, categoryId))
                        .must(QueryBuilders.termQuery(GoodsDocument.UP_DOWN_STATUS, 0))) // 只查上架
                .withSort(SortBuilders.fieldSort(GoodsDocument.SALES).
                        order(SortOrder.DESC)) // 按销量排序
                .withFields("_id")
                .withPageable(PageRequest.of(0, 200))
                .build();

        SearchHits<GoodsDocument> searchHits = esTemplate.search(query, GoodsDocument.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getGoodsId())
                .collect(Collectors.toList());
    }

    /**
     * 辅助方法：模拟类目预测 (实际项目中通常调用 AI 模型接口)
     */
    private Long predictCategory(String keyword) {
        // 简单实现：如果包含“手机”，则预测为手机分类 ID 1001
        if (keyword.contains("手机")) return 1001L;
        if (keyword.contains("电脑")) return 1002L;
        return null;
    }

    /**
     * 辅助方法：获取扩展词/联想词 (实际项目中通常从 Redis 或字典表获取)
     */
    private List<String> getExpandWords(String keyword) {
        // 简单实现：手动定义一些同义词
        //通常管理系统里 给每个商品定义一些同义词
        if ("手机".equals(keyword)) {
            return Arrays.asList("智能手机", "移动电话");
        }
        return Collections.emptyList();
    }
    /**
     * 从 ES 批量获取指定商品 ID 的文本匹配得分 (fx1)
     *
     * @param ids 召回阶段产生的商品 ID 集合
     * @param keyword 用户搜索的关键词
     * @return Map<商品ID, 文本得分>
     */
    private Map<Long, Double> getTextScoresFromEs(List<Long> ids, String keyword) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 构建查询：既要匹配关键词，又要限制在召回的 ID 范围内
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 核心：计算这些商品相对于该关键词的相关性得分
        boolQuery.must(QueryBuilders.matchQuery(GoodsDocument.GOODS_NAME, keyword)
                .minimumShouldMatch("80%"));

        // 过滤：只在我们召回的这些 ID 里找
        boolQuery.filter(QueryBuilders.idsQuery().addIds(
                ids.stream().map(String::valueOf).toArray(String[]::new)
        ));

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withFields("_id") // 依然只需要 ID，分数在 SearchHit 对象里
                .withPageable(PageRequest.of(0, ids.size())) // 确保能取回所有结果
                .build();

        // 2. 执行查询
        SearchHits<GoodsDocument> searchHits = esTemplate.search(query, GoodsDocument.class);

        // 3. 提取 ID 和对应的 Score
        Map<Long, Double> scoreMap = new HashMap<>();
        for (SearchHit<GoodsDocument> hit : searchHits.getSearchHits()) {
            Long goodsId = hit.getContent().getGoodsId();
            float score = hit.getScore(); // 获取 ES 计算出的 _score
            scoreMap.put(goodsId, (double) score);
        }

        return scoreMap;
    }




        /**
     * 根据 ID 列表从 ES 批量获取完整的商品展示数据
     */
    private List<HomePageVO> fetchFullGoodsFromEs(List<Long> pageIds) {
        if (pageIds == null || pageIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 构建 IDs 查询
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.idsQuery().addIds(
                        pageIds.stream().map(String::valueOf).toArray(String[]::new)
                ))
                // 2. 【关键优化】：只获取展示需要的字段，减少网络传输和 ES 压力
                .withSourceFilter(new FetchSourceFilter(
                        new String[]{
                                GoodsDocument.GOODS_ID,
                                GoodsDocument.GOODS_NAME,
                                GoodsDocument.MAIN_IMAGE,
                                GoodsDocument.PRICE,
                                GoodsDocument.SALES,
                                GoodsDocument.EXTENDED_TAGS
                        },
                        null))
                .build();

        // 3. 执行批量查询
        SearchHits<GoodsDocument> searchHits = esTemplate.search(query, GoodsDocument.class);

        // 4. 转换为 VO 对象
        List<HomePageVO> voList = searchHits.getSearchHits().stream()
                .map(hit -> convertToVO(hit.getContent()))
                .collect(Collectors.toList());

        // 5. 【重要】：保持排序一致性
        // ES 的 idsQuery 返回的顺序可能和传入的 ID 顺序不一致，需要按照 pageIds 的顺序重新排列
        Map<Long, HomePageVO> voMap = voList.stream()
                .collect(Collectors.toMap(HomePageVO::getGoodsId, vo -> vo));

        return pageIds.stream()
                .map(voMap::get)
                .filter(Objects::nonNull) // 过滤掉可能存在但已被删除的商品
                .collect(Collectors.toList());
    }

    /**
     * 辅助方法：将 Document 转换为 VO
     */
    private HomePageVO convertToVO(GoodsDocument document) {
        if (document == null) return null;
        HomePageVO vo = new HomePageVO();
        vo.setGoodsId(document.getGoodsId());
        vo.setGoodsName(document.getGoodsName());
        vo.setMainImage(document.getMainImage());
        vo.setPrice(document.getPrice());
        vo.setSales(document.getSales());
        vo.setExtendedTags(document.getExtendedTags());
        return vo;
    }

        /**
     * 构建缓存 Key
     * 确保相同的搜索条件生成相同的 Key
     */
    private String buildCacheKey(GoodsSearchQuery query) {
        // 使用 StringBuilder 拼接关键过滤条件和排序规则
        StringBuilder sb = new StringBuilder();

        // 1. 关键词 (核心)
        sb.append("kw:").append(query.getKeyword() != null ? query.getKeyword() : "all");
        sb.append("|");

        // 2. 分类与品牌 (精确匹配项)
        sb.append("cat:").append(query.getCategoryId() != null ? query.getCategoryId() : 0);
        sb.append("|brand:").append(query.getBrandId() != null ? query.getBrandId() : 0);
        sb.append("|shop:").append(query.getShopId() != null ? query.getShopId() : 0);
        sb.append("|");

        // 3. 价格区间 (范围匹配项)
        sb.append("price:").append(query.getMinPrice() != null ? query.getMinPrice() : 0);
        sb.append("-").append(query.getMaxPrice() != null ? query.getMaxPrice() : "max");
        sb.append("|");

        // 4. 排序规则 (不同排序结果不同，必须区分)
        sb.append("sort:").append(query.getSortField() != null ? query.getSortField() : "default");
        sb.append("_").append(query.getSortOrder() != null ? query.getSortOrder() : "desc");

        // 5. 为了安全，可以对整个字符串做 MD5 加密，防止 Key 过长
        // return DigestUtils.md5DigestAsHex(sb.toString().getBytes());
        return sb.toString();
    }

}
