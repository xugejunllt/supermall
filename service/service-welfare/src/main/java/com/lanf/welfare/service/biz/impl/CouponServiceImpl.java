package com.lanf.welfare.service.biz.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.security.utils.UserUtil;
import com.lanf.web.exception.BizException;
import com.lanf.welfare.mapper.CouponMapper;
import com.lanf.welfare.model.dto.ReceiveCouponDTO;
import com.lanf.welfare.model.dto.UseCouponDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.vo.CouponVO;
import com.lanf.welfare.model.vo.ShopCouponVO;
import com.lanf.welfare.model.vo.UseCouponVO;
import com.lanf.welfare.service.biz.ICouponService;
import com.lanf.welfare.service.biz.ICouponTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponDO> implements ICouponService {

    @Autowired
    private ICouponTemplateService couponTemplateService;


    @Override
    public void receiveCoupon(ReceiveCouponDTO dto) {

        checkReceive(dto);
        Long couponTemplateId = dto.getCouponTemplateId();
        CouponTemplateDO couponTemplate = couponTemplateService.getById(dto.getCouponTemplateId());
        CouponDO couponDO = new CouponDO();
        couponDO.setShopId(couponTemplate.getShopId());
        couponDO.setUserId(UserUtil.getUserId());
        couponDO.setTemplateId(couponTemplateId);
        couponDO.setEndTime(couponTemplate.getEndTime());
        couponDO.setUsed(0);
        this.save(couponDO);

    }



    private void  checkReceive(ReceiveCouponDTO dto){

        CouponTemplateDO couponTemplate = couponTemplateService.getById(dto.getCouponTemplateId());
        if (couponTemplate == null){

            throw new BizException("优惠券不存在");
        }
        if (new Date().getTime()>couponTemplate.getEndTime().getTime()){
            throw new BizException("优惠券过期");
        }
        Integer receiveCount = couponTemplate.getReceiveCount();
        List<CouponDO> couponDOList = this.lambdaQuery().eq(CouponDO::getTemplateId, couponTemplate.getId()).list();
        if (!couponDOList.isEmpty() && couponDOList.size()+1>receiveCount){
            throw new BizException("超过最大领取次数");
        }

    }
    @Override
    public List<ShopCouponVO> shopCouponList(Long shopId) {

        List<CouponDO> couponList = this.lambdaQuery().eq(CouponDO::getUserId, UserUtil.getUserId()).
                eq(CouponDO::getShopId, shopId).
                eq(CouponDO::getUsed, 0).list();
        if (couponList.isEmpty()){
            return  new ArrayList<>();
        }
        List<ShopCouponVO> shopCouponVOList = new ArrayList<>();
        couponList.forEach(a->{
            ShopCouponVO shopCouponVO = new ShopCouponVO();
            shopCouponVO.setCouponId(a.getId());
            shopCouponVO.setTemplateId(a.getTemplateId());
            shopCouponVOList.add(shopCouponVO);
        });

        List<Long> templateIdList = couponList.stream().map(CouponDO::getTemplateId).collect(Collectors.toList());

        List<CouponTemplateDO> templateDOList = couponTemplateService.lambdaQuery().in(BaseEntity::getId, templateIdList).list();
        Map<Long, CouponTemplateDO> couponTemplateMap = templateDOList.stream()
                .collect(Collectors.toMap(CouponTemplateDO::getId, Function.identity()));
        shopCouponVOList.forEach(a->{
            CouponTemplateDO couponTemplateDO = couponTemplateMap.get(a.getTemplateId());

            BeanCopyUtils.copy(couponTemplateDO,a);
        });


        return shopCouponVOList;
    }

    @Override
    public UseCouponVO useCoupon(UseCouponDTO dto) {

        useCouponCheck( dto);
        /**
         * 用用户id 保证该优惠券属于该用户的 即使前段couponId传错误
         */
        Long userId = dto.getUserId();
        Long couponId = dto.getCouponId();
        CouponDO couponDO = this.lambdaQuery().eq(BaseEntity::getId,couponId).
                eq(CouponDO::getUserId,userId).
                one();
        if (couponDO == null){
            throw new BizException("优惠券不存在");
        }
        CouponTemplateDO templateDO = couponTemplateService.lambdaQuery().eq(BaseEntity::getId, couponDO.getTemplateId()).one();
        boolean update = this.lambdaUpdate().
                eq(BaseEntity::getId, couponId).
                set(CouponDO::getUsed, 1).
                update();
        if (!update){
            throw new BizException("更新失败");
        }
        return new UseCouponVO(templateDO.getDiscountMoney(),templateDO.getShopId(),couponDO.getId());
    }



    private void useCouponCheck(UseCouponDTO dto){
        Long couponId = dto.getCouponId();
        BigDecimal payMoney = dto.getOrderMoney();
        Long userId = dto.getUserId();

        /**
         * 用用户id 保证该优惠券属于该用户的 即使前段couponId传错误
         */
        CouponDO couponDO = this.lambdaQuery().eq(BaseEntity::getId,couponId).
                eq(CouponDO::getUserId,userId).
                one();
        if (couponDO == null){

            throw new BizException("优惠券不存在");
        }
        Integer used = couponDO.getUsed();
        if (used ==1){
            throw new BizException("优惠券已使用");
        }
        Date endTime = couponDO.getEndTime();
        if (new Date().getTime()>endTime.getTime()){
            throw new BizException("优惠券已过期");
        }
        Long templateId = couponDO.getTemplateId();
        CouponTemplateDO templateDO = couponTemplateService.lambdaQuery().eq(BaseEntity::getId, templateId).one();
        if (templateDO == null){

            throw new BizException("优惠券模板不存在");
        }
        BigDecimal meetMoney = templateDO.getMeetMoney();
        if (BigDecimalUtils.compareTo(meetMoney,payMoney) == 1){
            throw new BizException("不满足使用条件");
        }


    }
    @Override
    public List<UseCouponVO> bathUseCoupon(List<UseCouponDTO> dtoList) {

        List<UseCouponVO> useCouponVOList = new ArrayList<>(dtoList.size());

        dtoList.forEach(a->{

            UseCouponVO useCouponVO = useCoupon(a);
            useCouponVOList.add(useCouponVO);
        });

        return useCouponVOList;
    }

    @Override
    public List<CouponVO> queryByIdSet(Set<Long> idSet) {

        List<CouponDO> couponDOList = this.lambdaQuery().in(BaseEntity::getId, idSet).list();
        if (couponDOList.isEmpty()){
            return new ArrayList<>();
        }
        return BeanCopyUtils.copyBeanList(couponDOList,CouponVO.class);
    }

}
