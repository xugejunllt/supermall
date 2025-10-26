package com.lanf.search.service;

import com.lanf.rocketmq.model.message.GoodsAddMsg;
import com.lanf.search.model.dto.GoodsUpdateDTO;
import com.lanf.search.model.query.GoodsPageQuery;
import com.lanf.search.model.query.GoodsPageVO;
import com.lanf.search.model.query.PageResult;

import java.util.List;

public interface GoodsService {

    void addGoods(GoodsAddMsg dto);

    List<String> searchWordsList(String searchWords);

    PageResult<GoodsPageVO> goodsPage(GoodsPageQuery query);


    void updateGoods(GoodsUpdateDTO dto);
}
