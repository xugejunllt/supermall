package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.dto.CartAddDTO;
import com.lanf.goods.model.dto.DecrementCartItemQuantityDTO;
import com.lanf.goods.model.dto.IncrementCartItemQuantityDTO;
import com.lanf.goods.model.entity.CartDO;
import com.lanf.goods.model.vo.CartAddVO;
import com.lanf.goods.model.vo.CartListVO;
import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2024-06-13
 */
public interface ICartService extends IService<CartDO> {


    /**
     * 从商品详细页添加到购物车
     *
     */
    CartAddVO cartAdd(CartAddDTO dto);

    /**
     * 从购物列表中增加商品数量
     *
     */
    void incrementCartItemQuantity(IncrementCartItemQuantityDTO dto);

    /**
     * 从购物列表中减少商品数量
     *
     */
    void decrementCartItemQuantity(DecrementCartItemQuantityDTO dto);

    /**
     * 购物车列表
     *
     */
    PageResult<CartListVO> listCart(PageQuery query);



}
