package com.lanf.search.service.impl;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.search.model.document.GoodsDocument;
import com.lanf.search.model.query.GoodsSearchQuery;
import com.lanf.search.model.query.HomePageQuery;
import com.lanf.search.model.query.SuggestQuery;
import com.lanf.search.model.vo.HomePageVO;
import com.lanf.search.model.vo.SuggestVO;
import com.lanf.search.repository.GoodsRepository;
import com.lanf.search.service.IGoodsDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IGoodsDocumentServiceImpl implements IGoodsDocumentService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private RedissonCacheService redissonCacheService;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final String SUGGEST_CACHE_KEY_PREFIX = "search:suggest:";



    @Override
    public PageResult<HomePageVO> pageHomePage(HomePageQuery query) {
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 0上架
       boolQuery.filter(QueryBuilders.termQuery(GoodsDocument.UP_DOWN_STATUS, 0));

        // 设置查询和排序
        queryBuilder.withQuery(boolQuery)
                .withSort(SortBuilders.fieldSort(GoodsDocument.UPDATE_TIME).order(SortOrder.DESC))
                //第一页页码 从0开始 所以-1
                .withPageable(PageRequest.of(query.getPage()-1, query.getPageSize()));

        // 设置返回字段（提高查询性能）
   queryBuilder.withSourceFilter(new FetchSourceFilter(
        new String[]{
                GoodsDocument.GOODS_ID,
                GoodsDocument.GOODS_NAME,
                GoodsDocument.MAIN_IMAGE,
                GoodsDocument.PRICE,
                GoodsDocument.EXTENDED_TAGS
        },
        null));


        NativeSearchQuery searchQuery = queryBuilder.build();

        // 执行查询
        SearchHits<GoodsDocument> searchHits = elasticsearchRestTemplate.search(
                searchQuery, GoodsDocument.class);

        // 转换结果
        List<HomePageVO> content = searchHits.getSearchHits().stream()
                .map(hit -> convertToVO(hit.getContent()))
                .collect(Collectors.toList());
        return new PageResult<>(content,
                content.size(), searchHits.getTotalHits());
    }

    @Override
    public PageResult<HomePageVO> searchGoods(GoodsSearchQuery query) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1. 全文检索 (匹配商品名称)
        if (StringUtils.hasText(query.getKeyword())) {
            // 使用 match_query 利用 IK 分词器进行智能匹配
            boolQuery.must(QueryBuilders.matchQuery(GoodsDocument.GOODS_NAME, query.getKeyword())
                    .minimumShouldMatch("80%")); // 至少匹配 80% 的分词
        }

        // 2. 精确过滤 (Filter 上下文，不计算得分，性能更高且有缓存)
        boolQuery.filter(QueryBuilders.termQuery(GoodsDocument.UP_DOWN_STATUS, query.getUpDownStatus()));
        
        if (query.getCategoryId() != null) {
            boolQuery.filter(QueryBuilders.termQuery(GoodsDocument.THREE_LEVEL_CATEGORY_ID, query.getCategoryId()));
        }
        if (query.getBrandId() != null) {
            boolQuery.filter(QueryBuilders.termQuery(GoodsDocument.BRAND_ID, query.getBrandId()));
        }
        if (query.getShopId() != null) {
            boolQuery.filter(QueryBuilders.termQuery(GoodsDocument.SHOP_ID, query.getShopId()));
        }

        // 3. 价格区间查询
        if (query.getMinPrice() != null || query.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery(GoodsDocument.PRICE)
                    .gte(query.getMinPrice() != null ? query.getMinPrice() : 0)
                    .lte(query.getMaxPrice() != null ? query.getMaxPrice() : Double.MAX_VALUE));
        }

        // 4. 构建查询
        queryBuilder.withQuery(boolQuery);

        // 5. 排序逻辑
        String sortField = query.getSortField();
        String sortOrder = query.getSortOrder();
        SortOrder order = "asc".equalsIgnoreCase(sortOrder) ? SortOrder.ASC : SortOrder.DESC;

        if ("price".equalsIgnoreCase(sortField)) {
            queryBuilder.withSort(SortBuilders.fieldSort(GoodsDocument.PRICE).order(order));
        } else if ("sales".equalsIgnoreCase(sortField)) {
            queryBuilder.withSort(SortBuilders.fieldSort(GoodsDocument.SALES).order(order));
        } else {
            // 默认按更新时间或相关度排序
            queryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
            queryBuilder.withSort(SortBuilders.fieldSort(GoodsDocument.UPDATE_TIME).order(SortOrder.DESC));
        }

        // 6. 分页与字段裁剪
        queryBuilder.withPageable(PageRequest.of(query.getPage() - 1, query.getPageSize()));
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{
                        GoodsDocument.GOODS_ID,
                        GoodsDocument.GOODS_NAME,
                        GoodsDocument.MAIN_IMAGE,
                        GoodsDocument.PRICE,
                        GoodsDocument.SALES,
                        GoodsDocument.EXTENDED_TAGS
                }, null));

        // 7. 执行查询
        SearchHits<GoodsDocument> searchHits = elasticsearchRestTemplate.search(
                queryBuilder.build(), GoodsDocument.class);

        // 8. 结果转换
        List<HomePageVO> content = searchHits.getSearchHits().stream()
                .map(hit -> convertToVO(hit.getContent()))
                .collect(Collectors.toList());

        return new PageResult<>(content, content.size(), searchHits.getTotalHits());
    }


    @Override
    public List<SuggestVO> getSuggestions(SuggestQuery query) {
        if (!StringUtils.hasText(query.getPrefix())) {
            return Collections.emptyList();
        }

        String cacheKey = SUGGEST_CACHE_KEY_PREFIX + query.getPrefix() 
                + ":cat:" + (query.getCategoryId() != null ? query.getCategoryId() : "all")
                + ":size:" + query.getSize();

//        // 尝试从缓存获取
//        List<SuggestVO> cachedSuggestions = redissonCacheService.get(cacheKey);
//        if (cachedSuggestions != null && !cachedSuggestions.isEmpty()) {
//            return cachedSuggestions;
//        }

        List<SuggestVO> suggestions = new ArrayList<>();

        // 1. 使用 Completion Suggester 获取自动补全建议
        suggestions.addAll(getCompletionSuggestions(query));

        // 2. 使用 Phrase Suggester 获取短语纠正建议（针对拼写错误）
        if (query.getSize() > suggestions.size()) {
            suggestions.addAll(getPhraseSuggestions(query));
        }

        // 3. 去重：按文本内容去重，保留最高分
        Map<String, SuggestVO> uniqueSuggestions = new LinkedHashMap<>();
        for (SuggestVO suggest : suggestions) {
            String text = suggest.getText();
            
            if (!uniqueSuggestions.containsKey(text)) {
                uniqueSuggestions.put(text, suggest);
            } else {
                SuggestVO existing = uniqueSuggestions.get(text);
                
                // 比较得分，保留更高的
                if (suggest.getScore() > existing.getScore()) {
                    uniqueSuggestions.put(text, suggest);
                } else if (Double.compare(suggest.getScore(), existing.getScore()) == 0) {
                    // 得分相同时，优先级：completion > phrase
                    if ("completion".equals(suggest.getType()) && !"completion".equals(existing.getType())) {
                        uniqueSuggestions.put(text, suggest);
                    }
                }
            }
        }

        // 4. 按得分降序排序并截取指定数量
        List<SuggestVO> result = uniqueSuggestions.values().stream()
                .sorted((a, b) -> {
                    int scoreCompare = Double.compare(b.getScore(), a.getScore());
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    // 得分相同，按类型排序：completion 优先
                    if ("completion".equals(a.getType()) && !"completion".equals(b.getType())) {
                        return -1;
                    }
                    return 1;
                })
                .limit(query.getSize())
                .collect(Collectors.toList());

//        // 5. 缓存结果，有效期 5 分钟
//        if (!result.isEmpty()) {
//            redissonCacheService.set(cacheKey, result, 5, TimeUnit.MINUTES);
//        }

        return result;
    }

    /**
     * 使用 Completion Suggester 获取自动补全建议（Spring Boot 2.7 使用原生 ES Client）
     *
     * ElasticsearchRestTemplate 有版本兼容性问题
     */
    private List<SuggestVO> getCompletionSuggestions(SuggestQuery query) {
        List<SuggestVO> suggestions = new ArrayList<>();

        try {
            // 1. 构建搜索请求 - 对应 POST /goods_index/_search
            SearchRequest searchRequest = new SearchRequest("goods_index");
            
            // 2. 构建 Completion Suggestion
            CompletionSuggestionBuilder completionSuggestion = SuggestBuilders
                    .completionSuggestion("suggest")
                    .prefix(query.getPrefix())
                    .skipDuplicates(true)
                    .size(query.getSize() > 0 ? query.getSize() : 10);

            // 3. 添加到 SuggestBuilder
            final String SUGGEST_NAME = "goods_suggest";
            SuggestBuilder suggestBuilder = new SuggestBuilder();
            suggestBuilder.addSuggestion(SUGGEST_NAME, completionSuggestion);
            
            // 4. 设置到 SearchSourceBuilder
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.suggest(suggestBuilder);
            
            // ✅ 关键：必须将 sourceBuilder 设置到 searchRequest
            searchRequest.source(sourceBuilder);

            // 打印生成的 DSL（用于调试）
            log.info("Completion Suggest Query DSL: {}", sourceBuilder.toString());

            // 5. 执行查询
            SearchResponse searchResponse = restHighLevelClient.search(
                    searchRequest, RequestOptions.DEFAULT);

            // 6. 检查响应状态
            log.info("Search Response Status: {}", searchResponse.status());
            log.info("Total Hits: {}", searchResponse.getHits().getTotalHits());

            // 7. 解析结果
            Suggest suggest = searchResponse.getSuggest();
            if (suggest != null) {
                // 获取名为 "test_suggest" 的 suggestion
                CompletionSuggestion completionSuggest = suggest.getSuggestion(SUGGEST_NAME);
                if (completionSuggest != null) {

                    // 临时列表用于收集所有选项
                    List<SuggestVO> tempSuggestions = new ArrayList<>();
                    
                    for (CompletionSuggestion.Entry entry : completionSuggest.getEntries()) {

                        for (CompletionSuggestion.Entry.Option option : entry.getOptions()) {
                            String text = option.getText().string();
                            /**
                             * ES 已经按 weight 排序，score 反映 weight
                             * 这个 weight写入时可以指定，默认为 1
                             */
                            float score = option.getScore();
                            
                            // ✅ 获取 weight（如果需要）
                            // weight 存储在 _source 中，需要通过 ID 查询获取
                            // 或者在构建 completion 时使用 score 字段模拟
                            tempSuggestions.add(SuggestVO.builder()
                                    .text(text)
                                    .type("completion")
                                    .count(0L)
                                    .score((double) score)
                                    .build());
                        }
                    }
                    
                    // ✅ 按 score 降序排序（）
                    tempSuggestions.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
                    
                    // 添加到最终结果
                    suggestions.addAll(tempSuggestions);
                    
                } else {
                    log.warn("❌ No completion suggestion found for 'test_suggest'");
                }
            } else {
                log.warn("❌ Search response has no suggestions for prefix: {}", query.getPrefix());
            }
        } catch (Exception e) {
            log.error("❌ Completion Suggester 获取建议词失败, prefix: {}", query.getPrefix(), e);
        }

        return suggestions;
    }

    /**
     * 使用 Phrase Suggester 获取短语纠正建议
     */
    private List<SuggestVO> getPhraseSuggestions(SuggestQuery query) {
        List<SuggestVO> suggestions = new ArrayList<>();

        try {
            // 构建 Phrase Suggestion 查询
            PhraseSuggestionBuilder phraseSuggestion = SuggestBuilders
                    .phraseSuggestion("goodsName")
                    .text(query.getPrefix())
                    .maxErrors(2)
                    .confidence(1.0f)
                    .size(query.getSize());

            SuggestBuilder suggestBuilder = new SuggestBuilder();
            suggestBuilder.addSuggestion("phrase_suggest", phraseSuggestion);

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withSuggestBuilder(suggestBuilder)
                    .build();

            // 执行查询
            org.springframework.data.elasticsearch.core.SearchHits<GoodsDocument> searchHits =
                    elasticsearchRestTemplate.search(searchQuery, GoodsDocument.class);

            if (searchHits != null && !searchHits.isEmpty()) {
                try {
                    Object suggestObj = searchHits.getSuggest();
                    if (suggestObj != null) {
                        PhraseSuggestion phraseSuggest = (PhraseSuggestion) suggestObj;
                        processPhraseSuggestion(phraseSuggest, suggestions);
                    }
                } catch (Exception e) {
                    log.warn("无法直接获取 Phrase Suggest");
                }
            }
        } catch (Exception e) {
            log.error("Phrase Suggester 获取建议词失败", e);
        }

        return suggestions;
    }

    /**
     * 处理 Phrase Suggestion 结果
     */
    private void processPhraseSuggestion(PhraseSuggestion phraseSuggest, List<SuggestVO> suggestions) {
        if (phraseSuggest == null) {
            return;
        }

        for (PhraseSuggestion.Entry entry : phraseSuggest.getEntries()) {
            for (PhraseSuggestion.Entry.Option option : entry.getOptions()) {
                String text = option.getText().string();
                float score = option.getScore();

                suggestions.add(SuggestVO.builder()
                        .text(text)
                        .type("phrase")
                        .count(0L)
                        .score((double) score)
                        .build());
            }
        }
    }

    private HomePageVO convertToVO(GoodsDocument document) {
        HomePageVO vo = new HomePageVO();
        vo.setGoodsId(document.getGoodsId());
        vo.setGoodsName(document.getGoodsName());
        vo.setMainImage(document.getMainImage());
        vo.setPrice(document.getPrice());
        vo.setExtendedTags(document.getExtendedTags());
        return vo;
    }
}
