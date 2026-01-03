package com.lanf.goods.controller.aip;

import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.service.base.IBaseGoodsService;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api")
public class GoodsApiController {

    @Autowired
    private ICartService cartService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IBaseGoodsService baseGoodsService;
    @Autowired
    private IStockService stockService;

    /**
     * 扣减库存
     */
    @PostMapping("/deductStock")
    public Result<Void> deductStock(@RequestBody @Valid DeductStockDTO deductStockDTO) {
        log.info("扣减库存开始[{}]", deductStockDTO);
        stockService.deductStock(deductStockDTO);
        return Result.ok();

    }



}
