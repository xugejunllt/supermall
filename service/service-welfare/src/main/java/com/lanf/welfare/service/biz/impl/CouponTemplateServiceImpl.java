package com.lanf.welfare.service.biz.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.exception.BizException;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.AdminSessionCache;
import com.lanf.system.api.SystemService;
import com.lanf.welfare.mapper.CouponTemplateMapper;
import com.lanf.welfare.model.bo.DiscountUseConditionBO;
import com.lanf.welfare.model.bo.FullDiscountUseConditionBO;
import com.lanf.welfare.model.bo.NoConditionUseConditionBO;
import com.lanf.welfare.model.dto.CouponTemplateAddDTO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.enums.CouponPurpose;
import com.lanf.welfare.model.enums.CouponType;
import com.lanf.welfare.model.query.CouponTemplatePageQuery;
import com.lanf.welfare.model.query.CouponTemplatePageQuery2;
import com.lanf.welfare.model.vo.CouponPurposeVO;
import com.lanf.welfare.service.biz.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 优惠券模板 服务实现类
 * </p>
 *
 * @author
 * @since 2024-08-01
 */
@Slf4j
@Service
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplateDO> implements ICouponTemplateService {

    @Autowired
    private SystemService systemService;


    @DistributedLock(key = "#dto.adminUserId")//防止重复点击提交
    @Override
    public void couponTemplateAdd(CouponTemplateAddDTO dto) {

        checkAdd(dto);
        CouponTemplateDO couponTemplate = new CouponTemplateDO();
        BeanCopyUtils.copy(dto, couponTemplate);
        //不同优惠卷类型 不同的使用条件
        String useCondition = buildUseCondition( dto);
        Long shopId = dto.getShopId();
        CouponPurpose byCode = CouponPurpose.getByCode(dto.getPurpose());

        if ( !CouponPurpose.SHOP.equals(byCode)) {
            //如果不是店铺优惠卷，-1默认所有店铺都能使用
            shopId = -1L;
        }
        couponTemplate.setUseCondition(useCondition);
        couponTemplate.setShopId(shopId);
        couponTemplate.setExpire(0);
        couponTemplate.setRemainCount(dto.getTotalCount());
        this.save(couponTemplate);

    }


    private String buildUseCondition( CouponTemplateAddDTO dto) {

        Integer couponType = dto.getType();

        String useCondition = null;
        if (CouponType.FULL.getCode().equals(couponType) ) {

            useCondition = JsonUtils.toJsonString(dto.getFullDiscountUseCondition());
        }
        if (CouponType.FIXED.getCode().equals(couponType) ) {

            useCondition = JsonUtils.toJsonString(dto.getNoConditionUseCondition());

        }
        if (CouponType.DISCOUNT.getCode().equals(couponType) ) {
            useCondition = JsonUtils.toJsonString(dto.getDiscountUseCondition());

        }
        return useCondition;

    }
    private void checkAdd(CouponTemplateAddDTO dto) {

        /**
         * 自动校验不能添加 手动校验
         */
        if (!CouponType.include(dto.getType())) {
            log.info("不支持的优惠卷类型");
            throw new BizException("不支持的优惠卷类型");
        }
        if (!CouponPurpose.include(dto.getPurpose())) {
            log.info("不支持的优惠卷用途");
            throw new BizException("不支持的优惠卷用途");
        }

        Integer purpose = dto.getPurpose();
        CouponPurpose byCode = CouponPurpose.getByCode(purpose);
        if (CouponPurpose.SHOP.equals(byCode) && dto.getShopId() == null) {
            log.info("店铺id不能为空");
            throw new BizException("店铺id不能为空");

        }
        /**
         * 优惠卷用途权限校验
         */
        if ( !platformTenant()) {
            if (!CouponPurpose.notPlatformCouponPurpose.contains(byCode)) {
                log.info("没有权限");
                throw new BizException("没有权限");
            }
        }

        /**
         * 必须校验 否则序列化时候会报错
         */
        Integer couponType = dto.getType();
        FullDiscountUseConditionBO fullDiscountUseCondition = dto.getFullDiscountUseCondition();
        DiscountUseConditionBO discountUseCondition = dto.getDiscountUseCondition();
        NoConditionUseConditionBO noConditionUseCondition = dto.getNoConditionUseCondition();
        if (CouponType.FULL.getCode().equals(couponType) && fullDiscountUseCondition == null) {

            log.info("使用条件不能为空");
            throw new BizException("使用条件不能为空");
        }
        if (CouponType.FIXED.getCode().equals(couponType) && noConditionUseCondition == null) {

            log.info("使用条件不能为空");
            throw new BizException("使用条件不能为空");

        }
        if (CouponType.DISCOUNT.getCode().equals(couponType) && discountUseCondition == null) {

            log.info("优惠券使用条件不能为空");
            throw new BizException("优惠券使用条件不能为空");
        }

        if (dto.getReceiveEndTime().getTime() <
                dto.getReceiveStartTime().getTime()) {
            log.info("领取时间错误");
            throw new BizException("领取时间错误");
        }
        if (dto.getUseStartTime().getTime() <
                dto.getReceiveStartTime().getTime()) {
            log.info("使用开始时间错误");
            throw new BizException("使用开始时间错误");
        }
        if (dto.getUseEndTime().getTime() <
                dto.getReceiveEndTime().getTime()) {
            log.info("使用结束时间错误");
            throw new BizException("使用结束时间错误");
        }
        if (dto.getUseEndTime().getTime() <
                dto.getUseStartTime().getTime()) {
            log.info("使用结束时间错误");
            throw new BizException("使用结束时间错误");
        }


    }

    @Override
    public List<CouponPurposeVO> couponPurposeList() {

        /**
         * 过滤
         *
         */
        List<CouponPurpose> purposeList;
        if ( platformTenant()){
             log.info("平台管理员");
            purposeList = Arrays.asList(CouponPurpose.values());

        } else {
            //非平台租户、
            log.info("非平台租户");
            purposeList = CouponPurpose.notPlatformCouponPurpose;
        }
        return purposeList.stream()
                .map(purpose -> {
                    CouponPurposeVO vo = new CouponPurposeVO();
                    vo.setCode(purpose.getCode());
                    vo.setName(purpose.getName());
                    return vo;
                })
                .collect(Collectors.toList());

    }

    private boolean platformTenant() {
        String tenantCode = AdminSessionCache.getSysUser().getTenantCode();
        return Constants.ADMIN_TENANT_CODE.equals(tenantCode);
    }
    @Override
    public PageResult<CouponTemplateDO> couponTemplatePage(CouponTemplatePageQuery query) {

        IPage<CouponTemplateDO> page = this.lambdaQuery().eq(CouponTemplateDO::getShopId, query.getShopId()).
                page(PageResult.buildIPage(query, CouponTemplateDO.class));

        return PageResult.toPageResult(page);
    }

    @Override
    public PageResult<CouponTemplateDO> couponTemplatePage2(CouponTemplatePageQuery2 query) {
        IPage<CouponTemplateDO> page = this.lambdaQuery().
                orderByDesc(BaseEntity::getUpdateTime).
                page(PageResult.buildIPage(query, CouponTemplateDO.class));

        return PageResult.toPageResult(page);
    }
}
