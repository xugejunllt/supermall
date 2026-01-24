package com.lanf.goods.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.CartAddDTO;
import com.lanf.goods.model.dto.DecrementCartItemQuantityDTO;
import com.lanf.goods.model.dto.IncrementCartItemQuantityDTO;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.security.utils.UserIdContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @PostMapping("/addCart")
    public Result<Void> cartAdd(@Validated @RequestBody CartAddDTO dto) {

        log.info("添加购物车:dto{}", dto);
        dto.setUserId(UserIdContext.getUserId());
        cartService.cartAdd(dto);
        return Result.ok();
    }

    @PostMapping("/incrementCartItemQuantity")
    public Result<Void> incrementCartItemQuantity(@Validated @RequestBody IncrementCartItemQuantityDTO dto) {

        log.info("购物车项数量增加:dto{}", dto);
        cartService.incrementCartItemQuantity(dto);
        return Result.ok();
    }

    @PostMapping("/decrementCartItemQuantity")
    public Result<Void> decrementCartItemQuantity(@Validated @RequestBody DecrementCartItemQuantityDTO dto) {

        log.info("购物车项数量减少:dto{}", dto);
        cartService.decrementCartItemQuantity(dto);
        return Result.ok();
    }




}

