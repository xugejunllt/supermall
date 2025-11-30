package com.lanf.search.service.impl;


import com.lanf.lock.service.DistributedLocker;
import com.lanf.search.mapper.GoodsMapper;
import com.lanf.rocketmq.model.message.GoodsAddMsg;
import com.lanf.search.model.bo.GoodsDocumentIdBO;
import com.lanf.search.model.dto.GoodsUpdateDTO;
import com.lanf.search.model.entity.GoodsDO;
import com.lanf.search.model.query.GoodsPageQuery;
import com.lanf.search.model.query.GoodsPageVO;
import com.lanf.search.model.query.PageResult;
import com.lanf.search.service.GoodsService;

import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {


    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private DistributedLocker distributedLocker;


    @Override
    public void addGoods(GoodsAddMsg dto) {

        Boolean lockerLock = distributedLocker.getLock(dto.getGoodsId() + "", 3L, TimeUnit.MINUTES);
         if ( !lockerLock){
             log.info("商品已同步");
             return;
         }

        GoodsDocumentIdBO goodsDocumentIdBO = queryByGoodsId(dto.getGoodsId());
        if (goodsDocumentIdBO != null){
            log.info("文档已存在");
            return;
        }
        GoodsDO goodsDO = toGoodsDO(dto);
        goodsMapper.save(goodsDO);


    }


    private GoodsDO toGoodsDO(GoodsAddMsg dto) {

        GoodsDO goodsDO = new GoodsDO();
        goodsDO.setGoodsId(dto.getGoodsId());
        goodsDO.setCode(dto.getCode());
        goodsDO.setName(dto.getName());
        goodsDO.setUpDownStatus(dto.getUpDownStatus());
        goodsDO.setPrice(dto.getPrice().doubleValue());
        goodsDO.setPicture(dto.getPicture());
        goodsDO.setCreateTime(dto.getCreateTime().getTime());
        goodsDO.setUpdateTime(dto.getUpdateTime().getTime());
        goodsDO.setSearchWords(dto.getSearchWords());
        return goodsDO;
    }

    @Override
    public List<String> searchWordsList(String searchWords) {

        //指定搜索的字段
        CompletionSuggestionBuilder suggest = SuggestBuilders.completionSuggestion("searchWords")
                //搜索词
                .prefix(searchWords)
                //去掉重复
                .skipDuplicates(true)
                //匹配数量
                .size(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        //指定suggest的名称
        suggestBuilder.addSuggestion("title_suggest", suggest);

        IndexCoordinates indexCoordinates = elasticsearchOperations.getIndexCoordinatesFor(GoodsDO.class);
        // 查询
        SearchResponse goodsNameSuggestResp = elasticsearchRestTemplate.suggest(suggestBuilder, indexCoordinates);
        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion = goodsNameSuggestResp
                .getSuggest().getSuggestion("title_suggest");

        // 处理返回
        List<String> suggests = suggestion.getEntries().stream().map(x -> x.getOptions().stream().map(y -> y.getText().
                toString()).collect(Collectors.toList())).findFirst().get();

        return  suggests;
    }

    @Override
    public PageResult<GoodsPageVO> goodsPage(GoodsPageQuery query) {

        //构建条件查询Builder
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", query.getSearchWords());

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().
                //查询条件
                withQuery(matchQueryBuilder).
                //分页
                withPageable(PageRequest.of(query.getPage() - 1, query.getPageSize())).
                build();
        //进行查询
        SearchHits<GoodsPageVO> search = elasticsearchRestTemplate.search
                (searchQuery, GoodsPageVO.class, IndexCoordinates.of("goods_index"));

        List<GoodsPageVO> result = new ArrayList<>();
        search.forEach((hits)->result.add(hits.getContent()));

        return new PageResult(result,search.getTotalHits(),query.getPageSize());
    }


    private GoodsDocumentIdBO queryByGoodsId(Long goodsId){

        //构建条件查询Builder
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("goodsId",goodsId);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().
                //查询条件
                        withQuery(termQueryBuilder).
                build();
        //进行查询
        SearchHits<GoodsDocumentIdBO> search = elasticsearchRestTemplate.search
                (searchQuery, GoodsDocumentIdBO.class, IndexCoordinates.of("goods_index"));

        List<GoodsDocumentIdBO> result = new ArrayList<>();
        search.forEach((hits)->result.add(hits.getContent()));
        if (result.isEmpty()){

            return  null;
        }
        return  result.get(0);
    }
    @Override
    public void updateGoods(GoodsUpdateDTO dto) {

        Boolean lockerLock = distributedLocker.getLock(dto.getGoodsId() + "", 3L, TimeUnit.MINUTES);
        if ( !lockerLock){
            throw new BizException("商品写入未完成");
        }
        GoodsDocumentIdBO goodsDocumentIdBO = queryByGoodsId(dto.getGoodsId());
        if (goodsDocumentIdBO == null){
           throw  new BizException("文档不存在");
        }
        goodsMapper.deleteById(goodsDocumentIdBO.getId());
        GoodsDO goodsDO2 = toGoodsDO2(dto);
        goodsMapper.save(goodsDO2);

    }

    private GoodsDO toGoodsDO2(GoodsUpdateDTO dto) {

        GoodsDO goodsDO = new GoodsDO();
        goodsDO.setGoodsId(dto.getGoodsId());
        goodsDO.setCode(dto.getCode());
        goodsDO.setName(dto.getName());
        goodsDO.setUpDownStatus(dto.getUpDownStatus());
        goodsDO.setPrice(dto.getPrice().doubleValue());
        goodsDO.setPicture(dto.getPicture());
        goodsDO.setCreateTime(dto.getCreateTime().getTime());
        goodsDO.setUpdateTime(dto.getUpdateTime().getTime());
        goodsDO.setSearchWords(dto.getSearchWords());
        return goodsDO;
    }
}
