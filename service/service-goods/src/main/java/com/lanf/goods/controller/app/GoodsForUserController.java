package com.lanf.goods.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.goods.model.vo.GoodsDetailForUserVO;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
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
public class GoodsForUserController {

    @Autowired
    private IGoodsService goodsService;

    private IGoodsSkuService goodsSkuService;


    @GetMapping("/goodsDetailForUserQuery")
    public Result<GoodsDetailForUserVO> goodsDetailForUserQuery(Long id) {

        log.info("用户商品详细:id{}", id);

        return Result.ok(goodsService.goodsDetailForUserQuery(id));
    }



}

