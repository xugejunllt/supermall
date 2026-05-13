package com.lanf.goods.controller.admin;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.api.goods.model.dto.AddGoodsAttributeDTO;
import com.lanf.api.goods.model.dto.UpdateGoodsAttributeDTO;
import com.lanf.api.goods.model.vo.GoodsAttributeDetailVO;
import com.lanf.api.goods.model.vo.GoodsAttributeListVO;
import com.lanf.api.goods.model.vo.GoodsAttributePageVO;
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

    @PostMapping("/addGoodsAttribute")
    public Result<Void> addGoodsAttribute(@Validated @RequestBody AddGoodsAttributeDTO dto) {

        log.info("添加商品属性:dto{}", dto);
        goodsAttributeService.addGoodsAttribute(dto);
        return Result.ok();
    }

    @GetMapping("/goodsAttributePageQuery")
    public Result<PageResult<GoodsAttributePageVO>> goodsAttributePageQuery(PageQuery query) {

        log.info("分页查询商品属性:query{}", query);

        return Result.ok(goodsAttributeService.goodsAttributePageQuery(query));
    }


    @PostMapping("/updateGoodsAttribute")
    public Result<Void> updateGoodsAttribute(@Validated @RequestBody UpdateGoodsAttributeDTO dto) {

        log.info("更新商品属性:dto{}", dto);
        goodsAttributeService.updateGoodsAttribute(dto);
        return Result.ok();
    }

    @GetMapping("/goodsAttributeListQuery")
    public Result<List<GoodsAttributeListVO>> goodsAttributeListQuery() {

        log.info("查询所有商品属性");

        return Result.ok(goodsAttributeService.goodsAttributeListQuery());
    }
    @GetMapping("/goodsAttributeDetailQuery")
    public Result<GoodsAttributeDetailVO> goodsAttributeDetailQuery(Long id) {

        log.info("属性详细{}",id);

        return Result.ok(goodsAttributeService.goodsAttributeDetailQuery(id));
    }
}

