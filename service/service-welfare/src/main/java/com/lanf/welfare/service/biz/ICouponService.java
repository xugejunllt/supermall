package com.lanf.welfare.service.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.welfare.model.dto.ReceiveCouponDTO;
import com.lanf.welfare.model.dto.UseCouponDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.vo.CouponVO;
import com.lanf.welfare.model.vo.ShopCouponVO;
import com.lanf.welfare.model.vo.UseCouponVO;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 优惠券 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
public interface ICouponService extends IService<CouponDO> {

    /**
     * 领取优惠券
     *
     */
    void receiveCoupon(ReceiveCouponDTO dto);

    /**
     * 店铺可使用优惠券列表
     *
     *
     */
    List<ShopCouponVO> shopCouponList( Long shopId);

    /**
     * 使用优惠券
     *
     *
     */
    UseCouponVO useCoupon(UseCouponDTO dto);

    /**
     *
     *
     * 批量使用优惠券
     *
     */
    List<UseCouponVO> bathUseCoupon(List<UseCouponDTO> dtoList);

    List<CouponVO> queryByIdSet(Set<Long> idSet);
}
