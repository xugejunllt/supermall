package com.lanf.goods.controller.app;

import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.StockEnoughDTO;
import com.lanf.goods.model.query.StockQueryByGoodsIdQuery;
import com.lanf.goods.model.vo.StockEnoughVO;
import com.lanf.goods.model.vo.StockWithDistanceVO;
import com.lanf.goods.service.stock.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/app/stock")
public class StockController {

    @Autowired
    private IStockService stockService;

    /**
     * 单笔下单时 校验库存是否足够
     *
     */
    @PostMapping("/isStockEnough")
    public Result<StockEnoughVO> isStockEnough(@RequestBody @Validated StockEnoughDTO dto) {

        log.info("商品库存是否充足:{}", dto);

        return Result.ok(stockService.isStockEnough(dto));
    }
    @PostMapping("/stockQueryByGoodsId")
    public Result<List<StockWithDistanceVO>> stockQueryByGoodsId(@RequestBody @Validated
                                                                 StockQueryByGoodsIdQuery dto) {

                log.info("商品库存查询:{}", dto);
            return Result.ok(stockService.stockQueryByGoodsId(dto));
    }

}
