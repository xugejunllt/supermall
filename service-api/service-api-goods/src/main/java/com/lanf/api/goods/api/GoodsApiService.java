package com.lanf.api.goods.api;

import com.lanf.api.goods.model.dto.*;
import com.lanf.api.goods.model.query.BaseGoodsPageQuery;
import com.lanf.api.goods.model.query.GoodsPageQuery;
import com.lanf.api.goods.model.query.GoodsSkuPageQuery;
import com.lanf.api.goods.model.query.ReconciliationStockFlowQuery;
import com.lanf.api.goods.model.query.UserStockPageQuery;
import com.lanf.api.goods.model.query.UserStockFlowPageQuery;
import com.lanf.api.goods.model.query.UserStockPreorderPublishLogPageQuery;
import com.lanf.api.goods.model.vo.*;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Component
@FeignClient(name = "service-goods",  url = "localhost:9005")
public interface GoodsApiService {

    @Hmily
    @PostMapping("/goods/api/deductStock")
    public Result<DeductStockVO> deductStock(@RequestBody DeductStockDTO deductStockDTO);
    @Hmily
    @PostMapping("/goods/api/bathDeductStock")
    public Result<Void> bathDeductStock(@RequestBody @Validated BathDeductStockDTO deductStockDTO);


    @PostMapping("/goods/api/calculateOrderTotalAmount")
    public Result<CalculateOrderTotalAmountVO> calculateOrderTotalAmount(@RequestBody CalculateOrderTotalAmountDTO dto);


    @PostMapping("/goods/api/validateCartItem")
    public Result<ValidateCartItemVO>  validateCartItem(@RequestBody @Validated ValidateCartDTO dto);

    @Hmily
    @PostMapping("/goods/api/clearCart")
    public Result<Void>  clearCart(@RequestBody @Validated ClearCartDTO dto);

    @PostMapping("/goods/api/queryCartGoodsInfo")
    public Result<ClearCartVO>  queryCartGoodsInfo(@RequestBody @Validated ClearCartDTO dto);

    @PostMapping("/goods/api/reconciliationStockFlowQuery")
    public Result<ReconciliationStockFlowVO> reconciliationStockFlowQuery(@RequestBody  ReconciliationStockFlowQuery query);
    @Hmily
    @PostMapping("/goods/api/seckillStockPreoccupation")
    public Result<Void> seckillStockPreoccupation(@RequestBody @Validated SeckillStockPreoccupationDTO dto);

    @Deprecated
    @PostMapping("/goods/goodsApi/emptyCart")
    public Result<EmptyCartVO> emptyCart(@RequestBody Set<Long> cartIdLis);
    @Deprecated
    @PostMapping("/goods/goodsApi/queryBySkuCode")
    public Result<List<ApiGoodsSkuVO>> queryBySkuCode(@RequestBody List<String> skuCode);
    @Deprecated
    @PostMapping("/goods/goodsApi/baseGoodsBySkuCodeBathQuery")
    public Result<List<BaseGoodsBySkuCodeVO>> baseGoodsBySkuCodeBathQuery(@RequestBody List<String> skuCodeList);
    @Deprecated
    @PostMapping("/goods/goodsApi/checkAndQueryGoods")
    public Result<ApiGoodsSkuVO> checkAndQueryGoods(@RequestBody CheckAndQueryGoodsDTO dto);
    /**
     * 后台管理系统接口
     */
    // ==================== 商品管理 ====================

    /**
     * 添加商品
     */
    @PostMapping("/goods/admin/goods/addGoods")
    Result<Void> addGoods(@Validated @RequestBody AddGoodsDTO dto);

    /**
     * 上架商品
     */
    @PostMapping("/goods/admin/goods/upGoods")
    Result<Void> upGoods(@Validated @RequestBody UpGoodsDTO dto);

    /**
     * 分页查询商品列表
     */
    @GetMapping("/goods/admin/goods/goodsPageQuery")
    Result<PageResult<GoodsPageVO>> goodsPageQuery(@SpringQueryMap GoodsPageQuery query);

    /**
     * 商品详细信息
     */
    @GetMapping("/goods/admin/goods/goodsDetailQuery")
    Result<GoodsDetailVO> goodsDetailQuery(@RequestParam("id") Long id);

    // ==================== 基础商品管理 ====================

    /**
     * 分页查询基础商品信息列表
     */
    @GetMapping("/goods/admin/baseGoods/baseGoodsPageQuery")
    Result<PageResult<BaseGoodsPageVO>> baseGoodsPageQuery(@SpringQueryMap BaseGoodsPageQuery query);

    /**
     * 添加基础商品信息
     */
    @PostMapping("/goods/admin/baseGoods/addBaseGoods")
    Result<Void> addBaseGoods(@Validated @RequestBody AddBaseGoodsDTO baseGoodsAdd);

    /**
     * 根据商品编码查询商品信息
     */
    @GetMapping("/goods/admin/baseGoods/baseGoodsByCodeQuery")
    Result<BaseGoodsByCodeVO> baseGoodsByCodeQuery(@RequestParam("code") String code);

    /**
     * 根据sku编码查询sku信息
     */
    @GetMapping("/goods/admin/baseGoods/baseGoodsBySkuCodeQuery")
    Result<BaseGoodsBySkuCodeVO> baseGoodsBySkuCodeQuery(@RequestParam("skuCode") String skuCode);

    // ==================== 商品属性管理 ====================

    /**
     * 添加商品属性
     */
    @PostMapping("/goods/admin/goodsAttribute/addGoodsAttribute")
    Result<Void> addGoodsAttribute(@Validated @RequestBody AddGoodsAttributeDTO dto);

    /**
     * 分页查询商品属性
     */
    @GetMapping("/goods/admin/goodsAttribute/goodsAttributePageQuery")
    Result<PageResult<GoodsAttributePageVO>> goodsAttributePageQuery(@SpringQueryMap PageQuery query);

    /**
     * 更新商品属性
     */
    @PostMapping("/goods/admin/goodsAttribute/updateGoodsAttribute")
    Result<Void> updateGoodsAttribute(@Validated @RequestBody UpdateGoodsAttributeDTO dto);

    /**
     * 查询所有商品属性
     */
    @GetMapping("/goods/admin/goodsAttribute/goodsAttributeListQuery")
    Result<List<GoodsAttributeListVO>> goodsAttributeListQuery();

    /**
     * 属性详细
     */
    @GetMapping("/goods/admin/goodsAttribute/goodsAttributeDetailQuery")
    Result<GoodsAttributeDetailVO> goodsAttributeDetailQuery(@RequestParam("id") Long id);

    // ==================== 商品品牌管理 ====================

    /**
     * 添加商品品牌
     */
    @PostMapping("/goods/admin/goodsBrand/addGoodsBrand")
    Result<Void> addGoodsBrand(@Validated @RequestBody AddGoodsBrandDTO dto);

    /**
     * 分页查询商品品牌
     */
    @GetMapping("/goods/admin/goodsBrand/goodsBrandPageQuery")
    Result<PageResult<GoodsBrandPageVO>> goodsBrandPageQuery(@SpringQueryMap PageQuery query);

    /**
     * 查询所有品牌
     */
    @GetMapping("/goods/admin/goodsBrand/goodsBrandListQuery")
    Result<List<GoodsBrandListVO>> goodsBrandListQuery();

    // ==================== 商品分类管理 ====================

    /**
     * 添加商品分类
     */
    @PostMapping("/goods/admin/goodsCategory/addGoodsCategory")
    Result<Void> addGoodsCategory(@Validated @RequestBody AddGoodsCategoryDTO dto);

    /**
     * 分页查询商品分类
     */
    @GetMapping("/goods/admin/goodsCategory/goodsCategoryPageQuery")
    Result<PageResult<GoodsCategoryPageVO>> goodsCategoryPageQuery(@SpringQueryMap PageQuery query);

    // ==================== 店铺管理 ====================

    /**
     * 添加店铺
     */
    @PostMapping("/goods/admin/shop/addShop")
    Result<Void> addShop(@Validated @RequestBody AddShopDTO dto);

    /**
     * 分页查询店铺
     */
    @GetMapping("/goods/admin/shop/shopPageQuery")
    Result<PageResult<ShopPageVO>> shopPageQuery(@SpringQueryMap PageQuery query);

    /**
     * 查询所有店铺
     */
    @GetMapping("/goods/admin/shop/shopListQuery")
    Result<List<ShopListVO>> shopListQuery();

    // ==================== 库存预售发布日志管理 ====================

    /**
     * 回收库存
     */
    @PostMapping("/goods/admin/userStockPreorderPublishLog/recycleStock")
    Result<Void> recycleStock(@Validated @RequestBody RecycleStockDTO recycleStockDTO);

    /**
     * 分页查询库存
     */
    @GetMapping("/goods/admin/stock/stockPageQuery")
    Result<PageResult<StockPageVO>> stockPageQuery(@SpringQueryMap UserStockPageQuery query);

    /**
     * 分页查询库存流水
     */
    @GetMapping("/goods/admin/userStockFlow/userStockFlowPageQuery")
    Result<PageResult<UserStockFlowPageVO>> userStockFlowPageQuery(@SpringQueryMap UserStockFlowPageQuery query);

    /**
     * 分页查询库存预售发布日志
     */
    @GetMapping("/goods/admin/userStockPreorderPublishLog/userStockPreorderPublishLogPageQuery")
    Result<PageResult<UserStockPreorderPublishLogPageVO>> userStockPreorderPublishLogPageQuery(@SpringQueryMap UserStockPreorderPublishLogPageQuery query);

    // ==================== 商品SKU管理 ====================

    /**
     * 分页查询商品SKU列表
     */
    @GetMapping("/goods/admin/goods/goodsSkuPageQuery")
    Result<PageResult<GoodsSkuPageVO>> goodsSkuPageQuery(@SpringQueryMap GoodsSkuPageQuery query);
}
