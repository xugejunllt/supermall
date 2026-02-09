package com.lanf.goods.controller.aip;

import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.CalculateOrderTotalAmountDTO;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.dto.ValidateCartDTO;
import com.lanf.goods.model.vo.CalculateOrderTotalAmountVO;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.goods.model.vo.ValidateCartItemVO;
import com.lanf.goods.service.base.IBaseGoodsService;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.service.goods.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    private IGoodsSkuService goodsSkuService;

    /**
     * 扣减库存
     */
    @PostMapping("/deductStock")
    public Result<DeductStockVO> deductStock(@RequestBody @Validated DeductStockDTO deductStockDTO) {
        log.info("扣减库存开始[{}]", deductStockDTO);

        return Result.ok(stockService.deductStock(deductStockDTO));

    }

    /**
     * 金额订单总金额
     *
     *
     */
    @PostMapping("/calculateOrderTotalAmount")
    public Result<CalculateOrderTotalAmountVO> calculateOrderTotalAmount(@RequestBody @Validated CalculateOrderTotalAmountDTO dto)  {

        log.info("金额订单总金额[{}]", dto);

        return Result.ok(goodsSkuService.calculateOrderTotalAmount(dto));

    }

    @PostMapping("/validateCartItem")
    public Result<ValidateCartItemVO>  validateCartItem(@RequestBody @Validated ValidateCartDTO dto)  {

        log.info("提交订单校验购物车项目[{}]", dto);

        return Result.ok(cartService.validateCartItem(dto));

    }

}
