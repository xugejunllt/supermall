package com.lanf.search.service.impl;

import com.lanf.mybatis.base.PageResult;
import com.lanf.search.model.document.GoodsDocument;
import com.lanf.search.model.query.HomePageQuery;
import com.lanf.search.model.vo.HomePageVO;
import com.lanf.search.repository.GoodsRepository;
import com.lanf.search.service.IGoodsDocumentService;
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

import java.util.List;
import java.util.stream.Collectors;

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

        // 添加过滤条件
        boolQuery.filter(QueryBuilders.termQuery(GoodsDocument.IS_DELETED, 0));
        // 0上架
        boolQuery.filter(QueryBuilders.termQuery(GoodsDocument.UP_DOWN_STATUS, 0));

        // 设置查询和排序
        queryBuilder.withQuery(boolQuery)
                .withSort(SortBuilders.fieldSort(GoodsDocument.UPDATE_TIME).order(SortOrder.DESC))
                .withPageable(PageRequest.of(query.getPage(), query.getPageSize()));

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
                searchQuery.getPageable().getPageNumber(), searchHits.getTotalHits());
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
