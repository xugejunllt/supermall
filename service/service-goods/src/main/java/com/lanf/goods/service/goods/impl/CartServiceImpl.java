package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.goods.mapper.CartMapper;
import com.lanf.goods.model.dto.CartAddDTO;
import com.lanf.goods.model.dto.ChangeCartQuantityDTO;
import com.lanf.goods.model.entity.CartDO;
import com.lanf.goods.model.entity.GoodsDO;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.goods.model.vo.CartGoodsVO;
import com.lanf.goods.model.vo.EmptyCartGoodsSkuVO;
import com.lanf.goods.model.vo.EmptyCartVO;
import com.lanf.goods.service.goods.ICartService;
import com.lanf.goods.service.goods.IGoodsService;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.security.utils.UserUtils;
import com.lanf.system.api.SystemService;
import com.lanf.system.model.vo.ShopVO;
import com.lanf.web.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, CartDO> implements ICartService {

    @Autowired
    private SystemService systemService;
    @Autowired
    private IGoodsSkuService goodsSkuService;
    @Autowired
    private IGoodsService goodsService;


    @Override
    public void cartAdd(CartAddDTO dto) {

        Long skuId = dto.getSkuId();
        ThreadLocalUtils.addIgnoreTableName(true);
        GoodsSkuDO goodsSkuDO = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getId, skuId).one();
        if (goodsSkuDO == null) {
            throw new BizException("商品不存在");
        }
        Long goodsId = goodsSkuDO.getGoodsId();
        ThreadLocalUtils.addIgnoreTableName(true);
        GoodsDO goodsDO = goodsService.getById(goodsId);


        Integer quantity = dto.getQuantity();
        if (quantity > goodsSkuDO.getStock()) {
            throw new BizException("商品库存不足");
        }
        CartDO cartDO1 = this.lambdaQuery().eq(CartDO::getSkuId, skuId).one();
        if (cartDO1 == null) {
            //新增
            CartDO cartDO = new CartDO();
            cartDO.setUserId(UserUtils.getUserId());
            cartDO.setShopId(goodsDO.getShopId());
            cartDO.setSkuId(skuId);
            cartDO.setQuantity(quantity);
            cartDO.setGoodsId(goodsSkuDO.getGoodsId());
            this.save(cartDO);
        } else {
            //累加更新
            boolean update = this.lambdaUpdate().
                    set(CartDO::getQuantity, quantity + cartDO1.getQuantity()).
                    eq(BaseEntity::getId, cartDO1.getId()).
                    update();
            if (!update) {
                throw new BizException("更新失败");
            }

        }


    }

    @Override
    public void changeCartQuantity(ChangeCartQuantityDTO dto) {

        CartDO cartDO = this.getById(dto.getId());
        if (cartDO == null) {
            throw new BizException("购物车不存在");
        }
        Integer quantity = dto.getChangeQuantity();

        boolean update = this.lambdaUpdate().
                //数量为0，删除购物车
                        set(quantity == 0, BaseEntity::getIsDeleted, 1).
                set(CartDO::getQuantity, quantity).
                eq(BaseEntity::getId, cartDO.getId()).
                update();
        if (!update) {
            throw new BizException("更新失败");
        }


    }

    /**
     * 清空购物车
     */
    @Transactional
    @Override
    public EmptyCartVO emptyCart(Set<Long> cartIdLis) {

        //校验购物车是否存在
        List<CartDO> cartDOList = this.lambdaQuery().in(BaseEntity::getId, cartIdLis).list();
        if (cartIdLis.size() != cartDOList.size()) {

            throw new BizException("购物车信息不存在");
        }

        Map<Long, CartDO> cartMap = cartDOList.stream()
                .collect(Collectors.toMap(CartDO::getSkuId, Function.identity()));

        List<Long> skuIdList = cartDOList.stream().map(CartDO::getSkuId).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);
        List<GoodsSkuDO> goodsSkuDOList = goodsSkuService.lambdaQuery().in(BaseEntity::getId, skuIdList).list();

        Map<Long, GoodsSkuDO> goodsSkuMap = goodsSkuDOList.stream()
                .collect(Collectors.toMap(GoodsSkuDO::getId, Function.identity()));
        //校验库存是否存在
        for (CartDO cd : cartDOList) {
            Long skuId = cd.getSkuId();
            GoodsSkuDO goodsSkuDO = goodsSkuMap.get(skuId);
            Integer quantity = cd.getQuantity();
            if (quantity > goodsSkuDO.getStock()) {
                throw new BizException("商品" + goodsSkuDO.getSkuCode() + "库存不足");
            }
        }
        //清空购物车
        this.removeByIds(cartIdLis);
        //扣减库存
        for (CartDO cd : cartDOList) {

            Long skuId = cd.getSkuId();
            GoodsSkuDO goodsSkuDO = goodsSkuMap.get(skuId);
            Integer quantity = cd.getQuantity();
            Integer surplusStock = goodsSkuDO.getStock() - quantity;
            ThreadLocalUtils.addIgnoreTableName(true);
            goodsSkuService.lambdaUpdate().set(GoodsSkuDO::getStock, surplusStock).
                    eq(BaseEntity::getId, goodsSkuDO.getId()).

                    update();
        }
        //构建返回信息
        List<Long> goodsIdList = goodsSkuDOList.stream().map(GoodsSkuDO::getGoodsId).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);
        List<GoodsDO> goodsDOList = goodsService.lambdaQuery().in(BaseEntity::getId, goodsIdList).list();

        Map<Long, GoodsDO> goodsMap = goodsDOList.stream()
                .collect(Collectors.toMap(GoodsDO::getId, Function.identity()));


        EmptyCartVO emptyCartVO = new EmptyCartVO();
        List<EmptyCartGoodsSkuVO> goodsSkuVOS = BeanCopyUtils.copyBeanList(goodsSkuDOList, EmptyCartGoodsSkuVO.class);
        emptyCartVO.setGoodsSkuVOList(goodsSkuVOS);

        goodsSkuVOS.forEach(a -> {

            CartDO cartDO = cartMap.get(a.getId());
            GoodsDO goodsDO = goodsMap.get(a.getGoodsId());
            a.setQuantity(cartDO.getQuantity());
            a.setShopId(goodsDO.getShopId());
            a.setGoodsName(goodsDO.getName());
            a.setGoodsTitle(goodsDO.getTitle());
        });

        return emptyCartVO;
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
                if (i == cartDOList1.size()-1){
                    cartGoodsVO.setLast(true);
                }
                cartGoodsVOList.add(cartGoodsVO);

            }
            result.addAll(cartGoodsVOList);
        }

        return result;
    }
}
