package com.lanf.search.controller.api;

import com.lanf.search.model.dto.GoodsUpdateDTO;
import com.lanf.search.service.GoodsService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/goods")
public class GoodsController {


    @Autowired
    private GoodsService goodsService; // 自动注入Repository


    @PostMapping("/update")
    public Result updateGoods(@RequestBody GoodsUpdateDTO dto)  {

        log.info("更新商品:{}",dto);
        dto.setCreateTime(new Date());
        dto.setUpdateTime(new Date());
        goodsService.updateGoods(dto);

        return  Result.ok();
    }

}
