package com.lanf.goods.controller.admin;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.api.goods.model.dto.AddShopDTO;
import com.lanf.api.goods.model.vo.ShopListVO;
import com.lanf.api.goods.model.vo.ShopPageVO;
import com.lanf.goods.service.goods.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 店铺信息 前端控制器
 * </p>
 *
 * @author jarven
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/admin/shop")
public class ShopController {
    @Autowired
    private IShopService shopService;

    @PostMapping("/addShop")
    public Result<Void> addShop(@Validated @RequestBody AddShopDTO dto) {
        log.info("添加店铺:dto{}", dto);
        shopService.addShop(dto);
        return Result.ok();
    }

    @GetMapping("/shopPageQuery")
    public Result<PageResult<ShopPageVO>> shopPageQuery(PageQuery query) {

        log.info("分页查询店铺:query{}", query);

        return Result.ok(shopService.shopPageQuery(query));
    }

    @GetMapping("/shopListQuery")
    public Result<List<ShopListVO>> shopListQuery() {

        log.info("查询所有店铺");

        return Result.ok(shopService.shopListQuery());
    }

}

