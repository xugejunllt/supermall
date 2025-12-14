package com.lanf.welfare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.welfare.model.dto.CouponTemplateAddDTO;
import com.lanf.welfare.model.dto.CouponTemplateRevokeDTO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.query.CouponTemplatePageQuery2;
import com.lanf.welfare.model.vo.CouponPurposeVO;
import com.lanf.welfare.model.vo.CouponTemplateListVO;

import java.util.List;

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

    List<CouponPurposeVO> couponPurposeList();

    PageResult<CouponTemplateDO> couponTemplatePage(CouponTemplatePageQuery2 query);

    /**
     * 查询店铺优惠卷模板列表
     *
     *
     */
    List<CouponTemplateListVO> listShopCouponTemplate(Long shopId);

    /**
     * 作废优惠卷模板
     *
     */
    void couponTemplateRevoke(CouponTemplateRevokeDTO dto);
}
