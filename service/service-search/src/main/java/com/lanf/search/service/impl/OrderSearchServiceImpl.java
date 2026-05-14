package com.lanf.search.service.impl;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.search.model.document.OrderDocument;
import com.lanf.search.model.query.OrderSearchQuery;
import com.lanf.search.model.vo.OrderSearchVO;
import com.lanf.search.repository.OrderRepository;
import com.lanf.search.service.IOrderSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderSearchServiceImpl implements IOrderSearchService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    /**
     * 订单综合搜索服务
     */
    @Override
    public PageResult<OrderSearchVO> searchOrders(OrderSearchQuery query) {


        String orderNumber = query.getOrderNumber();
        Long tenantId = query.getTenantId();
        Integer orderStatus = query.getOrderStatus();
        String searchWord = query.getSearchWord();
        Long userId = query.getUserId();
        long pageNum = query.getPage();
        long pageSize = query.getPageSize();

        // 1. 构建布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 等值查询：订单编号
        if (StringUtils.hasText(orderNumber)) {
            boolQuery.must(QueryBuilders.termQuery(OrderDocument.ORDER_NO, orderNumber));
        }
        // 等值查询：用户id
        if (userId != null) {
            boolQuery.must(QueryBuilders.termQuery(OrderDocument.USER_ID, userId));
        }
        // 等值查询：租户ID
        if (tenantId != null) {
            boolQuery.must(QueryBuilders.termQuery(OrderDocument.TENANT_ID, tenantId));
        }

        // 等值查询：订单状态
        if (orderStatus != null) {
            boolQuery.must(QueryBuilders.termQuery(OrderDocument.ORDER_STATUS, orderStatus));
        }

        // 模糊搜索：商品名称 (使用 match_query 支持 IK 分词)
        if (StringUtils.hasText(searchWord)) {
            boolQuery.must(QueryBuilders.matchQuery(OrderDocument.GOODS_NAME, searchWord));
        }

        // 2. 构建分页与排序 (按创建时间倒序)
        PageRequest pageRequest = PageRequest.of((int) (pageNum - 1),
                (int) pageSize, Sort.by(Sort.Direction.DESC, OrderDocument.CREATE_TIME));

        // 3. 执行查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageRequest);

        SearchHits<OrderDocument> searchHits = esTemplate.search(queryBuilder.build(), OrderDocument.class);

        // 4. 转换结果集为 VO
        List<OrderSearchVO> voList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> {
                    OrderSearchVO vo = new OrderSearchVO();
                    vo.setOrderId(doc.getOrderId());
                    vo.setUserId(doc.getUserId());
                    vo.setGoodsName(doc.getGoodsName());
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList,
                voList.size(), searchHits.getTotalHits());
    }
}
