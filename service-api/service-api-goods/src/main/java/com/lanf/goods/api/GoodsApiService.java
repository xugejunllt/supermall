package com.lanf.goods.api;

import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.CheckAndQueryGoodsDTO;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.vo.ApiGoodsSkuVO;
import com.lanf.goods.model.vo.BaseGoodsBySkuCodeQueryVO;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.goods.model.vo.EmptyCartVO;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Component
@FeignClient(name = "service-goods",url = "localhost:9005") //调用的服务名称
public interface GoodsApiService {

    @Hmily
    @PostMapping("/goods/api/deductStock")
    public Result<DeductStockVO> deductStock(@RequestBody @Valid DeductStockDTO deductStockDTO);

    @PostMapping("/goods/goodsApi/emptyCart")
    public Result<EmptyCartVO> emptyCart(@RequestBody Set<Long> cartIdLis);

    @PostMapping("/goods/goodsApi/queryBySkuCode")
    public Result<List<ApiGoodsSkuVO>> queryBySkuCode(@RequestBody List<String> skuCode);

    @PostMapping("/goods/goodsApi/baseGoodsBySkuCodeBathQuery")
    public Result<List<BaseGoodsBySkuCodeQueryVO>> baseGoodsBySkuCodeBathQuery(@RequestBody List<String> skuCodeList);
    @PostMapping("/goods/goodsApi/checkAndQueryGoods")
    public Result<ApiGoodsSkuVO> checkAndQueryGoods(@RequestBody CheckAndQueryGoodsDTO dto);
}
