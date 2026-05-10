package com.lanf.goods.controller.admin;


import com.lanf.goods.model.dto.GoodsCategoryAddDTO;
import com.lanf.goods.model.vo.GoodsCategoryPageVO;
import com.lanf.goods.service.goods.IGoodsCategoryService;
import com.lanf.constant.web.PageQuery;
import com.lanf.constant.web.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商品分类 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Slf4j
@RestController
@RequestMapping("/admin/goodsCategory")
public class GoodsCategoryController {

    @Autowired
    private IGoodsCategoryService goodsCategoryService;

    @PostMapping("/goodsCategoryAdd")
    public Result goodsCategoryAdd(@Validated @RequestBody GoodsCategoryAddDTO dto) {

        log.info("添加商品分类:dto{}", dto);
        goodsCategoryService.goodsCategoryAdd(dto);

        return Result.ok();
    }
    @GetMapping("/goodsCategoryPage")
    public Result<PageResult<GoodsCategoryPageVO>> goodsCategoryPage( PageQuery query) {

        log.info("分页查询商品分类:query{}", query);

        return Result.ok( goodsCategoryService.goodsCategoryPage(query));
    }

}

