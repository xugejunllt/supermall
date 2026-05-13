package com.lanf.goods.controller.admin;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.AddGoodsBrandDTO;
import com.lanf.goods.model.vo.GoodsBrandListVO;
import com.lanf.goods.model.vo.GoodsBrandPageVO;
import com.lanf.goods.service.goods.IGoodsBrandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/addGoodsBrand")
    public Result<Void> addGoodsBrand(@Validated @RequestBody AddGoodsBrandDTO dto) {
        log.info("添加商品品牌:dto{}", dto);
        goodsBrandService.addGoodsBrand(dto);
        return Result.ok();
    }

    @GetMapping("/goodsBrandPageQuery")
    public Result<PageResult<GoodsBrandPageVO>> goodsBrandPageQuery(PageQuery query) {

        log.info("分页查询商品品牌:query{}", query);

        return Result.ok(goodsBrandService.goodsBrandPageQuery(query));
    }

    @GetMapping("/goodsBrandListQuery")
    public Result<List<GoodsBrandListVO>> goodsBrandListQuery() {

        log.info("查询所有品牌");

        return Result.ok(goodsBrandService.goodsBrandListQuery());
    }

}

