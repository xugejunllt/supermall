package com.lanf.goods.controller.admin;


import com.lanf.common.utils.JsonUtils;
import com.lanf.goods.model.dto.BaseGoodsAddDTO;
import com.lanf.goods.model.query.BaseGoodsPageQuery;
import com.lanf.goods.model.vo.BaseGoodsByCodeQueryVO;
import com.lanf.goods.model.vo.BaseGoodsBySkuCodeQueryVO;
import com.lanf.goods.model.vo.BaseGoodsPageVO;
import com.lanf.goods.service.base.IBaseGoodsService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/baseGoodsPage")
    public Result<PageResult<BaseGoodsPageVO>> baseGoodsPage(BaseGoodsPageQuery query) {

        log.info("[{}]开始,入参:[{}]", "分页查询基础商品信息列表", JsonUtils.toJsonString(query));

        return Result.ok(baseGoodsService.baseGoodsPage(query));
    }

    @PostMapping("/baseGoodsAdd")
    public Result<PageResult<BaseGoodsPageVO>> baseGoodsAdd(@Validated @RequestBody BaseGoodsAddDTO baseGoodsAdd) {

        log.info("添加基础商品信息:baseGoodsAdd{}", baseGoodsAdd);
        baseGoodsService.baseGoodsAdd(baseGoodsAdd);
        return Result.ok();
    }
    @GetMapping("/baseGoodsByCodeQuery")
    public Result<BaseGoodsByCodeQueryVO> baseGoodsByCodeQuery(String code) {

        log.info("根据商品编码查询商品信息:code{}", code);

        return Result.ok(baseGoodsService.baseGoodsByCodeQuery(code));
    }
    @GetMapping("/baseGoodsBySkuCodeQuery")
    public Result<BaseGoodsBySkuCodeQueryVO> baseGoodsBySkuCodeQuery(String skuCode) {

        log.info("根据sku编码查询sku信息:skuCode{}", skuCode);

        return Result.ok(baseGoodsService.baseGoodsBySkuCodeQuery(skuCode));
    }
}

