package com.lanf.search.service.impl;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.search.model.document.GoodsDocument;
import com.lanf.search.model.query.GoodsSearchQuery;
import com.lanf.search.model.query.HomePageQuery;
import com.lanf.search.model.query.SuggestQuery;
import com.lanf.search.model.vo.HomePageVO;
import com.lanf.search.model.vo.SearchPageVO;
import com.lanf.search.model.vo.SuggestVO;
import com.lanf.search.repository.GoodsRepository;
import com.lanf.search.service.IGoodsDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
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
    public PageResult<SearchPageVO> searchGoods(GoodsSearchQuery query) {
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

        // 3. 属性筛选 (Nested Query - 匹配 attributes 嵌套对象)
        if (StringUtils.hasText(query.getAttrName()) && StringUtils.hasText(query.getAttrValue())) {
            // 构建 nested query 匹配 attrName 和 attrValue
            BoolQueryBuilder attributeQuery = QueryBuilders.boolQuery()
                    //子字段名称
                    .must(QueryBuilders.termQuery("attributes.attrName.keyword", query.getAttrName()))
                    .must(QueryBuilders.termQuery("attributes.attrValue.keyword", query.getAttrValue()));
            
            NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery(
                    "attributes", 
                    attributeQuery, 
                    ScoreMode.None
            );
            
            boolQuery.filter(nestedQuery);
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
        List<SearchPageVO> content = searchHits.getSearchHits().stream()
                .map(hit -> convertToSearchPageVO(hit.getContent()))
                .collect(Collectors.toList());

        return new PageResult<>(content, content.size(), searchHits.getTotalHits());
    }


    @Override
    public List<SuggestVO> getSuggestions(SuggestQuery query) {
        if (!StringUtils.hasText(query.getPrefix())) {
            return Collections.emptyList();
        }

        // 1. ✅ 先执行 Completion Suggester（自动补全）
        List<SuggestVO> suggestions = new ArrayList<>(getCompletionSuggestions(query));

        // 2. ✅ 如果补全结果不足，再执行拼音搜索（支持拼音纠错）
        if (query.getSize() > suggestions.size()) {
            suggestions.addAll(getPinyinSuggestions(query, suggestions.size()));
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
                    // 得分相同时，优先级：completion > pinyin
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

            log.debug("Completion Suggest Query DSL: {}", sourceBuilder.toString());

            // 5. 执行查询
            SearchResponse searchResponse = restHighLevelClient.search(
                    searchRequest, RequestOptions.DEFAULT);
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
     * 使用拼音搜索获取建议（支持拼音纠错）
     * 支持：
     * 1. 完整拼音搜索：pingguo shouji → 苹果手机
     * 2. 拼音模糊纠错：pinggo shouji → 苹果手机（少了一个u）
     * 3. 拼音前缀搜索：pg sj → 苹果手机
     */
    private List<SuggestVO> getPinyinSuggestions(SuggestQuery query, int currentSize) {
        List<SuggestVO> suggestions = new ArrayList<>();
        
        try {
            int remainingSize = query.getSize() - currentSize;
            if (remainingSize <= 0) {
                return suggestions;
            }

            // 构建搜索请求
            SearchRequest searchRequest = new SearchRequest("goods_index");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            // 判断输入是否为拼音（简单判断：包含字母且不含中文）
            boolean isPinyin = query.getPrefix().matches(".*[a-zA-Z].*") && 
                              !query.getPrefix().matches(".*[\u4e00-\u9fa5].*");
            
            if (isPinyin) {
                // 拼音搜索：使用 spellCheck 字段，支持模糊匹配
                MatchQueryBuilder matchQuery = QueryBuilders.matchQuery(GoodsDocument.SPELL_CHECK, query.getPrefix())
                        .fuzziness(Fuzziness.AUTO)  // 自动纠错
                        .operator(Operator.OR);
                
                sourceBuilder.query(matchQuery);
                log.debug("拼音搜索 Query DSL: {}", sourceBuilder.toString());
            } else {
                // 非拼音输入，不执行拼音搜索
                return suggestions;
            }
            
            // 限制返回数量
            sourceBuilder.size(remainingSize);
            
            // 只返回需要的字段
            sourceBuilder.fetchSource(new String[]{
                GoodsDocument.GOODS_NAME,
                GoodsDocument.SPELL_CHECK
            }, null);
            
            searchRequest.source(sourceBuilder);
            
            // 执行查询
            SearchResponse searchResponse = restHighLevelClient.search(
                    searchRequest, RequestOptions.DEFAULT);
            
            // 解析结果
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = hit.getSourceAsMap();
                String goodsName = (String) sourceMap.get(GoodsDocument.GOODS_NAME);
                
                if (StringUtils.hasText(goodsName)) {
                    float score = hit.getScore();
                    
                    suggestions.add(SuggestVO.builder()
                            .text(goodsName)
                            .type("pinyin")
                            .count(0L)
                            .score((double) score)
                            .build());
                }
            }
            
            log.info("✅ 拼音搜索返回 {} 条建议", suggestions.size());
            
        } catch (Exception e) {
            log.error("❌ 拼音搜索获取建议词失败, prefix: {}", query.getPrefix(), e);
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
    private SearchPageVO convertToSearchPageVO(GoodsDocument document) {
        SearchPageVO vo = new SearchPageVO();
        vo.setGoodsId(document.getGoodsId());
        vo.setGoodsName(document.getGoodsName());
        vo.setMainImage(document.getMainImage());
        vo.setPrice(document.getPrice());
        vo.setExtendedTags(document.getExtendedTags());
        return vo;
    }
}
