package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.dto.CartAddDTO;
import com.lanf.goods.model.dto.ChangeCartQuantityDTO;
import com.lanf.goods.model.entity.CartDO;
import com.lanf.goods.model.vo.CartGoodsVO;
import com.lanf.goods.model.vo.EmptyCartVO;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
public interface ICartService extends IService<CartDO> {

    void  cartAdd(CartAddDTO dto);

   void changeCartQuantity(ChangeCartQuantityDTO dto);


    /**
     * 清空购物车
     *
     *
     */
    EmptyCartVO emptyCart(Set<Long> cartIdLis);

    List<CartGoodsVO> cartList();


}
