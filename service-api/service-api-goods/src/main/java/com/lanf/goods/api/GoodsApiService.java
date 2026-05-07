package com.lanf.goods.api;

import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.*;
import com.lanf.goods.model.query.ReconciliationStockFlowQuery;
import com.lanf.goods.model.vo.*;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@Component
@FeignClient(name = "service-goods",url = "localhost:9005") //调用的服务名称
public interface GoodsApiService {

    @Hmily
    @PostMapping("/goods/api/deductStock")
    public Result<DeductStockVO> deductStock(@RequestBody  DeductStockDTO deductStockDTO);


    @PostMapping("/goods/api/calculateOrderTotalAmount")
    public Result<CalculateOrderTotalAmountVO> calculateOrderTotalAmount(@RequestBody  CalculateOrderTotalAmountDTO dto);


    @PostMapping("/goods/api/validateCartItem")
    public Result<ValidateCartItemVO>  validateCartItem(@RequestBody @Validated ValidateCartDTO dto);

    @Hmily
    @PostMapping("/goods/api/clearCart")
    public Result<ClearCartVO>  clearCart(@RequestBody @Validated ClearCartDTO dto);

    @PostMapping("/goods/api/reconciliationStockFlowQuery")
    public Result<ReconciliationStockFlowVO> reconciliationStockFlowQuery(@RequestBody  ReconciliationStockFlowQuery query);

    @PostMapping("/goods/api/seckillStockPreoccupation")
    public Result<Void> seckillStockPreoccupation(@RequestBody @Validated SeckillStockPreoccupationDTO dto)

    @Deprecated
    @PostMapping("/goods/goodsApi/emptyCart")
    public Result<EmptyCartVO> emptyCart(@RequestBody Set<Long> cartIdLis);
    @Deprecated
    @PostMapping("/goods/goodsApi/queryBySkuCode")
    public Result<List<ApiGoodsSkuVO>> queryBySkuCode(@RequestBody List<String> skuCode);
    @Deprecated
    @PostMapping("/goods/goodsApi/baseGoodsBySkuCodeBathQuery")
    public Result<List<BaseGoodsBySkuCodeQueryVO>> baseGoodsBySkuCodeBathQuery(@RequestBody List<String> skuCodeList);
    @Deprecated
    @PostMapping("/goods/goodsApi/checkAndQueryGoods")
    public Result<ApiGoodsSkuVO> checkAndQueryGoods(@RequestBody CheckAndQueryGoodsDTO dto);
}
