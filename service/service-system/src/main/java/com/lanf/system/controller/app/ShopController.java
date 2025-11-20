package com.lanf.system.controller.app;


import com.lanf.mybatis.base.PageResult;
import com.lanf.system.model.entiry.ShopDO;
import com.lanf.system.model.query.ShopPageQuery;
import com.lanf.system.service.merchant.IShopService;
import com.lanf.web.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
@Slf4j
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private IShopService service;
    @PreAuthorize("hasAuthority('bnt.shop.list')")
    @ApiOperation(value = "分页查询店铺列表")
    @GetMapping("/shopPage")
    public Result<PageResult<ShopDO>> shopPage(ShopPageQuery query) {

        log.info("分页查询店铺列表:query{}",query);

        return Result.ok(service.shopPage(query));
    }

}

