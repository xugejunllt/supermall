package com.lanf.goods.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.CalculateOrderAmountDTO;
import com.lanf.goods.model.vo.CalculateOrderAmountVO;
import com.lanf.goods.model.vo.SkuDetailVO;
import com.lanf.goods.model.vo.UserGoodsDetailVO;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 基础商品 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Slf4j
@RestController
@RequestMapping("/app/goods")
public class GoodsAppController {

    @Autowired
    private IGoodsService goodsService;

    private IGoodsSkuService goodsSkuService;


    @GetMapping("/goodsDetail")
    public Result<UserGoodsDetailVO> goodsDetail(Long id) {

        log.info("用户商品详细:id{}", id);

        return Result.ok(goodsService.userGoodsDetail(id));
    }

    @GetMapping("/queryBySkuId")
    public Result<SkuDetailVO> queryBySkuId(Long skuId) {

        log.info("查询sku详细:id{}", skuId);

        return Result.ok(goodsService.queryBySkuId(skuId));
    }

    /**
     * 计算订单金额
     */
    @PostMapping("/calculateOrderAmount")
    public Result<CalculateOrderAmountVO> calculateOrderAmount(@RequestBody @Validated CalculateOrderAmountDTO dto) {

        log.info("计算订单金额:{}", dto);
        return Result.ok(goodsSkuService.calculateOrderAmount(dto));

    }
}

