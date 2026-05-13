package com.lanf.goods.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.api.goods.model.dto.AddGoodsDTO;
import com.lanf.api.goods.model.dto.UpGoodsDTO;
import com.lanf.api.goods.model.query.GoodsPageQuery;
import com.lanf.api.goods.model.vo.GoodsDetailVO;
import com.lanf.api.goods.model.vo.GoodsPageVO;
import com.lanf.goods.service.goods.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/addGoods")
    public Result<Void> addGoods(@Validated @RequestBody AddGoodsDTO dto) {
        log.info("添加商品:dto{}", dto);
        goodsService.addGoods(dto);
        return Result.ok();
    }

    @PostMapping("/upGoods")
    public Result<Void> upGoods(@Validated @RequestBody UpGoodsDTO dto) {

        log.info("上架商品:goodsId{}", dto);
        goodsService.upGoods(dto);
        return Result.ok();
    }


    @GetMapping("/goodsPageQuery")
    public Result<PageResult<GoodsPageVO>> goodsPageQuery(GoodsPageQuery query) {

        log.info("分页查询商品列表:query{}", query);

        return Result.ok(goodsService.goodsPageQuery(query));
    }

    @GetMapping("/goodsDetailQuery")
    public Result<GoodsDetailVO> goodsDetailQuery(Long id) {

        log.info("商品详细信息:id{}", id);

        return Result.ok(goodsService.goodsDetailQuery(id));
    }

}

