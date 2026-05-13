package com.lanf.goods.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.AddBaseGoodsDTO;
import com.lanf.goods.model.query.BaseGoodsPageQuery;
import com.lanf.goods.model.vo.BaseGoodsByCodeVO;
import com.lanf.goods.model.vo.BaseGoodsBySkuCodeVO;
import com.lanf.goods.model.vo.BaseGoodsPageVO;
import com.lanf.goods.service.base.IBaseGoodsService;
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
 * @since 2024-06-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/baseGoods")
public class BaseGoodsController {

    @Autowired
    private IBaseGoodsService baseGoodsService;

    @GetMapping("/baseGoodsPageQuery")
    public Result<PageResult<BaseGoodsPageVO>> baseGoodsPageQuery(BaseGoodsPageQuery query) {

        log.info("分页查询基础商品信息列表开始,入参:[{}]", query);

        return Result.ok(baseGoodsService.baseGoodsPageQuery(query));
    }

    @PostMapping("/addBaseGoods")
    public Result<Void> addBaseGoods(@Validated @RequestBody AddBaseGoodsDTO baseGoodsAdd) {

        log.info("添加基础商品信息:baseGoodsAdd{}", baseGoodsAdd);
        baseGoodsService.addBaseGoods(baseGoodsAdd);
        return Result.ok();
    }
    @GetMapping("/baseGoodsByCodeQuery")
    public Result<BaseGoodsByCodeVO> baseGoodsByCodeQuery(String code) {

        log.info("根据商品编码查询商品信息:code{}", code);

        return Result.ok(baseGoodsService.baseGoodsByCodeQuery(code));
    }
    @GetMapping("/baseGoodsBySkuCodeQuery")
    public Result<BaseGoodsBySkuCodeVO> baseGoodsBySkuCodeQuery(String skuCode) {

        log.info("根据sku编码查询sku信息:skuCode{}", skuCode);

        return Result.ok(baseGoodsService.baseGoodsBySkuCodeQuery(skuCode));
    }
}

