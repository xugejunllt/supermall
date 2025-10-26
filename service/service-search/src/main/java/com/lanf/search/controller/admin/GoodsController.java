package com.lanf.search.controller.admin;

import com.lanf.rocketmq.model.message.GoodsAddMsg;
import com.lanf.search.model.bo.GoodsUpdateDTO;
import com.lanf.search.model.query.GoodsPageQuery;
import com.lanf.search.model.query.GoodsPageVO;
import com.lanf.search.model.query.PageResult;
import com.lanf.search.service.GoodsService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {


    @Autowired
    private GoodsService goodsService; // 自动注入Repository

    @PostMapping("/save")
    public Result addGoods(@RequestBody GoodsAddMsg dto) {

        log.info("添加商品:{}",dto);
        dto.setCreateTime(new Date());
        dto.setUpdateTime(new Date());
        goodsService.addGoods(dto);

        return  Result.ok();
    }
    @GetMapping("/searchWords")
    public Result<List<String>> searchWordsList(String searchWords)  {

        log.info("搜索提示词:{}",searchWords);


        return  Result.ok(goodsService.searchWordsList(searchWords));
    }
    @GetMapping("/goodsPage")
    public Result<PageResult<GoodsPageVO>> goodsPage(GoodsPageQuery query)  {

        log.info("分页搜索商品列表:{}",query);


        return  Result.ok(goodsService.goodsPage(query));
    }

    @PostMapping("/update")
    public Result updateGoods(@RequestBody GoodsUpdateDTO dto)  {

        log.info("更新商品:{}",dto);
        dto.setCreateTime(new Date());
        dto.setUpdateTime(new Date());
        goodsService.updateGoods(dto);

        return  Result.ok();
    }

}
