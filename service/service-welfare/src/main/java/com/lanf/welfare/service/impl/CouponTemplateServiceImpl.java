package com.lanf.welfare.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.*;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.exception.BizException;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.cache.constant.RedisCacheConstants;
import com.lanf.cache.service.RedisCache;
import com.lanf.rocketmq.model.message.DeductCouponTemplateCountMsg;
import com.lanf.security.utils.AdminSessionCache;
import com.lanf.system.api.SystemService;
import com.lanf.welfare.mapper.CouponTemplateMapper;
import com.lanf.welfare.model.bo.*;
import com.lanf.welfare.model.dto.CouponTemplateAddDTO;
import com.lanf.welfare.model.dto.CouponTemplateRevokeDTO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.entity.CouponTemplateHistoryDO;
import com.lanf.welfare.model.entity.CouponTemplateRevokeDO;
import com.lanf.welfare.model.enums.CouponPurpose;
import com.lanf.welfare.model.enums.CouponTemplateStatus;
import com.lanf.welfare.model.enums.CouponType;
import com.lanf.welfare.model.query.CouponTemplatePageQuery2;
import com.lanf.welfare.model.vo.CouponPurposeVO;
import com.lanf.welfare.model.vo.CouponTemplateListVO;
import com.lanf.welfare.service.ICouponTemplateHistoryService;
import com.lanf.welfare.service.ICouponTemplateRevokeService;
import com.lanf.welfare.service.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ICouponTemplateHistoryService couponTemplateHistoryService;

    @Autowired
    private ICouponTemplateRevokeService couponTemplateRevokeService;

    @Autowired
    private CouponCacheService couponCacheService;

    @DistributedLock(key = "#dto.adminUserId")//防止重复点击提交
    @Transactional
    @Override
    public void couponTemplateAdd(CouponTemplateAddDTO dto) {

        checkAdd(dto);
        CouponTemplateDO couponTemplate = new CouponTemplateDO();
        BeanCopyUtils.copy(dto, couponTemplate);
        //不同优惠卷类型 不同的使用条件
        String useCondition = buildUseCondition(dto);
        Long shopId = dto.getShopId();
        CouponPurpose byCode = CouponPurpose.getByCode(dto.getPurpose());

        if (!CouponPurpose.SHOP.equals(byCode)) {
            //如果不是店铺优惠卷，-1默认所有店铺都能使用
            shopId = -1L;
        }
        Long couponTemplateId = IdUtils.generateId();
        couponTemplate.setUseCondition(useCondition);
        couponTemplate.setShopId(shopId);
        couponTemplate.setRemainCount(dto.getTotalCount());
        //默认已发布
        couponTemplate.setStatus(CouponTemplateStatus.PUSH.getCode());
        couponTemplate.setVersion(1L);
        couponTemplate.setId(couponTemplateId);
        /**
         * 构建发布历史
         */
        CouponTemplateHistoryDO templateHistoryDO = BeanCopyUtils.copyBean(couponTemplate, CouponTemplateHistoryDO.class);
        templateHistoryDO.setCouponTemplateId(couponTemplateId);

        this.save(couponTemplate);
        couponTemplateHistoryService.save(templateHistoryDO);
    }


    private String buildUseCondition(CouponTemplateAddDTO dto) {

        Integer couponType = dto.getType();

        String useCondition = null;
        if (CouponType.FULL.getCode().equals(couponType)) {

            useCondition = JsonUtils.toJsonString(dto.getFullDiscountUseCondition());
        }
        if (CouponType.FIXED.getCode().equals(couponType)) {

            useCondition = JsonUtils.toJsonString(dto.getNoConditionUseCondition());

        }
        if (CouponType.DISCOUNT.getCode().equals(couponType)) {
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
        if (!platformTenant()) {
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
        if (platformTenant()) {
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
    public PageResult<CouponTemplateDO> couponTemplatePage(CouponTemplatePageQuery2 query) {
        IPage<CouponTemplateDO> page = this.lambdaQuery().
                orderByDesc(BaseEntity::getUpdateTime).
                page(PageResult.buildIPage(query, CouponTemplateDO.class));

        return PageResult.toPageResult(page);
    }

    @Override
    public List<CouponTemplateListVO> listShopCouponTemplate(Long shopId) {


        List<CacheCouponTemplateListBO> listVOList = buildCouponTemplateList( shopId);
        //过滤数量不足的优惠卷
        filterNotCount(shopId, listVOList);
        if (IStringUtils.isEmpty(listVOList)) {
            return new ArrayList<>();
        }
        //过滤领取时间已结束的优惠卷
        filterEnded(  listVOList);
        if (IStringUtils.isEmpty(listVOList)) {
            return new ArrayList<>();
        }
//        //过滤已作废的优惠卷
//        filterRevoke(listVOList);
//        if (IStringUtils.isEmpty(listVOList)) {
//            return new ArrayList<>();
//        }
        return BeanCopyUtils.copyBeanList(listVOList, CouponTemplateListVO.class);
    }

    /**
     * 过滤已作废的优惠卷模板
     *
     */
    private void filterRevoke(List<CacheCouponTemplateListBO> listVOList) {


        Set<Long> cacheSet = redisCache.getCacheSet(RedisCacheConstants.COUPON_REVOKE);
        if (IStringUtils.isEmpty(cacheSet)){
            List<CouponTemplateRevokeDO> templateRevokeDOS = loadDBCouponTemplateRevokeDO();
            if (IStringUtils.isEmpty(templateRevokeDOS)){
                return;
            }
            Set<Long> templateIdSet = templateRevokeDOS.stream().
                    map(CouponTemplateRevokeDO::getCouponTemplateId).
                    collect(Collectors.toSet());
            couponCacheService.setRevokeCouponCache(templateIdSet);

        }
        Set<Long> templateIdSet =  couponCacheService.getRevokeCouponCache();
        //过滤正在废弃的优惠卷模板
        listVOList.removeIf(next -> {
            boolean contains = templateIdSet.contains(next.getId());
            if (contains ){
                log.info("正在废弃的优惠券:[{}]",next.getId());
            }
            return contains ;
        } );
    }

    private List<CouponTemplateRevokeDO> loadDBCouponTemplateRevokeDO() {

       return couponTemplateRevokeService.lambdaQuery().
                eq(CouponTemplateRevokeDO::getStatus, 0).list();

    }



    /**
     * 过滤超过领取时间优惠卷
     *
     */
    private void filterEnded( List<CacheCouponTemplateListBO> listVOList){

        Iterator<CacheCouponTemplateListBO> iterator = listVOList.iterator();

        while (iterator.hasNext()) {

            CacheCouponTemplateListBO next = iterator.next();
            if ( System.currentTimeMillis() > next.getReceiveEndTime().getTime()){
                log.info("优惠券已结束:[{}]",next.getId());
                iterator.remove();
            }

        }
    }


    private List<CacheCouponTemplateListBO> buildCouponTemplateList(Long shopId) {
        List<CacheCouponTemplateListBO> cache = couponCacheService.getShopCouponCache(shopId);
        List<CacheCouponTemplateListBO> listVOList = null;
        if (cache == null) {

            listVOList = loadDBCouponTemplateList(shopId);
            if (IStringUtils.isEmpty(listVOList)) {

                return new ArrayList<>();
            }
            couponCacheService.setShopCouponCache(shopId, listVOList);
        }
        listVOList = couponCacheService.getShopCouponCache(shopId);

        return listVOList;
    }


    /**
     * 过滤数量不足的优惠卷
     */
    private void filterNotCount(Long shopId,
                                          List<CacheCouponTemplateListBO> listVOList) {

        /**
         * 过滤 优惠卷剩余数量不足的
         */

        // key 优惠卷id value:剩余数量
        Map<String, String> remainCountCacheMap = buildShopCouponRemainCount( shopId);
        Iterator<CacheCouponTemplateListBO> iterator = listVOList.iterator();

        while (iterator.hasNext()) {

            CacheCouponTemplateListBO next = iterator.next();
            Long id = next.getId();
            String remainCounts = remainCountCacheMap.get(id.toString());
            if (remainCounts == null) {
                //告警
                iterator.remove();
                log.error("优惠卷剩余数量缓存为空[{}]", id);
            } else {
                int remainCount = Integer.parseInt(remainCounts);
                if (remainCount < 1) {
                    //剩余数量小于1 不展示
                    log.info("优惠卷剩余数量小于1[{}]", id);
                    iterator.remove();
                }
            }


        }
    }



    public Map<String, String> buildShopCouponRemainCount(Long shopId) {
        // key:优惠卷id value:剩余数量
        Map<String, String> remainCountCache = couponCacheService.getShopCouponRemainCountCache(shopId);
        if (remainCountCache.isEmpty()) {

            List<CacheCouponTemplateListBO> couponTemplateListBOList = loadDBCouponTemplateList(shopId);

            if (IStringUtils.isEmpty(couponTemplateListBOList)) {
                return new HashMap<>();
            }
            List<ShopCouponRemainCountCacheBO> couponRemainCountCacheBOList = couponTemplateListBOList.stream()
                    .map(template -> {
                        ShopCouponRemainCountCacheBO bo = new ShopCouponRemainCountCacheBO();
                        bo.setCouponTemplateId(template.getId());
                        bo.setRemainCount(template.getRemainCount());
                        return bo;
                    })
                    .collect(Collectors.toList());

            couponCacheService.setShopCouponRemainCountCache(shopId, couponRemainCountCacheBOList);
            remainCountCache = couponCacheService.getShopCouponRemainCountCache(shopId);

        }
        // key 优惠卷id value:剩余数量
        return remainCountCache;
    }



    private List<CacheCouponTemplateListBO> loadDBCouponTemplateList(Long shopId) {

        log.info("从DB加载优惠券列表开始");
        List<CouponTemplateDO> templateDOList = this.lambdaQuery()
                //领取结束时间大于当前时间
                .gt(CouponTemplateDO::getReceiveEndTime, new Date())
                .gt(CouponTemplateDO::getRemainCount, 0)
                .eq(CouponTemplateDO::getStatus, CouponTemplateStatus.PUSH.getCode())
                .eq(CouponTemplateDO::getShopId, shopId)
                .eq(CouponTemplateDO::getCouponType, CouponPurpose.SHOP.getCode())
                .list();
        if (IStringUtils.isEmpty(templateDOList)) {
            log.info("DB数据为空");
            return new ArrayList<>();
        }
        List<CacheCouponTemplateListBO> couponTemplateListBOList = new ArrayList<>();
        for (CouponTemplateDO couponTemplateDO : templateDOList) {

            Integer type = couponTemplateDO.getType();
            String useCondition = couponTemplateDO.getUseCondition();
            CacheCouponTemplateListBO cacheCouponTemplateListBO = BeanCopyUtils.copyBean(couponTemplateDO, CacheCouponTemplateListBO.class);

            if (CouponType.DISCOUNT.getCode().equals(type)) {
                DiscountUseConditionBO conditionBO = JsonUtils.toObject(useCondition, DiscountUseConditionBO.class);
                cacheCouponTemplateListBO.setDiscountUseCondition(conditionBO);
            }
            if (CouponType.FULL.getCode().equals(type)) {
                FullDiscountUseConditionBO conditionBO = JsonUtils.toObject(useCondition, FullDiscountUseConditionBO.class);
                cacheCouponTemplateListBO.setFullDiscountUseCondition(conditionBO);
            }
            if (CouponType.FIXED.getCode().equals(type)) {
                NoConditionUseConditionBO conditionBO = JsonUtils.toObject(useCondition, NoConditionUseConditionBO.class);
                cacheCouponTemplateListBO.setNoConditionUseCondition(conditionBO);
            }
            couponTemplateListBOList.add(cacheCouponTemplateListBO);
        }
        return couponTemplateListBOList;

    }

    @Transactional
    @Override
    public void couponTemplateRevoke(CouponTemplateRevokeDTO dto) {

        Long couponTemplateId = dto.getCouponTemplateId();
        CouponTemplateDO templateDO = this.getById(couponTemplateId);
        if (templateDO == null) {
            throw new BizException("数据不存在");
        }
        if (!CouponTemplateStatus.PUSH.getCode().equals(templateDO.getStatus())) {

            throw new BizException("状态异常");
        }
        Long version = templateDO.getVersion();
        Long updateVersion = version + 1;
        //构建历史记录
        CouponTemplateHistoryDO templateHistoryDO = BeanCopyUtils.copyBean(templateDO, CouponTemplateHistoryDO.class);
        templateHistoryDO.setCouponTemplateId(couponTemplateId);
        templateHistoryDO.setVersion(updateVersion);
        //构建 CouponTemplateRevokeDO
        CouponTemplateRevokeDO couponTemplateRevokeDO = new CouponTemplateRevokeDO();
        couponTemplateRevokeDO.setCouponTemplateId(couponTemplateId);
        couponTemplateRevokeDO.setStatus(0);
        couponTemplateRevokeDO.setCouponTemplateVersion(updateVersion);
        /**
         * 保存DB
         */
        boolean update = this.lambdaUpdate().
                eq(CouponTemplateDO::getId, couponTemplateId)
                .eq(CouponTemplateDO::getVersion, version)
                .set(CouponTemplateDO::getStatus, CouponTemplateStatus.REVOKE.getCode())
                .set(CouponTemplateDO::getVersion, updateVersion).update();
        if (!update) {
            throw new BizException("更新失败");

        }
        //copy把id复制过来了 所以删除id
        templateHistoryDO.setId(null);
        couponTemplateHistoryService.save(templateHistoryDO);
        couponTemplateRevokeService.save(couponTemplateRevokeDO);

        if (CouponPurpose.SHOP.getCode().equals(templateDO.getCouponType())) {
            //删除店铺优惠卷缓存
            couponCacheService.removeShopCouponCache(templateDO.getShopId());
            couponCacheService.removeShopCouponRemainCountCache(templateDO.getShopId());
        }

    }
    @Override
    public void deductCouponTemplateCount(DeductCouponTemplateCountMsg message) {

        Integer deductCount = message.getDeductCount();
        Long couponTemplateId = message.getCouponTemplateId();
        try {

        AutoIgnoreTenantContext.setAutoIgnoreMark(true);
        CouponTemplateDO templateDO = this.getById(couponTemplateId);
        Long version = templateDO.getVersion();
        Long updateVersion = version + 1;
        //护理多租户字段
        boolean update = this.lambdaUpdate()
                .eq(CouponTemplateDO::getId, couponTemplateId)
                .eq(CouponTemplateDO::getVersion, version)
                .ge(CouponTemplateDO::getRemainCount, deductCount)
                .set(CouponTemplateDO::getRemainCount, templateDO.getRemainCount() - deductCount)
                .set(CouponTemplateDO::getVersion, updateVersion)
                .update();

        if ( !update){
            log.error("更新失败");
            throw new BizException("更新失败");
        }
        } finally {
            AutoIgnoreTenantContext.removeAutoIgnoreMark();
        }
    }


}
