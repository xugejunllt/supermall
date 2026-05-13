package com.lanf.system.controller.goods;


import com.lanf.api.goods.api.GoodsApiService;
import com.lanf.api.goods.model.dto.*;
import com.lanf.api.goods.model.query.BaseGoodsPageQuery;
import com.lanf.api.goods.model.query.GoodsPageQuery;
import com.lanf.api.goods.model.vo.*;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器（远程调用）
 */
@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsAdminController {

    @Autowired
    private GoodsApiService goodsAdminApiService;

    // ==================== 商品管理 ====================

    /**
     * 添加商品
     */
    @PostMapping("/addGoods")
    public Result<Void> addGoods(@Validated @RequestBody AddGoodsDTO dto) {
        log.info("[{}]开始,入参:[{}]", "添加商品", dto);
        return goodsAdminApiService.addGoods(dto);
    }

    /**
     * 上架商品
     */
    @PostMapping("/upGoods")
    public Result<Void> upGoods(@Validated @RequestBody UpGoodsDTO dto) {
        log.info("[{}]开始,入参:[{}]", "上架商品", dto);
        return goodsAdminApiService.upGoods(dto);
    }

    /**
     * 分页查询商品列表
     */
    @GetMapping("/goodsPageQuery")
    public Result<PageResult<GoodsPageVO>> goodsPageQuery(GoodsPageQuery query) {
        log.info("[{}]开始,入参:[{}]", "分页查询商品列表", query);
        return goodsAdminApiService.goodsPageQuery(query);
    }

    /**
     * 商品详细信息
     */
    @GetMapping("/goodsDetailQuery")
    public Result<GoodsDetailVO> goodsDetailQuery(@RequestParam("id") Long id) {
        log.info("[{}]开始,id:[{}]", "商品详细信息", id);
        return goodsAdminApiService.goodsDetailQuery(id);
    }

    // ==================== 基础商品管理 ====================

    /**
     * 分页查询基础商品信息列表
     */
    @GetMapping("/baseGoodsPageQuery")
    public Result<PageResult<BaseGoodsPageVO>> baseGoodsPageQuery(BaseGoodsPageQuery query) {
        log.info("[{}]开始,入参:[{}]", "分页查询基础商品信息列表", query);
        return goodsAdminApiService.baseGoodsPageQuery(query);
    }

    /**
     * 添加基础商品信息
     */
    @PostMapping("/addBaseGoods")
    public Result<Void> addBaseGoods(@Validated @RequestBody AddBaseGoodsDTO baseGoodsAdd) {
        log.info("[{}]开始,入参:[{}]", "添加基础商品信息", baseGoodsAdd);
        return goodsAdminApiService.addBaseGoods(baseGoodsAdd);
    }

    /**
     * 根据商品编码查询商品信息
     */
    @GetMapping("/baseGoodsByCodeQuery")
    public Result<BaseGoodsByCodeVO> baseGoodsByCodeQuery(@RequestParam("code") String code) {
        log.info("[{}]开始,code:[{}]", "根据商品编码查询商品信息", code);
        return goodsAdminApiService.baseGoodsByCodeQuery(code);
    }

    /**
     * 根据sku编码查询sku信息
     */
    @GetMapping("/baseGoodsBySkuCodeQuery")
    public Result<BaseGoodsBySkuCodeVO> baseGoodsBySkuCodeQuery(@RequestParam("skuCode") String skuCode) {
        log.info("[{}]开始,skuCode:[{}]", "根据sku编码查询sku信息", skuCode);
        return goodsAdminApiService.baseGoodsBySkuCodeQuery(skuCode);
    }

    // ==================== 商品属性管理 ====================

    /**
     * 添加商品属性
     */
    @PostMapping("/addGoodsAttribute")
    public Result<Void> addGoodsAttribute(@Validated @RequestBody AddGoodsAttributeDTO dto) {
        log.info("[{}]开始,入参:[{}]", "添加商品属性", dto);
        return goodsAdminApiService.addGoodsAttribute(dto);
    }

    /**
     * 分页查询商品属性
     */
    @GetMapping("/goodsAttributePageQuery")
    public Result<PageResult<GoodsAttributePageVO>> goodsAttributePageQuery(PageQuery query) {
        log.info("[{}]开始,入参:[{}]", "分页查询商品属性", query);
        return goodsAdminApiService.goodsAttributePageQuery(query);
    }

    /**
     * 更新商品属性
     */
    @PostMapping("/updateGoodsAttribute")
    public Result<Void> updateGoodsAttribute(@Validated @RequestBody UpdateGoodsAttributeDTO dto) {
        log.info("[{}]开始,入参:[{}]", "更新商品属性", dto);
        return goodsAdminApiService.updateGoodsAttribute(dto);
    }

    /**
     * 查询所有商品属性
     */
    @GetMapping("/goodsAttributeListQuery")
    public Result<List<GoodsAttributeListVO>> goodsAttributeListQuery() {
        log.info("[{}]开始", "查询所有商品属性");
        return goodsAdminApiService.goodsAttributeListQuery();
    }

    /**
     * 属性详细
     */
    @GetMapping("/goodsAttributeDetailQuery")
    public Result<GoodsAttributeDetailVO> goodsAttributeDetailQuery(@RequestParam("id") Long id) {
        log.info("[{}]开始,id:[{}]", "属性详细", id);
        return goodsAdminApiService.goodsAttributeDetailQuery(id);
    }

    // ==================== 商品品牌管理 ====================

    /**
     * 添加商品品牌
     */
    @PostMapping("/addGoodsBrand")
    public Result<Void> addGoodsBrand(@Validated @RequestBody AddGoodsBrandDTO dto) {
        log.info("[{}]开始,入参:[{}]", "添加商品品牌", dto);
        return goodsAdminApiService.addGoodsBrand(dto);
    }

    /**
     * 分页查询商品品牌
     */
    @GetMapping("/goodsBrandPageQuery")
    public Result<PageResult<GoodsBrandPageVO>> goodsBrandPageQuery(PageQuery query) {
        log.info("[{}]开始,入参:[{}]", "分页查询商品品牌", query);
        return goodsAdminApiService.goodsBrandPageQuery(query);
    }

    /**
     * 查询所有品牌
     */
    @GetMapping("/goodsBrandListQuery")
    public Result<List<GoodsBrandListVO>> goodsBrandListQuery() {
        log.info("[{}]开始", "查询所有品牌");
        return goodsAdminApiService.goodsBrandListQuery();
    }

    // ==================== 商品分类管理 ====================

    /**
     * 添加商品分类
     */
    @PostMapping("/addGoodsCategory")
    public Result<Void> addGoodsCategory(@Validated @RequestBody AddGoodsCategoryDTO dto) {
        log.info("[{}]开始,入参:[{}]", "添加商品分类", dto);
        return goodsAdminApiService.addGoodsCategory(dto);
    }

    /**
     * 分页查询商品分类
     */
    @GetMapping("/goodsCategoryPageQuery")
    public Result<PageResult<GoodsCategoryPageVO>> goodsCategoryPageQuery(PageQuery query) {
        log.info("[{}]开始,入参:[{}]", "分页查询商品分类", query);
        return goodsAdminApiService.goodsCategoryPageQuery(query);
    }

    // ==================== 店铺管理 ====================

    /**
     * 添加店铺
     */
    @PostMapping("/addShop")
    public Result<Void> addShop(@Validated @RequestBody AddShopDTO dto) {
        log.info("[{}]开始,入参:[{}]", "添加店铺", dto);
        return goodsAdminApiService.addShop(dto);
    }

    /**
     * 分页查询店铺
     */
    @GetMapping("/shopPageQuery")
    public Result<PageResult<ShopPageVO>> shopPageQuery(PageQuery query) {
        log.info("[{}]开始,入参:[{}]", "分页查询店铺", query);
        return goodsAdminApiService.shopPageQuery(query);
    }

    /**
     * 查询所有店铺
     */
    @GetMapping("/shopListQuery")
    public Result<List<ShopListVO>> shopListQuery() {
        log.info("[{}]开始", "查询所有店铺");
        return goodsAdminApiService.shopListQuery();
    }

    // ==================== 库存预售发布日志管理 ====================

    /**
     * 回收库存
     */
    @PostMapping("/recycleStock")
    public Result<Void> recycleStock(@Validated @RequestBody RecycleStockDTO recycleStockDTO) {
        log.info("[{}]开始,入参:[{}]", "回收库存", recycleStockDTO);
        return goodsAdminApiService.recycleStock(recycleStockDTO);
    }

}
