package com.lanf.welfare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.welfare.model.dto.AddCouponTemplateDTO;
import com.lanf.welfare.model.dto.RevokeCouponTemplateDTO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.query.CouponTemplateForAdminPageQuery;
import com.lanf.welfare.model.vo.CouponPurposeVO;
import com.lanf.welfare.model.vo.CouponTemplateListVO;
import com.lanf.welfare.model.vo.CouponTemplatePageVO;
import com.lanf.welfare.mq.message.DeductCouponTemplateCountMessage;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 优惠券模板 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
public interface ICouponTemplateService extends IService<CouponTemplateDO> {

    void addCouponTemplate(AddCouponTemplateDTO dto);

    List<CouponPurposeVO> couponPurposeListQuery();

    PageResult<CouponTemplatePageVO> couponTemplatePageQuery(CouponTemplateForAdminPageQuery query);

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
    void revokeCouponTemplate(RevokeCouponTemplateDTO dto);
    /**
     * 构建优惠卷模板剩余数量 缓存
     *
     */
     Map<String, String> buildShopCouponRemainCount(Long shopId);

    /**
     *
     * 扣减优惠卷模板数量
     *
     */
     void  deductCouponTemplateCount(DeductCouponTemplateCountMessage message);
}
