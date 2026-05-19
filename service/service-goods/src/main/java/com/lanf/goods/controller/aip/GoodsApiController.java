package com.lanf.goods.controller.aip;

import com.lanf.api.goods.model.dto.*;
import com.lanf.api.goods.model.vo.*;
import com.lanf.constant.result.Result;
import com.lanf.api.goods.model.query.ReconciliationStockFlowQuery;
import com.lanf.constant.utils.UserContext;
import com.lanf.goods.service.base.IBaseGoodsService;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
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

    @Autowired
    private IUserStockFlowService userStockFlowService;

    /**
     * 单笔下单扣减库存
     */
    @PostMapping("/deductStock")
    public Result<DeductStockVO> deductStock(@RequestBody @Validated DeductStockDTO deductStockDTO) {
        log.info("下单冻结库存开始[{}]", deductStockDTO);

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

    /**
     *
     * 提交订单校验购物车项目
     *
     */
    @PostMapping("/validateCartItem")
    public Result<ValidateCartItemVO>  validateCartItem(@RequestBody @Validated ValidateCartDTO dto)  {

        log.info("提交订单校验购物车项目[{}]", dto);
        dto.setUserId(UserContext.getUserId());

        return Result.ok(cartService.validateCartItem(dto));

    }

    /**
     *
     * 清空购物车
     *
     */
    @PostMapping("/clearCart")
    public Result<Void>  clearCart(@RequestBody @Validated ClearCartDTO dto)  {

        log.info("清空购物车预执行[{}]", dto);
        cartService.clearCart(dto);
        return Result.ok();

    }
    @PostMapping("/queryCartGoodsInfo")
    public Result<ClearCartVO>  queryCartGoodsInfo(@RequestBody @Validated ClearCartDTO dto)  {

        log.info("查询购物车商品信息{}", dto);

        return Result.ok(cartService.queryCartGoodsInfo(dto));

    }
    /**
     * 查询库存对账单 库存流水
     *
     *
     */
    @PostMapping("/reconciliationStockFlowQuery")
    public Result<ReconciliationStockFlowVO> reconciliationStockFlowQuery(@RequestBody @Validated ReconciliationStockFlowQuery query){


        log.info("查询库存对账单 库存流水[{}]", query);

        return Result.ok(userStockFlowService.reconciliationStockFlowQuery( query));
    }

    /**
     *
     * 添加秒杀商品预占库存
     *
     */
    @PostMapping("/seckillStockPreoccupation")
    public Result<Void> seckillStockPreoccupation(@RequestBody @Validated SeckillStockPreoccupationDTO dto){


        log.info("添加秒杀商品预占库存[{}]", dto);

        stockService.seckillStockPreoccupation( dto);
        return Result.ok();
    }


}
