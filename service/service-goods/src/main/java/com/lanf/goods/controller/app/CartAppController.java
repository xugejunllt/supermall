package com.lanf.goods.controller.app;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.goods.model.dto.AddCartDTO;
import com.lanf.goods.model.dto.DecrementCartItemQuantityDTO;
import com.lanf.goods.model.dto.IncrementCartItemQuantityDTO;
import com.lanf.goods.model.vo.CartListVO;
import com.lanf.goods.service.goods.ICartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public Result<Void> addCart(@Validated @RequestBody AddCartDTO dto) {

        log.info("添加购物车:dto{}", dto);
        dto.setUserId(UserContext.getUserId());
        cartService.addCart(dto);
        return Result.ok( );
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

    @GetMapping("/listCart")
    public Result<PageResult<CartListVO>> listCart(PageQuery query) {

       log.info("分页查询购物车列表:query{}", query);
        return Result.ok(cartService.listCart(query));
    }



}

