package com.lanf.search.service.impl;

import com.lanf.constant.web.PageResult;
import com.lanf.search.model.document.GoodsDocument;
import com.lanf.search.model.query.GoodsSearchQuery;
import com.lanf.search.model.query.HomePageQuery;
import com.lanf.search.model.vo.HomePageVO;
import com.lanf.search.repository.GoodsRepository;
import com.lanf.search.service.IGoodsDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IGoodsDocumentServiceImpl implements IGoodsDocumentService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;



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
