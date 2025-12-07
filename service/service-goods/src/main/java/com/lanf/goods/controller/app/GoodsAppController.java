package com.lanf.goods.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.goods.model.vo.SkuDetailVO;
import com.lanf.goods.model.vo.UserGoodsDetailVO;
import com.lanf.goods.service.goods.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}

