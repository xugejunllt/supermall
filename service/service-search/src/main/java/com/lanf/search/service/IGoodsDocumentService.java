package com.lanf.search.service;

import com.lanf.mybatis.base.PageResult;
import com.lanf.search.model.query.HomePageQuery;
import com.lanf.search.model.vo.HomePageVO;

public interface IGoodsDocumentService {

    /**
     * 首页
     *
     *
     */
    PageResult<HomePageVO> pageHomePage(HomePageQuery query);

}
