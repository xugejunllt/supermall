package com.lanf.goods.controller.aip;

import com.lanf.goods.model.dto.CheckAndQueryGoodsDTO;
import com.lanf.goods.model.vo.BaseGoodsBySkuCodeQueryVO;
import com.lanf.goods.model.vo.EmptyCartVO;
import com.lanf.goods.model.vo.ApiGoodsSkuVO;
import com.lanf.goods.service.base.IBaseGoodsService;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/goodsApi")
public class GoodsApiController {

    @Autowired
    private ICartService cartService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IBaseGoodsService baseGoodsService;

    @PostMapping("/emptyCart")
    public Result<EmptyCartVO> emptyCart(@RequestBody Set<Long> cartIdLis) {

        log.info("清空购物车:cartIdLis{}", cartIdLis);
        return Result.ok(cartService.emptyCart(cartIdLis));
    }

    @PostMapping("/queryBySkuCode")
    public Result<List<ApiGoodsSkuVO>> queryBySkuCode(@RequestBody List<String> skuCode) {

        log.info("根据skuCode查询商品信息:skuCode{}", skuCode);

        return Result.ok(goodsService.queryBySkuCode(skuCode));
    }

    @PostMapping("/baseGoodsBySkuCodeBathQuery")
    public Result<List<BaseGoodsBySkuCodeQueryVO>> baseGoodsBySkuCodeBathQuery(@RequestBody List<String> skuCodeList) {

        log.info("根据skuCode批量查询基础商品信息:skuCodeList{}", skuCodeList);

        return Result.ok(baseGoodsService.baseGoodsBySkuCodeBathQuery(skuCodeList));
    }

    @PostMapping("/checkAndQueryGoods")
    public Result<ApiGoodsSkuVO> checkAndQueryGoods(@RequestBody CheckAndQueryGoodsDTO dto) {

        log.info("单笔下单，查询和校验商品信息:dto{}", dto);

        return Result.ok(goodsService.checkAndQueryGoods(dto));
    }

}
