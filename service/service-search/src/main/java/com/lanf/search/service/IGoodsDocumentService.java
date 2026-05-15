package com.lanf.search.service;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.search.model.query.GoodsSearchQuery;
import com.lanf.search.model.query.HomePageQuery;
import com.lanf.search.model.query.SuggestQuery;
import com.lanf.search.model.vo.HomePageVO;
import com.lanf.search.model.vo.SuggestVO;

import java.util.List;

public interface IGoodsDocumentService {

    /**
     * 首页
     *
     *
     */
    PageResult<HomePageVO> pageHomePage(HomePageQuery query);

    /**
     * 商品综合搜索
     */
    PageResult<HomePageVO> searchGoods(GoodsSearchQuery query);

    /**
     * 查询词建议 - 输入前缀返回建议词列表
     * @param query 建议查询参数
     * @return 建议词列表
     */
    List<SuggestVO> getSuggestions(SuggestQuery query);
}
