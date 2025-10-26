package com.lanf.welfare.service.biz;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;
import com.lanf.welfare.model.dto.CouponTemplateAddDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.query.CouponTemplatePageQuery;
import com.lanf.welfare.model.query.CouponTemplatePageQuery2;

/**
 * <p>
 * 优惠券模板 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
public interface ICouponTemplateService extends IService<CouponTemplateDO> {
    void couponTemplateAdd(CouponTemplateAddDTO dto);

    PageResult<CouponTemplateDO> couponTemplatePage(CouponTemplatePageQuery query);
    PageResult<CouponTemplateDO> couponTemplatePage2(CouponTemplatePageQuery2 query);
}
