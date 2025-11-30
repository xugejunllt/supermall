package com.lanf.goods.controller.app;


import com.lanf.goods.model.dto.CartAddDTO;
import com.lanf.goods.model.dto.ChangeCartQuantityDTO;
import com.lanf.goods.model.vo.CartGoodsVO;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Slf4j
@RestController
@RequestMapping("/app/cart")
public class CartAppController {

    @Autowired
    private ICartService cartService;


    @PostMapping("/cartAdd")
    public Result cartAdd(@Validated @RequestBody CartAddDTO dto) {

        log.info("添加购物车:dto{}", dto);
        cartService.cartAdd(dto);
        return Result.ok();
    }

    @PostMapping("/changeCartQuantity")
    public Result changeCartQuantity(@Validated @RequestBody ChangeCartQuantityDTO dto) {

        log.info("变更购物车商品数量:dto{}", dto);
        cartService.changeCartQuantity(dto);
        return Result.ok();
    }

    @GetMapping("/cartList")
    public Result<List<CartGoodsVO>> cartList() {

        log.info("查询购物车列表");
        return Result.ok(cartService.cartList());
    }

}

