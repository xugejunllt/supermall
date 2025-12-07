package com.lanf.goods.controller.admin;


import com.lanf.goods.model.dto.GoodsAddDTO;
import com.lanf.goods.model.dto.UpDownStatusDTO;
import com.lanf.goods.model.query.GoodsPageQuery;
import com.lanf.goods.model.vo.GoodsDetailVO;
import com.lanf.goods.model.vo.GoodsPageVO;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.security.utils.MerchantIdContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 基础商品 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Slf4j
@RestController
@RequestMapping("/admin/goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;


    @PostMapping("/goodsAdd")
    public Result goodsAdd(@Validated @RequestBody GoodsAddDTO dto) {
        log.info("添加商品:dto{}", dto);
        goodsService.goodsAdd(dto);
        return Result.ok();
    }

    @PostMapping("/upGoods")
    public Result upGoods(@Validated @NotNull(message = "商品id不能为空") Long goodsId) {

        log.info("上架商品:goodsId{}", goodsId);
        MerchantIdContext.setMerchantId(1441223317880180736L);
        goodsService.upGoods(goodsId);
        return Result.ok();
    }


    @GetMapping("/goodsPage")
    public Result<PageResult<GoodsPageVO>> goodsPage(GoodsPageQuery query) {

        log.info("分页查询商品列表:query{}", query);

        return Result.ok(goodsService.goodsPage(query));
    }

    @GetMapping("/goodsDetail")
    public Result<GoodsDetailVO> goodsDetail(Long id) {

        log.info("商品详细信息:id{}", id);

        return Result.ok(goodsService.goodsDetail(id));
    }


    @PostMapping("/upDownStatus")
    public Result upDownStatus(@Validated @RequestBody UpDownStatusDTO dto) {
        log.info("上下架商品:dto{}", dto);
        goodsService.upDownStatus(dto);
        return Result.ok();
    }
}

