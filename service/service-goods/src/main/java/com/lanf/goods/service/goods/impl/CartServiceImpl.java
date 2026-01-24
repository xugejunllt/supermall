package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.goods.mapper.CartMapper;
import com.lanf.goods.model.dto.CartAddDTO;
import com.lanf.goods.model.dto.DecrementCartItemQuantityDTO;
import com.lanf.goods.model.dto.IncrementCartItemQuantityDTO;
import com.lanf.goods.model.entity.CartDO;
import com.lanf.goods.model.entity.GoodsDO;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.vo.CartGoodsVO;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.utils.GoodsServiceUtils;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.security.utils.UserIdContext;
import com.lanf.security.utils.UserUtils;
import com.lanf.system.api.SystemService;
import com.lanf.system.model.vo.ShopVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private SystemService systemService;
    @Autowired
    private IGoodsSkuService goodsSkuService;
    @Autowired
    private IGoodsService goodsService;

    @DistributedLock(key = "#dto.skuCode")
    @Override
    public void cartAdd(CartAddDTO dto) {

        String skuCode = dto.getSkuCode();
        // 验证库存和SKU信息
        validateCartItem(dto, skuCode);
        // 处理购物车项（创建或更新）
        getOrCreateCartDO(dto);

    }
    /**
     * 验证商品库存和SKU信息
     */
    private void validateCartItem(CartAddDTO dto, String skuCode) {
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

    /**
     * 获取或创建购物车项
     */
    private void getOrCreateCartDO(CartAddDTO dto) {

        // 获取SKU和商品信息
        GoodsSkuDO goodsSkuDO = goodsSkuService.lambdaQuery()
                .eq(GoodsSkuDO::getSkuCode, dto.getSkuCode()).one();
        Long goodsId = goodsSkuDO.getGoodsId();
        GoodsDO goodsDO = goodsService.getById(goodsId);
        Long userId = UserIdContext.getUserId();
        Long skuId = goodsSkuDO.getId();

        // 查询是否已存在相同的购物车项
        CartDO existingCartDO = this.lambdaQuery()
                .eq(CartDO::getSkuId, skuId)
                .eq(CartDO::getUserId, userId)
                .one();

        if (existingCartDO == null) {
            createNewCartDO(dto, userId, goodsSkuDO, goodsDO, skuId);
        } else {
            updateExistingCartDO(dto, existingCartDO);
        }
    }

    /**
     * 创建新的购物车项
     */
    private void createNewCartDO(CartAddDTO dto, Long userId, GoodsSkuDO goodsSkuDO,
                                 GoodsDO goodsDO, Long skuId) {
        CartDO cartDO = new CartDO();
        cartDO.setUserId(userId);
        cartDO.setShopId(goodsDO.getShopId());
        cartDO.setSkuId(skuId);
        cartDO.setGoodsId(goodsSkuDO.getGoodsId());
        cartDO.setQuantity(dto.getQuantity());
        cartDO.setSkuCode(dto.getSkuCode());
        cartDO.setVersion(1L);
        this.save(cartDO);
    }

    /**
     * 更新现有购物车项
     */
    private void updateExistingCartDO(CartAddDTO dto, CartDO existingCartDO) {
        Long version = existingCartDO.getVersion();
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, existingCartDO.getId())
                .eq(CartDO::getVersion, version)
                .set(CartDO::getQuantity, dto.getQuantity() + existingCartDO.getQuantity())
                .set(CartDO::getVersion, version + 1)
                .update();
        if (!update) {
            throw new BizException("更新失败");
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
        StockDO stockDO = GoodsServiceUtils.findStockDO(cartDO.getSkuCode());

        if (dto.getIncrementQuantity() > stockDO.getUsableStock()) {
            log.warn("商品库存不足,剩余库存[{}]", stockDO.getUsableStock());
            throw new BizException("商品库存不足");
        }

        Long version = cartDO.getVersion();
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, cartDO.getId())
                .eq(CartDO::getVersion, version)
                .set(CartDO::getQuantity, dto.getIncrementQuantity() + cartDO.getQuantity())
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


    @Override
    public List<CartGoodsVO> cartList() {

        Long userId = UserUtils.getUserId();

        List<CartDO> cartDOList = this.lambdaQuery().eq(CartDO::getUserId, userId).orderByDesc(BaseEntity::getUpdateTime).list();

        if (cartDOList.isEmpty()) {

            return new ArrayList<>();
        }

        List<Long> goodsIdList = cartDOList.stream().map(CartDO::getGoodsId).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);

        List<GoodsDO> goodsDOList = goodsService.lambdaQuery().in(GoodsDO::getId, goodsIdList).list();
        Map<Long, GoodsDO> goodsMap = goodsDOList.stream()
                .collect(Collectors.toMap(GoodsDO::getId, Function.identity()));

        List<Long> shopIdList = cartDOList.stream().map(CartDO::getShopId).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);
        List<ShopVO> shopVOList = systemService.shopQuery(shopIdList).getData();
        if (shopVOList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, ShopVO> shopVOMap = shopVOList.stream()
                .collect(Collectors.toMap(ShopVO::getId, Function.identity()));
        //sku
        List<Long> skuIdList = cartDOList.stream().map(CartDO::getSkuId).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);

        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().in(BaseEntity::getId, skuIdList).list();
        Map<Long, GoodsSkuDO> goodsSkuMap = goodsSkuDOList.stream()
                .collect(Collectors.toMap(GoodsSkuDO::getId, Function.identity()));

        Map<Long, List<CartDO>> shopMap = new HashMap<>();
        for (CartDO a : cartDOList) {

            Long shopId = a.getShopId();
            List<CartDO> cartDOS = shopMap.get(shopId);
            if (cartDOS == null) {
                cartDOS = new ArrayList<>();
                shopMap.put(shopId, cartDOS);

            }
            cartDOS.add(a);

        }
        List<CartGoodsVO> result = new ArrayList<>();

        for (Long shopId : shopMap.keySet()) {

            List<CartDO> cartDOList1 = shopMap.get(shopId);
            CartDO a = cartDOList1.get(0);

            List<CartGoodsVO> cartGoodsVOList = new ArrayList<>(cartDOList1.size());
            String shopName = shopVOMap.get(a.getShopId()).getName();

            for (int i = 0; i < cartDOList1.size(); i++) {

                CartDO b = cartDOList1.get(i);
                GoodsDO goodsDO = goodsMap.get(b.getGoodsId());
                GoodsSkuDO goodsSkuDO = goodsSkuMap.get(b.getSkuId());

                CartGoodsVO cartGoodsVO = new CartGoodsVO();
                cartGoodsVO.setId(b.getId());
                cartGoodsVO.setGoodsId(b.getGoodsId());
                cartGoodsVO.setName(goodsDO.getName());
                cartGoodsVO.setSkuPictureAddress(goodsSkuDO.getSkuPictureAddress());
                cartGoodsVO.setPrice(goodsSkuDO.getPrice());
                cartGoodsVO.setQuantity(b.getQuantity());
                cartGoodsVO.setSkuName(goodsSkuDO.getSkuName());
                cartGoodsVO.setShopId(shopId);
                if (i == 0) {

                    cartGoodsVO.setShopName(shopName);
                }
                if (i == cartDOList1.size() - 1) {
                    cartGoodsVO.setLast(true);
                }
                cartGoodsVOList.add(cartGoodsVO);

            }
            result.addAll(cartGoodsVOList);
        }

        return result;
    }
}
