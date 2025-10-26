package com.lanf.welfare.service.biz.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.UserUtil;
import com.lanf.system.api.SystemService;
import com.lanf.system.model.vo.ShopVO;
import com.lanf.web.exception.BizException;
import com.lanf.welfare.mapper.CouponTemplateMapper;
import com.lanf.welfare.model.dto.CouponTemplateAddDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.query.CouponTemplatePageQuery;
import com.lanf.welfare.model.query.CouponTemplatePageQuery2;
import com.lanf.welfare.service.biz.ICouponTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 优惠券模板 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
@Service
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplateDO> implements ICouponTemplateService {

    @Autowired
    private SystemService systemService;
    @Override
    public void couponTemplateAdd(CouponTemplateAddDTO dto) {

        dto.setShopId(UserUtil.getUserInfo().getShopId());
        checkAdd( dto);
        CouponTemplateDO couponTemplate = new CouponTemplateDO();
        BeanCopyUtils.copy(dto,couponTemplate);
        this.save(couponTemplate);

    }



    private void  checkAdd(CouponTemplateAddDTO dto){

        //校验店铺是否存在
        List<ShopVO> shopVOList = systemService.shopQuery(Arrays.asList(dto.getShopId())).getData();
        if (shopVOList== null || shopVOList.isEmpty()){

            throw new BizException("店铺信息不存在");
        }
        /**
         *
         * 金额校验 -是否超过配置
         *
         */

        if (new Date().getTime()>dto.getEndTime().getTime()){
            throw new BizException("活动结束时间不能小于当前时间");
        }





    }

    @Override
    public PageResult<CouponTemplateDO> couponTemplatePage(CouponTemplatePageQuery query) {

        IPage<CouponTemplateDO> page = this.lambdaQuery().eq(CouponTemplateDO::getShopId,query.getShopId()).
                page(PageResult.buildIPage(query, CouponTemplateDO.class));

        return PageResult.toPageResult(page);
    }

    @Override
    public PageResult<CouponTemplateDO> couponTemplatePage2(CouponTemplatePageQuery2 query) {
        query.setShopId(UserUtil.getShopId());
        IPage<CouponTemplateDO> page = this.lambdaQuery().
                eq(CouponTemplateDO::getShopId,query.getShopId()).
                orderByDesc(BaseEntity::getId).
                page(PageResult.buildIPage(query, CouponTemplateDO.class));

        return PageResult.toPageResult(page);
    }
}
