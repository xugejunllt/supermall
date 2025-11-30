package com.lanf.goods.controller.admin;


import com.lanf.goods.model.dto.GoodsBrandAddDTO;
import com.lanf.goods.model.entity.GoodsBrandDO;
import com.lanf.goods.service.goods.IGoodsBrandService;
import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 商品品牌 前端控制器
 * </p>
 *
 * @author
 * @since 2024-06-11
 */
@Slf4j
@RestController
@RequestMapping("/admin/goodsBrand")
public class GoodsBrandController {

    @Autowired
    private IGoodsBrandService goodsBrandService;

    @PostMapping("/goodsBrandAdd")
    public Result goodsBrandAdd(@Validated @RequestBody GoodsBrandAddDTO dto) {
        log.info("添加商品品牌:dto{}", dto);
        goodsBrandService.goodsBrandAdd(dto);
        return Result.ok();
    }

    @GetMapping("/goodsBrandPage")
    public Result<PageResult<GoodsBrandDO>> goodsBrandPage(PageQuery query) {

        log.info("分页查询商品品牌:query{}", query);

        return Result.ok(goodsBrandService.goodsBrandPage(query));
    }

    @GetMapping("/goodsBrandList")
    public Result<List<GoodsBrandDO>> goodsBrandList() {

        log.info("查询所有品牌");

        return Result.ok(goodsBrandService.goodsBrandList());
    }

}

