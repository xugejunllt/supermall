package com.lanf.goods.controller.admin;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.GoodsAttributeAddDTO;
import com.lanf.goods.model.dto.GoodsAttributeUpdateDTO;
import com.lanf.goods.model.entity.GoodsAttributeDO;
import com.lanf.goods.service.base.IGoodsAttributeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 商品属性 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-06
 */
@Slf4j
@RestController
@RequestMapping("/admin/goodsAttribute")
public class GoodsAttributeController {

    @Autowired
    private IGoodsAttributeService goodsAttributeService;

    @PostMapping("/goodsAttributeAdd")
    public Result<Void> goodsAttributeAdd(@Validated @RequestBody GoodsAttributeAddDTO dto) {

        log.info("添加商品属性:dto{}", dto);
        goodsAttributeService.goodsAttributeAdd(dto);
        return Result.ok();
    }

    @GetMapping("/goodsAttributePage")
    public Result<PageResult<GoodsAttributeDO>> goodsAttributePage(PageQuery query) {

        log.info("分页查询商品属性:query{}", query);

        return Result.ok(goodsAttributeService.goodsAttributePage(query));
    }


    @PostMapping("/goodsAttributeUpdate")
    public Result goodsAttributeUpdate(@Validated @RequestBody GoodsAttributeUpdateDTO dto) {

        log.info("更新商品属性:dto{}", dto);
        goodsAttributeService.goodsAttributeUpdate(dto);
        return Result.ok();
    }

    @GetMapping("/goodsAttributeList")
    public Result<List<GoodsAttributeDO>> goodsAttributeList() {

        log.info("查询所有商品属性");

        return Result.ok(goodsAttributeService.goodsAttributeList());
    }
    @GetMapping("/detail")
    public Result<GoodsAttributeDO> detail(Long id) {

        log.info("属性详细");

        return Result.ok(goodsAttributeService.detail(id));
    }
}

