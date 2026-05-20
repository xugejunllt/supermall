package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.model.bo.GoodsItem;
import com.lanf.api.goods.model.bo.ShopGoods;
import com.lanf.api.goods.model.dto.ClearCartDTO;
import com.lanf.api.goods.model.dto.ValidateCartDTO;
import com.lanf.api.goods.model.vo.ClearCartVO;
import com.lanf.api.goods.model.vo.ValidateCartItemVO;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.UserContext;
import com.lanf.goods.mapper.CartMapper;
import com.lanf.goods.model.bo.CartSortOrderBO;
import com.lanf.goods.model.dto.AddCartDTO;
import com.lanf.goods.model.dto.DecrementCartItemQuantityDTO;
import com.lanf.goods.model.dto.IncrementCartItemQuantityDTO;
import com.lanf.goods.model.entity.*;
import com.lanf.goods.model.query.StockQueryByGoodsIdQuery;
import com.lanf.goods.model.vo.CartItemVO;
import com.lanf.goods.model.vo.CartPageVO;
import com.lanf.goods.model.vo.StockWithDistanceVO;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.service.goods.IShopService;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.utils.GoodsServiceUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Slf4j
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, CartDO> implements ICartService {


    @Autowired
    private IGoodsSkuService goodsSkuService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IShopService shopService;
    @Autowired
    private ITccOperationService tccOperationService;
    @Autowired
    private IStockService stockService;

    @DistributedLock(key = "#dto.userId")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addCart(AddCartDTO dto) {

        /**
         * 校验库存是否足够
         */
        StockQueryByGoodsIdQuery byGoodsIdQuery = new StockQueryByGoodsIdQuery();
        byGoodsIdQuery.setGoodsId(dto.getGoodsId());
        byGoodsIdQuery.setSkuCode(dto.getSkuCode());
        byGoodsIdQuery.setAreaCode(dto.getAreaCode());
        byGoodsIdQuery.setLongitude(dto.getLongitude());
        byGoodsIdQuery.setLatitude(dto.getLatitude());
        List<StockWithDistanceVO> distanceVOS = stockService.stockQueryByGoodsId(byGoodsIdQuery);
        if (distanceVOS.isEmpty()) {
            throw new BizException("商品无库存");
        }
        StockWithDistanceVO stock = distanceVOS.get(0);

        String skuCode = dto.getSkuCode();
        Long goodsId = dto.getGoodsId();
        // 查询是否已存在相同的购物车项
        Long userId = UserContext.getUserId();
        GoodsSkuDO goodsSkuDO = goodsSkuService.lambdaQuery()
                .eq(GoodsSkuDO::getGoodsId, goodsId)
                .eq(GoodsSkuDO::getSkuCode, dto.getSkuCode())
                .one();

        GoodsDO goodsDO = goodsService.getById(goodsSkuDO.getGoodsId());
        Long shopId = goodsDO.getShopId();
        CartDO existingCartDO = this.lambdaQuery()
                .eq(CartDO::getGoodsId, goodsId)
                .eq(CartDO::getSkuCode, skuCode)
                .eq(CartDO::getUserId, userId)
                .one();
        CartDO cartDO = null;
        if (existingCartDO == null) {
             cartDO = buildCartDO(dto, goodsSkuDO, goodsDO);
             if (cartDO.getQuantity() > stock.getUsableStock()) {
                throw new BizException("商品库存不足");
              }
        }
        //计算购物车项的排序码
        CartSortOrderBO cartSortOrderBO = calculateSortOrder( shopId,  userId);

        if (existingCartDO == null) {
            try {
                this.save(cartDO);
            } catch (DuplicateKeyException e) {
               throw new BizException("添加失败");
            }
        } else {

            int updateQuantity = dto.getQuantity() + existingCartDO.getQuantity();
            if (updateQuantity > stock.getUsableStock()) {
                throw new BizException("商品库存不足");
            }


            Long version = existingCartDO.getVersion();
            boolean update = this.lambdaUpdate()
                    .eq(BaseEntity::getId, existingCartDO.getId())
                    .eq(CartDO::getVersion, version)
                    .set(CartDO::getQuantity, updateQuantity)
                    .set(CartDO::getVersion, version + 1)
                    .update();
            if (!update) {
                throw new BizException("更新失败");
            }

        }
        batchUpdateSortOrderByShopId( cartSortOrderBO);

    }
    /**
     * 计算购物车项的排序码
     */
    private CartSortOrderBO calculateSortOrder(Long shopId, Long userId) {
        List<CartDO> cartDOList = this.lambdaQuery()
                .eq(CartDO::getShopId, shopId)
                .eq(CartDO::getUserId, userId)
                .orderByDesc(CartDO::getSortOrder)
                .list();

        CartSortOrderBO cartSortOrderBO = new CartSortOrderBO();
        cartSortOrderBO.setShopId(shopId);
        cartSortOrderBO.setUserId(userId);
        if (cartDOList.isEmpty()) {
            // 不存在该店铺商品 排序码初始为1
            cartSortOrderBO.setSortOrder(1L);
        } else {
            // 取当前排序码最大的一个
            CartDO maxSortOrderCart = cartDOList.get(0);
            cartSortOrderBO.setSortOrder(maxSortOrderCart.getSortOrder() + 1);
        }

        return cartSortOrderBO;
    }

    private CartDO buildCartDO(AddCartDTO dto, GoodsSkuDO goodsSkuDO, GoodsDO goodsDO){


        Long sortOrder = 1L;
        CartDO cartDO = new CartDO();
        cartDO.setUserId(UserContext.getUserId());
        cartDO.setShopId(goodsDO.getShopId());
        cartDO.setSkuId(goodsSkuDO.getId());
        cartDO.setGoodsId(goodsSkuDO.getGoodsId());
        cartDO.setQuantity(dto.getQuantity());
        cartDO.setSkuCode(dto.getSkuCode());
        cartDO.setVersion(1L);
        cartDO.setSortOrder(sortOrder);
        return cartDO;
    }
    /**
     * 验证商品库存和SKU信息
     */
    private void validateCartItem(AddCartDTO dto, String skuCode) {
        // 验证库存
        StockDO stockDO = GoodsServiceUtils.findStockDO(skuCode);
        if (stockDO == null) {
            log.warn("商品库存不存在");
            throw new BizException("商品库存不存在");
        }
        if (dto.getQuantity() > stockDO.getUsableStock()) {
            log.warn("商品库存不足,剩余库存[{}]", stockDO.getUsableStock());
            throw new BizException("商品库存不足");
        }
        // 验证SKU存在性
        GoodsSkuDO goodsSkuDO = goodsSkuService.lambdaQuery()
                .eq(GoodsSkuDO::getSkuCode, skuCode).one();
        if (goodsSkuDO == null) {
            log.warn("sku不存在");
            throw new BizException("sku不存在");
        }
    }







    @DistributedLock(key = "#dto.cartId")
    @Override
    public void incrementCartItemQuantity(IncrementCartItemQuantityDTO dto) {

        Long cartId = dto.getCartId();
        CartDO cartDO = this.getById(cartId);
        if (cartDO == null) {
            log.warn("购物车项不存在");
            throw new BizException("购物车项不存在");
        }
        StockQueryByGoodsIdQuery byGoodsIdQuery = new StockQueryByGoodsIdQuery();
        byGoodsIdQuery.setGoodsId(cartDO.getGoodsId());
        byGoodsIdQuery.setSkuCode(cartDO.getSkuCode());
        byGoodsIdQuery.setAreaCode(dto.getAreaCode());
        byGoodsIdQuery.setLongitude(dto.getLongitude());
        byGoodsIdQuery.setLatitude(dto.getLatitude());

        List<StockWithDistanceVO> distanceVOS = stockService.stockQueryByGoodsId(byGoodsIdQuery);
        if (distanceVOS.isEmpty()) {
            throw new BizException("商品无库存");
        }
        StockWithDistanceVO stock = distanceVOS.get(0);

        int updateQuantity = dto.getIncrementQuantity() + cartDO.getQuantity();

        if (updateQuantity  > stock.getUsableStock()) {
            log.warn("商品库存不足,剩余库存[{}]", stock.getUsableStock());
            throw new BizException("商品库存不足");
        }

        Long version = cartDO.getVersion();
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, cartDO.getId())
                .eq(CartDO::getVersion, version)
                .set(CartDO::getQuantity, updateQuantity)
                .set(CartDO::getVersion, version + 1)
                .update();
        if (!update) {
            throw new BizException("更新失败");
        }
    }
    @DistributedLock(key = "#dto.cartId")
    @Override
    public void decrementCartItemQuantity(DecrementCartItemQuantityDTO dto) {


        Long cartId = dto.getCartId();
        CartDO cartDO = this.getById(cartId);
        if (cartDO == null) {
            log.warn("购物车项不存在");
            throw new BizException("购物车项不存在");
        }
        boolean delete = cartDO.getQuantity() <= dto.getDecrementQuantity();
        if (delete) {

            log.info("购物车项数量不足,删除购物车项");
             this.removeById(cartDO.getId());
        } else {
            log.info("购物车项数量充足,减少购物车项数量");
            Long version = cartDO.getVersion();
            boolean update = this.lambdaUpdate()
                    .eq(BaseEntity::getId, cartDO.getId())
                    .eq(CartDO::getVersion, version)
                    .set(CartDO::getQuantity, cartDO.getQuantity() - dto.getDecrementQuantity())
                    .set(CartDO::getVersion, version + 1)
                    .update();
            if (!update) {
                throw new BizException("更新失败");
            }


        }


    }


    private void batchUpdateSortOrderByShopId(CartSortOrderBO cartSortOrderBO){

        boolean update = this.lambdaUpdate()
                .eq(CartDO::getShopId, cartSortOrderBO.getShopId())
                .eq(CartDO::getUserId, cartSortOrderBO.getUserId())
                .set(CartDO::getSortOrder, cartSortOrderBO.getSortOrder())
                .update();
        if (!update) {
            log.warn("批量更新失败");
            throw new BizException("批量更新失败");
        }

    }

    /**
     * 待性能优化
     * 填充是否有库存
     *
     */
    @Override
    public PageResult<CartPageVO> cartPageQuery(PageQuery query) {

        Long userId = UserContext.getUserId();
        IPage<CartDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<CartDO> pageResult = this.lambdaQuery()
                .eq(CartDO::getUserId, userId)
                .orderByDesc(CartDO::getSortOrder)
                .orderByDesc(BaseEntity::getCreateTime)
                .page(page);
        List<CartDO> cartDOList = pageResult.getRecords();
        if (cartDOList.isEmpty()){
            return PageResult.emptyResult();
        }
        /**
         * 手动过滤 保证shopId 顺序
         */
        List<Long> shopIdList = new ArrayList<>();
        for (CartDO cartDO : cartDOList){
            Long shopId = cartDO.getShopId();
            if ( !shopIdList.contains(shopId)){
                shopIdList.add(shopId);
            }
        }

        List<ShopDO> shopDOList = shopService.lambdaQuery().in(BaseEntity::getId, shopIdList).list();
        //key：shopId
        Map<Long, ShopDO> shopDOMap = shopDOList.stream().collect(Collectors.toMap(BaseEntity::getId, v -> v));
        //key：shopId
        Map<Long, List<CartDO>> cartDOMap = new HashMap<>(shopIdList.size());
        for (CartDO cartDO: cartDOList){

            Long shopId = cartDO.getShopId();
            List<CartDO> cartList = cartDOMap.computeIfAbsent(shopId, k -> new ArrayList<>());
            cartList.add(cartDO);

        }

        List<Long> skuIdList = cartDOList.stream().map(CartDO::getSkuId).collect(Collectors.toList());
        List<Long> goodsIdList = cartDOList.stream().map(CartDO::getGoodsId).collect(Collectors.toList());

        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().in(GoodsSkuDO::getId, skuIdList).list();
        Map<Long, GoodsSkuDO> goodsSkuDOMap = goodsSkuDOList.stream().collect(Collectors.
                toMap(BaseEntity::getId, v -> v));
        List<GoodsDO> goodsDOList = goodsService.lambdaQuery().in(BaseEntity::getId, goodsIdList).list();
        Map<Long, GoodsDO> goodsDOMap = goodsDOList.stream().collect(Collectors.
                toMap(BaseEntity::getId, v -> v));


        List<CartPageVO> cartListVOList = new ArrayList<>(shopIdList.size());
        for (Long shopId : shopIdList){

            List<CartDO> cartDOList1 = cartDOMap.get(shopId);
            List<CartItemVO> cartItemList = new ArrayList<>(cartDOList1.size());
            for (CartDO cartDO : cartDOList1){
                GoodsSkuDO goodsSkuDO = goodsSkuDOMap.get(cartDO.getSkuId());
                GoodsDO goodsDO = goodsDOMap.get(cartDO.getGoodsId());
                CartItemVO cartItemVO = getCartItemVO(cartDO, goodsSkuDO, goodsDO);
                cartItemList.add(cartItemVO);
            }
            CartPageVO cartListVO = new CartPageVO();
            cartListVO.setShopId(shopId);
            cartListVO.setShopName(shopDOMap.get(shopId).getName());
            cartListVO.setCartItemList(cartItemList);
            cartListVOList.add(cartListVO);

        }
        PageResult<CartPageVO> resultVo = new PageResult<>();
        resultVo.setTotal(pageResult.getTotal());
        resultVo.setSize(query.getPageSize());
        resultVo.setRecords(cartListVOList);
        return resultVo;
    }



    private static CartItemVO getCartItemVO(CartDO cartDO, GoodsSkuDO goodsSkuDO, GoodsDO goodsDO) {
        CartItemVO cartItemVO = new CartItemVO();
        cartItemVO.setCartId(cartDO.getId());
        cartItemVO.setGoodsId(cartDO.getGoodsId());
        cartItemVO.setSkuId(cartDO.getSkuId());
        cartItemVO.setSkuCode(cartDO.getSkuCode());
        cartItemVO.setGoodsName(goodsDO.getName());
        cartItemVO.setQuantity(cartDO.getQuantity());
        cartItemVO.setPrice(goodsSkuDO.getPrice());
        cartItemVO.setSkuName(goodsSkuDO.getAttributeDetail());
        cartItemVO.setSkuPictureAddress(goodsSkuDO.getSkuPictureAddress());
        return cartItemVO;
    }
    @Override
    public ValidateCartItemVO validateCartItem(ValidateCartDTO dto) {

        List<Long> cartIds = dto.getCartIds();
        Long userId = dto.getUserId();

        List<CartDO> cartDOList = this.lambdaQuery()
                .eq(CartDO::getUserId, userId)
                .in(CartDO::getId, cartIds)
                .list();
        if (cartDOList.isEmpty()){
            log.warn("购物车项不存在");
           throw  new BizException("购物车项不存在");
        }
        if (cartIds.size() != cartDOList.size()){
            log.warn("部分购物车项不存在");
            throw  new BizException("部分购物车项不存在");
        }

        /**
         * 构建返回数据
         */
        Set<Long> shopIdSet = cartDOList.stream().map(CartDO::getShopId).collect(Collectors.toSet());
        List<ShopDO> shopDOList = shopService.lambdaQuery().in(BaseEntity::getId, shopIdSet).list();
        Map<Long, ShopDO> shopDOMap = shopDOList.stream().collect(Collectors.toMap(BaseEntity::getId, v -> v));
        Set<Long> goodsSet = cartDOList.stream().map(CartDO::getGoodsId).collect(Collectors.toSet());
        List<GoodsDO> goodsDOList = goodsService.lambdaQuery().in(BaseEntity::getId, goodsSet).list();
        Map<Long, GoodsDO> goodsDOMap = goodsDOList.stream().collect(Collectors.toMap(BaseEntity::getId, v -> v));
        List<Long> skuIdList = cartDOList.stream().map(CartDO::getSkuId).collect(Collectors.toList());
        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().in(BaseEntity::getId, skuIdList).list();
        Map<Long, GoodsSkuDO> goodsSkuDOMap = goodsSkuDOList.stream().collect(Collectors.toMap(BaseEntity::getId, v -> v));
        //相同店铺id为一组
        Map<Long, List<CartDO>> shopIdCartDOMap = cartDOList.stream().collect(Collectors.groupingBy(CartDO::getShopId));

        List<ShopGoods> goodsVOList = new ArrayList<>(shopIdSet.size());
        Set<Map.Entry<Long, List<CartDO>>> entries = shopIdCartDOMap.entrySet();
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Map.Entry<Long, List<CartDO>> entry : entries){
            Long shopId = entry.getKey();
            ShopGoods shopGoodsVO = new ShopGoods();
            shopGoodsVO.setShopId(shopId);
            ShopDO shopDO = shopDOMap.get(shopId);
            if ( shopDO!=null){
                /**
                 * 即使店铺不存在  购物车项可以正常添加
                 */
                shopGoodsVO.setShopName(shopDO.getName());
            }
            List<GoodsItem> cartItemList = new ArrayList<>(cartDOList.size());
            for (CartDO cartDO : entry.getValue()){
                GoodsSkuDO goodsSkuDO = goodsSkuDOMap.get(cartDO.getSkuId());
                GoodsItem goodsItemVO = new GoodsItem();
                goodsItemVO.setSkuId(cartDO.getSkuId());
                goodsItemVO.setCartId(cartDO.getId());
                goodsItemVO.setGoodsName(goodsDOMap.get(cartDO.getGoodsId()).getName());
                goodsItemVO.setQuantity(cartDO.getQuantity());
                goodsItemVO.setPrice(goodsSkuDO.getPrice());
                goodsItemVO.setSkuCode(goodsSkuDO.getSkuCode());
                goodsItemVO.setSkuName(goodsSkuDO.getAttributeDetail());
                goodsItemVO.setGoodsId(cartDO.getGoodsId());
                goodsItemVO.setGoodsTitle(goodsDOMap.get(cartDO.getGoodsId()).getTitle());
                goodsItemVO.setSkuPictureAddress(goodsSkuDO.getSkuPictureAddress());
                goodsItemVO.setGoodsVersion(goodsDOMap.get(cartDO.getGoodsId()).getVersion());
                goodsItemVO.setSkuVersion(goodsSkuDO.getVersion());
                goodsItemVO.setTenantId(goodsSkuDO.getTenantId());
                cartItemList.add(goodsItemVO);
                //累加总金额
                totalPrice = BigDecimalUtil.add(totalPrice, GoodsServiceUtils.calculateTotalAmount(goodsSkuDO.getPrice(),
                        cartDO.getQuantity()));
            }
            shopGoodsVO.setCartItemList(cartItemList);
            goodsVOList.add(shopGoodsVO);
        }
        ValidateCartItemVO validateCartItemVO = new ValidateCartItemVO();
        validateCartItemVO.setTotalPrice(totalPrice);
        validateCartItemVO.setGoodsVOList(goodsVOList);

        return validateCartItemVO;
    }

    @Override
    public ClearCartVO queryCartGoodsInfo(ClearCartDTO dto) {

        ValidateCartDTO dto1   = new ValidateCartDTO();
        dto1.setCartIds(dto.getCartIds());
        dto1.setUserId(dto.getUserId());
        ValidateCartItemVO validateCartItemVO = validateCartItem(dto1);
        ClearCartVO clearCartVO = new ClearCartVO();
        clearCartVO.setGoodsVOList(validateCartItemVO.getGoodsVOList());
        clearCartVO.setTotalPrice(validateCartItemVO.getTotalPrice());

        return clearCartVO;
    }


    @HmilyTCC(confirmMethod = "confirmClearCart", cancelMethod = "cancelClearCart")
    @Override
    public void clearCart(ClearCartDTO dto) {

        List<Long> cartIds = dto.getCartIds();
        Long userId = dto.getUserId();

        List<CartDO> cartDOList = this.lambdaQuery()
                .eq(CartDO::getUserId, userId)
                .in(CartDO::getId, cartIds)
                .list();
        if (cartDOList.isEmpty()){
            log.warn("购物车项不存在");
            throw  new BizException("购物车项不存在");
        }
        if (cartIds.size() != cartDOList.size()){
            log.warn("部分购物车项不存在");
            throw  new BizException("部分购物车项不存在");
        }


    }

    /**
     * 暂时不删除 方便测试
     * @param dto
     */
    public void  confirmClearCart(ClearCartDTO dto){

        log.info("清空购物车数据:{}", dto);

//         this.lambdaUpdate()
//                .eq(CartDO::getUserId, dto.getUserId())
//                .in(CartDO::getId, dto.getCartIds())
//                .remove();

    }

    public void  cancelClearCart(ClearCartDTO dto){
        log.info("cancelClearCart");


    }



}
